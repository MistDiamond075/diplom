package com.diplom.diplom.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ConfPropsCookies {
    @Value(value = "${cookie.id}")
    private String cookie_id;

    public String getCookie_id() {
        return cookie_id;
    }
}
