package org.hikingdev.microsoft_hackathon.security.failures.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RegisterAttemptService extends BaseCache {
    @Value("${max.registration.attempts}")
    private int MAX_ATTEMPTS;

    @Override
    protected int getAttempts() {
        return MAX_ATTEMPTS;
    }
}
