package com.gumeng.entity.vo.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * 功能：
 * 作者：Z
 * 日期：2025/6/8 下午3:44
 */
@Data
public class ToModelInfoVO {
    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private String id;

    /**
     * 任务id
     */
    @TableField(value = "task_id")
    private String taskId;

    /**
     * 模型地址
     */
    @TableField(value = "model_url")
    private String modelUrl;

    /**
     * 预览图
     */
    @TableField(value = "render_url")
    private String renderUrl;

    /**
     * 基础模型路径
     */
    @TableField(value = "base_model_url")
    private String baseModelUrl;

    /**
     * pbr模型的 URL
     */
    @TableField(value = "pbr_model_url")
    private String pbrModelUrl;

    /**
     * 生成状态
     */
    @TableField(value = "status")
    private String status;


    /**
     * 逻辑删除
     */
    @TableField(value = "is_delete")
    private Integer isDelete;

}
