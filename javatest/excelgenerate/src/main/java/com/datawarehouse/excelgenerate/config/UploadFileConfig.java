package com.datawarehouse.excelgenerate.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;

@Configuration
@Data
@ConfigurationProperties(prefix="uploadfile")
public class UploadFileConfig {
    private String ip;
    private int port;
    private String userName;
    private String passWord;
    private LinkedHashMap<String,String> localAndRemoteDir;
}
