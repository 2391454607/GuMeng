package com.gumeng.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gumeng.domain.pages.IchCategory;
import com.gumeng.service.IchCategoryService;
import com.gumeng.mapper.IchCategoryMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author Chine
* @description 针对表【ich_category(非遗类别附表)】的数据库操作Service实现
* @createDate 2025-03-26 10:16:14
*/
@Service
public class IchCategoryServiceImpl extends ServiceImpl<IchCategoryMapper, IchCategory>
    implements IchCategoryService{

    @Resource
    private IchCategoryMapper ichCategoryMapper;

    @Override
    public List<IchCategory> getIchCategory() {
        return ichCategoryMapper.getIchCategory();
    }
}




