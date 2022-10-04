package com.datawarehouse.excelgenerate.service;

import com.alibaba.excel.EasyExcel;
//import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.ExcelWriter;
//import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.WriteWorkbook;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.datawarehouse.excelgenerate.config.InputExcelConfig;
import com.datawarehouse.excelgenerate.config.OutputExcelConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

@Service
public class WriteExcel {
/*    public String fileName;

    public String sheetName;
    public List<SdmExcel> data;*/
    public File templateFile;
    public File destFile;

    public OutputExcelConfig outputExcelConfig;
/*    public WriteExcel(OutputExcelConfig outputExcelConfig){
        this.destFile = outputExcelConfig.getDestFile();
        this.templateFile=outputExcelConfig.getTemplateFile();
    }*/
    private static final Logger logger = LoggerFactory.getLogger(WriteExcel.class);
    public void writeCommon(String fileName,String sheetName,List<List<String>> data){
        ExcelWriter excelWriter=new ExcelWriter(new WriteWorkbook());
        try{
            File templateFile = new File(fileName);
            File destFile = new File("gen_"+fileName);
            WriteSheet writeSheet = EasyExcel.writerSheet(sheetName).build();
            if(templateFile.exists()){
                excelWriter = EasyExcel.write(templateFile).withTemplate(templateFile)
                        //.file() 指定目标文件，不能与模板文件是同一个文件
                        .file(destFile).autoCloseStream(false).build();
                /*EasyExcel.write(fileName).withTemplate(fileName).sheet(sheetName).doWrite(data);*/
            }else{
                excelWriter = EasyExcel.write(templateFile).build();
            }
            excelWriter.write(data, writeSheet);
            logger.info("写入 "+fileName+" 成功");
        }catch(Exception e){
            logger.error("写入 "+fileName+" 失败");
            e.printStackTrace();
        }finally {
            // 千万别忘记finish 会帮忙关闭流
            if (excelWriter != null) {
                excelWriter.finish();
            }
        }
    }
    public void writeCommon(String fileName,String sheetName,List<List<String>> data,List<List<String>>headList){
        ExcelWriter excelWriter = EasyExcel.write(fileName).registerWriteHandler(new LongestMatchColumnWidthStyleStrategy()).build();
        try{
            File destFile = new File(fileName);
            WriteSheet writeSheet = EasyExcel.writerSheet(sheetName).head(headList).build();
            excelWriter.write(data,writeSheet);
            logger.info("写入 "+fileName+" 成功");
        }catch(Exception e){
            logger.error("写入 "+fileName+" 失败");
            e.printStackTrace();
        }finally {
            // 千万别忘记finish 会帮忙关闭流
            if (excelWriter != null) {
                excelWriter.finish();
            }
        }
    }
    public <U>void addWrite(String fileName,String sheetName,List<U> data){
        File templateFile = new File(fileName);
        File destFile = new File("gen_"+fileName);
/*        outputExcelConfig.setTemplateFile(templateFile);
        outputExcelConfig.setDestFile(destFile);*/
        ExcelWriter excelWriter=new ExcelWriter(new WriteWorkbook());
        try {
            Class<?> aClass = data.get(0).getClass();
            if (templateFile.exists()) {
                //追加数据，目标文件与原始文件不能是同一个文件名
                //withTemplate()指定模板文件

                excelWriter = EasyExcel.write(templateFile).withTemplate(templateFile)
                        //.file() 指定目标文件，不能与模板文件是同一个文件
                        .file(destFile).autoCloseStream(false).build();
            }
            else {
                excelWriter = EasyExcel.write(destFile,aClass )
                        .build();
            }
            WriteSheet writeSheet = EasyExcel.writerSheet(sheetName)
                    .build();
            excelWriter.write(data, writeSheet);
        }catch(Exception e){
            System.out.println("===========写入出错，请检查原excel或目标路径是否有同名目标文件============");
            e.getMessage();
        }

        finally {
            // 千万别忘记finish 会帮忙关闭流
            if (excelWriter != null) {
                excelWriter.finish();
            }
        }
    }
}
