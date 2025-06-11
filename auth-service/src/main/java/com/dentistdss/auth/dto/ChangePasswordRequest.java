package com.dentistdss.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 *
 */
@Data
public class ChangePasswordRequest {

    @NotBlank(message = "New password is required")
    private String newPassword;
}
