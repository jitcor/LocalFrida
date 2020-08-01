package com.mhook.sample.tool.common.go;

import org.apache.commons.lang3.StringUtils;

public class Results {
    public static <T> Result<T> New(T value,Error error){
        return new Result<T>() {
            @Override
            public T value() {
                return value;
            }

            @Override
            public Error error() {
                return error;
            }
        };
    }
}
