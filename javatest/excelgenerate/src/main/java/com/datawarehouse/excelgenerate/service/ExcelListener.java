package com.datawarehouse.excelgenerate.service;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.datawarehouse.excelgenerate.config.InputExcelConfig;
import com.datawarehouse.excelgenerate.config.OutputExcelConfig;
import com.datawarehouse.excelgenerate.config.TimeConfig;
import com.datawarehouse.excelgenerate.entity.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


/*    非常坑爹之listener，无法自动注入spring bean  需要使用无参构造或者有参构造的方式注入，特别注意，困扰一下午。
    https://www.yuque.com/easyexcel/doc/read  监听器部分*/
@Service

public class ExcelListener<T> extends AnalysisEventListener<T> {
    public InputExcelConfig inputExcelConfig;
    public OutputExcelConfig outputExcelConfig;
    public WriteExcel writeExcel;
    public long start;
    public long end;

    //构造方法传参，必须使用此方法listener   好像反射也可以，inovke
    public ExcelListener(InputExcelConfig inputExcelConfig, OutputExcelConfig outputExcelConfig, TimeConfig timeConfig) {
        this.inputExcelConfig = inputExcelConfig;
        this.outputExcelConfig = outputExcelConfig;
//        writeExcel = new WriteExcel(outputExcelConfig);
        writeExcel = new WriteExcel();

        this.start = timeConfig.getStart();

//        this.sdmExcel=sdmExcel;
    }

    private List<T> dataInputList = new ArrayList<>();

    @Override
    public void invoke(T t, AnalysisContext analysisContext) {
        dataInputList.add(t);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        filter();
    }

/*    //根据T类型判断，执行生成sdm、pdm、filter Excel操作
    public void judge() {
        DcdsExcel dcdsExcel=new DcdsExcel();
        DiipExcel diipExcel=new DiipExcel();
        FieldAnalyseExcel fieldAnalyseExcel=new FieldAnalyseExcel();
        List<T> filter = filter();
        List<SdmExcel> listSdm =new ArrayList<>();
        List<PdmExcel> listPdm = new ArrayList<>();
        if(filter.get(0).getClass().getName().equals(dcdsExcel.getClass().getName())){
            listSdm = convertDcdsToSdm(filter);
            generateExcel(listSdm);

        }else if (filter.get(0).getClass().equals(diipExcel.getClass())){

        }
    }*/

    //写入文件
    public <U> void generateExcel(String fileName, String sheetName, List<U> listU) {
         /*        writeSdmExcel.dowriteSdmExcel(targetExcelConfig.getoutputExcelFileName(),
            targetExcelConfig.getoutputExcelSheetName(), sdmExcels);*/
        writeExcel.addWrite(fileName, sheetName, listU);
        String generateExcel = listU.get(0).getClass().getSimpleName();
        System.out.println();
        System.out.println("============================生成 " +"gen_"+generateExcel + " 完成============================");
        System.out.println("写入" + generateExcel + " 文件名为： " + "gen_"+fileName);
        System.out.println("写入 " + generateExcel + " SheetName为： " + sheetName);
        System.out.println("=========================共写入 " + listU.size() + " 条数据========================");
        end = System.currentTimeMillis();
        System.out.println("============================共耗时 " + (end - start) + " ms===========================");
        start = end;
    }

