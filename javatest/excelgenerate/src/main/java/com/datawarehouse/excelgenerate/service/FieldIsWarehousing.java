package com.datawarehouse.excelgenerate.service;

import com.datawarehouse.excelgenerate.entity.DemandInputTemplateDetail;
import com.datawarehouse.excelgenerate.entity.SdmExcelOffical;
import com.datawarehouse.excelgenerate.entity.reduceDiameter.InputAndSdmField;
import com.datawarehouse.excelgenerate.entity.reduceDiameter.StandardDataExcel;
import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * @ClassName FieldIsWarehousing
 * @Description TODO
 * @Author xjy
 * @Date 2022/9/18 22:14
 * @Version 1.0
 **/
@Service
public class FieldIsWarehousing implements Callable {
    private static final Logger logger = LoggerFactory.getLogger(FieldIsWarehousing.class);
    private String inputStr;
    private List<SdmExcelOffical> sdmExcelOfficalList;
    private DemandInputTemplateDetail detail;
    private List<StandardDataExcel> standardList;
    public FieldIsWarehousing(){

    }
    public FieldIsWarehousing(String inputStr, List<SdmExcelOffical> sdmExcelOfficalList,DemandInputTemplateDetail detail,List<StandardDataExcel> standardList){
        this.inputStr = inputStr;
        this.sdmExcelOfficalList = sdmExcelOfficalList;
        this.detail = detail;
        this.standardList = standardList;
    }
    //为了去除一个源字段对应多个目标字段的情况，如参与方编号等不在考虑范围内
    public List<SdmExcelOffical> removeDuplicateMTableNameCnTechnologyField(List<SdmExcelOffical> inputLs){
        if(inputLs.size()>1){
            //注意这里ArrayList里存放的对象不是Integer 和String，自定义对象属于可变类，因此需要深拷贝，否则只是指向地址，更改拷贝的数组时，原数组也会更改。
            List<SdmExcelOffical> copyArr = new ArrayList<>();
            for(int i=0;i<inputLs.size(); i++){
                copyArr.add(inputLs.get(i));
            }
            for(int i=0; i<copyArr.size(); i++){
                String targetFieldNameCn = copyArr.get(i).getTargetFieldNameCn();
                if(technologyFieldLs().contains(targetFieldNameCn)){
                    copyArr.remove(i--);
                }
            }
            if(copyArr.size()>0) return copyArr;
        }
        return inputLs;
    }

    public List<SdmExcelOffical> removeDuplicateMTableNameEnHst(List<SdmExcelOffical> inputLs){
        //hst表
        if(inputLs.size()>1){
            int countHst =0;
            for(int i=0; i<inputLs.size(); i++){
                String targetTableNameEn = inputLs.get(i).getTargetTableNameEn().toLowerCase();
                if(targetTableNameEn.endsWith("_hst")) countHst++;
            }
            if(countHst>0&&countHst<inputLs.size()){
                List<SdmExcelOffical> list = new ArrayList<SdmExcelOffical>();
                for(int j=0; j<inputLs.size(); j++){
                    String targetTableNameEn = inputLs.get(j).getTargetTableNameEn().toLowerCase();
                    if(!targetTableNameEn.endsWith("_hst")){
                        list.add(inputLs.get(j));
                    }
                }
                inputLs=list;
            }
        }
        return inputLs;
    }

    public List<SdmExcelOffical> removeDuplicateMTableNameCnTypeCode(List<SdmExcelOffical> inputLs){
        //类型代码
        if(inputLs.size()>1){
            int countTypeCode=0;
            for(int i=0; i<inputLs.size(); i++){
                String targetTableNameCn = inputLs.get(i).getTargetTableNameCn().toLowerCase();
                if(targetTableNameCn.endsWith("类型代码")) countTypeCode++;
            }
            if(countTypeCode>0&&countTypeCode<inputLs.size()){
                List<SdmExcelOffical> list = new ArrayList<SdmExcelOffical>();
                for(int j=0; j<inputLs.size(); j++){
                    String targetTableNameCn = inputLs.get(j).getTargetTableNameCn().toLowerCase();
                    if(!targetTableNameCn.endsWith("类型代码")){
                        list.add(inputLs.get(j));
                    }
                }
                inputLs=list;
            }
        }
        return inputLs;
    }

