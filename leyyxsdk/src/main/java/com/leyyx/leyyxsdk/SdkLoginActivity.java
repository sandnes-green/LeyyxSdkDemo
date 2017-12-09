package com.leyyx.leyyxsdk;


import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

final public class SdkLoginActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "SdkLoginActivity";

    private ImageButton seePwd;
    private EditText etUsername;
    private EditText etPassword;

    final private static String LOGIN_ROUTER = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sdk_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        //toolBar回退按钮
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Button btnLogin = (Button) findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(this);

        etUsername = (EditText) findViewById(R.id.etUsername);
        etPassword = (EditText) findViewById(R.id.etPassword);


        TextView register = (TextView) findViewById(R.id.register);
        register.setOnClickListener(this);

        seePwd = (ImageButton) findViewById(R.id.seePwd);
        seePwd.setOnClickListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //回退按钮
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_login) {
            final String userName = etUsername.getText().toString();
            final String password = etPassword.getText().toString();
            Log.d(TAG, "onClick: login button");

            new HttpClient(new IRequestBuilder() {
                @Override
                RequestData build() {
                    String param = "username=" + userName + "&password=" + password;
                    IRequestBuilder.RequestData data = new IRequestBuilder.RequestData();
                    data.setPost(true).setPath(LOGIN_ROUTER).setParameter(param);
                    return data;
                }
            }, new IResponseParser() {
                @Override
                void parse(ResponseInfo info) {
                    Log.d(TAG, "onResponse: code:" + info.getCode() + ", body:" + info.getBody());

                    if (info.getCode() == 200) {
                        String vstr = null;
                        String status = null;
                        try {
                            JSONObject json = new JSONObject(info.getBody());
                            status = json.getString("status");
                            JSONObject dataNode = json.getJSONObject("data");
                            vstr = dataNode.getString("vstr");

                        } catch (JSONException e) {
                            Log.e(TAG, "parse: error.");
                            e.printStackTrace();
                        }
                        if ("1".equals(status)) {
                            if (vstr != null) {
                                SharedPreferences.Editor pref = getSharedPreferences("leyyx_sdk_data", MODE_PRIVATE).edit();
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
                            data.putString("data", info.getBody());
                            LeyyxSdkEventBus.dispatchEvent(ELeyyxSdkEventType.LOGIN, 0, data);
                        }
                        finish();
                    } else {
                        Bundle data = new Bundle();
                        data.putString("data", "网络错误");
                        LeyyxSdkEventBus.dispatchEvent(ELeyyxSdkEventType.LOGIN, 0, data);
                    }
                }
            }).request();
        } else if (v.getId() == R.id.register) {
            Intent intent = new Intent(SdkLoginActivity.this, SdkRegisterActivity.class);
            startActivity(intent);
        } else if (v.getId() == R.id.seePwd) {
            String tag = (String) seePwd.getTag();
            if (tag.equals("invisible")) {
                seePwd.setTag("visible");
                seePwd.setImageResource(R.drawable.visible);
                //注意：通过代码给InputType赋值时，不是设置TYPE_XXX_VARIATION_YYY，而是要设置TYPE_CLASS_XXX | TYPE_XXXX_VARAITION_YYY
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                seePwd.setTag("invisible");
                seePwd.setImageResource(R.drawable.invisible);
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
        }
    }
}