    //接收List<>，根据tableNameEn和tableNameCn筛选，返回List<>
    public void filter() {

        int dataListSize = dataInputList.size();
        //根据原表中文名和原表英文名匹配目标表名存入targetList
 /*       尝试使用泛型，写通用方法，但是getTableNameEn为类的独有方法，无法通过泛型去调用，涉及到泛型擦除，用反射invoke也不行，获取不了
        泛型T t的对象，只能采用笨方法，去判断类属于哪个类来执行各自的方法，留待后续基础扎实再去做*/
        System.out.println("=================读取文件属于 " + dataInputList.get(0).getClass().getSimpleName() + "================");
        if (dataInputList.get(0).getClass().getSimpleName().equals("DcdsExcel")) {
            List<DcdsExcel> targetList = new ArrayList<>();
            if (inputExcelConfig.getIsFiltrate().equals("false")) {
                targetList = (List<DcdsExcel>) dataInputList;
            } else {
                for (int i = 0; i < dataListSize; i++) {
                    DcdsExcel dcdsExcel = (DcdsExcel) dataInputList.get(i);
                    //注意： 调用.equals() 的对象不能为null，如果为null 放到后面
                    for (String tableNameEn : inputExcelConfig.tableNameEn) {
                        if (tableNameEn != null) {
                            if (tableNameEn.equals(dcdsExcel.getTableNameEn())) {
                                targetList.add(dcdsExcel);
                            }
                        }

                    }
                    for (String tableNameCn : inputExcelConfig.tableNameCn) {
                        if (tableNameCn != null) {
                            if (tableNameCn.equals(dcdsExcel.getTableNameCn())) {
                                targetList.add(dcdsExcel);
                            }
                        }

                    }
                }
            }
            System.out.println("=======================抽取原DCDS Excel 信息完成=====================");
            System.out.println("=======================共抽取 " + targetList.size() + " 条数据=====================");
            List<SdmExcel> sdmExcels = convertDcdsToSdm(targetList);
            List<PdmExcel> pdmExcels = convertSdmToPdm(sdmExcels);
            List<PhysicalModel> physicalModels = convertPdmToPhysicalModel(pdmExcels);
            generateExcel(outputExcelConfig.getOutputExcelSdmFileName(), outputExcelConfig.getOutputExcelSdmSheetName(), sdmExcels);
            generateExcel(outputExcelConfig.getOutputExcelPdmFileName(), outputExcelConfig.getOutputExcelPdmSheetName(), pdmExcels);
            generateExcel(outputExcelConfig.getOutputExcelPhysicalModelFileName(), outputExcelConfig.getOutputExcelPhysicalModelSheetName(), physicalModels);
        }
        else if (dataInputList.get(0).getClass().getSimpleName().equals("SdmExcel")) {
            List<SdmExcel> targetList = new ArrayList<>();
            if (inputExcelConfig.getIsFiltrate().equals("false")) {
                targetList = (List<SdmExcel>) dataInputList;
            } else {
                for (int i = 0; i < dataListSize; i++) {
                    SdmExcel sdmExcel = (SdmExcel) dataInputList.get(i);

                    for (String tableNameEn : inputExcelConfig.tableNameEn) {
                        if (tableNameEn != null) {
                            if (tableNameEn.equals(sdmExcel.getOriginalTableNameEn())) {
                                targetList.add(sdmExcel);
                            }
                        }
                    }

                    for (String tableNameCn : inputExcelConfig.tableNameCn) {
                        if (tableNameCn != null) {
                            if (tableNameCn.equals(sdmExcel.getOriginalTableNameCn())) {
                                targetList.add(sdmExcel);
                            }
                        }
                    }
                }
            }
            System.out.println("=======================抽取原Sdm Excel 信息完成=====================");
            System.out.println("=======================共抽取 " + targetList.size() + " 条数据=====================");
            List<SdmExcel> sdmExcels = targetList;
            List<PdmExcel> pdmExcels = convertSdmToPdm(sdmExcels);
            List<PhysicalModel> physicalModels = convertPdmToPhysicalModel(pdmExcels);

            generateExcel(outputExcelConfig.getOutputExcelSdmFileName(), outputExcelConfig.getOutputExcelSdmSheetName(), sdmExcels);
            generateExcel(outputExcelConfig.getOutputExcelPdmFileName(),outputExcelConfig.getOutputExcelPdmSheetName(),pdmExcels);
            generateExcel(outputExcelConfig.getOutputExcelPhysicalModelFileName(), outputExcelConfig.getOutputExcelPhysicalModelSheetName(), physicalModels);

        }
        else if (dataInputList.get(0).getClass().getSimpleName().equals("PdmExcel")) {
            List<PdmExcel> targetList = new ArrayList<>();
            if (inputExcelConfig.getIsFiltrate().equals("false")) {
                targetList = (List<PdmExcel>) dataInputList;
            } else {
                for (int i = 0; i < dataListSize; i++) {
                    PdmExcel pdmExcel = (PdmExcel) dataInputList.get(i);
                    for (String tableNameEn : inputExcelConfig.tableNameEn) {
                        if (tableNameEn != null) {
                            if (tableNameEn.equals(pdmExcel.getTargetTableNameEn())) {
                                targetList.add(pdmExcel);
                            }
                        }
                    }

                    for (String tableNameCn : inputExcelConfig.tableNameCn) {
                        if (tableNameCn != null) {
                            if (tableNameCn.equals(pdmExcel.getTargetTableNameCn())) {
                                targetList.add(pdmExcel);
                            }
                        }
                    }
                }
            }
            System.out.println("=======================抽取原Pdm Excel 信息完成=====================");
            System.out.println("=======================共抽取 " + targetList.size() + " 条数据=====================");
            List<PdmExcel> pdmExcels = targetList;
            List<PhysicalModel> physicalModels = convertPdmToPhysicalModel(pdmExcels);

            generateExcel(outputExcelConfig.getOutputExcelPdmFileName(),outputExcelConfig.getOutputExcelPdmSheetName(),pdmExcels);
            generateExcel(outputExcelConfig.getOutputExcelPhysicalModelFileName(), outputExcelConfig.getOutputExcelPhysicalModelSheetName(), physicalModels);

        }
        else if (dataInputList.get(0).getClass().getSimpleName().equals("DiipExcel")) {
            List<DiipExcel> targetList = new ArrayList<>();
            for (int i = 0; i < dataListSize; i++) {
                DiipExcel diipExcel = (DiipExcel) dataInputList.get(i);
                for (String tableNameEn : inputExcelConfig.tableNameEn) {

                    if (diipExcel.getTableNameEn().equals(tableNameEn)) {
                        targetList.add(diipExcel);
                    }
                }
                for (String tableNameCn : inputExcelConfig.tableNameCn) {

                    if (diipExcel.getTableNameCn().equals(tableNameCn)) {
                        targetList.add(diipExcel);
                    }
                }
            }
            System.out.println("=======================抽取原Diip Excel 信息完成=====================");
            System.out.println("=======================共抽取" + targetList.size() + " 条数据=====================");

        }
        else if (dataInputList.get(0).getClass().getSimpleName().equals("FieldAnalyseExcel")) {
            List<FieldAnalyseExcel> targetList = new ArrayList<>();
            for (int i = 0; i < dataListSize; i++) {
                FieldAnalyseExcel fieldAnalyseExcel = (FieldAnalyseExcel) dataInputList.get(i);
                for (String tableNameEn : inputExcelConfig.tableNameEn) {

                    if (fieldAnalyseExcel.getTableNameEn().equals(tableNameEn)) {
                        targetList.add(fieldAnalyseExcel);
                    }
                }
                for (String tableNameCn : inputExcelConfig.tableNameCn) {

                    if (fieldAnalyseExcel.getTableNameCn().equals(tableNameCn)) {
                        targetList.add(fieldAnalyseExcel);
                    }
                }
            }
            System.out.println("=======================抽取原字段级分析 Excel 信息完成=====================");
            System.out.println("=======================共抽取" + targetList.size() + " 条数据=====================");
        }
        else if (dataInputList.get(0).getClass().getSimpleName().equals("ChangeRecord")) {
            List<ChangeRecord> targetList = new ArrayList<>();
            if (inputExcelConfig.getIsFiltrate().equals("false")) {
                targetList = (List<ChangeRecord>) dataInputList;
            } else {
                for (int i = 0; i < dataListSize; i++) {
                    ChangeRecord changeRecord = (ChangeRecord) dataInputList.get(i);
                    for (String tableNameEn : inputExcelConfig.tableNameEn) {
                        if (tableNameEn != null) {
                            if (tableNameEn.equals(changeRecord.getTargetTableNameEn())) {
                                targetList.add(changeRecord);
                            }
                        }
                    }

                    for (String tableNameCn : inputExcelConfig.tableNameCn) {
                        if (tableNameCn != null) {
                            if (tableNameCn.equals(changeRecord.getTargetTableNameCn())) {
                                targetList.add(changeRecord);
                            }
                        }
                    }
                }
            }
            System.out.println("=======================抽取原Pdm Excel 信息完成=====================");
            System.out.println("=======================共抽取 " + targetList.size() + " 条数据=====================");
            List<ChangeRecord> changeRecords = targetList;
            List<PhysicalModelTableInfo> physicalModelTableInfos = convertChangeRecordToPhysicalModelTableInfo(changeRecords);

            generateExcel(outputExcelConfig.getOutputExcelPhysicalModelChangeRecordFileName(),outputExcelConfig.getOutputExcelPhysicalModelSheetNameTable(),physicalModelTableInfos);
        }


    }

