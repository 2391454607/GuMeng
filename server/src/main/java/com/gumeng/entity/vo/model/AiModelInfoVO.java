package com.gumeng.entity.vo.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gumeng.domain.ModelInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 功能：
 * 作者：Z
 * 日期：2025/6/3 下午7:42
 */
@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiModelInfoVO implements Serializable  {
    private String id;
    private String userUid;
    private String taskId;
    private String modelUrl;
    private String renderUrl;
    private String baseModelUrl;
    private String pbrModelUrl;
    private String status;
    private String type;
    private Map<String, Object> value;
    private LocalDateTime createTime;
    private Integer isDelete;
    private Integer isCommon;
    private Integer isDisable;
    private String disableCause;
    private Map<String, Object> modelExtra;

    @Serial
    private static final long serialVersionUID = 1L;

    // Jackson ObjectMapper for JSON serialization/deserialization
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 构造函数，从ModelInfo对象创建ModelInfoVo对象
     */
    public AiModelInfoVO(ModelInfo modelInfo) {
        this.id = modelInfo.getId();
        this.userUid = modelInfo.getUserUid();
        this.taskId = modelInfo.getTaskId();
        this.modelUrl = modelInfo.getModelUrl();
        this.renderUrl = modelInfo.getRenderUrl();
        this.baseModelUrl = modelInfo.getBaseModelUrl();
        this.pbrModelUrl = modelInfo.getPbrModelUrl();
        this.status = modelInfo.getStatus();
        this.type = modelInfo.getType();

        // 将JSON字符串反序列化为Map
        this.value = convertJsonToMap(modelInfo.getValue());
        this.createTime = modelInfo.getCreateTime();
        this.isDelete = modelInfo.getIsDelete();
        this.isCommon = modelInfo.getIsCommon();
        this.isDisable = modelInfo.getIsDisable();
        this.disableCause = modelInfo.getDisableCause();

        // 将JSON字符串反序列化为Map
        this.modelExtra = convertJsonToMap(modelInfo.getModelExtra());
    }

    /**
     * 将JSON字符串转换为Map
     */
    private Map<String, Object> convertJsonToMap(String json) {
        if (json == null || json.isEmpty()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.error("Failed to convert JSON to Map: {}", e.getMessage());
            return new HashMap<>();
        }
    }
}
