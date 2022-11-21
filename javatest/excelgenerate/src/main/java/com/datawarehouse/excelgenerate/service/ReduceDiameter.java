package com.datawarehouse.excelgenerate.service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.WriteWorkbook;
import com.datawarehouse.excelgenerate.config.DataLakeInfoConfig;
import com.datawarehouse.excelgenerate.config.MatchFieldConfig;
import com.datawarehouse.excelgenerate.entity.DataLakeTable;
import com.datawarehouse.excelgenerate.entity.SdmExcelOffical;
import com.datawarehouse.excelgenerate.entity.reduceDiameter.InputAndSdmField;
import com.datawarehouse.excelgenerate.entity.reduceDiameter.StandardDataExcel;
import com.datawarehouse.excelgenerate.mapper.CommonMapper;
import com.datawarehouse.excelgenerate.service.easyExcelSet.HyperlinkAlreadyInCellWriteHandler;
import com.datawarehouse.excelgenerate.service.easyExcelSet.HyperlinkTableListCellWriteHandler;
import com.datawarehouse.excelgenerate.utils.ObjectHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.crypto.Data;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @ClassName ReduceDiameter
 * @Description 根据业务技术口径生成还原口径
 * @Author xjy
 * @Date 2022/9/22 23:17
 * @Version 1.0
 **/
@Service
public class ReduceDiameter {
    @Autowired
    MatchFieldConfig matchFieldConfig;
    @Autowired
    MatchField matchField;
    @Autowired
    WriteExcel writeExcel;
    @Autowired
    DataLakeInfo dataLakeinfo;
    @Autowired
    DataLakeInfoConfig dataLakeInfoConfig;
    @Autowired
    CommonMapper commonMapper;

    public List<String> listSheet;
    private static final Logger logger = LoggerFactory.getLogger(ReduceDiameter.class);

    //将读取的数据转为List，方便处理，后面使用模板追加写
    public List<List<List<String>>> convertObjToList() {
        List<List<InputAndSdmField>> lists = matchField.doWrite();
        //去重
        lists = lists.stream().distinct().collect(Collectors.toList());
        List<InputAndSdmField> lnputLsDomestic = lists.get(0);
        List<InputAndSdmField> lnputLsOverseas = lists.get(1);
        List<List<List<String>>> lls = new ArrayList<>();
        List<List<String>> listDomestic = ObjectHandle.convertList(lnputLsDomestic);
        List<List<String>> listOverseas = ObjectHandle.convertList(lnputLsOverseas);
        lls.add(listDomestic);
        lls.add(listOverseas);
        return lls;
    }

    //通过变更记录里的日期筛选目标数据
    public List<List<String>> filter(List<List<String>> llsInput, Integer dateLowerLimit, Integer dateUpperLimit) {
        List<List<String>> retLLs = new ArrayList<List<String>>();
        if (dateLowerLimit != null && dateUpperLimit != null) {
            for (List<String> ls : llsInput) {
                String changeRecord = ls.get(8);
                if (changeRecord != null && changeRecord.length() >= 10 && !changeRecord.contains("删除")) {
                    String date = changeRecord.substring(0, 8);
                    Integer dateInt = Integer.parseInt(date);
//                    String changeType = changeRecord.substring(8,10);
                    if (changeRecord.contains("新增")) {
                        if (dateLowerLimit.equals(dateUpperLimit)) {
                            if (dateInt.equals(dateLowerLimit)) {
                                retLLs.add(ls);
                            }
                        } else {
                            if (dateInt <= dateUpperLimit && dateInt >= dateLowerLimit) {
                                retLLs.add(ls);
                            }
                        }
                    }
                }

            }
            return retLLs;
        } else {
            for (List<String> ls1 : llsInput) {
                String changeRecord = ls1.get(8);
                if (changeRecord == null) {
                    retLLs.add(ls1);
                } else if (!changeRecord.contains("删除")) {
                    retLLs.add(ls1);
                }
            }
            return retLLs;
        }

    }

