package com.threedagpl.system.service.impl;

import com.threedagpl.system.domain.AiEmbedding;
import com.threedagpl.system.mapper.AiEmbeddingMapper;
import com.threedagpl.system.service.IAiEmbeddingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AI向量嵌入Service业务层处理
 * 
 * @author threedagpl
 */
@Service
public class AiEmbeddingServiceImpl implements IAiEmbeddingService {
    
    @Autowired
    private AiEmbeddingMapper aiEmbeddingMapper;

    /**
     * 查询AI向量嵌入
     * 
     * @param id AI向量嵌入ID
     * @return AI向量嵌入
     */
    @Override
    public AiEmbedding selectAiEmbeddingById(Long id) {
        return aiEmbeddingMapper.selectAiEmbeddingById(id);
    }

    /**
     * 查询AI向量嵌入列表
     * 
     * @param aiEmbedding AI向量嵌入
     * @return AI向量嵌入
     */
    @Override
    public List<AiEmbedding> selectAiEmbeddingList(AiEmbedding aiEmbedding) {
        return aiEmbeddingMapper.selectAiEmbeddingList(aiEmbedding);
    }

    /**
     * 新增AI向量嵌入
     * 
     * @param aiEmbedding AI向量嵌入
     * @return 结果
     */
    @Override
    public int insertAiEmbedding(AiEmbedding aiEmbedding) {
        return aiEmbeddingMapper.insertAiEmbedding(aiEmbedding);
    }

    /**
     * 批量新增AI向量嵌入
     * 
     * @param embeddings AI向量嵌入列表
     * @return 结果
     */
    @Override
    public int batchInsertAiEmbedding(List<AiEmbedding> embeddings) {
        return aiEmbeddingMapper.batchInsertAiEmbedding(embeddings);
    }

    /**
     * 修改AI向量嵌入
     * 
     * @param aiEmbedding AI向量嵌入
     * @return 结果
     */
    @Override
    public int updateAiEmbedding(AiEmbedding aiEmbedding) {
        return aiEmbeddingMapper.updateAiEmbedding(aiEmbedding);
    }

    /**
     * 批量删除AI向量嵌入
     * 
     * @param ids 需要删除的AI向量嵌入ID
     * @return 结果
     */
    @Override
    public int deleteAiEmbeddingByIds(Long[] ids) {
        return aiEmbeddingMapper.deleteAiEmbeddingByIds(ids);
    }

    /**
     * 删除AI向量嵌入信息
     * 
     * @param id AI向量嵌入ID
     * @return 结果
     */
    @Override
    public int deleteAiEmbeddingById(Long id) {
        return aiEmbeddingMapper.deleteAiEmbeddingById(id);
    }

    /**
     * 根据文档ID删除向量
     * 
     * @param documentId 文档ID
     * @return 结果
     */
    @Override
    public int deleteAiEmbeddingByDocumentId(Long documentId) {
        return aiEmbeddingMapper.deleteAiEmbeddingByDocumentId(documentId);
    }

    /**
     * 向量相似度搜索
     * 
     * @param embedding 查询向量
     * @param topK 返回数量
     * @return 向量嵌入列表
     */
    @Override
    public List<AiEmbedding> searchSimilarEmbeddings(float[] embedding, int topK) {
        return aiEmbeddingMapper.searchSimilarEmbeddings(embedding, topK);
    }
}
