package com.dentistdss.auth.dto;

import java.util.HashSet;
import java.util.Set;

import com.dentistdss.auth.model.Role;

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
public class UserResponse {
    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String address;
    private String profilePictureUrl;
    @Builder.Default
    private Set<Role> roles = new HashSet<>();
    private Long clinicId;
    private String clinicName;
    private Boolean enabled;
}