package com.datawarehouse.excelgenerate.service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.datawarehouse.excelgenerate.config.FindTableRelationConfig;
import com.datawarehouse.excelgenerate.entity.JobStreamRelation;
import com.datawarehouse.excelgenerate.entity.RelationTree;
import com.datawarehouse.excelgenerate.entity.SdmExcelOffical;
import com.datawarehouse.excelgenerate.entity.WarehousingAnalysize;
import com.datawarehouse.excelgenerate.mapper.CommonMapper;
import org.apache.poi.ss.formula.functions.T;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * @ClassName FindTableRelation
 * @Description 通过读取工作流依赖excel和sdm excel 比对表记信息，获取t层表后续的m表和c表
 * @Author xjy
 * @Date 2022/8/9 22:00
 * @Version 1.0
 **/

@Service
public class FindTableRelation {

    @Autowired
    FindTableRelationConfig findTableRelationConfig;

    @Autowired
    ReadExcel readExcel;

    @Autowired
    CommonMapper mapper;

 /*   @Autowired
    RelationTree relationTree;*/

    private static final Logger logger = LoggerFactory.getLogger(FindTableRelation.class);

    /**
     * @return
     * @Description 读取入仓输入表，获取表的信息
     * @Date 2022/8/10 9:33
     * @Param
     **/
    public List<List<String>> doReadInputExcel(String excelFileName,String excelSheetName) {
        logger.info("开始读取t层输入excel");
        List<String> listDomesticTTableName = new ArrayList<>();
        List<String> listOverseasTTableName = new ArrayList<>();
        List<List<String>> listDomesticAndOverseasTTableName =new ArrayList<>();

        List<WarehousingAnalysize> ts = readExcel.doReadCommonExcel(excelFileName, excelSheetName, WarehousingAnalysize.class);
        logger.info("输入表共"+ts.size()+"张");
        List<List<WarehousingAnalysize>> listDomesticAndOverseas = judgeDomesticOrOverseas(ts); //判断表名是国内还是海外

        //获取国内和海外的表名
        for (WarehousingAnalysize t : listDomesticAndOverseas.get(0)) {
            String spliceTableName = t.getSpliceTableName().toLowerCase();
            String s = "t_"+spliceTableName;
            listDomesticTTableName.add(s);
        }
        for (WarehousingAnalysize t : listDomesticAndOverseas.get(1)) {
            String spliceTableName = t.getSpliceTableName().toLowerCase();
            String s = "t_"+spliceTableName;
            listOverseasTTableName.add(s);
        }
        listDomesticAndOverseasTTableName.add(listDomesticTTableName);
        listDomesticAndOverseasTTableName.add(listOverseasTTableName);

        return listDomesticAndOverseasTTableName;
    }

    /**
     * @Description 根据传入的list中系统名判断表名是国内还是海外
     * @Date 2022/8/14 15:19
     * @Param
     * @param
     * @return java.lang.String
     **/
    public List<List<WarehousingAnalysize>> judgeDomesticOrOverseas(List<WarehousingAnalysize> listWarehousingAnalysize ){
        List<List<WarehousingAnalysize>>ls=new ArrayList<>();
        List<WarehousingAnalysize> domestic = new ArrayList<>();
        List<WarehousingAnalysize> overseas = new ArrayList<>();
        for (WarehousingAnalysize t : listWarehousingAnalysize) {
            String systemName = t.getSourceSystemName();
            String systemNameLower = t.getSourceSystemName().toLowerCase();
            if(systemName.contains("海外")||systemNameLower.contains("-o")||systemNameLower.contains("_o")){
                overseas.add(t);
            }else if(systemName.contains("国内")||systemNameLower.endsWith("-d")||systemNameLower.endsWith("_d")){
                domestic.add(t);
            }else{
                domestic.add(t);
            }
        }
        ls.add(domestic);
        ls.add(overseas);
        logger.info("国内表共"+domestic.size()+"张,海外表共"+overseas.size()+"张");
        return ls;
    }

