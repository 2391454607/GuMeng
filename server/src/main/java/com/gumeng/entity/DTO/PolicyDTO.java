package com.gumeng.entity.DTO;

import lombok.Data;

import java.io.Serializable;

/**
 * 功能：非遗政策上传功能DTO
 * 作者：Z
 * 日期：2025/5/6 上午8:08
 */
@Data
public class PolicyDTO {

    private Integer id;

    private String title;

    private String type;

    private String documentNumber;

    private String publishOrg;

    private String publishDate;

    private String effectiveDate;

    private String base64File;  // 用于接收base64编码的PDF文件

}
