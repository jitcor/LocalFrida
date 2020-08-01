package com.mhook.libfridaapi;

public interface OnFridaListener {
    void onStarted();
    void onMessage(String msg);
    void onError(String err);
    void onStopped();
}
