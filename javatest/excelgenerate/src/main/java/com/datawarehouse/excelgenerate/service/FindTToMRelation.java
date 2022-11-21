package com.datawarehouse.excelgenerate.service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.datawarehouse.excelgenerate.config.FindTToBRelationConfig;
import com.datawarehouse.excelgenerate.entity.SdmExcelOffical;
import com.datawarehouse.excelgenerate.entity.TtoMOutputExcel;
import com.datawarehouse.excelgenerate.entity.WarehousingAnalysize;
import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import org.apache.poi.ss.formula.functions.T;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @ClassName FindTToBRelation
 * @Description 通过t表excel和sdm 找到对应的m表名和sdm,主要是判断是否入仓，将入仓的ods表名及表中文名输出
 * @Author xjy
 * @Date 2022/8/16 16:45
 * @Version 1.0
 **/
@Service
public class FindTToMRelation {
    @Autowired
    FindTableRelation findTableRelation;

    @Autowired
    ReadExcel readExcel;

    @Autowired
    FindTToBRelationConfig findTToBRelationConfig;

    private static final Logger logger =LoggerFactory.getLogger(FindTableRelation.class);

    public List<List<WarehousingAnalysize>> initTableName(){
        String inputExcelName = findTToBRelationConfig.getWarehousingExcelName();
        String inputExcelSheetName = findTToBRelationConfig.getWarehousingExcelSheetName();
        List<List<WarehousingAnalysize>> listDomesticAndOverseas = doReadInputExcel(inputExcelName, inputExcelSheetName);
/*       List<String> listDomestic = listDomesticAndOverseas.get(0);
        List<String> listOverseas = listDomesticAndOverseas.get(1);*/
        return listDomesticAndOverseas;
    }

    public List<List<SdmExcelOffical>> initSdmExcel(){
        String domesticSdmExcelName = findTToBRelationConfig.getDomesticSdmExcelName();
        String domesticSdmExcelSheetName = findTToBRelationConfig.getDomesticSdmExcelSheetName();
        String overseasSdmExcelName = findTToBRelationConfig.getOverseasSdmExcelName();
        String overseasSdmExcelSheetName = findTToBRelationConfig.getOverseasSdmExcelSheetName();
        List<List<SdmExcelOffical>> list = new ArrayList<List<SdmExcelOffical>>();
        List<List<SdmExcelOffical>> listSdmExcelDomesticAndOverseas = findTableRelation.doReadSdmExcel(domesticSdmExcelName, domesticSdmExcelSheetName, overseasSdmExcelName, overseasSdmExcelSheetName);//读国内全量sdmExcel
        List<SdmExcelOffical> listDomesticSdmExcel = listSdmExcelDomesticAndOverseas.get(0);
        List<SdmExcelOffical> listOverseasSdmExcel = listSdmExcelDomesticAndOverseas.get(1); //读海外全量sdmExcel
        list.add(listDomesticSdmExcel);
        list.add(listOverseasSdmExcel);
        return list;
    }


    public void doWriteIsWarehousingExcel(String fileName,List<TtoMOutputExcel> listTtoMOutputExcel){
        ExcelWriter writer = EasyExcel.write(fileName).build();
        try{
            WriteSheet writeSheet1 = EasyExcel.writerSheet(0, "入仓表").head(TtoMOutputExcel.class).build();

            writer.write(listTtoMOutputExcel, writeSheet1);

            logger.info("写入excel"+fileName+"成功");
        } finally {
            // 千万别忘记close 会帮忙关闭流
            if (writer != null) {
                writer.close();
            }
        }
    }

    /**
     * @Description 多sheet写入
     * @Date 2022/9/17 22:47
     * @param fileName
     * @param map 中存放<sheet名，list<对象>
     * @param lsClass 指对象的类型
     * @return void
     **/
    public void doWriteMulti(String fileName, Map<String,Object> map,List<Class> lsClass){
        ExcelWriter writer = EasyExcel.write(fileName).registerWriteHandler(new LongestMatchColumnWidthStyleStrategy()).build();
        Set<Map.Entry<String, Object>> entries = map.entrySet();
        int countSheetNo = 0;
        int countLsClass = 0;
        try{

            for (Map.Entry<String, Object> entry : entries) {
                List<T> ls = (List<T>)entry.getValue();
                WriteSheet writeSheet1 = EasyExcel.writerSheet(countSheetNo, entry.getKey()).head(lsClass.get(countLsClass)).build();
                writer.write(ls, writeSheet1);
                countSheetNo++;
                countLsClass++;
            }
            logger.info("写入excel "+fileName+" 成功");
        } finally {
            // 千万别忘记close 会帮忙关闭流
            if (writer != null) {
                writer.close();
            }
        }
    }