    /**
     * @return java.util.List<org.apache.poi.ss.formula.functions.T>
     * @Description 读取国内sdm和国外sdm，获取全量的ods层表名和对应m表名及中文名
     * @Date 2022/8/10 9:47
     * @Param
     **/
    public List<List<SdmExcelOffical>> doReadSdmExcel(String domesticSdmExcelName,String domesticSdmExcelSheetName,String overseasSdmExcelName,String overseasSdmExcelSheetName) {

        List<SdmExcelOffical>domesticExcelList=new ArrayList<>();
        List<SdmExcelOffical> overseasExcelList = new ArrayList<>();
        if(domesticSdmExcelName!=""&&domesticSdmExcelSheetName!=""){
            domesticExcelList = readExcel.doReadCommonExcel(domesticSdmExcelName, domesticSdmExcelSheetName, SdmExcelOffical.class);

        }
        if(overseasSdmExcelName!=""&&overseasSdmExcelSheetName!=""){
            overseasExcelList = readExcel.doReadCommonExcel(overseasSdmExcelName, overseasSdmExcelSheetName, SdmExcelOffical.class);

        }
        List<List<SdmExcelOffical>> listDomesticAndOverseasExcel =new ArrayList<>();
        listDomesticAndOverseasExcel.add(domesticExcelList);
        listDomesticAndOverseasExcel.add(overseasExcelList);
        logger.info("读取国内和海外sdmExcel，国内sdm 共计"+domesticExcelList.size()+"条记录，"+"海外sdm 共计"+overseasExcelList.size()+"条记录");
        return listDomesticAndOverseasExcel;
    }

    public List<SdmExcelOffical> getMLevelbyNeed(List<List<String>> listMTableName, List<SdmExcelOffical> allSdm,List<String> listTtableName){
        List<SdmExcelOffical> listSdmExcel=new ArrayList<>();
        for(int j=0;j<listMTableName.size();j++){
            for(String mTableName:listMTableName.get(j)){
                String tTableName = listTtableName.get(j).substring(2);
                for(int i=0;i<allSdm.size();i++){
                    //对m表名进行处理
                    int count =mTableName.length()-5;

                    String mTableName1=mTableName.substring(0,count).toLowerCase();
                    String sdmTableName = allSdm.get(i).getTargetTableNameEn().toLowerCase();
                    String groupNumber = allSdm.get(i).getGroupNumber().toLowerCase();
                    if(mTableName1.equals(sdmTableName)&&groupNumber.contains(tTableName)){
                        listSdmExcel.add(allSdm.get(i));
                    }
                }
            }
        }

        return listSdmExcel;
    }


    public List<T> doReadTmplateExcel() {
        String templateOutputExcelName = findTableRelationConfig.getTemplateOutputExcelName();
        String templateOutputExcelSheetName = findTableRelationConfig.getTemplateOutputExcelSheetName();
        List<T> templateOutputExcelList = readExcel.doReadCommonExcel(templateOutputExcelName, templateOutputExcelSheetName, SdmExcelOffical.class);
        return templateOutputExcelList;
    }

    /**
     * @return
     * @Description 备选项，当不从数据库读取工作流关系时，读取excel数据
     * @Date 2022/8/10 19:42
     * @Param
     **/
    public List<ArrayList<String>> doReadRealtionExcel(String relationExcelName,String relationExcelSheetName) {
        logger.info("开始读取表关联关系excel");
        List<JobStreamRelation> objects = readExcel.doReadCommonExcel(relationExcelName, relationExcelSheetName, JobStreamRelation.class);

        //将一维对象数组转为二维数组
        List<ArrayList<String>> twoDimensionalArray = new ArrayList<>();

        for (int i = 0; i < objects.size(); i++) {
            String strPostposition = objects.get(i).getJobStreamPostposition();
            String strPreposition = objects.get(i).getJobStreamPreposition();
            if (!"itl_start".equals(strPreposition)) {
                ArrayList<String> arrList = new ArrayList<>();
                String post = strPostposition.substring(19);
                String pre = strPreposition.substring(19);
/*                arrList.add(post);
                arrList.add(pre);*/
                if(!(post.startsWith("e")||pre.startsWith("e")||post.startsWith("z")||pre.startsWith("z"))||post.startsWith("a")||pre.startsWith("a")){
                    arrList.add(post);
                    arrList.add(pre);
                    twoDimensionalArray.add(arrList);
                }
            }
        }
/*        for(List<String> s:twoDimensionalArray){
            System.out.println(s);
        }*/
        logger.info("通过excel读取工作流关系"+twoDimensionalArray.size()+"个");
//        System.out.println(twoDimensionalArray);
//        getRelationTree(twoDimensionalArray);
        return twoDimensionalArray;
    }

