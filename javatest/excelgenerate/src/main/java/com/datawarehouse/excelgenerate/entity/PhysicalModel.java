package com.datawarehouse.excelgenerate.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.HeadFontStyle;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import com.alibaba.excel.annotation.write.style.HeadStyle;
import com.alibaba.excel.enums.BooleanEnum;
import lombok.Data;
import org.apache.poi.ss.usermodel.IndexedColors;

@Data
//@HeadStyle(hidden=BooleanEnum.TRUE)
@HeadFontStyle(bold = BooleanEnum.TRUE,color =100 )
@HeadRowHeight(30)
public class PhysicalModel {
    @ExcelProperty(value = "序号")
//    @ExcelProperty(index=5)
    private String seiralNumber;

    @ExcelProperty(value = "系统模块")
//    @ExcelProperty(index=5)
    private String systemModule;

    @ExcelProperty(value = "主题")
//    @ExcelProperty(index=23)
    private String itsTheme;

    @ExcelProperty("表英文名")
//    @ExcelProperty(index=0)
    private String targetTableNameEn;

    @ExcelProperty("字段英文名")
//    @ExcelProperty(index=1)
    private String targetFieldNameEn;

    @ExcelProperty("表中文名")
//    @ExcelProperty(index=2)
    private String targetTableNameCn;

    @ExcelProperty(value = "字段中文名")
//    @ExcelProperty(index=3)
    private String targetFieldNameCn;

    @ExcelProperty(value = "主键")
//    @ExcelProperty(index=4)
    private String primaryKey;

    @ExcelProperty(value = "字段序号")
//    @ExcelProperty(index=5)
    private String fieldSeiralNumber;

    @ExcelProperty(value = "字段类型")
//    @ExcelProperty(index=6)
    private String targetFieldDataType;

    @ExcelProperty(value = "是否分布键")
//    @ExcelProperty(index=6)
    private String isDistributionKey;

    @ExcelProperty(value = "是否自增序列")
//    @ExcelProperty(index=13)
    private String isIdentityProperty;

    @ExcelProperty(value = "建表类型")
//    @ExcelProperty(index=14)
    private String createTableType;

    @ExcelProperty(value = "是否代码字段")
//    @ExcelProperty( index = 16)
    private String isCodeField;

    @ExcelProperty(value = "引用代码表")
//    @ExcelProperty( index = 17)
    private String referenceCodeTable;

    @ExcelProperty(value="问题/备注")
//    @ExcelProperty(index=18)
    private String compareLastVersion2;

    @ExcelProperty(value = "更新时间")
//    @ExcelProperty(index=19)
    private String updateTime;

    @ExcelProperty(value = "上线时间")
//    @ExcelProperty(index=19)
    private String onLineTime;

    @ExcelProperty(value = "来源系统")
//    @ExcelProperty(index=19)
    private String sourceSystem;

    @ExcelProperty(value = "更改记录")
//    @ExcelProperty(index=19)
    private String changeRecord;

    @ExcelProperty(value = "异常标识")
//    @ExcelProperty(index=19)
    private String exceptionSign;

    @ExcelProperty(value = "异常类型")
//    @ExcelProperty(index=19)
    private String exceptionType;

    @ExcelProperty(value = "变更描述")
//    @ExcelProperty(index=19)
    private String changeDescription;

}

