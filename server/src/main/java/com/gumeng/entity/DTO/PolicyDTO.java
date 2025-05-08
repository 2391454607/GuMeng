package com.gumeng.entity.DTO;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.util.Date;

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

    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Date publishDate;

    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Date effectiveDate;

    private String base64File;  // 用于接收base64编码的PDF文件

}
