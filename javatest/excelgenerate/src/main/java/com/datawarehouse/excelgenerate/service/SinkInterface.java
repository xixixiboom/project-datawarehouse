package com.datawarehouse.excelgenerate.service;

import com.alibaba.fastjson.JSON;
import com.datawarehouse.excelgenerate.config.SinkInterfaceConfig;
import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName SinkInterface
 * @Description 下沉接口
 * @Author xjy
 * @Date 2022/10/10 21:35
 * @Version 1.0
 **/
@Service
public class SinkInterface {
    @Autowired
    ReadExcel readExcel;
    @Autowired
    SinkInterfaceConfig sinkInterfaceConfig;
    @Autowired
    WriteExcel writeExcel;
    private static final Logger LOGGER = LoggerFactory.getLogger(SinkInterface.class);

    public void doWrite() {
        List<LinkedHashMap<Integer, String>> cPdmChangeRecordMap = initCPdmChangeRecord();
        List<LinkedHashMap<Integer, String>> cPdmFieldLsMap = initCPdmFieldInfo();
        String s = JSON.toJSONString(cPdmChangeRecordMap);

        //从修改记录中获取并去重变更表名
        List<String> meetTimeCPdmTableLs = filterCPdmChangeRecord(cPdmChangeRecordMap);
        List<String> cPdmTableLsDistinct = removeDuplicate(meetTimeCPdmTableLs);


        //变更记录
        List<List> changeContextLs = getChangeContext(cPdmTableLsDistinct, cPdmChangeRecordMap);
        List<List<String>> changeRecordLls = convertToSinkChangeRecord(cPdmTableLsDistinct, cPdmChangeRecordMap, changeContextLs);
        //变更表结构数据 todo
        List<List<String>> changeTableDetails = getChangeTableDetails(cPdmTableLsDistinct, cPdmFieldLsMap,changeContextLs);
        //写入sheet
        writeMulti(changeTableDetails,changeRecordLls);
    }

    public List<List<String>> convertMapToList(List<LinkedHashMap<Integer, String>> map){
        List<List<String>> result = new ArrayList<List<String>>();

        for(LinkedHashMap<Integer, String> m:map){
            List<String> ls = new ArrayList<String>();
            for(Integer s:m.keySet()){
                ls.add(m.get(s));
            }
            result.add(ls);
        }
        return result;
    }

    public void writeMulti(List<List<String>>changeTableDetails,List<List<String>>changeRecordLls){
        Map<String,List<List<String>>> map = new LinkedHashMap<>();
        map.put("变更表结构",changeTableDetails);
        map.put("变更记录新增",changeRecordLls);
        String outputFileName=sinkInterfaceConfig.getOutputFileName();
        writeExcel.writeCommon(outputFileName,map);
    }
    //将c pdm数据转换成变更表结构sheet的数据    changeContextLs存着[【字段名，变更时间】]
    public List<String> cPdmToChangeTableStructure(LinkedHashMap<Integer,String> inputLs,Map<String,Integer> countLs,List<List<String>> changeContextLs){
        List<String> retLs = new ArrayList<String>();
        retLs.add("up");
        retLs.add("edw");
        retLs.add("inner");
        retLs.add("d0");
        //层级   eg icl
        retLs.add(inputLs.get(1).toLowerCase());
        //表名
        retLs.add(inputLs.get(3).toLowerCase());
        //表中文名
        retLs.add(inputLs.get(5));
        //表类型
/*        String inputTableType=inputLs.get(11).toLowerCase();
        String type="";
        if(inputTableType.equals("ev")) type="流水表";
        if(inputTableType.equals("st")) type="拉链表";
        if(inputTableType.equals("sn")) type="快照表";
        retLs.add(type);*/
//        retLs.add("快照表");
        retLs.add(null);
        //字段顺序
        retLs.add(inputLs.get(8));
        //字段名
        retLs.add(inputLs.get(4).toUpperCase());
        //字段类型
        retLs.add(inputLs.get(9).toUpperCase());
        //字段中文名
        retLs.add(inputLs.get(6));
        //主键
        retLs.add(inputLs.get(7));
        //下发频率
        retLs.add("日");
        //字段个数
        Integer count = countLs.get(inputLs.get(3).toLowerCase());
        retLs.add(count.toString());
        //是否机构编号
        /*if(inputLs.get(6).contains("机构号")||inputLs.get(6).contains("机构编号")){
            retLs.add("1");
        }else{
            retLs.add("0");
        }*/
        if(inputLs.get(23)==null){
            retLs.add("0");
        }else if(inputLs.get(23).equals("1")||inputLs.get(23).equals("是")){
            retLs.add("1");
        }else{
            retLs.add("0");
        }
        //脱敏类型
        retLs.add(inputLs.get(22));
        //所属任务
        retLs.add(null);
        //变更记录
        retLs.add(inputLs.get(19));
        //是否可为空 //
        String primaryKey = inputLs.get(7);
        if(primaryKey==null || primaryKey.equals("N")||primaryKey.equals("0")){
            retLs.add("Y");
        }else if(primaryKey.equals("Y")||primaryKey.equals("是")||primaryKey.equals("1")){
            retLs.add("N");
        }else{
            retLs.add("Y");
        }
        //数据格式说明
        retLs.add(null);
        //生效日期
        retLs.add("D+1");
        //投产批次
        retLs.add(null);
        //变更日期
/*        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String format = sdf.format(new Date());
        retLs.add(format);*/
        for(List<String> ls:changeContextLs){
            if(ls.get(0)==null) continue;
//            System.out.println(ls.get(0) +"..."+inputLs.get(4));
            if(ls.get(0).toUpperCase().equals(inputLs.get(4).toUpperCase())){
                retLs.add(convertTime(ls.get(1)));
                break;
            }

        }
        return retLs;
    }

