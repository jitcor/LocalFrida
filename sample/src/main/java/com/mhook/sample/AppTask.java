package com.mhook.sample;

import android.app.Application;

import io.github.kbiakov.codeview.classifier.CodeProcessor;

/**
 * Created by ASUS on 2020/7/31.
 */

public class AppTask extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CodeProcessor.init(this);
    }
}
