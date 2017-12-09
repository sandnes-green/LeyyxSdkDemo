package com.leyyx.leyyxsdk;


import android.os.Handler;
import android.os.Message;
import android.util.Log;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


abstract class IRequestBuilder {

     static class RequestData {
        private Boolean post = false;
        private String path;
        private String parameter;
        // 暂时不单独设置header

        RequestData setPost(Boolean post) {
            this.post = post;
            return this;
        }

        RequestData setPath(String path) {
            this.path = path;
            return this;
        }

        RequestData setParameter(String parameter) {
            this.parameter = parameter;
            return this;
        }

        Boolean getPost() {
            return post;
        }

        String getPath() {
            return path;
        }

        String getParameter() {
            return parameter;
        }
    }

    abstract RequestData build();

}

abstract class IResponseParser {

     static class ResponseInfo {
        private int code = 0;
        // header 暂时不需要
        private String body = null;

        ResponseInfo setCode(int code) {
            this.code = code;
            return this;
        }

        ResponseInfo setBody(String body) {
            this.body = body;
            return this;
        }

        int getCode() {
            return code;
        }

        String getBody() {
            return body;
        }
    }

    abstract void parse(ResponseInfo info);
}


class HttpClient {

    private static final String TAG = "HttpClient";

    private IRequestBuilder requestBuilder;
    private IResponseParser responseParser;
    private IResponseParser.ResponseInfo responseInfo;

    final private static String X_LEYYXSDK_UTOKEN = "X-Leyyxsdk-Utoken";
    final private static String SET_X_LEYYXSDK_UTOKEN = "Set-X-Leyyxsdk-Utoken";

    private static _TokenCollections tokenCollections = new _TokenCollections();

    private static class _TokenCollections {

        private Map<String, String> tokens = new HashMap<>();

        // 这里可能存在并发访问，需要使用同步机制
        String getToken(String name) {
            synchronized (this) {
                if (tokens.containsKey(name)) {
                    return tokens.get(name);
                } else {
                    return null;
                }
            }
        }

        void setToken(String name, String token) {
            synchronized (this) {
                if (token != null && !token.isEmpty()) {
                    tokens.put(name, token);
                } else {
                    tokens.remove(name);
                }
            }

        }
    }


    private _MessageHandler theHandler = new _MessageHandler(this);

    private static class _MessageHandler extends Handler {
        private final WeakReference<HttpClient> _outer;

        private  _MessageHandler(HttpClient _outer) {
            this._outer = new WeakReference<>(_outer);
        }

        @Override
        public void handleMessage(Message msg) {
            _outer.get().handleMessage(msg);
        }
    }

    HttpClient(IRequestBuilder builder, IResponseParser parser) {
        this.requestBuilder = builder;
        this.responseParser = parser;
    }

    private void handleMessage(Message msg) {
        if (msg.what == 1) {
            responseParser.parse((IResponseParser.ResponseInfo) msg.obj);
        }
        theHandler.removeCallbacksAndMessages(null);
    }

    void request() {

        final IRequestBuilder.RequestData data = requestBuilder.build();

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!data.getPost()) {
                    httpGet(data.getPath(), data.getParameter());
                } else {
                    httpPost(data.getPath(), data.getParameter());
                }
                theHandler.obtainMessage(1, responseInfo).sendToTarget();
            }
        }).start();
    }

    private void httpGet(String path, String query) {
        HttpURLConnection conn = null;
        BufferedReader reader = null;

        try {
            URL url;
            if (query != null) {
                url = new URL(path + "?" + query);
            } else {
                url = new URL(path);
            }
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("GET");

            String t = tokenCollections.getToken(X_LEYYXSDK_UTOKEN);
            if (t != null) {
                conn.setRequestProperty(X_LEYYXSDK_UTOKEN, t);
            }

            responseInfo = new IResponseParser.ResponseInfo();
            Map<String, List<String>> headers = conn.getHeaderFields();
            if (headers.containsKey(SET_X_LEYYXSDK_UTOKEN)) {
                List<String> v = headers.get(SET_X_LEYYXSDK_UTOKEN);
                if (v != null) {
                    tokenCollections.setToken(X_LEYYXSDK_UTOKEN, v.get(0));
                }
            }

            responseInfo.setCode(conn.getResponseCode());

            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            responseInfo.setBody(builder.toString());
        } catch (MalformedURLException e) {
            Log.w(TAG, "doGet: MalformedURLException");
        } catch (IOException e) {
            Log.w(TAG, "doGet: IOException");
        } catch (Exception e) {
            Log.w(TAG, "doGet: Exception");
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.w(TAG, "doGet: reader close failed.");
                }
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private void httpPost(String path, String data) {
        HttpURLConnection conn = null;
        BufferedReader reader = null;

        try {
            URL url = new URL(path);
            conn = (HttpURLConnection) url.openConnection();

            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");

            String t = tokenCollections.getToken(X_LEYYXSDK_UTOKEN);
            if (t != null) {
                conn.setRequestProperty(X_LEYYXSDK_UTOKEN, t);
            }

            // 发送POST数据
            DataOutputStream out = new DataOutputStream(conn.getOutputStream());
            out.writeBytes(data);
            out.flush();
            out.close();

            // 开始读响应
            responseInfo = new IResponseParser.ResponseInfo();

            Map<String, List<String>> headers = conn.getHeaderFields();
            if (headers.containsKey(SET_X_LEYYXSDK_UTOKEN)) {
                List<String> v = headers.get(SET_X_LEYYXSDK_UTOKEN);
                if (v != null) {
                    tokenCollections.setToken(X_LEYYXSDK_UTOKEN, v.get(0));
                }
            }

            responseInfo.setCode(conn.getResponseCode());
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            responseInfo.setBody(builder.toString());
        } catch (MalformedURLException e) {
            Log.w(TAG, "doGet: MalformedURLException");
        } catch (IOException e) {
            Log.w(TAG, "doGet: IOException");
        } catch (Exception e) {
            Log.w(TAG, "doGet: Exception");
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.w(TAG, "doGet: reader close failed.");
                }
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}
