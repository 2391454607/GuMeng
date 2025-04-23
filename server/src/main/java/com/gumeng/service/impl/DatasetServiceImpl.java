package com.gumeng.service.impl;

import com.gumeng.entity.vo.DatasetVO;
import com.gumeng.mapper.DatasetMapper;
import com.gumeng.service.DatasetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class DatasetServiceImpl implements DatasetService {

    @Autowired
    private DatasetMapper datasetMapper;

    @Override
    public DatasetVO getDatasetStats() {
        DatasetVO datasetVO = new DatasetVO();
        
        // 获取性别统计
        List<Map<String, Object>> genderStats = datasetMapper.getGenderStats();
        DatasetVO.GenderStats gs = new DatasetVO.GenderStats();
        gs.setGenders(genderStats.stream().map(m -> (String)m.get("gender")).toArray(String[]::new));
        gs.setCounts(genderStats.stream().map(m -> ((Number)m.get("count")).intValue()).toArray(Integer[]::new));
        datasetVO.setGenderStats(gs);
        
        // 获取地区统计
        List<Map<String, Object>> regionStats = datasetMapper.getRegionStats();
        DatasetVO.RegionStats rs = new DatasetVO.RegionStats();
        rs.setRegions(regionStats.stream().map(m -> (String)m.get("region")).toArray(String[]::new));
        rs.setCounts(regionStats.stream().map(m -> ((Number)m.get("count")).intValue()).toArray(Integer[]::new));
        datasetVO.setRegionStats(rs);
        
        // 获取民族统计
        List<Map<String, Object>> ethnicStats = datasetMapper.getEthnicStats();
        DatasetVO.EthnicStats es = new DatasetVO.EthnicStats();
        es.setEthnics(ethnicStats.stream().map(m -> (String)m.get("ethnic")).toArray(String[]::new));
        es.setCounts(ethnicStats.stream().map(m -> ((Number)m.get("count")).intValue()).toArray(Integer[]::new));
        datasetVO.setEthnicStats(es);
        
        // 获取级别统计
        List<Map<String, Object>> levelStats = datasetMapper.getLevelStats();
        DatasetVO.LevelStats ls = new DatasetVO.LevelStats();
        ls.setLevels(levelStats.stream().map(m -> (String)m.get("level")).toArray(String[]::new));
        ls.setCounts(levelStats.stream().map(m -> ((Number)m.get("count")).intValue()).toArray(Integer[]::new));
        datasetVO.setLevelStats(ls);
        
        // 获取批次统计
        List<Map<String, Object>> batchStats = datasetMapper.getBatchStats();
        DatasetVO.BatchStats bs = new DatasetVO.BatchStats();
        bs.setBatches(batchStats.stream().map(m -> (String)m.get("batch")).toArray(String[]::new));
        bs.setCounts(batchStats.stream().map(m -> ((Number)m.get("count")).intValue()).toArray(Integer[]::new));
        datasetVO.setBatchStats(bs);
        
        return datasetVO;
    }
} 