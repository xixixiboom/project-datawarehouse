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
public class PdmExcel {
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

    @ExcelProperty(value = "是否理论主键标志[Y/空]")
//    @ExcelProperty(index=4)
    private String primaryKey;

    @ExcelProperty(value = "字段序号")
//    @ExcelProperty(index=5)
    private String fieldSeiralNumber;

    @ExcelProperty(value = "数据类型")
//    @ExcelProperty(index=6)
    private String targetFieldDataType;

    @ExcelProperty(value = "变更版本号")
//    @ExcelProperty(index=13)
    private String versionNumber;

    @ExcelProperty(value = "变更描述")
//    @ExcelProperty(index=14)
    private String changeDescription;

    @ExcelProperty(value = "较上个版本",index=16)
//    @ExcelProperty( index = 16)
    private String compareLastVersion;

    @ExcelProperty(value = "较生产基线", index=17)
//    @ExcelProperty( index = 17)
    private String compareProductionBaseline;

    @ExcelProperty(value = "较上个版本",index=18)
//    @ExcelProperty(index=18)
    private String compareLastVersion2;

    @ExcelProperty(value = "较生产基线",index=19)
//    @ExcelProperty(index=19)
    private String compareProductionBaseline2;

    @ExcelProperty(value = "所属主题")
//    @ExcelProperty(index=23)
    private String itsTheme;
}