    public List<Object> convertToTableList(List<List<String>> inputLls, String type, List<StandardDataExcel> standardLs) {
        int row = inputLls.size();
        List<String> spliceTableNameLs = new ArrayList<>();
        //为了去除相同源字段名的元素影响
        List<String> spliceTableAndFieldLs = new ArrayList<>();
        //存放表的信息<tableName,<countField,countWareField>
        Map<String, Integer> countSrcFieldMap = new LinkedHashMap<>();
        Map<String, Integer> countTargetFieldMap = new LinkedHashMap<>();

        //存放去重后的list
        List<String> removeDuplicateTable = new ArrayList<>();
        List<String> removeDuplicateField = new ArrayList<>();
        //存放未入仓表
        List<List<String>> notInWarehouseLs = new ArrayList<>();
        //记录index list,用以确定遍历的相同表名是唯一的
        List<Integer> indexLs = new ArrayList<>();
        //记录目标表名list
        Map<String, List<String>> targetTableMap = new LinkedHashMap<>();
        //存已入仓字段
        List<List<String>> alreadyInList = new ArrayList<>();
        //记录字段入仓数,表名去重
/*        for (int i = 0; i < row; i++) {
            List<String> elementLs = inputLls.get(i);
            String spliceTableName = elementLs.get(1) + "_" + elementLs.get(2);
            spliceTableName = spliceTableName.toUpperCase();
            String srcTableNameEn = elementLs.get(27);
            String targetTableNameEn = elementLs.get(10);
            //记录目标表名
            if (targetTableNameEn != null) {
                alreadyInList.add(elementLs);
                if (!targetTableMap.containsKey(spliceTableName)) {
                    List<String> ls = new ArrayList<>();
                    ls.add(targetTableNameEn);
                    targetTableMap.put(spliceTableName, ls);
                } else {
                    if (!targetTableMap.get(spliceTableName).contains(targetTableNameEn)) {
                        List<String> strings = targetTableMap.get(spliceTableName);
                        strings.add(targetTableNameEn);
                        targetTableMap.put(spliceTableName, strings);
                    }
                }
            }
            //确保记录的表的唯一性
            if (!removeDuplicateTable.contains(spliceTableName)) {
                removeDuplicateTable.add(spliceTableName);
                countSrcFieldMap.put(spliceTableName, 1);
                indexLs.add(i);
            } else {
                countSrcFieldMap.put(spliceTableName, countSrcFieldMap.get(spliceTableName) + 1);
            }

            if ("#N/A".equals(srcTableNameEn)) {
                if (countTargetFieldMap.containsKey(spliceTableName)) {
                    countTargetFieldMap.put(spliceTableName, countTargetFieldMap.get(spliceTableName) + 1);
                } else {
                    countTargetFieldMap.put(spliceTableName, 1);
                }
            }
        }*/

        //20221019 记录sheet页和表内容
        Map<String, List<String>> idMap = new LinkedHashMap<>();
        //记录字段入仓数,表名去重
        for (int i = 0; i < row; i++) {
            List<String> elementLs = inputLls.get(i);
            String spliceTableName = elementLs.get(1) + "_" + elementLs.get(2);
            spliceTableName = spliceTableName.toUpperCase();
            String srcTableNameEn = elementLs.get(27);
            String targetTableNameEn = elementLs.get(10);
            String id = elementLs.get(1);
            //记录目标表名
            if (targetTableNameEn != null) {
                alreadyInList.add(elementLs);
                if (!targetTableMap.containsKey(spliceTableName)) {
                    List<String> ls = new ArrayList<>();
                    ls.add(targetTableNameEn);
                    targetTableMap.put(spliceTableName, ls);

                    if (!targetTableMap.get(spliceTableName).contains(targetTableNameEn)) {
                        List<String> strings = targetTableMap.get(spliceTableName);
                        strings.add(targetTableNameEn);
                        targetTableMap.put(spliceTableName, strings);
                    }
                }
            }
            /*//20221019更改逻辑  写入sheet页名称由目标表中文名改为系统id
            if(targetTableNameEn != null){
                alreadyInList.add(elementLs);
                if(id==null){
                    logger.error("id为空，请检查输入文件");
                }else{
                    id = id.toUpperCase();
                }
                if (!idMap.containsKey(spliceTableName)) {
                    List<String> ls = new ArrayList<>();
                    ls.add(id);
                    idMap.put(spliceTableName, ls);

                    if (!targetTableMap.get(spliceTableName).contains(targetTableNameEn)) {
                        List<String> strings = targetTableMap.get(spliceTableName);
                        strings.add(targetTableNameEn);
                        targetTableMap.put(spliceTableName, strings);
                    }
                }

            }*/

            //确保记录的表的唯一性
            if (!removeDuplicateTable.contains(spliceTableName)) {
                removeDuplicateTable.add(spliceTableName);
                countSrcFieldMap.put(spliceTableName, 1);
                indexLs.add(i);
            } else {
                countSrcFieldMap.put(spliceTableName, countSrcFieldMap.get(spliceTableName) + 1);
            }

            if ("#N/A".equals(srcTableNameEn)) {
                if (countTargetFieldMap.containsKey(spliceTableName)) {
                    countTargetFieldMap.put(spliceTableName, countTargetFieldMap.get(spliceTableName) + 1);
                } else {
                    countTargetFieldMap.put(spliceTableName, 1);
                }
            }
        }
        logger.info(type+"字段去重成功");
        //存放写入数据list ，准备使用模板写，所以不用实体类
        List<List<String>> retLls = new ArrayList<>();
        List<String> allWarehousedTableLs = new ArrayList<>();
        List<String> partWarehousedTableLs = new ArrayList<>();
        List<List<SdmExcelOffical>> lists = matchField.initSdmExcel();
        for (Integer j : indexLs) {
            List<String> retLs = new ArrayList<>();
            List<String> elementLs = inputLls.get(j);
            String spliceTableName = elementLs.get(1) + "_" + elementLs.get(2);
            spliceTableName = spliceTableName.toUpperCase();
            List<String> dataLakeInfo = getDataLakeInfo(spliceTableName, type);
            String odsNameEn = dataLakeInfo.get(0);
            String odsNameSnapshot = dataLakeInfo.get(1);
            String odsNameCn = dataLakeInfo.get(2);
            //源系统
            retLs.add(elementLs.get(0));
            //源系统标识
            retLs.add(elementLs.get(1));
            //源表英文名
            retLs.add(elementLs.get(2));
            //源表中文名
            retLs.add(elementLs.get(3));
            retLs.add(null);
            //数据湖是否存在截面
            if (odsNameSnapshot == null) {
                retLs.add("否");
            } else {
                retLs.add("是");
            }
            //接收方式
            if(odsNameEn!=null){
                char c = odsNameEn.toCharArray()[odsNameEn.length()-3];
                if('f'==c){
                    retLs.add("全量");
                }else if('i'==c){
                    retLs.add("增量");
                }
            }else{
                retLs.add("湖未接入");
            }

            //数仓表名
            List<String> strings = targetTableMap.get(spliceTableName);
            if (strings == null) {
                retLs.add("#N/A");
            } else {
                String s = "";
                for (int k = 0; k < strings.size(); k++) {
                    if (k == 0) {
                        s = s + strings.get(k);
                    } else {
                        s = s + "\n" + strings.get(k);
                    }
                }
                retLs.add(s);
            }
            //下传方式 todo
            retLs.add(null);
            //是否落标
            String standardSituation = getStandardSituation(spliceTableName, standardLs);
            String needFieldStandardSituation = getNeedFieldStandardSituation(spliceTableName, inputLls);
            String standard = "";
            if(needFieldStandardSituation.startsWith("全落标")){
                standard = "是";
            }else if(needFieldStandardSituation.startsWith("未落标")){
                standard= "否";
            }else {
                standard="否（部分字段）";
            }
            retLs.add(standard);
            //是否入仓
            Integer countSrcField = countSrcFieldMap.get(spliceTableName);
            Integer countTargetField = 0;
            if (countTargetFieldMap != null && countTargetFieldMap.containsKey(spliceTableName))
                countTargetField = countTargetFieldMap.get(spliceTableName);
            countTargetField = countSrcField - countTargetField;
            String isWarehousing = "";
            String isWare ="";
//            countTargetField = countSrcField-countTargetField;
            if (countSrcField == countTargetField) {
                isWare ="是";
                isWarehousing = "全部入仓" + "(" + countTargetField + "/" + countSrcField + ")";
                //记录字段全部入仓的表
                allWarehousedTableLs.add(spliceTableName);
            } else if (countTargetField == 0) {
                if (initMatch(spliceTableName, type, lists)) {
                    isWare = "否（部分字段）";
                    isWarehousing = "部分入仓" + "(" + countTargetField + "/" + countSrcField + ")";
                    //记录字段部分入仓的表
                    partWarehousedTableLs.add(spliceTableName);
                } else {
                    isWare = "否";
                    isWarehousing = "未入仓" + "(" + countTargetField + "/" + countSrcField + ")";
                    List<String> notInLs = new ArrayList<String>();
                    //写入未入仓sheet
                    notInLs.add(elementLs.get(0));
                    notInLs.add(elementLs.get(2));
                    notInLs.add(elementLs.get(3));
                    notInWarehouseLs.add(notInLs);
                }

            } else {
                isWare = "否（部分字段）";
                isWarehousing = "部分入仓" + "(" + countTargetField + "/" + countSrcField + ")";
                partWarehousedTableLs.add(spliceTableName);
            }
            retLs.add(isWare);

            //登记日期
            //获取当天年月日
            Date date = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
            String nowDate = dateFormat.format(date);
            retLs.add(nowDate);
            //还原口径日期
            retLs.add(null);
            retLs.add(null);
            retLs.add(null);
            retLs.add(odsNameEn);
            retLs.add(odsNameSnapshot);
            retLs.add(odsNameCn);
            retLs.add(isWarehousing);
            retLs.add(standardSituation+"|"+needFieldStandardSituation);
            retLls.add(retLs);

        }
        //todo 1.全表清单中表是否入仓要遍历sdm判断。2.返回已入仓表未入仓字段数组。3.返回已入仓表数组。
        //遍历找出已入仓表未入仓字段和全部已入仓表字段
        List<List<String>> allInWarehouseFieldLs = new ArrayList<>();
        List<List<String>> partInWarehouseFieldLs = new ArrayList<>();

        for (int k = 0; k < row; k++) {
            List<String> elementLs = inputLls.get(k);
            String spliceTableName = elementLs.get(1) + "_" + elementLs.get(2);
            spliceTableName = spliceTableName.toUpperCase();
            String srcTableNameEn = elementLs.get(27);
            String targetTableNameEn = elementLs.get(10);
            for (String allInTable : allWarehousedTableLs) {
                if (allInTable.equals(spliceTableName)) {
                    allInWarehouseFieldLs.add(elementLs);
                }
            }
            for (String partInTable : partWarehousedTableLs) {
                if (partInTable.equals(spliceTableName) && "#N/A".equals(srcTableNameEn)) {
                    partInWarehouseFieldLs.add(elementLs);
                }
            }
        }
        List<Object> retLs = new ArrayList<Object>();
        //全表清单
        retLs.add(retLls);
        //已入仓表未入仓字段
        retLs.add(partInWarehouseFieldLs);
        //未入仓表
        retLs.add(notInWarehouseLs);
        //已入仓字段
//        retLs.add(allInWarehouseFieldLs);
        retLs.add(alreadyInList);

        //统计写入记录个数
        int countRetLls =0;
        int countPartInWarehouseFieldLs =0;
        int countNotInWarehouseLs =0;
        int countAlreadyInList =0;
        if(retLls!=null)   countRetLls =retLls.size();
        if(partInWarehouseFieldLs!=null) countPartInWarehouseFieldLs =partInWarehouseFieldLs.size();
        if(notInWarehouseLs!=null) countNotInWarehouseLs =notInWarehouseLs.size();
        if(alreadyInList!=null) countAlreadyInList =alreadyInList.size();
        logger.info(type+"全表清单"+countRetLls+"条记录，已入仓表未入仓字段"+countPartInWarehouseFieldLs+"条记录，未入仓表"+countNotInWarehouseLs+"条记录，已入仓字段"+countAlreadyInList+"条记录");
        logger.info(type+"信息处理成功");
        return retLs;
    }

