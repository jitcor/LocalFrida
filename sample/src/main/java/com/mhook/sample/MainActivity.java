package com.mhook.sample;

import android.content.Context;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.mhook.sample.task.FridaTaskWrapper;
import com.mhook.sample.tool.App;
import com.mhook.sample.tool.BuildTool;
import com.mhook.sample.tool.Debug;
import com.mhook.sample.tool.FileTool;
import com.mhook.sample.tool.ShellUtil;
import com.mhook.sample.tool.go.Go;

import org.apache.commons.lang3.StringUtils;


public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }


    public void test(View view) {
        Toast.makeText(MainActivity.this, "测试...." + test(), Toast.LENGTH_LONG).show();
    }

    public static int test() {
        return 12345;
    }

    public void go(View view) {
        Go.go(() -> {
            Debug.LogI(TAG,"getCpuType...");
            String fridaServerName;
            switch (BuildTool.getCpuType()) {
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
            Debug.LogI(TAG,"getCpuType...",fridaServerName);
            String targetPath = getFilesDir().getAbsolutePath() + "/" + fridaServerName;
            if (!FileTool.copyToFiles(this, fridaServerName, targetPath)) {
                Debug.LogE(TAG, "error FileTool.copyToFiles");
                return;
            }
           if(!ShellUtil.permission()){
                Debug.LogE(TAG,"not root permission!!");
                return;
           }
            ShellUtil.execCommandNoWait(
                    new String[]{
                            StringUtils.join("cd ", getFilesDir().getAbsolutePath()),
                            StringUtils.join("chmod 777 ", fridaServerName),
                            StringUtils.join("./", fridaServerName, " -D -l 127.0.0.1:", App.FRIDA_SERVER_PORT)
                    },
                    true,false);
//            if (commandResult.result != 0) {
//                Debug.LogE(TAG, "error commandResult.result!=0:", commandResult.errorMsg);
//                return;
//            }
//            Debug.LogI(TAG,"commandResult:",commandResult.successMsg);
            Debug.LogI(TAG,"start frida task...");
            new FridaTaskWrapper(this, "com.mhook.sample",
                    "Java.perform(function () {\n" +
                            "    const MainActivity = Java.use(\"com.mhook.sample.MainActivity\");\n" +
                            "    MainActivity.test.overload().implementation = function () {\n" +
                            "        return 54321;\n" +
                            "    }\n" +
                            "});", App.FRIDA_SERVER_PORT).setFridaTaskListener(new FridaTaskWrapper.OnFridaTaskListener() {
                @Override
                public void onStarted() {
                    runOnUiThread(()->{
                        Toast.makeText(MainActivity.this, "启动成功....", Toast.LENGTH_LONG).show();
                    });
                }

                @Override
                public void onStopped() {
                    runOnUiThread(()->{
                        Toast.makeText(MainActivity.this, "已停止....", Toast.LENGTH_LONG).show();
                    });

                }
            }).start();

        });

    }
}
