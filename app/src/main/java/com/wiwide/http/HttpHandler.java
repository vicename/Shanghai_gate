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
 * Created by yueguang on 15-7-30.
 */
public class HttpHandler
{
    public static void getCode(final Context context, String phone, JsonHttpResponseHandler responseHandler)
    {
        try {
            RequestParams params = new RequestParams();
            params.put("MOB", phone);
            params.put("SRC", CommonDefine.APP_SRC);
            String mac = Util.getMac(context);
            params.put("MAC", mac.replace(":", "").toUpperCase());
            params.put("IMEI", Util.getImei(context));
            params.put("IMSI", Util.getImsi(context));
            params.put("MTY", CommonDefine.MOBILE_TYPE);
            params.put("OTY", CommonDefine.OS_TYPE);
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            MySSLSocketFactory socketFactory = new MySSLSocketFactory(trustStore);
            socketFactory.setHostnameVerifier(MySSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            AsyncHttpClient client = new AsyncHttpClient();
            client.setTimeout(30 * 1000);
            client.setSSLSocketFactory(socketFactory);
            client.get(CommonDefine.SERVER_BASE + CommonDefine.AUTHENTICATION, params, responseHandler);
        } catch (KeyStoreException | CertificateException | UnrecoverableKeyException
                | NoSuchAlgorithmException | IOException | KeyManagementException e) {
            e.printStackTrace();
        }
    }

    public static void bindMoble(final Context context, String phone, String checkCode, JsonHttpResponseHandler responseHandler)
    {
        try {
            RequestParams params = new RequestParams();
            params.put("MOB", phone);
            String mac = Util.getMac(context);
            params.put("CODE", checkCode);
            params.put("SRC", CommonDefine.APP_SRC);
            params.put("MAC", mac.replace(":", "").toUpperCase());
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            MySSLSocketFactory socketFactory = new MySSLSocketFactory(trustStore);
            socketFactory.setHostnameVerifier(MySSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            AsyncHttpClient client = new AsyncHttpClient();
            client.setTimeout(30 * 1000);
            client.setSSLSocketFactory(socketFactory);
            Log.i("----------------", checkCode);
            client.get(CommonDefine.SERVER_BASE + CommonDefine.AUTHORIZATION, params, responseHandler);
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

    //同步checkin方法
    public static void checkIn(final Context context, String uid, JsonHttpResponseHandler responseHandler)
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
            SyncHttpClient client = new SyncHttpClient();
            client.setTimeout(30 * 1000);
            client.setSSLSocketFactory(socketFactory);
            client.get(CommonDefine.SERVER_BASE + CommonDefine.CHECKIN, params, responseHandler);
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
//            由于此处为子线程操作,故应使用同步网络请求
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
}
