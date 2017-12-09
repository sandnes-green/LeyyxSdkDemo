package com.leyyx.leyyxsdk;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

final class LeyyxSdk extends LeyyxSdkEventBus implements ILeyyxSdk {
    private static final String TAG = "LeyyxSdk";

    static LeyyxSdk createInstance() {
        return new LeyyxSdk();
    }

    private LeyyxSdk() {
        super();
    }

    @Override
    public void onCreate(Activity activity) {
        SdkFeatureNoActivity feature = new SdkFeatureNoActivity(activity);
        feature.tryAutoLogin();
    }

    @Override
    public void onDestroy(Activity activity) {
    }

    @Override
    public void onStart(Activity activity) {

    }

    @Override
    public void onStop(Activity activity) {

    }

    @Override
    public void onRestart(Activity activity) {

    }

    @Override
    public void onResume(Activity activity) {

    }

    @Override
    public void onPause(Activity activity) {

    }

    @Override
    public void onNewIntent(Activity activity, Intent intent) {

    }

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult: requestCode=" + requestCode);
    }

    @Override
    public void doLogin(Activity activity, Intent data) {
        if (activity != null) {
            activity.startActivity(new Intent(activity, SdkLoginActivity.class));
        }
    }

    @Override
    public void doLogout(Activity activity, Intent data) {
        SdkFeatureNoActivity feature = new SdkFeatureNoActivity(activity);
        feature.tryLogout();
    }

    @Override
    public void doPay(Activity activity, Bundle data) {
        if (activity != null) {
            activity.startActivity(new Intent(activity,SdkPayActivity.class).putExtras(data));
        }
    }
}
