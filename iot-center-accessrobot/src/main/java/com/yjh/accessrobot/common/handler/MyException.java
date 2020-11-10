package com.yjh.accessrobot.common.handler;

public class MyException extends RuntimeException {
    public MyException(String message){
        super(message);
    }

    public MyException(String message, Throwable e){
        super(message + e.getLocalizedMessage());
    }

}
