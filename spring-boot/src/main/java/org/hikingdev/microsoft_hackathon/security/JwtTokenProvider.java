package org.hikingdev.microsoft_hackathon.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.hikingdev.microsoft_hackathon.user.entities.Role;
import org.hikingdev.microsoft_hackathon.util.InvalidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

@Component
public class JwtTokenProvider {
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class.getName());

    @Value("${security.jwt.token.expire-length:3600000}")
    private long validityInMilliseconds = 60000;

    private final UserDetailsImpl userDetails;

    private String encoderKey;
    private String signalRKey;

    @Autowired
    public JwtTokenProvider(UserDetailsImpl userDetails, @Qualifier("encoderKey") String encoderKey, @Qualifier("signalRKey") String signalRKey){
        this.userDetails = userDetails;
        this.encoderKey = encoderKey;
        this.signalRKey = signalRKey;
    }

    @PostConstruct
    protected void init(){
        encoderKey = Base64.getEncoder().encodeToString(encoderKey.getBytes());
    }

    public String createUserToken(Long id, Role role){
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + validityInMilliseconds);

        // Create a Key instance from the encoderKey
        Key key = Keys.hmacShaKeyFor(encoderKey.getBytes());

        // Build the token directly using JwtBuilder

        return Jwts.builder()
                .subject(id.toString()) // Set the subject
                .claim("auth", new SimpleGrantedAuthority(role.toString())) // Add custom claims
                .claim("allowedEndpoints", List.of("/v1/user", "/v1/chat/negotiate", "/v1/chat/communicate"))
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key) // Sign with the Key object
                .compact();
    }

    public String generateSignalRToken(String audience, String userId) {
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);

        long expMillis = nowMillis + (30 * 60 * 1000);
        Date exp = new Date(expMillis);

        byte[] apiKeySecretBytes = signalRKey.getBytes(StandardCharsets.UTF_8);
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
        Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());

        JwtBuilder builder = Jwts.builder()
                .setAudience(audience)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(signingKey);

        if (userId != null) {
            builder.claim("nameid", userId);
        }

        return builder.compact();
    }


    public String generateApiToken(Long id){
        // Create a Key instance from the encoderKey
        Key key = Keys.hmacShaKeyFor(encoderKey.getBytes());

        // Build the token directly using JwtBuilder
        return Jwts.builder()
                .subject(id.toString()) // Set the subject
                .claim("allowedEndpoints", List.of("/v1/events"))
                .issuedAt(new Date())
                .signWith(key) // Sign with the Key object
                .compact();
    }

    public Authentication getAuthentication(Claims claims) throws InvalidationException {
        UserDetails userDetails;
        try {
            String profileId = validateToken(claims);
            userDetails = this.userDetails.loadUserByUsername(profileId);
        } catch (UsernameNotFoundException e){
            throw new InvalidationException("Expired or invalid Jwt Token");
        }
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    public Claims getClaims(String token) throws InvalidationException {
        try {
            // Convert the encoderKey string into a Key object
            SecretKey key = Keys.hmacShaKeyFor(encoderKey.getBytes());
            //If validated return id in token
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e){
            logger.info("Validating token " + token + " failed.");
            throw new InvalidationException("Expired or invalid Jwt Token");
        }
    }

    public String validateToken(Claims claims) throws InvalidationException{
        try {
            //If validated return id in token
            return claims.getSubject();
        } catch (JwtException | IllegalArgumentException e){
            logger.info("Error while parsing claims");
            throw new InvalidationException("Expired or invalid Jwt Token");
        }
    }

    public String resolveToken(HttpServletRequest req){
        String bearerToken = req.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")){
            return bearerToken.substring(7);
        }
        return null;
    }
}
