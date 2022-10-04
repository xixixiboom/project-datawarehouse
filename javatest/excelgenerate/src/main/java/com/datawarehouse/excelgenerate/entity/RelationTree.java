package com.datawarehouse.excelgenerate.entity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName RelationTree
 * @Description TODO
 * @Author xjy
 * @Date 2022/8/10 14:51
 * @Version 1.0
 **/


@Service
public class RelationTree<E> {
    //树形结构容器类--递归方法的使用--this关键字的指向

/**
 * 自定义树形结构容器：
 * 能够找到当前节点的父节点
 * 能够找到当前节点的子节点
 * 能够找到当前节点的兄弟节点
 * 能够找到当前节点的祖先节点
 * 能够找到当前节点的子孙节点
 *
 */
    /**
     * 基于树形结构实现元素存储的容器
     */
//一个节点就是一个元素
    //子节点映射父节点
    private Map<E,E> aa=new LinkedHashMap<>();
    //父节点映射子节点
    private Map<E,List<E>> bb=new LinkedHashMap<>();

    /**
     * @Description
     * @Date 2022/8/11 15:46
     * @Param
     * @return E
     **/
/*    public E getRoot(){
//        Set<E> keySets = this.bb.keySet();
        E myKey = (E)this.aa.keySet().toArray()[0];
        List<E> fullFathers = getFullFathers(myKey);
        return fullFathers.get(0);
    }*/
    //向容器中添加元素的方法
    public void add(E parent,E item){          //参数为父节点和子节点，每个节点就是一个元素
        //完成单节点之间的映射，子节点映射父节点
        this.aa.put(item,parent);
        //完成多节点之间的映射，父节点映射子节点
        List<E> list=this.bb.get(parent);
        //判断装载子节点的容器是否为空，为空则创建新list容器装载子节点
        if (list==null){
            list=new ArrayList<>();
            this.bb.put(parent,list);       //将新创建的list容器赋给父节点的value
        }
        list.add(item);     //为list容器添加子节点元素

    }

    //根据当前节点获取其父节点
    public E getParent(E item){
        return this.aa.get(item);
    }

    //获取当前节点的子节点
    public List<E> getChild(E item){
        return this.bb.get(item);
    }

    //获取当前节点的兄弟节点
    public List<E> getBrother(E item){
        //获取当前节点的父节点
        E parent=this.getParent(item);
        //获取当亲节点的子节点容器
        List<E> list=this.getChild(parent);
        //list容器中的子节点不能删除，否则子节点会被永久删除
        List<E> brother=new ArrayList<>();
        if (brother != null){           //新建ArrayList对象为[]，不为null
            brother.addAll(list);
            brother.remove(item);
        }
        return brother;
    }

    //获取当前节点的祖先节点
    public List<E> getFullFathers(E item){
        //获取当前节点的父节点
        E parent=this.getParent(item);
        //递归终止条件
        if (parent==null){
            return new ArrayList<>();       //此处return直接返回ArrayList容器对象
        }
        List<E> list=this.getFullFathers(parent);
        list.add(parent);
        return list;
    }

    //获取当前节点的子孙节点
    public List<E> getGrandChild(E item){
        //创建容器装载当前节点所有子孙节点
        List<E> list= new ArrayList<>();

        List<E> child=this.getChild(item);
        if (child==null){
            return list;
        }
        for (int i = 0; i <child.size() ; i++) {
            E ele = child.get(i);
            List<E> temp =this.getGrandChild(ele);      //将list易容成temp，list就是temp，返回的list，与temp等同
            temp.add(ele);           //因为list与temp等同，所以此处使用谁都行
            list.addAll(temp);      //list不能与list求并集，因此使用temp代替list
        }
        return list;
    }

/*    @Override
    public String toString(){
        Set<E> es = bb.keySet();
        for(E e : es){
            List<E> listValue = bb.get(e);
            String value ="";
            for(E v:listValue){
                value=value+v+",";
            }
        }

    }*/

    public List<String> retList(){
        Set<E> es = bb.keySet();
        List<String> list = new ArrayList<>();
        for(E e : es){
            List<E> listValue = bb.get(e);
            listValue = listValue.stream().distinct().collect(Collectors.toList());    //去重
            String value =e+": ";
            for(int i=0;i<listValue.size();i++){
                if(i!=listValue.size()-1){
                    value = value+listValue.get(i)+",";
                }else{
                    value = value+listValue.get(i);
                }
            }
            list.add(value);
        }
        return list;
    }

    /*//返回根节点和所有子孙节点
    public List<E> getRootAndGrandChild(E item){
        List<E> grandChild = getGrandChild(item);
        List<E> getRootAndGrandChild = new ArrayList<>();
        getRootAndGrandChild.add(item);
        getRootAndGrandChild.addAll(grandChild);
        return getRootAndGrandChild;
    }*/


    /*public static void main(String[] args) {

        RelationTree<String> uu=new RelationTree<>();

        uu.add("root","生物");
        uu.add("生物","植物");
        uu.add("生物","动物");
        uu.add("生物","动物");
        uu.add("动物","脊索动物");
        uu.add("动物","脊椎动物");
        uu.add("动物","肠腔动物");
        uu.add("脊椎动物","哺乳动物");
        uu.add("脊椎动物","鱼类");
        uu.add("哺乳动物","猫");
        uu.add("哺乳动物","牛");
        uu.add("哺乳动物","人");

        System.out.println("========获取当前节点的父节点=========");
        String aa = uu.getParent("鱼类");
        System.out.println(aa);

        System.out.println("========获取当前节点的父节点=========");
        List<String> bb = uu.getChild("脊椎动物");
        for (int i = 0; i <bb.size() ; i++) {
            System.out.println(bb.get(i));
        }

        System.out.println("==========获取兄弟节点==================");
        List<String> cc=uu.getBrother("动物");
        for (int i = 0; i < cc.size(); i++) {
            System.out.println(cc.get(i));
        }

        System.out.println("===========获取祖先节点================");
        List<String> dd = uu.getFullFathers("人");
        for (int i = 0; i <dd.size() ; i++) {
            System.out.println(dd.get(i));
        }

        System.out.println("============获取子孙节点==============");
        List<String> ff=uu.getGrandChild("动物");
        for (int i = 0; i <ff.size() ; i++) {
            System.out.println(ff.get(i));
        }

    }*/
}