    //List<SdmExcel> 到 List<DcdsExcel>的转换
    public List<SdmExcel> convertDcdsToSdm(List<DcdsExcel> listDcdsExcel) {
        System.out.println("==============================开始 Dcds->Sdm 转换==============================");

        int excelSize = listDcdsExcel.size();

        List<SdmExcel> listT = new ArrayList<SdmExcel>(excelSize);

        for (int i = 0; i < excelSize; i++) {
            SdmExcel s = new SdmExcel();
            DcdsExcel d = listDcdsExcel.get(i);
            s.setGroupNumber(d.getTableNameEn() + "-1"); //组号
//            s.setTargetTableNameEn(d.getTableNameEn());  //目标表英文名没法确认
            s.setTargetFieldNameEn(d.getFieldNameEn());   //目标字段英文名
            s.setTargetTableWarehouseName("IML");   //目标字段英文名
            s.setTargetFieldNameCn(d.getFieldNameCn());   //目标字段中文名

            s.setPrimaryKey(d.getIsPrimaryKey());   //主键
            String mappingRuleStr = (d.getFieldNameCn().contains("代码") ? "【代码转换】" : "【直接映射】");
            s.setMappingRule(mappingRuleStr);      //概要映射规则注释
            s.setOriginalFieldNameEn(d.getFieldNameEn());   //主源字段英文名
            s.setOriginalFieldNameCn(d.getFieldNameCn());   //主源字段中文名
            //字段类型
            String originalDataType;
            if (d.getDecimalLength() != null) {
                originalDataType = d.getDataType().trim() + "(" + d.getDataMaxLength() + "," + d.getDecimalLength() + ")";     //主源字段数据类型
            } else {
                originalDataType = d.getDataType().trim() + "(" + d.getDataMaxLength() + ")";     //主源字段数据类型
            }
            s.setOriginalDataType(originalDataType);
            String targetDataType = originalDataType;
            if (targetDataType.contains("VARCHAR2")) {
                targetDataType = targetDataType.replace("VARCHAR2", "VARCHAR");
            }
            if (targetDataType.contains("NUMBER")) {
                targetDataType = targetDataType.replace("NUMBER", "DECIMAL");
            }
            if(targetDataType.contains("CHAR")){
                targetDataType = targetDataType.replace("CHAR", "VACHAR");
            }
            s.setTargetFieldDataType(targetDataType); //目标字段数据类型

            s.setOriginalTableWarehouseName("${itl_schema}");     //主源表库名
            s.setOriginalTableNameEn(d.getTableNameEn());      //主源表英文名
            s.setOriginalTableOtherName("T1");  //主源表别名
            s.setAssignmentRule(s.getOriginalTableOtherName() + "." + s.getOriginalFieldNameEn().toLowerCase());    //目标字段赋值规则

            s.setOriginalTableNameCn(d.getTableNameCn());   //主源表中文名
//            s.setAlgorithmIdentifies("SN_F;
            s.setVersionNumber("042-P2207-市场风险-1");     //变更版本号
            s.setChangeDescription("新增表");      //变更描述
            s.setCompareLastVersion("01-新增表映射");    //较上个版本
            s.setCompareProductionBaseline("01-新需求");   //较生产基线
            s.setCompareLastVersion2("01-新增表映射");    //较上个版本
            s.setCompareProductionBaseline2("01-新需求");   //较生产基线
            listT.add(s);
        }
        System.out.println("============================== Dcds->Sdm 转换完成==============================");
        return listT;
    }

