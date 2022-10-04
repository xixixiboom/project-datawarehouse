package com.datawarehouse.excelgenerate.service;

import com.datawarehouse.excelgenerate.entity.RelationTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @ClassName GenerateTree
 * @Description TODO
 * @Author xjy
 * @Date 2022/8/11 13:09
 * @Version 1.0
 **/
public class GenerateTree implements Callable {
    /**
     * @param tableName
     * @param twoDimensionalArray
     * @return java.util.List<java.lang.Integer>
     * @Description twoDimensionalArray中筛选与tableName值相同的列，记录twoDimensionalArray中的坐标
     * @Date 2022/8/11 9:44
     * @Param
     **/
    private static final Logger logger = LoggerFactory.getLogger(GenerateTree.class);
    private String tableName;
    private List<ArrayList<String>> twoDimensionalArray;
    private RelationTree<String> relationTree;

    public GenerateTree(String tTableName, List<ArrayList<String>> twoDimensionalArray, RelationTree<String> relationTree){
        this.tableName = tTableName;
        this.relationTree=relationTree;
        this.twoDimensionalArray = twoDimensionalArray;
    }

    public List<Integer> filter(String tableName, List<ArrayList<String>> twoDimensionalArray) {
        List<Integer> intList = new ArrayList<>();
        for (int i = 0; i < twoDimensionalArray.size(); i++) {
            String parent = twoDimensionalArray.get(i).get(1);
            String item = twoDimensionalArray.get(i).get(0);
            if (tableName.equals(parent)) {
                intList.add(i);
            }
        }
        return intList;
    }

    /**
     * @param tTableName
     * @param twoDimensionalArray
     * @param relationTree
     * @return com.datawarehouse.excelgenerate.entity.RelationTree
     * @Description 递归处理两列表，返回树的类RelationTree
     * @Date 2022/8/11 10:06
     * @Param
     **/
    public RelationTree pushElementInTree(String tTableName, List<ArrayList<String>> twoDimensionalArray, RelationTree<String> relationTree) {
        List<Integer> intList = filter(tTableName, twoDimensionalArray);
        if (intList.size() == 0|| intList==null) {
            return relationTree;
        } else {
            for (int i = 0; i < intList.size(); i++) {
                int subscript = intList.get(i);
                String parent = twoDimensionalArray.get(subscript).get(1);
                String item = twoDimensionalArray.get(subscript).get(0);
                relationTree.add(parent, item);
                //todo 目前递归写的有问题，需要重新写
                pushElementInTree(item,twoDimensionalArray,relationTree);

            }
            return relationTree;
        }
    }

    public boolean isLast(String s){
        for(int i=0;i<twoDimensionalArray.size();i++){
            if(twoDimensionalArray.get(i).get(1).equals(s)){
                return false;
            }
        }
        return true;
    }

    public RelationTree circulation(RelationTree relationTree,String tableName){
        if(isLast(tableName)){
            return relationTree;
        }
        for(int j =0;j<twoDimensionalArray.size();j++){
            if(tableName.equals(twoDimensionalArray.get(j).get(1))){
                relationTree.add(twoDimensionalArray.get(j).get(1),twoDimensionalArray.get(j).get(0));
                circulation(relationTree,twoDimensionalArray.get(j).get(0));

            }
        }
        return relationTree;
    }



/*    public RelationTree pushElementInTree2(String tTableName, List<ArrayList<String>> twoDimensionalArray, RelationTree<String> relationTree){
        int count =0;
        for(int i=0;i<twoDimensionalArray.size();i++){
            if(tTableName.equals(twoDimensionalArray.get(i).get(1))){
                count++;
            }
        }
        if(count==0){
            return relationTree;
        }

        for(int i=0;i<twoDimensionalArray.size();i++){
            String parent = twoDimensionalArray.get(i).get(1);
            String item = twoDimensionalArray.get(i).get(0);
            if(tTableName.equals(parent)){
                relationTree.add(parent,item);
                pushElementIntTree2(item,twoDimensionalArray,relationTree);
            }
        }
        return relationTree;
    }*/


/*    @Override
    public void run() {
        try{
             pushElementInTree(tableName, twoDimensionalArray, this.relationTree);
            System.out.println(relationTree.getGrandChild(tableName));
            logger.info("构造树成功");
        } catch(Exception e) {
            logger.error("构造树失败");
            e.printStackTrace();
        }
    }*/

    @Override
    public RelationTree call() {
        try{
//            relationTree=pushElementInTree(tableName, twoDimensionalArray, relationTree);
            relationTree=circulation(relationTree,tableName);
//            logger.info("构造树成功");
        } catch(Exception e) {
            logger.error("构造树失败");
            e.printStackTrace();
        }
        return relationTree;
    }
}
