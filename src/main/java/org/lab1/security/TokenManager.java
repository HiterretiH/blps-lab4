package org.lab1.security;

import io.github.cdimascio.dotenv.Dotenv;
import io.jsonwebtoken.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TokenManager {
    private static final Dotenv dotenv = Dotenv.load();
    private static final String SECRET_KEY = dotenv.get("SECRET_KEY");
    private final Map<String, List<GrantedAuthority>> roleAuthorities = new HashMap<>() {{
        put("USER", List.of(
                new SimpleGrantedAuthority("ROLE_USER"),
                new SimpleGrantedAuthority("application.read"),
                new SimpleGrantedAuthority("application_stats.read"),
                new SimpleGrantedAuthority("in_app_add.read"),
                new SimpleGrantedAuthority("in_app_purchase.read"),
                new SimpleGrantedAuthority("user.download_application"),
                new SimpleGrantedAuthority("user.purchase_in_app_item"),
                new SimpleGrantedAuthority("user.view_advertisement")
        ));
        put("DEVELOPER", List.of(
                new SimpleGrantedAuthority("ROLE_DEVELOPER"),
                new SimpleGrantedAuthority("application.manage"),
                new SimpleGrantedAuthority("application.read"),
                new SimpleGrantedAuthority("application_stats.manage"),
                new SimpleGrantedAuthority("application_stats.read"),
                new SimpleGrantedAuthority("application_stats.verify"),
                new SimpleGrantedAuthority("developer.manage"),
                new SimpleGrantedAuthority("developer.read"),
                new SimpleGrantedAuthority("form.read"),
                new SimpleGrantedAuthority("in_app_add.manage"),
                new SimpleGrantedAuthority("in_app_add.read"),
                new SimpleGrantedAuthority("in_app_purchase.manage"),
                new SimpleGrantedAuthority("in_app_purchase.read"),
                new SimpleGrantedAuthority("monetization.read"),
                new SimpleGrantedAuthority("monetization.payout.request"),
                new SimpleGrantedAuthority("monetization.payout.execute"),
                new SimpleGrantedAuthority("monetized_application.manage"),
                new SimpleGrantedAuthority("monetized_application.read"),
                new SimpleGrantedAuthority("payment_request.create"),
                new SimpleGrantedAuthority("payment_request.read"),
                new SimpleGrantedAuthority("payment_request.validate_card"),
                new SimpleGrantedAuthority("payout_log.manage"),
                new SimpleGrantedAuthority("payout_log.read"),
                new SimpleGrantedAuthority("verification_log.manage"),
                new SimpleGrantedAuthority("verification_log.read")
        ));
        put("PRIVACY_POLICY", List.of(
                new SimpleGrantedAuthority("ROLE_PRIVACY_POLICY"),
                new SimpleGrantedAuthority("developer.manage"),
                new SimpleGrantedAuthority("developer.read"),
                new SimpleGrantedAuthority("form.manage"),
                new SimpleGrantedAuthority("form.read")
        ));
    }};

    private final long VALIDITY_IN_MILLISECONDS = 3600000;

    public String generateToken(String username, String role, int userId) {
        Claims claims = Jwts.claims().setSubject(username);
        claims.put("role", role);
        claims.put("userId", userId);
        Date now = new Date();
        Date validity = new Date(now.getTime() + VALIDITY_IN_MILLISECONDS);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    public Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token)
                    .getBody();

            Date expiration = claims.getExpiration();
            System.out.println("Exp: " + expiration);

            if (expiration != null && expiration.after(new Date())) {
                System.out.println("Token is valid.");
                return true;
            } else {
                System.out.println("Token has expired.");
                return false;
            }
        } catch (SignatureException e) {
            System.out.println("Invalid signature.");
        } catch (ExpiredJwtException e) {
            System.out.println("Token has expired.");
        } catch (MalformedJwtException e) {
            System.out.println("Malformed token.");
        } catch (Exception e) {
            System.out.println("Token validation error: " + e.getMessage());
        }
        return false;
    }

    public List<GrantedAuthority> getAuthoritiesByRole(String role) {
        return roleAuthorities.get(role.toUpperCase());
    }
}