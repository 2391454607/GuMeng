package com.gumeng.entity.DTO.ToModels;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 功能：图片生成模型
 * 作者：Z
 * 日期：2025/6/4 上午10:26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageToModelRequest {

    private final String type = "image_to_model";

    @NotBlank(message = "模型描述不能为空")
    private String prompt;

    // 方式一：使用文件对象上传（推荐）
    private FileObject file;

    // 方式二（可选）：使用图片 URL 上传
    //图片的直接 URL 支持 JPEG 和 PNG 格式，最大尺寸为 20MB。
    private String url;

    //texture（可选）：用于启用纹理的布尔选项。默认值为True，设置False为获取不带任何纹理的基础模型。
    private Boolean texture;

    //auto_size（可选）：自动将模型缩放至真实尺寸，单位为米。默认值为False。
    private Boolean autoSize;

    //style（可选）：定义要应用于 3D 模型的艺术样式或变换，并根据预设选项改变其外观。忽略此选项可保留原始样式和外观。
    //realistic：写实风格，生成的模型具有高度的现实感，适用于需要逼真效果的场景。
    //cartoon：卡通风格，生成的模型具有简化和夸张的特征，适用于动画或游戏风格的设计。
    //cyberpunk：赛博朋克风格，生成的模型具有未来科技感和都市氛围，适用于科幻题材的创作。
    private String style;
}
