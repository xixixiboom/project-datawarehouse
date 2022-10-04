package com.datawarehouse.excelgenerate.service;

import com.datawarehouse.excelgenerate.entity.SdmExcelOffical;
import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

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
    public FieldIsWarehousing(){

    }
    public FieldIsWarehousing(String inputStr, List<SdmExcelOffical> sdmExcelOfficalList){
        this.inputStr = inputStr;
        this.sdmExcelOfficalList = sdmExcelOfficalList;
    }

    public List<SdmExcelOffical> matchField(){
        List<SdmExcelOffical> sdmListRet = new ArrayList<SdmExcelOffical>();
        for (int i = 0; i < sdmExcelOfficalList.size(); i++) {
            SdmExcelOffical sdmExcelOffical = sdmExcelOfficalList.get(i);
            String odsNameEn = sdmExcelOffical.getOriginalTableNameEn();
            String spliceTableName = null;
            //海外可能会是o_34_b_acc_det_entry2_rep的格式
            if(odsNameEn.startsWith("o_")){
                spliceTableName = odsNameEn.substring(2);
            }else{
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
    @Override
    public List<SdmExcelOffical> call(){
        List<SdmExcelOffical> ls = new ArrayList<SdmExcelOffical>();
        try{
            ls = matchField();
        }catch(Exception e){
            logger.error("匹配字段出错，请检查sdm和输入文件");
        }
        return ls;
    }
}
