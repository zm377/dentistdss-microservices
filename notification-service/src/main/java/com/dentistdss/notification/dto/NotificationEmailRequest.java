package com.dentistdss.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

/**
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEmailRequest {
    private String to;
    private String templateName;
    private Map<String, String> variables;
}
