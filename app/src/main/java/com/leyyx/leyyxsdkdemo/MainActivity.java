package com.leyyx.leyyxsdkdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.leyyx.leyyxsdk.ILeyyxSdk;
import com.leyyx.leyyxsdk.ILeyyxSdkEventBus;
import com.leyyx.leyyxsdk.ELeyyxSdkEventType;
import com.leyyx.leyyxsdk.ILeyyxSdkEventListener;
import com.leyyx.leyyxsdk.LeyyxSdkFactory;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, ILeyyxSdkEventListener {
    private static final String TAG = "MainActivity";

    private ILeyyxSdk leyyxSdk;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageButton back = (ImageButton) findViewById(R.id.back);
        back.setOnClickListener(this);

        Button btnLogin = (Button) findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(this);

        Button btnLogout = (Button) findViewById(R.id.btn_logout);
        btnLogout.setOnClickListener(this);

        Button btnPay = (Button) findViewById(R.id.btn_pay);
        btnPay.setOnClickListener(this);

        leyyxSdk = LeyyxSdkFactory.createInstance();

        ILeyyxSdkEventBus eb = (ILeyyxSdkEventBus) leyyxSdk;
        eb.addEventListener(ELeyyxSdkEventType.LOGOUT.intVal(), this);
        eb.addEventListener(ELeyyxSdkEventType.LOGIN.intVal(), this);
        eb.addEventListener(ELeyyxSdkEventType.PAY.intVal(), this);
        leyyxSdk.onCreate(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login:
                leyyxSdk.doLogin(this, null);
                break;
            case R.id.btn_logout:
                leyyxSdk.doLogout(this, null);
                break;
            case R.id.btn_pay:
                //模拟订单数据
                String amount = "0.05";
                String user_id = "1";
                String game_id = "1";
                String server_id = "1";
                Log.d(TAG + "onclick on ", "btn_pay");
                Bundle bundle = new Bundle();
                bundle.putString("user_id", user_id);
                bundle.putString("amount", amount);
                bundle.putString("game_id", game_id);
                bundle.putString("server_id", server_id);
                leyyxSdk.doPay(this, bundle);
                break;
            case R.id.back:
                finish();
                break;

            default:
                break;
        }
    }

    @Override
    public Boolean onLeyyxSdkEvent(ELeyyxSdkEventType type, int resultCode, Bundle data) {
        Log.d(TAG, String.format("onLeyyxSdkEvent: type=%s, resultCode=%d", type.toString(), resultCode));
        String info = data.getString("data");
        if (type == ELeyyxSdkEventType.LOGIN) {
            if (resultCode == 1) {
                Log.d(TAG + "==>", info);
            } else {
                Log.d(TAG + "==>", info);
            }
        } else if (type == ELeyyxSdkEventType.PAY) {
            if (resultCode == 1) {
                Log.d(TAG + "==>", info);
            } else {
                Log.d(TAG + "==>", info);
             }
        } else if (type == ELeyyxSdkEventType.LOGOUT) {
            if (resultCode == 1) {
                Log.d(TAG + "==>", info);
            } else {
                Log.d(TAG + "==>", info);
            }
        } else if (type == ELeyyxSdkEventType.REGISTER) {

            if (resultCode == 1) {
                Log.d(TAG + "==>", info);
            } else {
                Log.d(TAG + "==>", info);
            }
        }
        return true;
    }
}
