package com.dentistdss.userprofile.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.dentistdss.userprofile.dto.ApiResponse;
import com.dentistdss.userprofile.dto.UserResponse;
import com.dentistdss.userprofile.dto.UserUpdateRequest;
import com.dentistdss.userprofile.model.User;
import com.dentistdss.userprofile.model.Role;
import com.dentistdss.userprofile.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 *
 */
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;

    public List<UserResponse> listAllUsers() {
        return userRepository.findAll().stream()
                .map(User::toUserResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public String getUserEmail(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        return user.getEmail();
    }
    
    @Transactional(readOnly = true)
    public String getUserFullName(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        return user.getFirstName() + " " + user.getLastName();
    }
    
    @Transactional(readOnly = true)
    public ApiResponse<UserDetailsResponse> getUserDetails(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        UserDetailsResponse details = new UserDetailsResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhone(),
                user.getAddress()
        );

        return ApiResponse.success(details);
    }

    public List<UserResponse> listClinicUsers(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + userEmail));
        return userRepository.findByClinicId(user.getClinicId()).stream()
                .map(User::toUserResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));
    }

    @Transactional
    public ApiResponse<UserResponse> updateUserProfile(Long userId, UserUpdateRequest updateRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        // Update only the allowed fields
        if (updateRequest.getFirstName() != null) {
            user.setFirstName(updateRequest.getFirstName());
        }
        if (updateRequest.getLastName() != null) {
            user.setLastName(updateRequest.getLastName());
        }
        if (updateRequest.getPhone() != null) {
            user.setPhone(updateRequest.getPhone());
        }
        if (updateRequest.getDateOfBirth() != null) {
            user.setDateOfBirth(updateRequest.getDateOfBirth());
        }
        if (updateRequest.getAddress() != null) {
            user.setAddress(updateRequest.getAddress());
        }
        if (updateRequest.getProfilePictureUrl() != null) {
            user.setProfilePictureUrl(updateRequest.getProfilePictureUrl());
        }

        User savedUser = userRepository.save(user);
        return ApiResponse.success(savedUser.toUserResponse());
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getClinicDentists(Long clinicId) {
        return userRepository.findByClinicIdAndRoles(clinicId, Role.DENTIST).stream()
                .map(User::toUserResponse)
                .collect(Collectors.toList());
    }

    // Inner class for UserDetailsResponse
    public static class UserDetailsResponse {
        public Long id;
        public String email;
        public String firstName;
        public String lastName;
        public String fullName;
        public String phone;
        public String address;
        public java.util.Set<String> roles;
        public Long clinicId;
        public String clinicName;
        public Boolean enabled;

        public UserDetailsResponse(Long id, String email, String firstName, String lastName,
                String phone, String address) {
            this.id = id;
            this.email = email;
            this.firstName = firstName;
            this.lastName = lastName;
            this.fullName = firstName + " " + lastName;
            this.phone = phone;
            this.address = address;
        }

        public UserDetailsResponse(Long id, String email, String firstName, String lastName,
                String phone, String address, java.util.Set<String> roles, Long clinicId,
                String clinicName, Boolean enabled) {
            this.id = id;
            this.email = email;
            this.firstName = firstName;
            this.lastName = lastName;
            this.fullName = firstName + " " + lastName;
            this.phone = phone;
            this.address = address;
            this.roles = roles;
            this.clinicId = clinicId;
            this.clinicName = clinicName;
            this.enabled = enabled;
        }
    }
}
