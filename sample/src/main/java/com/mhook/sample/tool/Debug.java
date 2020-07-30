package com.mhook.sample.tool;

import android.util.Log;

public class Debug {
    public static final boolean DEBUG=Log.isLoggable("localfridasample",Log.DEBUG);
    public static void LogI(String tag,Object... msg){
        if(DEBUG){
            Log.i(tag,sprintf(msg));
        }
    }
    public static void LogE(String tag,Object... msg){
        if(DEBUG){
            Log.e(tag,sprintf(msg));
        }
    }

    private static String sprintf(Object... objs){
        StringBuilder builder=new StringBuilder();
        for (int i = 0; i < objs.length; i++) {
            builder.append("%s");
        }
        return String.format(builder.toString(),objs);
    }
}
