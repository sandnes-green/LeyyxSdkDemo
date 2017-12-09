package com.leyyx.leyyxsdk;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import static android.content.Context.MODE_PRIVATE;

final class SdkFeatureNoActivity {
    private static final String TAG = "SdkFeatureNoActivity";

    final private static String AUTOLOGIN_ROUTER = "";
    final private static String LOGOUT_ROUTER = "";
    final private Activity activity;

    SdkFeatureNoActivity(Activity activity) {
        this.activity = activity;
    }

    void tryLogout() {
        new HttpClient(
                new IRequestBuilder() {
                    @Override
                    RequestData build() {
                        RequestData data = new RequestData();
                        data.setPost(false).setPath(LOGOUT_ROUTER).setParameter(null);
                        return data;
                    }
                },
                new IResponseParser() {
                    @Override
                    void parse(ResponseInfo info) {
                        Log.d(TAG, "parse: code=" + info.getCode() + ", body=" + info.getBody());
                        String status = null;
                        if (info.getCode() == 200) {
                            try {
                                JSONObject json = new JSONObject(info.getBody());
                                status = json.getString("status");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            if ("1".equals(status)) {
                                SharedPreferences sp = activity.getSharedPreferences("leyyx_sdk_data", MODE_PRIVATE);
                                SharedPreferences.Editor editor = sp.edit();
                                editor.clear();
                                editor.apply();
                                Bundle data = new Bundle();
                                data.putString("data", info.getBody());
                                LeyyxSdkEventBus.dispatchEvent(ELeyyxSdkEventType.LOGOUT, 1, data);
                            } else {
                                String returnData = null;
                                Log.d("===>", info.getBody());
                                try {
                                    JSONObject jsonObject = new JSONObject(info.getBody());
                                    returnData = jsonObject.getString("info");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                Bundle data = new Bundle();
                                data.putString("data", returnData);
                                LeyyxSdkEventBus.dispatchEvent(ELeyyxSdkEventType.LOGOUT, 0, data);
                            }
                        }
                    }
                }
        ).request();
    }

    void tryAutoLogin() {
        SharedPreferences pref = activity.getSharedPreferences("leyyx_sdk_data", MODE_PRIVATE);
        final String vstr = pref.getString("vstr", "");
        if (!vstr.isEmpty()) {
            final ProgressDialog dialog = new ProgressDialog(activity);
            dialog.setProgress(ProgressDialog.STYLE_HORIZONTAL);
            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setTitle("自动登录中，请稍侯...");
            dialog.show();

            new HttpClient(
                    new IRequestBuilder() {
                        @Override
                        RequestData build() {
                            RequestData data = new RequestData();
                            String param = "";
                            try {
                                param = URLEncoder.encode(vstr, "UTF-8");
                            } catch (UnsupportedEncodingException e) {
                                Log.e(TAG, "parse: error.");
                                e.printStackTrace();
                            }

                            data.setPost(true).setPath(AUTOLOGIN_ROUTER).setParameter("vstr=" + param);

                            return data;
                        }
                    },
                    new IResponseParser() {
                        @Override
                        void parse(ResponseInfo info) {
                            Log.d(TAG, "parse: code=" + info.getCode() + ", body=" + info.getBody());
                            String status = null;
                            if (info.getCode() == 200) {
                                String vstr = null;
                                try {
                                    JSONObject json = new JSONObject(info.getBody());
                                    status = json.getString("status");
                                    JSONObject dataNode = json.getJSONObject("data");
                                    vstr = dataNode.getString("vstr");

                                } catch (JSONException e) {
                                    Log.e(TAG, "parse: error.");
                                    e.printStackTrace();
                                }
                                if ("-1".equals(status)) {
                                    SharedPreferences.Editor pref = activity.getSharedPreferences("leyyx_sdk_data", MODE_PRIVATE).edit();
                                    pref.putString("vstr", vstr);
                                    pref.apply();
                                    Bundle data = new Bundle();
                                    data.putString("data", info.getBody());
                                    LeyyxSdkEventBus.dispatchEvent(ELeyyxSdkEventType.LOGIN, 1, data);
                                } else {
                                    Bundle data = new Bundle();
                                    data.putString("data", info.getBody());
                                    LeyyxSdkEventBus.dispatchEvent(ELeyyxSdkEventType.LOGIN, 0, data);
                                }
                            } else {
                                Bundle data = new Bundle();
                                data.putString("data", "自动登录失败");
                                LeyyxSdkEventBus.dispatchEvent(ELeyyxSdkEventType.LOGIN, 0, data);
                            }
                            dialog.dismiss();
                        }
                    }
            ).request();
        } else {
            Bundle data = new Bundle();
            data.putString("data", "自动登录失败");
            LeyyxSdkEventBus.dispatchEvent(ELeyyxSdkEventType.LOGIN, 0, data);
        }
    }
}
