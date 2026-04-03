package com.threedagpl.system.service;

/**
 * AI向量嵌入工具Service接口
 * 用于批量生成文档的向量嵌入
 * 
 * @author threedagpl
 */
public interface IAiEmbeddingToolService {
    
    /**
     * 为指定文档生成向量嵌入
     * 
     * @param documentId 文档ID
     * @return 结果
     */
    int generateEmbeddingForDocument(Long documentId);

    /**
     * 批量为多个文档生成向量嵌入
     * 
     * @param documentIds 文档ID数组
     * @return 结果
     */
    int batchGenerateEmbeddings(Long[] documentIds);

    /**
     * 为所有没有向量的文档生成向量嵌入
     * 
     * @return 处理的文档数量
     */
    int generateEmbeddingsForAllDocuments();

    /**
     * 重新生成指定文档的向量嵌入
     * 
     * @param documentId 文档ID
     * @return 结果
     */
    int regenerateEmbeddingForDocument(Long documentId);
}
