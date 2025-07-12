package com.bxt.usercenter2.enums;

import com.bxt.usercenter2.common.ErrorCode;
import com.bxt.usercenter2.exception.BusinessException;

public enum StatusCode {
    PUBLIC(0,"public"),
    PRIVATE(1,"private"),
    SECRET(2,"secret");




    private final int code;
    private final String message;

    StatusCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
    public static StatusCode getTypeByValue(Integer code) {
        if (code==null) throw new BusinessException(ErrorCode.PARAMS_ERROR, "Status code cannot be null");
        for (StatusCode status : StatusCode.values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "Invalid status code: " + code);
    }

}
