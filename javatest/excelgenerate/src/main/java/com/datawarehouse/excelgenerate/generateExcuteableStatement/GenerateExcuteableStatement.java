package com.datawarehouse.excelgenerate.generateExcuteableStatement;

import com.datawarehouse.excelgenerate.BeforeEtl.BeforeEtl;
import com.datawarehouse.excelgenerate.service.InitFile;
import com.datawarehouse.excelgenerate.utils.FileHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class GenerateExcuteableStatement {
    @Autowired
    InitFile initFile;
    @Autowired
    GenerateExcuteableStatementConfig generateExcuteableStatementConfig;
    @Autowired
    BeforeEtl beforeEtl;
    private static final Logger logger = LoggerFactory.getLogger(GenerateExcuteableStatement.class);

    public void doGenerateStatement() {
        //初始化信息
        String mDmlDir = generateExcuteableStatementConfig.getMDmlDir();
        String mDdlDir = generateExcuteableStatementConfig.getMDdlDir();
//        String mIulDir= generateExcuteableStatementConfig.getMIulDir();
        String imlDate = generateExcuteableStatementConfig.getImlDate();
        String iulDate = generateExcuteableStatementConfig.getIulDate();
        String generateDir = generateExcuteableStatementConfig.getGenerateDir();

        //创建写入路径和文件名
        FileHandle.makeDir(new File(generateDir)); //如果不存在创建目录
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        Date date = new Date(System.currentTimeMillis());
        String dateStr = simpleDateFormat.format(date);
        String outputFilePath = "generateExcuteableStatement" + dateStr;
        String destDir = generateDir+File.separator+outputFilePath+".txt";
        System.out.println(destDir);
        try {
            File file = new File(destDir);
            if (!file.exists()) {
                file.createNewFile(); // 创建新文件,有同名的文件的话直接覆盖
            }
            //创建流
            FileOutputStream fos = new FileOutputStream(file, true);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            BufferedWriter bw = new BufferedWriter(osw);
            generateLocalStatement(bw);
            if(mDdlDir!=null){generateDdl(bw,mDdlDir);}
            if(mDmlDir!=null){
                generateDmlOrIul(bw,mDmlDir,imlDate,"iml");
                generateDmlOrIul(bw,mDmlDir,iulDate,"iul");
            }
//            if(mIulDir!=null){generateDmlOrIul(bw,mIulDir,iulDate);}
            osw.close();
            fos.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    public void generateDdl(BufferedWriter bw,String sourceDir){
        String[] dirFileName = FileHandle.getDirFileName(sourceDir);
        String start = "python $ETL_HOME/script/init.py iml ";
        String startTogether = start;
        try{
            //gbase 数据库表根据快照和流水区分
            bw.newLine();
            String sn="";
            String sf="";
            for(int i=0;i<dirFileName.length;i++){
                String str = dirFileName[i].substring(4,dirFileName[i].length()-4);
                String strOut = "'"+str+"',";
                if(str.contains("_5ev")){
                    sf= sf+strOut;
                }else{
                    sn = sn+strOut;
                }
            }
            if(sf.endsWith(",")){
                sf=sf.substring(0,sf.length()-1);
            }
            if(sn.endsWith(",")){
                sn=sn.substring(0,sn.length()-1);
            }
            bw.write("#sn为：\n"+sn+"\n\n");
            bw.write("#sf为：\n"+sf+"\n\n");

            //ddl
            bw.write("#ddl语句多次：");
            bw.newLine();
            for(int i=0;i<dirFileName.length;i++){
                String str = dirFileName[i].substring(4,dirFileName[i].length()-4);
                String strOut = str+" ";
                String strOneByOne = start+str;     //生成分开的语句
                bw.write(strOneByOne);
                bw.newLine();
                startTogether = startTogether+strOut;
            }
            bw.newLine();
            bw.write("#ddl语句一次：\n");

            bw.write(startTogether+"\n\n");
            bw.flush();
        }catch (IOException e){
            logger.error("#ddl语句生成出错");
            e.printStackTrace();
        }
        logger.info("写入ddl成功");
    }

    public void generateDmlOrIul(BufferedWriter bw,String sourceDir,String dateStr,String type){
        String[] dirFileName = FileHandle.getDirFileName(sourceDir);
        String start = "python $ETL_HOME/script/main.py "+dateStr+" ";
//        String type = sourceDir.substring(sourceDir.length()-3);
        try{
            bw.write("#"+type+"语句多次：");
            bw.newLine();
            for(int i=0;i<dirFileName.length;i++){
                String str="";
                if("iml".equals(type)){
                    str = dirFileName[i].substring(0,dirFileName[i].length()-4);
                }else if("iul".equals(type)){
                    str = "iul_"+dirFileName[i].substring(0,dirFileName[i].length()-9);
                }else {
                    logger.warn("路径出错");
                }
                String strOneByOne = start+str;     //生成分开的语句
                bw.write(strOneByOne+"\n");
            }
            bw.newLine();
            bw.flush();
        }catch (IOException e){
            logger.error(type+"语句生成出错");
            e.printStackTrace();
        }
        logger.info("写入"+type+"成功");
    }

    public void generateLocalStatement(BufferedWriter bw){
        LinkedHashMap<String,String> dirList = beforeEtl.getBeforeEtlConfig().getNeedBackupAndCreateDir();
        String sdmDir = dirList.get("SDM");
        String pdmDir = dirList.get("physicalModel");
        String iulDir = dirList.get("iulExcel");
        try{
            bw.write("#国内本地命令：");
            bw.newLine();
            bw.write("python2 %ETL_HOME%/script/ext/dict_metadata_transfer.py iml\n" +
                    "python2 %ETL_HOME%/script/ext/sdm_mapping_transfer.py SDM.xlsx\n" +
                    "python2 %ETL_HOME%/script/meta_init.py sys src iml\n" +
                    "python2 %ETL_HOME%/script/gen_etl_sql.py ddl iml *\n" +
                    "python2 %ETL_HOME%/script/gen_etl_sql.py dml iml *\n\n");

            bw.write("#海外本地命令：");
            bw.newLine();
            bw.write("python2 %ETL_HOME%/script/ext/dict_metadata_transfer.py iml\n" +
                    "python2 %ETL_HOME%/script/ext/sdm_mapping_transfer.py SDM_OVERSEAS.xlsx\n" +
                    "python2 %ETL_HOME%/script/meta_init.py sys src map iml    \n" +
                    "python2 %ETL_HOME%/script/gen_etl_sql.py ddl iml *\n" +
                    "python2 %ETL_HOME%/script/gen_etl_sql.py dml iml *\n");
            bw.newLine();
            //三个常用excel目录
            bw.write("#sdm目录：");
            bw.newLine();
            bw.write(sdmDir+"\n\n");
            bw.write("#pdm目录：");
            bw.newLine();
            bw.write(pdmDir+"\n\n");
            bw.write("#iul目录：");
            bw.newLine();
            bw.write(iulDir);
            bw.newLine();
        }catch (IOException e){
            logger.error("生成本地命令出错");
            e.printStackTrace();
        }

    }



}
