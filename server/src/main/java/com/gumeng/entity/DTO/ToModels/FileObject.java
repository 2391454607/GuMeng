package com.gumeng.entity.DTO.ToModels;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 功能：
 * 作者：Z
 * 日期：2025/6/4 下午2:54
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileObject {
    private String type;        // "png", "jpeg", "webp"
    private String file_token;  // Tripo3D 上传后返回的 file_token
}
