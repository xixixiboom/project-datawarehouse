package com.example.exceldemo.pollingFolder;

import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

public class ContextRefreshedListener implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    FileAlterationMonitor monitor;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        try {
            //开始监听
            monitor.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
