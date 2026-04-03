package com.threedagpl.system.service.impl;

import com.pgvector.PGvector;
import com.threedagpl.system.domain.AiDocument;
import com.threedagpl.system.domain.AiEmbedding;
import com.threedagpl.system.mapper.AiDocumentMapper;
import com.threedagpl.system.mapper.AiEmbeddingMapper;
import com.threedagpl.system.service.IAiEmbeddingToolService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * AI向量嵌入工具Service业务层处理
 * 
 * @author threedagpl
 */
@Service
public class AiEmbeddingToolServiceImpl implements IAiEmbeddingToolService {
    
    private static final Logger log = LoggerFactory.getLogger(AiEmbeddingToolServiceImpl.class);

    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private AiDocumentMapper aiDocumentMapper;

    @Autowired
    private AiEmbeddingMapper aiEmbeddingMapper;

    /**
     * 为指定文档生成向量嵌入
     * 
     * @param documentId 文档ID
     * @return 结果
     */
    @Override
    @Transactional
    public int generateEmbeddingForDocument(Long documentId) {
        try {
            // 1. 查询文档
            AiDocument document = aiDocumentMapper.selectAiDocumentById(documentId);
            if (document == null) {
                log.warn("文档不存在: {}", documentId);
                return 0;
            }

            // 2. 检查是否已有向量
            List<AiEmbedding> existingEmbeddings = aiEmbeddingMapper.selectAiEmbeddingList(
                new AiEmbedding() {{ setDocumentId(documentId); }});
            
            if (existingEmbeddings != null && !existingEmbeddings.isEmpty()) {
                log.info("文档已有向量,先删除旧向量: {}", documentId);
                aiEmbeddingMapper.deleteAiEmbeddingByDocumentId(documentId);
            }

            // 3. 生成向量
            String content = document.getContent();
            if (content == null || content.isEmpty()) {
                log.warn("文档内容为空: {}", documentId);
                return 0;
            }

            // 4. 调用嵌入模型
            float[] floatArray = embeddingModel.embed(content);

            // 5. 保存向量
            AiEmbedding aiEmbedding = new AiEmbedding();
            aiEmbedding.setDocumentId(documentId);
            aiEmbedding.setContent(content);
            aiEmbedding.setEmbedding(new PGvector(floatArray));
            
            return aiEmbeddingMapper.insertAiEmbedding(aiEmbedding);
        } catch (Exception e) {
            log.error("为文档生成向量失败: {}", documentId, e);
            throw new RuntimeException("生成向量失败: " + e.getMessage());
        }
    }

    /**
     * 批量为多个文档生成向量嵌入
     * 
     * @param documentIds 文档ID数组
     * @return 结果
     */
    @Override
    @Transactional
    public int batchGenerateEmbeddings(Long[] documentIds) {
        int successCount = 0;
        for (Long documentId : documentIds) {
            try {
                successCount += generateEmbeddingForDocument(documentId);
            } catch (Exception e) {
                log.error("批量生成向量失败,文档ID: {}", documentId, e);
            }
        }
        return successCount;
    }

    /**
     * 为所有没有向量的文档生成向量嵌入
     * 
     * @return 处理的文档数量
     */
    @Override
    public int generateEmbeddingsForAllDocuments() {
        try {
            // 查询所有文档
            List<AiDocument> allDocuments = aiDocumentMapper.selectAiDocumentList(new AiDocument());
            
            int successCount = 0;
            for (AiDocument document : allDocuments) {
                try {
                    // 检查是否已有向量
                    List<AiEmbedding> existingEmbeddings = aiEmbeddingMapper.selectAiEmbeddingList(
                        new AiEmbedding() {{ setDocumentId(document.getId()); }});
                    
                    if (existingEmbeddings == null || existingEmbeddings.isEmpty()) {
                        successCount += generateEmbeddingForDocument(document.getId());
                    }
                } catch (Exception e) {
                    log.error("为文档生成向量失败: {}", document.getId(), e);
                }
            }
            
            log.info("批量生成向量完成,成功处理: {} 个文档", successCount);
            return successCount;
        } catch (Exception e) {
            log.error("批量生成向量失败", e);
            throw new RuntimeException("批量生成向量失败: " + e.getMessage());
        }
    }

    /**
     * 重新生成指定文档的向量嵌入
     * 
     * @param documentId 文档ID
     * @return 结果
     */
    @Override
    @Transactional
    public int regenerateEmbeddingForDocument(Long documentId) {
        try {
            // 先删除旧向量
            aiEmbeddingMapper.deleteAiEmbeddingByDocumentId(documentId);
            
            // 重新生成
            return generateEmbeddingForDocument(documentId);
        } catch (Exception e) {
            log.error("重新生成向量失败: {}", documentId, e);
            throw new RuntimeException("重新生成向量失败: " + e.getMessage());
        }
    }
}
