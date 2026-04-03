package com.threedagpl.system.service.impl;

import com.threedagpl.system.domain.AiDocument;
import com.threedagpl.system.mapper.AiDocumentMapper;
import com.threedagpl.system.service.IAiDocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AI文档Service业务层处理
 * 
 * @author threedagpl
 */
@Service
public class AiDocumentServiceImpl implements IAiDocumentService {
    
    @Autowired
    private AiDocumentMapper aiDocumentMapper;

    /**
     * 查询AI文档
     * 
     * @param id AI文档ID
     * @return AI文档
     */
    @Override
    public AiDocument selectAiDocumentById(Long id) {
        return aiDocumentMapper.selectAiDocumentById(id);
    }

    /**
     * 查询AI文档列表
     * 
     * @param aiDocument AI文档
     * @return AI文档
     */
    @Override
    public List<AiDocument> selectAiDocumentList(AiDocument aiDocument) {
        return aiDocumentMapper.selectAiDocumentList(aiDocument);
    }

    /**
     * 新增AI文档
     * 
     * @param aiDocument AI文档
     * @return 结果
     */
    @Override
    public int insertAiDocument(AiDocument aiDocument) {
        return aiDocumentMapper.insertAiDocument(aiDocument);
    }

    /**
     * 修改AI文档
     * 
     * @param aiDocument AI文档
     * @return 结果
     */
    @Override
    public int updateAiDocument(AiDocument aiDocument) {
        return aiDocumentMapper.updateAiDocument(aiDocument);
    }

    /**
     * 批量删除AI文档
     * 
     * @param ids 需要删除的AI文档ID
     * @return 结果
     */
    @Override
    public int deleteAiDocumentByIds(Long[] ids) {
        return aiDocumentMapper.deleteAiDocumentByIds(ids);
    }

    /**
     * 删除AI文档信息
     * 
     * @param id AI文档ID
     * @return 结果
     */
    @Override
    public int deleteAiDocumentById(Long id) {
        return aiDocumentMapper.deleteAiDocumentById(id);
    }
}