    public List<SdmExcelOffical> removeDuplicateMTableNameCnPossessorNumber(List<SdmExcelOffical> inputLs){
        if(inputLs.size()>1){
            int countPossessorNumber=0;
            int countCustomerNumber=0;
            for(int i=0; i<inputLs.size(); i++){
                String targetTableNameCn = inputLs.get(i).getTargetTableNameCn().toLowerCase();
                if(targetTableNameCn.endsWith("持有人编号")) countPossessorNumber++;
                if(targetTableNameCn.endsWith("客户编号"))  countCustomerNumber++;
            }
            if(countPossessorNumber>0&&countCustomerNumber>0){
                List<SdmExcelOffical> list = new ArrayList<SdmExcelOffical>();
                for(int j=0; j<inputLs.size(); j++){
                    String targetTableNameCn = inputLs.get(j).getTargetTableNameCn().toLowerCase();
                    if(!targetTableNameCn.endsWith("持有人编号")){
                        list.add(inputLs.get(j));
                    }
                }
                inputLs=list;
            }
        }
        return inputLs;
    }
    public List<SdmExcelOffical> removeDuplicateMTableNameEn(List<SdmExcelOffical> inputLs){
        Boolean isHasField = false;
        if(inputLs.size()>1){
            List<SdmExcelOffical> list = new ArrayList<SdmExcelOffical>();
            for(SdmExcelOffical s:inputLs){
                String targetTableNameEn = s.getTargetTableNameEn().toUpperCase();
                if(targetTableNameEn!=null){
                    if(!targetTableNameEn.equals("M_3AG_CNTRCT")){
                        isHasField=true;
                        list.add(s);
                    }
                }
            }
            if(isHasField){
                inputLs=list;
            }
        }
        return inputLs;
    }

    public List<String> technologyFieldLs (){
        List<String> retLs = new ArrayList<>();
        retLs.add("参与方编号");
        retLs.add("参与方编号(数仓)");
        retLs.add("合约编号");
        retLs.add("合约编号(数仓)");
        retLs.add("事件编号");
        retLs.add("事件编号(数仓)");
        retLs.add("角色编号");
        retLs.add("角色编号(数仓)");
        retLs.add("产品编号");
        retLs.add("产品编号(数仓)");
        retLs.add("法人机构编号");
        retLs.add("中银集团银行号");
        return retLs;
    }
    public List<SdmExcelOffical> matchFieldSdm(){
        List<SdmExcelOffical> sdmListRet = new ArrayList<SdmExcelOffical>();
        for (int i = 0; i < sdmExcelOfficalList.size(); i++) {
            SdmExcelOffical sdmExcelOffical = sdmExcelOfficalList.get(i);
            String odsNameEn = sdmExcelOffical.getOriginalTableNameEn();
            String spliceTableName = null;
            //海外可能会是o_34_b_acc_det_entry2_rep的格式
            //20221115更新 没有考虑snapshot的格式，匹配的格式有问题，应该用正则的
            if(odsNameEn.startsWith("o_")){
                spliceTableName = odsNameEn.substring(2);
            }else if(odsNameEn.endsWith("snapshot")){
                spliceTableName=odsNameEn.substring(4,odsNameEn.length()-16);
            }else {
                spliceTableName = odsNameEn.substring(4,odsNameEn.length()-7);
            }
            String fieldNameEn = sdmExcelOffical.getOriginalFieldNameEn();
            String tableAndField = spliceTableName+"_"+fieldNameEn;
            if(inputStr!=null){
                if(inputStr.toLowerCase().equals(tableAndField.toLowerCase())){
                    sdmListRet.add(sdmExcelOffical);
                }
            }
        }
        if(sdmListRet.size()==0){
            SdmExcelOffical sdm=new SdmExcelOffical();
            sdm.setOriginalTableNameEn("#N/A");
            sdmListRet.add(sdm);
        }
        return sdmListRet;
    }

