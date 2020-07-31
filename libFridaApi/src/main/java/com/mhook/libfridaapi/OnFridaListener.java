package com.mhook.libfridaapi;

public interface OnFridaListener {
    void onStarted();
    void onMessage(String msg);
    void onStopped();
}
