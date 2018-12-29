package mikehoang.smartfurniture;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface Furniture {
    @GET("api/v1/furniture")
    Call<Response> getAllFurniture(@Header("Authorization") String token);

    @POST("api/v1/furniture")
    Call<Response> createFurniture(@Header("Authorization") String token, @Query("code") String code, @Query("brand") String brand, @Query("type") String type, @Query("is_public") Boolean isPublic, @Query("owner") int owner);

    @GET("api/v1/furniture/{id}")
    Call<Response> getFurniture(@Header("Authorization") String token, @Path("id") int id);

    @PUT("api/v1/furniture/{id}")
    Call<Response> editFurniture(@Header("Authorization") String token, @Path("id") int id, @Query("code") String code, @Query("brand") String brand, @Query("type") String type, @Query("is_public") Boolean isPublic);
}
