package com.datawarehouse.excelgenerate.service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson.JSON;
import com.datawarehouse.excelgenerate.config.FindCConfig;
import com.datawarehouse.excelgenerate.entity.SdmExcelOffical;
import com.datawarehouse.excelgenerate.service.listener.CLevelExcelListener;
import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @ClassName FindCRelation
 * @Description 读取cea合并表，获取c层对应的表
 * @Author xjy
 * @Date 2022/8/16 15:27
 * @Version 1.0
 **/
@Service
public class FindCRelation {

    @Autowired
    FindTableRelation findTableRelation;
    @Autowired
    FindCConfig findCConfig;
    @Autowired
    ReadExcel readExcel;
    private static final Logger logger = LoggerFactory.getLogger(FindCRelation.class);
    public List<LinkedHashMap<Integer,String>> ReadExcel() {
        String fileName = "SDMcea合并.xlsm";
        // 这里 只要，然后读取第一个sheet 同步读取会自动finish
        CLevelExcelListener cLevelExcelListener =new CLevelExcelListener();
        EasyExcel.read(fileName, cLevelExcelListener).sheet(5).doRead();
        List<LinkedHashMap<Integer, String>> linkedHashMaps = cLevelExcelListener.retData();
        for(int i=0;i<linkedHashMaps.size();i++){
//            logger.info(JSON.toJSONString(linkedHashMaps.get(i)));
        }
        return linkedHashMaps;
    }


    /**
     * @Description 寻找表关联信息的索引值，将excel区分成各个list
     * @Date 2022/8/17 9:03
     * @Param
     * @param linkedHashMaps
     * @return void
     **/
    public void checkout(List<LinkedHashMap<Integer,String>> linkedHashMaps){
        int headCount=linkedHashMaps.size();
        List<List<LinkedHashMap<Integer,String>>> twoList = new ArrayList<>();

        //寻找表关联信息的索引值，将excel区分成各个list
        List<Integer> listCount =new ArrayList<>();
        for(int i=0;i<headCount;i++){
//            if("源表schema".equals(linkedHashMaps.get(i).get(1))){
            if(linkedHashMaps.get(i).get(1)!=""&&linkedHashMaps.get(i).get(1).contains("字段映射")){
                listCount.add(i);
            }
            if("分组条件（group by）".equals(linkedHashMaps.get(i).get(1))){
                listCount.add(i);

            }
        }

        //拆分成n个table放入twoList中
        int subTableCount = listCount.size()/2;
        logger.info("临时表和非临时表共"+subTableCount+"张");
        for(int i=0;i<subTableCount;i+=2){
            List<LinkedHashMap<Integer,String>> listMap = new ArrayList<>();
            for(int j=listCount.get(i);j<listCount.get(i+1);j++){
                listMap.add(linkedHashMaps.get(j));
            }
            twoList.add(listMap);
        }
/*        //打印具体的子table
        for (int i = 0; i < twoList.size(); i++) {
            List<LinkedHashMap<Integer, String>> linkedHashMaps1 = twoList.get(i);
            for (int i1 = 0; i1 < linkedHashMaps1.size(); i1++) {
                logger.info(JSON.toJSONString(linkedHashMaps1.get(i1)));
            }
        }*/

        //
        for (int i = 0; i < twoList.size(); i++) {
            //子table
            List<LinkedHashMap<Integer, String>> linkedHashMaps1 = twoList.get(i);

            //创建List接收正则化后的表别名和 [字段名，码值]
            List<List<List<String >>> listReg = new ArrayList<>();
//            List<LinkedHashMap<String, List<String>>> listReg = new ArrayList<>();

            //创建map接收变量表别名和真名
            LinkedHashMap<String, String> tableAlias= new LinkedHashMap<>();
            for (int i1 = 0; i1 < linkedHashMaps1.size(); i1++) {
                LinkedHashMap<Integer,String> headContext = linkedHashMaps1.get(i1);
                if(headContext.get(1).startsWith("${")){
                    tableAlias.put(headContext.get(4),headContext.get(3));  //eg. P1,M_xxxx  放入map中
                    String associatedConditions = headContext.get(6);   //关联条件
                    if(associatedConditions!=null){
                        List<List<String>> regularization = regularization(associatedConditions);
                        listReg.add(regularization);
                    }
                }

                if(headContext.get(1).equals("过滤条件（where）")){
                    String filterConditions = headContext.get(2);   //过滤条件
                    if(filterConditions!=null){
                        List<List<String>> regularization = regularization(filterConditions);
                        listReg.add(regularization);
                    }
                }
            }
//            System.out.println(listReg);

            for (int i2 = 0; i2 < listReg.size(); i2++) {
                for(List<String> ls:listReg.get(i2)){
                    String alias = ls.get(0);
                    String fieldName = ls.get(2);
                    String fieldValue = ls.get(1);
                    String tableName = tableAlias.get(alias);
                    //todo 将别名替换为表名
                    ls.set(0,tableName);
                    logger.info(tableName+" "+fieldName+" "+fieldValue);
                    SdmExcelOffical sdmExcelOffical = matchSdm(ls, initSdmExcel());
                    System.out.println(sdmExcelOffical);
                }

//                logger.info(JSON.toJSONString(tableAlias));
            }
//            System.out.println(listReg);
/*            int m = listReg.size()-1;
            int n = listReg.size()-3;
            System.out.println(listReg.get(m));
            System.out.println(listReg.get(n));*/
        }


    }


