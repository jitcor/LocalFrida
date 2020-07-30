package com.mhook.localfrida.tool;

import org.json.JSONObject;

public class Json {
    public static String formatJson(String json){
        try{
            return new JSONObject(json).toString(1);
        }catch (Exception e){
            e.printStackTrace();
            return json;
        }

    }
}
