package com.datawarehouse.excelgenerate.service.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import org.apache.poi.ss.formula.functions.T;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName CommonExcelListener
 * @Description TODO
 * @Author xjy
 * @Date 2022/8/9 21:41
 * @Version 1.0
 **/
public class CommonExcelListener<T> extends AnalysisEventListener<T> {
    private List<T> dataInputList = new ArrayList<T>();
    @Override
    public void invoke(T t, AnalysisContext analysisContext) {
        dataInputList.add(t);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
    }

    public List<T> getDatas(){
        return dataInputList;
    }
}
