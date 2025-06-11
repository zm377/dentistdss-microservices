package press.mizhifei.dentist.chatlog.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * PHI (Protected Health Information) redaction metadata
 * Tracks what information was redacted for HIPAA compliance
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PHIRedactionInfo {
    
    /**
     * Whether any PHI was detected and redacted
     */
    private Boolean phiDetected;
    
    /**
     * Types of PHI that were redacted (e.g., "ssn", "phone", "email")
     */
    private List<String> redactedTypes;
    
    /**
     * Number of redactions performed
     */
    private Integer redactionCount;
    
    /**
     * Timestamp when redaction was performed
     */
    @Builder.Default
    private LocalDateTime redactionTimestamp = LocalDateTime.now();
    
    /**
     * Redaction algorithm version for audit purposes
     */
    private String redactionVersion;
    
    /**
     * Additional metadata about the redaction process
     */
    private Map<String, Object> redactionMetadata;
    
    /**
     * Hash of original content for verification (without storing actual PHI)
     */
    private String originalContentHash;
}
