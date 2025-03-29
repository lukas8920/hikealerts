package org.hikingdev.microsoft_hackathon.util.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.OkHttpClient;
import org.hikingdev.microsoft_hackathon.geotrek.api.GeotrekDbService;
import org.hikingdev.microsoft_hackathon.security.gpg.GpgService;
import org.hikingdev.microsoft_hackathon.util.json.LocalDateTimeAdapter;
import org.hikingdev.microsoft_hackathon.util.json.SerializationMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.time.LocalDateTime;

@Configuration
@Profile("prod")
public class ProdApiProvider {
    @Value("${gpg.service.url}")
    private String gpgUrl;

    @Value("${geotrek.db.service.url}")
    private String geotrekDbServiceUrl;

    @Bean("GpgService")
    public GpgService provideGpgService(){
        OkHttpClient client = new OkHttpClient.Builder().build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(gpgUrl)
                .addConverterFactory(JacksonConverterFactory.create())
                .client(client)
                .build();
        return retrofit.create(GpgService.class);
    }

    Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();

    @Bean("GeotrekDbService")
    public GeotrekDbService provideGeotrekDbService(){
        SerializationMapper serializationMapper = new SerializationMapper();
        OkHttpClient client = new OkHttpClient.Builder().build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(geotrekDbServiceUrl)
                .addConverterFactory(JacksonConverterFactory.create(serializationMapper))
                .client(client)
                .build();
        return retrofit.create(GeotrekDbService.class);
    }
}
