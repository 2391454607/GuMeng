package com.gumeng.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

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
    private String documentNumber;

    /**
     * 发布机构
     */
    private String publishOrg;

    /**
     * 发布日期
     */
    private LocalDate publishDate;

    /**
     * 生效日期
     */
    private LocalDate effectiveDate;

}
