package org.hikingdev.microsoft_hackathon.util;

import okhttp3.OkHttpClient;
import org.hikingdev.microsoft_hackathon.security.gpg.GpgService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Configuration
@Profile("prod")
public class ApiProvider {
    @Value("${gpg.service.url}")
    private String gpgUrl;

    @Bean("GpgService")
    public GpgService provideGpgService(){
        OkHttpClient client = new OkHttpClient.Builder().build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(gpgUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
        return retrofit.create(GpgService.class);
    }
}
