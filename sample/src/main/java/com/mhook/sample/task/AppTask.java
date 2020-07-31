package com.mhook.sample.task;

import android.app.Application;

import com.mhook.sample.tool.App;
import com.mhook.sample.tool.MyFile;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;


/**
 * Created by ASUS on 2020/7/31.
 */

public class AppTask extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
//        CodeProcessor.init(this);
        copyFridaJs();
    }

    private void copyFridaJs() {
        try {
            FileUtils.write(new File(App.FRIDA_JS_PATH+"/"+App.ASSETS_FRIDA_JS_NAME), MyFile.assetsText(this,App.ASSETS_FRIDA_JS_NAME));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
