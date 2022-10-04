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
public class PhysicalModelTableInfo {
    @ExcelProperty(value="序号")
    private String serialNumber;

    @ExcelProperty(value="系统模块")
    private String systemModule;

    @ExcelProperty(value="主题")
    private String theme;

    @ExcelProperty(value="表英文名")
    private String tableNameEn;

    @ExcelProperty(value="表中文名")
    private String tableNameCn;

    @ExcelProperty(value="描述")
    private String description;

    @ExcelProperty(value="表类型")
    private String tableType;

    @ExcelProperty(value="算法类型")
    private String algorithmType;

    @ExcelProperty(value="是否存在主键")
    private String isExistPrimaryKey;

    @ExcelProperty(value="唯一索引")
    private String uniqueIndex;

    @ExcelProperty(value="非唯一索引")
    private String noUniqueIndex;

    @ExcelProperty(value="来源系统")
    private String sourceSystem;

    @ExcelProperty(value="异常标识")
    private String exceptionFlag;

    @ExcelProperty(value="异常类型")
    private String exceptionType;
}
