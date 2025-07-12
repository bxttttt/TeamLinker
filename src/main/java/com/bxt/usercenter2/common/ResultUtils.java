package com.bxt.usercenter2.common;

import com.sun.net.httpserver.Authenticator;
import org.springframework.scripting.bsh.BshScriptUtils;

import javax.crypto.interfaces.PBEKey;

public class ResultUtils<T> {
    public static<T> BaseResponse<T> success(T data){
        return new BaseResponse<T>(0,data,"ok");
    }
    public static BaseResponse error(ErrorCode e){
        return new BaseResponse<>(e);
    }
    public static BaseResponse error(ErrorCode e,String description){
        return new BaseResponse<>(e,description);
    }
    public static BaseResponse error(ErrorCode e,String description,String message){
        return new BaseResponse<>(e,description,message);
    }

    public static BaseResponse error(int code,String description, String message){
        return new BaseResponse<>(code,message,description);
    }

}