    public Boolean doMatchIsWarehousing(String spliceTableName, List<SdmExcelOffical> listSdm) {
        Pattern n = Pattern.compile("(ods|o)_" + spliceTableName + "(_(d|g)\\d_(i|f|z)_)?(\\w)?(_snapshot)?");
        for (SdmExcelOffical sdmExcelOffical : listSdm) {
            String originalTableNameEn = sdmExcelOffical.getOriginalTableNameEn();
            //eg ods_01_invm_d0_i_d
            if (originalTableNameEn == null) {
                continue;
            }
            originalTableNameEn = originalTableNameEn.toUpperCase();
            Matcher m = n.matcher(originalTableNameEn);
            if (m.matches()) {
                return true;
            }
        }
        return false;
    }

    public Boolean initMatch(String spliceTableName, String type, List<List<SdmExcelOffical>> lists) {
        List<SdmExcelOffical> sdmLsDomestic = lists.get(0);
        List<SdmExcelOffical> sdmLsOverseas = lists.get(1);
        if (type.equals("国内")) {
            return doMatchIsWarehousing(spliceTableName, sdmLsDomestic);
        } else if (type.equals("海外")) {
            return doMatchIsWarehousing(spliceTableName, sdmLsOverseas);
        }
        return false;
    }

    public List<Object> getData() {
        String reduceDiameterOutputFileName = matchFieldConfig.getReduceDiameterOutputFileName();
        List<List<List<String>>> lists = convertObjToList();
        List<List<String>> inputDomestic = lists.get(0);
        List<List<String>> inputOverseas = lists.get(1);
        //根据变更记录筛选
        Integer dateDomesticUpperLimit = matchFieldConfig.getDateDomesticUpperLimit();
        Integer dateDomesticLowerLimit = matchFieldConfig.getDateDomesticLowerLimit();
        Integer dateOverseasUpperLimit = matchFieldConfig.getDateOverseasUpperLimit();
        Integer dateOverseasLowerLimit = matchFieldConfig.getDateOverseasLowerLimit();
        inputDomestic = filter(inputDomestic, dateDomesticLowerLimit, dateDomesticUpperLimit);
        inputOverseas = filter(inputOverseas, dateOverseasLowerLimit, dateOverseasUpperLimit);

        int countInputDomestic =0;
        int countInputOverseas =0;
        if(inputDomestic != null) countInputDomestic=inputDomestic.size();
        if(inputOverseas != null) countInputOverseas=inputOverseas.size();
        logger.info("本次更新日期为：  国内： "+dateDomesticLowerLimit+"-"+dateDomesticUpperLimit+"   海外： "+dateOverseasLowerLimit+"-"+dateOverseasUpperLimit);
        logger.info("国内所需字段"+countInputDomestic+"个，海外所需字段"+countInputOverseas+"个");

        List<List<StandardDataExcel>> lists1 = matchField.initStandardDataExcel();
        List<StandardDataExcel> standardLsDomestic = lists1.get(0);
        List<StandardDataExcel> standardLsOverseas = lists1.get(1);

        //表头
        List<List<String>> headList = getHeadList();
        List<Object> lsObject = new ArrayList<Object>();
        lsObject.add(reduceDiameterOutputFileName);
        lsObject.add(headList);

        //国内
        if(inputDomestic!=null){
            List<Object> outputDomestic = convertToTableList(inputDomestic, "国内",standardLsDomestic);
            List<List<String>> tableListDomestic = (List<List<String>>) outputDomestic.get(0);
            List<List<String>> partInListDomestic = (List<List<String>>) outputDomestic.get(1);
            List<List<String>> notInListDomestic = (List<List<String>>) outputDomestic.get(2);
            List<List<String>> alreadyInListDomestic = (List<List<String>>) outputDomestic.get(3);
            Map<String, List<List<String>>> alreadyInMapDomestic = splitList(alreadyInListDomestic);
            Map<String, List<List<String>>> convertAlreadyInMapDomestic = convertToAlreadyIn(alreadyInMapDomestic, "国内");
            lsObject.add(tableListDomestic);
            lsObject.add(partInListDomestic);
            lsObject.add(notInListDomestic);
            lsObject.add(convertAlreadyInMapDomestic);
        }else{
            lsObject.add(null);
            lsObject.add(null);
            lsObject.add(null);
            lsObject.add(null);
        }

        //海外
        if(inputDomestic != null){
            List<Object> outputOverseas = convertToTableList(inputOverseas, "海外",standardLsOverseas);
            //全表清单
            List<List<String>> tableListOverseas = (List<List<String>>) outputOverseas.get(0);
//        List<List<String>> tableList = ObjectHandle.mergeList(tableListDomestic, tableListOverseas);
            //已入仓表未入仓字段
            List<List<String>> partInListOverseas = (List<List<String>>) outputOverseas.get(1);
//        List<List<String>> partInList = ObjectHandle.mergeList(partInListDomestic, partInListOverseas);
            //未入仓表
            List<List<String>> notInListOverseas = (List<List<String>>) outputOverseas.get(2);
//        List<List<String>> notInList = ObjectHandle.mergeList(notInListDomestic, notInListOverseas);
            //已入仓字段
            List<List<String>> alreadyInListOverseas = (List<List<String>>) outputOverseas.get(3);
//        List<List<String>> alreadyInList = ObjectHandle.mergeList(alreadyInListDomestic, alreadyInListOverseas);
            //去重分组
            //转换成写入list
            Map<String, List<List<String>>> alreadyInMapOverseas = splitList(alreadyInListOverseas);
            Map<String, List<List<String>>> convertAlreadyInMapOverseas = convertToAlreadyIn(alreadyInMapOverseas, "海外");
            lsObject.add(tableListOverseas);
            lsObject.add(partInListOverseas);
            lsObject.add(notInListOverseas);
            lsObject.add(convertAlreadyInMapOverseas);
        }else{
            lsObject.add(null);
            lsObject.add(null);
            lsObject.add(null);
            lsObject.add(null);
        }

        return lsObject;
    }

