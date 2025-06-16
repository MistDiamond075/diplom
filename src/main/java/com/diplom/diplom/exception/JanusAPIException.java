package com.diplom.diplom.exception;

import org.springframework.http.HttpStatus;

public class JanusAPIException extends BaseException {
    public JanusAPIException(HttpStatus status, Integer code, String description, String msg_for_user) {
        super(status, code, description, msg_for_user);
    }

    public JanusAPIException(HttpStatus status, String description, String msg_for_user) {
        super(status, description, msg_for_user);
    }

    public JanusAPIException(HttpStatus status, Integer code, String description) {
        super(status, code, description);
    }
}
