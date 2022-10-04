package com.datawarehouse.excelgenerate.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.HeadFontStyle;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import com.alibaba.excel.enums.BooleanEnum;
import lombok.Data;

@Data
//@HeadStyle(hidden=BooleanEnum.TRUE)
@HeadFontStyle(bold = BooleanEnum.TRUE,color =100 )
@HeadRowHeight(30)
public class ChangeRecord {
    @ExcelProperty(value="变更编号")
    private String changeSerialNumber;

    @ExcelProperty(value="变更日期")
    private String changeDate;

    @ExcelProperty(value="变更人")
    private String changePerson;

    @ExcelProperty(value="变更分类")
    private String changeClassify;

    @ExcelProperty(value="变更级别")
    private String changeLevel;

    @ExcelProperty(value="目标表英文名")
    private String targetTableNameEn;

    @ExcelProperty(value="目标表中文名")
    private String targetTableNameCn;

    @ExcelProperty(value="变更版本号")
    private String changeVersionNumber;

    @ExcelProperty(value="PDM变更描述")
    private String pdmChangeDescription;

    @ExcelProperty(value="问题单号")
    private String questionNumber;

    @ExcelProperty(value="变更类型")
    private String changeType;

    @ExcelProperty(value="需求类型")
    private String requirementType;

    @ExcelProperty(value="所属组")
    private String subordinateGroup;

    @ExcelProperty(value="所属主题")
    private String subordinateTheme;

}
