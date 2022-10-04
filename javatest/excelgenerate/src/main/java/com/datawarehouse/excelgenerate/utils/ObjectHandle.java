package com.datawarehouse.excelgenerate.utils;

import org.apache.poi.ss.formula.functions.T;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName ObjectHandle
 * @Description TODO
 * @Author xjy
 * @Date 2022/9/20 22:32
 * @Version 1.0
 **/
public class ObjectHandle {

    private static final Logger logger = LoggerFactory.getLogger(ObjectHandle.class);


    /**
     * @Description 合并两个对象的值
     * @Date 2022/9/20 22:35
     * @Param sourceBean 数据来源对象
     * @param targetBean 目标对象
     * @return
     **/
    public static Object mergerData(Object sourceBean,Object targetBean){
//     spring自带，照样需要sourceBean与targetBean不为空   BeanUtils.copyProperties();
        Field[] sourceFields = sourceBean.getClass().getDeclaredFields();
        Field[] targetFields = targetBean.getClass().getDeclaredFields();
        try {
            for (int i = 0; i < sourceFields.length; i++) {
                Field sourceField = sourceFields[i];
                //这里遍历主要是为了适应双方对象属性顺序不一致的情况
                for (int j = 0; j < targetFields.length; j++) {
                    Field targetField = targetFields[j];
                    if (sourceField.getName().equals(targetField.getName())) {
                        sourceField.setAccessible(true);
                        targetField.setAccessible(true);
                        if (!(sourceField.get(sourceBean) == null)) {
                            targetField.set(targetBean, sourceField.get(sourceBean));
                        }
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return targetBean;

    }

    public static List convertObjToList(Object obj){
        List<String> list=new ArrayList<String>();
        if (obj == null)
            return null;
        Field[] fields = obj.getClass().getDeclaredFields();
        try {
            for(int i=0;i<fields.length;i++){
                try {
                    Field f = obj.getClass().getDeclaredField(fields[i].getName());
                    f.setAccessible(true);
                    Object o = f.get(obj);
                    if(o==null){
                        list.add(null);
                    }else{
                        list.add(o.toString());
                    }
                } catch (NoSuchFieldException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return list;
    }

    public static<T> List<List<String>> convertList(List<T> listInput){
        List<List<String>> result = new ArrayList<List<String>>();
        List<Object> ls = (List<Object>)listInput;
        for(Object o : ls){
            List list = convertObjToList(o);
            result.add(list);
        }
        return result;
    }

    public static<T> List<T> mergeList(List<T> ls1,List<T>ls2){
        List<T> retLs =new ArrayList<>();
        if(ls1!=null) retLs.addAll(ls1);
        if(ls2!=null) retLs.addAll(ls2);
        return retLs;
    }

}
