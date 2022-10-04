package com.example.demo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@Data
@ConfigurationProperties(prefix="dcdsexcelconfig")
public class  DemoConfig {
    private String dcdsExcelFileName;

    private String dcdsExcelSheetName;

    public List<String> tableNameEn;

}
