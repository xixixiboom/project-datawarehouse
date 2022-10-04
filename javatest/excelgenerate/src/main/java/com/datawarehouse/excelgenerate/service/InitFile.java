package com.datawarehouse.excelgenerate.service;

import com.datawarehouse.excelgenerate.config.FileNameConfig;
import com.datawarehouse.excelgenerate.generateExcuteableStatement.GenerateExcuteableStatementConfig;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;

@Service
public class InitFile {
    @Autowired
    FileNameConfig fileNameConfig;
    private static final Logger logger = LoggerFactory.getLogger(InitFile.class);


    public ArrayList<File> fileList;

    public void initFile(String fileName){
        File templateFile = new File(fileName);
        File destFile = new File("generate" + fileName);
        fileNameConfig.setDestFile(destFile);
        fileNameConfig.setTemplateFile(templateFile);
        System.out.println(fileNameConfig.getDestFile().getName());
    }

    //todo
    //轮询文件夹,实时监控，暂时不需要
    public void initPollingFolder(String dir){
        try {

            // 构造观察类主要提供要观察的文件或目录，当然还有详细信息的filter
            FileAlterationObserver observer = new FileAlterationObserver(dir, new FileListenerAdaptor.FileFilterImpl());
            // 构造收听类 没啥好说的
            FileListenerAdaptor listener = new FileListenerAdaptor();
            // 为观察对象添加收听对象
            observer.addListener(listener);
            // 配置Monitor，第一个参数单位是毫秒，是监听的间隔；第二个参数就是绑定我们之前的观察对象。
            FileAlterationMonitor fileMonitor = new FileAlterationMonitor(10000,
                    new FileAlterationObserver[] { observer });
            // 启动开始监听
            fileMonitor.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}
