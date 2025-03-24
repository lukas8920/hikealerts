package org.hikingdev.microsoft_hackathon.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.hikingdev.microsoft_hackathon.security.failures.Publisher;
import org.hikingdev.microsoft_hackathon.security.failures.service.LoginAttemptService;
import org.hikingdev.microsoft_hackathon.security.failures.service.RegisterAttemptService;
import org.hikingdev.microsoft_hackathon.user.entities.Role;
import org.hikingdev.microsoft_hackathon.util.exceptions.InvalidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class JwtTokenFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenFilter.class.getName());

    private final JwtTokenProvider jwtTokenProvider;
    private final Publisher publisher;
    private final LoginAttemptService loginAttemptService;
    private final RegisterAttemptService registerAttemptService;
    private final RoleRegister roleRegister;

    public JwtTokenFilter(RegisterAttemptService registerAttemptService, JwtTokenProvider jwtTokenProvider, Publisher publisher, LoginAttemptService service,
                          RoleRegister roleRegister){
        this.jwtTokenProvider = jwtTokenProvider;
        this.publisher = publisher;
        this.loginAttemptService = service;
        this.registerAttemptService = registerAttemptService;
        this.roleRegister = roleRegister;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest,
                                    HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
        String ip = getClientIP(httpServletRequest);
        if (loginAttemptService.isBlocked(ip) || registerAttemptService.isBlocked(ip)) {
            logger.info("IP is blocked: " + ip);
            httpServletResponse.sendError(403, "Access Forbidden.");
            return;
        }

        String token = jwtTokenProvider.resolveToken(httpServletRequest);
        try {
            if (token != null) {
                Claims claims = jwtTokenProvider.getClaims(token);
                Authentication auth = jwtTokenProvider.getAuthentication(claims);

                List<Role> roles = (List<Role>) claims.get("roles", List.class);
                List<String> allowedEndpoints = getAllowedEndpoints(roles);

                String requestPath = httpServletRequest.getRequestURI();
                logger.info("{} is trying to access {}", ip, requestPath);
                logger.info("User {} is allowed to access {}", ip, allowedEndpoints);
                if (allowedEndpoints.isEmpty() || !this.isPathAllowed(requestPath, allowedEndpoints)) {
                    httpServletResponse.setStatus(HttpStatus.FORBIDDEN.value());
                    httpServletResponse.getWriter().write("Access to this endpoint is not allowed with this token");
                    return;
                } else {
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    this.publisher.publishAuthorizationSuccess();
                }
            }
        } catch (InvalidationException e){
            SecurityContextHolder.clearContext();
            this.publisher.publishAuthorizationFailure();
            logger.info("Return validation error with code " + 400 + " to clients.");
            httpServletResponse.sendError(400, e.getMessage());
            return;
        }
        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }

    List<String> getAllowedEndpoints(List<Role> roles){
        List<String> allowedEndpoints = new ArrayList<>();
        if (roles != null){
            allowedEndpoints.addAll(roles.stream()
                    .map(roleRegister.getRoles()::get)
                    .filter(Objects::nonNull)
                    .flatMap(List::stream)
                    .distinct().toList());
        }
        return allowedEndpoints;
    }

    private boolean isPathAllowed(String requestPath, List<String> allowedEndpoints) {
        for (String endpoint : allowedEndpoints) {
            if (requestPath.startsWith(endpoint)) {
                return true;
            }
        }
        return false;
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null){
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
