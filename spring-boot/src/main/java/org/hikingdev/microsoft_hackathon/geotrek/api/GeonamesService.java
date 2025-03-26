package org.hikingdev.microsoft_hackathon.geotrek.api;

import org.hikingdev.microsoft_hackathon.geotrek.entities.GeonamesResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GeonamesService {
    @GET("/countryCodeJSON")
    public Call<GeonamesResponse> countryCode(@Query("lng") double x, @Query("lat") double y, @Query("username") String username);
}