    /**
     * @return void
     * @Description 读取数据库获取作业流依赖关系
     * @Date 2022/8/10 13:49
     * @Param
     **/
    public void getJobStreamRelationByMysql() {
        //读取数据库中数据
        logger.info("开始读取数据库");
        String sql = "select t2_n.f_name as postposition,t1_n.f_name as preposition from jcm_xjobpreprocess x left join jcm_flow t1_n on x." +
                "f_prexjobid = t1_n.f_id left join jcm_flow t2_n on x.f_xjobid = t2_n.f_id where t2_n.f_name like 'edwd0_etl_flow%' order by t2_n.f_name";

        List<LinkedHashMap<String,String>> maps = mapper.queryByTableName(sql);
        List<ArrayList<String>> twoDimensionalArray = new ArrayList<>();
        logger.info("数据库中共有" + maps.size() + "条记录");

        //转换为二维数组
        for (int i = 0; i < maps.size(); i++) {
            String strPreposition = (String) maps.get(i).get("preposition");
            String strPostposition = (String) maps.get(i).get("postposition");
            //二维数组中第一列为后置，第二列为前置
            if (!"itl_start".equals(strPreposition)) {
                twoDimensionalArray.get(i).add(strPreposition.substring(19));
                twoDimensionalArray.get(i).add(strPostposition.substring(19));
            }
        }


    }

    /**
     * @Description 得到全量树的根节点数组 通过作业流excel
     * @Date 2022/8/11 16:49
     * @Param
     * @return void
     **/
    public List<String> getRootListByExcel(List<ArrayList<String>> twoDimensionalArray){
        int arraySize = twoDimensionalArray.size();
        List<String> removeDuplication = new ArrayList<>();  //去重
        for (int j = 0; j < arraySize; j++) {
            String s = twoDimensionalArray.get(j).get(1);
            String sSub = s.substring(0, 1);

            if (sSub.equals("t") && !removeDuplication.contains(s)) {
                removeDuplication.add(s);
            }
        }
        int removeDuplicationSize = removeDuplication.size();
        for (String ls:removeDuplication) {
            System.out.println(ls);
        }
        logger.info("共有t层表" + removeDuplicationSize + "张");
        return removeDuplication;
    }

    /**
     * @param twoDimensionalArray
     * @return void
     * @Description 数据来源可以是excel，也可以是数据库，得到树 数组，树保存一个t层表对应的各个表
     * @Date 2022/8/10 20:36
     * @Param   removeDuplication inputTableNameList 注意是输入的表，输入多少个表就生成多少个树，即数组
     * @Param   twoDimensionalArray 工作流前后关系来源，可以来自于数据库，也可以来自于jobStreamRelation excel
     **/
    public List<RelationTree> getRelationTree(List<String>removeDuplication,List<ArrayList<String>> twoDimensionalArray) {
        List<RelationTree> relationTreesList = new ArrayList<>();
        int removeDuplicationSize = removeDuplication.size();
//        System.out.println(twoDimensionalArray);
        //从t层构建树
        for (int k = 0; k < removeDuplicationSize; k++) {
            try{
                String tTableName = removeDuplication.get(k);
                //Runnable实现，没有返回值
/*            RelationTree<String> relationTree = new RelationTree<>();
//            RelationTree relationTreeAfter = pushElementInTree(tTableName, twoDimensionalArray, relationTree);
            Thread thread = new Thread(new GenerateTree(tTableName, twoDimensionalArray, relationTree));
//            thread.setName("第"+k+"个树");
            logger.info("第"+k+"个树构建成功");
//            relationTreesList.add(relationTreeAfter);
//            logger.info("构建第" + k + "个树成功");*/

                RelationTree<String> relationTree = new RelationTree<>();
                GenerateTree generateTree = new GenerateTree(tTableName, twoDimensionalArray, relationTree);
/*                //callable方式1
                //执行Callable方式，需要FutureTask实现类的支持，用于接收运算结果 FutureTask是Future接口的实现类
                FutureTask<RelationTree> futureTask = new FutureTask<RelationTree>(generateTree);
                //开启线程
                new Thread(futureTask).start();//当线程执行完毕后才能获得 线程执行结果
                //获取线程call方法返回值（接收线程运算后的结果）
                RelationTree relationTree1 = futureTask.get();
//                System.out.println(relationTree1);*/

                //callable方式2
                //为了提高效率 也可以使用service方式调用
                ExecutorService service = Executors.newFixedThreadPool(9);
                Future<RelationTree> prime1 = service.submit(generateTree);
                RelationTree relationTree1 = prime1.get();
                relationTreesList.add(relationTree1);
/*                System.out.println(tTableName);
                System.out.println(relationTree1.getGrandChild(tTableName));*/
//                System.out.println(JSON.toJSONString(relationTree1));
                int count= k+1;
                logger.info("构建第" + count + "个树成功");
            }catch (InterruptedException e1) {
                logger.error("构造树失败");
                e1.printStackTrace();
            }catch(ExecutionException e2){
                logger.error("构造树失败");
                e2.printStackTrace();
            }

        }
        return relationTreesList;
    }






