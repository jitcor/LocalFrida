package com.mhook.sample;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.mhook.sample.task.AppTask;
import com.mhook.sample.tool.App;
import com.mhook.sample.tool.common.Shell;
import com.mhook.sample.tool.common.Tips;
import com.mhook.sample.tool.common.go.Result;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    public static final String TAG="ExampleInstrumentedTest";
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.mhook.sample", appContext.getPackageName());
    }
    @Test
    public void shellTest(){
        if(Shell.requestPermission()){
            Result< Shell.CommandResult> result=Shell.execCommandNoWait(
                    new String[]{
                            StringUtils.join("cd ", App.FRIDA_JS_PATH),
                            StringUtils.join("chmod 777 ", App.getFridaServerName()),
                            StringUtils.join("./", App.getFridaServerName(), " -D -l 127.0.0.1:", App.FRIDA_SERVER_PORT)
                    },
                    true, true);
            if(result.error()!=null){
                Log.e(TAG,result.error().error());
                return;
            }
            Log.i(TAG,result.value().errorMsg+";"+result.value().successMsg+";"+result.value().result);
        }

    }
    @Test
    public void showToastTest(){
        Tips.showToast(AppTask.getInstance(),"test");
    }
}
