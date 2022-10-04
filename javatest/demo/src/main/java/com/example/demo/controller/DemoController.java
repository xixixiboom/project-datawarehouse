package com.example.demo.controller;

import com.example.demo.config.DemoConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.w3c.dom.ls.LSOutput;

import javax.annotation.PostConstruct;

public class DemoController {
    @Autowired
    DemoConfig demoConfig;

    public void demo(){
        System.out.println(demoConfig);
    }
}
