package com.mhook.sample.tool;

import android.content.Context;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by ASUS on 2020/7/29.
 */

public class MyFile {
    public static String readLine(File file){
        try {
            InputStreamReader reader=new InputStreamReader(new FileInputStream(file));
            BufferedReader bufferedReader=new BufferedReader(reader);
            return bufferedReader.readLine();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
    public static byte[] assets(Context context,String fileName) {
        try {
            InputStream inputStream = context.getAssets().open(fileName);
            ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
            byte[] buff = new byte[100]; //buff用于存放循环读取的临时数据 
            int rc = 0;
            while ((rc = inputStream.read(buff, 0, 100)) > 0) {
                swapStream.write(buff, 0, rc);
            }
            return swapStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static String assetsText(Context context,String fileName) {
        try {
            InputStream inputStream = context.getAssets().open(fileName);
            ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
            byte[] buff = new byte[100]; //buff用于存放循环读取的临时数据 
            int rc = 0;
            while ((rc = inputStream.read(buff, 0, 100)) > 0) {
                swapStream.write(buff, 0, rc);
            }
            return swapStream.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int getFileSize(Context context, String assetsFileName) {
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

    public static boolean copyToFiles(Context context, String assetsFileName, String targetPath) {
        try {
            InputStream open = context.getAssets().open(assetsFileName);
            int fileSize = open.available();
            FileOutputStream fileOutputStream;
            File file = new File(targetPath);
            if (file.exists()) {
                if (file.length() == ((long) fileSize)) {
                    return true;
                }
                file.delete();
            }

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
        } catch (IOException e) {
            e.printStackTrace();
        }
        new File(targetPath).delete();
        return false;
    }

}
