package press.mizhifei.dentist.userprofile.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
public class ReviewApprovalRequest {
    
    @NotNull(message = "Approval decision is required")
    private Boolean approved;
    
    private String reviewNotes;
}
