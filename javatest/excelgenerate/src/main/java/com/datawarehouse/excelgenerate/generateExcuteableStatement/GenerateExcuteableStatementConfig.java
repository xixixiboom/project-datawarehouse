package com.datawarehouse.excelgenerate.generateExcuteableStatement;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
@ConfigurationProperties(prefix = "generateexecuteablestatement")
public class GenerateExcuteableStatementConfig {
    private String mDdlDir;
    private String mDmlDir;
//    private String mIulDir;
    private String imlDate;
    private String iulDate;
    private String generateDir;
}
