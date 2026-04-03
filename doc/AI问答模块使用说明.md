# AI问答模块使用说明

## 功能概述

AI问答模块基于Spring AI + Ollama实现,支持RAG(检索增强生成)功能。主要功能包括:

1. **AI智能问答**: 用户提问,AI基于大模型生成答案
2. **RAG知识库检索**: 基于pgvector实现向量相似度搜索,检索相关知识片段增强答案
3. **对话历史管理**: 保存和查看历史问答记录
4. **文档管理**: 管理知识库文档,自动生成向量嵌入

## 技术栈

- **后端**: Spring Boot 3.3.0 + Spring AI 1.0.0-M5 + MyBatis-Plus
- **AI模型**: Ollama (deepseek-r1:8b 聊天模型, nomic-embed-text 向量模型)
- **数据库**: PostgreSQL + pgvector
- **前端**: Vue 2 + Element UI

## 环境准备

### 1. 安装Ollama

```bash
# Windows/Mac/Linux 安装Ollama
# 访问 https://ollama.com 下载安装
```

### 2. 下载AI模型

```bash
# 下载聊天模型
ollama pull deepseek-r1:8b

# 下载向量模型
ollama pull nomic-embed-text
```

### 3. 启动Ollama服务

```bash
# Ollama默认运行在 http://localhost:11434
# 确认Ollama服务已启动
curl http://localhost:11434/api/tags
```

### 4. 安装pgvector扩展

```sql
-- 在PostgreSQL中启用pgvector扩展
CREATE EXTENSION IF NOT EXISTS vector;
```

### 5. 初始化数据库

执行SQL脚本创建相关表:

```bash
psql -U postgres -d 3DAGPL -f sql/ai_chat_module.sql
```

或手动执行 `sql/ai_chat_module.sql` 中的SQL语句。

## 配置说明

### application.yml 配置

```yaml
spring:
  ai:
    ollama:
      base-url: http://127.0.0.1:11434
      chat:
        model: deepseek-r1:8b
      embedding:
        model: nomic-embed-text

# AI RAG 配置
ai:
  rag:
    topk: 5  # 默认检索TopK数量
```

## 使用指南

### 1. 添加知识库文档

通过API或后台管理界面添加文档到知识库:

```java
AiDocument document = new AiDocument();
document.setTitle("文档标题");
document.setContent("文档内容...");
aiDocumentService.insertAiDocument(document);
```

### 2. 生成向量嵌入

需要将文档内容转换为向量并存储到ai_embedding表中。可以使用以下方法:

```java
// 生成向量
float[] embedding = embeddingModel.embed(document.getContent());

// 创建向量记录
AiEmbedding aiEmbedding = new AiEmbedding();
aiEmbedding.setDocumentId(document.getId());
aiEmbedding.setContent(document.getContent());
aiEmbedding.setEmbedding(new PGvector(embedding));
aiEmbeddingService.insertAiEmbedding(aiEmbedding);
```

### 3. 使用AI问答

前端访问 `/AI/aichat` 页面,或使用API:

```javascript
// 前端API调用
import { chatAi } from "@/api/system/aichat";

chatAi({
  question: "你的问题",
  useRag: true,  // 是否使用知识库
  topK: 5        // 检索知识片段数量
}).then(response => {
  console.log(response.data.answer);
});
```

### 4. 查看对话历史

```javascript
import { listChatHistory } from "@/api/system/aichat";

listChatHistory({ pageNum: 1, pageSize: 10 }).then(response => {
  console.log(response.rows);
});
```

## API接口说明

### AI问答接口

- **URL**: `POST /system/ai/chat/chat`
- **权限**: `system:aichat:chat`
- **请求体**:
```json
{
  "question": "问题内容",
  "useRag": true,
  "topK": 5
}
```
- **响应**:
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "id": 1,
    "question": "问题内容",
    "answer": "AI生成的答案",
    "knowledgeContext": "参考的知识片段",
    "userId": 1,
    "createTime": "2024-01-01 12:00:00"
  }
}
```

### 对话历史列表

- **URL**: `GET /system/ai/chat/history/list`
- **权限**: `system:aichat:list`
- **参数**: pageNum, pageSize, question(可选)

### 删除对话历史

- **URL**: `DELETE /system/ai/chat/history/{ids}`
- **权限**: `system:aichat:remove`

## RAG工作流程

```
用户提问
   ↓
生成问题向量 (使用nomic-embed-text模型)
   ↓
pgvector相似度搜索 (检索TopK知识片段)
   ↓
拼接Prompt (用户问题 + 参考知识)
   ↓
LLM生成答案 (使用deepseek-r1:8b模型)
   ↓
返回结果 (答案 + 参考知识上下文)
   ↓
保存对话历史
```

## 权限配置

需要在系统菜单中添加以下权限:

- `system:aichat:chat` - AI问答
- `system:aichat:list` - 查看对话历史
- `system:aichat:query` - 查看对话详情
- `system:aichat:remove` - 删除对话历史
- `system:document:list` - 查看文档列表
- `system:document:query` - 查看文档详情
- `system:document:add` - 添加文档
- `system:document:edit` - 编辑文档
- `system:document:remove` - 删除文档

## 注意事项

1. **Ollama服务**: 确保Ollama服务已启动并可访问
2. **向量模型**: 确保已下载nomic-embed-text向量模型
3. **pgvector扩展**: 确保PostgreSQL已安装并启用pgvector扩展
4. **向量维度**: ai_embedding表的embedding字段维度需与向量模型匹配(默认1536)
5. **性能优化**: 大量数据时建议配置合适的TopK值和索引参数

## 常见问题

### Q: 调用AI接口超时

A: 检查以下几点:
1. Ollama服务是否正常运行
2. 模型是否已下载
3. application.yml中的base-url配置是否正确
4. 考虑增加超时时间配置

### Q: 向量搜索结果为空

A: 检查:
1. ai_embedding表中是否有数据
2. pgvector扩展是否已启用
3. 向量维度是否匹配

### Q: 前端页面无法访问

A: 确认:
1. 前端路由是否已配置
2. 用户是否有访问权限
3. API接口路径是否正确

## 扩展开发

### 添加PDF上传功能

1. 前端添加文件上传组件
2. 后端添加PDF解析功能(使用Apache PDFBox)
3. 将解析后的文本存储到ai_document表
4. 生成向量并存储到ai_embedding表

### 支持多轮对话

1. 修改AiChatRequestVO添加conversationId字段
2. 在Service层维护对话上下文
3. 使用Spring AI的MessageHistory保存对话历史
4. 在多轮对话中传递历史消息上下文

### 流式输出

1. 使用Spring AI的StreamingChatModel
2. 后端实现SSE(Server-Sent Events)
3. 前端使用EventSource接收流式响应
4. 实时显示生成的文本

## 更新日志

### v1.0.0 (2024-01-01)
- 初始版本发布
- 支持基础AI问答功能
- 支持RAG知识库检索
- 支持对话历史管理
