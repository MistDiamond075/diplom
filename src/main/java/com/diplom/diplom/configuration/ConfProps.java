package com.diplom.diplom.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ConfProps {
    @Value("mode.production")
    private String production;

    public boolean isProduction() {
        return Boolean.parseBoolean(production);
    }
}
