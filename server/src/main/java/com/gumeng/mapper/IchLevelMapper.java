package com.gumeng.mapper;

import com.gumeng.domain.pages.IchLevel;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
* @author Chine
* @description 针对表【ich_level(非遗保护级别附表)】的数据库操作Mapper
* @createDate 2025-03-26 10:16:14
* @Entity com.gumeng.domain.pages.IchLevel
*/
@Mapper
public interface IchLevelMapper extends BaseMapper<IchLevel> {

    List<IchLevel> getIchLevel();

}




