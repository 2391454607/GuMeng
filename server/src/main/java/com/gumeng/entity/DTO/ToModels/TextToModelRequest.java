package com.gumeng.entity.DTO.ToModels;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 功能：
 * 作者：Z
 * 日期：2025/6/3 上午10:44
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TextToModelRequest {

    private final String type = "text_to_model";

    //指导模型生成的文本输入。
    @NotBlank(message = "模型描述不能为空")
    private String prompt;

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
