package cn.syx.registry.server.controller;

import cn.syx.registry.server.mode.ExceptionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class SyxExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ExceptionResponse handlerException(Exception e) {
        return new ExceptionResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }
}
