package com.dentistdss.userprofile.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.dentistdss.userprofile.dto.UserResponse;
import com.dentistdss.userprofile.model.Role;
import com.dentistdss.userprofile.model.User;
import com.dentistdss.userprofile.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for dentist-specific profile management
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 *
 */
@Service
@RequiredArgsConstructor
public class DentistService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<UserResponse> listAllDentists() {
        return userRepository.findByRole(Role.DENTIST).stream()
                .map(User::toUserResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getDentistsByClinic(Long clinicId) {
        return userRepository.findByRoleAndClinicId(Role.DENTIST, clinicId).stream()
                .map(User::toUserResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserResponse getDentistProfile(Long dentistId) {
        User dentist = userRepository.findById(dentistId)
                .orElseThrow(() -> new IllegalArgumentException("Dentist not found with id: " + dentistId));
        
        // Verify the user is actually a dentist
        if (!dentist.getRoles().contains(Role.DENTIST)) {
            throw new IllegalArgumentException("User is not a dentist");
        }
        
        return dentist.toUserResponse();
    }
}
