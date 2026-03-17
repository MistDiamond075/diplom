package com.diplom.diplom.controller;

import com.diplom.diplom.configuration.ConfProps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class CtrlConfig {
    private final ConfProps confProps;

    @Autowired
    public CtrlConfig(ConfProps confProps) {
        this.confProps = confProps;
    }

    @GetMapping("/getMode")
    public @ResponseBody boolean getMode(){
        return confProps.isProduction();
    }
}
