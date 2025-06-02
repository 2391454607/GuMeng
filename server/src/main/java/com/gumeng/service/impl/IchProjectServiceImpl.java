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
        Page<IchProjectListVO> page = new Page<>(current, size);
        return ichProjectMapper.getIchProject(page, levelId, categoryId);
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
        
        // 4. 获取分类名称和级别名称
        try {
            // 使用Mapper获取分类和级别名称
            detailVO.setCategoryName(ichProjectMapper.getCategoryNameById(project.getCategoryId()));
            detailVO.setLevelName(ichProjectMapper.getLevelNameById(project.getLevelId()));
            
            // 5. 获取相关项目（同类别的其他项目，限制5个）
            List<IchProjectListVO> relatedProjects = ichProjectMapper.getRelatedProjects(project.getId(), project.getCategoryId(), 5);
            detailVO.setRelatedProjects(relatedProjects);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return detailVO;
    }
}




