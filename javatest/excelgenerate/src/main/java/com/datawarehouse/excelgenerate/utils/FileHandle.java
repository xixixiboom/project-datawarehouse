package com.datawarehouse.excelgenerate.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.font.TrueTypeFont;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class FileHandle {
//    public static void renameFile(File destFile, File templateFile){
    //todo
    //不能renameTo文件，可能原因为Listener未结束，一直占用两个文件
    // 尝试将new File()调到ReadExcel前面，报could not close io错误，定位为writeExcel中finally中关闭流

    private static final Logger logger = LoggerFactory.getLogger(FileHandle.class);
    public static void renameFile(String fileName) {
        File templateFile = new File(fileName);
        File destFile = new File("generate" + fileName);
        if (destFile.exists()) {
            //删除原模板文件，新生成的文件变成新的模板文件
            templateFile.delete();
            try {
                Boolean bool = destFile.renameTo(templateFile);
                System.out.println(bool);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public static void fileNameSortByName(List<File> list){
        Collections.sort(list,new Comparator<File>(){
            @Override
            public int compare(File o1,File o2){
                if(o1.isDirectory()&&o2.isFile()){
                    return 1;
                }
                if(o1.isFile()&&o2.isDirectory()){
                    return -1;
                }
                return o1.getName().compareTo(o2.getName());
            }
        });

    }

    //返回File[]
    public static File[] returnFileArray(String dir,String operationType ){

        File file = new File(dir);
        if(!file.exists()){
            logger.error(dir+"待"+operationType+"目录："+dir+"不存在,请检查配置文件");
            System.exit(0);     //终止程序
        }
        else {
            if (!file.isDirectory()) {
                logger.error("配置文件中" + dir + "不是目录，请检查");
                System.exit(0);     //终止程序
            }
            return file.listFiles();
        }
        return file.listFiles();
    }

    //必须是目录否则退出程序
    public static void isDirectory(File file){
//        File file = new File(dir);
        if(!file.exists()){
            logger.error("目录"+file.getAbsolutePath()+"不存在,请检查配置文件");
            System.exit(0);     //终止程序
        }
        else {
            if (!file.isDirectory()) {
                logger.error("配置文件中" + file.getName() + "不是文件夹，请检查");
                System.exit(0);     //终止程序
            }
        }
    }

    //如果没有该目录则创建
    public static void makeDir(File file){
//        File file = new File(dir);
        if(!file.exists()){
            createDirAbsolute(file);
        }
        else {
            return;
        }
    }

    public static boolean isDirExist(File file){
        if(file.exists()){
            return true;
        }
        return false;
    }

    // 创建目录（拼接目录） 返回绝对路径
    public static String createDirRelative(String targetBackupDir,String relativeDir){
        File file = new File(targetBackupDir);
        FileHandle.isDirectory(file);   //不终止程序代表是目录
        String targetAbsoluteDir = targetBackupDir+File.separator+relativeDir;
//        logger.info("创建目录为："+targetAbsoluteDir);
        File targetDir = new File(targetAbsoluteDir);
        if(!FileHandle.isDirExist(targetDir)){
            Boolean createDirFlag = targetDir.mkdir();
            if(!createDirFlag){
                logger.error("目录"+targetDir+"创建失败");
            }
        }
        return targetAbsoluteDir;
    }

    // 创建目录
    public static void createDirAbsolute(File file){
//        FileHandle.isDirectory(file);   //不终止程序代表是目录
        if(!FileHandle.isDirExist(file)){
            Boolean createDirFlag = file.mkdir();
            if(!createDirFlag){
                logger.error(file.getAbsolutePath()+"目录创建失败");
            }
        }
    }

    // 复制目录下所有文件(只复制文件，不复制文件夹）
    public static void copyFile(String srcDir,String destDir){
        File srcFile = new File(srcDir);
        FileHandle.isDirectory(srcFile);    //原目录必须存在，否则中止程序
        File destFile = new File(destDir);
        FileHandle.makeDir(destFile);       //确保目标目录存在

        File[] files=FileHandle.returnFileArray(srcDir,"");
        try{
            for(File f:files){
                if(f.isFile()){
                    String strDir = srcDir+File.separator+f.getName();
                    Files.copy(Paths.get(strDir), Paths.get(destDir));
                }
            }
        }catch(IOException e){
            logger.error("目录"+srcDir+"文件备份失败,请检查是否有程序占用文件");
            e.printStackTrace();
        }
        logger.info("目录"+srcDir+"文件备份成功");
    }


    public static void copyFileAndDir(File srcDir,File decDir) {
        File[] files = srcDir.listFiles();//获取要复制文件夹所有内容
        if (files == null) return;//同样排除无权限的目录
        for (File file : files) {//遍历文件数组
            if (file.isFile()) {//如果是文件,直接复制
                try{
                    String strDest = decDir + File.separator + file.getName();
                    String strSrc = file.getAbsolutePath();

                    Files.copy(Paths.get(strSrc),Paths.get(strDest));
                }catch (IOException e){
                    logger.error("备份文件失败");
                    e.printStackTrace();
                }
            } else {//如果是文件夹
                //先创建目标目录下相对应文件夹
                File file1 = new File(decDir + File.separator + file.getName());
                //创建目录
                file1.mkdir();
                copyFileAndDir(file, file1);//递归调用
            }
        }
    }

    //param dir 待清空目录   只清空当前目录下的文件
    public static void emptyFile(String dir){
        String operationType = "清空";
        File[] files=FileHandle.returnFileArray(dir, operationType);
        int count=0;
        int countFile = 0;
        for(File f:files){
            if(f.isFile()){
                countFile++;
                boolean delete = f.delete();
                if(delete){count++;}
            }
        }
        if(count==countFile){
            logger.info("目录"+dir+"中文件已全部删除并备份");
        }else{
            logger.warn("目录"+dir+"中文件未全部删除并备份");
        }
    }

    //清空当前目录及子目录的文件，保留目录
    //param dir 待清空目录   只清空当前目录下的文件
    public static void emptyAllFiles(String dir){
        String operationType = "清空";
        File[] files=FileHandle.returnFileArray(dir, operationType);
        int count=0;
        int countFile = 0;
        for(File f:files){
            if(f.isFile()){
                countFile++;
                boolean delete = f.delete();
                if(delete){count++;}
            }else{
                emptyAllFiles(f.getAbsolutePath());
            }
        }
        if(count==countFile){
            logger.info("目录:"+dir+"中文件已全部删除并备份");
        }else{
            logger.warn("目录:"+dir+"中文件未全部删除并备份");
        }
    }

    //获取文件列表，只获取文件名 不包括子目录的文件名
    public static String[] getDirFileName(String dir){
        String operationType = "获取文件名";
        File[] files = FileHandle.returnFileArray(dir, operationType);
        String[] fileNameList = new String[files.length];
        for(int i=0;i<files.length;i++){
            fileNameList[i]=files[i].getName();
        }
        return fileNameList;
    }

    /**
     * @Description 获取目录下符合条件的所有文件名的绝对路径（包括子目录下的文件
     * @Date 2022/9/24 18:31
     * @Param dir 要遍历的路径
     * @param fileType 文件类型，即后缀名，如果有内容，则根据后缀名筛选文件，size==0则返回全部文件名
     * @return 绝对路径名和相对路径名的列表
     **/
    public static List<List<String>> getAllDirPath(String dir,List<String> fileType){
        List<List<String>> ls = new ArrayList<List<String>>();
        List<List<String>> ls11 = new ArrayList<List<String>>();
        List<List<String>> retList = returnAllFileArray(dir, ls);
        if(fileType.size()==0) return retList;
        for(List<String> ls1:retList){
            List<String> getFilterLs = new ArrayList<>();
            for(String s:ls1){
                //用remove的话，将第一个元素去除后，列表元素变为1，循环就结束了,所以new 一个list 接收筛选后的元素
                if(s.contains(".")){
                    String[] arr = s.split("\\.");
                    int length = arr.length;
                    String s1 = arr[length-1];
                    if(fileType.contains(s1)) {
//                        ls1.remove(s);
                        getFilterLs.add(s);
                    }
                }
            }
            if(getFilterLs.size()!=0) ls11.add(getFilterLs);
        }
        return ls11;

    }



    //返回包括子目录的File[]  递归实现
    public static List<List<String>> returnAllFileArray(String dir,List<List<String>> fileList){

        File file = new File(dir);
        if (file.isDirectory()) {

            String[] list = file.list();
            for(String s:list){
                String s1 = file.getAbsolutePath() + File.separator + s;
                returnAllFileArray(s1,fileList);
            }
        }else if(file.isFile()){
            String absolutePath = file.getAbsolutePath();
            String relativePath = file.getName();
            List<String> ls = new ArrayList<String>();
            ls.add(absolutePath);
            ls.add(relativePath);
            fileList.add(ls);
            return fileList;
        }
        return fileList;
    }

    //获取给定目录的子目录列表
    public static List<File> returnDirList(String dir){
        File file = new File(dir);
        List<File> lsFiles = new ArrayList<File>();
        logger.info("目录为："+file.getAbsolutePath());
        if(file.isDirectory()){
            //list 和listFiles 区别，list返回文件和目录，listFiles返回文件
            String[] list = file.list();
            for(String s:list){
                String s1 = file.getAbsolutePath() + File.separator + s;
                File fileChild = new File(s1);
                if (fileChild.isDirectory()) {
                    lsFiles.add(fileChild);
                }
            }
        }
        return lsFiles;
    }

    //从给定目录列表中获取时间最新的目录
    public static String getLatestTime(List<File> files){
        List<String> tmpLs = new ArrayList<>();
        if(files==null || files.size()==0) {
            logger.error("给定目录中无以时间开头的子目录，请检查yml文件");
            System.exit(0);
        }else{
            for(File file:files){
                if(file.getName().startsWith("20")){
                    tmpLs.add(file.getName());
                }
            }
        }
        if(tmpLs.size()!=0){
            tmpLs.sort(String::compareTo);
            logger.info("待读取目录为："+tmpLs.get(tmpLs.size()-1));
            return tmpLs.get(tmpLs.size()-1);
        }else{
            logger.error("给定目录中无以时间开头的子目录，请检查yml文件");
            System.exit(0);
        }
        return "";
    }

    //合并目录与文件名
    public static String mergeDirAndFile(String fileName){
        //todo 合并目录
/*        String  parentDir = "./execute";
        List<File> files = returnDirList(parentDir);
        String latestTime = getLatestTime(files);
        System.out.println( parentDir+latestTime+File.separator+fileName);
        return parentDir+latestTime+File.separator+fileName;*/
        return fileName;
    }




}
