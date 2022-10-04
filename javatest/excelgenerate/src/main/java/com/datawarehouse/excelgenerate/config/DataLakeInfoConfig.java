package com.datawarehouse.excelgenerate.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @ClassName DataLakeInfoConfig
 * @Description TODO
 * @Author xjy
 * @Date 2022/9/15 16:25
 * @Version 1.0
 **/
@Configuration
@Data
@ConfigurationProperties("datalakeinfo")
public class DataLakeInfoConfig {
    private String demandInputTemplateTableListFileName;
    private String demandInputTemplateTableListSheetName;
    private String dataUpperLimit;
    private String dataLowerLimit;
    private int dataSizeStandardValue;
    private String odsOutputFileName;

}
