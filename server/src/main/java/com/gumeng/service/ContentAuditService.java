package com.gumeng.service;

import com.alibaba.fastjson.JSONObject;
import com.gumeng.AI.GLMChatService;
import com.gumeng.utils.SensitiveWordFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 内容审核服务
 * 整合基于DFA算法的敏感词过滤和基于大模型的上下文感知审核
 */
@Service
@Slf4j
public class ContentAuditService {

    @Autowired
    private GLMChatService glmChatService;

    private final SensitiveWordFilter sensitiveWordFilter = SensitiveWordFilter.getInstance();

    /**
     * 智能内容审核
     * 
     * @param content 需要审核的内容
     * @return 审核结果对象，包含是否通过、敏感词列表等信息
     */
    public AuditResult auditContent(String content) {
        if (content == null || content.isEmpty()) {
            return new AuditResult(true, new ArrayList<>(), content);
        }

        // 第一步：使用传统敏感词过滤器进行初步检测
        boolean containsSensitiveWords = sensitiveWordFilter.containsSensitiveWord(content);
        
        // 如果没有检测到敏感词，直接通过
        if (!containsSensitiveWords) {
            return new AuditResult(true, new ArrayList<>(), content);
        }
        
        // 获取具体的敏感词列表
        List<String> sensitiveWords = SensitiveWordFilter.getSensitiveWords(content);
        log.info("检测到敏感词: {}", sensitiveWords);
        
        // 第二步：调用GLM-4v-Flash进行上下文感知审核
        try {
            boolean aiApproved = checkWithAI(content, sensitiveWords);
            
            // 如果AI判断内容没问题，则允许通过
            if (aiApproved) {
                log.info("AI判断内容合规，允许发布");
                return new AuditResult(true, sensitiveWords, content);
            } else {
                // AI判断内容有问题，进行敏感词过滤替换
                String filteredContent = sensitiveWordFilter.filter(content);
                log.info("AI判断内容不合规，已过滤敏感词");
                return new AuditResult(false, sensitiveWords, filteredContent);
            }
        } catch (Exception e) {
            log.error("AI审核异常，默认使用传统过滤: {}", e.getMessage());
            // 发生异常时，使用传统过滤方式作为备选
            String filteredContent = sensitiveWordFilter.filter(content);
            return new AuditResult(false, sensitiveWords, filteredContent);
        }
    }
    
    /**
     * 调用GLM-4v-Flash模型判断内容是否合规
     * 
     * @param content 原始内容
     * @param sensitiveWords 检测到的敏感词列表
     * @return true表示AI判断内容合规，false表示不合规
     */
    private boolean checkWithAI(String content, List<String> sensitiveWords) throws Exception {
        List<JSONObject> messages = new ArrayList<>();
        
        // 添加系统提示
        JSONObject systemPrompt = new JSONObject();
        systemPrompt.put("role", "system");
        systemPrompt.put("content", "你是一个专业的内容审核助手，需要判断用户内容是否存在真正的违规。" + 
                "你需要根据上下文理解内容的真实含义，避免机械地判断敏感词。" + 
                "例如，一些医学、教育或新闻讨论可能包含敏感词，但在特定上下文中是合理的。" + 
                "如果内容在上下文中是合适的，即使包含了通常被认为是敏感的词，也应该判断为合规。" +
                "请只回答'合规'或'不合规'。");
        messages.add(systemPrompt);
        
        // 添加用户消息
        JSONObject userPrompt = new JSONObject();
        userPrompt.put("role", "user");
        userPrompt.put("content", "以下内容中检测到可能的敏感词：" + String.join("、", sensitiveWords) + 
                "。请判断在整体上下文中，这段内容是否实际存在不当含义：\n\n" + content);
        messages.add(userPrompt);
        
        // 调用GLM-4v-Flash
        String aiResponse = glmChatService.chat(messages);
        log.info("AI审核结果: {}", aiResponse);
        
        // 修改判断逻辑：只有当回复明确包含"合规"且不包含"不合规"时才判为合规
        boolean isApproved = aiResponse.contains("合规") && !aiResponse.contains("不合规");
        
        if (isApproved) {
            log.info("AI判断内容合规，允许发布");
        } else {
            log.info("AI判断内容不合规，不允许发布");
        }
        
        return isApproved;
    }
    
    /**
     * 审核结果类
     */
    public static class AuditResult {
        private boolean passed;           // 是否通过审核
        private List<String> sensitiveWords; // 检测到的敏感词列表
        private String filteredContent;    // 过滤后的内容
        
        public AuditResult(boolean passed, List<String> sensitiveWords, String filteredContent) {
            this.passed = passed;
            this.sensitiveWords = sensitiveWords;
            this.filteredContent = filteredContent;
        }
        
        public boolean isPassed() {
            return passed;
        }
        
        public List<String> getSensitiveWords() {
            return sensitiveWords;
        }
        
        public String getFilteredContent() {
            return filteredContent;
        }
    }
} 