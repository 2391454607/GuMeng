package com.gumeng.service.web;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gumeng.domain.pages.Model;
import com.gumeng.entity.vo.model.ModelInfoVO;
import com.gumeng.entity.vo.model.ModelVO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author Chine
* @description 针对表【model(3d模型表)】的数据库操作Service
* @createDate 2025-04-22 18:23:07
*/
@Service
public interface ModelService extends IService<ModelVO> {

    List<ModelVO> getModelList();

    List<ModelInfoVO> getModel(Integer modelId);

}
