package com.bxt.usercenter2.exception;

import com.bxt.usercenter2.common.ErrorCode;

import javax.crypto.interfaces.PBEKey;

public class BusinessException extends RuntimeException{
    private final int code;



    private final String description;

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public BusinessException(String message,int code,String description){
        super(message);
        this.code=code;
        this.description=description;
    }

    public BusinessException(ErrorCode e){
        super(e.getMessage());
        this.code=e.getCode();
        this.description=e.getDescription();
    }
    public BusinessException(ErrorCode e,String description){
        super(e.getMessage());
        this.code=e.getCode();
        this.description=description;
    }

}
