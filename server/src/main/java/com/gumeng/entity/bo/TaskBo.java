package com.gumeng.entity.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 功能：
 * 作者：Z
 * 日期：2025/6/3 下午4:54
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskBo {
    private String taskId; // 任务的唯一标识符
    private String type; // 任务类型
    private String status; // 任务状态：queued, running, success, failed, cancelled, unknown, finalized
    private Object input; // 输入数据对象，结构取决于任务类型
    private Output output; // 包含任务结果的对象
    private int progress; // 任务进度，范围从0到100
    private Instant createTime; // 创建任务的时间戳

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Output {
        private String model; // 下载模型的URL
        private String base_model; // 下载基本模型的URL
        private String pbr_model; // 下载PBR模型的URL
        private String rendered_image; // 模型预览图像的URL
    }
}
