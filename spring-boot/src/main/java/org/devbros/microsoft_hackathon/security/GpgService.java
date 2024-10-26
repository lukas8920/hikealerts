package org.devbros.microsoft_hackathon.security;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GpgService {
    @GET("get_password")
    Call<GpgSecret> getSecret(@Query("entry") String key);
}
