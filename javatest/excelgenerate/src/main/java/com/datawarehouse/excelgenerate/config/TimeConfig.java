package com.datawarehouse.excelgenerate.config;

import lombok.Data;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class TimeConfig {

    public long start;
    public long end;

}
