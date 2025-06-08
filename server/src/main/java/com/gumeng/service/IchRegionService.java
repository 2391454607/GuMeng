package com.gumeng.service;

import com.gumeng.domain.pages.IchRegion;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author Lorn
* @description 针对表【ich_region(非遗地区表)】的数据库操作Service
* @createDate 2025-06-08 17:00:00
*/
public interface IchRegionService extends IService<IchRegion> {
    /**
     * 获取所有地区
     */
    List<IchRegion> getAllRegions();
    
    /**
     * 根据ID获取地区名称
     */
    String getRegionNameById(Integer regionId);
} 