package com.example.exceldemo;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;

@Configuration
@Data
@ConfigurationProperties(prefix = "test")
public class Test {
    private LinkedHashMap<String,String> map;
}
