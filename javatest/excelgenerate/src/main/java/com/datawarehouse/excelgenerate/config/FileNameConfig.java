package com.datawarehouse.excelgenerate.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
@Data
public class FileNameConfig {
/*    @Autowired
    OutputExcelConfig outputExcelConfig;*/
    public File templateFile;
    public File destFile;
}
