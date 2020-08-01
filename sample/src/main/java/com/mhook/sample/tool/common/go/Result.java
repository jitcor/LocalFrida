package com.mhook.sample.tool.common.go;

public interface Result<T> {
    T value();
    Error error();
}
