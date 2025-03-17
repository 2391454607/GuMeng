package com.gumeng.entity;

import lombok.Data;

/**
 * 功能：
 * 作者：Z
 * 日期：2025/3/17 下午4:26
 */
@Data
public class Menu {

    private Integer id;  //主键id

    private String name;  //菜单名称

    private String type;  //菜单类型

    private String url;   //路径

    private String permission;  //

    private String icon;  //图标

    private Integer parent;  //父菜单id，没有为0

    private boolean isDelete;  //逻辑删除
}
