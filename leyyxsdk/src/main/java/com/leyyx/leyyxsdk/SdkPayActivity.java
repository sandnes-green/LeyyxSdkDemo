package com.leyyx.leyyxsdk;

import android.icu.text.LocaleDisplayNames;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.alipay.sdk.app.EnvUtils;
import com.alipay.sdk.app.PayTask;
import com.example.alipay.PayResult;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;


final public class SdkPayActivity extends AppCompatActivity implements View.OnClickListener, MessageHandlerProvider, ActivityCompat.OnRequestPermissionsResultCallback {

    public static final int ALI_PAY_FLAG = 1;
    public static final int WX_AUTH_FLAG = 2;
    public static final int PAY_ERROR = 3;

    //得到APP传入的数据
    private String amount = "";
    private String user_id = "";
    private String game_id = "";
    private String server_id = "";
    private String res_xml ="";
    private String req_xml = "";
    private String ceshi = "";


    private String resultInfo = null;
    private String resultStatus = null;
    String responseOrder = null;
    JSONObject dataNode;

    final private static String ALIPAY_ROUTER = "";
    final private static String WXPAY_ROUTER = "";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EnvUtils.setEnv(EnvUtils.EnvEnum.SANDBOX);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sdk_pay);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        //toolBar回退按钮
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle bundle = getIntent().getExtras();
        amount = bundle.getString("amount");
        user_id = bundle.getString("user_id");
        game_id = bundle.getString("game_id");
        server_id = bundle.getString("server_id");

        TextView select_pay = (TextView) findViewById(R.id.select_pay);
        select_pay.setText("订单已经生成，需要支付￥" + amount + "元，请选择支付方式");
        Button btnalipay = (Button) findViewById(R.id.btn_alipay);
        Button btnweixin = (Button) findViewById(R.id.btn_weixin);
        btnweixin.setOnClickListener(this);
        btnalipay.setOnClickListener(this);

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
        int i = v.getId();
        if (i == R.id.btn_alipay) {
            new HttpClient(new IRequestBuilder() {
                @Override
                RequestData build() {
                    String param = "user_id=" + user_id + "&amount=" + amount + "&game_id=" + game_id + "&server_id=" + server_id;
                    IRequestBuilder.RequestData data = new IRequestBuilder.RequestData();
                    data.setPost(true).setPath(ALIPAY_ROUTER).setParameter(param);
                    return data;
                }
            }, new IResponseParser() {
                @Override
                void parse(final ResponseInfo info) {
                    //网络状态码200，正常访问
                    if (info.getCode() == 200) {
                        String responseStatus = "";
                        try {
                            JSONObject json = new JSONObject(info.getBody());
                            responseStatus = json.getString("status");
                            JSONObject dataNode = json.getJSONObject("data");
                            responseOrder = dataNode.getString("response");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        //服务端返回值为-1，则退出支付界面
                        if ("-1".equals(responseStatus) || "-2".equals(responseStatus)) {
                            Bundle data = new Bundle();
                            data.putString("data", info.getBody());
                            LeyyxSdkEventBus.dispatchEvent(ELeyyxSdkEventType.PAY, 0, data);
                            finish();
                        } else {

                            Runnable payRunnable = new Runnable() {
                                @Override
                                public void run() {
                                    PayTask alipay = new PayTask(SdkPayActivity.this);
                                    Log.d("responseOrder", responseOrder);
                                    Map<String, String> result = alipay.payV2(responseOrder, true);


                                    PayResult payResult = new PayResult(result);
                                    resultInfo = payResult.getResult();
                                    resultStatus = payResult.getResultStatus();
                                    if (resultStatus.equals("9000")) {
                                        Message msg = new Message();
                                        msg.what = ALI_PAY_FLAG;
                                        msg.obj = result;
                                        mHandler.sendMessage(msg);
                                    } else {
                                        Message msg = new Message();
                                        msg.what = PAY_ERROR;
                                        msg.obj = "支付出错";
                                        mHandler.sendMessage(msg);
                                    }

                                }
                            };
                            Thread payThread = new Thread(payRunnable);
                            payThread.start();

                        }
                        //网络没有正常访问
                    } else {
                        Bundle data = new Bundle();
                        data.putString("data", "网络错误");
                        LeyyxSdkEventBus.dispatchEvent(ELeyyxSdkEventType.PAY, 0, data);
                    }
                }
            }).request();
        } else if (i == R.id.btn_close) {
            finish();
        }else if(i == R.id.btn_weixin){
            new HttpClient(new IRequestBuilder() {
                @Override
                RequestData build() {
                    amount = "5";
                    String param = "&amount="+amount;
                    RequestData data = new RequestData();
                    data.setPost(true).setPath(WXPAY_ROUTER).setParameter(param);
                    return data;
                }
            }, new IResponseParser() {
                @Override
                void parse(final ResponseInfo info) {
                    String appId = null;
                    String partnerId = null;
                    String prepayId = null;
                    String packageValue = null;
                    String nonceStr = null;
                    String timeStamp = null;
                    String sign = null;
                    //网络状态码200，正常访问
                    if (info.getCode() == 200) {
                        String responseStatus = "";
                        try {
                            JSONObject json = new JSONObject(info.getBody());
                            responseStatus = json.getString("status");
                            dataNode = json.getJSONObject("data");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        //服务端返回值为-1，则退出支付界面
                        if ("1".equals(responseStatus)) {
                            Log.d("data", String.valueOf(dataNode));
                        }
                        //网络没有正常访问
                    } else {
                        Bundle data = new Bundle();
                        data.putString("data", "网络错误");
                        LeyyxSdkEventBus.dispatchEvent(ELeyyxSdkEventType.PAY, 0, data);
                    }
                }
            }).request();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //避免内存溢出
        mHandler.destory();
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case ALI_PAY_FLAG: {
                showDetail(resultInfo);
                Bundle data = new Bundle();
                data.putString("data", "支付成功");
                LeyyxSdkEventBus.dispatchEvent(ELeyyxSdkEventType.PAY, 1, data);
                break;
            }
            case PAY_ERROR:
                Bundle data = new Bundle();
                data.putString("data", msg.obj.toString());
                LeyyxSdkEventBus.dispatchEvent(ELeyyxSdkEventType.PAY, 0, data);
                finish();
            case WX_AUTH_FLAG: {
                break;
            }
            default:
                break;
        }
    }

    private MessageHandler<SdkPayActivity> mHandler = new MessageHandler<>(this);

    public void showDetail(String resultInfo) {
        setContentView(R.layout.details);
        Button closeBtn = (Button) findViewById(R.id.btn_close);
        closeBtn.setOnClickListener(SdkPayActivity.this);
        TextView order_no = (TextView) findViewById(R.id.trade_no);
        TextView trade_msg = (TextView) findViewById(R.id.trade_msg);
        TextView trade_time = (TextView) findViewById(R.id.trade_time);
        TextView trade_amount = (TextView) findViewById(R.id.trade_amount);
        TextView out_order_no = (TextView) findViewById(R.id.out_trade_no);
        try {
            JSONObject jsonObject = new JSONObject(resultInfo);
            String order_info = jsonObject.getString("alipay_trade_app_pay_response");
            JSONObject order_json = new JSONObject(order_info);
            String trade_no = order_json.getString("trade_no");
            String order_msg = order_json.getString("msg");
            String time = order_json.getString("timestamp");
            String amount = order_json.getString("total_amount");
            String out_trade_no = order_json.getString("out_trade_no");
            Log.d("out_trade_no==>", out_trade_no);
            order_no.setText(trade_no);
            trade_msg.setText(order_msg);
            trade_time.setText(time);
            trade_amount.setText("￥" + amount);
            out_order_no.setText(out_trade_no);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


}