    /**
     * @Description 输入字符串返回正则结果
     * @Date 2022/8/17 9:32
     * @Param
     * @param srcStr
     * @return java.util.LinkedHashMap<java.lang.String,java.lang.String>
     **/
    public List<List<String >> regularization(String srcStr){
        String pattern = "(P|p|T|t)[0-9]{1,2}\\.\\S{1,25}(\\s)?=(\\s)?\\'\\S{1,25}\\'";
        // 创建 Pattern 对象
        Pattern r = Pattern.compile(pattern);
        // 现在创建 matcher 对象
        Matcher m = r.matcher(srcStr);
        List<List<String >> reg=new ArrayList<>();
        while(m.find()) {
            String destStr = m.group();
            int i = destStr.indexOf("'");
            int j = destStr.lastIndexOf("'");
            int k = destStr.indexOf("=");
            String trueName=destStr.substring(i,j+1);   //码值
            String alias=destStr.substring(0,2);     //P1
            String fieldName = destStr.substring(3,k);  //字段值
            List<String> ls = new ArrayList();
            ls.add(alias);
            ls.add(trueName);
            ls.add(fieldName);

            //如果已经有了匹配的条件就不加进list了
            boolean isContainsFlag = false;
            for(List<String> elements:reg){
                if(elements==ls){
                    isContainsFlag =true;
                    break;
                }
            }
            if(!isContainsFlag){
                reg.add(ls);
            }
        }
        return reg;
    }

    public void regularizationSrcTab(String srcStr){
        String str = srcStr.toUpperCase();
        String pattern = "(P|p|T|t)[0-9]{1,2}\\.SRC_TAB(\\))?(\\s)?=(\\s)?'\\S{1,25}\\'";
    }

    public List<SdmExcelOffical> initSdmExcel() {
        String domesticSdmExcelName=findCConfig.getDomesticSdmExcelName();
        String domesticSdmExcelSheetName = findCConfig.getDomesticSdmExcelSheetName();
        List<SdmExcelOffical>domesticExcelList=new ArrayList<>();
        if(domesticSdmExcelName!=""&&domesticSdmExcelSheetName!="") {
            domesticExcelList = readExcel.doReadCommonExcel(domesticSdmExcelName, domesticSdmExcelSheetName, SdmExcelOffical.class);
        }
        logger.info("读取国内sdm 共计"+domesticExcelList.size()+"条记录");
        return domesticExcelList;
    }

    public SdmExcelOffical matchSdm(List<String> listStr,List<SdmExcelOffical> listSdmExcelOffical){
        String fieldName = listStr.get(2);
        String fieldValue = listStr.get(1);
        String tableName = listStr.get(0);
        for(SdmExcelOffical sdm:listSdmExcelOffical){
            if(tableName.equals(sdm.getTargetTableNameEn())&&fieldName.equals(sdm.getTargetFieldNameEn())&&fieldValue.equals(sdm.getAssignmentRule())){
                return sdm;
            }
        }
        return null;
    }

    public void doFind(){
        List<LinkedHashMap<Integer, String>> linkedHashMaps = ReadExcel();
        checkout(linkedHashMaps);


    }




/*    public void sdmMatch(String mTableName,String mFieldName,String ){

    }*/

    /*public void retListSdm(){
        String domesticSdmExcelName =findCConfig.getDomesticSdmExcelName();
        String domesticSdmExcelSheetName=findCConfig.getDomesticSdmExcelSheetName();
        String overseasSdmExcelName=findCConfig.getOverseasSdmExcelName();
        String overseasSdmExcelSheetName=findCConfig.getOverseasSdmExcelSheetName();
        if(domesticSdmExcelName!=""&&domesticSdmExcelSheetName!=""&&listDomesticTTableName.size()!=0){
            List<SdmExcelOffical> listDomesticSdmExcel = findTableRelation.doReadSdmExcel(domesticSdmExcelName,domesticSdmExcelSheetName,overseasSdmExcelName,overseasSdmExcelSheetName).get(0); //读国内全量sdmExcel
        }
        if(overseasSdmExcelName!=null&&overseasSdmExcelSheetName!=null&&listOverseasTTableName.size()!=0){
            List<SdmExcelOffical> listOverseasSdmExcel=findTableRelation.doReadSdmExcel(domesticSdmExcelName,domesticSdmExcelSheetName,overseasSdmExcelName,overseasSdmExcelSheetName).get(1);
        }
    }*/
}
