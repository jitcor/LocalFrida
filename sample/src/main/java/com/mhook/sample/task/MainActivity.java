package com.mhook.sample.task;


import android.Manifest;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.mhook.sample.R;
import com.mhook.sample.tool.Frida;
import com.mhook.sample.tool.FridaTaskWrapper;
import com.mhook.sample.tool.App;
import com.mhook.sample.tool.Debug;
import com.mhook.sample.tool.common.Shell;
import com.mhook.sample.tool.bean.JsBean;
import com.mhook.sample.tool.common.go.Go;
import com.mhook.sample.tool.common.go.Result;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";
    public TextView message;
    private List<FridaTaskWrapper> fridaTaskWrapperList = new ArrayList<>();

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
        message = findViewById(R.id.message);
        message.setMovementMethod(ScrollingMovementMethod.getInstance());
    }


    public void test(View view) {
        Toast.makeText(MainActivity.this, "测试...." + test(), Toast.LENGTH_LONG).show();
    }

    public static int test() {
        return 12345;
    }

    public static byte[] testSendBytes() {
        return new byte[]{1, 2, 3, 4};
    }

    public void go(View view) {
        if (!Shell.requestPermission()) {
            Debug.LogE(TAG, "not root permission!!");
            return;
        }
        findViewById(R.id.js_tips).setVisibility(View.GONE);
        Button button = (Button) view;
        boolean started = button.getTag() != null && (boolean) button.getTag();
        if (started) {
            button.setTag(false);
            button.setText("注入");
            for (FridaTaskWrapper wrapper : fridaTaskWrapperList) {
                wrapper.stop();
            }
            fridaTaskWrapperList.clear();
            return;
        }
        button.setEnabled(false);
        Go.go(() -> {
            if (!Frida.checkFridaServer()) {
                Debug.LogI(TAG, "start frida-server.");
                Result<String> result=Shell.execCommandNoWait(
                        new String[]{
                                StringUtils.join("cd ", getFilesDir().getAbsolutePath()),
                                StringUtils.join("chmod 777 ", App.getFridaServerName()),
                                StringUtils.join("./", App.getFridaServerName(), " -D -l 127.0.0.1:", App.FRIDA_SERVER_PORT)
                        },
                        true, true);
                if(result.error()!=null){
                    Debug.LogE(TAG,result.error().error());
                    return;
                }
                Debug.LogI(TAG,result.value());
                return;
            }

            Debug.LogI(TAG, "start frida task...");
            List<JsBean> jsBeanList = JsBean.parse(new File(App.FRIDA_JS_PATH));
            for (JsBean jsBean : jsBeanList) {
                fridaTaskWrapperList.add(new FridaTaskWrapper(this, jsBean.getProcess(),
                        jsBean.getJs(), App.FRIDA_SERVER_PORT,isReboot()).setFridaTaskListener(new FridaTaskWrapper.OnFridaTaskListener() {
                    @Override
                    public void onStarted() {
                        sendMessage(jsBean.getJsFileName(), ":Injected:", jsBean.getProcess(), "\n");
                        if (jsBean.getProcess().equals(getPackageName())) {
                            Debug.LogI(TAG, "Test:", test());
                        }
                    }

                    @Override
                    public void onMessage(String msg) {
                        sendMessage(jsBean.getJsFileName(), ":", msg);
                    }

                    @Override
                    public void onError(String err) {
                        sendMessage(jsBean.getJsFileName(), ":", err);
                    }

                    @Override
                    public void onStopped() {
                        sendMessage(jsBean.getJsFileName(), ":stopped:", jsBean.getProcess(), "\n");
                        runOnUiThread(() -> {
                            button.setEnabled(true);
                            button.setTag(false);
                            button.setText("注入");
                        });
                    }
                }).start());

            }
            runOnUiThread(() -> {
                button.setEnabled(true);
                button.setTag(true);
                button.setText("停止");
                message.setText("");
            });

        });

    }

    private boolean isReboot() {
        return ((CheckBox)findViewById(R.id.reboot)).isChecked();
    }

    private void sendMessage(String... messages) {
        runOnUiThread(() -> {
            for (String msg : messages) {
                message.append(msg);
            }
            message.append("\n");
            int offset = message.getLineCount() * message.getLineHeight();
            if (offset > message.getHeight()) {
                message.scrollTo(0, offset - message.getHeight());
            }
        });
    }

    public void stop(View view) {
        for (FridaTaskWrapper wrapper : fridaTaskWrapperList) {
            wrapper.stop();
        }
        fridaTaskWrapperList.clear();
    }
}
