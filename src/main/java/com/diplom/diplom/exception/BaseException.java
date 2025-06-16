package com.diplom.diplom.exception;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

public class BaseException extends Exception{
    private final HttpStatus status;
    private final Integer code;
    private final String description;
    private final String msg_for_user;

    public BaseException(HttpStatus status, Integer code, String description, String msg_for_user) {
        this.status = status;
        this.code = code;
        this.description = description;
        this.msg_for_user = msg_for_user;
    }

    public BaseException(HttpStatus status, String description, String msg_for_user) {
        this.status = status;
        this.code = null;
        this.description = description;
        this.msg_for_user = msg_for_user;
    }

    public BaseException(HttpStatus status, Integer code, String description) {
        this.status = status;
        this.code = code;
        this.description = description;
        this.msg_for_user = null;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public String getMsg_for_user() {
        return msg_for_user;
    }

    @Override
    public String toString(){
        String  simple_description=getStackTraceDetails(getStackTrace());
        return "["+ LocalDateTime.now() +"] Error ("+code+"): "+description+'\n'+ "details { \n"+simple_description+"\n}";
    }

    private String getStackTraceDetails(StackTraceElement[] stackTraceElements){
        if(stackTraceElements.length>0){
             StackTraceElement element=stackTraceElements[0];
             return "\tClass: "+element.getClassName()+'\n'
                     +"\tMethod: "+element.getMethodName()+'\n'
                     +"\tLine: "+element.getLineNumber();
        }
        return "unknown";
    }
}
