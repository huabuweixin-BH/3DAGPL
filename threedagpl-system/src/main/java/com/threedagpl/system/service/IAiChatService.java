package com.threedagpl.system.service;

import com.threedagpl.system.domain.AiChatHistory;
import com.threedagpl.system.domain.vo.AiChatRequestVO;
import com.threedagpl.system.domain.vo.AiChatResponseVO;

import java.util.List;

/**
 * AI问答Service接口
 * 
 * @author threedagpl
 */
public interface IAiChatService {
    
    /**
     * AI问答
     * 
     * @param request 问答请求
     * @return 问答响应
     */
    AiChatResponseVO chat(AiChatRequestVO request);

    /**
     * 查询对话历史
     * 
     * @param id 对话历史ID
     * @return 对话历史
     */
    AiChatHistory selectAiChatHistoryById(Long id);

    /**
     * 查询对话历史列表
     * 
     * @param aiChatHistory 对话历史
     * @return 对话历史集合
     */
    List<AiChatHistory> selectAiChatHistoryList(AiChatHistory aiChatHistory);

    /**
     * 查询用户的对话历史
     * 
     * @param userId 用户ID
     * @return 对话历史集合
     */
    List<AiChatHistory> selectAiChatHistoryByUserId(Long userId);

    /**
     * 删除对话历史
     * 
     * @param id 对话历史ID
     * @return 结果
     */
    int deleteAiChatHistoryById(Long id);

    /**
     * 批量删除对话历史
     * 
     * @param ids 需要删除的对话历史ID
     * @return 结果
     */
    int deleteAiChatHistoryByIds(Long[] ids);
}
