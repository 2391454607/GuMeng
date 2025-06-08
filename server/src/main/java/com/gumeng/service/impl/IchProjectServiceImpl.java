package com.gumeng.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gumeng.domain.pages.IchProject;
import com.gumeng.entity.vo.IchProjectListVO;
import com.gumeng.entity.vo.IchProjectDetailVO;
import com.gumeng.service.IchProjectService;
import com.gumeng.mapper.IchProjectMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.beans.BeanUtils;

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
        return getIchProject(current, size, levelId, categoryId, null);
    }
    
    @Override
    public Page<IchProjectListVO> getIchProject(Integer current, Integer size, Integer levelId, Integer categoryId, Integer regionId) {
        Page<IchProjectListVO> page = new Page<>(current, size);
        try {
            Page<IchProjectListVO> result = ichProjectMapper.getIchProject(page, levelId, categoryId, regionId);
            // 处理可能为null的字段
            if (result != null && result.getRecords() != null) {
                result.getRecords().forEach(project -> {
                    if (project.getCategoryName() == null) {
                        project.setCategoryName("未分类");
                    }
                    if (project.getLevelName() == null) {
                        project.setLevelName("未知级别");
                    }
                    if (project.getRegionName() == null) {
                        project.setRegionName("未知地区");
                    }
                });
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return new Page<>(); // 返回空结果页
        }
    }
    
    @Override
    public Page<IchProjectListVO> searchIchProject(Integer current, Integer size, String keyword) {
        Page<IchProjectListVO> page = new Page<>(current, size);
        try {
            Page<IchProjectListVO> result = ichProjectMapper.searchIchProject(page, keyword);
            // 处理可能为null的字段
            if (result != null && result.getRecords() != null) {
                result.getRecords().forEach(project -> {
                    if (project.getCategoryName() == null) {
                        project.setCategoryName("未分类");
                    }
                    if (project.getLevelName() == null) {
                        project.setLevelName("未知级别");
                    }
                    if (project.getRegionName() == null) {
                        project.setRegionName("未知地区");
                    }
                });
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return new Page<>();
        }
    }
    
    @Override
    public IchProjectDetailVO getProjectDetail(Integer id) {
        // 1. 获取基本项目信息
        IchProject project = this.getById(id);
        if (project == null) {
            return null;
        }
        
        // 2. 更新浏览计数
        project.setViewCount(project.getViewCount() == null ? 1 : project.getViewCount() + 1);
        this.updateById(project);
        
        // 3. 创建详情VO并设置基本属性
        IchProjectDetailVO detailVO = new IchProjectDetailVO();
        BeanUtils.copyProperties(project, detailVO);
        
        // 处理封面图片：优先使用images字段，如果有多张图片，使用第一张作为封面
        if (project.getImages() != null && !project.getImages().isEmpty()) {
            detailVO.setImages(project.getImages());
            if (project.getImages().contains(",")) {
                String firstImage = project.getImages().split(",")[0].trim();
                detailVO.setCoverImage(firstImage);
            } else {
                detailVO.setCoverImage(project.getImages());
            }
        }
        
        // 4. 获取分类名称和级别名称
        try {
            // 使用Mapper获取分类和级别名称
            detailVO.setCategoryName(ichProjectMapper.getCategoryNameById(project.getCategoryId()));
            detailVO.setLevelName(ichProjectMapper.getLevelNameById(project.getLevelId()));
            
            // 获取地区名称
            if (project.getRegionId() != null) {
                detailVO.setRegionName(ichProjectMapper.getRegionNameById(project.getRegionId()));
            }
            
            // 5. 获取相关项目（同类别的其他项目，限制5个）
            List<IchProjectListVO> relatedProjects = ichProjectMapper.getRelatedProjects(project.getId(), project.getCategoryId(), 5);
            detailVO.setRelatedProjects(relatedProjects);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return detailVO;
    }
}




