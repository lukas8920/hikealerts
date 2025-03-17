package org.hikingdev.microsoft_hackathon.geotrek;

import org.hikingdev.microsoft_hackathon.geotrek.entities.GeotrekToken;
import org.hikingdev.microsoft_hackathon.geotrek.entities.GeotrekUser;
import org.hikingdev.microsoft_hackathon.repository.geotrek.IGeotrekRepository;
import org.hikingdev.microsoft_hackathon.security.UserDetailsImpl;
import org.hikingdev.microsoft_hackathon.user.entities.User;
import org.hikingdev.microsoft_hackathon.util.encryption.AESEncryption;
import org.hikingdev.microsoft_hackathon.util.exceptions.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import javax.crypto.SecretKey;
import java.util.UUID;

@Service
public class GeotrekService {
    private static final Logger logger = LoggerFactory.getLogger(GeotrekService.class);

    private final IGeotrekRepository iGeotrekRepository;
    private final UserDetailsImpl userDetails;
    private final GeotrekDbService geotrekDbService;

    private SecretKey key;

    @Autowired
    public GeotrekService(IGeotrekRepository iGeotrekRepository, UserDetailsImpl userDetails, @Qualifier("aes_decryption_key") String aesDecryptionKey,
                          @Qualifier("GeotrekDbService") GeotrekDbService geotrekDbService){
        this.iGeotrekRepository = iGeotrekRepository;
        this.userDetails = userDetails;
        this.geotrekDbService = geotrekDbService;
        try {
            this.key = AESEncryption.generateKey(aesDecryptionKey);
        } catch (Exception e) {
            logger.error("Error while initializing the aes decrpytion key.");
        }
    }

    public void register(User user) throws Exception {
        String userName = UUID.randomUUID().toString();
        String password = UUID.randomUUID().toString();

        String encryptedPassword = AESEncryption.encrypt(password, key);
        GeotrekToken geotrekToken = new GeotrekToken(user.getId(), userName, encryptedPassword);

        GeotrekUser geotrekUser = new GeotrekUser();
        geotrekUser.setUsername(userName);
        geotrekUser.setPassword(encryptedPassword);
        geotrekUser.setFirstName(userName);
        geotrekUser.setLastName(userName);
        geotrekUser.setActive(true);
        geotrekUser.setSuperuser(false);

        this.geotrekDbService.postUser(geotrekUser).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                logger.info("Successful creation of user in geotrek db.");
                GeotrekService.this.iGeotrekRepository.save(geotrekToken);
            }

            @Override
            public void onFailure(Call<Void> call, Throwable throwable) {
                logger.error("Error while posting user to geotrek db.");
            }
        });
    }

    public GeotrekToken findToken() throws BadRequestException {
        UserDetails userDetails = this.userDetails.getSecurityContext();
        if (userDetails == null){
            throw new BadRequestException("No existing user authenticated.");
        }
        Long id = Long.parseLong(userDetails.getUsername());

        GeotrekToken geotrekToken = this.iGeotrekRepository.find(id);
        if (geotrekToken == null){
            throw new BadRequestException("Could not find any token for user " + id);
        }

        String decryptedPassword = "";
        try {
            decryptedPassword = AESEncryption.decrypt(geotrekToken.getPassword(), key);
        } catch (Exception e){
            logger.error("Error while decrypting the password from db");
            throw new BadRequestException("Internal server error when trying to find the geotrek token ...");
        }
        geotrekToken.setPassword(decryptedPassword);
        return geotrekToken;
    }
}
