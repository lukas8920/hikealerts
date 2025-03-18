package org.hikingdev.microsoft_hackathon.geotrek;

import org.hikingdev.microsoft_hackathon.geotrek.entities.GeotrekUser;
import org.hikingdev.microsoft_hackathon.geotrek.entities.Salt;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface GeotrekDbService {
    @POST("/v1/geotrek/user")
    Call<Void> postUser(@Body GeotrekUser geotrekUser);

    @GET("/v1/geotrek/salt")
    Call<Salt> getSalt();
}
