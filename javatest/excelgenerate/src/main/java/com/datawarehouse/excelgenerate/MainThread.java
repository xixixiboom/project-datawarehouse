package com.datawarehouse.excelgenerate;

import com.datawarehouse.excelgenerate.BeforeEtl.BeforeEtl;
import com.datawarehouse.excelgenerate.config.InputExcelConfig;
import com.datawarehouse.excelgenerate.config.OutputExcelConfig;
import com.datawarehouse.excelgenerate.config.UploadFileConfig;
import com.datawarehouse.excelgenerate.controller.TestController;
import com.datawarehouse.excelgenerate.generateExcuteableStatement.GenerateExcuteableStatement;
import com.datawarehouse.excelgenerate.service.*;
import com.datawarehouse.excelgenerate.utils.FileHandle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

import java.util.Scanner;

@Service
public class MainThread implements ApplicationRunner {
    @Autowired
    ReadExcel readExcel;
    @Autowired
    OutputExcelConfig outputExcelConfig;
    @Autowired
    InputExcelConfig inputExcelConfig;
    @Autowired
    InitFile initFile;
    @Autowired
    GenerateExcuteableStatement generateExcuteableStatement;
    @Autowired
    BeforeEtl beforeEtl;
    @Autowired
    SFTPUtils sftpUtils;
    @Autowired
    UploadFileConfig uploadFileConfig;
    @Autowired
    FindTableRelation findTableRelation;
    @Autowired
    FindCRelation findCRelation;
    @Autowired
    FindTToMRelation findTToMRelation;
    @Autowired
    DataLakeInfo dataLakeInfo;
    @Autowired
    MatchField matchField;
    @Autowired
    Version version;
    @Autowired
    ReduceDiameter reduceDiameter;
    @Autowired
    SinkInterface sinkInterface;
    @Autowired
    OverseasDcdsProvinceScope overseasDcdsProvinceScope;
    public void run(ApplicationArguments args) throws Exception {
//        initFile.initFile(outputExcelConfig.getOutputExcelFileName());

        while(true) {
            Scanner sc = new Scanner(System.in);
            System.out.println("生成excel-------------input：1");
            System.out.println("ETL开发前删除文件及备份--------------input：2");
            System.out.println("监听目录生成执行语句-------------input：3");
            System.out.println("上传文件至服务器------------input：4");
            System.out.println("查找表关联关系-------------input : 5");
            System.out.println("查找c层关联t和m表-------------input : 6");
            System.out.println("查找表是否入仓及对应的m表-------------input : 7");
            System.out.println("查找数据湖中表的相关信息-----------input：8");
            System.out.println("返回字段匹配是否入仓结果-----------input:9----注意输入为数仓需求模板且明细页增加一列系统标识");
            System.out.println("版本相关获取所有文件名-------input:10");
            System.out.println("生成还原口径---------input：11");
            System.out.println("生成下沉接口-----------input：12");
            System.out.println("dcds海外分省范围梳理---------input : 13");
            System.out.println("关闭程序-------------input：0");
            if(sc.hasNext()){
                String inputStr = sc.nextLine();
                int num=0;
                try{
                    num = Integer.parseInt(inputStr);
                }catch(NumberFormatException e){
                    System.out.println("输入不合法，请再次输入");
                }
                if(num ==1){
                    readExcel.doReadExcel();
                    FileHandle.renameFile(inputExcelConfig.getFileName());
                }else if(num ==2){
                    beforeEtl.doBackupAndDelete();
                }
                else if(num==3){
                    generateExcuteableStatement.doGenerateStatement();
                }
                else if(num==4){
                    sftpUtils.doUpload(uploadFileConfig.getIp(),uploadFileConfig.getUserName(),uploadFileConfig.getPassWord(),uploadFileConfig.getPort(),uploadFileConfig.getLocalAndRemoteDir());
                }
                else if(num==5){
//                    String[][] realtionExcel2 = findTableRelation.doReadRealtionExcel2();
//                    findTableRelation.getRelationTree();
//                    findTableRelation.doReadRealtionExcel();
                    findTableRelation.doSaveRelationByNeed();
//                    findTableRelation.doSaveRelation();

//                    for (String[] ele : realtionExcel2) {
//                        for (String item :
//                                ele) {
//                            System.out.println(item);
//                        }
//                    }

                }
                else if(num==6){
                    findCRelation.doFind();
                }
                else if(num==7){
                    findTToMRelation.isWarehousing();
                }
                else if (num==8){
                    dataLakeInfo.writeDataLakeInfo();
                }
                else if (num==9){
                    matchField.doWrite();
                }
                else if(num==10){
                    version.getDirList();
                }
                else if(num==11){
                    reduceDiameter.doWrite();
                }
                else if(num==12){
                    sinkInterface.doWrite();
                }else if(num ==13) overseasDcdsProvinceScope.doWrite();
                else if(num==99){
                    new TestController().contextLoads();
                }
                else if(num==0){
                    System.exit(0);
                }
            }
        }

    }
}