    /**
     * @Description 根据传入的tableName 和 listSdm 进行匹配，找到对应的ods层表英文名和中文名，
     * @Date 2022/8/19 15:21
     * @Param
     * @param
     * @param sdmType  国内or海外
     * @return java.util.List<java.lang.String>
     **/
    public List<Object> matchSdmTableName(List<WarehousingAnalysize> listInput, List<SdmExcelOffical >listSdm,String sdmType){
        List<TtoMOutputExcel>  listTtoMOutputExcel = new ArrayList<>();
        List<SdmExcelOffical> listRetSdmExcel = new ArrayList<>();
        List<Object> retList = new ArrayList<>();
        int countIsWarehousing = 0;
        List<String> inputIsRepetitive = new ArrayList<>();
        for(WarehousingAnalysize input:listInput){
            boolean flagIsWarehousing = false;
            String tableName = input.getSpliceTableName();
//            Pattern n = Pattern.compile("ods_"+tableName+"_(d|g)\\d_(i|f/z)_\\w(_snapshot)?");
            Pattern n = Pattern.compile("(ods|o)_" + tableName + "(_(d|g)\\d_(i|f|z)_)?(\\w)?(_snapshot)?");

            //eg 01_invm
            for(SdmExcelOffical sdmExcelOffical:listSdm){
                String originalTableNameEn = sdmExcelOffical.getOriginalTableNameEn();
                //eg ods_01_invm_d0_i_d
                if(originalTableNameEn==null){
                    continue;
                }

                originalTableNameEn=originalTableNameEn.toLowerCase();
                Matcher m = n.matcher(originalTableNameEn);
                if(m.matches()){
                    listRetSdmExcel.add(sdmExcelOffical);
                    flagIsWarehousing = true;

                    TtoMOutputExcel ttoMOutputExcel = new TtoMOutputExcel();
                    ttoMOutputExcel.setSystemFlag(input.getSystemFlag());
                    ttoMOutputExcel.setSourceSystemName(input.getSourceSystemName());
                    ttoMOutputExcel.setSpliceTableName(input.getSpliceTableName());
                    ttoMOutputExcel.setSourceFileNameCn(input.getTableNameCn());
//                    countIsWarehousing++;
                    ttoMOutputExcel.setIsWarehousing("是");
                    ttoMOutputExcel.setOdsTableName(originalTableNameEn);
                    ttoMOutputExcel.setMTableNameEn(sdmExcelOffical.getTargetTableNameEn());
                    ttoMOutputExcel.setOdsTableNameCn(sdmExcelOffical.getOriginalTableNameCn());
                    ttoMOutputExcel.setMTableNameCn(sdmExcelOffical.getTargetTableNameCn());

                    //去重，方便筛选
                    if(!inputIsRepetitive.contains(input.getSpliceTableName())){
                        inputIsRepetitive.add(input.getSpliceTableName());
                        ttoMOutputExcel.setUniqueInputTableName(input.getSpliceTableName());
                    }
                    listTtoMOutputExcel.add(ttoMOutputExcel);
                }
//                if(s1[0].equals(s2[1])&&s1[1].equals(s2[2])&&s2[3].){}
            }
            if(!flagIsWarehousing){
                TtoMOutputExcel ttoMOutputExcel = new TtoMOutputExcel();
                ttoMOutputExcel.setSystemFlag(input.getSystemFlag());
                ttoMOutputExcel.setSourceSystemName(input.getSourceSystemName());
                ttoMOutputExcel.setSpliceTableName(input.getSpliceTableName());
                ttoMOutputExcel.setSourceFileNameCn(input.getTableNameCn());
                ttoMOutputExcel.setIsWarehousing(("否"));
                countIsWarehousing++;

                //去重，方便筛选
                if(!inputIsRepetitive.contains(input.getSpliceTableName())){
                    inputIsRepetitive.add(input.getSpliceTableName());
                    ttoMOutputExcel.setUniqueInputTableName(input.getSpliceTableName());
                }
                listTtoMOutputExcel.add(ttoMOutputExcel);
            }
        }
        logger.info(sdmType+countIsWarehousing+"张表未入仓");
        listTtoMOutputExcel = removeDuplicatesOutput(listTtoMOutputExcel);

        Collections.sort(listTtoMOutputExcel, new Comparator<TtoMOutputExcel>(){
            public int compare(TtoMOutputExcel o1, TtoMOutputExcel o2) {
                //按照学生的年龄进行升序排列
                if(o1.getSpliceTableName().compareTo(o2.getSpliceTableName()) == 0){
                    return 0;
                }
                else if(o1.getSpliceTableName().compareTo(o2.getSpliceTableName()) > 0){
                    return 1;
                } else {
                    return -1;
                }

            }
        });
//        listTtoMOutputExcel = listTtoMOutputExcel.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(o1 -> o1.getMTableNameEn()+o1.getSpliceTableName()))), ArrayList::new));
        retList.add(listTtoMOutputExcel);
        retList.add(listRetSdmExcel);
        return retList;
    }

