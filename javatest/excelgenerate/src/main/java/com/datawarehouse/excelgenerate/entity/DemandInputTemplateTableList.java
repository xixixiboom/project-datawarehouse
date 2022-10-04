package com.datawarehouse.excelgenerate.entity;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * @ClassName DemandInputTemplateTableList
 * @Description TODO
 * @Author xjy
 * @Date 2022/9/15 15:57
 * @Version 1.0
 **/
@Data
public class DemandInputTemplateTableList {
    @ExcelProperty(index=0)
    private String systemName;
    @ExcelProperty(index=1)
    private String systemID;
    @ExcelProperty(index=2)
    private String tableNameEn;
    @ExcelProperty(index=3)
    private String tableNameCn;
    @ExcelProperty(index=4)
    private String isBottom;
    @ExcelProperty(index=5)
    private String bottomTime;
    @ExcelProperty(index=6)
    private String dataTimeliness;
}
