package com.datawarehouse.excelgenerate.controller;

import com.datawarehouse.excelgenerate.config.InputExcelConfig;
import com.datawarehouse.excelgenerate.config.OutputExcelConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;




@Controller
public class SelectFile {
    @Autowired
    InputExcelConfig inputExcelConfig;

    @Autowired
    OutputExcelConfig outputExcelConfig;




}
