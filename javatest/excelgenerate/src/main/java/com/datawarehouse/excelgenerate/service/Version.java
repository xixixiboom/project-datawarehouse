package com.datawarehouse.excelgenerate.service;

import com.datawarehouse.excelgenerate.config.VersionConfig;
import com.datawarehouse.excelgenerate.utils.FileHandle;
import org.apache.commons.compress.utils.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName Version
 * @Description TODO
 * @Author xjy
 * @Date 2022/9/24 19:34
 * @Version 1.0
 **/
@Service
public class Version {
    @Autowired
    VersionConfig versionConfig;
    @Autowired
    WriteExcel writeExcel;
    private static final Logger logger = LoggerFactory.getLogger(Version.class);
    public void getDirList(){
        String dir = versionConfig.getDestDir();
        String outputFileName = versionConfig.getOutputFileName();
        String outputSheetName = versionConfig.getOutputSheetName();
        List<String> fileType = versionConfig.getFileType();
        List<List<String>> allDirPath = FileHandle.getAllDirPath(dir, fileType);
        List<List<String>> headList = getHeadList();
        writeExcel.writeCommon(outputFileName,outputSheetName,allDirPath,headList);

    }
    public List<List<String>> getHeadList(){
        List<List<String>> lls = new ArrayList<List<String>>();

        List<String> ls1 = new ArrayList<>();
        List<String> ls2 = new ArrayList<>();
        ls1.add("绝对路径");
        ls2.add("文件名");
        lls.add(ls1);
        lls.add(ls2);
        return lls;
    }
}
