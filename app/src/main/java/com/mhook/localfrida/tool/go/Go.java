package com.mhook.localfrida.tool.go;

import java9.util.function.Function;

/**
 * Created by ASUS on 2020/7/25.
 */

public class Go {

    public static void go(Runnable runnable){
        new Thread(runnable).start();
    }
}