    /**
     * @Description 对输入文件进行去重，将重复的t表去除
     * @Date 2022/8/24 14:14
     * @Param
     * @param listTtoMOutputExcel
     * @return java.util.List<com.datawarehouse.excelgenerate.entity.WarehousingAnalysize>
     **/
/*    public List<WarehousingAnalysize> removeDuplicates(List<WarehousingAnalysize> listTtoMOutputExcel) {
        List<Integer> index = new ArrayList<>();
        List<WarehousingAnalysize> ret= new ArrayList<>();
        List<WarehousingAnalysize> lsCopy = listTtoMOutputExcel;
        for(int i=0; i<listTtoMOutputExcel.size(); i++) {
            for(int j=i+1; j<lsCopy.size();j++){
                if(listTtoMOutputExcel.get(i).getSpliceTableName()!=null){
                    if(listTtoMOutputExcel.get(i).getSpliceTableName().equals(lsCopy.get(j).getSpliceTableName())){
                        index.add(j);
                    }
                }

            }
        }
        for(int i=0; i<listTtoMOutputExcel.size(); i++) {
            if(!index.contains(i)){
                ret.add(listTtoMOutputExcel.get(i));
            }
        }
        return ret;

    }*/

    public List<WarehousingAnalysize> removeDuplicates(List<WarehousingAnalysize> listTtoMOutputExcel){
        Set<WarehousingAnalysize> personSet = new TreeSet<>((o1, o2) -> o1.getSpliceTableName().compareTo(o2.getSpliceTableName()));
        personSet.addAll(listTtoMOutputExcel);

        return new ArrayList<>(personSet);
    }

    /**
     * @Description 对输出文件进行去重，使m层表唯一，原因是一个t表可能对应多个m表 ,自己写的很垃圾，运行效率贼慢
     * @Date 2022/8/24 14:13
     * @Param
     * @param
     * @return java.util.List<com.datawarehouse.excelgenerate.entity.TtoMOutputExcel>
     **/


    //写的有问题
    public List<TtoMOutputExcel> removeDuplicatesOutput(List<TtoMOutputExcel> persons) {
        Set<TtoMOutputExcel> personSet = new TreeSet<>((o1, o2) -> (o1.getMTableNameEn()+o1.getSpliceTableName()).compareTo(o2.getMTableNameEn()+o2.getSpliceTableName()));
        personSet.addAll(persons);

        return new ArrayList<>(personSet);
    }





