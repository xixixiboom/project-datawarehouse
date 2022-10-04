package com.datawarehouse.excelgenerate.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * @ClassName JobStreamRelation
 * @Description TODO
 * @Author xjy
 * @Date 2022/8/10 10:17
 * @Version 1.0
 **/

@Data
public class JobStreamRelation {
    @ExcelProperty(value="作业流后置")
    private String jobStreamPostposition;

    @ExcelProperty(value="作业流前置")
    private String jobStreamPreposition;

}
