package com.datawarehouse.excelgenerate.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @ClassName MatchFieldConfig
 * @Description TODO
 * @Author xjy
 * @Date 2022/9/18 16:17
 * @Version 1.0
 **/
@Configuration
@Data
@ConfigurationProperties("matchfield")
public class MatchFieldConfig {
    private String demandInputTemplateDetailFileName;
    private String demandInputTemplateDetailSheetName;
    private String matchFieldOutputFileName;
    private String domesticSdmFileName;
    private String overseasSdmFileName;
    private String domesticSdmSheetName;
    private String overseasSdmSheetName;
    private String reduceDiameterOutputFileName;
    private Integer dateDomesticUpperLimit;
    private Integer dateDomesticLowerLimit;
    private Integer dateOverseasUpperLimit;
    private Integer dateOverseasLowerLimit;
}
