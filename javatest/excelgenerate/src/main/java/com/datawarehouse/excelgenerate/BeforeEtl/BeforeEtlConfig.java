package com.datawarehouse.excelgenerate.BeforeEtl;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Configuration
@Data
@ConfigurationProperties(prefix="beforeetl")
public class BeforeEtlConfig {
    private List<String> emptyDir;
//    private List<String> needBackupDir;
    private LinkedHashMap<String,String> needBackupAndCreateDir;
//    private Boolean isDeleteDir;
//    private Boolean isBackupDir;
    private String targetBackupDir;
}
