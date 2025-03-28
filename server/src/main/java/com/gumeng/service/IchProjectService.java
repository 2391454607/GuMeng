package com.gumeng.service;

import cn.hutool.db.PageResult;
import com.gumeng.domain.pages.IchProject;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gumeng.entity.vo.IchProjectListVO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author Chine
* @description 针对表【ich_project(非遗项目主表)】的数据库操作Service
* @createDate 2025-03-26 10:16:14
*/
@Service
public interface IchProjectService extends IService<IchProject> {

    List<IchProjectListVO> getIchProject();

}