    public List<PdmExcel> convertSdmToPdm(List<SdmExcel> listSdmExcel) {
        System.out.println("==============================开始 Sdm->Pdm 转换==============================");
        int excelSize = listSdmExcel.size();

        List<PdmExcel> listT = new ArrayList<>(excelSize);

        for (int i = 0; i < excelSize; i++) {

            PdmExcel p = new PdmExcel();
            SdmExcel s = listSdmExcel.get(i);
            p.setTargetTableNameEn(s.getTargetTableNameEn()); //英文名
            p.setTargetFieldNameEn(s.getTargetFieldNameEn());   //目标字段英文名
            p.setTargetTableNameCn(s.getTargetTableNameCn()); //中文名
            p.setTargetFieldNameCn(s.getTargetFieldNameCn());   //目标字段中文名
            p.setPrimaryKey(s.getPrimaryKey());   //主键
            p.setFieldSeiralNumber(s.getSerialNumber());    //序号
            p.setTargetFieldDataType(s.getTargetFieldDataType());      //数据类型
            p.setVersionNumber(s.getVersionNumber());//变更版本号
            p.setChangeDescription(s.getChangeDescription());      //变更描述
            p.setCompareLastVersion(s.getCompareLastVersion());    //较上个版本
            p.setCompareProductionBaseline(s.getCompareProductionBaseline());   //较生产基线
            p.setCompareLastVersion2(s.getCompareLastVersion2());    //较上个版本
            p.setCompareProductionBaseline2(s.getCompareProductionBaseline2());   //较生产基线
            listT.add(p);
        }
        System.out.println("============================== Sdm->Pdm 转换完成==============================");
        return listT;
    }

