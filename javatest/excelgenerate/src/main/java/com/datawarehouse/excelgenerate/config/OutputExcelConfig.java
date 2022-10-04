package com.datawarehouse.excelgenerate.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
@Data
public class OutputExcelConfig {
    @Value(value = "${outputExcelConfig.sdmFileName}")
    private String outputExcelSdmFileName;

    @Value(value = "${outputExcelConfig.sdmSheetName}")
    private String outputExcelSdmSheetName;

    @Value(value = "${outputExcelConfig.pdmFileName}")
    private String outputExcelPdmFileName;

    @Value(value = "${outputExcelConfig.pdmSheetName}")
    private String outputExcelPdmSheetName;

    @Value(value="${outputExcelConfig.physicalModelFileName}")
    private String outputExcelPhysicalModelFileName;

    @Value(value="${outputExcelConfig.physicalModelSheetName}")
    private String outputExcelPhysicalModelSheetName;

    @Value(value="${outputExcelConfig.physicalModelSheetName_table}")
    private String outputExcelPhysicalModelSheetNameTable;

    @Value(value="${outputExcelConfig.physicalModelChangeRecordFileName}")
    private String outputExcelPhysicalModelChangeRecordFileName;

    private String OtherSheetName;  //判断是否是pdm的第二个sheet  true/false
/*    public File templateFile;
    public File destFile;*/

}
