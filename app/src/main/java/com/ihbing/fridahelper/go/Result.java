package com.ihbing.fridahelper.go;

public interface Result<T> {
    T value();
    Error error();
}
