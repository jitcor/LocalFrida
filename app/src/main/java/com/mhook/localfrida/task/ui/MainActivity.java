package com.mhook.localfrida.task.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.mhook.localfrida.R;
import com.mhook.localfrida.task.fridatask.FridaTask;
import com.mhook.localfrida.tool.Debug;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    public void inject(View view) {
//        new FridaTask("com.mhook.localfrida","Java.perform(function () {\n" +
//                "    const MainActivity = Java.use(\"com.mhook.localfrida.task.ui.MainActivity\");\n" +
//                "    MainActivity.getVersionName.implementation = function () {\n" +
//                "        return \"9.9.9\";\n" +
//                "    }\n" +
//                "});",-1,true).start();
        new FridaTask("com.mhook.sample","Java.perform(function () {\n" +
                "    const MainActivity = Java.use(\n" +
                "        \"com.mhook.sample.task.MainActivity\");\n" +
                "    MainActivity.test.overload()\n" +
                "        .implementation = function () {\n" +
                "        console.log(\"Main.test:\" + this.test())\n" +
                "        return 54321;\n" +
                "    }\n" +
                "});\n",-1,true).start();

    }

    public void test(View view) {
        Toast.makeText(this, "verison:"+getVersionName(), Toast.LENGTH_SHORT).show();
    }
    public static String getVersionName(){
        return "1.0.0";
    }
}
