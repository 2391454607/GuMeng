package com.gumeng.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gumeng.domain.dataset.DatasetEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface DatasetMapper extends BaseMapper<DatasetEntity> {
    
    @Select("SELECT gender, COUNT(*) as count FROM dataset_gumeng GROUP BY gender")
    List<Map<String, Object>> getGenderStats();
    
    @Select("SELECT region, COUNT(*) as count FROM dataset_gumeng GROUP BY region")
    List<Map<String, Object>> getRegionStats();
    
    @Select("SELECT ethnic, COUNT(*) as count FROM dataset_gumeng GROUP BY ethnic")
    List<Map<String, Object>> getEthnicStats();
    
    @Select("SELECT level, COUNT(*) as count FROM dataset_gumeng GROUP BY level")
    List<Map<String, Object>> getLevelStats();
    
    @Select("SELECT batch, COUNT(*) as count FROM dataset_gumeng GROUP BY batch")
    List<Map<String, Object>> getBatchStats();
} 