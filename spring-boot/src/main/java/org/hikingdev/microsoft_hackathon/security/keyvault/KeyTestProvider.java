package org.hikingdev.microsoft_hackathon.security.keyvault;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("test")
public class KeyTestProvider {
    @Value("${event.endpoint.bearer.token:default}")
    private String bearerToken;
    @Value("${queue.connection.string:default}")
    private String queueConnectionString;
    @Value("${spring.security.encoder.key}")
    private String encoderKey;
    @Value("${contact.mail.address.password}")
    private String mailPassword;
    @Value("${azure.openai.key}")
    private String openaiKey;
    @Value("${azure.openai.endpoint}")
    private String openaiEndpoint;

    @Bean(name = "bearerToken")
    public String bearerToken(){
        return this.bearerToken;
    }

    @Bean(name = "queueConnectionString")
    public String queueConnectionString(){
        return this.queueConnectionString;
    }

    @Bean(name = "encoderKey")
    public String encoderKey() { return this.encoderKey; }

    @Bean(name = "mailPassword")
    public String mailPassword() { return this.mailPassword; }

    @Bean(name = "openai_key")
    public String openaiKey() { return this.openaiKey; }

    @Bean(name = "openai_endpoint")
    public String openaiEndpoint() { return this.openaiEndpoint; }
}
