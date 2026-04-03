package com.threedagpl.system.mapper;

import com.threedagpl.system.domain.AiEmbedding;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * AI向量嵌入Mapper接口
 * 
 * @author threedagpl
 */
@Mapper
public interface AiEmbeddingMapper {
    
    /**
     * 查询AI向量嵌入
     * 
     * @param id AI向量嵌入ID
     * @return AI向量嵌入
     */
    AiEmbedding selectAiEmbeddingById(Long id);

    /**
     * 查询AI向量嵌入列表
     * 
     * @param aiEmbedding AI向量嵌入
     * @return AI向量嵌入集合
     */
    List<AiEmbedding> selectAiEmbeddingList(AiEmbedding aiEmbedding);

    /**
     * 新增AI向量嵌入
     * 
     * @param aiEmbedding AI向量嵌入
     * @return 结果
     */
    int insertAiEmbedding(AiEmbedding aiEmbedding);

    /**
     * 批量新增AI向量嵌入
     * 
     * @param embeddings AI向量嵌入列表
     * @return 结果
     */
    int batchInsertAiEmbedding(@Param("embeddings") List<AiEmbedding> embeddings);

    /**
     * 修改AI向量嵌入
     * 
     * @param aiEmbedding AI向量嵌入
     * @return 结果
     */
    int updateAiEmbedding(AiEmbedding aiEmbedding);

    /**
     * 删除AI向量嵌入
     * 
     * @param id AI向量嵌入ID
     * @return 结果
     */
    int deleteAiEmbeddingById(Long id);

    /**
     * 批量删除AI向量嵌入
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    int deleteAiEmbeddingByIds(Long[] ids);

    /**
     * 根据文档ID删除向量
     * 
     * @param documentId 文档ID
     * @return 结果
     */
    int deleteAiEmbeddingByDocumentId(Long documentId);

    /**
     * 向量相似度搜索
     * 
     * @param embedding 查询向量
     * @param topK 返回数量
     * @return 向量嵌入列表
     */
    List<AiEmbedding> searchSimilarEmbeddings(@Param("embedding") float[] embedding, 
                                               @Param("topK") int topK);
}
