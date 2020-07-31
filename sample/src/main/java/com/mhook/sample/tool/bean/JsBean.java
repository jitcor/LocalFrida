package com.mhook.sample.tool.bean;

import android.text.TextUtils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ASUS on 2020/7/31.
 */

public class JsBean {
    private final String process;
    private final String js;
<<<<<<< HEAD
    private final String jsFileName;

    public JsBean(String process, String js, String jsFileName) {
        this.process = process;
        this.js = js;
        this.jsFileName = jsFileName;
    }

    public String getJsFileName() {
        return jsFileName;
=======

    public JsBean(String process, String js) {
        this.process = process;
        this.js = js;
>>>>>>> 68eed3e42872565ebfb459cc70abcffacdf5d7f8
    }

    public String getProcess() {
        return process;
    }

    public String getJs() {
        return js;
    }
    public static List<JsBean> parse(File dir){
        List<JsBean> jsBeans=new ArrayList<>();
        for(File file: FileUtils.listFiles(dir,new String[]{"js"},false)){
            if(!file.exists())continue;
            try {
                List<String> readLines = FileUtils.readLines(file);
                if(readLines.size()>0){
                    String fristLine=readLines.get(0);
                    if(!TextUtils.isEmpty(fristLine)){
                        if(fristLine.trim().startsWith("//")){
                            String process=fristLine.substring(2).trim();
                            StringBuilder jsBuffer =new StringBuilder();
                            for (int i=1;i<readLines.size();i++){
                                jsBuffer.append(readLines.get(i)).append("\n");
                            }
<<<<<<< HEAD
                            jsBeans.add(new JsBean(process,jsBuffer.toString(), file.getName()));
=======
                            jsBeans.add(new JsBean(process,jsBuffer.toString()));
>>>>>>> 68eed3e42872565ebfb459cc70abcffacdf5d7f8
                        }
                    }

                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return jsBeans;
    }

}