    public StandardDataExcel matchFieldStandard(){
        StandardDataExcel standardDataExcel = new StandardDataExcel();

        if(standardList!=null){
            for(StandardDataExcel s:standardList){
                if(s!=null){
                    String sysId = s.getSysId();
                    if(sysId!=null){
                        if(sysId.length()==1){
                            sysId = "0".concat(sysId);
                        }
                    }
                    String splice = sysId+"_"+s.getSrcTableName()+"_"+s.getSrcFieldName();
                    if(inputStr!=null){
                        if(inputStr.toLowerCase().equals(splice.toLowerCase())){
                            standardDataExcel = s;
                        }
                    }
                }
            }
        }
        return standardDataExcel;
    }

    //20221206更新，将匹配出的list进行去重，防止出现重复情况
    public List<SdmExcelOffical> removeDuplicateValue (List<SdmExcelOffical> inputLs){
        if(inputLs.size()>1){
            Map<Object,Boolean> map =new HashMap<>();
            List<SdmExcelOffical> collect = inputLs.stream().filter(i -> map.putIfAbsent((i.getOriginalTableNameEn() + i.getOriginalFieldNameEn() + i.getTargetTableNameEn() + i.getTargetFieldNameEn()).toUpperCase(), Boolean.TRUE) == null).distinct().collect(Collectors.toList());
            return collect;
        }
        return inputLs;
    }

    @Override
    public List<InputAndSdmField> call(){
        List<InputAndSdmField> retLs = new ArrayList<InputAndSdmField>();
        try{
            List<SdmExcelOffical> sdmList = matchFieldSdm();
            StandardDataExcel standard = matchFieldStandard();
            //去除技术字段
            List<SdmExcelOffical>tempRemoveDuplicate= removeDuplicateMTableNameCnTechnologyField(sdmList);
            if(tempRemoveDuplicate!=null&&tempRemoveDuplicate.size()>0)    sdmList = tempRemoveDuplicate;
            //去除 3AG合约主表
            tempRemoveDuplicate =removeDuplicateMTableNameEn(sdmList);
            if(tempRemoveDuplicate!=null&&tempRemoveDuplicate.size()>0)    sdmList = tempRemoveDuplicate;
            //去除持有人编号
            tempRemoveDuplicate =removeDuplicateMTableNameCnPossessorNumber(sdmList);
            if(tempRemoveDuplicate!=null&&tempRemoveDuplicate.size()>0)    sdmList = tempRemoveDuplicate;
            //去除类型代码
            tempRemoveDuplicate =removeDuplicateMTableNameCnTypeCode(sdmList);
            if(tempRemoveDuplicate!=null&&tempRemoveDuplicate.size()>0)    sdmList = tempRemoveDuplicate;
            //去除历史表
            tempRemoveDuplicate =removeDuplicateMTableNameEnHst(sdmList);
            if(tempRemoveDuplicate!=null&&tempRemoveDuplicate.size()>0)    sdmList = tempRemoveDuplicate;
            //去重
            tempRemoveDuplicate =removeDuplicateValue(sdmList);
            if(tempRemoveDuplicate!=null&&tempRemoveDuplicate.size()>0)    sdmList = tempRemoveDuplicate;

            //防止报空指针异常
            for(SdmExcelOffical sdmRet : sdmList){
                InputAndSdmField inputAndSdmField = new InputAndSdmField();
                inputAndSdmField.setSrcSystem( detail.getSrcSystem());
                //将属性相同的值复制过去
                BeanUtils.copyProperties(detail, inputAndSdmField);
                BeanUtils.copyProperties(sdmRet, inputAndSdmField);
                BeanUtils.copyProperties(standard, inputAndSdmField);
                retLs.add(inputAndSdmField);
            }
        }catch(Exception e){
            logger.error("匹配字段出错，请检查sdm和输入文件");
        }

        /*List<SdmExcelOffical> sdmList= prime.get();

        //防止报空指针异常
        for(SdmExcelOffical sdmRet : sdmList){
            InputAndSdmField inputAndSdmField = new InputAndSdmField();
            inputAndSdmField.setSrcSystem( detailDomestic.getSrcSystem());
            //将属性相同的值复制过去
            BeanUtils.copyProperties(detailDomestic, inputAndSdmField);
            BeanUtils.copyProperties(sdmRet, inputAndSdmField);
            lsRet.add(inputAndSdmField);
        }*/
        return retLs;
    }
}
