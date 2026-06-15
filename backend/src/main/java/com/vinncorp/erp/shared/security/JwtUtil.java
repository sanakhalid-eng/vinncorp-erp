package com.vinncorp.erp.shared.security;

import com.vinncorp.erp.core.user.entity.Permission;
import com.vinncorp.erp.core.user.entity.User;
import com.vinncorp.erp.core.user.entity.UserRole;
import com.vinncorp.erp.core.user.entity.UserGlobalRole;
import com.vinncorp.erp.core.user.repository.UserRepository;
import com.vinncorp.erp.core.user.repository.UserGlobalRoleRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserGlobalRoleRepository userGlobalRoleRepository;

    // Safe key generation
    private Key getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);

        if (keyBytes.length < 32) {
            throw new RuntimeException("JWT secret must be at least 32 characters long");
        }

        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Central parser
    private JwtParser getParser() {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .setAllowedClockSkewSeconds(60)
                .build();
    }

    // Generate token (legacy: uses global user_roles)
    public String generateToken(User user) {
        return generateToken(user, null, null);
    }

    public String generateToken(User user, Long workspaceId, String workspaceSlug) {
        Map<String, Object> claims = new HashMap<>();
        User userWithRoles = userRepository.findByIdWithRoles(user.getId())
                .orElse(user);
        List<String> permissions = userWithRoles.getUserRoles().stream()
                .map(UserRole::getRole)
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getName)
                .distinct()
                .collect(Collectors.toList());
        claims.put("permissions", permissions);
        if (workspaceId != null) {
            claims.put("workspaceId", workspaceId);
        }
        if (workspaceSlug != null) {
            claims.put("workspaceSlug", workspaceSlug);
        }
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userWithRoles.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Generate a token with workspace-specific permissions and roles.
     * This is the primary method for workspace-scoped RBAC.
     */
    public String generateTokenWithWorkspacePermissions(
            User user, Long workspaceId, String workspaceSlug,
            List<String> workspacePermissions, List<String> workspaceRoles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("permissions", workspacePermissions != null ? workspacePermissions : List.of());
        claims.put("workspaceRoles", workspaceRoles != null ? workspaceRoles : List.of());

        // Add global roles
        List<String> globalRoles = userGlobalRoleRepository.findGlobalRoleNamesByUserId(user.getId());
        claims.put("globalRoles", globalRoles != null ? globalRoles : List.of());

        if (workspaceId != null) {
            claims.put("workspaceId", workspaceId);
        }
        if (workspaceSlug != null) {
            claims.put("workspaceSlug", workspaceSlug);
        }
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Extract email
    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    // Extract claims
    public Claims extractAllClaims(String token) {
        return getParser()
                .parseClaimsJws(token)
                .getBody();
    }

    // Extract permissions safely
    public List<String> extractPermissions(String token) {
        Claims claims = extractAllClaims(token);

        List<?> rawPermissions = claims.get("permissions", List.class);

        if (rawPermissions == null) return List.of();

        return rawPermissions.stream()
                .map(Object::toString)
                .collect(Collectors.toList());
    }

    // Extract workspace roles from JWT
    public List<String> extractWorkspaceRoles(String token) {
        Claims claims = extractAllClaims(token);
        List<?> rawRoles = claims.get("workspaceRoles", List.class);
        if (rawRoles == null) return List.of();
        return rawRoles.stream()
                .map(Object::toString)
                .collect(Collectors.toList());
    }

    // Extract global roles from JWT
    public List<String> extractGlobalRoles(String token) {
        Claims claims = extractAllClaims(token);
        List<?> rawRoles = claims.get("globalRoles", List.class);
        if (rawRoles == null) return List.of();
        return rawRoles.stream()
                .map(Object::toString)
                .collect(Collectors.toList());
    }

    // Check expiration
    public boolean isTokenExpired(String token) {
        try {
            Date expirationDate = extractAllClaims(token).getExpiration();
            return expirationDate.before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    // Validate token
    public boolean validateToken(String token) {
        try {
            getParser().parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            System.out.println("JWT ERROR: Token expired");
        } catch (UnsupportedJwtException e) {
            System.out.println("JWT ERROR: Unsupported token");
        } catch (MalformedJwtException e) {
            System.out.println("JWT ERROR: Malformed token");
        } catch (SignatureException e) {
            System.out.println("JWT ERROR: Invalid signature");
        } catch (IllegalArgumentException e) {
            System.out.println("JWT ERROR: Token is empty");
        }
        return false;
    }
}
