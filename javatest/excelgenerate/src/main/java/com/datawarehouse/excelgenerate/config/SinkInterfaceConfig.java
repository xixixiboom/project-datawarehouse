package com.datawarehouse.excelgenerate.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @ClassName SinkInterfaceConfig
 * @Description TODO
 * @Author xjy
 * @Date 2022/10/10 21:41
 * @Version 1.0
 **/
@Configuration
@ConfigurationProperties("sinkinterface")
@Data
public class SinkInterfaceConfig {
    private String cPdmFileName;
    private String cPdmSheetChangeRecord;
    private String cPdmSheetFieldInfo;
    private String cPdmChangeTimeUpperLimit;
    private String cPdmChangeTimeLowerLimit;
    private String outputFileName;

}
