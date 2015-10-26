package com.wiwide.common;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.wiwide.wifitool.ApplicationPlus;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by yueguang on 15-7-30.
 */
public class Util {

    /**
     * 获取手机号码，先从本地获取，如果本地没有就从TelephonyManager获取（有些手机可能获取不到）。再没有的话返回空，返回空的情况需要弹窗提示用户输入
     *
     * @return 电话号码
     */
    public static String getPhone(Context context) {
        TelephonyManager mTelephonyMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String phone = mTelephonyMgr.getLine1Number();
        if (!TextUtils.isEmpty(phone)) {
            if (phone.startsWith("+86")) {
                phone = phone.substring(3, phone.length());
            }
        }
        if (TextUtils.isEmpty(phone)) {
            Logger.w("获取手机号码失败");
            phone = "";
        }

        return phone;
    }

    public static String getImei(Context context) {
        return ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE))
                .getDeviceId();
    }

    public static String getImsi(Context context) {
        TelephonyManager telManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return telManager.getSubscriberId();
    }

    /**
     * 获取mac地址
     *
     * @param context 上下文
     * @return mac地址
     */
    public static String getMac(Context context) {
        WifiManager wifiMng = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiMng.getConnectionInfo();
        return wifiInfo.getMacAddress();
    }

    //获取路由器mac
    public static String getBSSID(Context context) {
        WifiManager wifiMng = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiMng.getConnectionInfo();
        return wifiInfo.getBSSID();
    }
    /**
     * 将路由器mac地址(BSSID)转换为大写且使用"-"分隔
     *
     * @param context
     * @return XX-XX-XX-XX-XX-XX
     */
    public static String getUpperBSSID(Context context) {
        String bssid = getBSSID(context);
        bssid = bssid.toUpperCase();
        bssid = bssid.replaceAll(":", "");
        StringBuffer sb = new StringBuffer(bssid);
        for (int i = 2; i < sb.length(); i += 3) {
            sb.insert(i, "-");
        }
        return sb.toString();
    }

    /**
     * MD5加密
     * @param str
     * @return
     */
    public static String getMD5(String str) {
        try {
            // Create MD5 Hash
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(str.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                hexString.append(Integer.toHexString(0xFF & aMessageDigest));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 将mac点地址转换为wiwide要求的格式:xx:xx:xx:xx:xx 全部小写
     *
     * @param context
     * @return
     */
    public static String getLowerMac(Context context) {
        String lowerMac = getMac(context);
        StringBuffer sb = new StringBuffer(lowerMac);
        for (int i = 2; i < sb.length(); i += 3) {
            sb.insert(i, ":");
        }
        lowerMac = sb.toString().toLowerCase();
        return lowerMac;
    }

    /**
     * 获取时间戳
     * @return 秒数
     */
    public static String getTime() {
        Long tsLong = System.currentTimeMillis();
        tsLong=tsLong/1000;
        return tsLong.toString();
    }

    public static String formatTime(long time) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(new Date(time));
    }

    //long类型毫秒数转换为String格式时间
    //yy/MM/dd HH:mm:ss 如 '2002/1/1 17:55:00'
    //yy/MM/dd HH:mm:ss pm 如 '2002/1/1 17:55:00 pm'
    //yy-MM-dd HH:mm:ss 如 '2002-1-1 17:55:00'
    //yy-MM-dd HH:mm:ss am 如 '2002-1-1 17:55:00 am'
    public static String getFormatedDateTime(long dateTime) {
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sDateFormat.format(new Date(dateTime + 0));
    }

    //根据时间差毫秒数计算几天前或几小时前
    public static String getTimeGap(long timeGap) {
        long min = timeGap / 1000 / 60;
        long hour = min / 60;
        long day = hour / 24;
        if (day > 0) {
            return String.valueOf(day) + "天前";
        }
        if (hour > 0) {
            return String.valueOf(hour) + "小时前";
        }
        if (min > 30) {
            return String.valueOf(min) + "分钟前";
        }
        return "刚刚";
    }

    public static boolean initPath(String path) {
        File rootFile = new File(path);
        return initPath(rootFile);
    }

    /**
     * 获取当前SSID
     *
     * @param context
     * @return
     */
    public static String getSSID(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        Log.d("wifiInfo", wifiInfo.toString());
        Log.d("SSID", wifiInfo.getSSID());
        String ssid = wifiInfo.getSSID();
        if (!TextUtils.isEmpty(ssid) && ssid.contains("\"")) {
            ssid = ssid.replace("\"", "");
        }
        return ssid;
    }

    /**
     * 判断当前网络是否可用
     *
     * @return 网络是否可用
     */
    public static boolean isNetworkUseful() {
        boolean isUseful = false;
        SyncResponse sr = HttpClient.getSync(CommonDefine.AUTHENTICATION_CHECK_URL, null, 2, 1500);
        if ((!TextUtils.isEmpty(sr.getResponse())) && sr.getResponse().contains(CommonDefine.NET_CHECK_CONTENT)) {
            isUseful = true;
        }
        return isUseful;
    }

    /**
     * 判断当前网络是否需要认证登录
     *
     * @return 同步响应
     */
    public static SyncResponse  checkNeedAuthentication(final Context context) {
        //检查是否重定向
        SyncResponse sr = HttpClient.checkRedirect(CommonDefine.AUTHENTICATION_CHECK_URL, null);
        if (sr.getStatusCode() != 0) {
            if (sr.isRedirects()) {
            //有中间页
                sr.setStatusCode(201);
            } else {
                sr.setStatusCode(200);
            }
        } else {
            //网络不行
            sr.setStatusCode(-1);
        }
        return sr;
    }


    public static boolean initPath(File rootFile) {
        return rootFile.exists() || rootFile.mkdirs();
    }
}
