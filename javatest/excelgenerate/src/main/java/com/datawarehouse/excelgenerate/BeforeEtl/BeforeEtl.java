package com.datawarehouse.excelgenerate.BeforeEtl;

import com.datawarehouse.excelgenerate.utils.FileHandle;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Data
public class BeforeEtl {
    @Autowired
    BeforeEtlConfig beforeEtlConfig;

    private static final Logger logger = LoggerFactory.getLogger(BeforeEtl.class);

    public void emptyFile(){
        List<String> emptyDir = beforeEtlConfig.getEmptyDir();
        for(String s:emptyDir){
            FileHandle.emptyAllFiles(s);
        }
    }

    public void doBackupAndDelete(){
        copyFile();
        emptyFile();
    }

    //返回此次备份目录名，以当前日期为目录名
    public String createBackupDir(){
        String targetBackupDir = beforeEtlConfig.getTargetBackupDir();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        Date date = new Date(System.currentTimeMillis());
        String dirDate =simpleDateFormat.format(date);
        String targetDir = targetBackupDir+File.separator+dirDate;
        logger.info("备份文件存放目录为："+targetDir);
        FileHandle.createDirRelative(targetBackupDir,dirDate);
        return targetDir;
    }

    public void copyFile(){
        //备份map内目录
        String targetDir=createBackupDir();
        LinkedHashMap<String, String> needBackupAndCreateDir = beforeEtlConfig.getNeedBackupAndCreateDir();
        Iterator iterator  = needBackupAndCreateDir.keySet().iterator();
        //key：备份目录相对目录  value：待备份目录绝对目录
        while(iterator.hasNext()){
            Object key =iterator.next();
            String value = needBackupAndCreateDir.get(key);
            String destDir = FileHandle.createDirRelative(targetDir, (String) key);

            File fileValue = new File(value);
            File fileDestDir = new File(destDir);
            FileHandle.copyFileAndDir(fileValue,fileDestDir);
            logger.info("目录："+value+"备份成功");
        }

        //备份待删除目录
        List<String> emptyDir = beforeEtlConfig.getEmptyDir();
        for(String s:emptyDir){
            String[] split = s.split("\\\\");
            String dir="";
            if(s.endsWith("iul")){
                dir = "iul";
            }else{
                dir = split[split.length-2];
            }
            String destDir = FileHandle.createDirRelative(targetDir, dir);//获取目标绝对路径
            File fileS = new File(s);
            File fileDestDir = new File(destDir);
            FileHandle.copyFileAndDir(fileS,fileDestDir);
        }
    }






}