    /**
     * @Description 将对象数组转换为二维数组
     * @Date 2022/8/11 16:02
     * @Param
     * @param inputTableList 去重后的数组，下标要与relationTreeList的下标保持一致，才能保证inputTableList里的元素是relationTreeList里的树的节点
     * @return java.util.List<java.util.List<java.lang.String>>
     **/
    public List<List<String>> getAllNode(List<RelationTree> relationTreeList,List<String> inputTableList){
        List<List<String>> listSecond = new ArrayList<>();


        for(int i=0;i<relationTreeList.size();i++){
            RelationTree relationTree = relationTreeList.get(i);
            //找一个节点
            String s = inputTableList.get(i);
            //输出全部去重后的表名
  /*
            //得到全部节点
            List<String> strList = relationTree.getGrandChild(s);
            //去重
            List<String> collect = strList.stream().distinct().collect(Collectors.toList());

            List<String> temp = new ArrayList<>();
            temp.add(s);
            temp.addAll(collect);
//            System.out.println(strList);
            listSecond.add(temp);*/

            //输出对应关系

            List list = relationTree.retList();
            list.add(0,s);
            listSecond.add(list);
        }
        return listSecond;
    }

    /**
     * @Description 从树数组和tTableName数组过滤数据返回所有m层表名
     * @Date 2022/8/13 21:38
     * @Param
     * @param
     * @return retMLevelTableName 返回所有m层表名
     **/
    public List<List<String>> retMLevelTableName(List<RelationTree> relationTreeList,List<String> inputTableList){

        List<List<String>> twoDimensionalArrayMtableName = new ArrayList<>();
        for(int i=0;i<relationTreeList.size();i++){
            List<String> listMtableName = new ArrayList<>();
            RelationTree relationTree = relationTreeList.get(i);
            //找一个节点
            String s = inputTableList.get(i);
            //输出全部去重后的表名
            //得到全部节点
            List<String> strList = relationTree.getGrandChild(s);
            //去重
            List<String> collect = strList.stream().distinct().collect(Collectors.toList());
            for(String tableName:collect){
                if(tableName.startsWith("m")){
                    listMtableName.add(tableName);
                }
            }
            twoDimensionalArrayMtableName.add(listMtableName);
        }
        return twoDimensionalArrayMtableName;
    }



    /**
     * @Description 将工作流关系写入excel
     * @Date 2022/8/11 16:07
     * @Param
     * @param dataList
     * @return void
     **/
    public void doWriteRelationExcel(String fileName,List<List<String>> dataList){
        ExcelWriter writer = EasyExcel.write(fileName).build();
        try{
            WriteSheet writeSheet1 = EasyExcel.writerSheet(0, "表级关系").build();
            writer.write(dataList, writeSheet1);
//        EasyExcel.write(fileName).sheet("sdm模型").doWrite(dataList);
            logger.info("写入excel"+fileName+"成功");

        }finally {
            // 千万别忘记close 会帮忙关闭流
            if (writer != null) {
                writer.close();
            }
        }

    }

    public void doWriteRelationExcel(String fileName,List<List<String>> dataList,List<SdmExcelOffical> sdmList){
        ExcelWriter writer = EasyExcel.write(fileName).build();
        try{

//        WriteSheet writeSheet = EasyExcel.writerSheet(0, "模板").head(DemoData.class).build();
            WriteSheet writeSheet1 = EasyExcel.writerSheet(0, "表级关系").build();

            WriteSheet writeSheet2 = EasyExcel.writerSheet(1, "sdm模型").head(SdmExcelOffical.class).build();

            writer.write(dataList, writeSheet1);
            writer.write(sdmList, writeSheet2);

            logger.info("写入excel"+fileName+"成功");
        } finally {
            // 千万别忘记close 会帮忙关闭流
            if (writer != null) {
                writer.close();
            }
        }
    }

/*    //输入数组参数来自于本身，即找到所有t表，报错内存溢出
    public void doSaveRelation(){
        List<ArrayList<String>> twoDimensionalArray = doReadRealtionExcel(); //读excel获取对象数组,
        List<String> rootListByExcel = getRootListByExcel(twoDimensionalArray);  //通过jobStreamRelationExcel 获取根节点数组
//        List<String> inputTableList = doReadInputExcel();   //输入数组
        List<RelationTree> relationTrees = getRelationTree(rootListByExcel,twoDimensionalArray); //通过对象数组构造成树数组，替换rootListByExcel 会造成内存溢出
        List<List<String>> allNode = getAllNode(relationTrees,rootListByExcel); //将树数组转换为二维数组
        doWriteRelationExcel(allNode);
    }*/

