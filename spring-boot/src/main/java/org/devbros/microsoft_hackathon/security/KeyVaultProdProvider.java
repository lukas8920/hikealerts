package org.devbros.microsoft_hackathon.security;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.jetty.JettyServerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration
@Profile("prod")
public class KeyVaultProdProvider {
    private static final String eventEndpoint = "event-endpoint-bearer-token";
    private static final String dbUrl = "sql-server-jdbc-string";
    private static final String sqlPassword = "sql-server-password";
    private static final String dbUsername = "sql-server-username";
    private static final String keyStorePassword = "ssl-keystore-password";
    private static final String queueConnectionString= "queue-connection-string";

    @Value("db.driver")
    private String dbDriver;
    @Value("azure.keyvault.url")
    private String keyvaultUrl;
    @Value("server.keystore.location")
    private String keystoreLocation;

    @Bean
    public SecretClient createSecretClient() {
        return new SecretClientBuilder()
                .vaultUrl(keyvaultUrl)
                .credential(new DefaultAzureCredentialBuilder().build())
                .buildClient();
    }

    @Bean(name = "bearerToken")
    public String bearerToken(SecretClient secretClient){
        return secretClient.getSecret(eventEndpoint).getValue();
    }

    @Bean(name = "queueConnectionString")
    public String queueConnectionString(SecretClient secretClient) {
        return secretClient.getSecret(queueConnectionString).getValue();
    }

    @Bean
    public DataSource dataSource(SecretClient secretClient) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(dbDriver);
        dataSource.setUrl(secretClient.getSecret(dbUrl).getValue());
        dataSource.setUsername(secretClient.getSecret(dbUsername).getValue());
        dataSource.setPassword(secretClient.getSecret(sqlPassword).getValue());
        return dataSource;
    }

    @Bean
    public JettyServerCustomizer jettyServerCustomizer(SecretClient secretClient) {
        String password = secretClient.getSecret(keyStorePassword).getValue();
        return server -> {
            // Create an SslContextFactory instance
            SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
            sslContextFactory.setKeyStorePath(keystoreLocation); // Path to your keystore
            sslContextFactory.setKeyStorePassword(password); // Keystore password
            sslContextFactory.setKeyManagerPassword(password); // Key password

            // Create an SSL connector with the SslContextFactory
            ServerConnector sslConnector = new ServerConnector(server, sslContextFactory);
            sslConnector.setPort(8080); // Port for HTTPS

            // Remove the default connector (usually HTTP)
            server.setConnectors(new Connector[] { sslConnector });
        };
    }
}
