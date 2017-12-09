package com.leyyx.leyyxsdk;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class SdkRegisterActivity extends AppCompatActivity implements View.OnClickListener, MessageHandlerProvider {
    private EditText etUsername;
    private EditText password;
    private EditText password1;
    private ImageButton seePwd1;
    private ImageButton seePwd2;

    private ImageButton verify_img;
    private EditText verify_code;
    private String verifyId = null;

    private static final String TAG = "SdkRegisterActivity";
    private static final String ROOT_ROUTER = "";
    private static final String REGISTER_ROUTER = ROOT_ROUTER + "";
    private static final String VERIFY_CREATE_ROUTER = ROOT_ROUTER + "";

    private static final int REGISTER_SUCC = 1;
    private static final int REGISTER_ERR = 2;
    private static final int VERIFY_CODE = 3;

     @Override
     public void handleMessage(Message msg) {
        switch (msg.what) {
            case REGISTER_SUCC:
                finish();
                break;
            case REGISTER_ERR:
                break;
            case VERIFY_CODE:
                verify_img.setImageBitmap((Bitmap) msg.obj);
                break;
            default:
                break;
        }
     }
    private MessageHandler<SdkRegisterActivity> handler = new MessageHandler<>(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sdk_register);
        etUsername = (EditText) findViewById(R.id.etUsername);
        password = (EditText) findViewById(R.id.etPassword);
        password1 = (EditText) findViewById(R.id.etPassword1);

        Button btn_submit = (Button) findViewById(R.id.btn_submit);
        btn_submit.setOnClickListener(this);

        seePwd1 = (ImageButton) findViewById(R.id.seePwd1);
        seePwd2 = (ImageButton) findViewById(R.id.seePwd2);

        seePwd1.setOnClickListener(this);
        seePwd2.setOnClickListener(this);

        verify_img = (ImageButton) findViewById(R.id.verify_image);
        verify_img.setOnClickListener(this);
        verify_code = (EditText) findViewById(R.id.verify_code);


        createVerify();
    }

    Boolean checkPostData(String username, String pwd1, String pwd2,String verifyCode){
        if (TextUtils.isEmpty(pwd1) || TextUtils.isEmpty(pwd2) || TextUtils.isEmpty(verifyCode) || TextUtils.isEmpty(username)) {
            Toast.makeText(SdkRegisterActivity.this, "密码、验证码、用户名不能为空", Toast.LENGTH_LONG).show();
            return false;
        } else if (username.length() < 4 || username.length() > 12) {
            Toast.makeText(SdkRegisterActivity.this, "用户名长度不合法", Toast.LENGTH_LONG).show();
            return false;
        } else if (!pwd1.equals(pwd2)) {
            Toast.makeText(SdkRegisterActivity.this, "两次密码输入不一致", Toast.LENGTH_LONG).show();
            return false;
        } else if(pwd1.length() < 4 || pwd1.length() > 12){
            Toast.makeText(SdkRegisterActivity.this, "密码长度不合法", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        final String pwd1 = password.getText().toString();
        final String pwd2 = password1.getText().toString();
        final String username = etUsername.getText().toString();
        final String verifyCode = verify_code.getText().toString();
        int i = v.getId();
        if (i == R.id.verify_image) {
            createVerify();
        } else if (i == R.id.btn_submit) {
            //参数检查
           if (!checkPostData(username,pwd1,pwd2,verifyCode)){
               return;
           }
            new HttpClient(new IRequestBuilder() {
                @Override
                RequestData build() {
                    StringBuilder sb = new StringBuilder();
                    Log.d("verifyCode",verifyCode);
                    Log.d("verifyId",verifyId);
                    sb.append("username=").append(username).append("&password=").append(pwd1).append("&verifyCode=").append(verifyCode).append("&verifyId=").append(verifyId);

                    RequestData data = new RequestData();
                    data.setPost(true).setPath(REGISTER_ROUTER).setParameter(sb.toString());

                    return data;
                }
            }, new IResponseParser() {
                @Override
                void parse(ResponseInfo info) {
                    Log.d(TAG, "onResponse: code:" + info.getCode() + ", body:" + info.getBody());
                    String status = null;
                    String vstr = null;
                    if (info.getCode() == 200) {
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
                            SharedPreferences.Editor pref = getSharedPreferences("leyyx_sdk_data", MODE_PRIVATE).edit();
                            pref.putString("vstr", vstr);
                            pref.apply();
                            Bundle data = new Bundle();
                            data.putString("data", info.getBody());
                            LeyyxSdkEventBus.dispatchEvent(ELeyyxSdkEventType.REGISTER, 1, data);
                        } else {
                            Bundle data = new Bundle();
                            data.putString("data", info.getBody());
                            LeyyxSdkEventBus.dispatchEvent(ELeyyxSdkEventType.REGISTER, 0, data);
                        }
                    } else {
                        Bundle data = new Bundle();
                        data.putString("data", "网络连接错误");
                        LeyyxSdkEventBus.dispatchEvent(ELeyyxSdkEventType.REGISTER, 0, data);
                    }
                    finish();
                }
            }).request();
        } else if (i == R.id.seePwd1) {
            String tag = (String) seePwd1.getTag();
            if (tag.equals("invisible")) {
                seePwd1.setTag("visible");
                seePwd1.setImageResource(R.drawable.visible);
                //注意：通过代码给InputType赋值时，不是设置TYPE_XXX_VARIATION_YYY，而是要设置TYPE_CLASS_XXX | TYPE_XXXX_VARAITION_YYY
                password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                seePwd1.setTag("invisible");
                seePwd1.setImageResource(R.drawable.invisible);
                password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
        } else if (i == R.id.seePwd2) {
            String tag = (String) seePwd2.getTag();
            if (tag.equals("invisible")) {
                seePwd2.setTag("visible");
                seePwd2.setImageResource(R.drawable.visible);
                password1.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                seePwd2.setTag("invisible");
                seePwd2.setImageResource(R.drawable.invisible);
                password1.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.destory();
    }

    public Bitmap getInternetPicture(String urlPath) {

        Bitmap bm = null;

        try {
            URL uri = new URL(urlPath);

            HttpURLConnection connection = (HttpURLConnection) uri.openConnection();
            connection.setRequestMethod("GET");
            connection.setReadTimeout(5000);
            connection.setConnectTimeout(5000);
            connection.connect();
            if (connection.getResponseCode() == 200) {
                InputStream is = connection.getInputStream();
                bm = BitmapFactory.decodeStream(is);
                Log.i("", "网络请求成功");
                connection.disconnect();
            } else {
                Log.v("tag", "网络请求失败");
                bm = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bm;

    }

    private void createVerify(){
        new HttpClient(new IRequestBuilder() {
            @Override
            RequestData build() {
                IRequestBuilder.RequestData data = new IRequestBuilder.RequestData();
                return data.setPost(false).setPath(VERIFY_CREATE_ROUTER);
            }
        }, new IResponseParser() {
            @Override
            void parse(ResponseInfo info) {
                if (info.getCode() == 200) {
                    try {
                        JSONObject json = new JSONObject(info.getBody());
                        String status = json.getString("status");
                        JSONObject dataNode = json.getJSONObject("data");
                        verifyId = dataNode.getString("id");
                        String verifyImgUrl = dataNode.getString("url");
                        if (!"1".equals(status)) {
                            Toast.makeText(SdkRegisterActivity.this, "验证码创建失败", Toast.LENGTH_LONG).show();
                        } else {
                            getVerifyImg(verifyImgUrl);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else {
                    Toast.makeText(SdkRegisterActivity.this,"获取验证码失败",Toast.LENGTH_LONG).show();
                }
            }
        }).request();
    }


    void getVerifyImg(final String imgUrl) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String urlPath = ROOT_ROUTER + "/" + imgUrl;
                Bitmap bm = getInternetPicture(urlPath);
                Message msg = new Message();
                msg.obj = bm;
                msg.what = VERIFY_CODE;
                handler.sendMessage(msg);
            }
        }).start();
    }
}