    public List<LinkedHashMap<Integer, String>> initCPdmChangeRecord() {
        String cPdmFileName = sinkInterfaceConfig.getCPdmFileName();
        String cPdmSheetChangeRecord = sinkInterfaceConfig.getCPdmSheetChangeRecord();
        if (!cPdmFileName.equals("") && !cPdmSheetChangeRecord.equals("")) {
            List<LinkedHashMap<Integer, String>> retLls = readExcel.doReadCommonExcel(cPdmFileName, cPdmSheetChangeRecord);
            return retLls;
        }
        return null;
    }



    /**
     * @Description 匹配字段让变更时间只出现在变更的字段上
     * @Date 2022/11/3 20:48
     * @Param changeTableLs 所有变更记录去重过滤日期后的表名，即所有目标变更的表名
     * @param allCPdmLls 字段信息中所有表结构数据
     * @return changeLs 记录了变更sheet页的数据，是一个二维数组，    // lls:[字段英文名，变更时间] recordInfo:[lls]  change:[String] 每张表的变更记录
     **/

    //写入新的list
    public List<List<String>> getChangeTableDetails(List<String> changeTableLs, List<LinkedHashMap<Integer,String>> allCPdmLls,List<List>changeLs) {
        List<List<String>> retLls = new ArrayList<>();
        //记录表的字段数
        Map<String,Integer> countLs =new LinkedHashMap<>();
        List<List<List<String>>> changeContextLs = changeLs.get(1);

        for(String tableName : changeTableLs){
            int count =0;
            for (LinkedHashMap<Integer,String> cDetail : allCPdmLls) {
                String s = cDetail.get(3);
                if (tableName.toUpperCase().equals(s.toUpperCase())) {
                    count++;
                    countLs.put(tableName.toLowerCase(),count);
                }
            }
        }
        String countSout = "";
        for(String key:countLs.keySet()) {
            countSout += countLs.get(key)+";   ";
        }
        LOGGER.info("各表字段数分别为 "+countSout);
        for (int i=0; i<changeTableLs.size(); i++) {
            List<List<String>> lls = changeContextLs.get(i);
//            System.out.println(lls);
            String tableName = changeTableLs.get(i);
            System.out.println(tableName);
            for (LinkedHashMap<Integer,String> cDetail : allCPdmLls) {

                String s = cDetail.get(3);
                if (tableName.toUpperCase().equals(s.toUpperCase())) {
                    //在一张表内匹配
                    List<String> sinkTableStructure = cPdmToChangeTableStructure(cDetail,countLs,lls);
                    retLls.add(sinkTableStructure);
                }
            }
        }

        return retLls;
    }


