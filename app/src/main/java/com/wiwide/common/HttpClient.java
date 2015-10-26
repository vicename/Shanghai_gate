package com.wiwide.common;

import android.text.TextUtils;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Map;

/**
 * Http请求封装工具
 * <p/>
 * Created by yueguang on 15-3-24.
 */
public class HttpClient {
    private static boolean isUploadOk = false;

    /**
     * 检查重定向
     *
     * @param url    地址
     * @param params 参数
     */
    public static SyncResponse checkRedirect(final String url, RequestParams params) {
        CheckRedirectHandler redirectHandler = new CheckRedirectHandler();
        SyncHttpClient syncClient = getSyncClient();
        ((DefaultHttpClient) syncClient.getHttpClient()).setRedirectHandler(redirectHandler);
        final SyncResponse response = new SyncResponse();
        syncClient.get(url, params, new TextHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                response.setStatusCode(statusCode);
                //这边的处理是为了应对通过dns污染加js的location.replace()实现重定向的情况
                if (responseString.contains(CommonDefine.NET_CHECK_CONTENT)) {
                    response.setIsRedirects(false);
                } else {
                    response.setIsRedirects(true);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Logger.e("Err checkRedirect statusCode:" + statusCode + " Url:" + url + " Error:" + throwable);
                response.setStatusCode(statusCode);
            }
        });

        if (!response.isRedirects()) {
            response.setIsRedirects(redirectHandler.isEnableRedirects());
        }