    public void doWrite() {
        List<Object> data = getData();
        String reduceDiameterOutputFileName = (String) data.get(0);
        List<List<String>> headList = (List<List<String>>) data.get(1);
        List<List<String>> tableListDomestic = (List<List<String>>) data.get(2);
        List<List<String>> partInListDomestic = (List<List<String>>) data.get(3);
        List<List<String>> notInListDomestic = (List<List<String>>) data.get(4);
        Map<String, List<List<String>>> convertAlreadyInMapDomestic = (Map<String, List<List<String>>>) data.get(5);
        List<List<String>> tableListOverseas = (List<List<String>>) data.get(6);
        List<List<String>> partInListOverseas = (List<List<String>>) data.get(7);
        List<List<String>> notInListOverseas = (List<List<String>>) data.get(8);
        Map<String, List<List<String>>> convertAlreadyInMapOverseas = (Map<String, List<List<String>>>) data.get(9);

        List<String> sheetListDomestic = getSheetList(tableListDomestic, convertAlreadyInMapDomestic);
        List<String> sheetListOverseas = getSheetList(tableListOverseas, convertAlreadyInMapOverseas);


        writeMulti(reduceDiameterOutputFileName, sheetListDomestic, sheetListOverseas, headList, tableListDomestic, partInListDomestic, notInListDomestic, convertAlreadyInMapDomestic, tableListOverseas, partInListOverseas, notInListOverseas, convertAlreadyInMapOverseas);
    }

