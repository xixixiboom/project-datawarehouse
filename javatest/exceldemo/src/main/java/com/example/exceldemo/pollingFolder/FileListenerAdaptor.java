package com.example.exceldemo.pollingFolder;

import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;

import java.io.File;

public class FileListenerAdaptor extends FileAlterationListenerAdaptor{
    @Override
    public void onFileCreate(File file) {
        System.out.println("[新建]:" + file.getAbsolutePath());
    }
    @Override
    public void onFileChange(File file) {
        System.out.println("[修改]:" + file.getAbsolutePath());
    }
    @Override
    public void onFileDelete(File file) {
        System.out.println("[删除]:" + file.getAbsolutePath());
    }
}