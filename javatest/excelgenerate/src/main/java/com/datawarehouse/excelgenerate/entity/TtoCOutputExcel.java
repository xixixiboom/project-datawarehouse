package com.datawarehouse.excelgenerate.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * @ClassName TtoCOutputExcel
 * @Description TODO
 * @Author xjy
 * @Date 2022/8/23 15:57
 * @Version 1.0
 **/
@Data
public class TtoCOutputExcel {
    @ExcelProperty(value = "ods表英文名")
    private String odsTableNameEn;
    @ExcelProperty(value = "ods表中文名")
    private String odsTableNameCn;
    @ExcelProperty(value = "M层表英文名")
    private String mTableNameEn;
    @ExcelProperty(value = "M层表中文名")
    private String mTableNameCn;
    @ExcelProperty(value = "C层表英文名")
    private String cTableNameEn;
    @ExcelProperty(value = "C层表中文名")
    private String cTableNameCn;
    @ExcelProperty(value = "M层字段英文名")
    private String mFieldNameEn;
    @ExcelProperty(value = "M层字段中文名")
    private String mFieldNameCn;
    @ExcelProperty(value = "C层字段英文名")
    private String cFieldNameEn;
    @ExcelProperty(value = "C层字段中文名")
    private String cFieldNameCn;
}
