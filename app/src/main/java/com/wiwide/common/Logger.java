package com.wiwide.common;

import android.util.Log;

import com.wiwide.wifitool.ApplicationPlus;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * 写log而已
 * Created by yueguang on 15-3-23.
 */
public class Logger {
    private static final String DEFAULT_FLAG = "---D-C---";
    private static LogObserver mObserver;

    public static void i(String flag, String msg) {
        if (CommonDefine.IS_DEBUG) {
            Log.i(flag, msg);
        }
    }

    public static void i(String msg) {
        if (CommonDefine.IS_DEBUG) {
            Log.i(DEFAULT_FLAG, msg);
            if (CommonDefine.LOG_TO_FILE) {
                msg = Util.formatTime(System.currentTimeMillis()) + "    " + msg + "\n";
                writeToFile(msg);
                if (mObserver != null) {
                    mObserver.onLogChange(msg);
                }
            }
        }
    }

    public static void e(String flag, String msg) {
        if (CommonDefine.IS_DEBUG) {
            Log.e(flag, msg);
        }
    }

    public static void e(String msg) {
        if (CommonDefine.IS_DEBUG) {
            Log.e(DEFAULT_FLAG, msg);
            if (CommonDefine.LOG_TO_FILE) {
                msg = Util.formatTime(System.currentTimeMillis()) + "    " + msg + "\n";
                writeToFile(msg);
                if (mObserver != null) {
                    mObserver.onLogChange(msg);
                }
            }
        }
    }

    public static void w(String flag, String msg) {
        if (CommonDefine.IS_DEBUG) {
            Log.w(flag, msg);
        }
    }

    public static void w(String msg) {
        if (CommonDefine.IS_DEBUG) {
            Log.w(DEFAULT_FLAG, msg);
        }
    }

    public static void writeToFile(final String msg) {
        ApplicationPlus.getInstance().submitTask(new Runnable() {
            @Override
            public void run() {
                File logPath = new File(CommonDefine.PATH_LOG);
                if (!logPath.exists()) {
                    logPath.mkdirs();
                }

                File log = new File(logPath, "log.txt");
                FileWriter fw = null;
                try {
                    fw = new FileWriter(log, true);
                    fw.write(msg);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (fw != null) {
                        try {
                            fw.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    public static void registerLogObserver(LogObserver observer) {
        mObserver = observer;
    }

    public static void unregisterLogObserver() {
        mObserver = null;
    }

    public static void clearLogFile() {
        File logPath = new File(CommonDefine.PATH_LOG);
        if (logPath.exists()) {
            File log = new File(logPath, "log.txt");
            if (log.exists()) {
                log.delete();
            }
        }
    }
}
