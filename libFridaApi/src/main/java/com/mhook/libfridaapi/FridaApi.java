package com.mhook.libfridaapi;

public interface FridaApi {
    void start();
    void stop();
    void setFridaTaskListener(OnFridaListener fridaListener);
}