        response.setRedirectsLocation(redirectHandler.getRedirectsUrl());
        return response;
    }


    /**
     * 异步GET请求
     *
     * @param url             地址
     * @param params          参数
     * @param responseHandler 响应回调
     */
    public static void getAsync(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        AsyncHttpClient asyncClient = getAsyncClient();
        asyncClient.get(url, params, responseHandler);
    }

    /**
     * 异步POST请求
     *
     * @param url             地址
     * @param params          参数
     * @param responseHandler 响应回调
     */
    public static void postAsync(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        AsyncHttpClient asyncClient = getAsyncClient();
        asyncClient.post(url, params, responseHandler);
    }

    public static void postJson(String url, JSONObject params, AsyncHttpResponseHandler responseHandler) {
        Logger.i(params.toString());
        AsyncHttpClient asyncClient = getAsyncClient();
        StringEntity se;
        try {
            se = new StringEntity(params.toString(), HTTP.UTF_8);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return;
        }
        se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json;charset=UTF-8"));
        asyncClient.post(null, url, se, "application/json", responseHandler);
    }

    public static void post(String url, byte[] data, AsyncHttpResponseHandler responseHandler) {
        AsyncHttpClient asyncClient = getAsyncClient();
        ByteArrayEntity se;
        se = new ByteArrayEntity(data);
        se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/octet-stream"));
        asyncClient.post(null, url, se, "application/octet-stream", responseHandler);
    }

    public static void postSync(String url, byte[] data, AsyncHttpResponseHandler responseHandler) {
        SyncHttpClient asyncClient = getSyncClient();
        ByteArrayEntity se;
        se = new ByteArrayEntity(data);
        se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/octet-stream"));
        asyncClient.post(null, url, se, "application/octet-stream", responseHandler);
    }

    /**
     * 同步GET请求
     *
     * @param url    地址
     * @param params 参数
     */
    public static SyncResponse getSync(final String url, RequestParams params) {
        return getSync(url, params, AsyncHttpClient.DEFAULT_MAX_RETRIES, AsyncHttpClient.DEFAULT_RETRY_SLEEP_TIME_MILLIS);
    }

    /**
     * 同步GET请求
     *
     * @param url    地址
     * @param params 参数
     */
    public static SyncResponse getSync(final String url, RequestParams params, int maxRetry, int timeOut) {
        final SyncResponse response = new SyncResponse();
        SyncHttpClient syncClient = getSyncClient();
        syncClient.setMaxRetriesAndTimeout(maxRetry, timeOut);
        syncClient.get(url, params, new TextHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, String responseString) {
                        response.setStatusCode(statusCode);
                        response.setHeaders(headers);
                        response.setResponse(responseString);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        Logger.e("Err statusCode:" + statusCode + " Url:" + url + " Error:" + throwable);
                        response.setStatusCode(statusCode);
                        response.setHeaders(headers);
                        response.setResponse(responseString);
                        response.setThrowable(throwable);
                    }

                    @Override
                    public void onRetry(int retryNo) {
                        super.onRetry(retryNo);
                        Logger.i("getSync onRetry:" + retryNo);
                    }
                }
        );
        return response;
    }

    /**
     * 同步POST请求
     *
     * @param url    地址
     * @param params 参数
     */
    public static SyncResponse postSync(final String url, RequestParams params) {
        return postSync(url, null, params);
    }

    /**
     * 同步POST请求
     *
     * @param url     地址
     * @param headers 头部参数
     * @param params  参数
     */
    public static SyncResponse postSync(final String url, Map<String, String> headers, RequestParams params) {
        final SyncResponse response = new SyncResponse();
        SyncHttpClient syncClient = getSyncClient();
        if (headers != null) {
            for (Object object : headers.entrySet()) {
                Map.Entry<String, String> entry = (Map.Entry) object;
                syncClient.addHeader(entry.getKey(), entry.getValue());
            }
        }
        syncClient.post(url, params, new TextHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, String responseString) {
                        response.setStatusCode(statusCode);
                        response.setHeaders(headers);
                        response.setResponse(responseString);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        Logger.e("Err statusCode:" + statusCode + " Url:" + url + " Error:" + throwable);
                        response.setStatusCode(statusCode);
                        response.setHeaders(headers);
                        response.setResponse(responseString);
                        response.setThrowable(throwable);
                    }
                }
        );
        return response;
    }

    public static boolean uploadFile(String url, RequestParams params, File file) {
        isUploadOk = false;
        try {
            params.put("file", file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        SyncHttpClient syncClient = getSyncClient();
        syncClient.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                isUploadOk = true;
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Logger.e("Err uploadFile statusCode:" + statusCode + " Error:" + error);
                error.printStackTrace();
                isUploadOk = false;
            }
        });
        return isUploadOk;
    }

    private static SyncHttpClient getSyncClient() {
        SyncHttpClient syncClient = new SyncHttpClient();
        syncClient.setTimeout(20 * 1000);
        syncClient.setConnectTimeout(20 * 1000);
        syncClient.setResponseTimeout(20 * 1000);
        ((DefaultHttpClient) syncClient.getHttpClient()).setHttpRequestRetryHandler(new CustomRetryHandler(AsyncHttpClient.DEFAULT_MAX_RETRIES, AsyncHttpClient.DEFAULT_RETRY_SLEEP_TIME_MILLIS));
        return syncClient;
    }

    private static AsyncHttpClient getAsyncClient() {
        AsyncHttpClient asyncClient = new AsyncHttpClient();
        asyncClient.setTimeout(20 * 1000);
        asyncClient.setConnectTimeout(20 * 1000);
        asyncClient.setResponseTimeout(20 * 1000);
        ((DefaultHttpClient) asyncClient.getHttpClient()).setHttpRequestRetryHandler(new CustomRetryHandler(AsyncHttpClient.DEFAULT_MAX_RETRIES, AsyncHttpClient.DEFAULT_RETRY_SLEEP_TIME_MILLIS));
        return asyncClient;
    }

    /**
     * 下载二进制文件(比如图片)
     *
     * @param fileUrl 地址
     * @param file    保存路径
     */
    public static boolean downloadFile(String fileUrl, File file) {
        boolean isOk;
        try {
            URL url = new URL(fileUrl);
            OutputStream os = new FileOutputStream(file);
            InputStream is = url.openStream();
            byte[] buff = new byte[1024];
            while (true) {
                int readCount = is.read(buff);
                if (readCount == -1) {
                    break;
                }
                os.write(buff, 0, readCount);
            }
            is.close();
            os.close();
            isOk = true;
        } catch (IOException e) {
            isOk = downloadCharFile(fileUrl, file);
        }
        return isOk;
    }

    /**
     * 下载文件
     *
     * @param fileUrl 地址
     * @param file    保存路径
     */
    public static boolean downloadCharFile(String fileUrl, File file) {
        boolean isOk = false;
        try {
            BufferedWriter os = new BufferedWriter(new FileWriter(file));
            String response = getSync(fileUrl, null).getResponse();
            Logger.i(response);
            if (!TextUtils.isEmpty(response)) {
                os.write(response);
                os.close();
                isOk = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return isOk;
    }
}