    public List<LinkedHashMap<Integer, String>> initCPdmFieldInfo() {
        String cPdmFileName = sinkInterfaceConfig.getCPdmFileName();
        String cPdmSheetFieldInfo = sinkInterfaceConfig.getCPdmSheetFieldInfo();
        if (!cPdmFileName.equals("") && !cPdmSheetFieldInfo.equals("")) {
            List<LinkedHashMap<Integer, String>> retLls = readExcel.doReadCommonExcel(cPdmFileName, cPdmSheetFieldInfo);
            return retLls;
        }
        return null;
    }

    //根据时间过滤修改记录中的表，获得本次变更的表名
    public List<String> filterCPdmChangeRecord(List<LinkedHashMap<Integer,String>> inputLls) {
        List<String> retLs = new ArrayList<String>();
        String upperTime = sinkInterfaceConfig.getCPdmChangeTimeUpperLimit();
        String lowerTime = sinkInterfaceConfig.getCPdmChangeTimeLowerLimit();
        LOGGER.info("本次筛选表变更时间中\t起始时间："+lowerTime+"\t结束时间："+upperTime);
        for (LinkedHashMap<Integer,String> ls : inputLls) {
            String time = ls.get(11);
            String cTableName = ls.get(3);
            if (time == null) {
                LOGGER.error("修改记录中修改日期不能为空");
                return null;
            }
            if (cTableName == null) {
                LOGGER.error("修改记录中表名不能为空");
                return null;
            }
            cTableName = cTableName.toUpperCase();
            //如果有一个时间为空则不过滤
            if(upperTime.equals("")||lowerTime.equals("")){
                retLs.add(cTableName);
            } else if (upperTime.equals(lowerTime)) {
                if (time.equals(upperTime)) {
                    retLs.add(cTableName);
                }
            } else {
                if (compareDate(time, upperTime) != 1 && compareDate(lowerTime, time) != 1) {
                    retLs.add(cTableName);
                }
            }
        }
        return retLs;
    }

    public List<String> removeDuplicate(List<String> inputLs) {
        List<String> collect = inputLs.stream().distinct().collect(Collectors.toList());
        LOGGER.info("本次修改表" + collect.size() + "张");
        LOGGER.info("分别为"+collect);
        return collect;
    }

    //获取每张表的变更内容,并转化成变更内容数据  [[],[[[]]]]
    // lls:[字段英文名，变更时间] recordInfo:[lls]  change:[String] 每张表的变更记录
    public List<List> getChangeContext(List<String> tableList, List<LinkedHashMap<Integer,String>> inputLls) {
        String upperTime = sinkInterfaceConfig.getCPdmChangeTimeUpperLimit();
        String lowerTime = sinkInterfaceConfig.getCPdmChangeTimeLowerLimit();
        List<String> change = new ArrayList<>();
        //每张表各字段的（字段英文名，变更时间）
        List<List<List<String>>> recordInfo= new ArrayList<>();
        List<List> ret = new ArrayList<>();
        for (String tableName : tableList) {
//            Set<String> changeT = changeTypeMap.get(tableName);
            Map<String,String> tempMap = new HashMap<String,String>();
            List<List<String>> lls = new ArrayList<>();
            int count =0;

            for (LinkedHashMap<Integer,String> inputLs : inputLls) {

                String inputTableName = inputLs.get(3);
                String fieldNameEn = inputLs.get(4);
                String changeType=inputLs.get(13);
                String srcValue = inputLs.get(14);
                String destValue = inputLs.get(15);
                String changeTime = inputLs.get(11);
                //获取变更记录
                //表英文名相同，修改时间在范围内，则获取
                if(tableName.toUpperCase().equals(inputTableName.toUpperCase())&&compareDate(changeTime, upperTime) != 1 && compareDate(lowerTime, changeTime) != 1) {
                    count++;
                    List<String> ls = new ArrayList<>();
                    String ss=";";
                    if(srcValue!=null){
                        ss=":"+srcValue.toUpperCase()+"->"+destValue.toUpperCase()+";   ";
                    }
                    if(!tempMap.containsKey(changeType)){
                        tempMap.put(changeType,fieldNameEn+ss);
                    }else{
                        String key = tempMap.get(changeType);
                        if(fieldNameEn!=null) fieldNameEn=fieldNameEn.toUpperCase();
                        key =key+fieldNameEn+ss;
                        tempMap.put(changeType,key);
                    }

                    //record 获取变更时间，取字段英文名是为了后面匹配
                    ls.add(fieldNameEn);
                    ls.add(changeTime);
                    lls.add(ls);
                }
            }
            recordInfo.add(lls);
//            System.out.println(recordInfo+"111111111111111111111111111111111111111111");

            String tempS = "";
            for(String key : tempMap.keySet()){
                tempS = tempS+key+":\n"+tempMap.get(key)+"\n";
            }
            change.add(tempS);

//            System.out.println(tempS+"22222222222222222222222222222222222");
        }
        ret.add(change);
        ret.add(recordInfo);

        //记录修改记录条数
        String countRecord = " ";
        for(List<List<String>> record:recordInfo){
            countRecord+=record.size()+";   ";
        }
        LOGGER.info("各修改记录条数分别为"+countRecord);
        return ret;
    }


