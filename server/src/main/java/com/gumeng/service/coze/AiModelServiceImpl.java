package com.gumeng.service.coze;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.gumeng.code.HttpResponse;
import com.gumeng.domain.ModelInfo;
import com.gumeng.entity.bo.ModelBo;
import com.gumeng.entity.bo.TaskBo;
import com.gumeng.entity.vo.model.AiModelInfoVO;
import com.gumeng.enums.AppCode;
import com.gumeng.enums.TaskStatus;
import com.gumeng.utils.QiniuUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 功能：
 * 作者：Z
 * 日期：2025/6/3 下午5:55
 */
@Service
public class AiModelServiceImpl implements AiModelService {

    @Value("${tripo3d.api.key}")
    private String apiToken;


    private final ModelInfoService modelInfoService;

    private final QiniuUtils qiniuUtils;

    public AiModelServiceImpl(ModelInfoService modelInfoService, QiniuUtils qiniuUtils) {
        this.modelInfoService = modelInfoService;
        this.qiniuUtils = qiniuUtils;
    }


    //查询任务状态
    @Override
    public ModelBo<TaskBo> taskPolling(String taskId) {
        Map<String,String> headers=new HashMap<>();
        headers.put("Authorization","Bearer "+apiToken);

        String body = HttpRequest
                .get("https://api.tripo3d.ai/v2/openapi/task/"+taskId)
                .addHeaders(headers)
                .execute()
                .body();

        Map<String,Object> result=JSONObject.parseObject(body);
        TaskBo taskBo = JSONObject.parseObject(result.get("data").toString(), TaskBo.class);

        ModelBo<TaskBo> modelBo=new ModelBo<>();
        modelBo.setData(taskBo);
        modelBo.setSuccess(true);

        return modelBo;
    }

    //查询任务并处理状态
    @Override
    public HttpResponse taskText(String taskId) {
        QueryWrapper<ModelInfo> modelInfoQueryWrapper = new QueryWrapper<>();
        modelInfoQueryWrapper.eq("task_id", taskId);
        ModelInfo modelInfo = modelInfoService.getOne(modelInfoQueryWrapper);
        if (modelInfo == null) {
            return HttpResponse.error("模型id不存在，请重试");
        }
        modelInfo.setValue(modelInfo.getValue());
        if (modelInfo.getStatus().equals("init") ||
                modelInfo.getStatus().equals(TaskStatus.QUEUED.getValue())
                || modelInfo.getStatus().equals(TaskStatus.RUNNING.getValue())) {
            ModelBo<TaskBo> modelBo = taskPolling(taskId);
            TaskStatus taskStatus = TaskStatus.fromValue(modelBo.getData().getStatus());
            modelStatusUpdate(taskId, modelBo.getData().getStatus());
            switch (taskStatus) {
                case QUEUED:
                    // 处理排队任务的逻辑
                    return HttpResponse.success(AppCode.MODEL_QUEUED);
                case RUNNING:
                    // 处理运行中的任务的逻辑
                    modelStatusUpdate(taskId, modelInfo.getValue());
                    return HttpResponse.success(AppCode.MODEL_RUNNING);
                case SUCCESS:
                    //处理模型完成
                    UpdateWrapper<ModelInfo> updateWrapper = new UpdateWrapper<>();
                    updateWrapper.eq("task_id", taskId);
                    modelInfo.setStatus(taskStatus.getValue());
                    // 渲染图
                    byte[] renderBytes = HttpUtil.downloadBytes(modelBo.getData().getOutput().getRendered_image());
                    String renderFileName = getFileNameFromUrl(modelBo.getData().getOutput().getRendered_image());
                    String renderQiniuUrl = qiniuUtils.uploadModelImage(renderBytes, renderFileName);
                    modelInfo.setRenderUrl(renderQiniuUrl);
                    // pbr_model
                    byte[] pbrModelBytes = HttpUtil.downloadBytes(modelBo.getData().getOutput().getPbr_model());
                    String pbrModelFileName = getFileNameFromUrl(modelBo.getData().getOutput().getPbr_model());
                    String pbrModelQiniuUrl = qiniuUtils.uploadModelFile(pbrModelBytes, pbrModelFileName);
                    modelInfo.setPbrModelUrl(pbrModelQiniuUrl);
                    modelInfoService.update(modelInfo, updateWrapper);
                    AiModelInfoVO result=new AiModelInfoVO(modelInfo);
                    return HttpResponse.success(AppCode.MODEL_SUCCESS,result);
                case FAILED:
                    // 处理失败任务的逻辑
                    return HttpResponse.success(AppCode.MODEL_FAILED);
                case CANCELLED:
                    // 处理已取消任务的逻辑
                    return HttpResponse.success(AppCode.MODEL_CANCELLED);
                case UNKNOWN:
                    // 处理未知状态的逻辑
                    return HttpResponse.success(AppCode.MODEL_UNKNOWN);
                default:
                    // 默认情况下处理未预料的状态
                    return HttpResponse.success(AppCode.MODEL_UNKNOWN);
            }
        } else {
            AiModelInfoVO result=new AiModelInfoVO(modelInfo);
            return HttpResponse.success(result);
        }
    }


    /**
     * 从给定的URL中提取文件名。
     *
     * @param fileUrl 文件的URL地址
     * @return 文件名（不含路径）
     */
    private String getFileNameFromUrl(String fileUrl) {
        try {
            String decodedUrl = URLDecoder.decode(fileUrl, StandardCharsets.UTF_8);
            URL url = new URL(decodedUrl);
            String path = url.getPath();
            return java.nio.file.Paths.get(path).getFileName().toString();
        } catch (MalformedURLException e) {
            return e.getMessage();
        }
    }

    /**
     * 模型生成状态更新
     */
    private void modelStatusUpdate(String taskId, String value) {
        UpdateWrapper<ModelInfo> modelInfoUpdateWrapper = new UpdateWrapper<>();
        modelInfoUpdateWrapper.eq("task_id", taskId).set("status", value);
        modelInfoService.update(modelInfoUpdateWrapper);
    }
}
