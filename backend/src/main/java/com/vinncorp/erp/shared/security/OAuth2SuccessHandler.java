package com.vinncorp.erp.shared.security;

import com.vinncorp.erp.core.user.repository.RoleRepository;
import com.vinncorp.erp.core.user.entity.User;
import com.vinncorp.erp.core.user.repository.UserRepository;
import com.vinncorp.erp.core.user.repository.UserRoleRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.*;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtService;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Override
    public void onAuthenticationSuccess(@NonNull HttpServletRequest request,
                                        @NonNull HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        assert oAuth2User != null;
        String username = oAuth2User.getAttribute("login");
        String email = oAuth2User.getAttribute("email");

        // GitHub may not provide email
        if (email == null) {
            email = username + "@github.com";
        }

        final String finalEmail = email;

        // Check or create user
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(finalEmail);
                    newUser.setName(username);
                    // Auto-assign USER role for OAuth users
                    newUser.setUserRoles(new java.util.HashSet<>());
                    return userRepository.save(newUser);
                });

        // Generate JWT
        String token = jwtService.generateToken(user);

        // Redirect to frontend
        response.sendRedirect("http://localhost:5173/oauth-success?token=" + token);
    }
}

