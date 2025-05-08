package com.gumeng.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

import lombok.Data;

/**
 * 非遗政策表
 * @TableName policy
 */
@TableName(value ="policy")
@Data
public class Policy implements Serializable {
    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 政策标题
     */
    @TableField(value = "title")
    private String title;

    /**
     * 政策类型
     */
    @TableField(value = "type")
    private String type;

    /**
     * 发文字号
     */
    @TableField(value = "document_number")
    private String documentNumber;

    /**
     * 发布机构
     */
    @TableField(value = "publish_org")
    private String publishOrg;

    /**
     * 发布日期
     */
    @TableField(value = "publish_date")
    private Date publishDate;

    /**
     * 生效日期
     */
    @TableField(value = "effective_date")
    private Date effectiveDate;

    /**
     * 政策内容PDF格式
     */
    @TableField(value = "content")
    private byte[] content;

    /**
     * 附件URL
     */
    @TableField(value = "attachment_url")
    private String attachmentUrl;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    private LocalDateTime updateTime;

    /**
     * 删除状态
     */
    @TableField(value = "is_delete")
    private String isDelete;

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
        Policy other = (Policy) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getTitle() == null ? other.getTitle() == null : this.getTitle().equals(other.getTitle()))
            && (this.getType() == null ? other.getType() == null : this.getType().equals(other.getType()))
            && (this.getDocumentNumber() == null ? other.getDocumentNumber() == null : this.getDocumentNumber().equals(other.getDocumentNumber()))
            && (this.getPublishOrg() == null ? other.getPublishOrg() == null : this.getPublishOrg().equals(other.getPublishOrg()))
            && (this.getPublishDate() == null ? other.getPublishDate() == null : this.getPublishDate().equals(other.getPublishDate()))
            && (this.getEffectiveDate() == null ? other.getEffectiveDate() == null : this.getEffectiveDate().equals(other.getEffectiveDate()))
            && (this.getContent() == null ? other.getContent() == null : this.getContent().equals(other.getContent()))
            && (this.getAttachmentUrl() == null ? other.getAttachmentUrl() == null : this.getAttachmentUrl().equals(other.getAttachmentUrl()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()))
            && (this.getIsDelete() == null ? other.getIsDelete() == null : this.getIsDelete().equals(other.getIsDelete()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getTitle() == null) ? 0 : getTitle().hashCode());
        result = prime * result + ((getType() == null) ? 0 : getType().hashCode());
        result = prime * result + ((getDocumentNumber() == null) ? 0 : getDocumentNumber().hashCode());
        result = prime * result + ((getPublishOrg() == null) ? 0 : getPublishOrg().hashCode());
        result = prime * result + ((getPublishDate() == null) ? 0 : getPublishDate().hashCode());
        result = prime * result + ((getEffectiveDate() == null) ? 0 : getEffectiveDate().hashCode());
        result = prime * result + ((getContent() == null) ? 0 : getContent().hashCode());
        result = prime * result + ((getAttachmentUrl() == null) ? 0 : getAttachmentUrl().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
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
        sb.append(", title=").append(title);
        sb.append(", type=").append(type);
        sb.append(", documentNumber=").append(documentNumber);
        sb.append(", publishOrg=").append(publishOrg);
        sb.append(", publishDate=").append(publishDate);
        sb.append(", effectiveDate=").append(effectiveDate);
        sb.append(", content=").append(content);
        sb.append(", attachmentUrl=").append(attachmentUrl);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", isDelete=").append(isDelete);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}