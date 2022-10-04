package com.datawarehouse.excelgenerate.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @ClassName FindTableRelationConfig
 * @Description 包含四个excel的表名和sheet名
 * @Author xjy
 * @Date 2022/8/9 16:20
 * @Version 1.0
 **/
@Configuration
@ConfigurationProperties(prefix="findtablerelation")
@Data
public class FindTableRelationConfig {
    private String warehousingExcelName;
    private String warehousingExcelSheetName;
    private String domesticSdmExcelName;
    private String domesticSdmExcelSheetName;
    private String overseasSdmExcelName;
    private String overseasSdmExcelSheetName;
    private String templateOutputExcelName;
    private String templateOutputExcelSheetName;
    private String domesticRelationExcelName;
    private String domesticRelationExcelSheetName;
    private String overseasRelationExcelName;
    private String overseasRelationExcelSheetName;
    private String domesticOutputExcelName;
    private String overseasOutputExcelName;
    private String domesticOutputExcelNameWithoutSdm;
    private String overseasOutputExcelNameWithoutSdm;
}
