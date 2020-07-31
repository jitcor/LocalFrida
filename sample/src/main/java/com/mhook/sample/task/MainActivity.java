package com.mhook.sample.task;


import android.Manifest;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.mhook.sample.R;
import com.mhook.sample.tool.FridaTaskWrapper;
import com.mhook.sample.tool.App;
import com.mhook.sample.tool.MyBuild;
import com.mhook.sample.tool.Debug;
import com.mhook.sample.tool.MyFile;
import com.mhook.sample.tool.Shell;
import com.mhook.sample.tool.bean.JsBean;
import com.mhook.sample.tool.go.Go;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;



public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";
    public TextView message;
    private List<FridaTaskWrapper> fridaTaskWrapperList=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        permissions();
        message();
    }

    private void permissions() {
        Permissions.check(this/*context*/, Manifest.permission.WRITE_EXTERNAL_STORAGE, null, new PermissionHandler() {
            @Override
            public void onGranted() {
                // do your task.
            }
        });
    }

    private void message() {
//        codeView = findViewById(R.id.code_view);
//        codeView.setCode(MyFile.assetsText(this,"frida.js"), "js");
        message=findViewById(R.id.message);
        message.setMovementMethod(ScrollingMovementMethod.getInstance());
    }


    public void test(View view) {
        Toast.makeText(MainActivity.this, "测试...." + test(), Toast.LENGTH_LONG).show();
    }

    public static int test() {
        return 12345;
    }
    public static byte[] testSendBytes() {

        return new byte[]{1,2,3,4};
    }

    public void go(View view) {
        Go.go(() -> {
            for (FridaTaskWrapper wrapper:fridaTaskWrapperList){
                wrapper.stop();
            }
            fridaTaskWrapperList.clear();
            Debug.LogI(TAG, "getCpuType...");
            String fridaServerName;
            switch (MyBuild.getCpuType()) {
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
            String targetPath = getFilesDir().getAbsolutePath() + "/" + fridaServerName;
            if (!MyFile.copyToFiles(this, fridaServerName, targetPath)) {
                Debug.LogE(TAG, "error FileTool.copyToFiles");
                return;
            }
            if (!Shell.permission()) {
                Debug.LogE(TAG, "not root permission!!");
                return;
            }
            Shell.execCommandNoWait(
                    new String[]{
                            StringUtils.join("cd ", getFilesDir().getAbsolutePath()),
                            StringUtils.join("chmod 777 ", fridaServerName),
                            StringUtils.join("./", fridaServerName, " -D -l 127.0.0.1:", App.FRIDA_SERVER_PORT)
                    },
                    true, false);
            Debug.LogI(TAG, "start frida task...");
            List<JsBean> jsBeanList = JsBean.parse(new File(App.FRIDA_JS_PATH));
            for (JsBean jsBean:jsBeanList){
              fridaTaskWrapperList.add(new FridaTaskWrapper(this, jsBean.getProcess(),
                        jsBean.getJs(), App.FRIDA_SERVER_PORT).setFridaTaskListener(new FridaTaskWrapper.OnFridaTaskListener() {
                    @Override
                    public void onStarted() {
                        sendMessage(jsBean.getJsFileName(),":已注入",jsBean.getProcess(),"\n");
                        if(jsBean.getProcess().equals(getPackageName())){
                            Debug.LogI(TAG,"Test:",test());
                        }
                    }

                    @Override
                    public void onMessage(String msg) {
                        sendMessage(jsBean.getJsFileName(),":",msg,"\n");
                    }

                    @Override
                    public void onStopped() {
                        sendMessage(jsBean.getJsFileName(),":已停止",jsBean.getProcess(),"\n");
                    }
                }).start());

            }

        });

    }

    private void sendMessage(String... messages) {
        runOnUiThread(()->{
            for (String msg:messages){
                message.append(msg);
            }
            int offset=message.getLineCount()*message.getLineHeight();
            if(offset>message.getHeight()){
                message.scrollTo(0,offset-message.getHeight());
            }
        });
    }
}
