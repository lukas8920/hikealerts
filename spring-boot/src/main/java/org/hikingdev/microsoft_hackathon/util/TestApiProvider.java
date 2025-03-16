package org.hikingdev.microsoft_hackathon.util;

import okhttp3.OkHttpClient;
import org.hikingdev.microsoft_hackathon.geotrek.GeotrekDbService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Configuration
@Profile("test")
public class TestApiProvider {
    @Bean("GeotrekDbService")
    public GeotrekDbService provideGeotrekDbService(){
        OkHttpClient client = new OkHttpClient.Builder().build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://localhost:8000")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
        return retrofit.create(GeotrekDbService.class);
    }
}
