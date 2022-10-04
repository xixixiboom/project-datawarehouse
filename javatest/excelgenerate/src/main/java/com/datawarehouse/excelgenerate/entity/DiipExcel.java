
package com.datawarehouse.excelgenerate.entity;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class DiipExcel {
    @ExcelProperty(value = "组件中文名称")
    private String componentNameCn;

    @ExcelProperty(value = "组件英文名称")
    private String componentNameEn;

    @ExcelProperty(value = "组件编号")
    private String componentNumber;

    @ExcelProperty(value = "变更状态")
    private String variationalStatus;

    @ExcelProperty(value = "变更内容")
    private String variationalContent;

    @ExcelProperty(value = "数据表中文名")
    private String TableNameCn;

    @ExcelProperty(value = "数据表英文名")
    private String TableNameEn;

    @ExcelProperty(value = "表内字段序号")
    private String fieldSerialNumber;

    @ExcelProperty(value = "数据项英文名")
    private String fieldNameEn;

    @ExcelProperty(value = "数据项中文名")
    private String fieldNameCn;

    @ExcelProperty(value = "数据类型")
    private String dataType;

    @ExcelProperty(value = "数据长度释义")
    private String dataLengthMeaning;

    @ExcelProperty(value = "数据最大长度")
    private String dataMaxLength;

    @ExcelProperty(value = "小数位长度")
    private String decimalLength;

    @ExcelProperty(value = "数据格式说明")
    private String dataFormatDescription;

    @ExcelProperty(value = "是否可为空")
    private String isPermitNull;

    @ExcelProperty(value = "是否主键")
    private String isPrimaryKey;

    @ExcelProperty(value = "字段含义及取值范围")
    private String fieldMeaning;

    @ExcelProperty(value = "共享类型")
    private String shareType;

    @ExcelProperty(value = "生效日期")
    private String effictiveDate;

    @ExcelProperty(value = "投产批次")
    private String productionBatch;

    @ExcelProperty(value = "任务单号")
    private String taskNumber;

    @ExcelProperty(value = "最后更新日期")
    private String lastUpdateDate;

    @ExcelProperty(value = "备注")
    private String remark;

    @ExcelProperty(value = "版本编号")
    private String versionNumber;
}
