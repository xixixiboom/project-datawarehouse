package com.datawarehouse.excelgenerate.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * @ClassName WarehousingAnalysize
 * @Description TODO
 * @Author xjy
 * @Date 2022/8/9 15:30
 * @Version 1.0
 **/
@Data
public class WarehousingAnalysize {
    @ExcelProperty(value="源系统名")
    private String sourceSystemName;

    @ExcelProperty(value="系统标识")
    private String systemFlag;

    @ExcelProperty(value="拼接表名")
    private String spliceTableName;

    @ExcelProperty(value="ods表名")
    private String odsTableName;

    @ExcelProperty(value="是否入仓")
    private String isWarehousing;

    @ExcelProperty(value="表名")
    private String tableNameCn;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WarehousingAnalysize warehousingAnalysize = (WarehousingAnalysize) o;

        if (!spliceTableName.equals(warehousingAnalysize.spliceTableName)) return false;
//        return name.equals(person.name);
        return true;

    }

}
