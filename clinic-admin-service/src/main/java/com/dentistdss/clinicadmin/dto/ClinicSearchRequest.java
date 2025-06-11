package com.dentistdss.clinicadmin.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
public class ClinicSearchRequest {
    
    @Size(max = 100, message = "Keywords must not exceed 100 characters")
    @Pattern(regexp = "^[a-zA-Z0-9\\s\\-.,@#&()]*$", 
             message = "Keywords can only contain letters, numbers, spaces, and common punctuation (- . , @ # & ( ))")
    private String keywords;
} 