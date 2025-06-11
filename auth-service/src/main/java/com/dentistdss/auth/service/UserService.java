package com.dentistdss.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.dentistdss.auth.dto.ApiResponse;
import com.dentistdss.auth.dto.UserDetailsResponse;
import com.dentistdss.auth.dto.UserResponse;
import com.dentistdss.auth.dto.UserUpdateRequest;
import com.dentistdss.auth.model.User;
import com.dentistdss.auth.model.Role;
import com.dentistdss.auth.repository.UserRepository;

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

        UserDetailsResponse details = UserDetailsResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFirstName() + " " + user.getLastName())
                .phone(user.getPhone())
                .address(user.getAddress())
                .roles(user.getRoles().stream().map(Enum::name).collect(Collectors.toSet()))
                .clinicId(user.getClinicId())
                .clinicName(user.getClinicName())
                .enabled(user.isEnabled())
                .build();

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
}