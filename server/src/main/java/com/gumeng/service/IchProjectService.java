package com.gumeng.service;

import cn.hutool.db.PageResult;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gumeng.domain.pages.IchProject;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gumeng.entity.vo.IchProjectListVO;
import com.gumeng.entity.vo.IchProjectDetailVO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author Lorn
* @description 针对表【ich_project(非遗项目主表)】的数据库操作Service
* @createDate 2025-03-26 10:16:14
*/
@Service
public interface IchProjectService extends IService<IchProject> {
    Page<IchProjectListVO> getIchProject(Integer current, Integer size, Integer levelId, Integer categoryId);
    
    Page<IchProjectListVO> getIchProject(Integer current, Integer size, Integer levelId, Integer categoryId, Integer regionId);
    
    /**
     * 关键字搜索非遗项目
     */
    Page<IchProjectListVO> searchIchProject(Integer current, Integer size, String keyword);
    
    IchProjectDetailVO getProjectDetail(Integer id);
}
