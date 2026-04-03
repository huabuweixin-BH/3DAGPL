### AI问答需求

我现在要实现一个AI问答功能，目前框架为ruoyi框架（springboot3）和postgresql,已经安装pgvector；目前计划使用spring AI+ollama实现AI问答并且实现rag功能。

rag：文章表用于向量数据库一部分，支持每轮上传pdf文本作为向量数据库

AI问答：前端增加AI问答功能，问答页面为src\views\AI\aichat\index.vue。

####项目已完成完成：

文章表，设计之初考虑到用于RAG。

```sql
CREATE TABLE "public"."blog_article" (
  -- 基础字段
  "id" bigint NOT NULL,
  "title" varchar(200) NOT NULL,                    -- 文章标题
  "summary" varchar(500),                           -- 文章摘要（列表展示用）
  "cover_url" varchar(255),                        -- 封面图地址
  
  -- 内容相关 (双字段设计：一个存样式，一个喂给AI)
  "content_html" text NOT NULL,                    -- wangEditor生成的HTML内容
  "content_text" text,                             -- 过滤标签后的纯文本内容（RAG核心字段）
  
  -- 状态与权限
  "status" char(1) DEFAULT '0',                    -- 审核状态（0待审核 1通过 2拒绝）
  "is_top" char(1) DEFAULT '0',                    -- 是否置顶（0否 1是）
  "is_visible" char(1) DEFAULT '1',                -- 是否公开（0私有 1公开）
  
  -- 若依标准审计字段
  "create_by" varchar(64),                         -- 创建者
  "create_time" timestamp(6),                      -- 创建时间
  "update_by" varchar(64),                         -- 更新者
  "update_time" timestamp(6),                      -- 更新时间
  "remark" varchar(500),                           -- 备注（可存审核拒绝原因）

  -- RAG 扩展字段 (需开启 pgvector)
  "embedding" vector(1536),                        -- 向量字段（1536是OpenAI常见的维度，可根据模型调整）

  CONSTRAINT "blog_article_pkey" PRIMARY KEY ("id")
);

-- 添加注释
COMMENT ON COLUMN "public"."blog_article"."content_html" IS '富文本HTML';
COMMENT ON COLUMN "public"."blog_article"."content_text" IS '纯文本内容（用于RAG检索）';
COMMENT ON COLUMN "public"."blog_article"."status" IS '审核状态（0待审核 1已发布 2被拒绝）';
COMMENT ON COLUMN "public"."blog_article"."embedding" IS '文章特征向量';

-- 创建索引优化查询
CREATE INDEX "idx_blog_article_status" ON "public"."blog_article" USING btree ("status");
CREATE INDEX "idx_blog_article_create_time" ON "public"."blog_article" USING btree ("create_time" DESC);
```

ollama已下载大模型：deepseek-r1:8b

ollama已下载向量模型：nomic-embed-text

知识库表：

```sql
CREATE TABLE ai_document (
    id BIGSERIAL PRIMARY KEY,
    title TEXT,
    content TEXT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

向量表:

```sql
CREATE TABLE ai_embedding (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT,
    content TEXT,
    embedding VECTOR(1536)  -- 根据模型维度调整
);
CREATE INDEX idx_embedding
ON ai_embedding
USING ivfflat (embedding vector_cosine_ops);
```

对话记录表：

```sql
CREATE TABLE ai_chat_history (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    question TEXT,
    answer TEXT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 目标：

用户提问
   ↓
生成问题向量
   ↓
pgvector相似度搜索
   ↓
取TopK知识片段
   ↓
拼接Prompt
   ↓
LLM生成答案
   ↓
返回结果