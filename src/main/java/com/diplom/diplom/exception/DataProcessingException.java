package com.diplom.diplom.exception;

import org.springframework.http.HttpStatus;

public class DataProcessingException extends BaseException {
    public DataProcessingException(HttpStatus status, Integer code, String description, String msg_for_user) {
        super(status, code, description, msg_for_user);
    }

    public DataProcessingException(HttpStatus status, String description, String msg_for_user) {
        super(status, description, msg_for_user);
    }

    public DataProcessingException(HttpStatus status, Integer code, String description) {
        super(status, code, description);
    }
}
