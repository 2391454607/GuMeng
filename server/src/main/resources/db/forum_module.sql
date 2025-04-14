-- 修改forum_post表，添加AUTO_INCREMENT并添加图片字段
ALTER TABLE forum_post 
    MODIFY COLUMN id int NOT NULL AUTO_INCREMENT COMMENT 'id',
    ADD COLUMN images TEXT NULL COMMENT '帖子图片，JSON格式存储多张图片URL',
    ADD COLUMN user_id int NOT NULL COMMENT '发帖用户id',
    ADD COLUMN view_count int DEFAULT 0 COMMENT '浏览次数';

-- 修改comments表，添加AUTO_INCREMENT
ALTER TABLE comments
    MODIFY COLUMN id int NOT NULL AUTO_INCREMENT COMMENT 'id';

-- 用于测试的论坛帖子数据
INSERT INTO forum_post (title, content, common_num, thumbs_up_num, topic, images, user_id, view_count, create_time, update_time, deleted)
VALUES 
('非遗保护倡议', '我们应该共同努力保护非物质文化遗产，传承历史文化。', 0, 0, '非遗保护', '[]', 1, 0, NOW(), NOW(), '0'),
('京剧艺术交流', '京剧作为中国传统戏曲，有着悠久的历史和丰富的表现形式。', 0, 0, '戏曲艺术', '[]', 2, 0, NOW(), NOW(), '0'),
('传统手工艺学习', '想学习传统手工艺的朋友们可以在这里交流经验。', 0, 0, '手工艺', '[]', 3, 0, NOW(), NOW(), '0'); 