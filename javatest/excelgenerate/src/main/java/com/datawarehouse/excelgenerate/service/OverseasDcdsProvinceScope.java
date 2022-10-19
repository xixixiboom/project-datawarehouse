package com.datawarehouse.excelgenerate.service;

import com.datawarehouse.excelgenerate.config.OverseasDcdsProvinceScopeConfig;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @ClassName OverseasDcdsProvinceScope
 * @Description TODO
 * @Author xjy
 * @Date 2022/10/14 9:54
 * @Version 1.0
 **/
@Service
public class OverseasDcdsProvinceScope {
    @Autowired
    ReadExcel readExcel;
    @Autowired
    OverseasDcdsProvinceScopeConfig overseasDcdsProvinceScopeConfig;
    @Autowired
    WriteExcel writeExcel;
    public List<LinkedHashMap<Integer, String>> initDcdsOverseasProvince(){
        String fileName =overseasDcdsProvinceScopeConfig.getDcdsProvinceScopeExcelName();
        String sheetName =overseasDcdsProvinceScopeConfig.getDcdsProvinceScopeSheetName();
        if (!fileName.equals("") && !sheetName.equals("")) {
            List<LinkedHashMap<Integer, String>> retLls = readExcel.doReadCommonExcel(fileName, sheetName);
            return retLls;
        }
        return null;
    }

    public List<List> getOverseasProvinceScope(List<LinkedHashMap<Integer, String>> inputMapLs){
        List<List> retLlls = new ArrayList<>();
        Set<String> scopeLs = new LinkedHashSet<String>();
        for(LinkedHashMap<Integer, String> map : inputMapLs){
            String overseasProvinceScope = map.get(9);
            String[] split = overseasProvinceScope.split("\\|");
            for(String s : split){
                if(s!=null){
                    scopeLs.add(s);
                }
            }
        }
        List<List<String>> retLls = new ArrayList<List<String>>();

        for(LinkedHashMap<Integer, String> map : inputMapLs){
            List<String> retLs = new ArrayList<String>();
            for(String s:scopeLs){
                String spliceTableName = map.get(1)+"_"+map.get(6);
                String overseasProvinceScope = map.get(9);

                if(overseasProvinceScope.contains(s)){
                    retLs.add(spliceTableName);
                }else{
                    retLs.add(null);
                }
            }
            retLls.add(retLs);
        }
        List<List<String>> headList = new ArrayList<List<String>>();
        for(String s :scopeLs){
            List<String> strings = Arrays.asList(s);
            headList.add(strings);
        }
        retLlls.add(headList);
        retLlls.add(retLls);
        return retLlls;
    }
    public void doWrite(){
        List<LinkedHashMap<Integer, String>> linkedHashMaps = initDcdsOverseasProvince();
        List<List> lls = getOverseasProvinceScope(linkedHashMaps);
        List<List<String>> headList = lls.get(0);
        List<List<String>> contextLls = lls.get(1);
        String outputFileName = overseasDcdsProvinceScopeConfig.getOutputFileName();
        writeExcel.writeCommon(outputFileName,"分行涉及表",contextLls,headList);
    }
}
