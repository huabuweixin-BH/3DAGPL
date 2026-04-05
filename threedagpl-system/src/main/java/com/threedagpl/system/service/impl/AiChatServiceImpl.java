package com.threedagpl.system.service.impl;

import com.pgvector.PGvector;
import com.threedagpl.common.utils.DateUtils;
import com.threedagpl.common.utils.SecurityUtils;
import com.threedagpl.system.domain.AiChatHistory;
import com.threedagpl.system.domain.AiEmbedding;
import com.threedagpl.system.domain.vo.AiChatRequestVO;
import com.threedagpl.system.domain.vo.AiChatResponseVO;
import com.threedagpl.system.mapper.AiChatHistoryMapper;
import com.threedagpl.system.service.IAiChatService;
import com.threedagpl.system.service.IAiEmbeddingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * AI问答Service业务层处理
 * 
 * @author threedagpl
 */
@Service
public class AiChatServiceImpl implements IAiChatService {
    
    private static final Logger log = LoggerFactory.getLogger(AiChatServiceImpl.class);

    @Autowired
    private ChatModel chatModel;

    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private IAiEmbeddingService aiEmbeddingService;

    @Autowired
    private AiChatHistoryMapper aiChatHistoryMapper;

    /**
     * TopK 默认值
     */
    @Value("${ai.rag.topk:5}")
    private int defaultTopK;

    /**
     * AI问答
     * 
     * @param request 问答请求
     * @return 问答响应
     */
    @Override
    public AiChatResponseVO chat(AiChatRequestVO request) {
        AiChatResponseVO response = new AiChatResponseVO();
        response.setQuestion(request.getQuestion());
        response.setUserId(SecurityUtils.getUserId());
        response.setCreateTime(DateUtils.getNowDate());

        try {
            String answer;
            String knowledgeContext = null;

            // 判断是否使用RAG
            if (Boolean.TRUE.equals(request.getUseRag())) {
                log.info("=== 开始RAG问答流程 ===");
                log.info("用户问题: {}", request.getQuestion());
                
                // RAG模式:检索相关知识
                int topK = request.getTopK() != null ? request.getTopK() : defaultTopK;
                log.info("向量检索TopK: {}", topK);

                // 1. 生成问题向量
                log.info("步骤1: 生成问题向量...");
                float[] questionEmbedding = generateEmbedding(request.getQuestion());
                log.info("问题向量生成完成,向量维度: {}", questionEmbedding.length);

                // 2. 向量相似度搜索
                log.info("步骤2: 执行向量相似度搜索...");
                List<AiEmbedding> similarEmbeddings = aiEmbeddingService.searchSimilarEmbeddings(
                    questionEmbedding, topK);
                log.info("向量检索完成,找到 {} 条相似记录", similarEmbeddings != null ? similarEmbeddings.size() : 0);

                // 3. 构建知识上下文
                if (similarEmbeddings != null && !similarEmbeddings.isEmpty()) {
                    knowledgeContext = similarEmbeddings.stream()
                        .map(AiEmbedding::getContent)
                        .collect(Collectors.joining("\n\n"));
                    log.info("知识上下文构建完成,总长度: {} 字符", knowledgeContext.length());
                    log.debug("知识上下文内容:\n{}", knowledgeContext);
                } else {
                    log.warn("未找到相关知识,将使用LLM直接回答");
                }

                // 4. 构建增强Prompt
                log.info("步骤3: 构建增强Prompt...");
                String enhancedPrompt = buildRagPrompt(request.getQuestion(), knowledgeContext);
                log.debug("增强Prompt内容:\n{}", enhancedPrompt);

                // 5. 调用LLM生成答案
                log.info("步骤4: 调用LLM生成答案...");
                answer = callLLM(enhancedPrompt);
                log.info("LLM调用完成,答案长度: {} 字符", answer.length());
                log.debug("LLM返回的答案:\n{}", answer);
                
                log.info("=== RAG问答流程完成 ===");
            } else {
                log.info("=== 直接LLM问答模式 ===");
                log.info("用户问题: {}", request.getQuestion());
                
                // 非RAG模式:直接调用LLM
                answer = callLLM(request.getQuestion());
                
                log.info("LLM调用完成,答案长度: {} 字符", answer.length());
                log.debug("LLM返回的答案:\n{}", answer);
                log.info("=== 直接LLM问答完成 ===");
            }

            response.setAnswer(answer);
            response.setKnowledgeContext(knowledgeContext);

            // 6. 保存对话历史
            saveChatHistory(request.getQuestion(), answer);

        } catch (Exception e) {
            log.error("AI问答失败", e);
            response.setAnswer("抱歉,AI服务出现异常,请稍后再试。错误信息:" + e.getMessage());
        }

        return response;
    }

