package org.devbros.microsoft_hackathon.security;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.jetty.JettyServerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import retrofit2.Response;

import javax.sql.DataSource;
import java.io.IOException;

@Configuration
@Profile("prod")
public class KeyVaultProdProvider {
    private static final Logger logger = LoggerFactory.getLogger(KeyVaultProdProvider.class.getName());

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
    public SecretClient createSecretClient(GpgService gpgService) throws IOException {
        logger.info("Init secret client.");
        String tenantId = null;
        String clientId = null;
        String password = null;

        try {
            Response<GpgSecret> tenantIdResponse = gpgService.getSecret("keyvault/tenantid").execute();
            Response<GpgSecret> clientIdResponse = gpgService.getSecret("keyvault/clientid").execute();
            Response<GpgSecret> passwordResponse = gpgService.getSecret("keyvault/password").execute();

            logger.info("Received response messages from the gpg server.");
            if (tenantIdResponse.body() != null && clientIdResponse.body() != null && passwordResponse.body() != null) {
                tenantId = tenantIdResponse.body().getPassword();
                clientId = clientIdResponse.body().getPassword();
                password = passwordResponse.body().getPassword();
            } else {
                logger.info("No response messages from gpg server.");
            }
        } catch (IOException e) {
            logger.error("Error while requesting secrets from GPG Server: " + e.getMessage());
            throw e;
        }

        if(tenantId != null && clientId != null && password != null){
            logger.info("Successfully requested keys from gpg.");
        }

        ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
                .tenantId(tenantId)
                .clientId(clientId)
                .clientSecret(password)
                .build();

        return new SecretClientBuilder()
                .vaultUrl(keyvaultUrl)
                .credential(clientSecretCredential)
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
