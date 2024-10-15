package org.devbros.microsoft_hackathon.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class ConstantBearerTokenFilter extends OncePerRequestFilter {
    private final String bearerToken;

    public ConstantBearerTokenFilter(String bearerToken){
        this.bearerToken = bearerToken;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader != null && authorizationHeader.equals("Bearer " + bearerToken)) {
            Authentication authentication = new PreAuthenticatedAuthenticationToken("user", null, null);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } else {
            logger.warn("Unauthorized access - " + request.getRemoteAddr());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
