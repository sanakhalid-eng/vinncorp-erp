package com.vinncorp.erp.shared.security;

import com.vinncorp.erp.platform.user.entity.Permission;
import com.vinncorp.erp.platform.user.entity.User;
import com.vinncorp.erp.platform.user.entity.UserRole;
import com.vinncorp.erp.platform.user.repository.UserRepository;
import com.vinncorp.erp.platform.user.repository.UserGlobalRoleRepository;
import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class JwtUtil {

    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    @Value("${jwt.expiration}")
    private long expiration;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserGlobalRoleRepository userGlobalRoleRepository;

    public JwtUtil(JwtKeyProvider keyProvider) {
        this.privateKey = keyProvider.getPrivateKey();
        this.publicKey = keyProvider.getPublicKey();
    }

    private JwtParser getParser() {
        return Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .setAllowedClockSkewSeconds(60)
                .build();
    }

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
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }

    public String generateTokenWithWorkspacePermissions(
            User user, Long workspaceId, String workspaceSlug,
            List<String> workspacePermissions, List<String> workspaceRoles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("permissions", workspacePermissions != null ? workspacePermissions : List.of());
        claims.put("workspaceRoles", workspaceRoles != null ? workspaceRoles : List.of());

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
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    public Claims extractAllClaims(String token) {
        return getParser()
                .parseClaimsJws(token)
                .getBody();
    }

    public List<String> extractPermissions(String token) {
        Claims claims = extractAllClaims(token);
        List<?> rawPermissions = claims.get("permissions", List.class);
        if (rawPermissions == null) return List.of();
        return rawPermissions.stream()
                .map(Object::toString)
                .collect(Collectors.toList());
    }

    public List<String> extractWorkspaceRoles(String token) {
        Claims claims = extractAllClaims(token);
        List<?> rawRoles = claims.get("workspaceRoles", List.class);
        if (rawRoles == null) return List.of();
        return rawRoles.stream()
                .map(Object::toString)
                .collect(Collectors.toList());
    }

    public List<String> extractGlobalRoles(String token) {
        Claims claims = extractAllClaims(token);
        List<?> rawRoles = claims.get("globalRoles", List.class);
        if (rawRoles == null) return List.of();
        return rawRoles.stream()
                .map(Object::toString)
                .collect(Collectors.toList());
    }

    public boolean isTokenExpired(String token) {
        try {
            Date expirationDate = extractAllClaims(token).getExpiration();
            return expirationDate.before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

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
