package org.hikingdev.microsoft_hackathon.security.failures.listeners;

import jakarta.servlet.http.HttpServletRequest;
import org.hikingdev.microsoft_hackathon.security.failures.Publisher;
import org.hikingdev.microsoft_hackathon.security.failures.service.RegisterAttemptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class RegistrationFailureListener implements ApplicationListener<Publisher.RegistrationFailureEvent> {
    private final RegisterAttemptService registerAttemptService;

    @Autowired
    public RegistrationFailureListener(RegisterAttemptService registerAttemptService){
        this.registerAttemptService = registerAttemptService;
    }

    @Override
    public void onApplicationEvent(Publisher.RegistrationFailureEvent event) {
        final HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        final String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            registerAttemptService.loginFailed(request.getRemoteAddr());
        } else {
            registerAttemptService.loginFailed(xfHeader.split(",")[0]);
        }
    }
}
