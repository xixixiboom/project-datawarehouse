package com.datawarehouse.excelgenerate.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @ClassName OverseasDcdsProvinceScopeConfig
 * @Description TODO
 * @Author xjy
 * @Date 2022/10/14 9:56
 * @Version 1.0
 **/
@Configuration
@ConfigurationProperties("overseasdcdsprovince")
@Data
public class OverseasDcdsProvinceScopeConfig {
    String dcdsProvinceScopeExcelName;
    String dcdsProvinceScopeSheetName;
    String outputFileName;
}
