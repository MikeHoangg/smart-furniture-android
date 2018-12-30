package mikehoang.smartfurniture;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ApplyOptions {
    @FormUrlEncoded
    @POST("apply-options")
    Call<ResponseBody> applyOptions(@Header("Authorization") String token,
                                    @Field("options") int options,
                                    @Field("furniture") int furniture);

    @FormUrlEncoded
    @POST("discard-options")
    Call<ResponseBody> discardOptions(@Header("Authorization") String token,
                                      @Field("user") int user,
                                      @Field("furniture") int furniture);
}
