package lamborghini.wallpapers.CarWallpapers.CarSounds.rests;

import lamborghini.wallpapers.CarWallpapers.CarSounds.Config;
import lamborghini.wallpapers.CarWallpapers.CarSounds.callbacks.CallbackCategories;
import lamborghini.wallpapers.CarWallpapers.CarSounds.callbacks.CallbackCategoryDetails;
import lamborghini.wallpapers.CarWallpapers.CarSounds.callbacks.CallbackListVideo;
import lamborghini.wallpapers.CarWallpapers.CarSounds.models.Setting;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

public interface ApiInterface {

    String CACHE = "Cache-Control: max-age=0";
    String AGENT = "Data-Agent: Your Videos Channel";
    String API_KEY = Config.API_KEY;

    @Headers({CACHE, AGENT})
    @GET("api/get_posts/?api_key=" + API_KEY)
    Call<CallbackListVideo> getPostByPage(
            @Query("page") int page,
            @Query("count") int count
    );

    @Headers({CACHE, AGENT})
    @GET("api/get_category_index/?api_key=" + API_KEY)
    Call<CallbackCategories> getAllCategories();

    @Headers({CACHE, AGENT})
    @GET("api/get_category_posts/?api_key=" + API_KEY)
    Call<CallbackCategoryDetails> getCategoryDetailsByPage(
            @Query("id") int id,
            @Query("page") int page,
            @Query("count") int count
    );

    @Headers({CACHE, AGENT})
    @GET("api/get_search_results/?api_key=" + API_KEY)
    Call<CallbackListVideo> getSearchPosts(
            @Query("search") String search,
            @Query("count") int count
    );

    @Headers({CACHE, AGENT})
    @GET("api/get_privacy_policy/?api_key=" + API_KEY)
    Call<Setting> getPrivacyPolicy();
/*
    @Headers({CACHE, AGENT})
    @GET("api/get_user_token")
    Call<CallbackUser> getUserToken(@Query("user_unique_id") String user_unique_id);
*/
    @Headers({CACHE, AGENT})
    @GET("api/get_package_name")
    Call<Setting> getPackageName();
/*
    @Headers({CACHE, AGENT})
    @GET("api/get_post_detail")
    Call<CallbackVideoDetail> getPostDetail(
            @Query("id") String id
    );
*/
}
