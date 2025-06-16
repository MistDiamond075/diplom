package com.diplom.diplom.controller;

import com.diplom.diplom.exception.AccessException;
import com.diplom.diplom.exception.BaseException;
import com.diplom.diplom.exception.EntityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class CtrlException {
    private static final Logger log = LoggerFactory.getLogger(CtrlException.class);

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<Map<String,Object>> BaseExceptionHandler(BaseException e){
        log.error("e: ", e);
        return ResponseEntity.status(e.getStatus()).body(createBaseExceptionResponse(e));
    }

    @ExceptionHandler(EntityException.class)
    public ResponseEntity<Map<String,Object>> EntityExceptionHandler(EntityException e){
        log.error("e: ", e);
        return ResponseEntity.status(e.getStatus()).body(createBaseExceptionResponse(e));
    }

    @ExceptionHandler(AccessException.class)
    public ResponseEntity<Map<String,Object>> AccesExceptionHandler(AccessException e){
        Map<String,Object> resp=createBaseExceptionResponse(e);
        resp.put("role",e.getUserRole());
        log.error("e: ", e);
        return ResponseEntity.status(e.getStatus()).body(resp);
    }

    private Map<String,Object> createBaseExceptionResponse(BaseException e){
        Map<String,Object> resp=new HashMap<>();
        resp.put("message",e.getMsg_for_user());
        resp.put("status",e.getStatus());
        resp.put("code",e.getCode());
        resp.put("time", LocalDateTime.now());
        return resp;
    }
}