    /**
     * 生成向量
     * 
     * @param text 文本
     * @return 向量数组
     */
    private float[] generateEmbedding(String text) {
        return embeddingModel.embed(text);
    }

    /**
     * 构建RAG Prompt
     * 
     * @param question 用户问题
     * @param knowledgeContext 知识上下文
     * @return 增强的Prompt
     */
    private String buildRagPrompt(String question, String knowledgeContext) {
        StringBuilder prompt = new StringBuilder();
        
        if (knowledgeContext != null && !knowledgeContext.isEmpty()) {
            // 有相关知识时,基于知识回答
            prompt.append("你是一个专业的AI助手,请根据以下参考知识回答用户的问题。\n\n");
            prompt.append("=== 参考知识 ===\n");
            prompt.append(knowledgeContext);
            prompt.append("\n\n=== 用户问题 ===\n");
            prompt.append(question);
            prompt.append("\n\n请基于上述参考知识,用简洁、准确的语言回答用户的问题。如果参考知识不足以完全回答问题,请结合你自己的知识进行补充。");
        } else {
            // 没有相关知识时,直接使用大模型回答
            prompt.append("你是一个专业的AI助手,请用简洁、准确的语言回答以下问题。\n\n");
            prompt.append("=== 用户问题 ===\n");
            prompt.append(question);
        }
        
        return prompt.toString();
    }

    /**
     * 调用LLM生成答案
     * 
     * @param prompt 提示词
     * @return 答案
     */
    private String callLLM(String prompt) {
        try {
            log.info("正在调用LLM,模型: {}", chatModel.getClass().getSimpleName());
            UserMessage userMessage = new UserMessage(prompt);
            ChatResponse chatResponse = chatModel.call(new Prompt(userMessage));

            if (chatResponse != null && chatResponse.getResult() != null) {
                String content = chatResponse.getResult().getOutput().getContent();
                log.info("LLM成功返回结果");
                return content;
            }
            log.warn("LLM返回结果为空");
            return "抱歉,无法生成答案。";
        } catch (Exception e) {
            log.error("调用LLM失败", e);
            throw new RuntimeException("调用LLM失败:" + e.getMessage());
        }
    }

    /**
     * 保存对话历史
     * 
     * @param question 问题
     * @param answer 答案
     */
    private void saveChatHistory(String question, String answer) {
        try {
            AiChatHistory chatHistory = new AiChatHistory();
            chatHistory.setUserId(SecurityUtils.getUserId());
            chatHistory.setQuestion(question);
            chatHistory.setAnswer(answer);
            aiChatHistoryMapper.insertAiChatHistory(chatHistory);
        } catch (Exception e) {
            log.error("保存对话历史失败", e);
        }
    }

    /**
     * 查询对话历史
     * 
     * @param id 对话历史ID
     * @return 对话历史
     */
    @Override
    public AiChatHistory selectAiChatHistoryById(Long id) {
        return aiChatHistoryMapper.selectAiChatHistoryById(id);
    }

    /**
     * 查询对话历史列表
     * 
     * @param aiChatHistory 对话历史
     * @return 对话历史
     */
    @Override
    public List<AiChatHistory> selectAiChatHistoryList(AiChatHistory aiChatHistory) {
        return aiChatHistoryMapper.selectAiChatHistoryList(aiChatHistory);
    }

    /**
     * 查询用户的对话历史
     * 
     * @param userId 用户ID
     * @return 对话历史
     */
    @Override
    public List<AiChatHistory> selectAiChatHistoryByUserId(Long userId) {
        return aiChatHistoryMapper.selectAiChatHistoryByUserId(userId);
    }

    /**
     * 删除对话历史
     * 
     * @param id 对话历史ID
     * @return 结果
     */
    @Override
    public int deleteAiChatHistoryById(Long id) {
        return aiChatHistoryMapper.deleteAiChatHistoryById(id);
    }

    /**
     * 批量删除对话历史
     * 
     * @param ids 需要删除的对话历史ID
     * @return 结果
     */
    @Override
    public int deleteAiChatHistoryByIds(Long[] ids) {
        return aiChatHistoryMapper.deleteAiChatHistoryByIds(ids);
    }
}
