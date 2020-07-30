package com.mhook.sample.tool;

import android.os.Build;
import android.text.TextUtils;

/**
 * Created by ASUS on 2020/7/29.
 */

public class BuildTool {
    public enum CpuType {
        ARM, ARM64, X86,UNKNOWN
    }

    public static CpuType getCpuType() {
        String[] abis;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            abis = Build.SUPPORTED_ABIS;
        } else {
            abis = new String[]{Build.CPU_ABI, Build.CPU_ABI2};
        }
        for (String abi : abis) {
            if(TextUtils.isEmpty(abi))continue;
            if(abi.toLowerCase().contains("x86"))return CpuType.X86;
            if(abi.toLowerCase().contains("arm64"))return CpuType.ARM64;
            if(abi.toLowerCase().contains("arm"))return CpuType.ARM;
        }
        return CpuType.UNKNOWN;
    }
}
