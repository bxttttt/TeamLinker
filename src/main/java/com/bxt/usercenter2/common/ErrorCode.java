package com.bxt.usercenter2.common;

import org.apache.tomcat.util.http.fileupload.InvalidFileNameException;

public enum ErrorCode {
    SYSTEM_ERROR(50000,"系统内部错误",""),
    SUCCESS(0,"ok",""),
    PARAMS_ERROR(40000,"参数错误",""),
    NULL_ERROR(40001,"请求数据为空",""),
    NOT_LOGIN(40002,"未登录",""),
    NO_AUTH(40003,"没权限","");




    private final int code;
    private final String message;

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getDescription() {
        return description;
    }

    private final String description;

    ErrorCode(int code, String message, String description) {
        this.code = code;
        this.message = message;
        this.description = description;
    }


}
