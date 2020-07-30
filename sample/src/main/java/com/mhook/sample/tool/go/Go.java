package com.mhook.sample.tool.go;

/**
 * Created by ASUS on 2020/7/25.
 */

public class Go {

    public static void go(Runnable runnable){
        new Thread(runnable).start();
    }
}
