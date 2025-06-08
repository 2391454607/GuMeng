package com.gumeng.service.coze;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gumeng.domain.ModelInfo;
import com.gumeng.entity.vo.model.ToModelInfoVO;

import java.util.List;

/**
 * @author LL318
 * @description 针对表【model_info(模型生成信息)】的数据库操作Service
 * @createDate 2025-04-28 16:23:47
 */
public interface ModelInfoService extends IService<ModelInfo> {

    List<ToModelInfoVO> getToModelList();
}
