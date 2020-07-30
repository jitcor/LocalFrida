package com.mhook.sample.tool;

import android.content.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by ASUS on 2020/7/29.
 */

public class FileTool {
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
