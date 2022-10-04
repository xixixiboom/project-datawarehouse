package com.example.exceldemo.entity;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.util.Date;

@Data
public class DemoData {
    @ExcelProperty(index=10)
    private String string;
    @ExcelProperty("日期标题")
    private Date date;
/*    @ExcelProperty("数字标题")
    private Double doubleData;*/
    /**
     * 忽略这个字段
     */
    @ExcelIgnore
    private String ignore;
}