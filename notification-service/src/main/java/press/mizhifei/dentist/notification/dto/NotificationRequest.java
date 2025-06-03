package press.mizhifei.dentist.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
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
public class NotificationRequest {
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    private String templateName;
    
    @NotNull(message = "Notification type is required")
    private String type; // EMAIL, SMS, PUSH, IN_APP
    
    private String subject;
    
    @NotBlank(message = "Body is required")
    private String body;
    
    private LocalDateTime scheduledFor;
    
    private Map<String, Object> metadata;
    
    private Map<String, String> templateVariables;
} 