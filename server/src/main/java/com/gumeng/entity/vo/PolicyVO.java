package com.gumeng.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 功能：非遗政策封装类
 * 作者：Z
 * 日期：2025/4/11 上午10:13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PolicyVO {
    /**
     * id
     */
    private Integer id;

    /**
     * 政策标题
     */
    private String title;

    /**
     * 政策类型
     */
    private String type;

    /**
     * 发文字号
     */
    private Integer documentNumber;

    /**
     * 发布机构
     */
    private String publishOrg;

    /**
     * 发布日期
     */
    private LocalDateTime publishDate;

    /**
     * 生效日期
     */
    private LocalDateTime effectiveDate;

    /**
     * 政策内容
     */
    private String content;

    /**
     * 附件URL
     */
    private String attachmentUrl;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
