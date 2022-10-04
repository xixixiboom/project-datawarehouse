package com.datawarehouse.excelgenerate.entity;


import com.alibaba.excel.annotation.ExcelProperty;
import com.datawarehouse.excelgenerate.config.InputExcelConfig;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;


@Data
public class TableLevelAnalysize {
    @Autowired
    InputExcelConfig inputExcelConfig;

    @ExcelProperty(value="${inputExcelConfig.filterColumn}")
    private String filterColumn;
}
