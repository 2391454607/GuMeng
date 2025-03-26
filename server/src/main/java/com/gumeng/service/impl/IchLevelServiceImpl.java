package com.gumeng.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gumeng.domain.pages.IchLevel;
import com.gumeng.service.IchLevelService;
import com.gumeng.mapper.IchLevelMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author Chine
* @description 针对表【ich_level(非遗保护级别附表)】的数据库操作Service实现
* @createDate 2025-03-26 10:16:14
*/
@Service
public class IchLevelServiceImpl extends ServiceImpl<IchLevelMapper, IchLevel>
    implements IchLevelService{

    @Resource
    private IchLevelMapper ichLevelMapper;

    @Override
    public List<IchLevel> getIchLevel() {
        return ichLevelMapper.getIchLevel();
    }
}




