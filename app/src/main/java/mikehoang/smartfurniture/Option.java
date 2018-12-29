package mikehoang.smartfurniture;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface Option {
    @POST("furniture/")
    Call<ResponseBody> createOptions(@Header("Authorization") String token, @Query("name") String name, @Query("type") String type, @Query("length") double length, @Query("height") double height, @Query("width") double width, @Query("incline") double incline, @Query("temperature") double temperature, @Query("rigidity") String rigidity, @Query("massage") String massage, @Query("creator") int creator);

    @GET("options/{id}/")
    Call<ResponseBody> getOptions(@Header("Authorization") String token, @Path("id") int id);

    @PUT("options/{id}/")
    Call<ResponseBody> editOptions(@Header("Authorization") String token, @Path("id") int id, @Query("code") String code, @Query("brand") String brand, @Query("type") String type, @Query("is_public") Boolean isPublic);
}
