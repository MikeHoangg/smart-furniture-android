package mikehoang.smartfurniture;

import java.util.Arrays;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface API {
    String BASE_URL = "http://172.20.10.3:8000/en/api/v1/";
    List<String> FURNITURE_LIST = Arrays.asList("owned_furniture",
            "allowed_furniture",
            "current_furniture");

    @FormUrlEncoded
    @POST("login/")
    Call<ResponseBody> login(@Field("username") String username,
                             @Field("password") String password);

    @POST("logout/")
    Call<ResponseBody> logout();

    @FormUrlEncoded
    @POST("register/")
    Call<ResponseBody> register(@Field("username") String username,
                                @Field("email") String email,
                                @Field("password1") String password1,
                                @Field("password2") String password2);

    @FormUrlEncoded
    @POST("apply-options/")
    Call<ResponseBody> applyOptions(@Header("Authorization") String token,
                                    @Field("options") int options,
                                    @Field("furniture") int furniture);

    @FormUrlEncoded
    @POST("discard-options/")
    Call<ResponseBody> discardOptions(@Header("Authorization") String token,
                                      @Field("user") int user,
                                      @Field("furniture") int furniture);

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
    @PUT("furniture/{id}/")
    Call<ResponseBody> editFurniture(@Header("Authorization") String token,
                                     @Path("id") int id,
                                     @Field("code") String code,
                                     @Field("brand") String brand,
                                     @Field("type") String type,
                                     @Field("is_public") Boolean isPublic);

    @DELETE("furniture/{id}/")
    Call<ResponseBody> deleteFurniture(@Header("Authorization") String token,
                                       @Path("id") int id);

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

    @DELETE("options/{id}/")
    Call<ResponseBody> deleteOptions(@Header("Authorization") String token,
                                     @Path("id") int id);

    @GET("furniture-types/")
    Call<ResponseBody> getFurnitureTypes();

    @GET("massage-rigidity-types/")
    Call<ResponseBody> getRigidityMassageTypes();

    @GET("user/")
    Call<ResponseBody> getCurrentUser(@Header("Authorization") String token);

    @FormUrlEncoded
    @PUT("user/")
    Call<ResponseBody> editCurrentUser(@Header("Authorization") String token,
                                       @Field("username") String username,
                                       @Field("email") String email,
                                       @Field("first_name") String firstName,
                                       @Field("last_name") String lastName,
                                       @Field("height") double height);
}
