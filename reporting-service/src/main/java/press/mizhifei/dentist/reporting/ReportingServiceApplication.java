package press.mizhifei.dentist.reporting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Reporting Service Application
 * 
 * Advanced analytics and reporting microservice for DentistDSS that provides:
 * - Comprehensive data analytics and aggregation
 * - Multi-format report generation (PDF, Excel, CSV)
 * - Automated scheduling and email delivery
 * - Real-time and historical data analysis
 * - Performance-optimized queries with read replicas
 * - HIPAA-compliant data handling and anonymization
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableCaching
@EnableAsync
@EnableScheduling
public class ReportingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReportingServiceApplication.class, args);
    }
}
