package com.datawarehouse.excelgenerate.entity.reduceDiameter;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.HeadFontStyle;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import com.alibaba.excel.enums.BooleanEnum;
import lombok.Data;

/**
 * @ClassName InputAndSdmField
 * @Description TODO
 * @Author xjy
 * @Date 2022/9/20 22:12
 * @Version 1.0
 **/
@Data
@HeadFontStyle(bold = BooleanEnum.TRUE,color =30 )
@HeadRowHeight(30)
public class InputAndSdmField {
    @ExcelProperty("源系统")
    private String srcSystem;

    @ExcelProperty("源系统标识")
    private String srcSystemFlag;

    @ExcelProperty("源表英文名")
    private String srcTableNameEn;

    @ExcelProperty("源表中文名")
    private String srcTableNameCn;

    @ExcelProperty("源字段英文名")
    private String srcFieldNameEn;

    @ExcelProperty("源字段中文名")
    private String srcFieldNameCn;

    @ExcelProperty("字段类型")
    private String srcFiledType;

    @ExcelProperty("表结构来源")
    private String tableStructureSource;
    @ExcelProperty("变更记录")
    private String changeRecord;

    @ExcelProperty("组号")
//    @ExcelProperty(index=0)
    private String groupNumber;

    @ExcelProperty("目标表英文名")
//    @ExcelProperty(index=1)
    private String targetTableNameEn;

    @ExcelProperty("目标字段英文名")
//    @ExcelProperty(index=2)
    private String targetFieldNameEn;

    //20230330update 增加三个字段
    @ExcelProperty("字典ID")
//    @ExcelProperty(index=2)
    private String dicID;

    @ExcelProperty("落标字段英文名")
//    @ExcelProperty(index=2)
    private String standardFieldNameEn;

    @ExcelProperty("序号")
//    @ExcelProperty(index=3)
    private String serialNumber;
    @ExcelProperty("数据来源表编号")
//    @ExcelProperty(index=4)
    private String dataSourceTableSerialNumber;

    @ExcelProperty(value = "目标表库名")
//    @ExcelProperty(index=5)
    private String targetTableWarehouseName;

    @ExcelProperty(value = "目标表中文名")
//    @ExcelProperty(index=6)
    private String targetTableNameCn;

    @ExcelProperty(value = "目标字段中文名")
//    @ExcelProperty(index=7)
    private String targetFieldNameCn;

    @ExcelProperty(value = "落标字段中文名")
//    @ExcelProperty(index=7)
    private String standardFieldNameCn;


    @ExcelProperty(value = "目标字段数据类型")
//    @ExcelProperty(index=8)
    private String targetFieldDataType;

    @ExcelProperty(value = "主键")
//    @ExcelProperty(index=9)
    private String primaryKey;

    @ExcelProperty(value = "ETL任务名")
//    @ExcelProperty(index=10)
    private String etlTaskName;

    @ColumnWidth(25)
    @ExcelProperty(value = "目标字段赋值规则\n" +
            "(源字段直接加载时可不填)")
//    @ExcelProperty(index=11)
    private String assignmentRule;

    @ColumnWidth(25)
    @ExcelProperty(value = "概要映射规则注释")
//    @ExcelProperty(index=12)
    private String mappingRule;

    @ExcelProperty(value = "主源字段英文名")
//    @ExcelProperty(index=13)
    private String originalFieldNameEn;

    @ExcelProperty(value = "主源字段中文名")
//    @ExcelProperty(index=14)
    private String originalFieldNameCn;

    @ExcelProperty(value = "主源字段数据类型")
//    @ExcelProperty(index=15)
    private String originalDataType;

    @ExcelProperty(value = "UI")
//    @ExcelProperty(index=16)
    private String uI;

    @ExcelProperty(value = "主源表库名")
//    @ExcelProperty(index=17)
    private String originalTableWarehouseName;

    @ExcelProperty(value = "主源表英文名")
//    @ExcelProperty(index=18)
    private String originalTableNameEn;

    @ExcelProperty(value = "主源表别名")
//    @ExcelProperty(index=19)
    private String originalTableOtherName;

    @ExcelProperty(value = "主源表中文名")
//    @ExcelProperty(index=20)
    private String originalTableNameCn;

    @ExcelProperty(value = "JOIN方式")
//    @ExcelProperty(index=21)
    private String joinType;

    @ExcelProperty(value = "次源表库名")
//    @ExcelProperty(index=22)
    private String otherSourceTableDatabaseName;

    @ExcelProperty(value = "次源表英文名")
//    @ExcelProperty(index=23)
    private String otherSourceTableNameEn;

    @ExcelProperty(value = "次源表别名")
//    @ExcelProperty(index=24)
    private String otherSourceTableOtherName;

    @ExcelProperty(value = "次源表中文名")
//    @ExcelProperty(index=25)
    private String otherSourceTableNameCn;

    @ExcelProperty(value = "JOIN条件")
//    @ExcelProperty(index=26)
    private String joinCondition;

    @ExcelProperty(value = "是否用全量数据加载[Y/空]")
//    @ExcelProperty(index=27)
    private String isAllDataLoad;

    @ExcelProperty(value = "WHERE条件")
//    @ExcelProperty(index=28)
    private String whereCondition;

    @ExcelProperty(value = "GROUP BY列表")
//    @ExcelProperty(index=29)
    private String groupByList;

    @ExcelProperty(value = "T13变种算法中的删除加工")
//    @ExcelProperty(index=30)
    private String t13VariantAlgorithm;

    @ExcelProperty(value = "组级自定义SQL")
//    @ExcelProperty(index=31)
    private String customSqlGroupLevel;

    @ExcelProperty(value = "是否手工添加组标志")
//    @ExcelProperty(index=32)
    private String isManuallyAdd;

    @ExcelProperty(value = "算法标识")
//    @ExcelProperty(index=33)
    private String algorithmIdentifies;

 /*       @ExcelProperty(value = "修改关注标志")
//    @ExcelProperty(index=34)
    private String modifyConcernFlag;

        @ExcelProperty(value = "是否完成落标")
//    @ExcelProperty(index=35)
    private String isFallMark;

        @ExcelProperty(value = "备注")
//    @ExcelProperty(index=36)
    private String remark;

    @ExcelProperty(value = "变更版本号")
//    @ExcelProperty(index=37)
    private String versionNumber;

    @ExcelProperty(value = "变更描述")
//    @ExcelProperty(index=38)
    private String changeDescription;

    @ExcelProperty(value = "较上个版本", index = 42)
//    @ExcelProperty( index = 40)
    private String compareLastVersion;

    @ExcelProperty(value = "较生产基线", index = 43)
//    @ExcelProperty( index = 41)
    private String compareProductionBaseline;

    @ExcelProperty(value = "较上个版本")
//    @ExcelProperty(index=42)
    private String compareLastVersion2;

    @ExcelProperty(value = "较生产基线")
//    @ExcelProperty(index=43)
    private String compareProductionBaseline2;*/

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
