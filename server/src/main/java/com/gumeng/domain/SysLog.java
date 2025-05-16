package com.gumeng.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统操作日志表
 * @TableName sys_log
 */
@TableName(value ="sys_log")
@Data
public class SysLog implements Serializable {
    /**
     * 日志ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 操作用户ID
     */
    @TableField(value = "user_id")
    private Integer userId;

    /**
     * 操作用户名
     */
    @TableField(value = "username")
    private String username;

    /**
     * 操作模块
     */
    @TableField(value = "module")
    private String module;

    /**
     * 操作类型
     */
    @TableField(value = "operation")
    private String operation;

    /**
     * 操作IP地址
     */
    @TableField(value = "ip")
    private String ip;

    /**
     * IP归属地
     */
    @TableField(value = "location")
    private String location;

    /**
     * 操作状态(0失败,1成功)
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private LocalDateTime createTime;

    /**
     * 逻辑删除(0未删除,1已删除)
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
        SysLog other = (SysLog) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
                && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
                && (this.getUsername() == null ? other.getUsername() == null : this.getUsername().equals(other.getUsername()))
                && (this.getModule() == null ? other.getModule() == null : this.getModule().equals(other.getModule()))
                && (this.getOperation() == null ? other.getOperation() == null : this.getOperation().equals(other.getOperation()))
                && (this.getIp() == null ? other.getIp() == null : this.getIp().equals(other.getIp()))
                && (this.getLocation() == null ? other.getLocation() == null : this.getLocation().equals(other.getLocation()))
                && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
                && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
                && (this.getIsDelete() == null ? other.getIsDelete() == null : this.getIsDelete().equals(other.getIsDelete()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getUsername() == null) ? 0 : getUsername().hashCode());
        result = prime * result + ((getModule() == null) ? 0 : getModule().hashCode());
        result = prime * result + ((getOperation() == null) ? 0 : getOperation().hashCode());
        result = prime * result + ((getIp() == null) ? 0 : getIp().hashCode());
        result = prime * result + ((getLocation() == null) ? 0 : getLocation().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
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
        sb.append(", userId=").append(userId);
        sb.append(", username=").append(username);
        sb.append(", module=").append(module);
        sb.append(", operation=").append(operation);
        sb.append(", ip=").append(ip);
        sb.append(", location=").append(location);
        sb.append(", status=").append(status);
        sb.append(", createTime=").append(createTime);
        sb.append(", isDelete=").append(isDelete);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}