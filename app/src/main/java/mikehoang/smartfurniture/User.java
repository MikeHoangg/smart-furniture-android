package mikehoang.smartfurniture;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PUT;

public interface User {
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
