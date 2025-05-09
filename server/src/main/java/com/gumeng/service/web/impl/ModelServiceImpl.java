package com.gumeng.service.web.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gumeng.domain.pages.Model;
import com.gumeng.entity.vo.model.ModelInfoVO;
import com.gumeng.entity.vo.model.ModelVO;
import com.gumeng.service.web.ModelService;
import com.gumeng.mapper.web.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author Chine
* @description 针对表【model(3d模型表)】的数据库操作Service实现
* @createDate 2025-04-22 18:23:07
*/
@Service
public class ModelServiceImpl extends ServiceImpl<ModelMapper, Model>
    implements ModelService{

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public List<ModelVO> getModelList() {
        return modelMapper.getModelList();
    }

    @Override
    public List<ModelInfoVO> getModel(Integer modelId) {
        return modelMapper.getModelById(modelId);
    }
}




