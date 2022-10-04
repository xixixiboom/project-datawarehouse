package com.datawarehouse.excelgenerate.service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.datawarehouse.excelgenerate.config.*;
import com.datawarehouse.excelgenerate.entity.*;
import com.datawarehouse.excelgenerate.service.listener.CommonExcelListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReadExcel {
    @Autowired
    InputExcelConfig inputExcelConfig;

    @Autowired
    FileNameConfig fileNameConfig;

    @Autowired
    OutputExcelConfig outputExcelConfig;


    @Autowired
    TimeConfig timeConfig;

    @Autowired
    FindTableRelationConfig findTableRelationConfig;
    private static final Logger logger = LoggerFactory.getLogger(ReadExcel.class);

    public Class judgeExcel(String fileName){
        Class o=FieldAnalyseExcel.class;
        if(fileName.contains("字段级分析")){
            o= FieldAnalyseExcel.class;
        }else if(fileName.contains("传统批量文本")){
            o=DiipExcel.class;
        }else if(fileName.contains("表结构")){
            o= DcdsExcel.class;
        }else if(fileName.contains("SDM")&&(!fileName.contains("gen"))){
            o= SdmExcel.class;
        }else if(fileName.contains("PDM")&&(!fileName.contains("gen"))){
            o= PdmExcel.class;
        }
        return o;
    }
    
    public void doReadExcel() {

        timeConfig.setStart(System.currentTimeMillis());
        System.out.println("============================================================开始读取 Excel=========================================================================");
        String excelFileName = inputExcelConfig.getFileName();
        String excelSheetName = inputExcelConfig.getSheetName();
        List<String> dcdsExcelConfigTableNameEn = inputExcelConfig.getTableNameEn();
        List<String> dcdsExcelConfigTableNameCn = inputExcelConfig.getTableNameCn();

        System.out.println("Excel文件名为：  " + excelFileName);
        System.out.println("Sheet为： " + excelSheetName);
        System.out.println("TableNameEn 为：  " + dcdsExcelConfigTableNameEn);
        System.out.println("TableNameCn 为：  " + dcdsExcelConfigTableNameCn);
        ExcelReader excelReader = null;
        int headRowCount=1;
        Class o = judgeExcel(excelFileName);
        if(o.getSimpleName().contains("Sdm")||o.getSimpleName().contains("Pdm")){
            headRowCount=2;
        }
        try {
            excelReader = EasyExcel.read(excelFileName, o,
                    new ExcelListener(inputExcelConfig, outputExcelConfig, timeConfig)).headRowNumber(headRowCount).build();
            // 构建一个sheet 这里可以指定名字或者no
            ReadSheet readSheet = EasyExcel.readSheet(excelSheetName).build();
            // 读取一个sheet
            excelReader.read(readSheet);
        }catch(Exception e){
            System.out.println("=====error=====读取字段信息失败，请检查yml文件信息并检查原文件是否存在======");
            e.printStackTrace();
        }
        finally{
            if (excelReader != null) {
                // 这里千万别忘记关闭，读的时候会创建临时文件，到时磁盘会崩的
                excelReader.close();
            }
        }

        ExcelReader excelReaderChange = null;
        String excelSheetNameChange = inputExcelConfig.getChangeSheetName();


        if(o.getSimpleName().contains("Pdm")){
            System.out.println("==============================================================开始读取Pdm变更记录============================================================================");
//            outputExcelConfig.setOtherSheetName("true");   //判断是否是pdm的第二个sheet页
            try {
                excelReaderChange = EasyExcel.read(excelFileName, ChangeRecord.class,
                        new ExcelListener(inputExcelConfig, outputExcelConfig, timeConfig)).build();
                // 构建一个sheet 这里可以指定名字或者no
                ReadSheet readSheetChange = EasyExcel.readSheet(excelSheetNameChange).build();
                // 读取一个sheet
                excelReaderChange.read(readSheetChange);
            }catch(Exception e){
                logger.error("读取变更信息失败，请检查yml文件信息并检查原文件是否存在");
                e.printStackTrace();
            }
            finally{
                if (excelReader != null) {
                    // 这里千万别忘记关闭，读的时候会创建临时文件，到时磁盘会崩的
                    excelReader.close();

                }
                outputExcelConfig.setOtherSheetName("false");
            }
        }

    }

    /**
     * @Description 读取公共的excel
     * @Date 2022/8/9 16:30
     * @Param
     * @return void
     **/
    public <T>List<T> doReadCommonExcel(String excelFileName, String excelSheetName, Class className){
        ExcelReader excelReader = null;
        List<T> list = null;
        try {

            CommonExcelListener commonExcelListener = new CommonExcelListener();
            excelReader = EasyExcel.read(excelFileName,className,
                    commonExcelListener).build();
            // 构建一个sheet 这里可以指定名字或者no
            ReadSheet readSheet = EasyExcel.readSheet(excelSheetName).build();
            if(className.getSimpleName().equals("SdmExcelOffical")){
                readSheet = EasyExcel.readSheet(excelSheetName).headRowNumber(2).build();
            }
            if(className.getSimpleName().equals("DemandInputTemplateDetail")){
                readSheet = EasyExcel.readSheet(excelSheetName).headRowNumber(3).build();
            }
            // 读取一个sheet
            excelReader.read(readSheet);
            list = commonExcelListener.getDatas();

        }catch(Exception e){
           logger.error("=====error=====读取字段信息失败，请检查yml文件信息并检查原文件是否存在======");
            e.printStackTrace();
        }
        finally{
            if (excelReader != null) {
                // 这里千万别忘记关闭，读的时候会创建临时文件，到时磁盘会崩的
                excelReader.close();
            }
        }
        if(list == null){
            logger.error("读取EXCEL失败");
        }
        return list;
    }


}
