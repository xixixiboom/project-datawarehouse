package com.datawarehouse.excelgenerate.service;

import com.datawarehouse.excelgenerate.config.MatchFieldConfig;
import com.datawarehouse.excelgenerate.entity.*;
import com.datawarehouse.excelgenerate.entity.reduceDiameter.InputAndSdmField;
import com.datawarehouse.excelgenerate.mapper.CommonMapper;
import com.datawarehouse.excelgenerate.utils.ObjectHandle;
import org.hibernate.tool.schema.internal.exec.ScriptTargetOutputToFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @ClassName MatchField
 * @Description 匹配字段是否入仓
 * @Author xjy
 * @Date 2022/9/18 16:16
 * @Version 1.0
 **/
@Service
public class MatchField {
    @Autowired
    MatchFieldConfig matchFieldConfig;
    @Autowired
    ReadExcel readExcel;
    @Autowired
    FindTableRelation findTableRelation;
    @Autowired
    CommonMapper commonMapper;
    @Autowired
    FindTToMRelation findTToMRelation;
    private static final Logger logger = LoggerFactory.getLogger(MatchField.class);
    private List<DemandInputTemplateDetail> initInputData(){
        String inputFileName = matchFieldConfig.getDemandInputTemplateDetailFileName();
        String inputSheetName =matchFieldConfig.getDemandInputTemplateDetailSheetName();
        List<DemandInputTemplateDetail> ts = readExcel.doReadCommonExcel(inputFileName, inputSheetName, DemandInputTemplateDetail.class);
        //如果读取为空则去除
        ts.removeIf(Objects::isNull);
        logger.info("共读取字段"+ts.size()+"个");
        return ts;
    }

    public List<List<SdmExcelOffical>> initSdmExcel(){
        String domesticSdmExcelName = matchFieldConfig.getDomesticSdmFileName();
        String domesticSdmExcelSheetName = matchFieldConfig.getDomesticSdmSheetName();
        String overseasSdmExcelName = matchFieldConfig.getOverseasSdmFileName();
        String overseasSdmExcelSheetName = matchFieldConfig.getOverseasSdmSheetName();
        List<List<SdmExcelOffical>> list = new ArrayList<List<SdmExcelOffical>>();
        List<List<SdmExcelOffical>> listSdmExcelDomesticAndOverseas = findTableRelation.doReadSdmExcel(domesticSdmExcelName, domesticSdmExcelSheetName, overseasSdmExcelName, overseasSdmExcelSheetName);//读国内全量sdmExcel
        List<SdmExcelOffical> listDomesticSdmExcel = listSdmExcelDomesticAndOverseas.get(0);
        List<SdmExcelOffical> listOverseasSdmExcel = listSdmExcelDomesticAndOverseas.get(1); //读海外全量sdmExcel
        list.add(listDomesticSdmExcel);
        list.add(listOverseasSdmExcel);
        return list;
    }