    //外来excel输入
    public void doSaveRelationByNeed(){
        String domesticRelationExcelName = findTableRelationConfig.getDomesticRelationExcelName();
        String domesticRelationExcelSheetName = findTableRelationConfig.getDomesticRelationExcelSheetName();
        String overseasRelationExcelName = findTableRelationConfig.getOverseasRelationExcelName();
        String overseasRelationExcelSheetName = findTableRelationConfig.getOverseasRelationExcelSheetName();
        String inputExcelFileName = findTableRelationConfig.getWarehousingExcelName();
        String inputExcelSheetName = findTableRelationConfig.getWarehousingExcelSheetName();
        String domesticSdmExcelName = findTableRelationConfig.getDomesticSdmExcelName();
        String domesticSdmExcelSheetName = findTableRelationConfig.getDomesticSdmExcelSheetName();
        String overseasSdmExcelName = findTableRelationConfig.getOverseasSdmExcelName();
        String overseasSdmExcelSheetName = findTableRelationConfig.getOverseasSdmExcelSheetName();
        List<List<String>> listDomesticAndOverseasTTableName = doReadInputExcel(inputExcelFileName,inputExcelSheetName);   //输入数组
        List<String> listDomesticTTableName = listDomesticAndOverseasTTableName.get(0);
        List<String> listOverseasTTableName = listDomesticAndOverseasTTableName.get(1);
        List<List<SdmExcelOffical>> listDomesticAndOverseasExcel  = doReadSdmExcel(domesticSdmExcelName,domesticSdmExcelSheetName,overseasSdmExcelName,overseasSdmExcelSheetName);
        //生成国内数据
        if(domesticRelationExcelName!=""&&domesticRelationExcelSheetName!=""&&listDomesticTTableName.size()!=0){
            List<ArrayList<String>> twoDimensionalArrayDomestic = doReadRealtionExcel(domesticRelationExcelName,domesticRelationExcelSheetName); //读excel获取国内对象数组,
            List<RelationTree> relationTrees = getRelationTree(listDomesticTTableName,twoDimensionalArrayDomestic);
            List<List<String>> allNode = getAllNode(relationTrees,listDomesticTTableName); //将树数组转换为二维数组
            //生成sdm数组
            List<List<String>> mLevelTableNameDomestic = retMLevelTableName(relationTrees, listDomesticTTableName);   //获取需要的m层表名

            if(findTableRelationConfig.getDomesticSdmExcelName()!=""&&findTableRelationConfig.getDomesticSdmExcelSheetName()!=""){
                List<SdmExcelOffical> listDomesticSdmExcel = doReadSdmExcel(domesticSdmExcelName,domesticSdmExcelSheetName,overseasSdmExcelName,overseasSdmExcelSheetName).get(0); //读国内全量sdmExcel
                List<SdmExcelOffical> DomesticMLevelTableNamebyNeed = getMLevelbyNeed(mLevelTableNameDomestic, listDomesticSdmExcel,listDomesticTTableName);//根据需要的m层表名筛选sdmExcel
                doWriteRelationExcel(findTableRelationConfig.getDomesticOutputExcelName(),allNode,DomesticMLevelTableNamebyNeed);
            }else{
                doWriteRelationExcel(findTableRelationConfig.getDomesticOutputExcelNameWithoutSdm(),allNode);
            }

        }
/*        //生成表级关系数组
        List<ArrayList<String>> twoDimensionalArrayDomestic = doReadRealtionExcel(); //读excel获取国内对象数组,
//        List<String> rootListByExcel = getRootListByExcel(twoDimensionalArray);  //通过jobStreamRelationExcel 获取根节点数组


        List<RelationTree> relationTrees = getRelationTree(listDomesticTTableName,twoDimensionalArray);
        List<List<String>> allNode = getAllNode(relationTrees,inputTableList); //将树数组转换为二维数组

        //生成sdm数组
        List<String> mLevelTableName = retMLevelTableName(relationTrees, inputTableList);   //获取需要的m层表名
        List<SdmExcel> listSdmExcel = doReadSdmExcel(); //读全量sdmExcel
        List<SdmExcel> mLevelTableNamebyNeed = getMLevelbyNeed(mLevelTableName, listSdmExcel);//根据需要的m层表名筛选sdmExcel
        doWriteRelationExcel(allNode,mLevelTableNamebyNeed);*/

        //生成海外数据
        if(overseasRelationExcelName!=null&&overseasRelationExcelSheetName!=null&&listOverseasTTableName.size()!=0){
            List<ArrayList<String>> twoDimensionalArrayOverseas = doReadRealtionExcel(overseasRelationExcelName,overseasRelationExcelSheetName); //读excel获取国内对象数组,
            List<RelationTree> relationTrees = getRelationTree(listOverseasTTableName,twoDimensionalArrayOverseas);
            List<List<String>> allNode = getAllNode(relationTrees,listOverseasTTableName); //将树数组转换为二维数组

            //生成sdm数组
            List<List<String>> mLevelTableNameOverseas = retMLevelTableName(relationTrees, listOverseasTTableName);   //获取需要的m层表名

            if(findTableRelationConfig.getOverseasSdmExcelName()!=""&&findTableRelationConfig.getOverseasSdmExcelSheetName()!=""){

                List<SdmExcelOffical> listOverseasSdmExcel = listDomesticAndOverseasExcel.get(1); //读全量sdmExcel
                List<SdmExcelOffical> overseasMLevelTableNamebyNeed = getMLevelbyNeed(mLevelTableNameOverseas, listOverseasSdmExcel,listOverseasTTableName);//根据需要的m层表名筛选sdmExcel
                doWriteRelationExcel(findTableRelationConfig.getOverseasOutputExcelName(),allNode,overseasMLevelTableNamebyNeed);
            }else{
                doWriteRelationExcel(findTableRelationConfig.getOverseasOutputExcelNameWithoutSdm(),allNode);
            }
        }
    }

/**
 * @Description BY LIYONG
 * @Date 2022/8/11 12:56
 * @Param
 * @return void
 **/
/*    public void getRelationTree() {
        String[][] matrix = doReadRealtionExcel2();
*/
/*        ArrayList<String> list = new ArrayList<>();
        for (String[] item : matrix) {
            if (item[1].startsWith("t") && !list.contains(item[1])){
                list.add(item[1]);
            }
        }*/
/*
        List<String> strList = doReadInputExcel();
        RelationTree<String> relationTree = new RelationTree<>();
        ArrayList<RelationTree<String>> trees = new ArrayList<>();
        for (String tTableName : strList) {
            RelationTree tree = test(tTableName, matrix, relationTree);
            trees.add(tree);
*//*            System.out.println(tTableName);
            System.out.println(tree.getGrandChild(tTableName));*//*
        }
    }*/

