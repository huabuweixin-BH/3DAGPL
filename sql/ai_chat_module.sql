-- AI问答模块数据库初始化脚本

-- 1. AI文档表 (如果不存在)
CREATE TABLE IF NOT EXISTS ai_document (
    id BIGSERIAL PRIMARY KEY,
    title TEXT,
    content TEXT,
    create_by VARCHAR(64),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(64),
    update_time TIMESTAMP,
    remark VARCHAR(500)
);

COMMENT ON COLUMN ai_document.id IS '文档ID';
COMMENT ON COLUMN ai_document.title IS '文档标题';
COMMENT ON COLUMN ai_document.content IS '文档内容';
COMMENT ON COLUMN ai_document.create_by IS '创建者';
COMMENT ON COLUMN ai_document.create_time IS '创建时间';
COMMENT ON COLUMN ai_document.update_by IS '更新者';
COMMENT ON COLUMN ai_document.update_time IS '更新时间';
COMMENT ON COLUMN ai_document.remark IS '备注';

-- 2. AI向量嵌入表 (如果不存在)
CREATE TABLE IF NOT EXISTS ai_embedding (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT,
    content TEXT,
    embedding VECTOR(768)  -- 768维,匹配nomic-embed-text模型
);

COMMENT ON COLUMN ai_embedding.id IS '嵌入ID';
COMMENT ON COLUMN ai_embedding.document_id IS '文档ID';
COMMENT ON COLUMN ai_embedding.content IS '嵌入内容';
COMMENT ON COLUMN ai_embedding.embedding IS '向量数据(768维)';

-- 创建向量索引
CREATE INDEX IF NOT EXISTS idx_embedding_vector
ON ai_embedding
USING ivfflat (embedding vector_cosine_ops)
WITH (lists = 100);

-- 3. AI对话历史表 (如果不存在)
CREATE TABLE IF NOT EXISTS ai_chat_history (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    question TEXT,
    answer TEXT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON COLUMN ai_chat_history.id IS '对话ID';
COMMENT ON COLUMN ai_chat_history.user_id IS '用户ID';
COMMENT ON COLUMN ai_chat_history.question IS '问题';
COMMENT ON COLUMN ai_chat_history.answer IS '答案';
COMMENT ON COLUMN ai_chat_history.create_time IS '创建时间';

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_chat_history_user_id ON ai_chat_history USING btree (user_id);
CREATE INDEX IF NOT EXISTS idx_chat_history_create_time ON ai_chat_history USING btree (create_time DESC);
