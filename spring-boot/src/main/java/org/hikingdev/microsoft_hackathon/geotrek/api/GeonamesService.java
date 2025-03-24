package org.hikingdev.microsoft_hackathon.geotrek.api;

import org.hikingdev.microsoft_hackathon.geotrek.entities.GeonamesResponse;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface GeonamesService {
    @GET("/countryCodeJSON")
    public GeonamesResponse countryCode(@Path("lng") double x, @Path("lat") double y, @Path("username") String username);
}