    public List<List<DemandInputTemplateDetail>> judgeDomesticOrOverseas(List<DemandInputTemplateDetail> listDetail ){
        List<List<DemandInputTemplateDetail>>ls=new ArrayList<>();
        List<DemandInputTemplateDetail> domestic = new ArrayList<>();
        List<DemandInputTemplateDetail> overseas = new ArrayList<>();
        for (DemandInputTemplateDetail t : listDetail) {
            String systemName = t.getSrcSystem();
            String systemNameLower = t.getSrcSystem().toLowerCase();
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

    public List<DemandInputTemplateDetail> fillSystemFlag(List<DemandInputTemplateDetail> listDetail){
        for (int i = 0; i < listDetail.size(); i++) {
            DemandInputTemplateDetail detail=listDetail.get(i);
            String systemFlag = detail.getSrcSystemFlag();
            if(systemFlag!=null||!"".equals(systemFlag)){
                continue;
            }else{
                String srcSystem = detail.getSrcSystem().toLowerCase().replace("\n","");
//                String sqlGetSystemFlag = "select distinct sys_name,sys_id from stg_tb_schema_info where sys_name regexp '"+srcSystem+"';";
                String sqlGetSystemFlag = "select distinct sys_name,sys_id from stg_tb_schema_info where sys_name = '"+srcSystem+"';";

                //对系统名做字符串处理
                List<LinkedHashMap<String, String>> mapLsGetFlag = commonMapper.queryByTableName(sqlGetSystemFlag);
                if(mapLsGetFlag.size()==1){
                    detail.setSrcSystem(mapLsGetFlag.get(0).get("sys_id"));
                    continue;
                }else if(mapLsGetFlag.size()>1){
                    String s= null;
                    for(LinkedHashMap<String, String> m : mapLsGetFlag){
                        s=s+m.get("sys_id");
                    }
                    detail.setSrcSystem(s);
                }
 /*               if(srcSystem.endsWith("-国内")||srcSystem.endsWith("-海外")){
                }*/

            }
        }
        return listDetail;
    }
    public List<String> getSpliceTableAndFieldNameList(List<DemandInputTemplateDetail> templateList) {
        List<String> spliceTableNameList = new ArrayList<String>();
        for (int i = 0; i < templateList.size(); i++) {
            if(templateList.get(i)==null){
                spliceTableNameList.add(null);
            }
            String systemID=null;
            String tableNameEn=null;
            String fieldNameEn=null;
            //对输入字段进行修改格式和获取拼接字段
            if(templateList.get(i).getSrcSystem()!=null){
                String system = templateList.get(i).getSrcSystem().replace("\n", "").replace("_", "-");
                templateList.get(i).setSrcSystem(system);
            }
            if(templateList.get(i).getSrcSystemFlag()!=null){
                systemID = templateList.get(i).getSrcSystemFlag().trim().replace("\n","").replace("_","-");
                templateList.get(i).setSrcSystemFlag(systemID);
            }
            if(templateList.get(i).getSrcTableNameEn()!=null){
                tableNameEn = templateList.get(i).getSrcTableNameEn().trim().replace("\n","").replace("-","_");
                templateList.get(i).setSrcTableNameEn(tableNameEn);
            }
            if(templateList.get(i).getSrcFieldNameEn()!=null){
                fieldNameEn = templateList.get(i).getSrcFieldNameEn().trim().replace("\n","").replace("-","_");
                templateList.get(i).setSrcFieldNameEn(fieldNameEn);
            }
            String spliceTableName = systemID + "_" + tableNameEn+"_"+fieldNameEn;
            spliceTableNameList.add(spliceTableName);
        }
        return spliceTableNameList;
    }
    public List<List<InputAndSdmField>> doWrite(){
        //初始化输入数据
        List<DemandInputTemplateDetail> demandInputTemplateDetails = initInputData();
        //查数据库systemID  todo systemID通过数据库校验
        List<DemandInputTemplateDetail> detailsList = fillSystemFlag(demandInputTemplateDetails);
        //获取sdm国内海外
        List<List<SdmExcelOffical>> lists = initSdmExcel();
        List<SdmExcelOffical> sdmListDomestic = lists.get(0);
        List<SdmExcelOffical> sdmListOverseas = lists.get(1);
        //输入文件分出国内海外
        List<List<DemandInputTemplateDetail>> lists1 = judgeDomesticOrOverseas(detailsList);
        List<DemandInputTemplateDetail> detailsDomestic = lists1.get(0);
        List<DemandInputTemplateDetail> detailsOverseas = lists1.get(1);

        //输入文件获取拼接str
        List<String> spliceDomesticList = getSpliceTableAndFieldNameList(detailsDomestic);
        List<String> spliceOverseasList = getSpliceTableAndFieldNameList(detailsOverseas);

        List<List<InputAndSdmField>> lls = new ArrayList<>();
        //国内遍历
        List<InputAndSdmField> sdmMatchListDomestic = doMatch(spliceDomesticList, sdmListDomestic, detailsDomestic);

        //国外遍历
        List<InputAndSdmField> sdmMatchListOverseas = doMatch(spliceOverseasList, sdmListOverseas, detailsOverseas);
        lls.add(sdmMatchListDomestic);
        lls.add(sdmMatchListOverseas);
        //写入文件
        String outputFileName = matchFieldConfig.getMatchFieldOutputFileName();
        Map<String,Object> map = new LinkedHashMap<>();
        List<Class> ls = new ArrayList<>();
        if(sdmMatchListDomestic!=null){
            map.put("国内",sdmMatchListDomestic);
            ls.add(InputAndSdmField.class);
        }
        if(sdmMatchListOverseas!=null){
            map.put("海外",sdmMatchListOverseas);
            ls.add(InputAndSdmField.class);
        }
        if(sdmMatchListDomestic!=null||sdmMatchListOverseas!=null){
            findTToMRelation.doWriteMulti(outputFileName,map,ls);
        }else{
            logger.warn("处理数据结果为空，请检查");
        }
        return lls;

    }

    public List<InputAndSdmField> doMatch(List<String> splicList,List<SdmExcelOffical>sdmListBasic,List<DemandInputTemplateDetail>details){
        List<InputAndSdmField> lsRet = new ArrayList<>();
        for(int i=0;i<splicList.size();i++){
            String spliceDomestic = splicList.get(i);
            FieldIsWarehousing fieldIsWarehousing = new FieldIsWarehousing(spliceDomestic, sdmListBasic);
            ExecutorService service = Executors.newFixedThreadPool(9);
            Future<List<SdmExcelOffical>> prime = service.submit(fieldIsWarehousing);
//            SdmExcelOffical sdmExcelOffical = new SdmExcelOffical();
            try {
                List<SdmExcelOffical> sdmList= prime.get();
                if(removeDuplicate(sdmList).size()>0){
                    sdmList = removeDuplicate(sdmList);
                }
                DemandInputTemplateDetail detailDomestic = details.get(i);
                //防止报空指针异常
                for(SdmExcelOffical sdmRet : sdmList){
                    InputAndSdmField inputAndSdmField = new InputAndSdmField();
                    inputAndSdmField.setSrcSystem( detailDomestic.getSrcSystem());
                    BeanUtils.copyProperties(detailDomestic, inputAndSdmField);
                    BeanUtils.copyProperties(sdmRet, inputAndSdmField);
                    lsRet.add(inputAndSdmField);
                }
/*                Object o = ObjectHandle.mergerData(detailDomestic, inputAndSdmField);
                InputAndSdmField inputAndSdmListDomestic = (InputAndSdmField)(ObjectHandle.mergerData(sdmExcelOffical, o));*/
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        return lsRet;
    }

    //为了去除一个源字段对应多个目标字段的情况，如参与方编号等不在考虑范围内
    public List<SdmExcelOffical> removeDuplicate(List<SdmExcelOffical> inputLs){
        if(inputLs.size()>1){
            for(int i=0; i<inputLs.size(); i++){
                String targetFieldNameCn = inputLs.get(i).getTargetFieldNameCn();
                if(technologyFieldLs().contains(targetFieldNameCn)){
                    inputLs.remove(i--);
                }
            }
        }
        return inputLs;
    }

    //生成技术字段的list
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
        retLs.add("法人机构编号");
        retLs.add("中银集团银行号");
        return retLs;
    }

}
