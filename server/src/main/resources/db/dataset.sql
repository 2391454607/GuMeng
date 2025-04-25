CREATE TABLE IF NOT EXISTS `dataset_gumeng` (
  `id` int NOT NULL AUTO_INCREMENT,
  `project_name` varchar(255) NOT NULL COMMENT '项目名称',
  `category` varchar(50) NOT NULL COMMENT '项目类别',
  `region` varchar(50) NOT NULL COMMENT '地区',
  `protection_level` varchar(50) NOT NULL COMMENT '保护级别',
  `declaration_year` int NOT NULL COMMENT '申报年份',
  `inheritor_count` int DEFAULT '0' COMMENT '传承人数量',
  `activity_count` int DEFAULT '0' COMMENT '相关活动数量',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category`),
  KEY `idx_region` (`region`),
  KEY `idx_year` (`declaration_year`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='非遗数据集'; 