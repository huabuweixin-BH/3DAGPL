package com.threedagpl.system.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.threedagpl.common.annotation.Excel;
import com.threedagpl.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;

/**
 * 文章评论对象 blog_comment
 *
 * @author ruoyi
 * @date 2026-04-02
 */
public class BlogComment extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 评论 ID */
    private Long id;

    /** 关联文章 ID */
    @Excel(name = "关联文章 ID")
    private Long articleId;

    /** 评论人 ID */
    @Excel(name = "评论人 ID")
    private Long userId;

    /** 评论内容 */
    @Excel(name = "评论内容")
    private String content;

    /** 父评论 ID */
    private Long parentId;

    /** 回复的目标用户 ID */
    private Long replyToUserId;

    /** 回复的目标用户名 */
    private String replyToUserName;

    /** 用户名（非数据库字段） */
    private String userName;

    /** 状态（0 正常 1 待审核 2 被删除） */
    private String status;

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getId()
    {
        return id;
    }

    public void setArticleId(Long articleId)
    {
        this.articleId = articleId;
    }

    public Long getArticleId()
    {
        return articleId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public String getContent()
    {
        return content;
    }

    public void setParentId(Long parentId)
    {
        this.parentId = parentId;
    }

    public Long getParentId()
    {
        return parentId;
    }

    public void setReplyToUserId(Long replyToUserId)
    {
        this.replyToUserId = replyToUserId;
    }

    public Long getReplyToUserId()
    {
        return replyToUserId;
    }

    public void setReplyToUserName(String replyToUserName)
    {
        this.replyToUserName = replyToUserName;
    }

    public String getReplyToUserName()
    {
        return replyToUserName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getStatus()
    {
        return status;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("articleId", getArticleId())
                .append("userId", getUserId())
                .append("content", getContent())
                .append("parentId", getParentId())
                .append("replyToUserId", getReplyToUserId())
                .append("replyToUserName", getReplyToUserName())
                .append("userName", getUserName())
                .append("status", getStatus())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .toString();
    }
}
