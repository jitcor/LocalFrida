package com.mhook.sample.tool;

import android.util.Log;

import com.mhook.sample.task.AppTask;
import com.mhook.sample.tool.common.Tips;

import org.apache.commons.lang3.StringUtils;

public class Debug {
    public static final boolean DEBUG=Log.isLoggable("localfridasample",Log.DEBUG);
    public static void LogI(String tag,Object... msg){
        if(DEBUG){
            Log.i(tag, StringUtils.join(msg));
        }
    }
    public static void LogE(String tag,Object... msg){
        Log.e(tag, StringUtils.join(msg));
        Tips.showToast(AppTask.getInstance(), StringUtils.join(msg));
    }

}
