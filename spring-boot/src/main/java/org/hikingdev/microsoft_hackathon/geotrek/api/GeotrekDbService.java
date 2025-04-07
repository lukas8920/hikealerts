package org.hikingdev.microsoft_hackathon.geotrek.api;

import org.hikingdev.microsoft_hackathon.geotrek.entities.GeotrekTrail;
import org.hikingdev.microsoft_hackathon.geotrek.entities.GeotrekUser;
import org.hikingdev.microsoft_hackathon.geotrek.entities.Salt;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;

public interface GeotrekDbService {
    @POST("/v1/geotrek/user")
    Call<Void> postUser(@Body GeotrekUser geotrekUser);

    @GET("/v1/geotrek/salt")
    Call<Salt> getSalt();

    @POST("/v1/geotrek/trail")
    Call<GeotrekTrail> postTrail(@Body GeotrekTrail geotrekTrail);

    @GET("/v1/geotrek/trail")
    Call<List<GeotrekTrail>> findTrails(@Query("id") Long id);

    @DELETE("/v1/geotrek/trail")
    void deleteTrails(@Body List<GeotrekTrail> geotrekTrails);
}
