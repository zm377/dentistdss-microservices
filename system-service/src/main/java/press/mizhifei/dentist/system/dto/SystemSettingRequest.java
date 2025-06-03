package press.mizhifei.dentist.system.dto;

import lombok.*;

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
public class SystemSettingRequest {
    private String key;
    private String value;
    private String description;
} 