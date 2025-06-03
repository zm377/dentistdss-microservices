package press.mizhifei.dentist.audit.dto;

import lombok.*;

import java.util.Map;

/**
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 *
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuditEntryRequest {
    private String actor;
    private String action;
    private String target;
    private Map<String, Object> context;
} 