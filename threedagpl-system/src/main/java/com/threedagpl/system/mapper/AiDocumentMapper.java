package com.threedagpl.system.mapper;

import com.threedagpl.system.domain.AiDocument;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * AI文档Mapper接口
 * 
 * @author threedagpl
 */
@Mapper
public interface AiDocumentMapper {
    
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
     * 删除AI文档
     * 
     * @param id AI文档ID
     * @return 结果
     */
    int deleteAiDocumentById(Long id);

    /**
     * 批量删除AI文档
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    int deleteAiDocumentByIds(Long[] ids);
}