    public List<PhysicalModel> convertPdmToPhysicalModel(List<PdmExcel> listPdmExcel) {
        System.out.println("==============================开始 Pdm->物理模型 转换==============================");
        int excelSize = listPdmExcel.size();

        List<PhysicalModel> listT = new ArrayList<>(excelSize);

        for (int i = 0; i < excelSize; i++) {

            PhysicalModel ph = new PhysicalModel();
            PdmExcel p = listPdmExcel.get(i);

            ph.setSeiralNumber(Integer.toString(i + 1));  //序号
            ph.setSystemModule("IML");
            ph.setTargetTableNameEn(p.getTargetTableNameEn()); //表英文名
            ph.setTargetFieldNameEn(p.getTargetFieldNameEn());   //字段英文名
            ph.setTargetTableNameCn(p.getTargetTableNameCn()); //表中文名
            ph.setTargetFieldNameCn(p.getTargetFieldNameCn());   //字段中文名
            ph.setPrimaryKey(p.getPrimaryKey());   //主键
            ph.setFieldSeiralNumber(p.getFieldSeiralNumber());    //字段序号
            ph.setTargetFieldDataType(p.getTargetFieldDataType());      //字段类型
            if(p.getPrimaryKey()!=null){
                ph.setIsDistributionKey(p.getPrimaryKey().equals("Y") ? "1" : "");//是否分布键
            }
            ph.setCreateTableType("SN");
            listT.add(ph);
        }
        System.out.println("============================== Pdm->物理模型 转换完成==============================");
//        System.out.println(listT);
        return listT;
    }

    public List<PhysicalModelTableInfo> convertChangeRecordToPhysicalModelTableInfo(List<ChangeRecord> listChangeRecord) {
        System.out.println("==============================开始 Pdm变更记录->物理模型表级信息 转换==============================");
        int excelSize = listChangeRecord.size();

        List<PhysicalModelTableInfo> listT = new ArrayList<>(excelSize);

        for (int i = 0; i < excelSize; i++) {

            PhysicalModelTableInfo phm = new PhysicalModelTableInfo();
            ChangeRecord c = listChangeRecord.get(i);

            phm.setSerialNumber(Integer.toString(i + 1));  //序号
            phm.setSystemModule("IML");
            phm.setTableNameEn(c.getTargetTableNameEn()); //表英文名
            phm.setTableNameCn(c.getTargetTableNameCn());   //表中文名
            phm.setTableType("SN"); //表类型
            phm.setIsExistPrimaryKey("Y");   //是否存在主键
            listT.add(phm);
        }
        System.out.println("============================== Pdm变更记录->物理模型表级信息 转换完成==============================");
//        System.out.println(listT);
        return listT;
    }

}