    public List<String> getSheetList(List<List<String>> tableList, Map<String, List<List<String>>> alreadyInMap) {
        List<String> sheetList = new ArrayList<String>();

        for (List<String> ls : tableList) {
            Boolean isAlreadyIn =false;
            for (String key : alreadyInMap.keySet()) {
                String sTable = ls.get(1) + "_" + ls.get(2);
                if (sTable.toUpperCase().equals(key.toUpperCase())) {
                    List<List<String>> lls = alreadyInMap.get(key);
                    String s = lls.get(lls.size() - 1).get(0);
                    sheetList.add(s);
                    isAlreadyIn= true;
                }
            }
            if(!isAlreadyIn){
                sheetList.add("此处无需超链接");
            }
        }
        if(sheetList.size()==0){
            return null;
        }
//        logger.info("sheetList:" + sheetList);
        return sheetList;
    }

    public void writeMulti(String fileName, List<String> sheetListDomestic, List<String> sheetListOverseas, List<List<String>> headList, List<List<String>> tableListDomestic, List<List<String>> partInListDomestic, List<List<String>> notInListDomestic, Map<String, List<List<String>>> alreadyInMapDomestic
            , List<List<String>> tableListOverseas, List<List<String>> partInListOverseas, List<List<String>> notInListOverseas, Map<String, List<List<String>>> alreadyInMapOverseas) {
        ExcelWriter excelWriter = new ExcelWriter(new WriteWorkbook());
        try {
            File templateFile = new File(fileName);
            File destFile = new File("gen_" + fileName);
            excelWriter = EasyExcel.write(templateFile).withTemplate(templateFile)
                    //.file() 指定目标文件，不能与模板文件是同一个文件
                    .file(destFile).autoCloseStream(false).build();

/*            WriteSheet writeSheetTableListDomestic = EasyExcel.writerSheet("全表清单").registerWriteHandler(new HyperlinkTableListCellWriteHandler(sheetListDomestic)).build();
            excelWriter.write(tableListDomestic, writeSheetTableListDomestic);
            WriteSheet writeSheetTableListOverseas = EasyExcel.writerSheet("全表清单").registerWriteHandler(new HyperlinkTableListCellWriteHandler(sheetListOverseas)).build();
            excelWriter.write(tableListOverseas, writeSheetTableListOverseas);*/
            List<List<String>> tableList = ObjectHandle.mergeList(tableListDomestic, tableListOverseas);
            List<String> sheetList = ObjectHandle.mergeList(sheetListDomestic, sheetListOverseas);
            WriteSheet writeSheetTableList = EasyExcel.writerSheet("全表清单").registerWriteHandler(new HyperlinkTableListCellWriteHandler(sheetList)).build();
            excelWriter.write(tableList, writeSheetTableList);

            WriteSheet writeSheetPartIn = EasyExcel.writerSheet("已入仓表未入仓字段").build();
            if(partInListDomestic!=null) excelWriter.write(partInListDomestic, writeSheetPartIn);
            if(partInListOverseas!=null) excelWriter.write(partInListOverseas, writeSheetPartIn);

            WriteSheet writeSheetNotIn = EasyExcel.writerSheet("未入仓表").build();
            if(notInListDomestic!=null) excelWriter.write(notInListDomestic, writeSheetNotIn);
            if(notInListOverseas!=null) excelWriter.write(notInListOverseas, writeSheetNotIn);
            for (String key : alreadyInMapDomestic.keySet()) {
                List<List<String>> lls = alreadyInMapDomestic.get(key);
                String s = lls.get(lls.size() - 1).get(0);
                lls.remove(lls.size() - 1);
                WriteSheet writeSheet = EasyExcel.writerSheet(s).head(headList).registerWriteHandler(new HyperlinkAlreadyInCellWriteHandler()).build();
                excelWriter.write(lls, writeSheet);
            }
            for (String key : alreadyInMapOverseas.keySet()) {
                List<List<String>> lls = alreadyInMapOverseas.get(key);
                String s = lls.get(lls.size() - 1).get(0);
                lls.remove(lls.size() - 1);
                WriteSheet writeSheet = EasyExcel.writerSheet(s).head(headList).registerWriteHandler(new HyperlinkAlreadyInCellWriteHandler()).build();
                excelWriter.write(lls, writeSheet);
            }

/*            WriteSheet writeSheetTest = EasyExcel.writerSheet("测试").head(headList).registerWriteHandler(new HyperlinkAlreadyInCellWriteHandler()).build();
            excelWriter.write(tableList,writeSheetTest);*/
            logger.info("写入 " + "gen_" + fileName + " 成功");
        } catch (Exception e) {
            logger.error("写入 " + "gen_" + fileName + " 失败");
            e.printStackTrace();
        } finally {
            // 千万别忘记finish 会帮忙关闭流
            if (excelWriter != null) {
                excelWriter.finish();
            }
        }
    }


