package com.wiwide.common;

import android.os.SystemClock;
import android.util.Log;

import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashSet;

import javax.net.ssl.SSLException;

/**
 * 重试控制
 * Created by yueguang on 15-4-7.
 */
public class CustomRetryHandler implements HttpRequestRetryHandler {
    private final static HashSet<Class<?>> exceptionWhiteList = new HashSet<Class<?>>();
    private final static HashSet<Class<?>> exceptionBlackList = new HashSet<Class<?>>();

    static {
        // Retry if the server dropped connection on us
        exceptionWhiteList.add(NoHttpResponseException.class);
        // retry-this, since it may happens as part of a Wi-Fi to 3G failover
        exceptionWhiteList.add(UnknownHostException.class);
        // retry-this, since it may happens as part of a Wi-Fi to 3G failover
        exceptionWhiteList.add(SocketException.class);

        exceptionWhiteList.add(SocketTimeoutException.class);

        exceptionWhiteList.add(IOException.class);

        // never retry timeouts
        exceptionBlackList.add(InterruptedIOException.class);
        // never retry SSL handshake failures
        exceptionBlackList.add(SSLException.class);
    }

    private final int maxRetries;
    private final int retrySleepTimeMS;

    public CustomRetryHandler(int maxRetries, int retrySleepTimeMS) {
        this.maxRetries = maxRetries;
        this.retrySleepTimeMS = retrySleepTimeMS;
    }

    @Override
    public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
        boolean retry = true;

        Boolean b = (Boolean) context.getAttribute(ExecutionContext.HTTP_REQ_SENT);
        boolean sent = (b != null && b);
        if (executionCount > maxRetries) {
            // Do not retry if over max retry count
            retry = false;
        } else if (isInList(exceptionWhiteList, exception)) {
            // immediately retry if error is whitelisted
            Log.i("---retry", "-----true");
            retry = true;
        } else if (isInList(exceptionBlackList, exception)) {
            // immediately cancel retry if the error is blacklisted
            Log.i("---retry", "-----false");
            retry = false;
        } else if (!sent) {
            // for most other errors, retry only if request hasn't been fully sent yet
            Log.i("---retry", "-----true1");
            retry = true;
        }

//        if (retry) {
//            // resend all idempotent requests
//            HttpUriRequest currentReq = (HttpUriRequest) context.getAttribute(ExecutionContext.HTTP_REQUEST);
//            if (currentReq == null) {
//                return false;
//            }
//        }
        if (retry) {
            SystemClock.sleep(retrySleepTimeMS);
        } else {
            exception.printStackTrace();
        }
        return retry;
    }

    static void addClassToWhitelist(Class<?> cls) {
        exceptionWhiteList.add(cls);
    }

    static void addClassToBlacklist(Class<?> cls) {
        exceptionBlackList.add(cls);
    }

    protected boolean isInList(HashSet<Class<?>> list, Throwable error) {
        Logger.i(error.toString());
        for (Class<?> aList : list) {
            if (aList.isInstance(error)) {
                return true;
            }
        }
        return false;
    }
}
