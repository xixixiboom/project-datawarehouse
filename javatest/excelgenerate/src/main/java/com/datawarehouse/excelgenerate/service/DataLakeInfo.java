package com.datawarehouse.excelgenerate.service;

import com.datawarehouse.excelgenerate.config.DataLakeInfoConfig;
import com.datawarehouse.excelgenerate.entity.DataLakeTable;
import com.datawarehouse.excelgenerate.entity.DemandInputTemplateTableList;
import com.datawarehouse.excelgenerate.entity.DemandInputTemplateTableList;
import com.datawarehouse.excelgenerate.entity.RelationTree;
import com.datawarehouse.excelgenerate.mapper.CommonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @ClassName DataLakeInfo
 * @Description TODO
 * @Author xjy
 * @Date 2022/9/15 16:21
 * @Version 1.0
 **/
@Service
public class DataLakeInfo {
    @Autowired
    DataLakeInfoConfig dataLakeInfoConfig;
    @Autowired
    ReadExcel readExcel;
    @Autowired
    CommonMapper commonMapper;
    @Autowired
    WriteExcel writeExcel;
    @Autowired
    FindTToMRelation findTToMRelation;
    @Autowired
    GetDomesticOdsInfo getDomesticOdsInfo;
    @Autowired
    GetOverseasOdsInfo getOverseasOdsInfo;
    private static final Logger logger = LoggerFactory.getLogger(DataLakeInfo.class);

    public List<DemandInputTemplateTableList> initTableName() {
        String inputExcelName = dataLakeInfoConfig.getDemandInputTemplateTableListFileName();
        String inputExcelSheetName = dataLakeInfoConfig.getDemandInputTemplateTableListSheetName();
        List<DemandInputTemplateTableList> ts = readExcel.doReadCommonExcel(inputExcelName, inputExcelSheetName, DemandInputTemplateTableList.class);
        logger.info("共读取表"+ts.size()+"张");
        return ts;
    }

    public List<String> getSpliceTableNameList(List<DemandInputTemplateTableList> templateList) {
        List<String> spliceTableNameList = new ArrayList<String>();
        for (int i = 0; i < templateList.size(); i++) {
            String systemID = templateList.get(i).getSystemID().trim();
            String tableNameEn = templateList.get(i).getTableNameEn().trim();
            String spliceTableName = systemID + "_" + tableNameEn;
            spliceTableNameList.add(spliceTableName);
        }
        return spliceTableNameList;
    }

    public void writeDataLakeInfo(){
        List<DemandInputTemplateTableList> demandInputTemplateTableLists = initTableName();
        List<List<DemandInputTemplateTableList>> domesticOrOverseas = judgeDomesticOrOverseas(demandInputTemplateTableLists);
        List<String> spliceTableNameListDomestic = getSpliceTableNameList(domesticOrOverseas.get(0));
        List<String> spliceTableNameListOverseas = getSpliceTableNameList(domesticOrOverseas.get(1));


        int size = spliceTableNameListDomestic.size();
        List<DataLakeTable> dataLakeTableListDomestic = new ArrayList<DataLakeTable>(size);
        List<DataLakeTable> dataLakeTableListOverseas = new ArrayList<>();
        for (int i = 0; i < spliceTableNameListDomestic.size(); i++) {
            String spliceTableNameDomestic = spliceTableNameListDomestic.get(i);
            logger.info("拼接表名:"+spliceTableNameDomestic);
            GetDomesticOdsInfo getDomesticOdsInfo = new GetDomesticOdsInfo(spliceTableNameDomestic,commonMapper,dataLakeInfoConfig);
            ExecutorService service = Executors.newFixedThreadPool(9);
            Future<DataLakeTable> prime = service.submit(getDomesticOdsInfo);
            DataLakeTable dataLakeTable = new DataLakeTable();
            try {
                dataLakeTable = prime.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            dataLakeTableListDomestic.add(dataLakeTable);
        }
        for(int i=0;i<spliceTableNameListOverseas.size();i++) {
            String spliceTableNameOverseas = spliceTableNameListOverseas.get(i);
            logger.info("拼接表名:"+spliceTableNameOverseas);
            GetOverseasOdsInfo getOverseasOdsInfo = new GetOverseasOdsInfo(spliceTableNameOverseas,commonMapper,dataLakeInfoConfig);
            ExecutorService service = Executors.newFixedThreadPool(9);
            Future<List<DataLakeTable>> prime = service.submit(getOverseasOdsInfo);
            List<DataLakeTable> dataLakeTableListOverseasThread =new ArrayList<DataLakeTable>();
            try {
                dataLakeTableListOverseasThread = prime.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            dataLakeTableListOverseas.addAll(dataLakeTableListOverseasThread);

        }

        String fileName = dataLakeInfoConfig.getOdsOutputFileName();
//        String sheetName = dataLakeInfoConfig.getOdsOutputSheetName();
        Map<String,Object> map = new LinkedHashMap<>();
        List<Class> ls = new ArrayList<>();
        if(dataLakeTableListDomestic!=null){
            map.put("国内",dataLakeTableListDomestic);
            ls.add(DataLakeTable.class);
        }
        if(dataLakeTableListOverseas!=null){
            map.put("海外",dataLakeTableListOverseas);
            ls.add(DataLakeTable.class);
        }

        if(dataLakeTableListDomestic!=null||dataLakeTableListOverseas!=null){
            findTToMRelation.doWriteMulti(fileName,map,ls);
        }else{
            logger.warn("处理数据结果为空，请检查");
        }
    }

    public List<List<DemandInputTemplateTableList>> judgeDomesticOrOverseas(List<DemandInputTemplateTableList> listDemandInputTemplateTable ){
        List<List<DemandInputTemplateTableList>>ls=new ArrayList<>();
        List<DemandInputTemplateTableList> domestic = new ArrayList<>();
        List<DemandInputTemplateTableList> overseas = new ArrayList<>();
        for (DemandInputTemplateTableList t : listDemandInputTemplateTable) {
            String systemName = t.getSystemName();
            String systemNameLower = t.getSystemName().toLowerCase();
            if(systemName.contains("海外")||systemNameLower.contains("-o")||systemNameLower.contains("_o")){
                overseas.add(t);
            }else if(systemName.contains("国内")||systemNameLower.endsWith("-d")||systemNameLower.endsWith("_d")){
                domestic.add(t);
            }else{
                domestic.add(t);
            }
        }
        ls.add(domestic);
        ls.add(overseas);
        logger.info("国内表共"+domestic.size()+"张,海外表共"+overseas.size()+"张");
        return ls;
    }

    /**
     * @return java.util.List<java.lang.String>
     * @Description 根据给定的ods表名，日期上限，日期下限，有数的标准值，判断日期范围内是否有数据
     * @Date 2022/9/16 10:35
     * @Param
     **/

}
