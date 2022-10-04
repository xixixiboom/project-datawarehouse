package com.datawarehouse.excelgenerate.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * @ClassName DataLakeTable
 * @Description TODO
 * @Author xjy
 * @Date 2022/9/15 20:30
 * @Version 1.0
 **/
@Data
public class DataLakeTable {
    @ExcelProperty("拼接表名")
    private String spliceTableName;
    @ExcelProperty("数据湖表英文名")
    private String lakeTableNameEn;
    @ExcelProperty("数据湖snapshot英文名")
    private String lakeSnapshotTableNameEn;
    @ExcelProperty("数据湖表中文名")
    private String lakeTableNameCn;
    @ExcelProperty("时间范围")
    private String timeRange;
    @ExcelProperty("地区范围")
    private String areaRange;
    @ExcelProperty("登记时间")
    private String registerTime;
    @ExcelProperty("登记人")
    private String registrant;
    @ExcelProperty("需求号")
    private String demandNumber;
    @ExcelProperty("任务名称")
    private String taskName;
    @ExcelProperty("测试数据情况")
    private String testDataSituation;
    @ExcelProperty("备注")
    private String remark;
}
