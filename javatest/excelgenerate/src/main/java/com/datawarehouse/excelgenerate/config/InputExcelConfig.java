package com.datawarehouse.excelgenerate.config;


import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@Data
@ConfigurationProperties(prefix = "inputexcelconfig")
public class InputExcelConfig {

    private String fileName;

    private String sheetName;

    public List<String> tableNameEn;

    public List<String> tableNameCn;

    private String isFiltrate;

    private String changeSheetName;



//    @Value(value="${dcdsExcelConfig.fileName}")

}
