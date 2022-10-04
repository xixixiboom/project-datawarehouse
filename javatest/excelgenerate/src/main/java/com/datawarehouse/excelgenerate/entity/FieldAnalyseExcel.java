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
public class FieldAnalyseExcel {
    //    @ExcelProperty("字段序号")
    @ExcelProperty(index = 0)
    private String serialNumber;

    //    @ExcelProperty("源系统标识名")
    @ExcelProperty(index = 1)
    private String originalSystemIdentification;


    //    @ExcelProperty("源系统表名")
    @ExcelProperty(index = 2)
    private String tableNameEn;

    //    @ExcelProperty("源表中文名")
    @ExcelProperty(index = 3)
    private String tableNameCn;

    //    @ExcelProperty("BOCS字段内部存储英文名")
    @ExcelProperty(index = 4)
    private String bocsInternalFieldNameEn;

    //    @ExcelProperty("源系统字段名")
    @ExcelProperty(index = 5)
    private String originalFieldNameEn;

    //    @ExcelProperty(value = "源字段中文名")
    @ExcelProperty(index = 6)
    private String targetFieldNameCn;

    //    @ExcelProperty("字段含义及取值范围")
    @ExcelProperty(index = 7)
    private String fieldMeaning;

    //    @ExcelProperty(value = "源系统数据类型")
    @ExcelProperty(index = 8)
    private String originalSystemDataType;

    //    @ExcelProperty(value = "允许NULL标志[N/空]")
    @ExcelProperty(index = 9)
    private String permitNullFlag;

    //    @ExcelProperty(value = "是否主键")
    @ExcelProperty(index = 10)
    private String isPrimaryKey;

    //    @ExcelProperty(value = "是否入整合模型层标志")
    @ExcelProperty(index = 11)
    private String isPutModelLayerFlag;

    //    @ExcelProperty(value = "整合模型层LDM表中文名")
    @ExcelProperty(index = 12)
    private String ldmTableNameCn;

    //    @ExcelProperty(value = "整合模型层LDM字段中文名")
    @ExcelProperty(index = 13)
    private String ldmFieldNameCn;

    //    @ExcelProperty(value = "整合模型层组识别规则")
    @ExcelProperty(index = 13)
    private String ldmRecognitionRule;

    //    @ExcelProperty(value = "调研结果")
    @ExcelProperty(index = 13)
    private String researchResult;

    //    @ExcelProperty(value = "映射类型")
    @ExcelProperty(index = 13)
    private String mappingType;

    //    @ExcelProperty(value = "映射规则描述")
    @ExcelProperty(index = 14)
    private String mappingRuleDescription;

}