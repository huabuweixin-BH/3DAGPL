package com.threedagpl.system.domain.vo;

/**
 * AI问答请求VO
 * 
 * @author threedagpl
 */
public class AiChatRequestVO {
    
    /** 问题内容 */
    private String question;
    
    /** 是否使用RAG (默认true) */
    private Boolean useRag = true;
    
    /** TopK 检索数量 */
    private Integer topK = 5;

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public Boolean getUseRag() {
        return useRag;
    }

    public void setUseRag(Boolean useRag) {
        this.useRag = useRag;
    }

    public Integer getTopK() {
        return topK;
    }

    public void setTopK(Integer topK) {
        this.topK = topK;
    }
}
