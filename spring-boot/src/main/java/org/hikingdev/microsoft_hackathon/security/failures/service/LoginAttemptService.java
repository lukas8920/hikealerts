package org.hikingdev.microsoft_hackathon.security.failures.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class LoginAttemptService extends BaseCache {
    @Value("${max.authorization.attempts}")
    private int MAX_ATTEMPT;

    @Override
    protected int getAttempts() {
        return MAX_ATTEMPT;
    }
}
