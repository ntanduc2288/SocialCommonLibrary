package vinasource.com.commonlibrary;

import android.app.Application;

import com.facebook.FacebookSdk;

/**
 * Created by user on 4/12/16.
 */
public abstract class BaseFacebookApplication extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        FacebookSdk.sdkInitialize(getApplicationContext());
        FacebookSdk.setApplicationId(getFacebookAppId());
    }

    public abstract String getFacebookAppId();
}