    //将日期   2022/9/1 -> 20220901
    public String convertTime(String inputTime){
        String[] split = inputTime.split("/");
        String s0 = split[0];
        String s1 = split[1];
        String s2 = split[2];
        if(s1.length()==1) s1 ="0"+s1;
        if(s2.length()==1) s2 ="0"+s2;
        return s0+s1+s2;
    }
    //变更记录的list  表名list 和每张表的更改内容list
    public List<List<String>> convertToSinkChangeRecord(List<String> tableList, List<LinkedHashMap<Integer,String>> inputLls,List<List> changeLs){
        List<List<String>> retLls = new ArrayList<List<String>>();
        List<String> changeContextLs = changeLs.get(0);
        for(int i=0;i<tableList.size();i++){
            String tableName = tableList.get(i);
            List<String> tempLs = new ArrayList<String>();
            for (LinkedHashMap<Integer,String> inputLs : inputLls) {
                String inputTableName = inputLs.get(3);
                if(tableName.toUpperCase().equals(inputTableName.toUpperCase())){
                    tempLs.add("up");
                    tempLs.add("d0");
                    //表名
                    tempLs.add(inputTableName);
                    //表中文名
                    tempLs.add(inputLs.get(5));
                    //是否本批次新增
                    tempLs.add(null);
                    //变动
                    tempLs.add(changeContextLs.get(i));
                    //下发周期
                    tempLs.add("日");
                    //表类型
                    tempLs.add(null);
                    //使用类型
                    tempLs.add(null);
                    //用数方
                    tempLs.add("无");
                    //是否访问湖表
                    tempLs.add("否");
                    //访问端
                    tempLs.add(null);
                    //所属任务
                    tempLs.add(null);
                    //问题单号
                    tempLs.add(null);
                    //层级
                    Character tempSSS = tableName.toCharArray()[1];
                    tempLs.add(tempSSS.toString());
                    //是否下传P6
                    tempLs.add(null);
                    //增量/全量
                    tempLs.add(null);
                    //区域标识
                    tempLs.add("全辖一份-2");
                    //时点时段标识
                    tempLs.add("时点");
                    //投产批次
                    tempLs.add(null);
                    //分省字段
                    tempLs.add("无");
                    //数据量是否大于1000万条
                    tempLs.add("是");
                    //组件
                    tempLs.add(null);
                    //变更日期
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                    String format = sdf.format(new Date());
                    tempLs.add(format);
                    //下数日期
                    tempLs.add("D+1");
                    break;
                }
            }
            retLls.add(tempLs);
        }
        return retLls;
    }

    public Integer compareDate(String date1, String date2) {
        if (date1.equals(date2)) return 0;
        if(date1.contains("/")&&date2.contains("/")) {
            String[] d1 = date1.split("/");
            String[] d2 = date2.split("/");
            for(int i = 0; i < d1.length; i++) {
                Integer int1 = Integer.parseInt(d1[i]);
                Integer int2 = Integer.parseInt(d2[i]);
                int i1 =int1-int2;
                if(i1>0) return 1;
                else if(i1<0) return -1;
            }
        }else{
            DateFormat df = new SimpleDateFormat("yyyy/MM/dd");
            try {
                if (df.parse(date1).getTime() < df.parse(date2).getTime()) return -1;
                else return 1;
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }


}
