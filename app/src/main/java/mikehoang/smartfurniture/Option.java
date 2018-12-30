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

public interface Option {
    @FormUrlEncoded
    @POST("options/")
    Call<ResponseBody> createOptions(@Header("Authorization") String token,
                                     @Field("name") String name,
                                     @Field("type") String type,
                                     @Field("height") double height,
                                     @Field("length") double length,
                                     @Field("width") double width,
                                     @Field("incline") double incline,
                                     @Field("temperature") double temperature,
                                     @Field("rigidity") String rigidity,
                                     @Field("massage") String massage,
                                     @Field("creator") int creator);

    @GET("options/{id}/")
    Call<ResponseBody> getOptions(@Header("Authorization") String token,
                                  @Path("id") int id);

    @FormUrlEncoded
    @PUT("options/{id}/")
    Call<ResponseBody> editOptions(@Header("Authorization") String token,
                                   @Path("id") int id,
                                   @Field("code") String code,
                                   @Field("brand") String brand,
                                   @Field("type") String type,
                                   @Field("is_public") Boolean isPublic);
}
