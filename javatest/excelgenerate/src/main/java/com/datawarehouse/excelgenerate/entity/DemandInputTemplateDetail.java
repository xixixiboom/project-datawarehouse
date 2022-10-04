package com.datawarehouse.excelgenerate.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * @ClassName DemandInputTemplateDetail
 * @Description TODO
 * @Author xjy
 * @Date 2022/9/14 14:12
 * @Version 1.0
 **/
@Data
public class DemandInputTemplateDetail {
    @ExcelProperty(index=0)
    private String tableNameCn;

    @ExcelProperty(index=1)
    private String tableNameEn;

    @ExcelProperty(index=2)
    private String fieldNameCn;

    @ExcelProperty(index=3)
    private String fieldNameEn;

    @ExcelProperty(index=4)
    private String fieldType;

    @ExcelProperty(index=5)
    private String primaryKey;

    @ExcelProperty(index=6)
    private String distributionKey;

    @ExcelProperty(index=7)
    private String partitionField;

    @ExcelProperty(index=8)
    private String srcSystem;

    @ExcelProperty(index=9)
    private String srcSystemFlag;

    @ExcelProperty(index=10)
    private String srcTableNameEn;

    @ExcelProperty(index=11)
    private String srcTableNameCn;

    @ExcelProperty(index=12)
    private String srcFieldNameEn;

    @ExcelProperty(index=13)
    private String srcFieldNameCn;

    @ExcelProperty(index=14)
    private String srcFiledType;

    @ExcelProperty(index=15)
    private String tableStructureSource;

    @ExcelProperty(index=16)
    private String changeRecord;

    @ExcelProperty(index=17)
    private String dicID;

    @ExcelProperty(index=18)
    private String dicNameCn;

    @ExcelProperty(index=19)
    private String dicNameEn;

    @ExcelProperty(index=20)
    private String comment;

    @ExcelProperty(index=21)
    private String dataCategory;

    @ExcelProperty(index=22)
    private String dataType;

    @ExcelProperty(index=23)
    private String joinWay;

    @ExcelProperty(index=24)
    private String joinCondition;

    @ExcelProperty(index=25)
    private String filterCondition;

    @ExcelProperty(index=26)
    private String getDataLogic;

    @ExcelProperty(index=27)
    private String codeValue;

    @ExcelProperty(index=28)
    private String remark;

}
