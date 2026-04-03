-- 修复ai_embedding表的向量维度
-- nomic-embed-text模型生成的是768维向量,不是1536维

-- 方式1: 删除旧表重建(如果表为空)
DROP TABLE IF EXISTS ai_embedding;

CREATE TABLE ai_embedding (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT,
    content TEXT,
    embedding VECTOR(768)  -- 修改为768维
);

-- 创建向量索引
CREATE INDEX idx_embedding_vector
ON ai_embedding
USING ivfflat (embedding vector_cosine_ops)
WITH (lists = 100);

COMMENT ON COLUMN ai_embedding.id IS '嵌入ID';
COMMENT ON COLUMN ai_embedding.document_id IS '文档ID';
COMMENT ON COLUMN ai_embedding.content IS '嵌入内容';
COMMENT ON COLUMN ai_embedding.embedding IS '向量数据(768维,nomic-embed-text模型)';
