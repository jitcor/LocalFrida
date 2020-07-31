package com.mhook.sample.tool;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.mhook.libfridaapi.FridaApi;
import com.mhook.libfridaapi.OnFridaListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import dalvik.system.DexClassLoader;

public class FridaTaskWrapper {
    public static final String TAG="FridaTaskWrapper";
    private OnFridaTaskListener fridaTaskListener;
    private FridaApi fridaTask;

    private final String process;
    public FridaTaskWrapper(Context context,String process, String script,int port){
        this.process=process;
        try {
            String filesPath=context.getFilesDir().getAbsolutePath();
            String dexPath=filesPath + "/" + App.ASSETS_FRIDA_NAME;
            if(MyFile.copyToFiles(context,App.ASSETS_FRIDA_NAME,dexPath)){
                DexClassLoader dexClassLoader = new DexClassLoader(dexPath, filesPath,  null,context.getClassLoader());
                fridaTask = (FridaApi) newObject(dexClassLoader,App.ASSETS_FRIDA_CLASS_MAIN,new Object[]{process,script,port});
                fridaTask.setFridaTaskListener(new OnFridaListener() {
                    @Override
                    public void onStarted() {
                        if(fridaTaskListener!=null){
                            fridaTaskListener.onStarted();
                        }
                    }

                    @Override
                    public void onMessage(String msg) {
                        if(TextUtils.isEmpty(msg)){
                            return;
                        }
                        try {
                            JSONObject jsonObject=new JSONObject(msg);
                            String type=jsonObject.getString("type");
                            if(TextUtils.equals(type,"send")||TextUtils.equals(type,"log")){
                                if(fridaTaskListener!=null){
                                    fridaTaskListener.onMessage(jsonObject.getString("payload"));
                                }
                            }else if (TextUtils.equals(type,"error")){
                                if(fridaTaskListener!=null){
                                    fridaTaskListener.onMessage(jsonObject.getString("stack"));
                                }
                            }
//                            type = message["type"]
//                            msg = message
//                            if type == "send":
//                            msg = message["payload"]
//                            elif type == 'error':
//                            msg = message['stack']
//    else:
//                            msg = message
//                            print(msg)
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onStopped() {
                        if(fridaTaskListener!=null){
                            fridaTaskListener.onStopped();
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getProcess() {
        return process;
    }
    public FridaTaskWrapper setFridaTaskListener(OnFridaTaskListener fridaTaskListener) {
        this.fridaTaskListener = fridaTaskListener;
        return this;
    }

    public FridaTaskWrapper start(){
        if(fridaTask !=null){
           fridaTask.start();
        }
        return this;
    }
    public void stop(){
        if(fridaTask !=null){
           fridaTask.stop();
        }
    }

    public interface OnFridaTaskListener {
        void onStarted();
        void onMessage(String msg);
        void onStopped();
    }

    private static boolean copyToFiles(InputStream open, String targetPath, int fileSize) {
        FileOutputStream fileOutputStream;
        File file = new File(targetPath);
        if (file.exists()) {
            if (file.length() == ((long) fileSize)) {
                return true;
            }
            file.delete();
        }
        try {
            fileOutputStream = new FileOutputStream(file);
            try {
                byte[] bArr = new byte[8192];
                int readSize = 0;
                while (true) {
                    int read = open.read(bArr, 0, 8192);
                    if (read <= 0) {
                        break;
                    }
                    fileOutputStream.write(bArr, 0, read);
                    readSize += read;
                }
                fileOutputStream.flush();
                fileOutputStream.close();
                if (readSize == fileSize || fileSize == -1) {
                    return true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        new File(targetPath).delete();
        return false;
    }
    private static int getFileSize(Context context, String assetsFileName) {
        int result;
        try {
            InputStream openRawResource = context.getAssets().open(assetsFileName);
            result = openRawResource.available();
            try {
                openRawResource.close();
            } catch (Exception e3) {
                e3.printStackTrace();
                return result;
            }
        } catch (Exception e4) {
            result = -1;
            e4.printStackTrace();
        }
        return result;
    }
    public static Object newObject(ClassLoader clsLoader, String clsName, Object[] params) {
        try {
            Class<?> cls = findClass(clsLoader,clsName);
            if(cls==null){
                return null;
            }
            if (params == null) {
                return cls.newInstance();
            }
            Class[] clsArr = new Class[params.length];
            for (int i = 0; i < params.length; i++) {
                clsArr[i] = params[i].getClass();
            }
            return cls.getConstructor(clsArr).newInstance(params);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "create object failed for class:" + clsName);
            return null;
        }
    }

    private static Class<?> findClass(ClassLoader clsLoader, String str) {
        if (clsLoader == null) {
            try {
                return Class.forName(str);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return null;
            }
        }
        try {
            return clsLoader.loadClass(str);
        } catch (Exception e) {
            return null;
        }
    }

}
