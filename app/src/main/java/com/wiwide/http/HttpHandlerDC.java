package com.wiwide.http;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.MySSLSocketFactory;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;
import com.wiwide.common.CommonDefine;
import com.wiwide.common.Logger;
import com.wiwide.common.Util;

import org.apache.http.Header;
import org.json.JSONObject;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

/**
 * Created by DC-ADMIN on 15-9-15.
 */
public class HttpHandlerDC
{
    //获取验证码
    public static void auth(final Context context, String phone, JsonHttpResponseHandler responseHandler)
    {
        try {
            RequestParams params = new RequestParams();
            params.put("method", "auth");
            params.put("MOBILE", phone);
            params.put("APP_SRC", CommonDefine.APP_SRC);
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            MySSLSocketFactory socketFactory = new MySSLSocketFactory(trustStore);
            socketFactory.setHostnameVerifier(MySSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            AsyncHttpClient client = new AsyncHttpClient();
            client.setTimeout(30 * 1000);
            client.setSSLSocketFactory(socketFactory);
            client.get(CommonDefine.SERVER_BASE_V2 + CommonDefine.SERVER_METHOD_V2, params, responseHandler);
        } catch (KeyStoreException | CertificateException | UnrecoverableKeyException
                | NoSuchAlgorithmException | IOException | KeyManagementException e) {
            e.printStackTrace();
        }
    }

    //绑定手机
    public static void bind(final Context context, String phone, String checkCode,String androidModel, JsonHttpResponseHandler responseHandler)
    {
        try {
            RequestParams params = new RequestParams();
            params.put("method", "bind");
            params.put("MOBILE", phone);
            params.put("CHECK_CODE", checkCode);
            params.put("APP_SRC", CommonDefine.APP_SRC);
            params.put("MAIN_TYPE",androidModel);
            Log.i("--binding", androidModel);
            String mac = Util.getMac(context);
            params.put("MAC", mac.replace(":", "").toUpperCase());
            params.put("IMEI", Util.getImei(context));
            params.put("IMSI", Util.getImsi(context));
            params.put("OS_TYPE", CommonDefine.OS_TYPE);
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            MySSLSocketFactory socketFactory = new MySSLSocketFactory(trustStore);
            socketFactory.setHostnameVerifier(MySSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            AsyncHttpClient client = new AsyncHttpClient();
            client.setTimeout(30 * 1000);
            client.setSSLSocketFactory(socketFactory);
            client.get(CommonDefine.SERVER_BASE_V2 + CommonDefine.SERVER_METHOD_V2, params, responseHandler);
        } catch (KeyStoreException | CertificateException | UnrecoverableKeyException
                | NoSuchAlgorithmException | IOException | KeyManagementException e) {
            e.printStackTrace();
        }
    }

    public static void checkOut(final Context context, String uid, JsonHttpResponseHandler responseHandler)
    {
        try {
            RequestParams params = new RequestParams();
            params.put("UID", uid);
            params.put("SRC", CommonDefine.APP_SRC);
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            MySSLSocketFactory socketFactory = new MySSLSocketFactory(trustStore);
            socketFactory.setHostnameVerifier(MySSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            AsyncHttpClient client = new AsyncHttpClient();
            client.setTimeout(30 * 1000);
            client.setSSLSocketFactory(socketFactory);
            client.get(CommonDefine.SERVER_BASE + CommonDefine.CHECKOUT, params, responseHandler);
        } catch (KeyStoreException | CertificateException | UnrecoverableKeyException
                | NoSuchAlgorithmException | IOException | KeyManagementException e) {
            e.printStackTrace();
        }
    }

    public static void getBondedList(final Context context, String checkCode, String phone, JsonHttpResponseHandler responseHandler)
    {
        try {
            RequestParams params = new RequestParams();
            params.put("method", "list");
            params.put("CHECK_CODE", checkCode);
            params.put("MOBILE", phone);
            params.put("APP_SRC", CommonDefine.APP_SRC);
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            MySSLSocketFactory socketFactory = new MySSLSocketFactory(trustStore);
            socketFactory.setHostnameVerifier(MySSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            AsyncHttpClient client = new AsyncHttpClient();
            client.setTimeout(30 * 1000);
            client.setSSLSocketFactory(socketFactory);
            client.get(CommonDefine.SERVER_BASE_V2 + CommonDefine.SERVER_METHOD_V2, params, responseHandler);
        } catch (KeyStoreException | CertificateException | UnrecoverableKeyException
                | NoSuchAlgorithmException | IOException | KeyManagementException e) {
            e.printStackTrace();
        }
    }

    //解绑手机
    public static void unbind(final Context context, String phone,String checkCode,String clientCode, JsonHttpResponseHandler responseHandler)
    {
        try {
            RequestParams params = new RequestParams();
            params.put("method", "unbind");
            params.put("MOBILE", phone);
            params.put("APP_SRC", CommonDefine.APP_SRC);
            params.put("CHECK_CODE", checkCode);
            params.put("CLIENT_CODE", clientCode);
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            MySSLSocketFactory socketFactory = new MySSLSocketFactory(trustStore);
            socketFactory.setHostnameVerifier(MySSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            AsyncHttpClient client = new AsyncHttpClient();
            client.setTimeout(30 * 1000);
            client.setSSLSocketFactory(socketFactory);
            client.get(CommonDefine.SERVER_BASE_V2 + CommonDefine.SERVER_METHOD_V2, params, responseHandler);
        } catch (KeyStoreException | CertificateException | UnrecoverableKeyException
                | NoSuchAlgorithmException | IOException | KeyManagementException e) {
            e.printStackTrace();
        }
    }

    //    public static void checkIn(final Context context, String uid, JsonHttpResponseHandler responseHandler)
    //    {
    //        try {
    //            RequestParams params = new RequestParams();
    //            params.put("UID", uid);
    //            params.put("SRC", CommonDefine.APP_SRC);
    //            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
    //            trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
    //            trustStore.load(null, null);
    //            MySSLSocketFactory socketFactory = new MySSLSocketFactory(trustStore);
    //            socketFactory.setHostnameVerifier(MySSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
    //            AsyncHttpClient client = new AsyncHttpClient();
    //            client.setTimeout(30 * 1000);
    //            client.setSSLSocketFactory(socketFactory);
    //            client.get(CommonDefine.SERVER_BASE + CommonDefine.CHECKIN, params, responseHandler);
    //        } catch (KeyStoreException | CertificateException | UnrecoverableKeyException
    //                | NoSuchAlgorithmException | IOException | KeyManagementException e) {
    //            e.printStackTrace();
    //        }
    //    }

    //异步login方法
    public static void asyncLogin(final Context context, String uid,String location, JsonHttpResponseHandler responseHandler)
    {
        try {
            RequestParams params = new RequestParams();
            params.put("method", "login");
            params.put("SERVICE_CODE", CommonDefine.SERVICE_CODE);
//            params.put("SERVICE_CODE", location);
            params.put("UID", uid);
            String mac = Util.getMac(context);
            params.put("MAC", mac.replace(":", "").toUpperCase());
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            MySSLSocketFactory socketFactory = new MySSLSocketFactory(trustStore);
            socketFactory.setHostnameVerifier(MySSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            AsyncHttpClient client = new AsyncHttpClient();
            client.setTimeout(30 * 1000);
            client.setSSLSocketFactory(socketFactory);
            client.get(CommonDefine.SERVER_BASE_V2 + CommonDefine.SERVER_METHOD_V2, params, responseHandler);
        } catch (KeyStoreException | CertificateException | UnrecoverableKeyException
                | NoSuchAlgorithmException | IOException | KeyManagementException e) {
            e.printStackTrace();
        }
    }

    //同步login
    public static void login(final Context context, String uid, JsonHttpResponseHandler responseHandler)
    {
        try {
            RequestParams params = new RequestParams();
            params.put("method", "login");
            params.put("SERVICE_CODE", CommonDefine.SERVICE_CODE);
            params.put("UID", uid);
            String mac = Util.getMac(context);
            params.put("MAC", mac.replace(":", "").toUpperCase());
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            MySSLSocketFactory socketFactory = new MySSLSocketFactory(trustStore);
            socketFactory.setHostnameVerifier(MySSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            SyncHttpClient client = new SyncHttpClient();
            client.setTimeout(30 * 1000);
            client.setSSLSocketFactory(socketFactory);
            client.get(CommonDefine.SERVER_BASE_V2 + CommonDefine.SERVER_METHOD_V2, params, responseHandler);
        } catch (KeyStoreException | CertificateException | UnrecoverableKeyException
                | NoSuchAlgorithmException | IOException | KeyManagementException e) {
            e.printStackTrace();
        }
    }

    public static void securityTest(final Context context, String acode, JsonHttpResponseHandler responseHandler)
    {
        try {
            RequestParams params = new RequestParams();
            params.put("isxm@sao", acode);
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            MySSLSocketFactory socketFactory = new MySSLSocketFactory(trustStore);
            socketFactory.setHostnameVerifier(MySSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            //由于此处为子线程操作,故应使用同步网络请求
            SyncHttpClient client = new SyncHttpClient();
            client.setTimeout(30 * 1000);
            client.setSSLSocketFactory(socketFactory);
            //此处的url地址为测试地址,根据协议要求应改为随机ip
            client.get(CommonDefine.SERVER_BASE_SECURITY_TEST, params, responseHandler);
        } catch (KeyStoreException | CertificateException | UnrecoverableKeyException
                | NoSuchAlgorithmException | IOException | KeyManagementException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取公安场所代码
     * @param context
     * @param responseHandler
     */
    public static void getSCode(final Context context,String mac,String stamp,String sig,JsonHttpResponseHandler responseHandler)
    {
        try {
            RequestParams params = new RequestParams();
            params.put("mac", mac);
            params.put("stamp", stamp);
            params.put("sig", sig);
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            MySSLSocketFactory socketFactory = new MySSLSocketFactory(trustStore);
            socketFactory.setHostnameVerifier(MySSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            AsyncHttpClient client = new AsyncHttpClient();
            client.setTimeout(30 * 1000);
            client.setSSLSocketFactory(socketFactory);
            client.get(CommonDefine.SAFTY_LOCATION_URL, params, responseHandler);
        } catch (KeyStoreException | CertificateException | UnrecoverableKeyException
                | NoSuchAlgorithmException | IOException | KeyManagementException e) {
            e.printStackTrace();
        }
    }

    public static void getWiwideMac(final Context context, JsonHttpResponseHandler responseHandler) {
        try {
            RequestParams params = new RequestParams();
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            MySSLSocketFactory socketFactory = new MySSLSocketFactory(trustStore);
            socketFactory.setHostnameVerifier(MySSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            AsyncHttpClient client = new AsyncHttpClient();
            client.setTimeout(30 * 1000);
            client.setSSLSocketFactory(socketFactory);
            client.get(CommonDefine.GET_WIWIDE_MAC_URL, responseHandler);
        } catch (KeyStoreException | CertificateException | UnrecoverableKeyException
                | NoSuchAlgorithmException | IOException | KeyManagementException e) {
            e.printStackTrace();
        }
    }
}
