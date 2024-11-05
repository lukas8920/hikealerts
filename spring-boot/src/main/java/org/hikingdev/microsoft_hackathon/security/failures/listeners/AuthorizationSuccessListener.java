package org.hikingdev.microsoft_hackathon.security.failures.listeners;

import jakarta.servlet.http.HttpServletRequest;
import org.hikingdev.microsoft_hackathon.security.failures.Publisher;
import org.hikingdev.microsoft_hackathon.security.failures.service.LoginAttemptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class AuthorizationSuccessListener implements ApplicationListener<Publisher.AuthorizationSuccessEvent> {
    private final LoginAttemptService loginAttemptService;

    @Autowired
    public AuthorizationSuccessListener(LoginAttemptService loginAttemptService){
        this.loginAttemptService = loginAttemptService;
    }

    @Override
    public void onApplicationEvent(Publisher.AuthorizationSuccessEvent event) {
        final HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        final String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            loginAttemptService.loginSucceeded(request.getRemoteAddr());
        } else {
            loginAttemptService.loginSucceeded(xfHeader.split(",")[0]);
        }
    }
}
