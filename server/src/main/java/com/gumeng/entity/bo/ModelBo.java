package com.gumeng.entity.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 功能：
 * 作者：Z
 * 日期：2025/6/3 下午4:53
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ModelBo<T> {
    private boolean success;
    private T data;
    private String msg;
}
