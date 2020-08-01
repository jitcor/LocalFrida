package com.mhook.sample.tool.common.go;

import org.apache.commons.lang3.StringUtils;

public class Errors{
    public static Error New(Object... msg){
        return new Error() {
            @Override
            public String error() {
                return StringUtils.join(msg);
            }
        };
    }
}
