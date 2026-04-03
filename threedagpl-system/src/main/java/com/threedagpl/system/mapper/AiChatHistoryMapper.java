package com.threedagpl.system.mapper;

import com.threedagpl.system.domain.AiChatHistory;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * AI对话历史Mapper接口
 * 
 * @author threedagpl
 */
@Mapper
public interface AiChatHistoryMapper {
    
    /**
     * 查询AI对话历史
     * 
     * @param id AI对话历史ID
     * @return AI对话历史
     */
    AiChatHistory selectAiChatHistoryById(Long id);

    /**
     * 查询AI对话历史列表
     * 
     * @param aiChatHistory AI对话历史
     * @return AI对话历史集合
     */
    List<AiChatHistory> selectAiChatHistoryList(AiChatHistory aiChatHistory);

    /**
     * 查询用户的对话历史
     * 
     * @param userId 用户ID
     * @return AI对话历史集合
     */
    List<AiChatHistory> selectAiChatHistoryByUserId(Long userId);

    /**
     * 新增AI对话历史
     * 
     * @param aiChatHistory AI对话历史
     * @return 结果
     */
    int insertAiChatHistory(AiChatHistory aiChatHistory);

    /**
     * 删除AI对话历史
     * 
     * @param id AI对话历史ID
     * @return 结果
     */
    int deleteAiChatHistoryById(Long id);

    /**
     * 批量删除AI对话历史
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    int deleteAiChatHistoryByIds(Long[] ids);
}
