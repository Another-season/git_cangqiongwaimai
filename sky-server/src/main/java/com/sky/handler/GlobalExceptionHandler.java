package com.sky.handler;

import com.sky.constant.MessageConstant;
import com.sky.exception.BaseException;
import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 捕获业务异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(BaseException ex){
        log.error("异常信息：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }
    //向数据库插入时，捕获到重复的用户名异常
    @ExceptionHandler
    public Result exceptionHandler(SQLIntegrityConstraintViolationException ex){
        //异常值：Duplicate entry 'zhangsan' for key 'employee.username'
        String message = ex.getMessage();
        if(message.contains("Duplicate entry")) {
            String[] split = message.split("");
            String username = split[2];
            return Result.error(username + MessageConstant.ALREADY_EXISTS);
        } else {
            return Result.error("未知错误");
        }
    }

}