    public List<List<WarehousingAnalysize>> doReadInputExcel(String excelFileName,String excelSheetName) {
        logger.info("开始读取t层输入excel");
        List<WarehousingAnalysize> listDomesticT = new ArrayList<>();
        List<WarehousingAnalysize> listOverseasT = new ArrayList<>();
        List<List<WarehousingAnalysize>> listDomesticAndOverseasT =new ArrayList<>();

        List<WarehousingAnalysize> ts = readExcel.doReadCommonExcel(excelFileName, excelSheetName, WarehousingAnalysize.class);
        logger.info("输入表共"+ts.size()+"张");
        List<List<WarehousingAnalysize>> listDomesticAndOverseas = findTableRelation.judgeDomesticOrOverseas(ts); //判断表名是国内还是海外

        //获取国内和海外的表名
        for (WarehousingAnalysize t : listDomesticAndOverseas.get(0)) {
            if(t.getSpliceTableName()==null) continue;
            String spliceTableName = t.getSpliceTableName().toLowerCase();
            spliceTableName = spliceTableName.replace('-', '_');
            t.setSpliceTableName(spliceTableName);
            listDomesticT.add(t);
        }
        for (WarehousingAnalysize o : listDomesticAndOverseas.get(1)) {
            if(o.getSpliceTableName()==null) continue;
            String spliceTableName1 = o.getSpliceTableName().toLowerCase();
            spliceTableName1 = spliceTableName1.replace('-', '_');
            o.setSpliceTableName(spliceTableName1);
            listOverseasT.add(o);
        }
        listDomesticT=removeDuplicates(listDomesticT);
        listOverseasT=removeDuplicates(listOverseasT);
        logger.info("去重后国内表共"+listDomesticT.size()+"张，海外表共"+listOverseasT.size()+"张");
        listDomesticAndOverseasT.add(listDomesticT);
        listDomesticAndOverseasT.add(listOverseasT);

        return listDomesticAndOverseasT;
    }


    public void isWarehousing(){
        List<List<SdmExcelOffical>> listSdm = initSdmExcel();
        List<List<WarehousingAnalysize>> listInput = initTableName();
        List<SdmExcelOffical> listDomesticSdm = listSdm.get(0);
        List<SdmExcelOffical> listOverseasSdm = listSdm.get(1);
        List<WarehousingAnalysize> listInputDomestic = listInput.get(0);
        List<WarehousingAnalysize> listInputOverseas = listInput.get(1);
        String domesticOutputExcelName = findTToBRelationConfig.getDomesticOutputExcelName();
        String overseasOutputExcelName = findTToBRelationConfig.getOverseasOutputExcelName();

        Map<String,Object> map = new LinkedHashMap<>();
        List<Class> lsClass = new ArrayList<>();
        if(findTToBRelationConfig.getDomesticSdmExcelName()!=""&&findTToBRelationConfig.getDomesticSdmExcelSheetName()!=""&&listInputDomestic.size()!=0){
            List<Object> o = matchSdmTableName(listInputDomestic, listDomesticSdm,"国内");
            List<TtoMOutputExcel> listTtoMOutputExcelsDomestic = (List<TtoMOutputExcel>)o.get(0);
            List<SdmExcelOffical> listSdmExcel = (List<SdmExcelOffical>)o.get(1);
            map.put("入仓表",listTtoMOutputExcelsDomestic);
            map.put("sdm",listSdmExcel);
            lsClass.add(TtoMOutputExcel.class);
            lsClass.add(SdmExcelOffical.class);
//            doWriteIsWarehousingExcel(domesticOutputExcelName,listTtoMOutputExcelsDomestic);
            doWriteMulti(domesticOutputExcelName,map,lsClass);
        }
        if(findTToBRelationConfig.getOverseasSdmExcelName()!=""&&findTToBRelationConfig.getOverseasSdmExcelSheetName()!=""&&listInputOverseas.size()!=0){
            List<Object> o = matchSdmTableName(listInputOverseas, listOverseasSdm,"海外");
            List<TtoMOutputExcel> listTtoMOutputExcelsOverseas = (List<TtoMOutputExcel>)o.get(0);
            List<SdmExcelOffical> listSdmExcel = (List<SdmExcelOffical>)o.get(1);
            map.put("入仓表",listTtoMOutputExcelsOverseas);
            map.put("sdm",listSdmExcel);
            lsClass.add(TtoMOutputExcel.class);
            lsClass.add(SdmExcelOffical.class);
//            doWriteIsWarehousingExcel(domesticOutputExcelName,listTtoMOutputExcelsDomestic);
            doWriteMulti(overseasOutputExcelName,map,lsClass);

        }
    }
}
