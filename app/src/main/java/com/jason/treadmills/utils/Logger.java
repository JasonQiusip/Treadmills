package com.jason.treadmills.utils;

import android.os.Debug;
import android.util.Log;

/**
 * Created by Jason on 2015/3/13.
 */
public class Logger {

    private static final boolean DEBUG = true;

    public static void showErrorLog(String tag, String content) {
        if(DEBUG) {
            Log.e(tag, content);
        }
    }
}
