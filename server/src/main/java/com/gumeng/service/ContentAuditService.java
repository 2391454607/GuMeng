package com.gumeng.service;

import com.alibaba.fastjson.JSONObject;
import com.gumeng.AI.GLMChatService;
import com.gumeng.utils.SensitiveWordFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

    // 内容审核结果缓存，用于确保短期内相同内容的审核结果一致
    private static final Map<String, CachedAuditResult> auditCache = new ConcurrentHashMap<>();
    // 缓存过期时间（10分钟）
    private static final long CACHE_EXPIRY_MS = 10 * 60 * 1000;

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
                "你必须只回答\"合规\"或\"不合规\"这两个词中的一个，不能有任何其他文字。");
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
        
        // 使用标准化处理响应
        String normalizedResponse = normalizeAIResponse(aiResponse);
        
        // 修改判断逻辑：只检查是否为"合规"
        boolean isApproved = normalizedResponse.equals("合规");
        
        if (isApproved) {
            log.info("AI判断内容合规，允许发布");
        } else {
            log.info("AI判断内容不合规，不允许发布");
        }
        
        return isApproved;
    }
    
    /**
     * 对内容进行全面的AI审核，不依赖敏感词检测
     * 该方法可以捕获不在敏感词库中的有害内容，如侮辱性语言、隐晦攻击等
     *
     * @param content 需要审核的内容
     * @return true表示内容合规，false表示内容不合规
     * @throws Exception 如果AI服务调用失败
     */
    public boolean checkFullContentWithAI(String content) throws Exception {
        if (content == null || content.isEmpty()) {
            return true;
        }
        
        // 计算内容的哈希值用于缓存键
        String contentHash = DigestUtils.md5DigestAsHex(content.getBytes());
        
        // 检查缓存中是否有该内容的审核结果
        CachedAuditResult cachedResult = auditCache.get(contentHash);
        if (cachedResult != null && !cachedResult.isExpired()) {
            log.info("使用缓存的AI审核结果: {}", cachedResult.isApproved() ? "合规" : "不合规");
            return cachedResult.isApproved();
        }

        List<JSONObject> messages = new ArrayList<>();
        
        // 设置增强版系统提示，强调返回格式
        JSONObject systemPrompt = new JSONObject();
        systemPrompt.put("role", "system");
        systemPrompt.put("content", 
            "你是一个专业的内容审核助手，需要全面判断用户内容是否存在违规。请特别注意以下内容类型：\n" +
            "1. 侮辱性语言：如人身攻击、嘲笑残疾、歧视性言论等\n" +
            "2. 隐晦的攻击：使用比喻、拐弯抹角的方式攻击他人\n" +
            "3. 暴力威胁：任何形式的暴力或伤害威胁\n" +
            "4. 不当内容：色情、赌博、毒品等\n" +
            "5. 社会负面影响：传播仇恨、分裂言论\n\n" +
            "同时，请理解合理的上下文，例如医学讨论、新闻报道、教育内容中可能需要提及敏感话题。\n" +
            "你必须只回答\"合规\"或\"不合规\"这两个词中的一个，不能有任何其他文字。"
        );
        messages.add(systemPrompt);
        
        // 添加用户消息
        JSONObject userPrompt = new JSONObject();
        userPrompt.put("role", "user");
        userPrompt.put("content", "请审核以下内容，判断是否存在任何不当、侮辱、歧视、攻击性表达或其他违规内容：\n\n" + content);
        messages.add(userPrompt);
        
        // 调用GLM-4v-Flash
        String aiResponse = glmChatService.chat(messages);
        log.info("全文AI审核结果: {}", aiResponse);
        
        // 格式化和标准化AI响应
        String normalizedResponse = normalizeAIResponse(aiResponse);
        
        // 判断逻辑：只有明确包含"合规"且不包含"不合规"时才判为合规
        boolean isApproved = normalizedResponse.equals("合规");
        
        if (isApproved) {
            log.info("AI全文审核判断内容合规");
        } else {
            log.info("AI全文审核判断内容不合规: {}", content);
        }
        
        // 缓存审核结果
        auditCache.put(contentHash, new CachedAuditResult(isApproved, System.currentTimeMillis()));
        
        return isApproved;
    }
    
    /**
     * 标准化AI响应，确保只返回"合规"或"不合规"
     */
    private String normalizeAIResponse(String response) {
        if (response == null) {
            return "不合规";
        }
        
        // 去除多余空白字符和标点
        String normalized = response.trim()
            .replaceAll("[.,;:\"'!?()\\[\\]{}]", "")
            .replaceAll("\\s+", " ");
        
        // 检查是否明确包含关键词
        if (normalized.contains("合规") && !normalized.contains("不合规")) {
            return "合规";
        } else {
            return "不合规";
        }
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
    
    /**
     * 缓存的审核结果类
     */
    private static class CachedAuditResult {
        private final boolean approved;
        private final long timestamp;
        
        public CachedAuditResult(boolean approved, long timestamp) {
            this.approved = approved;
            this.timestamp = timestamp;
        }
        
        public boolean isApproved() {
            return approved;
        }
        
        public boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_EXPIRY_MS;
        }
    }
} 