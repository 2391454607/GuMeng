package com.gumeng.entity.vo;

import lombok.Data;

/**
 * 非遗传承人数据统计VO
 */
@Data
public class DatasetVO {
    
    /**
     * 性别统计
     */
    private GenderStats genderStats;
    
    /**
     * 地区统计
     */
    private RegionStats regionStats;
    
    /**
     * 民族统计
     */
    private EthnicStats ethnicStats;
    
    /**
     * 级别统计
     */
    private LevelStats levelStats;
    
    /**
     * 批次统计
     */
    private BatchStats batchStats;
    
    @Data
    public static class GenderStats {
        private String[] genders;
        private Integer[] counts;
    }
    
    @Data
    public static class RegionStats {
        private String[] regions;
        private Integer[] counts;
    }
    
    @Data
    public static class EthnicStats {
        private String[] ethnics;
        private Integer[] counts;
    }
    
    @Data
    public static class LevelStats {
        private String[] levels;
        private Integer[] counts;
    }
    
    @Data
    public static class BatchStats {
        private String[] batches;
        private Integer[] counts;
    }
} 