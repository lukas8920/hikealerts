package org.hikingdev.microsoft_hackathon.geotrek;

import org.hikingdev.microsoft_hackathon.geotrek.entities.GeotrekUser;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface GeotrekDbService {
    @POST("/v1/geotrek/user")
    Call<Void> postUser(@Body GeotrekUser geotrekUser);
}
