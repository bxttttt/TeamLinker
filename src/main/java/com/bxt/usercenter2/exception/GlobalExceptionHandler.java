package com.bxt.usercenter2.exception;

import com.bxt.usercenter2.common.BaseResponse;
import com.bxt.usercenter2.common.ErrorCode;
import com.bxt.usercenter2.common.ResultUtils;
//import lombok.extern.slf4j.Slf4j;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.logging.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.logging.Logger;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(BusinessException.class)
    @ResponseBody
    public BaseResponse businessExceptionHandler(BusinessException e) {
        log.error("businessException"+e.getMessage(),e);
        return ResultUtils.error(e.getCode(),e.getDescription(),e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseBody
    public BaseResponse runtimeExceptionHandler(RuntimeException e) {
        log.error("runtimeException",e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR,"", e.getMessage());
    }


}
