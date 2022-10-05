package com.datawarehouse.excelgenerate.service;

import com.datawarehouse.excelgenerate.config.MatchFieldConfig;
import com.datawarehouse.excelgenerate.entity.SdmExcelOffical;
import com.datawarehouse.excelgenerate.entity.TtoMOutputExcel;
import com.datawarehouse.excelgenerate.entity.WarehousingAnalysize;
import com.datawarehouse.excelgenerate.entity.reduceDiameter.InputAndSdmField;
import com.datawarehouse.excelgenerate.utils.FileHandle;
import com.datawarehouse.excelgenerate.utils.ObjectHandle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sun.awt.image.ImageWatched;

import javax.persistence.criteria.CriteriaBuilder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

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
    //将读取的数据转为List，方便处理，后面使用模板追加写
    public List<List<List<String>>> convertObjToList(){
        List<List<InputAndSdmField>> lists = matchField.doWrite();
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
    public List<List<String>> filter(List<List<String>> llsInput, Integer dateLowerLimit, Integer dateUpperLimit){

        List<List<String>> retLLs = new ArrayList<List<String>>();
        if(dateLowerLimit!=null&&dateUpperLimit!=null){
            for(List<String>ls : llsInput){
                String changeRecord = ls.get(8);
                if(changeRecord!=null&&changeRecord.length()==10){
                    String date = changeRecord.substring(0, 8);
                    Integer dateInt = Integer.parseInt(date);
                    String changeType = changeRecord.substring(8);
                    if(changeType.equals("新增")){
                        if(dateLowerLimit.equals(dateUpperLimit)){
                            if(dateInt.equals(dateLowerLimit)){
                                retLLs.add(ls);
                            }
                        }else{
                            if(dateInt<dateUpperLimit&&dateInt>dateLowerLimit){
                                retLLs.add(ls);
                            }
                        }
                    }
                }

            }
            return retLLs;
        }
        return llsInput;
    }

    public List<Object> convertToTableList(List<List<String>> inputLls,String type){
        int row = inputLls.size();
        List<String> spliceTableNameLs = new ArrayList<>();
        //为了去除相同源字段名的元素影响
        List<String> spliceTableAndFieldLs = new ArrayList<>();
        //存放表的信息<tableName,<countField,countWareField>
        Map<String,Integer> countSrcFieldMap = new LinkedHashMap<>();
        Map<String,Integer> countTargetFieldMap = new LinkedHashMap<>();
        //存放去重后的list
        List<String> removeDuplicateTable = new ArrayList<>();
        List<String> removeDuplicateField = new ArrayList<>();
        //存放未入仓表
        List<List<String>> notInWarehouseLs=new ArrayList<>();
        //记录index list,用以确定遍历的相同表名是唯一的
        List<Integer> indexLs = new ArrayList<>();
        //记录目标表名list
        Map<String, List<String>> targetTableMap =new LinkedHashMap<>();
        //存已入仓字段
        List<List<String>> alreadyLnList = new ArrayList<>();
        //记录字段入仓数,表名去重
        for(int i=0;i<row;i++){
            List<String> elementLs = inputLls.get(i);
            String spliceTableName = elementLs.get(1)+"_"+elementLs.get(2);
            spliceTableName=spliceTableName.toUpperCase();
            String srcTableNameEn = elementLs.get(27);
            String targetTableNameEn = elementLs.get(10);
            //记录目标表名
            if(targetTableNameEn!=null){
                alreadyLnList.add(elementLs);
                if(!targetTableMap.containsKey(spliceTableName)){
                    List<String> ls =new ArrayList<>();
                    ls.add(targetTableNameEn);
                    targetTableMap.put(spliceTableName,ls);
                }else{
                    if(!targetTableMap.get(spliceTableName).contains(targetTableNameEn)){
                        List<String> strings = targetTableMap.get(spliceTableName);
                        strings.add(targetTableNameEn);
                        targetTableMap.put(spliceTableName,strings);
                    }
                }
            }
            //确保记录的表的唯一性
            if(!removeDuplicateTable.contains(spliceTableName)){
                removeDuplicateTable.add(spliceTableName);
                countSrcFieldMap.put(spliceTableName,1);
                indexLs.add(i);
            }else{
                countSrcFieldMap.put(spliceTableName,countSrcFieldMap.get(spliceTableName)+1);
            }

            if("#N/A".equals(srcTableNameEn)){
                if(countTargetFieldMap.containsKey(spliceTableName)){
                    countTargetFieldMap.put(spliceTableName,countTargetFieldMap.get(spliceTableName)+1);
                }else{
                    countTargetFieldMap.put(spliceTableName,1);
                }
            }
        }
        //存放写入数据list ，准备使用模板写，所以不用实体类
        List<List<String>> retLls = new ArrayList<>();
        List<String> allWarehousedTableLs = new ArrayList<>();
        List<String> partWarehousedTableLs = new ArrayList<>();
        for(Integer j:indexLs){
            List<String> retLs = new ArrayList<>();
            List<String> elementLs = inputLls.get(j);
            String spliceTableName = elementLs.get(1)+"_"+elementLs.get(2);
            spliceTableName=spliceTableName.toUpperCase();
            //源系统
            retLs.add(elementLs.get(0));
            //源系统标识
            retLs.add(elementLs.get(1));
            //源表英文名
            retLs.add(elementLs.get(2));
            //源表中文名
            retLs.add(elementLs.get(3));
            //数仓表名
            List<String> strings = targetTableMap.get(spliceTableName);
            if(strings==null){
                retLs.add("#N/A");
            }else{
                String s = "";
                for(int k=0;k<strings.size();k++){
                    if(k==0){
                        s=s+strings.get(k);
                    }else{
                        s=s+"\n"+strings.get(k);
                    }
                }
                retLs.add(s);
            }
            //下传方式 todo
            retLs.add(null);
            //是否落标 todo
            retLs.add(null);
            //是否入仓
            Integer countSrcField = countSrcFieldMap.get(spliceTableName);
            Integer countTargetField = 0;
            if(countTargetFieldMap!=null&& countTargetFieldMap.containsKey(spliceTableName)) countTargetField=countTargetFieldMap.get(spliceTableName);
            countTargetField=countSrcField-countTargetField;
            String isWarehousing ="";
//            countTargetField = countSrcField-countTargetField;
            if(countSrcField==countTargetField){
                isWarehousing="全部入仓"+"("+countTargetField+"/"+countSrcField+")";
                //记录字段全部入仓的表
                allWarehousedTableLs.add(spliceTableName);
            }else if(countTargetField==0){
                if(initMatch(spliceTableName,type)){
                    isWarehousing="部分入仓"+"("+countTargetField+"/"+countSrcField+")";
                    //记录字段部分入仓的表
                    partWarehousedTableLs.add(spliceTableName);
                }else{
                    isWarehousing="未入仓"+"("+countTargetField+"/"+countSrcField+")";
                    List<String> notInLs = new ArrayList<String>();
                    //写入未入仓sheet
                    notInLs.add(elementLs.get(0));
                    notInLs.add(elementLs.get(2));
                    notInLs.add(elementLs.get(3));
                    notInWarehouseLs.add(notInLs);
                }

            }else{
                isWarehousing="部分入仓"+"("+countTargetField+"/"+countSrcField+")";
            }
            retLs.add(isWarehousing);

            //登记日期
            //获取当天年月日
            Date date = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
            String nowDate = dateFormat.format(date);
            retLs.add(nowDate);
            retLls.add(retLs);
        }
        //todo 1.全表清单中表是否入仓要遍历sdm判断。2.返回已入仓表未入仓字段数组。3.返回已入仓表数组。
        //遍历找出已入仓表未入仓字段和全部已入仓表字段
        List<List<String>> allInWarehouseFieldLs = new ArrayList<>();
        List<List<String>> partInWarehouseFieldLs = new ArrayList<>();

        for(int k=0;k<row;k++) {
            List<String> elementLs = inputLls.get(k);
            String spliceTableName = elementLs.get(1) + "_" + elementLs.get(2);
            spliceTableName = spliceTableName.toUpperCase();
            String srcTableNameEn = elementLs.get(27);
            String targetTableNameEn = elementLs.get(10);
            for(String allInTable:allWarehousedTableLs){
                if(allInTable.equals(spliceTableName)){
                    allInWarehouseFieldLs.add(elementLs);
                }
            }
            for(String partInTable:partWarehousedTableLs){
                if(partInTable.equals(spliceTableName)&&"#N/A".equals(srcTableNameEn)){
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
        retLs.add(alreadyLnList);
        return retLs;
    }

    public Boolean doMatchIsWarehousing(String spliceTableName, List<SdmExcelOffical> listSdm){
        Pattern n = Pattern.compile("（ods|o)_"+spliceTableName+"_(d|g)\\d_(i|f/z)_\\w(_snapshot)?");
        for(SdmExcelOffical sdmExcelOffical:listSdm){
            String originalTableNameEn = sdmExcelOffical.getOriginalTableNameEn();
            //eg ods_01_invm_d0_i_d
            if(originalTableNameEn==null){
                continue;
            }
            originalTableNameEn=originalTableNameEn.toUpperCase();
            Matcher m = n.matcher(originalTableNameEn);
            if(m.matches()) {
                return true;
            }
        }
        return false;
    }
    public Boolean initMatch(String spliceTableName,String type){
        List<List<SdmExcelOffical>> lists = matchField.initSdmExcel();
        List<SdmExcelOffical> sdmLsDomestic = lists.get(0);
        List<SdmExcelOffical> sdmLsOverseas = lists.get(1);
        if(type.equals("国内")){
            return doMatchIsWarehousing(spliceTableName,sdmLsDomestic);
        }else if(type.equals("海外")){
            return doMatchIsWarehousing(spliceTableName,sdmLsOverseas);
        }
        return false;
    }

    public void doWrite(){
        String reduceDiameterOutputFileName = matchFieldConfig.getReduceDiameterOutputFileName();
        List<List<List<String>>> lists = convertObjToList();
        List<List<String>> inputDomestic = lists.get(0);
        List<List<String>> inputOverseas = lists.get(1);
        //根据变更记录筛选
        Integer dateDomesticUpperLimit = matchFieldConfig.getDateDomesticUpperLimit();
        Integer dateDomesticLowerLimit = matchFieldConfig.getDateDomesticLowerLimit();
        Integer dateOverseasUpperLimit = matchFieldConfig.getDateOverseasUpperLimit();
        Integer dateOverseasLowerLimit = matchFieldConfig.getDateOverseasLowerLimit();
        inputDomestic=filter(inputDomestic,dateDomesticLowerLimit,dateDomesticUpperLimit);
        inputOverseas = filter(inputOverseas,dateOverseasLowerLimit,dateOverseasUpperLimit);

        List<Object> outputDomestic = convertToTableList(inputDomestic,"国内");
        List<Object> outputOverseas = convertToTableList(inputOverseas,"海外");

        //全表清单
        List<List<String>>tableListDomestic = (List<List<String>>)outputDomestic.get(0);
        List<List<String>>tableListOverseas = (List<List<String>>)outputOverseas.get(0);
        List<List<String>> tableList = ObjectHandle.mergeList(tableListDomestic, tableListOverseas);
        //已入仓表未入仓字段
        List<List<String>> partLnListDomestic = (List<List<String>>)outputDomestic.get(1);
        List<List<String>> partLnListOverseas = (List<List<String>>)outputOverseas.get(1);
        List<List<String>> partLnList = ObjectHandle.mergeList(partLnListDomestic, partLnListOverseas);
        //未入仓表
        List<List<String>> notLnListDomestic = (List<List<String>>)outputDomestic.get(2);
        List<List<String>> notLnListOverseas = (List<List<String>>)outputOverseas.get(2);
        List<List<String>> notLnList = ObjectHandle.mergeList(notLnListDomestic, notLnListOverseas);
        //已入仓字段
        List<List<String>> alreadyLnListDomestic = (List<List<String>>)outputDomestic.get(3);
        List<List<String>> alreadyLnListOverseas = (List<List<String>>)outputOverseas.get(3);
        List<List<String>> alreadyLnList = ObjectHandle.mergeList(alreadyLnListDomestic, alreadyLnListOverseas);

//        writeExcel.writeCommon(reduceDiameterOutputFileName,"全表清单",lsAll);
    }

    public void writeMulti(){

    }

    //将输入的list 根据表名进行拆分
    public Map<String, List<List<String>>> splitList(List<List<String>> lsInput){
        Map<String, List<List<String>>> splice = lsInput.stream()
                .filter(temp -> !"#N/A".equals(temp.get(27)))
                .collect(Collectors.groupingBy(
                        p -> p.get(1) + "_" + p.get(2)
                ));
        return splice;
    }

    //设置表头
    public List<List<String>> getHeadList(){
        List<List<String>> retLs= new ArrayList<>();
        List<String> ls0=Arrays.asList("源系统");
        List<String> ls1=Arrays.asList("源表英文名");
        List<String> ls2=Arrays.asList("源表中文名");
        List<String> ls3=Arrays.asList("源字段英文名");
        List<String> ls4=Arrays.asList("源字段中文名");
        List<String> ls5=Arrays.asList("M层表名");
        List<String> ls6=Arrays.asList("M层字段英文名");
        List<String> ls7=Arrays.asList("M层字段中文名");
        List<String> ls8=Arrays.asList("M是否主键");
        List<String> ls9=Arrays.asList("C层表名");
        List<String> ls10=Arrays.asList("C层字段英文名");
        List<String> ls11=Arrays.asList("C层字段中文名");
        List<String> ls12=Arrays.asList("C是否主键");
        List<String> ls13=Arrays.asList("加工规则");
        List<String> ls14=Arrays.asList("取数条件");
        List<String> ls15=Arrays.asList("备注");
        Collections.addAll(retLs,ls0,ls1,ls2,ls3,ls4,ls5,ls6,ls7,ls8,ls9,ls10,ls11,ls12,ls13,ls14,ls15);
        return retLs;
    }

    //写入数据转换
    public Map<String,List<List<String>>> convertToAlreadyLn(Map<String,List<List<String>>> mapLnput){
        return mapLnput;
    }
}
