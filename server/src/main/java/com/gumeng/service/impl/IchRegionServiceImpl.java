package com.gumeng.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gumeng.domain.pages.IchRegion;
import com.gumeng.service.IchRegionService;
import com.gumeng.mapper.IchRegionMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author Lorn
* @description 针对表【ich_region(非遗地区表)】的数据库操作Service实现
* @createDate 2025-06-08 17:00:00
*/
@Service
public class IchRegionServiceImpl extends ServiceImpl<IchRegionMapper, IchRegion>
    implements IchRegionService {

    @Resource
    private IchRegionMapper ichRegionMapper;
    
    @Override
    public List<IchRegion> getAllRegions() {
        return ichRegionMapper.getAllRegions();
    }
    
    @Override
    public String getRegionNameById(Integer regionId) {
        if (regionId == null) {
            return null;
        }
        return ichRegionMapper.getRegionNameById(regionId);
    }
} 