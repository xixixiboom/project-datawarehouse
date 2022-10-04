package com.datawarehouse.excelgenerate.service;

import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileFilter;

//todo
//轮询文件夹，可以使用多线程进行excel处理

public class FileListenerAdaptor extends FileAlterationListenerAdaptor {

    private static final Logger logger = LoggerFactory.getLogger(FileListenerAdaptor.class);

    /**
     * File system observer started checking event.
     */
    @Override
    public void onStart(FileAlterationObserver observer) {
        // TODO Auto-generated method stub
        super.onStart(observer);
        logger.info("文件系统观察者开始检查事件");
    }

    /**
     * File system observer finished checking event.
     */
    @Override
    public void onStop(FileAlterationObserver observer) {
        // TODO Auto-generated method stub
        super.onStop(observer);
        logger.info("文件系统完成检查事件观测器");
    }

    /**
     * Directory created Event.
     */
    @Override
    public void onDirectoryCreate(File directory) {
        // TODO Auto-generated method stub
        super.onDirectoryCreate(directory);
        logger.info("目录创建事件");
    }

    /**
     * Directory changed Event
     */
    @Override
    public void onDirectoryChange(File directory) {
        // TODO Auto-generated method stub
        super.onDirectoryChange(directory);
        logger.info("目录改变事件");
    }

    /**
     * Directory deleted Event.
     */
    @Override
    public void onDirectoryDelete(File directory) {
        // TODO Auto-generated method stub
        super.onDirectoryDelete(directory);
        logger.info("目录删除事件");
    }

    /**
     * File created Event.
     */
    @Override
    public void onFileCreate(File file) {
        // TODO Auto-generated method stub
        super.onFileCreate(file);
        logger.info("文件创建事件");
        logger.info("文件名称：" + file.getName());

    }

    /**
     * File changed Event.
     */
    @Override
    public void onFileChange(File file) {
        // TODO Auto-generated method stub
        super.onFileChange(file);
        logger.info("文件改变事件");
    }

    /**
     * File deleted Event.
     */
    @Override
    public void onFileDelete(File file) {
        // TODO Auto-generated method stub
        super.onFileDelete(file);
        logger.info("文件删除事件:" + file.getName());
    }

    static final class FileFilterImpl implements FileFilter {

        /**
         * @return return true:返回所有目录下所有文件详细(包含所有子目录)
         * @return return false:返回主目录下所有文件详细(不包含所有子目录)
         */
        @Override
        public boolean accept(File file) {
            // TODO Auto-generated method stub
            logger.info("文件路径: " + file);
            logger.info("最后修改时间： " + file.lastModified());
            return true;
        }
    }
}
