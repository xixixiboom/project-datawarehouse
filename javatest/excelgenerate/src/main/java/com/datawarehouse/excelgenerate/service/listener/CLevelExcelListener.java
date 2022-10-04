package com.datawarehouse.excelgenerate.service.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.fastjson.JSON;
import lombok.extern.flogger.Flogger;
import org.apache.poi.ss.formula.functions.T;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName CLevelExcelListener
 * @Description TODO
 * @Author xjy
 * @Date 2022/8/16 9:28
 * @Version 1.0
 **/
public class CLevelExcelListener extends AnalysisEventListener<LinkedHashMap<Integer, String>> {
    public static final Logger logger = LoggerFactory.getLogger(CLevelExcelListener.class);
    private List<LinkedHashMap<Integer, String>> list = new ArrayList<LinkedHashMap<Integer, String>>();

    @Override
    public void invoke(LinkedHashMap<Integer, String> data, AnalysisContext analysisContext) {
//        logger.info("解析到一条数据:{}", JSON.toJSONString(data));
//        logger.info(data.get(1));
        list.add(data);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }

    public List<LinkedHashMap<Integer, String>> retData(){
        return list;
    }

/*
    @Override
    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context){
        logger.info("解析到一条头数据:{}", JSON.toJSONString(headMap));
    }
*/

}
