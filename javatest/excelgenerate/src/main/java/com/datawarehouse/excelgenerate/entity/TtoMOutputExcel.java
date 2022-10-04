package com.datawarehouse.excelgenerate.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * @ClassName TtoMOutputExcel
 * @Description TODO
 * @Author xjy
 * @Date 2022/8/19 17:17
 * @Version 1.0
 **/

@Data
public class TtoMOutputExcel {
    @ExcelProperty(value="源系统名")
    private String sourceSystemName;

    @ExcelProperty(value="系统标识")
    private String systemFlag;

    @ExcelProperty(value="拼接表名")
    private String spliceTableName;

    @ExcelProperty(value="ods表名")
    private String odsTableName;

    @ExcelProperty(value = "ods表中文名")
    private String odsTableNameCn;

    @ExcelProperty(value="是否入仓")
    private String isWarehousing;

    @ExcelProperty(value = "文件名")
    private String sourceFileNameCn;

    @ExcelProperty(value = "M层英文名")
    private String mTableNameEn;

    @ExcelProperty(value = "M层中文名")
    private String mTableNameCn;

    @ExcelProperty(value = "唯一输入表名")
    private String uniqueInputTableName;
}
