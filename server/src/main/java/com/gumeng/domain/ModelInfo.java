package com.gumeng.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 功能：模型生成信息
 * 作者：Z
 * 日期：2025/6/3 下午7:24
 */
@TableName(value ="model_info")
@Data
public class ModelInfo implements Serializable {
    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private String id;

    /**
     * 用户id
     */
    @TableField(value = "user_uid")
    private String userUid;

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
     * 生成类型（纹理,文本，图片，多视图,优化）
     */
    @TableField(value = "type")
    private String type;

    /**
     * 生成提示值(json)
     */
    @TableField(value = "value")
    private String value;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private LocalDateTime createTime;

    /**
     * 公共模型
     */
    @TableField(value = "is_common")
    private Integer isCommon;

    /**
     * 禁用模型
     */
    @TableField(value = "is_disable")
    private Integer isDisable;

    /**
     * 禁用原因
     */
    @TableField(value = "disable_cause")
    private String disableCause;

    /**
     * 拓展配置
     */
    @TableField(value = "model_extra")
    private String modelExtra;

    /**
     * 逻辑删除
     */
    @TableField(value = "is_delete")
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        ModelInfo other = (ModelInfo) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
                && (this.getUserUid() == null ? other.getUserUid() == null : this.getUserUid().equals(other.getUserUid()))
                && (this.getTaskId() == null ? other.getTaskId() == null : this.getTaskId().equals(other.getTaskId()))
                && (this.getModelUrl() == null ? other.getModelUrl() == null : this.getModelUrl().equals(other.getModelUrl()))
                && (this.getRenderUrl() == null ? other.getRenderUrl() == null : this.getRenderUrl().equals(other.getRenderUrl()))
                && (this.getBaseModelUrl() == null ? other.getBaseModelUrl() == null : this.getBaseModelUrl().equals(other.getBaseModelUrl()))
                && (this.getPbrModelUrl() == null ? other.getPbrModelUrl() == null : this.getPbrModelUrl().equals(other.getPbrModelUrl()))
                && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
                && (this.getType() == null ? other.getType() == null : this.getType().equals(other.getType()))
                && (this.getValue() == null ? other.getValue() == null : this.getValue().equals(other.getValue()))
                && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
                && (this.getIsCommon() == null ? other.getIsCommon() == null : this.getIsCommon().equals(other.getIsCommon()))
                && (this.getIsDisable() == null ? other.getIsDisable() == null : this.getIsDisable().equals(other.getIsDisable()))
                && (this.getDisableCause() == null ? other.getDisableCause() == null : this.getDisableCause().equals(other.getDisableCause()))
                && (this.getModelExtra() == null ? other.getModelExtra() == null : this.getModelExtra().equals(other.getModelExtra()))
                && (this.getIsDelete() == null ? other.getIsDelete() == null : this.getIsDelete().equals(other.getIsDelete()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getUserUid() == null) ? 0 : getUserUid().hashCode());
        result = prime * result + ((getTaskId() == null) ? 0 : getTaskId().hashCode());
        result = prime * result + ((getModelUrl() == null) ? 0 : getModelUrl().hashCode());
        result = prime * result + ((getRenderUrl() == null) ? 0 : getRenderUrl().hashCode());
        result = prime * result + ((getBaseModelUrl() == null) ? 0 : getBaseModelUrl().hashCode());
        result = prime * result + ((getPbrModelUrl() == null) ? 0 : getPbrModelUrl().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getType() == null) ? 0 : getType().hashCode());
        result = prime * result + ((getValue() == null) ? 0 : getValue().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getIsCommon() == null) ? 0 : getIsCommon().hashCode());
        result = prime * result + ((getIsDisable() == null) ? 0 : getIsDisable().hashCode());
        result = prime * result + ((getDisableCause() == null) ? 0 : getDisableCause().hashCode());
        result = prime * result + ((getModelExtra() == null) ? 0 : getModelExtra().hashCode());
        result = prime * result + ((getIsDelete() == null) ? 0 : getIsDelete().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", userUid=").append(userUid);
        sb.append(", taskId=").append(taskId);
        sb.append(", modelUrl=").append(modelUrl);
        sb.append(", renderUrl=").append(renderUrl);
        sb.append(", baseModelUrl=").append(baseModelUrl);
        sb.append(", pbrModelUrl=").append(pbrModelUrl);
        sb.append(", status=").append(status);
        sb.append(", type=").append(type);
        sb.append(", value=").append(value);
        sb.append(", createTime=").append(createTime);
        sb.append(", isCommon=").append(isCommon);
        sb.append(", isDisable=").append(isDisable);
        sb.append(", disableCause=").append(disableCause);
        sb.append(", modelExtra=").append(modelExtra);
        sb.append(", isDelete=").append(isDelete);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}
