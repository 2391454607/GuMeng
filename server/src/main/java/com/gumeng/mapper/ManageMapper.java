package com.gumeng.mapper;

import com.gumeng.entily.Manage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ManageMapper {

    @Select("SELECT * FROM admin")
    List<Manage> findAll();

}