    //将输入的list 根据表名进行拆分
    public Map<String, List<List<String>>> splitList(List<List<String>> lsInput) {
        Map<String, List<List<String>>> splice = lsInput.stream()
                .filter(temp -> !"#N/A".equals(temp.get(27)))
                .collect(Collectors.groupingBy(
                        p -> p.get(1) + "_" + p.get(2)
                ));
        return splice;
    }

    //设置表头
    public List<List<String>> getHeadList() {
        List<List<String>> retLs = new ArrayList<>();
        List<String> ls0 = Arrays.asList("源系统");
        List<String> ls1 = Arrays.asList("源表英文名");
        List<String> ls2 = Arrays.asList("源表中文名");
        List<String> ls3 = Arrays.asList("源字段英文名");
        List<String> ls4 = Arrays.asList("源字段中文名");
        List<String> ls5 = Arrays.asList("M层表名");
        List<String> ls6 = Arrays.asList("M层字段英文名");
        List<String> ls7 = Arrays.asList("M层字段中文名");
        List<String> ls8 = Arrays.asList("M是否主键");
        List<String> ls9 = Arrays.asList("C层表名");
        List<String> ls10 = Arrays.asList("C层字段英文名");
        List<String> ls11 = Arrays.asList("C层字段中文名");
        List<String> ls12 = Arrays.asList("C是否主键");
        List<String> ls13 = Arrays.asList("加工规则");
        List<String> ls14 = Arrays.asList("取数条件");
        List<String> ls15 = Arrays.asList("备注");
        List<String> ls16=  Arrays.asList("M层表中文名");
        Collections.addAll(retLs, ls0, ls1, ls2, ls3, ls4, ls5, ls6, ls7, ls8, ls9, ls10, ls11, ls12, ls13, ls14, ls15,ls16);
        return retLs;
    }

