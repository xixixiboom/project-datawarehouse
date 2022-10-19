package com.datawarehouse.excelgenerate.service.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName CommonExcelLIstenerWithoutClass
 * @Description TODO
 * @Author xjy
 * @Date 2022/10/10 22:03
 * @Version 1.0
 **/
public class CommonExcelListenerWithoutClass extends AnalysisEventListener<List<String>> {
    private List<List<String>> dataInputList = new ArrayList<>();
    /*@Override
    public void invoke(List<String> t, AnalysisContext analysisContext) {
        dataInputList.add(t);
    }*/

    @Override
    public void invoke(List<String> o, AnalysisContext analysisContext) {
        dataInputList.add(o);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
    }

    public List<List<String>> getData(){
        return dataInputList;
    }
}
