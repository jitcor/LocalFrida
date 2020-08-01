package com.mhook.sample.tool;

import java.io.IOException;
import java.net.Socket;

public class Frida {
    public static final String TAG="Frida";
    public static boolean checkFridaServer(){
        try {
            Socket socket=new Socket("127.0.0.1",App.FRIDA_SERVER_PORT);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
