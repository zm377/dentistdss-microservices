package press.mizhifei.dentist.reporting.analytics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import press.mizhifei.dentist.reporting.dto.AnalyticsQuery;
import press.mizhifei.dentist.reporting.dto.AnalyticsResult;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Analytics Engine
 * 
 * Core service for executing analytical queries and data aggregation.
 * Optimized for performance with read replicas and intelligent caching.
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsEngine {

    @Qualifier("replicaJdbcTemplate")
    private final JdbcTemplate replicaJdbcTemplate;

    @Qualifier("primaryJdbcTemplate")
    private final JdbcTemplate primaryJdbcTemplate;

    /**
     * Execute analytical query with caching
     */
    @Cacheable(value = "analyticsResults", key = "#query.cacheKey", cacheManager = "redisCacheManager")
    public AnalyticsResult executeQuery(AnalyticsQuery query) {
        log.debug("Executing analytics query: {}", query.getQueryId());
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Use read replica for analytical queries to avoid impacting OLTP performance
            JdbcTemplate jdbcTemplate = query.isReadOnly() ? replicaJdbcTemplate : primaryJdbcTemplate;
            
            List<Map<String, Object>> results = jdbcTemplate.queryForList(
                query.getSql(), 
                query.getParameters().toArray()
            );
            
            long executionTime = System.currentTimeMillis() - startTime;
            
            log.info("Analytics query {} executed in {}ms, returned {} rows", 
                    query.getQueryId(), executionTime, results.size());
            
            return AnalyticsResult.builder()
                    .queryId(query.getQueryId())
                    .data(results)
                    .recordCount(results.size())
                    .executionTimeMs(executionTime)
                    .executedAt(LocalDateTime.now())
                    .cached(false)
                    .build();
                    
        } catch (Exception e) {
            log.error("Error executing analytics query {}: {}", query.getQueryId(), e.getMessage(), e);
            throw new AnalyticsException("Failed to execute query: " + query.getQueryId(), e);
        }
    }

    /**
     * Execute patient no-show analytics
     */
    public AnalyticsResult getPatientNoShowAnalytics(Long clinicId, LocalDateTime startDate, LocalDateTime endDate) {
        String sql = """
            SELECT 
                DATE_TRUNC('day', a.appointment_date) as date,
                COUNT(*) as total_appointments,
                COUNT(CASE WHEN a.status = 'NO_SHOW' THEN 1 END) as no_shows,
                ROUND(
                    COUNT(CASE WHEN a.status = 'NO_SHOW' THEN 1 END) * 100.0 / COUNT(*), 2
                ) as no_show_percentage
            FROM appointments a
            WHERE a.clinic_id = ?
                AND a.appointment_date BETWEEN ? AND ?
                AND a.status IN ('COMPLETED', 'NO_SHOW', 'CANCELLED')
            GROUP BY DATE_TRUNC('day', a.appointment_date)
            ORDER BY date
            """;

        AnalyticsQuery query = AnalyticsQuery.builder()
                .queryId("patient_no_show_analytics")
                .sql(sql)
                .parameters(List.of(clinicId, startDate, endDate))
                .readOnly(true)
                .build();

        return executeQuery(query);
    }

    /**
     * Execute appointment utilization analytics
     */
    public AnalyticsResult getAppointmentUtilizationAnalytics(Long clinicId, LocalDateTime startDate, LocalDateTime endDate) {
        String sql = """
            WITH time_slots AS (
                SELECT 
                    DATE_TRUNC('hour', a.appointment_date) as hour_slot,
                    COUNT(*) as scheduled_appointments,
                    COUNT(CASE WHEN a.status = 'COMPLETED' THEN 1 END) as completed_appointments,
                    AVG(EXTRACT(EPOCH FROM (a.actual_end_time - a.actual_start_time))/60) as avg_duration_minutes
                FROM appointments a
                WHERE a.clinic_id = ?
                    AND a.appointment_date BETWEEN ? AND ?
                GROUP BY DATE_TRUNC('hour', a.appointment_date)
            ),
            capacity AS (
                SELECT 
                    cs.clinic_id,
                    cs.day_of_week,
                    cs.start_time,
                    cs.end_time,
                    cs.max_concurrent_appointments
                FROM clinic_schedules cs
                WHERE cs.clinic_id = ?
            )
            SELECT 
                ts.hour_slot,
                ts.scheduled_appointments,
                ts.completed_appointments,
                ts.avg_duration_minutes,
                COALESCE(c.max_concurrent_appointments, 1) as capacity,
                ROUND(
                    ts.scheduled_appointments * 100.0 / COALESCE(c.max_concurrent_appointments, 1), 2
                ) as utilization_percentage
            FROM time_slots ts
            LEFT JOIN capacity c ON c.clinic_id = ?
                AND EXTRACT(DOW FROM ts.hour_slot) = c.day_of_week
                AND EXTRACT(HOUR FROM ts.hour_slot) BETWEEN 
                    EXTRACT(HOUR FROM c.start_time) AND EXTRACT(HOUR FROM c.end_time)
            ORDER BY ts.hour_slot
            """;

        AnalyticsQuery query = AnalyticsQuery.builder()
                .queryId("appointment_utilization_analytics")
                .sql(sql)
                .parameters(List.of(clinicId, startDate, endDate, clinicId, clinicId))
                .readOnly(true)
                .build();

        return executeQuery(query);
    }

    /**
     * Execute revenue analytics
     */
    public AnalyticsResult getRevenueAnalytics(Long clinicId, LocalDateTime startDate, LocalDateTime endDate) {
        String sql = """
            SELECT 
                DATE_TRUNC('day', b.created_at) as date,
                COUNT(DISTINCT b.id) as total_bills,
                SUM(b.total_amount) as total_revenue,
                SUM(CASE WHEN b.status = 'PAID' THEN b.total_amount ELSE 0 END) as paid_revenue,
                SUM(CASE WHEN b.status = 'PENDING' THEN b.total_amount ELSE 0 END) as pending_revenue,
                AVG(b.total_amount) as avg_bill_amount,
                COUNT(DISTINCT b.patient_id) as unique_patients
            FROM bills b
            WHERE b.clinic_id = ?
                AND b.created_at BETWEEN ? AND ?
            GROUP BY DATE_TRUNC('day', b.created_at)
            ORDER BY date
            """;

        AnalyticsQuery query = AnalyticsQuery.builder()
                .queryId("revenue_analytics")
                .sql(sql)
                .parameters(List.of(clinicId, startDate, endDate))
                .readOnly(true)
                .build();

        return executeQuery(query);
    }

    /**
     * Execute treatment completion analytics
     */
    public AnalyticsResult getTreatmentCompletionAnalytics(Long clinicId, LocalDateTime startDate, LocalDateTime endDate) {
        String sql = """
            WITH treatment_plans AS (
                SELECT 
                    tp.id,
                    tp.patient_id,
                    tp.created_at,
                    tp.status,
                    COUNT(tpi.id) as total_items,
                    COUNT(CASE WHEN tpi.status = 'COMPLETED' THEN 1 END) as completed_items
                FROM treatment_plans tp
                LEFT JOIN treatment_plan_items tpi ON tp.id = tpi.treatment_plan_id
                WHERE tp.clinic_id = ?
                    AND tp.created_at BETWEEN ? AND ?
                GROUP BY tp.id, tp.patient_id, tp.created_at, tp.status
            )
            SELECT 
                DATE_TRUNC('week', tp.created_at) as week,
                COUNT(*) as total_plans,
                COUNT(CASE WHEN tp.status = 'COMPLETED' THEN 1 END) as completed_plans,
                ROUND(
                    COUNT(CASE WHEN tp.status = 'COMPLETED' THEN 1 END) * 100.0 / COUNT(*), 2
                ) as completion_percentage,
                AVG(CASE WHEN tp.total_items > 0 THEN tp.completed_items * 100.0 / tp.total_items END) as avg_item_completion_percentage
            FROM treatment_plans tp
            GROUP BY DATE_TRUNC('week', tp.created_at)
            ORDER BY week
            """;

        AnalyticsQuery query = AnalyticsQuery.builder()
                .queryId("treatment_completion_analytics")
                .sql(sql)
                .parameters(List.of(clinicId, startDate, endDate))
                .readOnly(true)
                .build();

        return executeQuery(query);
    }

    /**
     * Execute AI usage statistics
     */
    public AnalyticsResult getAIUsageStatistics(Long clinicId, LocalDateTime startDate, LocalDateTime endDate) {
        String sql = """
            SELECT 
                cl.agent_type,
                cl.interaction_type,
                DATE_TRUNC('day', cl.timestamp) as date,
                COUNT(*) as interaction_count,
                AVG(cl.token_usage->>'totalTokens')::numeric as avg_tokens,
                SUM((cl.token_usage->>'totalTokens')::numeric) as total_tokens,
                AVG(cl.token_usage->>'responseTimeMs')::numeric as avg_response_time_ms,
                COUNT(CASE WHEN cl.phi_redaction->>'phiDetected' = 'true' THEN 1 END) as phi_redactions
            FROM chat_logs cl
            WHERE (? IS NULL OR cl.clinic_id = ?)
                AND cl.timestamp BETWEEN ? AND ?
            GROUP BY cl.agent_type, cl.interaction_type, DATE_TRUNC('day', cl.timestamp)
            ORDER BY date, cl.agent_type, cl.interaction_type
            """;

        AnalyticsQuery query = AnalyticsQuery.builder()
                .queryId("ai_usage_statistics")
                .sql(sql)
                .parameters(List.of(clinicId, clinicId, startDate, endDate))
                .readOnly(true)
                .build();

        return executeQuery(query);
    }

    /**
     * Custom exception for analytics errors
     */
    public static class AnalyticsException extends RuntimeException {
        public AnalyticsException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
