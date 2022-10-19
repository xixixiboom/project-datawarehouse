package com.datawarehouse.excelgenerate.entity.reduceDiameter;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * @ClassName StandradDataExcel
 * @Description TODO
 * @Author xjy
 * @Date 2022/10/8 17:21
 * @Version 1.0
 **/

@Data
public class StandardDataExcel {
    @ExcelProperty("sys_id")
    public String sysId;

    @ExcelProperty("源系统名")
    public String srcSystemName;

    @ExcelProperty("源表名")
    public String srcTableName;

    @ExcelProperty("源字段名")
    public String srcFieldName;

    @ExcelProperty("字典反馈结果")
    public String dicFeedbackResult;

    @ExcelProperty("字典ID")
    public String dicId;

    @ExcelProperty("字典名称")
    public String dicNameCn;

    @ExcelProperty("英文简称")
    public String dicNameEn;

    @ExcelProperty("数据项说明")
    public String dataDescription;

    @ExcelProperty("数据类型")
    public String dataType;

    @ExcelProperty("引用代码编号")
    public String citeCodeNumber;

    @ExcelProperty("代码集名称")
    public String codeSetName;


}
