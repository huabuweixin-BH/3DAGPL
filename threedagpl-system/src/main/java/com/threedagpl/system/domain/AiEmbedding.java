package com.threedagpl.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.pgvector.PGvector;

/**
 * AI向量嵌入对象 ai_embedding
 * 
 * @author threedagpl
 */
@TableName("ai_embedding")
public class AiEmbedding {
    private static final long serialVersionUID = 1L;

    /** 嵌入ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 文档ID */
    private Long documentId;

    /** 嵌入内容 */
    private String content;

    /** 向量数据 */
    @TableField("embedding")
    private PGvector embedding;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public PGvector getEmbedding() {
        return embedding;
    }

    public void setEmbedding(PGvector embedding) {
        this.embedding = embedding;
    }

    @Override
    public String toString() {
        return "AiEmbedding{" +
                "id=" + id +
                ", documentId=" + documentId +
                ", content='" + content + '\'' +
                ", embedding=" + embedding +
                "}";
    }
}
