package press.mizhifei.dentist.reporting.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import press.mizhifei.dentist.reporting.model.ReportTemplate;
import press.mizhifei.dentist.reporting.repository.ReportTemplateRepository;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Initialization Service
 * 
 * Loads sample report templates and initial data on application startup.
 * Only runs if no templates exist in the database.
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataInitializationService implements CommandLineRunner {

    private final ReportTemplateRepository templateRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void run(String... args) throws Exception {
        initializeReportTemplates();
    }

    /**
     * Initialize report templates if none exist
     */
    private void initializeReportTemplates() {
        try {
            long existingCount = templateRepository.count();
            if (existingCount > 0) {
                log.info("Report templates already exist ({}), skipping initialization", existingCount);
                return;
            }

            log.info("Initializing report templates from sample data...");

            ClassPathResource resource = new ClassPathResource("sample-templates.json");
            try (InputStream inputStream = resource.getInputStream()) {
                List<ReportTemplate> templates = objectMapper.readValue(
                    inputStream, 
                    new TypeReference<List<ReportTemplate>>() {}
                );

                // Set creation metadata
                LocalDateTime now = LocalDateTime.now();
                for (ReportTemplate template : templates) {
                    template.setCreatedAt(now);
                    template.setModifiedAt(now);
                    template.setCreatedBy(1L); // System user
                    template.setModifiedBy(1L); // System user
                }

                // Save templates
                List<ReportTemplate> savedTemplates = templateRepository.saveAll(templates);
                
                log.info("Successfully initialized {} report templates:", savedTemplates.size());
                for (ReportTemplate template : savedTemplates) {
                    log.info("  - {} ({})", template.getName(), template.getTemplateCode());
                }
            }

        } catch (Exception e) {
            log.error("Error initializing report templates: {}", e.getMessage(), e);
        }
    }
}
