package org.devbros.microsoft_hackathon.security;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Service
public class CredentialService {
    public String getPassword(String passName) {
        String password = "";
        try {
            // Use a shell command to avoid GPG prompt if necessary
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("docker", "exec", "hiking-alerts_gpg_agent_1", "pass", "show", passName);
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            password = reader.readLine(); // Read the password

            process.waitFor(); // Wait for the process to complete
        } catch (Exception e) {
            e.printStackTrace(); // Handle exceptions
        }
        return password;
    }
}
