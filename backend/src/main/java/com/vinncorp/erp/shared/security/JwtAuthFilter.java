package com.vinncorp.erp.shared.security;

import com.vinncorp.erp.platform.user.entity.User;
import com.vinncorp.erp.platform.user.repository.UserRepository;
import com.vinncorp.erp.modules.projects.entity.CustomUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/api/auth")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/api-docs")
                || path.startsWith("/actuator");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        boolean isValid = jwtUtil.validateToken(token);
        System.out.println("Is Valid: " + isValid);

        if (!isValid) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("Invalid or expired JWT token");
            return;
        }

        String email = jwtUtil.extractEmail(token);

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            User user = userRepository.findByEmailWithRoles(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Extract workspace-specific permissions from JWT
            List<String> jwtPermissions = jwtUtil.extractPermissions(token);
            List<String> jwtWorkspaceRoles = jwtUtil.extractWorkspaceRoles(token);
            List<String> jwtGlobalRoles = jwtUtil.extractGlobalRoles(token);

            // Build authorities from JWT permissions (workspace-scoped)
            // If JWT has workspace-specific permissions, use them directly
            // Otherwise fall back to user's global roles
            var authorities = buildAuthorities(user, jwtPermissions, jwtWorkspaceRoles, jwtGlobalRoles);

            CustomUserDetails userDetails = new CustomUserDetails(user, jwtPermissions, jwtWorkspaceRoles, jwtGlobalRoles);

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            authorities
                    );

            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        filterChain.doFilter(request, response);
    }

    private List<SimpleGrantedAuthority> buildAuthorities(
            User user, List<String> jwtPermissions, List<String> jwtWorkspaceRoles, List<String> jwtGlobalRoles) {

        // Start with global roles (e.g., ROLE_SUPER_ADMIN)
        Stream<String> roleStream = Stream.empty();
        if (jwtGlobalRoles != null) {
            roleStream = Stream.concat(roleStream,
                jwtGlobalRoles.stream().map(role -> "ROLE_" + role));
        }

        // Add workspace roles (e.g., ROLE_WORKSPACE_ADMIN)
        if (jwtWorkspaceRoles != null) {
            roleStream = Stream.concat(roleStream,
                jwtWorkspaceRoles.stream().map(role -> "ROLE_" + role));
        }

        // If JWT has workspace-specific permissions, use those
        if (jwtPermissions != null && !jwtPermissions.isEmpty()) {
            return Stream.concat(roleStream, jwtPermissions.stream())
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        }

        // Fallback: use global user_roles (legacy behavior)
        Stream<String> fallbackPerms = user.getUserRoles().stream()
                .flatMap(ur -> Stream.concat(
                        Stream.of("ROLE_" + ur.getRole().getName()),
                        ur.getRole().getPermissions().stream()
                                .map(p -> p.getName())
                ));

        return Stream.concat(roleStream, fallbackPerms)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
}
