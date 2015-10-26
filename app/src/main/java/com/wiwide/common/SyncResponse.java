package com.wiwide.common;

import org.apache.http.Header;

/**
 * 请求响应的封装
 * Created by yueguang on 15-3-24.
 */
public class SyncResponse {
    public String mResponse;
    public int mStatusCode;                 //状态码
    public Header[] mHeaders;               //响应头
    public byte[] mData;
    public Throwable mThrowable;
    public boolean mIsRedirects;            //是否重定向标记
    public String mRedirectsLocation;       //重定向地址

    public byte[] getData() {
        return mData;
    }

    public void setData(byte[] data) {
        mData = data;
    }

    public void setResponse(String response) {
        mResponse = response;
    }

    public void setStatusCode(int statusCode) {
        mStatusCode = statusCode;
    }

    public void setHeaders(Header[] headers) {
        mHeaders = headers;
    }

    public void setThrowable(Throwable throwable) {
        mThrowable = throwable;
    }

    public String getResponse() {
        return mResponse;
    }

    public int getStatusCode() {
        return mStatusCode;
    }

    public Header[] getHeaders() {
        return mHeaders;
    }

    public Throwable getThrowable() {
        return mThrowable;
    }

    public String getRedirectsLocation() {
        return mRedirectsLocation;
    }

    public void setRedirectsLocation(String redirectsLocation) {
        mRedirectsLocation = redirectsLocation;
    }

    public boolean isRedirects() {
        return mIsRedirects;
    }

    public void setIsRedirects(boolean isRedirects) {
        mIsRedirects = isRedirects;
    }
}
