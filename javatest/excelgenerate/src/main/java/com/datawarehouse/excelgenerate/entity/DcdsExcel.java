package com.datawarehouse.excelgenerate.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class DcdsExcel {
    @ExcelProperty(value = "数据表英文名")
    private String tableNameEn;

    @ExcelProperty(value = "数据表中文名")
    private String tableNameCn;

    @ExcelProperty(value = "数据项英文名")
    private String fieldNameEn;

    @ExcelProperty(value = "数据项中文名")
    private String fieldNameCn;

    @ExcelProperty(value = "数据类型")
    private String dataType;

    @ExcelProperty(value = "数据最大长度")
    private String dataMaxLength;

    @ExcelProperty(value="小数位长度")
    private String decimalLength;

    @ExcelProperty(value = "是否主键")
    private String isPrimaryKey;

    @ExcelProperty(value = "字段含义及取值范围")
    private String fieldMeaning;
}
