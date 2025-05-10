package press.mizhifei.dentist.oauth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zhifeimi
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApiResponse {
    private boolean success;
    private String message;
} 