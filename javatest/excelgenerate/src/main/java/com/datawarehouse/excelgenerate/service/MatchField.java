package com.datawarehouse.excelgenerate.service;

import com.datawarehouse.excelgenerate.config.MatchFieldConfig;
import com.datawarehouse.excelgenerate.entity.*;
import com.datawarehouse.excelgenerate.entity.reduceDiameter.InputAndSdmField;
import com.datawarehouse.excelgenerate.entity.reduceDiameter.StandardDataExcel;
import com.datawarehouse.excelgenerate.mapper.CommonMapper;
import com.datawarehouse.excelgenerate.utils.FileHandle;
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
import java.util.stream.Collectors;

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
        inputFileName= FileHandle.mergeDirAndFile(inputFileName);
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

    //初始化落标excel
    public List<List<StandardDataExcel>> initStandardDataExcel(){
        List<StandardDataExcel> retLsDomestic = new ArrayList<>();
        List<StandardDataExcel> retLsOverseas = new ArrayList<>();
        List<List<StandardDataExcel>> retLLs = new ArrayList<>();
        String domesticFileName=matchFieldConfig.getDomesticStandardDataFileName();
        String domesticSheetName = matchFieldConfig.getDomesticStandardDataSheetName();
        String overseasFileName = matchFieldConfig.getOverseasStandardDataFileName();
        String overseasStandardDataSheetName = matchFieldConfig.getOverseasStandardDataSheetName();
        if(!domesticFileName.equals("")&&!domesticSheetName.equals("")){
            retLsDomestic = readExcel.doReadCommonExcel(domesticFileName,domesticSheetName,StandardDataExcel.class);
            if(retLsDomestic==null) logger.error("国内落标excel读取到0条数据");
            logger.info("读取"+domesticFileName+"成功，"+"共计"+retLsDomestic.size()+"条数据");
        }
        if(!overseasFileName.equals("")&&!overseasStandardDataSheetName.equals("")){
            retLsOverseas = readExcel.doReadCommonExcel(overseasFileName,overseasStandardDataSheetName,StandardDataExcel.class);
            if(retLsOverseas==null) logger.error("海外落标excel读取到0条数据");
            logger.info("读取"+overseasFileName+"成功，"+"共计"+retLsOverseas.size()+"条数据");
        }
        retLLs.add(retLsDomestic);
        retLLs.add(retLsOverseas);
        return retLLs;
    }

    public List<List<DemandInputTemplateDetail>> judgeDomesticOrOverseas(List<DemandInputTemplateDetail> listDetail){
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
        logger.info("国内字段共"+domestic.size()+"条记录,海外字段共"+overseas.size()+"条记录");
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

    /**
     * @Description 20221202增加去重逻辑，将系统名+字段名 相同的值去重，根据变更日期排序，留下变更日期最小的值。
     * @Date 2022/12/2 14:12
     * @Param
     * @return
     **/
    public List<List<DemandInputTemplateDetail>> removeDuplicateField(List<DemandInputTemplateDetail> details){
        Map<String,DemandInputTemplateDetail> map=new LinkedHashMap<>();
        List<List<DemandInputTemplateDetail>> retLLs = new ArrayList<>();
        //存放被去重的行
        List<DemandInputTemplateDetail> duplicateFieldLs = new ArrayList<>();
        for(DemandInputTemplateDetail detail:details){
            String spliceTableAndField = detail.getSrcSystemFlag()+"_"+detail.getSrcTableNameEn()+"_"+detail.getSrcFieldNameEn();
            spliceTableAndField=spliceTableAndField.toUpperCase();
            String updateTime = detail.getChangeRecord();
            if(!map.containsKey(spliceTableAndField)){
                map.put(spliceTableAndField,detail);
            }else{
                DemandInputTemplateDetail tmpDm = map.get(spliceTableAndField);
                String tmpUpdateTime = tmpDm.getChangeRecord();
                if(compareTime(updateTime,tmpUpdateTime)){
                    duplicateFieldLs.add(detail);
                }else{
                    duplicateFieldLs.add(tmpDm);
                    map.put(spliceTableAndField,detail);
                }
            }
        }
        List<DemandInputTemplateDetail> retLs =new ArrayList<DemandInputTemplateDetail>();
        for(String s:map.keySet()){
            retLs.add(map.get(s));
        }
        retLLs.add(retLs);
        retLLs.add(duplicateFieldLs);
        return retLLs;
    }

    public Boolean compareTime(String t1,String t2){
        if(t1==null) t1="10000000新增";
        if(t2==null) t2="10000000新增";
        String t1Part = t1.substring(0,8);
        String t2Part = t2.substring(0,8);
        int t1Int = Integer.parseInt(t1Part);
        int t2Int = Integer.parseInt(t2Part);
        return t1Int>t2Int;
    }
    public List<List<InputAndSdmField>> doWrite(List<String> revisionRecord){
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
        //20221202增加去重逻辑，将系统名+字段名 相同的值去重，根据变更日期排序，留下变更日期最小的值。
        List<List<DemandInputTemplateDetail>> removeListDo = removeDuplicateField(detailsDomestic);
        detailsDomestic=removeListDo.get(0);
        List<DemandInputTemplateDetail> duplicateDo = removeListDo.get(1);
        List<List<DemandInputTemplateDetail>> removeListOv = removeDuplicateField(detailsOverseas);
        detailsOverseas=removeListOv.get(0);
        List<DemandInputTemplateDetail> duplicateOv = removeListOv.get(1);

        //输入文件获取拼接str
        List<String> spliceDomesticList = getSpliceTableAndFieldNameList(detailsDomestic);
        List<String> spliceOverseasList = getSpliceTableAndFieldNameList(detailsOverseas);

        //落标excel
        List<List<StandardDataExcel>> llsStandard = initStandardDataExcel();
        List<StandardDataExcel> standardLsDomestic = llsStandard.get(0);
        List<StandardDataExcel> standardLsOverseas = llsStandard.get(1);

        List<List<InputAndSdmField>> lls = new ArrayList<>();
        //国内遍历
        List<InputAndSdmField> sdmMatchListDomestic = doMatch(spliceDomesticList, sdmListDomestic, detailsDomestic,standardLsDomestic);

        //国外遍历
        List<InputAndSdmField> sdmMatchListOverseas = doMatch(spliceOverseasList, sdmListOverseas, detailsOverseas,standardLsOverseas);
        lls.add(sdmMatchListDomestic);
        lls.add(sdmMatchListOverseas);
        //写入文件
        String outputFileName = matchFieldConfig.getMatchFieldOutputFileName();
        Map<String,Object> map = new LinkedHashMap<>();
        List<Class> ls = new ArrayList<>();
        if(sdmMatchListDomestic.size()!=0){
            map.put("国内",sdmMatchListDomestic);
            ls.add(InputAndSdmField.class);
        }
        if(sdmMatchListOverseas.size()!=0){
            map.put("海外",sdmMatchListOverseas);
            ls.add(InputAndSdmField.class);
        }
        String s1 = "国内所需字段0个";
        if(detailsDomestic.size()!=0){
            map.put("国内所需字段",detailsDomestic);
            s1 = "国内所需字段"+detailsDomestic.size()+"个";
            ls.add(DemandInputTemplateDetail.class);
        }
        logger.info(s1);
        String s2 = "海外所需字段0个";
        if(detailsOverseas.size()!=0){
            map.put("海外所需字段",detailsOverseas);
            s2="海外所需字段"+detailsOverseas.size()+"个";
            ls.add(DemandInputTemplateDetail.class);
        }
        logger.info(s2);
        String s3 = "国内重复字段0个";
        if(duplicateDo.size()!=0){
            map.put("国内重复字段",duplicateDo);
            s3="国内重复字段"+duplicateDo.size()+"个";
            ls.add(DemandInputTemplateDetail.class);
        }
        logger.info(s3);
        String s4 = "海外重复字段0个";
        if(duplicateOv.size()!=0){
            map.put("海外重复字段",duplicateOv);
            s4="海外重复字段"+duplicateOv.size()+"个";
            ls.add(DemandInputTemplateDetail.class);
        }
        logger.info(s4);
        revisionRecord.add(s1+","+s2+","+s3+","+s4);
        if(sdmMatchListDomestic.size()!=0||sdmMatchListOverseas.size()!=0){
            findTToMRelation.doWriteMulti(outputFileName,map,ls);
        }else{
            logger.warn("处理数据结果为空，请检查");
        }
        return lls;
    }

    public List<InputAndSdmField> doMatch(List<String> spliceList,List<SdmExcelOffical>sdmListBasic,List<DemandInputTemplateDetail>details,List<StandardDataExcel> standardList){
        List<InputAndSdmField> lsRet = new ArrayList<>();
        for(int i=0;i<spliceList.size();i++){
            String spliceDomestic = spliceList.get(i);
            DemandInputTemplateDetail detailDomestic = details.get(i);
            FieldIsWarehousing fieldIsWarehousing = new FieldIsWarehousing(spliceDomestic, sdmListBasic,detailDomestic,standardList);
            ExecutorService service = Executors.newFixedThreadPool(9);
            Future<List<InputAndSdmField>> prime = service.submit(fieldIsWarehousing);
//            SdmExcelOffical sdmExcelOffical = new SdmExcelOffical();
            try {
                List<InputAndSdmField> inputAndSdmFieldList= prime.get();
                lsRet.addAll(inputAndSdmFieldList);
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

    //生成技术字段的list


}
