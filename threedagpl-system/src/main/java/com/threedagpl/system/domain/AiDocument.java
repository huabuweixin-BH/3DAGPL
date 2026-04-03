package com.threedagpl.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.threedagpl.common.annotation.Excel;
import com.threedagpl.common.core.domain.BaseEntity;

/**
 * AI文档对象 ai_document
 * 
 * @author threedagpl
 */
@TableName("ai_document")
public class AiDocument extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 文档ID */
    @TableId(type = IdType.AUTO)
    @Excel(name = "文档ID")
    private Long id;

    /** 文档标题 */
    @Excel(name = "文档标题")
    private String title;

    /** 文档内容 */
    @Excel(name = "文档内容")
    private String content;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "AiDocument{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                "}";
    }
}
