package com.threedagpl.system.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.threedagpl.common.annotation.Excel;
import com.threedagpl.common.core.domain.BaseEntity;

/**
 * 文章对象 blog_article
 *
 * @author ruoyi
 * @date 2026-04-01
 */
public class BlogArticle extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 用户id */
    private Long id;

    /** 标题 */
    @Excel(name = "标题")
    private String title;

    /** 摘要 */
    @Excel(name = "摘要")
    private String summary;

    /** 封面图地址 */
    @Excel(name = "封面图地址")
    private String coverUrl;

    /** 富文本HTML */
    @Excel(name = "富文本HTML")
    private String contentHtml;

    /** 纯文本内容（用于RAG检索） */
    @Excel(name = "纯文本内容", readConverterExp = "用=于RAG检索")
    private String contentText;

    /** 审核状态（0待审核 1已发布 2被拒绝） */
    @Excel(name = "审核状态", readConverterExp = "0=待审核,1=已发布,2=被拒绝")
    private String status;

    /** 是否置顶 */
    @Excel(name = "是否置顶")
    private String isTop;

    /** 是否公开 */
    @Excel(name = "是否公开")
    private String isVisible;

    /** 文章特征向量 */
    @Excel(name = "文章特征向量")
    private String embedding;

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getId()
    {
        return id;
    }
    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getTitle()
    {
        return title;
    }
    public void setSummary(String summary)
    {
        this.summary = summary;
    }

    public String getSummary()
    {
        return summary;
    }
    public void setCoverUrl(String coverUrl)
    {
        this.coverUrl = coverUrl;
    }

    public String getCoverUrl()
    {
        return coverUrl;
    }
    public void setContentHtml(String contentHtml)
    {
        this.contentHtml = contentHtml;
    }

    public String getContentHtml()
    {
        return contentHtml;
    }
    public void setContentText(String contentText)
    {
        this.contentText = contentText;
    }

    public String getContentText()
    {
        return contentText;
    }
    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getStatus()
    {
        return status;
    }
    public void setIsTop(String isTop)
    {
        this.isTop = isTop;
    }

    public String getIsTop()
    {
        return isTop;
    }
    public void setIsVisible(String isVisible)
    {
        this.isVisible = isVisible;
    }

    public String getIsVisible()
    {
        return isVisible;
    }
    public void setEmbedding(String embedding)
    {
        this.embedding = embedding;
    }

    public String getEmbedding()
    {
        return embedding;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("title", getTitle())
                .append("summary", getSummary())
                .append("coverUrl", getCoverUrl())
                .append("contentHtml", getContentHtml())
                .append("contentText", getContentText())
                .append("status", getStatus())
                .append("isTop", getIsTop())
                .append("isVisible", getIsVisible())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .append("embedding", getEmbedding())
                .toString();
    }
}
