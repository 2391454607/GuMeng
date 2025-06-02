import { http } from "@/utils/http.js";

// 发送消息到AI助手
export function sendAssistantMessage(messages) {
  return http.post('/ai/chat', {
    messages
  });
} 