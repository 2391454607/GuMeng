package com.gumeng.mapper.web;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gumeng.domain.pages.Model;
import com.gumeng.entity.vo.model.ModelInfoVO;
import com.gumeng.entity.vo.model.ModelVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
* @author Chine
* @description 针对表【model(3d模型表)】的数据库操作Mapper
* @createDate 2025-04-22 18:23:07
* @Entity com.gumeng.domain.pages.Model
*/
@Mapper
public interface ModelMapper extends BaseMapper<Model> {

    List<ModelVO> getModelList();

    List<ModelInfoVO> getModelById(Integer modelId);
}




