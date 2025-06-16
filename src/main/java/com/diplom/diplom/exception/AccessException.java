package com.diplom.diplom.exception;

import com.diplom.diplom.configuration.userdetails.DiplomUserDetails;
import com.diplom.diplom.misc.utils.Parser;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class AccessException extends BaseException{
    private final DiplomUserDetails userDetails;

    public AccessException(HttpStatus status, Integer code, String description, String msg_for_user, DiplomUserDetails userDetails) {
        super(status, code, description, msg_for_user);
        this.userDetails = userDetails;
    }

    public AccessException(HttpStatus status, String description, String msg_for_user, DiplomUserDetails userDetails) {
        super(status, description, msg_for_user);
        this.userDetails = userDetails;
    }

    public AccessException(HttpStatus status, Integer code, String description, DiplomUserDetails userDetails) {
        super(status, code, description);
        this.userDetails = userDetails;
    }

    public String getUserRole(){
        return Parser.parseUserRole(userDetails);
    }
}
