package com.gumeng.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gumeng.domain.ModelInfo;
import com.gumeng.entity.vo.model.ToModelInfoVO;

import java.util.List;

/**
 * @author LL318
 * @description 针对表【model_info(模型生成信息)】的数据库操作Mapper
 * @createDate 2025-04-28 16:23:47
 * @Entity generator.domain.ModelInfo
 */
public interface ModelInfoMapper extends BaseMapper<ModelInfo> {

    List<ToModelInfoVO> getToModelList();
}
