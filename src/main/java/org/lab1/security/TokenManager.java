package org.lab1.security;

import io.jsonwebtoken.*;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import org.lab1.config.EnvConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class TokenManager {
    private static final long TOKEN_VALIDITY_MILLISECONDS = 3600000;
    private static final String USER_ROLE = "USER";
    private static final String DEVELOPER_ROLE = "DEVELOPER";
    private static final String PRIVACY_POLICY_ROLE = "PRIVACY_POLICY";
    private static final String ROLE_USER_AUTHORITY = "ROLE_USER";
    private static final String ROLE_DEVELOPER_AUTHORITY = "ROLE_DEVELOPER";
    private static final String ROLE_PRIVACY_POLICY_AUTHORITY = "ROLE_PRIVACY_POLICY";
    private static final String USER_ID_CLAIM = "userId";
    private static final String ROLE_CLAIM = "role";
    private static final String ACTIVE_SESSIONS_METRIC = "auth.sessions.active";
    private static final String SIGNATURE_ALGORITHM = "HS256";
    private static final String EXPIRATION_LOG = "Exp: ";
    private static final String TOKEN_VALID_LOG = "Token is valid.";
    private static final String TOKEN_EXPIRED_LOG = "Token has expired.";
    private static final String INVALID_SIGNATURE_LOG = "Invalid signature.";
    private static final String MALFORMED_TOKEN_LOG = "Malformed token.";
    private static final String TOKEN_VALIDATION_ERROR_LOG = "Token validation error: ";

    private static final String SECRET_KEY = EnvConfig.get("SECRET_KEY");

    private final ConcurrentHashMap<String, Boolean> activeTokens = new ConcurrentHashMap<>();
    private final MeterRegistry meterRegistry;
    private final PasswordEncoder passwordEncoder;
    private AtomicInteger activeSessionsGauge;

    private final Map<String, List<GrantedAuthority>> roleAuthorities = initializeRoleAuthorities();

    @Autowired
    public TokenManager(MeterRegistry meterRegistry, PasswordEncoder passwordEncoder) {
        this.meterRegistry = meterRegistry;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void initMetrics() {
        this.activeSessionsGauge = new AtomicInteger(0);

        Gauge.builder(ACTIVE_SESSIONS_METRIC, activeSessionsGauge, AtomicInteger::get)
                .strongReference(true)
                .register(meterRegistry);
    }

    private Map<String, List<GrantedAuthority>> initializeRoleAuthorities() {
        Map<String, List<GrantedAuthority>> authorities = new HashMap<>();

        authorities.put(USER_ROLE, createUserAuthorities());
        authorities.put(DEVELOPER_ROLE, createDeveloperAuthorities());
        authorities.put(PRIVACY_POLICY_ROLE, createPrivacyPolicyAuthorities());

        return authorities;
    }

    private List<GrantedAuthority> createUserAuthorities() {
        return Arrays.asList(
                new SimpleGrantedAuthority(ROLE_USER_AUTHORITY),
                new SimpleGrantedAuthority("application.read"),
                new SimpleGrantedAuthority("application_stats.read"),
                new SimpleGrantedAuthority("in_app_add.read"),
                new SimpleGrantedAuthority("in_app_purchase.read"),
                new SimpleGrantedAuthority("user.download_application"),
                new SimpleGrantedAuthority("user.purchase_in_app_item"),
                new SimpleGrantedAuthority("user.view_advertisement")
        );
    }

    private List<GrantedAuthority> createDeveloperAuthorities() {
        return Arrays.asList(
                new SimpleGrantedAuthority(ROLE_DEVELOPER_AUTHORITY),
                new SimpleGrantedAuthority("application.manage"),
                new SimpleGrantedAuthority("application.read"),
                new SimpleGrantedAuthority("application.verify"),
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
                new SimpleGrantedAuthority("verification_log.read"),
                new SimpleGrantedAuthority("form.create"),
                new SimpleGrantedAuthority("stats.add_sheets"),
                new SimpleGrantedAuthority("stats.create"),
                new SimpleGrantedAuthority("google_results.read.own"),
                new SimpleGrantedAuthority("google_results.read.all"),
                new SimpleGrantedAuthority("google_results.read.by_operation"),
                new SimpleGrantedAuthority("google_results.read.errors"),
                new SimpleGrantedAuthority("google_results.read.successful"),
                new SimpleGrantedAuthority("google_results.read.by_id"),
                new SimpleGrantedAuthority("google_stats.read.own")
        );
    }

    private List<GrantedAuthority> createPrivacyPolicyAuthorities() {
        return Arrays.asList(
                new SimpleGrantedAuthority(ROLE_PRIVACY_POLICY_AUTHORITY),
                new SimpleGrantedAuthority("developer.manage"),
                new SimpleGrantedAuthority("developer.read"),
                new SimpleGrantedAuthority("form.manage"),
                new SimpleGrantedAuthority("form.read"),
                new SimpleGrantedAuthority("stats.update")
        );
    }

    public String generateToken(String username, String role, int userId) {
        Claims claims = createClaims(username, role, userId);
        String token = buildToken(claims);
        storeActiveToken(token);
        return token;
    }

    private Claims createClaims(String username, String role, int userId) {
        Claims claims = Jwts.claims().setSubject(username);
        claims.put(ROLE_CLAIM, role);
        claims.put(USER_ID_CLAIM, userId);
        return claims;
    }

    private String buildToken(Claims claims) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + TOKEN_VALIDITY_MILLISECONDS);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(SignatureAlgorithm.valueOf(SIGNATURE_ALGORITHM), SECRET_KEY)
                .compact();
    }

    private void storeActiveToken(String token) {
        String hashedToken = passwordEncoder.encode(token);
        activeTokens.put(hashedToken, true);
        updateActiveSessionsMetric();
    }

    public Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = validateTokenAndGetClaims(token);
            Date expiration = claims.getExpiration();

            System.out.println(EXPIRATION_LOG + expiration);

            if (isTokenNotExpired(expiration)) {
                System.out.println(TOKEN_VALID_LOG);
                return true;
            }

            removeExpiredToken(token);
            System.out.println(TOKEN_EXPIRED_LOG);
            return false;

        } catch (SignatureException signatureException) {
            System.out.println(INVALID_SIGNATURE_LOG);
        } catch (ExpiredJwtException expiredJwtException) {
            System.out.println(TOKEN_EXPIRED_LOG);
        } catch (MalformedJwtException malformedJwtException) {
            System.out.println(MALFORMED_TOKEN_LOG);
        } catch (Exception exception) {
            System.out.println(TOKEN_VALIDATION_ERROR_LOG + exception.getMessage());
        }

        return false;
    }

    private Claims validateTokenAndGetClaims(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
    }

    private boolean isTokenNotExpired(Date expiration) {
        return expiration != null && expiration.after(new Date());
    }

    private void removeExpiredToken(String token) {
        String hashedToken = passwordEncoder.encode(token);
        activeTokens.remove(hashedToken);
        updateActiveSessionsMetric();
    }

    private void updateActiveSessionsMetric() {
        activeSessionsGauge.set(activeTokens.size());
    }

    public List<GrantedAuthority> getAuthoritiesByRole(String role) {
        return roleAuthorities.get(role.toUpperCase());
    }
}
