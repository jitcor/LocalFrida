package com.mhook.sample.task;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.mhook.sample.tool.App;
import com.mhook.sample.tool.Debug;
import com.mhook.sample.tool.common.Builds;
import com.mhook.sample.tool.common.Files;

import org.apache.commons.lang3.StringUtils;


/**
 * Created by ASUS on 2020/7/31.
 */

public class AppTask extends Application {
    public static final String TAG="AppTask";
    private static AppTask instance;
    public static AppTask getInstance(){
        return instance;
    }
    private SharedPreferences preferences;
    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    private void init() {
        instance=this;
        preferences=getSharedPreferences(App.PREFERENCE_NAME, Context.MODE_PRIVATE);
        if(!isInitialized()){
            setupFridaJsTemplate();
            preferences.edit().putBoolean(App.PREFERENCE_INITIALIZED,true).apply();
        }
        setupFridaServer();
    }

    private void setupFridaServer() {
        Debug.LogI(TAG, "getCpuType...");
        String fridaServerName;
        switch (Builds.getCpuType()) {
            case ARM:
                fridaServerName = "fs12116arm";
                break;
            case ARM64:
                fridaServerName = "fs12116arm64";
                break;
            case X86:
                fridaServerName = "fs12116x86";
                break;
            default:
                Debug.LogE(TAG, "error BuildTool.getCpuType()");
                return;
        }
        Debug.LogI(TAG, "getCpuType...", fridaServerName);
        String targetPath = StringUtils.join(getFilesDir().getAbsolutePath() , "/" ,fridaServerName);
        if (!Files.copyToFiles(this, fridaServerName, targetPath)) {
            Debug.LogE(TAG, "error FileTool.copyToFiles");
        }
    }

    private void setupFridaJsTemplate() {
        if (!Files.copyToFiles(this, App.ASSETS_FRIDA_JS_NAME, App.FRIDA_JS_PATH+"/"+App.ASSETS_FRIDA_JS_NAME)) {
            Debug.LogE(TAG, "error FileTool.copyToFiles");
        }
    }

    public boolean isInitialized(){
       return preferences.getBoolean(App.PREFERENCE_INITIALIZED,false);
    }
}
