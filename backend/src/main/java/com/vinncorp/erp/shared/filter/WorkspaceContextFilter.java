package com.vinncorp.erp.shared.filter;

import com.vinncorp.erp.core.workspace.entity.Workspace;
import com.vinncorp.erp.core.user.repository.UserRepository;
import com.vinncorp.erp.core.workspace.repository.WorkspaceMemberRepository;
import com.vinncorp.erp.core.workspace.repository.WorkspaceRepository;
import com.vinncorp.erp.shared.security.JwtUtil;
import com.vinncorp.erp.modules.projects.entity.CustomUserDetails;
import com.vinncorp.erp.core.workspace.service.CurrentWorkspaceResolver;
import com.vinncorp.erp.core.workspace.service.Impl.DefaultWorkspaceResolver;
import com.vinncorp.erp.core.workspace.utils.TenantContext;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class WorkspaceContextFilter extends OncePerRequestFilter {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final CurrentWorkspaceResolver currentWorkspaceResolver;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    private static final String WORKSPACE_ID_HEADER = "X-Workspace-Id";

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/api/auth")
                || path.startsWith("/api/users")
                || path.startsWith("/api/workspaces")
                || path.startsWith("/api/workspace-invitations")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/api-docs")
                || path.startsWith("/invite")
                || path.startsWith("/actuator");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        Long workspaceId = null;
        Long jwtWorkspaceId = null;
        String headerWorkspaceId = request.getHeader(WORKSPACE_ID_HEADER);

        try {
            if (headerWorkspaceId != null && !headerWorkspaceId.isBlank()) {
                try {
                    workspaceId = Long.parseLong(headerWorkspaceId);
                } catch (NumberFormatException e) {
                    writeError(response, HttpStatus.BAD_REQUEST.value(),
                            "Invalid X-Workspace-Id header value");
                    return;
                }
            }

            if (workspaceId == null) {
                jwtWorkspaceId = extractWorkspaceIdFromJwt(request);
                if (jwtWorkspaceId != null) {
                    workspaceId = jwtWorkspaceId;
                }
            }

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Long userId = null;
            if (auth != null && auth.isAuthenticated()
                    && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
                userId = userDetails.getUserId();
            }

            if (workspaceId == null && userId != null) {
                workspaceId = currentWorkspaceResolver.resolveDefaultWorkspace(userId)
                        .map(Workspace::getId)
                        .orElse(null);
            }

            if (workspaceId == null) {
                filterChain.doFilter(request, response);
                return;
            }

            if (!workspaceExists(workspaceId)) {
                writeError(response, HttpStatus.NOT_FOUND.value(), "Workspace not found");
                return;
            }

            if (userId != null && !isUserMemberOfWorkspace(workspaceId, userId)) {
                writeError(response, HttpStatus.FORBIDDEN.value(),
                        "User is not an active member of this workspace");
                return;
            }

            TenantContext.setTenantId(workspaceId.toString());
            currentWorkspaceResolver.setCurrentWorkspace(workspaceId);

            filterChain.doFilter(request, response);

        } finally {
            TenantContext.clear();
            if (currentWorkspaceResolver instanceof DefaultWorkspaceResolver resolver) {
                resolver.clear();
            }
        }
    }

    private boolean workspaceExists(Long workspaceId) {
        return workspaceRepository.findById(workspaceId)
                .filter(Workspace::isActive)
                .isPresent();
    }

    private boolean isUserMemberOfWorkspace(Long workspaceId, Long userId) {
        return workspaceMemberRepository
                .existsByWorkspaceIdAndUserIdAndActiveTrue(workspaceId, userId);
    }

    private Long extractWorkspaceIdFromJwt(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        try {
            String token = authHeader.substring(7);
            Claims claims = jwtUtil.extractAllClaims(token);
            Object workspaceIdClaim = claims.get("workspaceId");
            if (workspaceIdClaim instanceof Number number) {
                return number.longValue();
            }
            if (workspaceIdClaim instanceof String str) {
                return Long.parseLong(str);
            }
        } catch (Exception e) {
            log.debug("Failed to extract workspaceId from JWT: {}", e.getMessage());
        }
        return null;
    }

    private void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write("{\"success\":false,\"message\":\"" + message + "\"}");
    }
}


