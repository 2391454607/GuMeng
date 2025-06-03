package com.gumeng.service.coze;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gumeng.domain.ModelInfo;
import com.gumeng.mapper.ModelInfoMapper;
import org.springframework.stereotype.Service;

/**
 * @author LL318
 * @description 针对表【model_info(模型生成信息)】的数据库操作Service实现
 * @createDate 2025-04-28 16:23:47
 */
@Service
public class ModelInfoServiceImpl extends ServiceImpl<ModelInfoMapper, ModelInfo>
        implements ModelInfoService{

}

