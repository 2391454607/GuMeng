package com.gumeng.service;

import com.gumeng.entity.vo.DatasetVO;

public interface DatasetService {
    
    /**
     * 获取数据集统计信息
     */
    DatasetVO getDatasetStats();
} 