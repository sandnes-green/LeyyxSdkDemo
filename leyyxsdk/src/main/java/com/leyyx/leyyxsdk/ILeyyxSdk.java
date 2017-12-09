package com.leyyx.leyyxsdk;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public interface ILeyyxSdk {
    void onCreate(Activity activity);
    void onDestroy(Activity activity);
    void onStart(Activity activity);
    void onStop(Activity activity);
    void onRestart(Activity activity);
    void onResume(Activity activity);
    void onPause(Activity activity);
    void onNewIntent(Activity activity, Intent intent);
    void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data);

    void doLogin(Activity activity, Intent data);
    void doLogout(Activity activity, Intent data);
    void doPay(Activity activity, Bundle data);
}
