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

-- 非遗项目表
CREATE TABLE IF NOT EXISTS `data_heritage_project` (
                                                       `id` int NOT NULL AUTO_INCREMENT,
                                                       `name` varchar(100) NOT NULL COMMENT '项目名称',
    `category` varchar(50) NOT NULL COMMENT '项目类别',
    `level` varchar(50) NOT NULL COMMENT '项目级别',
    `region` varchar(50) NOT NULL COMMENT '所属地区',
    `declaration_year` int NOT NULL COMMENT '申报年份',
    `batch` varchar(50) NOT NULL COMMENT '申报批次',
    `description` text COMMENT '项目描述',
    `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：1-正常 0-禁用',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_category` (`category`),
    KEY `idx_level` (`level`),
    KEY `idx_region` (`region`),
    KEY `idx_year` (`declaration_year`),
    KEY `idx_batch` (`batch`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='非遗项目表';

-- 传承人信息表
CREATE TABLE IF NOT EXISTS `data_inheritor` (
                                                `id` int NOT NULL AUTO_INCREMENT,
                                                `name` varchar(100) NOT NULL COMMENT '传承人姓名',
    `gender` varchar(10) NOT NULL COMMENT '性别',
    `ethnic` varchar(50) NOT NULL COMMENT '民族',
    `birth_year` int NOT NULL COMMENT '出生年份',
    `level` varchar(50) NOT NULL COMMENT '传承人级别',
    `region` varchar(50) NOT NULL COMMENT '所属地区',
    `project_id` int NOT NULL COMMENT '关联项目ID',
    `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：1-正常 0-禁用',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_project` (`project_id`),
    KEY `idx_level` (`level`),
    KEY `idx_region` (`region`),
    KEY `idx_ethnic` (`ethnic`),
    CONSTRAINT `fk_inheritor_project` FOREIGN KEY (`project_id`) REFERENCES `data_heritage_project` (`id`) ON DELETE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='传承人信息表';

-- 项目关系表
CREATE TABLE IF NOT EXISTS `data_project_relation` (
                                                       `id` int NOT NULL AUTO_INCREMENT,
                                                       `source_project_id` int NOT NULL COMMENT '源项目ID',
                                                       `target_project_id` int NOT NULL COMMENT '目标项目ID',
                                                       `relation_type` varchar(50) NOT NULL COMMENT '关系类型',
    `relation_strength` int NOT NULL DEFAULT '1' COMMENT '关系强度',
    `description` varchar(255) DEFAULT NULL COMMENT '关系描述',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_project_relation` (`source_project_id`,`target_project_id`,`relation_type`),
    KEY `idx_target` (`target_project_id`),
    CONSTRAINT `fk_relation_source` FOREIGN KEY (`source_project_id`) REFERENCES `data_heritage_project` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_relation_target` FOREIGN KEY (`target_project_id`) REFERENCES `data_heritage_project` (`id`) ON DELETE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='项目关系表';

-- 内容分析表
CREATE TABLE IF NOT EXISTS `data_content_analysis` (
                                                       `id` int NOT NULL AUTO_INCREMENT,
                                                       `project_id` int NOT NULL COMMENT '项目ID',
                                                       `category` varchar(50) NOT NULL COMMENT '内容类别',
    `view_count` int NOT NULL DEFAULT '0' COMMENT '浏览量',
    `comment_count` int NOT NULL DEFAULT '0' COMMENT '评论数',
    `like_count` int NOT NULL DEFAULT '0' COMMENT '点赞数',
    `share_count` int NOT NULL DEFAULT '0' COMMENT '分享数',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_project_category` (`project_id`,`category`),
    KEY `idx_category` (`category`),
    CONSTRAINT `fk_content_project` FOREIGN KEY (`project_id`) REFERENCES `data_heritage_project` (`id`) ON DELETE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='内容分析表';

-- 关键词表
CREATE TABLE IF NOT EXISTS `data_keyword` (
                                              `id` int NOT NULL AUTO_INCREMENT,
                                              `project_id` int NOT NULL COMMENT '项目ID',
                                              `keyword` varchar(100) NOT NULL COMMENT '关键词',
    `weight` int NOT NULL DEFAULT '0' COMMENT '权重',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_project` (`project_id`),
    KEY `idx_keyword` (`keyword`),
    CONSTRAINT `fk_keyword_project` FOREIGN KEY (`project_id`) REFERENCES `data_heritage_project` (`id`) ON DELETE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='关键词表';

-- 趋势分析表
CREATE TABLE IF NOT EXISTS `data_trend_analysis` (
                                                     `id` int NOT NULL AUTO_INCREMENT,
                                                     `year` int NOT NULL COMMENT '年份',
                                                     `category` varchar(50) NOT NULL COMMENT '类别',
    `region` varchar(50) NOT NULL COMMENT '地区',
    `project_count` int NOT NULL DEFAULT '0' COMMENT '项目数量',
    `inheritor_count` int NOT NULL DEFAULT '0' COMMENT '传承人数量',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_year_category_region` (`year`,`category`,`region`),
    KEY `idx_category` (`category`),
    KEY `idx_region` (`region`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='趋势分析表';