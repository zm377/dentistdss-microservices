package com.dentistdss.auth.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.dentistdss.auth.model.User;
import com.dentistdss.auth.repository.UserRepository;

/**
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 *
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Loading user by username: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User not found with email: {}", email);
                    return new UsernameNotFoundException("User not found with email : " + email);
                });

        log.debug("User found: {} - enabled: {}, accountNonLocked: {}, accountNonExpired: {}, credentialsNonExpired: {}",
            user.getEmail(), user.isEnabled(), user.isAccountNonLocked(),
            user.isAccountNonExpired(), user.isCredentialsNonExpired());

        UserPrincipal userPrincipal = UserPrincipal.create(user);
        log.debug("UserPrincipal created for user: {} with authorities: {}",
            userPrincipal.getEmail(), userPrincipal.getAuthorities());

        return userPrincipal;
    }

    @Transactional
    public UserDetails loadUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id : " + id));

        return UserPrincipal.create(user);
    }
}
