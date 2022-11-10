package com.meals.exception;

import com.meals.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 配置全局异常处理
 */
@ControllerAdvice(annotations = {RestController.class, Controller.class})//指定拦截哪些类上加了这些注解
@Slf4j
@ResponseBody//最后需要返回JSON数据
public class GlobalExceptionHandel  {

    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)//捕获SQLIntegrityConstraintViolationException这个异常
    public R<String> exceptionHander(SQLIntegrityConstraintViolationException exception){
        String message = exception.getMessage();//Duplicate entry 'lisi' for key 'idx_username'   名字重复异常
        if (message.contains("Duplicate entry")){
            String[] s = message.split(" ");
            String msg = "用户名"+s[2]+"已经存在";
            return R.error(msg);
        }

        return R.error("未知错误");
    }

    @ExceptionHandler(CustomException.class)//捕获SQLIntegrityConstraintViolationException这个异常
    public R<String> exceptionHander(CustomException customException){
        return R.error(customException.getMessage());
    }
}
