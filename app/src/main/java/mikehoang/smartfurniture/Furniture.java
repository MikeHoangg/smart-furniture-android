package mikehoang.smartfurniture;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface Furniture {
    @FormUrlEncoded
    @POST("furniture/")
    Call<ResponseBody> createFurniture(@Header("Authorization") String token,
                                       @Field("code") String code,
                                       @Field("brand") String brand,
                                       @Field("type") String type,
                                       @Field("is_public") Boolean isPublic,
                                       @Field("owner") int owner);

    @GET("furniture/{id}/")
    Call<ResponseBody> getFurniture(@Header("Authorization") String token,
                                    @Path("id") int id);

    @FormUrlEncoded
    @PUT("api/v1/furniture/{id}")
    Call<ResponseBody> editFurniture(@Header("Authorization") String token,
                                     @Path("id") int id,
                                     @Field("code") String code,
                                     @Field("brand") String brand,
                                     @Field("type") String type,
                                     @Field("is_public") Boolean isPublic);
}
