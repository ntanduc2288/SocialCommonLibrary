package vinasource.com.commonlibrary.instances;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.Arrays;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import vinasource.com.commonlibrary.activity.BaseFacebookHelperActivity;
import vinasource.com.commonlibrary.interfaces.IFacebookCallback;
import vinasource.com.commonlibrary.objects.FacebookUser;

/**
 * Created by user on 4/11/16.
 */
public final class FacebookHelper {

    private static FacebookHelper instance;
    private String[] loginPermission = {"public_profile", "user_friends", "email", "user_birthday"};
    private String facebookUserParams = "id, name, link, email, birthday, devices, gender, age_range, first_name, last_name, location, locale";

    public static FacebookHelper getInstance() {
        if (instance == null) {
            synchronized (FacebookHelper.class) {
                if (instance == null) {
                    instance = new FacebookHelper();
                }
            }
        }
        return instance;
    }

    private FacebookHelper() {
    }

    //GET FACEBOOK USER INFO SECTION    ///////////////////////////////////
    public void getFacebookUserInfo(@NonNull BaseFacebookHelperActivity activity, @NonNull IFacebookCallback<FacebookUser, Exception> iFacebookCallback) {
        getFacebookUserInfo(activity, activity.getCallbackManager(), iFacebookCallback);
    }

    /**
     * get facebook user info
     * Note: if activity is not instance of BaseFacebookHelperActivity, you must declare CallbackManager in your activity like following
     * OnCreate() -> callbackManager = CallbackManager.Factory.create();
     * OnActivityForResult() -> callbackManager.onActivityResult(requestCode, resultCode, data);
     *
     * @param activity
     * @param callbackManager
     * @param iFacebookCallback
     */
    public void getFacebookUserInfo(@NonNull Activity activity, @NonNull CallbackManager callbackManager, @NonNull IFacebookCallback<FacebookUser, Exception> iFacebookCallback) {
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                getUserInfoRxJava(AccessToken.getCurrentAccessToken(), iFacebookCallback);
            }

            @Override
            public void onCancel() {
                Log.d(FacebookHelper.class.getSimpleName(), "FbUser cancel");
                iFacebookCallback.requestCanceled();
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(FacebookHelper.class.getSimpleName(), "FbUser error: " + error.getMessage());
                iFacebookCallback.error(error);
            }
        });

        if (AccessToken.getCurrentAccessToken() == null || AccessToken.getCurrentAccessToken().isExpired()) {
            LoginManager.getInstance().logInWithReadPermissions(activity, Arrays.asList(loginPermission));
        } else {
            getUserInfoRxJava(AccessToken.getCurrentAccessToken(), iFacebookCallback);
        }
    }


    private void getUserInfoRxJava(@NonNull AccessToken accessToken, IFacebookCallback iFacebookCallback) {


        rx.Observable.just(accessToken)
                .map(accessTokenTmp -> getFbUserJsonObject(accessTokenTmp))
                //Map json response to facebookUser object
                .map(jsonObject1 -> {
                    Log.d(FacebookHelper.class.getSimpleName(), "FB response: " + jsonObject1);
                    Gson gson = new Gson();
                    FacebookUser facebookUser = gson.fromJson(jsonObject1.toString(), FacebookUser.class);
                    return facebookUser;
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(facebookUser -> {
                    iFacebookCallback.responseData(facebookUser);
                });


    }

    public void logoutFacebook() {
        LoginManager.getInstance().logOut();
    }

    private JSONObject getFbUserJsonObject(@NonNull AccessToken accessToken) {
        GraphRequest graphRequest = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
            }
        });
        GraphRequest.newMeRequest(accessToken, (jsonObject, response) -> {
        });
        Bundle parameters = new Bundle();
        parameters.putString("fields", facebookUserParams);
        graphRequest.setParameters(parameters);

        GraphResponse graphResponse = graphRequest.executeAndWait();

        JSONObject object = graphResponse.getJSONObject();

        return object;
    }

    //SHARE SECTION    ///////////////////////////////////
    public void shareLink(BaseFacebookHelperActivity activity, String link, String title, String description, String imageLink, IFacebookCallback<Sharer.Result, FacebookException> iFacebookCallback) {
        shareLink(activity, activity.getCallbackManager(), link, title, description, imageLink, iFacebookCallback);
    }

    public void shareLink(Activity activity, CallbackManager callbackManager, String link, String title, String description, String imageLink, IFacebookCallback<Sharer.Result, FacebookException> iFacebookCallback) {

        ShareDialog shareDialog = new ShareDialog(activity);
        shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
            @Override
            public void onSuccess(Sharer.Result result) {
                iFacebookCallback.responseData(result);
            }

            @Override
            public void onCancel() {
                iFacebookCallback.requestCanceled();
            }

            @Override
            public void onError(FacebookException error) {
                iFacebookCallback.error(error);
            }
        });
        if (ShareDialog.canShow(ShareLinkContent.class)) {
            ShareLinkContent shareLinkContent = new ShareLinkContent.Builder()
                    .setContentUrl(Uri.parse(link))
                    .setContentTitle(title)
                    .setContentDescription(description)
//                .setImageUrl(Uri.parse(imageLink))
                    .build();
            shareDialog.show(shareLinkContent);
        }
    }


}
