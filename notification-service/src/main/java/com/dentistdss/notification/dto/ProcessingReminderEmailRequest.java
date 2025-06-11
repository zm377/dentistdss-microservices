package com.dentistdss.notification.dto;

import lombok.AllArgsConstructor;
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
@NoArgsConstructor
@AllArgsConstructor
public class ProcessingReminderEmailRequest {
    private String clinicAdminEmail;
    private String clinicName;
    private String firstName;
    private String lastName;
    private String email;
    private String role;
}
