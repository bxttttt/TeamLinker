package com.bxt.usercenter2.common;

import ch.qos.logback.classic.spi.STEUtil;
import lombok.Data;

import java.io.PipedInputStream;
import java.io.Serializable;
import java.security.PrivateKey;

@Data
public class BaseResponse<T> implements Serializable {
    private int code;
    private T data;
    private String message;
    private String description;
    public BaseResponse(int code,T data,String message,String description){
        this.code=code;
        this.data=data;
        this.message=message;
        this.description=description;
    }
    public BaseResponse(int code,T data){
        this(code,data,"","");
    }
    public BaseResponse(int code,String message,String description){
        this(code,null,message,description);
    }
    public BaseResponse(int code,T data,String message){
        this(code,data,message,"");
    }

    public BaseResponse(ErrorCode e){
        this(e.getCode(), null, e.getDescription(),e.getMessage());
    }

    public BaseResponse(ErrorCode e,String description){
        this(e.getCode(), null, description,e.getMessage());
    }
    public BaseResponse(ErrorCode e,String description,String message){
        this(e.getCode(), null, description,message);
    }

}
