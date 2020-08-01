package com.mhook.sample.tool;

import com.mhook.sample.tool.common.Builds;

public class App {
    public static final String ASSETS_FRIDA_NAME = "frida12_1.0.apk";
    public static final String ASSETS_FRIDA_JS_NAME = "frida.js";
    public static final String ASSETS_FRIDA_CLASS_MAIN = "com.mhook.localfrida.task.fridatask.FridaTask";
    public static final String FRIDA_JS_PATH = "/sdcard/LocalFrida/";
    public static final String FRIDA_SERVER_DIR = "/data/local/tmp/";
    public static final int FRIDA_SERVER_PORT = 59527;
    public static final String PREFERENCE_NAME = "LocalFrida";
    public static final String PREFERENCE_INITIALIZED = "initialized";

    public static String getFridaServerName() {
        switch (Builds.getCpuType()) {
            case ARM:
                return "fs12116arm";
            case ARM64:
                return "fs12116arm64";
            case X86:
                return "fs12116x86";
            default:
                return "fs12116arm";
        }
    }

}
