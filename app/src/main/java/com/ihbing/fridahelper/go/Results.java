package com.ihbing.fridahelper.go;

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
