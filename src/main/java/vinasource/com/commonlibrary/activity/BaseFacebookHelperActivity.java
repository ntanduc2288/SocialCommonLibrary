package vinasource.com.commonlibrary.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import com.facebook.CallbackManager;

/**
 * Created by user on 4/12/16.
 */
public abstract class BaseFacebookHelperActivity extends FragmentActivity{

    private CallbackManager callbackManager;

    public abstract int getLayoutResId();

    public abstract void setupViews();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        callbackManager = CallbackManager.Factory.create();
        setContentView(getLayoutResId());
        setupViews();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public final CallbackManager getCallbackManager(){
        return callbackManager;
    }
}
