package com.datawarehouse.excelgenerate.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName CommonMapper
 * @Description TODO
 * @Author xjy
 * @Date 2022/8/10 11:29
 * @Version 1.0
 **/

@Mapper
@Repository
public interface CommonMapper {
    @Select("${selectRelation}")
    List<LinkedHashMap<String,String>> queryByTableName(@Param("selectRelation") String sql);
}
