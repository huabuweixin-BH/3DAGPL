package com.threedagpl.system.service;

import com.threedagpl.system.domain.AiDocument;

import java.util.List;

/**
 * AI文档Service接口
 * 
 * @author threedagpl
 */
public interface IAiDocumentService {
    
    /**
     * 查询AI文档
     * 
     * @param id AI文档ID
     * @return AI文档
     */
    AiDocument selectAiDocumentById(Long id);

    /**
     * 查询AI文档列表
     * 
     * @param aiDocument AI文档
     * @return AI文档集合
     */
    List<AiDocument> selectAiDocumentList(AiDocument aiDocument);

    /**
     * 新增AI文档
     * 
     * @param aiDocument AI文档
     * @return 结果
     */
    int insertAiDocument(AiDocument aiDocument);

    /**
     * 修改AI文档
     * 
     * @param aiDocument AI文档
     * @return 结果
     */
    int updateAiDocument(AiDocument aiDocument);

    /**
     * 批量删除AI文档
     * 
     * @param ids 需要删除的AI文档ID
     * @return 结果
     */
    int deleteAiDocumentByIds(Long[] ids);

    /**
     * 删除AI文档信息
     * 
     * @param id AI文档ID
     * @return 结果
     */
    int deleteAiDocumentById(Long id);
}
