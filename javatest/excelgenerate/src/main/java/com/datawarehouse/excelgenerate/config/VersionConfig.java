package com.datawarehouse.excelgenerate.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @ClassName versionConfig
 * @Description TODO
 * @Author xjy
 * @Date 2022/9/24 19:32
 * @Version 1.0
 **/
@Data
@Configuration
@ConfigurationProperties("version")
public class VersionConfig {
    String destDir;
    String outputFileName;
    String outputSheetName;
    List<String> fileType;
}
