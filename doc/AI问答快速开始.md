# AI问答模块快速开始指南

## 一、环境准备 (5分钟)

### 1. 安装并启动Ollama

```bash
# Windows用户: 从 https://ollama.com 下载安装包
# 安装后Ollama会自动启动

# 验证安装
ollama --version
```

### 2. 下载AI模型

打开命令行执行:

```bash
# 下载聊天模型 (约4-5GB)
ollama pull deepseek-r1:8b

# 下载向量模型 (约270MB)
ollama pull nomic-embed-text

# 查看已下载的模型
ollama list
```

### 3. 启动Ollama服务

```bash
# Windows: Ollama通常作为后台服务自动启动
# 验证服务是否运行
curl http://localhost:11434/api/tags
```

### 4. 配置PostgreSQL的pgvector扩展

```sql
-- 连接到你的数据库
psql -U postgres -d 3DAGPL

-- 启用pgvector扩展
CREATE EXTENSION IF NOT EXISTS vector;

-- 验证扩展已安装
SELECT * FROM pg_extension WHERE extname = 'vector';
```

## 二、数据库初始化 (2分钟)

### 方式一: 使用命令行

```bash
# 在项目根目录执行
psql -U postgres -d 3DAGPL -f sql/ai_chat_module.sql
```

### 方式二: 使用数据库管理工具

1. 打开Navicat/DBeaver等工具
2. 连接到PostgreSQL数据库
3. 打开并执行 `sql/ai_chat_module.sql` 文件
4. 确认创建了以下表:
   - `ai_document` - 文档表
   - `ai_embedding` - 向量表
   - `ai_chat_history` - 对话历史表

## 三、启动后端服务 (2分钟)

### 1. 确认配置文件

检查 `threedagpl-admin/src/main/resources/application.yml`:

```yaml
spring:
  ai:
    ollama:
      base-url: http://127.0.0.1:11434
      chat:
        model: deepseek-r1:8b
      embedding:
        model: nomic-embed-text

ai:
  rag:
    topk: 5
```

### 2. 启动项目

```bash
# Windows
ry.bat

# 或在IDE中直接运行 RuoYiApplication
```

### 3. 验证启动成功

访问: http://localhost:8080

## 四、测试AI问答 (3分钟)

### 方式一: 使用前端界面

1. 登录系统
2. 导航到 **AI问答** 菜单 (如果没有,需要添加菜单)
3. 在输入框中输入问题
4. 点击"发送"按钮
5. 等待AI回复

### 方式二: 使用API测试工具 (Postman/Apifox)

#### 1. 测试AI问答 (不使用RAG)

```http
POST http://localhost:8080/system/ai/chat/chat
Content-Type: application/json
Authorization: Bearer {your_token}

{
  "question": "你好,请介绍一下自己",
  "useRag": false,
  "topK": 0
}
```

预期响应:
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "question": "你好,请介绍一下自己",
    "answer": "你好!我是基于DeepSeek模型的AI助手...",
    "knowledgeContext": null,
    "createTime": "2024-01-01 12:00:00"
  }
}
```

#### 2. 添加测试文档

```http
POST http://localhost:8080/system/ai/document
Content-Type: application/json
Authorization: Bearer {your_token}

{
  "title": "测试文档",
  "content": "Spring Boot是一个用于简化Spring应用开发的框架。它采用约定优于配置的理念,让开发者能够快速创建独立的、生产级别的Spring应用。"
}
```

#### 3. 为文档生成向量

```http
POST http://localhost:8080/system/ai/embedding/generate/{documentId}
Authorization: Bearer {your_token}
```

#### 4. 测试RAG问答

```http
POST http://localhost:8080/system/ai/chat/chat
Content-Type: application/json
Authorization: Bearer {your_token}

{
  "question": "Spring Boot有什么特点?",
  "useRag": true,
  "topK": 5
}
```

## 五、添加菜单权限 (5分钟)

### 1. 登录系统管理员

访问: http://localhost:8080

### 2. 添加菜单

进入 **系统管理 > 菜单管理**

#### 添加一级菜单: AI应用

```
菜单名称: AI应用
菜单类型: 目录
显示顺序: 10
路由地址: AI
```

#### 添加二级菜单: AI问答

```
父菜单: AI应用
菜单名称: AI问答
菜单类型: 菜单
路由地址: aichat
组件路径: AI/aichat/index
权限标识: system:aichat:chat
```

### 3. 分配权限

进入 **系统管理 > 角色管理**,为相应角色分配AI问答权限。

## 六、常见问题排查

### Q1: 调用AI接口超时

**检查步骤:**

```bash
# 1. 检查Ollama是否运行
curl http://localhost:11434/api/tags

# 2. 检查模型是否已下载
ollama list

# 3. 测试模型是否可用
curl http://localhost:11434/api/generate -d '{
  "model": "deepseek-r1:8b",
  "prompt": "你好"
}'
```

**解决方案:**
- 确保Ollama服务已启动
- 检查application.yml中的base-url配置
- 增加超时时间配置

### Q2: 向量生成失败

**检查步骤:**

```bash
# 检查向量模型
ollama list | grep nomic-embed-text

# 测试向量生成
curl http://localhost:11434/api/embeddings -d '{
  "model": "nomic-embed-text",
  "prompt": "测试"
}'
```

**解决方案:**
- 确保nomic-embed-text模型已下载
- 检查数据库pgvector扩展是否启用

### Q3: 前端页面404

**解决方案:**
- 确认前端路由已配置
- 清除浏览器缓存
- 检查用户权限

### Q4: 数据库连接失败

**检查步骤:**

```bash
# 测试数据库连接
psql -U postgres -d 3DAGPL -c "SELECT 1"

# 检查pgvector
psql -U postgres -d 3DAGPL -c "SELECT * FROM pg_extension WHERE extname = 'vector'"
```

## 七、性能优化建议

### 1. Ollama性能优化

```bash
# 设置并发数
export OLLAMA_NUM_PARALLEL=4

# 设置GPU加速 (如果有GPU)
export OLLAMA_GPU_SUPPORT=true
```

### 2. 数据库优化

```sql
-- 创建合适的索引
CREATE INDEX idx_embedding_document ON ai_embedding(document_id);
CREATE INDEX idx_chat_history_user ON ai_chat_history(user_id);

-- 调整IVFFlat索引参数
ALTER INDEX idx_embedding_vector SET (lists = 200);
```

### 3. 应用配置优化

在application.yml中添加:

```yaml
spring:
  ai:
    ollama:
      chat:
        options:
          temperature: 0.7  # 创造性 (0-1)
          top_k: 40
          top_p: 0.9
```

## 八、下一步

1. **添加更多文档**: 通过API或管理界面添加知识库文档
2. **批量生成向量**: 使用批量接口为所有文档生成向量
3. **测试RAG效果**: 提问并查看检索到的知识片段
4. **监控性能**: 观察响应时间和资源使用情况
5. **自定义Prompt**: 根据业务需求调整Prompt模板

## 九、技术支持

如遇到问题,请检查:

1. ✅ Ollama服务是否运行
2. ✅ 模型是否已下载
3. ✅ 数据库连接是否正常
4. ✅ pgvector扩展是否启用
5. ✅ 配置文件是否正确
6. ✅ 权限是否配置

---

**祝使用愉快! 🚀**