    //写入数据转换,最后一个list是sheet名
    public Map<String, List<List<String>>> convertToAlreadyIn(Map<String, List<List<String>>> mapInput, String type) {
        LinkedHashMap<String, List<List<String>>> retMap = new LinkedHashMap<>();
        for (String key : mapInput.keySet()) {
            List<List<String>> lls = mapInput.get(key);
            List<List<String>> retLls = new ArrayList<>();
            for (int i = 0; i < lls.size(); i++) {
                List<String> ls = lls.get(i);
                List<String> retLs = new ArrayList<>();
                retLs.add(ls.get(0));
                retLs.add(ls.get(2));
                retLs.add(ls.get(3));
                retLs.add(ls.get(4));
                retLs.add(ls.get(5));
                retLs.add(ls.get(10));
                retLs.add(ls.get(11));
                retLs.add(ls.get(16));
                retLs.add(ls.get(18));
                retLs.add(null);
                retLs.add(null);
                retLs.add(null);
                retLs.add(null);
                //v20221009变更逻辑，取概要注释规则
                /*if(ls.get(20)==null){
                    retLs.add(null);
                    retLs.add("直取");
                }else{
                    if(ls.get(20).startsWith("T1.")){
                        retLs.add(null);
                        retLs.add("直取");
                    }else{
                        retLs.add(ls.get(20));
                        retLs.add(null);
                    }
                }*/
                retLs.add(ls.get(21));
                retLs.add(null);
                retLs.add(null);
                retLs.add(ls.get(15));
                retLls.add(retLs);
                //加个sheet名,在最后一行字段匹配的m表中文名
                if (i == lls.size() - 1) {
                    List<String> temp = new ArrayList<String>();
                    if (type.equals("国内")) {
                        temp.add(ls.get(1));
                    } else {
                        temp.add(ls.get(1) + "海外");
                    }
                    retLls.add(temp);
                }
            }
            retMap.put(key, retLls);
        }
        return retMap;
    }

