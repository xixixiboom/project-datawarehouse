package com.example.exceldemo.pollingFolder;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.ResourceUtils;

import java.io.File;

@Configuration
public class FileListenerConfig {

    @Bean
    public ContextRefreshedListener contextRefreshedListener() {
        return new ContextRefreshedListener();
    }

    @Bean
    public FileListenerAdaptor fileListener(){return new FileListenerAdaptor();}

    @Bean
    public FileAlterationMonitor fileMonitor(FileListenerAdaptor fileListener) throws Exception{
        FileAlterationMonitor fileMonitor = new FileAlterationMonitor();//默认是10s监听一次

        //获取绝对路径
        Resource resource = new ClassPathResource("");
        String workPath = resource.getFile().getAbsolutePath();
        int index = workPath.indexOf("target");     //indexOf 字符串在原字符串中首次出现的位置
        workPath = workPath.substring(0,index-1)+"/src/main/resources/";

        //指定观察者监听的目录
        File file = ResourceUtils.getFile(workPath);
        //实例化观察者
        FileAlterationObserver observer = new FileAlterationObserver(file);
        observer.addListener(fileListener);
        //监视器添加观察者
        fileMonitor.addObserver(observer);
        return fileMonitor;
    }
}
