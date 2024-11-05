package org.hikingdev.microsoft_hackathon.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.hikingdev.microsoft_hackathon.security.failures.Publisher;
import org.hikingdev.microsoft_hackathon.security.failures.service.LoginAttemptService;
import org.hikingdev.microsoft_hackathon.security.failures.service.RegisterAttemptService;
import org.hikingdev.microsoft_hackathon.util.InvalidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtTokenFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenFilter.class.getName());

    private final JwtTokenProvider jwtTokenProvider;
    private final Publisher publisher;
    private final LoginAttemptService loginAttemptService;
    private final RegisterAttemptService registerAttemptService;

    public JwtTokenFilter(RegisterAttemptService registerAttemptService, JwtTokenProvider jwtTokenProvider, Publisher publisher, LoginAttemptService service){
        this.jwtTokenProvider = jwtTokenProvider;
        this.publisher = publisher;
        this.loginAttemptService = service;
        this.registerAttemptService = registerAttemptService;
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
            if (token != null){
                Authentication auth = jwtTokenProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(auth);
                this.publisher.publishAuthorizationSuccess();
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

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null){
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
