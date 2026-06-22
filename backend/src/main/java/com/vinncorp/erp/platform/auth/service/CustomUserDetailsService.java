package com.vinncorp.erp.platform.auth.service;
import com.vinncorp.erp.modules.projects.entity.CustomUserDetails;

import com.vinncorp.erp.platform.user.entity.User;
import com.vinncorp.erp.platform.user.repository.UserRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(@NonNull String identifier)
            throws UsernameNotFoundException {

        User user = userRepository.findByEmailWithRoles(identifier)
                .orElseGet(() -> userRepository.findByUsername(identifier)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found with email or username: " + identifier)));

        if (user.getUserRoles() == null) {
            user = userRepository.findByIdWithRoles(user.getId())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + identifier));
        }

        if (!user.isEmailVerified()) {
            throw new UsernameNotFoundException("Email not verified. Please verify your email before logging in.");
        }

        return new CustomUserDetails(user);
    }
}


