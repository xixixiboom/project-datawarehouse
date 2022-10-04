package com.datawarehouse.excelgenerate.controller;

import com.alibaba.excel.EasyExcel;
import com.datawarehouse.excelgenerate.service.listener.CLevelExcelListener;
import org.springframework.stereotype.Controller;

/**
 * @ClassName TestController
 * @Description TODO
 * @Author xjy
 * @Date 2022/8/16 14:36
 * @Version 1.0
 **/
@Controller
public class TestController {

    public void contextLoads() {
        String fileName = "SDMcea合并.xlsm";
        // 这里 只要，然后读取第一个sheet 同步读取会自动finish
        EasyExcel.read(fileName, new CLevelExcelListener()).sheet(5).doRead();
    }
}