    public String getStandardSituation(String spliceIdAndTable, List<StandardDataExcel> standardList) {
        int countTable = 0;
        int countIn = 0;
        if (standardList == null) return null;
        for (StandardDataExcel standardDataExcel : standardList) {
            String sysId = standardDataExcel.getSysId();
            if (sysId != null) {
                if (sysId.length() == 1) {
                    sysId = "0".concat(sysId);
                }
            }
            String splice = sysId + "_"+standardDataExcel.getSrcTableName();
            if (spliceIdAndTable != null) {
                if (spliceIdAndTable.toLowerCase().equals(splice.toLowerCase())) {
                    countTable++;
                    if (standardDataExcel.getDicId() != null) countIn++;
                }
            }
        }
        if (countTable == 0) return "否(0/0)";
        if (countIn == 0) return "否(0/" + countTable + ")";
        if (countTable == countIn) return "是(" + countIn + "/" + countTable + ")";
        return "否(" + countIn + "/" + countTable + ")";
    }

    public String getNeedFieldStandardSituation(String spliceIdAndTable,List<List<String>> inputLls){
        int countTable=0;
        int countIn=0;
        for(List<String> ls:inputLls){
            String splice =ls.get(1)+ "_" + ls.get(2);
            if(splice.equals(spliceIdAndTable)){
                countTable++;
                if(ls.get(43)!=null){
                    countIn++;
                }
            }
        }
        String type="";
        if(countIn==0) type="未落标";
        else if(countIn<countTable) type="部分落标";
        else type="全落标";
        String retS = type+"("+countIn+"/"+countTable+")";
        return retS;
    }

    public List<String> getDataLakeInfo(String spliceTableName, String type) {
        List<String> retLs = new ArrayList<String>();
        DataLakeTable dataLakeTable = new DataLakeTable();
        if (type.equals("国内")) {
            GetDomesticOdsInfo getDomesticOdsInfo = new GetDomesticOdsInfo(spliceTableName, commonMapper, dataLakeInfoConfig);
            ExecutorService service = Executors.newFixedThreadPool(9);
            Future<DataLakeTable> prime = service.submit(getDomesticOdsInfo);
            try {
                dataLakeTable = prime.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        } else {
            GetOverseasOdsInfo getOverseasOdsInfo = new GetOverseasOdsInfo(spliceTableName, commonMapper, dataLakeInfoConfig);
            ExecutorService service = Executors.newFixedThreadPool(9);
            Future<List<DataLakeTable>> prime = service.submit(getOverseasOdsInfo);
            List<DataLakeTable> dataLakeTableListOverseasThread = new ArrayList<DataLakeTable>();
            try {
                dataLakeTableListOverseasThread = prime.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            if(dataLakeTableListOverseasThread==null||dataLakeTableListOverseasThread.size()==0){
                retLs.add(null);
                retLs.add(null);
                retLs.add(null);
                return retLs;
            }
            dataLakeTable = dataLakeTableListOverseasThread.get(0);
        }
        retLs.add(dataLakeTable.getLakeTableNameEn());
        retLs.add(dataLakeTable.getLakeSnapshotTableNameEn());
        retLs.add(dataLakeTable.getLakeTableNameCn());
        return retLs;
    }

}
