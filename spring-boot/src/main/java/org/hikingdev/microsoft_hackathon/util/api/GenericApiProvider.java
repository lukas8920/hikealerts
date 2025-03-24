package org.hikingdev.microsoft_hackathon.util.api;

import okhttp3.OkHttpClient;
import org.hikingdev.microsoft_hackathon.geotrek.api.GeonamesService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Configuration
public class GenericApiProvider {
    @Bean("GeonamesService")
    public GeonamesService provideGeonamesService(){
        OkHttpClient client = new OkHttpClient.Builder().build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://api.geonames.org")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
        return retrofit.create(GeonamesService.class);
    }
}
