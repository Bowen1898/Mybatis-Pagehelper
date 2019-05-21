package com.yuyuko.pagehelper;

public class PageException extends RuntimeException {

    public PageException(String message) {
        super(message);
    }

    public PageException(String message, Throwable cause) {
        super(message, cause);
    }
}
