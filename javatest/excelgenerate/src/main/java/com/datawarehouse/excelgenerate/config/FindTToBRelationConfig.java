package com.datawarehouse.excelgenerate.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @ClassName FindTToBConfig
 * @Description TODO
 * @Author xjy
 * @Date 2022/8/16 16:59
 * @Version 1.0
 **/
@Configuration
@ConfigurationProperties("findttobrelation")
@Data
public class FindTToBRelationConfig {
    private String warehousingExcelName;
    private String warehousingExcelSheetName;
    private String domesticSdmExcelName;
    private String domesticSdmExcelSheetName;
    private String overseasSdmExcelName;
    private String overseasSdmExcelSheetName;
    private String domesticOutputExcelName;
    private String overseasOutputExcelName;
}
