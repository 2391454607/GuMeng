package com.gumeng.service.coze;

import com.gumeng.code.HttpResponse;
import com.gumeng.entity.bo.ModelBo;
import com.gumeng.entity.bo.TaskBo;

/**
 * 功能：
 * 作者：Z
 * 日期：2025/6/3 下午4:52
 */
public interface AiModelService {
    ModelBo<TaskBo> taskPolling(String taskId);

    HttpResponse taskText(String taskId);

}
