package com.mhook.sample.tool.common;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;


public class Tips {
    private static Toast toast;
    public static synchronized void showToast(Context context, String... messages){
        new Handler(Looper.getMainLooper()).post(()->{
            String msg=StringUtils.join(messages);
            if(toast==null){
                toast=Toast.makeText(context,msg,Toast.LENGTH_LONG);
            }else {
                toast.setText(msg);
            }
            toast.show();
        });

    }
}
