package com.gumeng.mapper;

import com.gumeng.domain.pages.IchCategory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
* @author Chine
* @description 针对表【ich_category(非遗类别附表)】的数据库操作Mapper
* @createDate 2025-03-26 10:16:14
* @Entity com.gumeng.domain.pages.IchCategory
*/
@Mapper
public interface IchCategoryMapper extends BaseMapper<IchCategory> {

    List<IchCategory> getIchCategory();

}




