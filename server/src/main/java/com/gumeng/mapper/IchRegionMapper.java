package com.gumeng.mapper;

import com.gumeng.domain.pages.IchRegion;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author Lorn
* @description 针对表【ich_region(非遗地区表)】的数据库操作Mapper
* @createDate 2025-06-08 17:00:00
* @Entity com.gumeng.domain.pages.IchRegion
*/
@Mapper
public interface IchRegionMapper extends BaseMapper<IchRegion> {
    /**
     * 获取所有地区
     */
    List<IchRegion> getAllRegions();
    
    /**
     * 根据ID获取地区名称
     */
    String getRegionNameById(@Param("regionId") Integer regionId);
} 