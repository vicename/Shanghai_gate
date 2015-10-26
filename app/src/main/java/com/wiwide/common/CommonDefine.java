package com.wiwide.common;

import android.content.Context;

import java.io.File;

/**
 * Created by yueguang on 15-7-30.
 */
public class CommonDefine
{
    public static final boolean IS_DEBUG = true;

    public static final boolean LOG_TO_FILE = true;

    public static final String CONFIG = "config";

    public static final String APP_SRC = "66";   //app厂商来源，这是冯翔同学历经劫难拿来的好号
    //    public static final String APP_SRC = "src";   //app厂商来源，目前用的海蜘蛛的测试
    public static final int MOBILE_TYPE = 01;   //终端类型，01手机、02平板、99其他
    public static final int OS_TYPE = 02;   //系统类型，01IOS、02安卓、03微软、04塞班、05其他
    public static String PHONE_MODEL=android.os.Build.MODEL;
    public static String TEST_MAC = "00-1F-7A-A3-68-20";

    public static final String SAFTY_LOCATION_URL = "https://monitor.api.wiwide.com/location/query";
    public static final String SAFTY_LOCATION_URL_TEST = "http://monitor.api-test.wiwide.com/location/query";
    public static final String GET_WIWIDE_MAC_URL = "http://device.wiwide.com/goform/ShowSysInfo";

    public static final String SERVER_BASE = "https://115.28.41.58/";
    public static final String SERVER_BASE_V2 = "https://114.80.141.138/";
    public static final String SERVER_METHOD_V2 = "shauth";

    public static final String AUTHENTICATION_CHECK_URL = "http://suggestion.baidu.com";
    public static final String NET_CHECK_CONTENT = "window.baidu.sug";


    public static final String WIWIDE_PID = "10000004";
    public static final String WIWIDE_SEC = "sGZnDQy2";

    public static final String SERVICE_CODE = "31010439020201";//公安场所代码,目前使用上海永琪美容美发测试

    //获取验证码方法名
    public static final String AUTHENTICATION = "authentication";
    //验证接口方法名
    public static final String AUTHORIZATION = "authorization";
    //绑定记录接口名
    public static final String CHECKIN = "checkin";
    //解除绑定
    public static final String CHECKOUT = "checkout";
    //安全审计测试url
    public static final String SERVER_BASE_SECURITY_TEST = "http://www.baidu.com/";

    //存取数据key名
    public static final String UID = "UID";
    public static final String ACODE = "ACODE";
    public static final String IS_FIRST = "IS_FIRST";
    public static final String PHONE_NUM = "PHONE";
    public static final String CHECK_CODE = "CHECK_CODE";

    //路径定义
    public static String PATH_ROOT;
    public static String PATH_LOG;

    public static void initPath(Context context)
    {
        File externalFiles = context.getExternalFilesDir(null);
        if (externalFiles != null) {
            PATH_ROOT = externalFiles.getAbsolutePath();
        } else {
            PATH_ROOT = context.getFilesDir().getAbsolutePath();
        }

        PATH_LOG = PATH_ROOT + File.separator + "Log";

        Util.initPath(CommonDefine.PATH_LOG);
    }
}
