package org.hikingdev.microsoft_hackathon.util.api;

import okhttp3.OkHttpClient;
import org.hikingdev.microsoft_hackathon.geotrek.api.GeotrekDbService;
import org.hikingdev.microsoft_hackathon.util.json.SerializationMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

@Configuration
@Profile("test")
public class TestApiProvider {
    @Bean("GeotrekDbService")
    public GeotrekDbService provideGeotrekDbService(){
        SerializationMapper serializationMapper = new SerializationMapper();
        OkHttpClient client = new OkHttpClient.Builder().build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://localhost:8000")
                .addConverterFactory(JacksonConverterFactory.create(serializationMapper))
                .client(client)
                .build();
        return retrofit.create(GeotrekDbService.class);
    }
}