   /* public RelationTree test(String tTableName, String[][] matrix, RelationTree<String> relationTree) {
        if (isChild(tTableName, matrix)) {
            return relationTree;
        }

        for (String[] item : matrix) {
            if (tTableName.equals(item[1])){
                relationTree.add(tTableName, item[0]);
                test(item[0], matrix, relationTree);
            }
        }

        return relationTree;
    }

    private boolean isChild(String tableName, String[][] matrix) {
        for (String[] item : matrix) {
            if (item[1].equals(tableName)) {
                return false;
            }
        }
        return true;
    }


    public String[][] doReadRealtionExcel2() {
        logger.info("开始读取表关联关系excel");
        String relationExcelName = findTableRelationConfig.getRelationExcelName();
        String relationExcelSheetName = findTableRelationConfig.getRelationExcelSheetName();
        List<JobStreamRelation> objects = readExcel.doReadCommonExcel(relationExcelName, relationExcelSheetName, JobStreamRelation.class);

        int num = 0;
        for (int i = 0; i < objects.size(); i++) {
            JobStreamRelation item = objects.get(i);
            if (!item.getJobStreamPreposition().equals("itl_start")) {
                num++;
            }
        }

        String[][] matrix = new String[num][2];
        int index = 0;
        for (JobStreamRelation item : objects) {
            if (!item.getJobStreamPreposition().equals("itl_start")) {
                matrix[index][0] = item.getJobStreamPostposition().substring(19);
                matrix[index][1] = item.getJobStreamPreposition().substring(19);
                index++;
            }
        }

        return matrix;
    }*/


}