package com.datawarehouse.excelgenerate.service;

import com.datawarehouse.excelgenerate.config.DataLakeInfoConfig;
import com.datawarehouse.excelgenerate.entity.DataLakeTable;
import com.datawarehouse.excelgenerate.mapper.CommonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.Callable;


/**
 * @ClassName GetDomesticOdsInfo
 * @Description TODO 待优化项：for循环中执行查询数据库操作会频繁开闭链接，导致执行效率低，可以在查询时直接返回所有结果后处理
 * @Author xjy
 * @Date 2022/9/17 23:57
 * @Version 1.0
 **/
@Service
public class GetDomesticOdsInfo implements Callable {


    private static final Logger logger = LoggerFactory.getLogger(GetDomesticOdsInfo.class);
    private String spliceTableName;
    private CommonMapper commonMapper;
    private DataLakeInfoConfig dataLakeInfoConfig;
    public GetDomesticOdsInfo(){

    }
    public GetDomesticOdsInfo(String spliceTableName,CommonMapper commonMapper,DataLakeInfoConfig dataLakeInfoConfig){
        this.dataLakeInfoConfig=dataLakeInfoConfig;
        this.spliceTableName=spliceTableName;
        this.commonMapper=commonMapper;
    }

    public DataLakeTable getOdsInfoDomestic() {

        //初始化信息
        String dataLowerLimit = dataLakeInfoConfig.getDataLowerLimit();
        String dataUpperLimit = dataLakeInfoConfig.getDataUpperLimit();
        int dataSizeStandardValue = dataLakeInfoConfig.getDataSizeStandardValue();


        //查ods表名国内
        String sqlGetOdsTableName = "select distinct ods_table from ods_hive_schema where ods_table regexp 'ods_" + spliceTableName + "_d[0-9]_[i|f|z]_[d|m|y]' order by ods_table;";
        List<LinkedHashMap<String, String>> mapLsGetOdsTableName = commonMapper.queryByTableName(sqlGetOdsTableName);

        //存国内
        DataLakeTable dataLakeTable =new DataLakeTable();
        spliceTableName = spliceTableName.trim();
        dataLakeTable.setSpliceTableName(spliceTableName);
        if(mapLsGetOdsTableName==null||mapLsGetOdsTableName.isEmpty()){
            logger.info("[]");
            return dataLakeTable;
        }
        String odsTableNameEn = mapLsGetOdsTableName.get(0).get("ods_table");
        for(int j=0;j<mapLsGetOdsTableName.size();j++){
            logger.info(mapLsGetOdsTableName.get(j).get("ods_table"));
        }

        dataLakeTable.setLakeTableNameEn(odsTableNameEn);
        //查ods表中文名
        int index = spliceTableName.indexOf("_");
        String systemID = spliceTableName.substring(0,index);
        String tableNameEn = spliceTableName.substring(index+1);

        String sqlGetTableNameCn = "select distinct table_name,table_cn_name from stg_tb_schema_info where sys_id='" + systemID + "' and table_name ='" + tableNameEn + "';";
        List<LinkedHashMap<String, String>> mapLsGetTableNameCn = commonMapper.queryByTableName(sqlGetTableNameCn);
        if(mapLsGetTableNameCn==null||mapLsGetTableNameCn.isEmpty()){
            logger.info("[]");
            return dataLakeTable;
        }
        dataLakeTable.setLakeTableNameCn(mapLsGetTableNameCn.get(0).get("table_cn_name"));
        //有snapshot就加上
        if (mapLsGetOdsTableName.size() > 1) {
            String snapshotTableName = mapLsGetOdsTableName.get(1).get("ods_table");
//                logger.info("snapshot表名"+snapshotTableName);
            dataLakeTable.setLakeSnapshotTableNameEn(snapshotTableName);
            List<String> dateList = isHasData(snapshotTableName, dataUpperLimit, dataLowerLimit, dataSizeStandardValue);
            String timeRange = getTimeRange(snapshotTableName, dateList);
            dataLakeTable.setTimeRange(timeRange);
        }else {
            List<String> dateList = isHasData(odsTableNameEn, dataUpperLimit, dataLowerLimit, dataSizeStandardValue);
            String timeRange = getTimeRange(odsTableNameEn, dateList);
            dataLakeTable.setTimeRange(timeRange);
        }

        dataLakeTable.setAreaRange("总部+江苏+上海");

        //获取当天年月日
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        String nowDate = dateFormat.format(date);
        dataLakeTable.setRegisterTime(nowDate);

        return dataLakeTable;
    }

    public List<String> isHasData(String odsTableName, String dataUpperLimit, String dataLowerLimit, int dataSizeStandardValue) {
        List<String> dateList = new ArrayList<String>();
        String sqlGetDataSize = "select ods_table,data_date,inc_data_count from ods_statistics where ods_table = '" + odsTableName + "' and data_date>='"
                + dataLowerLimit + "' and data_date<='" + dataUpperLimit + "' and inc_data_count>='" + dataSizeStandardValue + "' order by data_date desc;";
        List<LinkedHashMap<String, String>> mapLsGetDataSize = commonMapper.queryByTableName(sqlGetDataSize);
        int mapSize = mapLsGetDataSize.size();
        if (mapSize >= 3) {
            for (int i = 0; i < 3; i++) {
                dateList.add(mapLsGetDataSize.get(i).get("data_date"));
            }
        } else if (mapSize < 3) {
            for (int i = 0; i < mapSize; i++) {
                dateList.add(mapLsGetDataSize.get(i).get("data_date"));
            }
        }
        return dateList;
    }

    public String getTimeRange(String odsTableName, List<String> dateList) {
        String timeRange = null;
        if(dateList.size()==3){
            String date0 = dateList.get(0);
            String date1 = dateList.get(1);
            String date2 = dateList.get(2);

            if (odsTableName.endsWith("_snapshot")) {
                timeRange = date2+"截面全量+"+date1+"增量+"+date0+"增量";
            } else if (odsTableName.endsWith("_d0_f_d")){
                timeRange = date2+"全量+"+date1+"全量+"+date0+"全量";
            } else if (odsTableName.endsWith("_d0_i_d")){
                timeRange = date2+"增量+"+date1+"增量+"+date0+"增量";
            } else if (odsTableName.endsWith("_d0_z_d")){
                timeRange = date2+"闭链+"+date1+"开链+"+date0+"开链";
            } else if (odsTableName.endsWith("_d0_f_m")){
                timeRange = date2+"全量+"+date1+"全量+";
            } else if (odsTableName.endsWith("_d0_i_m")){
                timeRange = date2+"增量+"+date1+"增量+";
            } else if (odsTableName.endsWith("_d0_f_y")){
                timeRange = date2.substring(0,3)+"年数据";
            }
        } else if (dateList.size()==1){
            timeRange = dateList.get(0);
        } else if (dateList.size()==2){
            timeRange = dateList.get(0)+"+"+dateList.get(1);
        }
        return timeRange;
    }

    @Override
    public DataLakeTable call() {
        DataLakeTable dataLakeTable =new DataLakeTable();
        try{
            dataLakeTable= getOdsInfoDomestic();
        } catch(Exception e) {
            logger.error("读取数据库失败");
            e.printStackTrace();
        }
        return dataLakeTable;
    }
}
