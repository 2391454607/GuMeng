package com.gumeng.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gumeng.domain.pages.IchProject;
import com.gumeng.entity.vo.IchProjectListVO;
import com.gumeng.service.IchProjectService;
import com.gumeng.mapper.IchProjectMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author Chine
* @description 针对表【ich_project(非遗项目主表)】的数据库操作Service实现
* @createDate 2025-03-26 10:16:14
*/
@Service
public class IchProjectServiceImpl extends ServiceImpl<IchProjectMapper, IchProject>
    implements IchProjectService {

    @Resource
    private IchProjectMapper ichProjectMapper;

    @Override
    public Page<IchProjectListVO> getIchProject(Integer current, Integer size, Integer levelId, Integer categoryId) {
        Page<IchProjectListVO> page = new Page<>(current, size);
        return ichProjectMapper.getIchProject(page, levelId, categoryId);
    }
}




