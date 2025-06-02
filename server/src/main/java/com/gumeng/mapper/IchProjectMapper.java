package com.gumeng.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gumeng.domain.pages.IchProject;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gumeng.entity.vo.IchProjectListVO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
* @author Chine
* @description 针对表【ich_project(非遗项目主表)】的数据库操作Mapper
* @createDate 2025-03-26 10:16:14
* @Entity com.gumeng.domain.pages.IchProject
*/
@Mapper
public interface IchProjectMapper extends BaseMapper<IchProject> {
    Page<IchProjectListVO> getIchProject(Page<IchProjectListVO> page, 
                                       @Param("levelId") Integer levelId,
                                       @Param("categoryId") Integer categoryId);
    
    /**
     * 根据分类ID获取分类名称
     */
    String getCategoryNameById(@Param("categoryId") Integer categoryId);
    
    /**
     * 根据级别ID获取级别名称
     */
    String getLevelNameById(@Param("levelId") Integer levelId);
    
    /**
     * 获取相关项目（同类别的其他项目）
     */
    List<IchProjectListVO> getRelatedProjects(@Param("id") Integer id, 
                                             @Param("categoryId") Integer categoryId,
                                             @Param("limit") Integer limit);
}




