package com.threedagpl.system.service.impl;

import com.threedagpl.common.utils.DateUtils;
import com.threedagpl.common.utils.SecurityUtils;
import com.threedagpl.system.domain.BlogComment;
import com.threedagpl.system.mapper.BlogCommentMapper;
import com.threedagpl.system.service.IBlogCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 文章评论 Service 业务层处理
 *
 * @author ruoyi
 * @date 2026-04-02
 */
@Service
public class BlogCommentServiceImpl implements IBlogCommentService
{
    @Autowired
    private BlogCommentMapper blogCommentMapper;

    /**
     * 查询文章评论
     *
     * @param id 文章评论主键
     * @return 文章评论
     */
    @Override
    public BlogComment selectBlogCommentById(Long id)
    {
        return blogCommentMapper.selectBlogCommentById(id);
    }

    /**
     * 查询文章评论列表
     *
     * @param blogComment 文章评论
     * @return 文章评论
     */
    @Override
    public List<BlogComment> selectBlogCommentList(BlogComment blogComment)
    {
        return blogCommentMapper.selectBlogCommentList(blogComment);
    }

    /**
     * 查询文章的评论列表（包含回复）
     *
     * @param articleId 文章 ID
     * @return 评论列表
     */
    @Override
    public List<BlogComment> selectCommentsByArticleId(Long articleId)
    {
        return blogCommentMapper.selectCommentsByArticleId(articleId);
    }

    /**
     * 新增文章评论
     *
     * @param blogComment 文章评论
     * @return 结果
     */
    @Override
    public int insertBlogComment(BlogComment blogComment)
    {
        blogComment.setUserId(SecurityUtils.getUserId());
        blogComment.setCreateBy(SecurityUtils.getUsername());
        blogComment.setCreateTime(DateUtils.getNowDate());
        return blogCommentMapper.insertBlogComment(blogComment);
    }

    /**
     * 修改文章评论
     *
     * @param blogComment 文章评论
     * @return 结果
     */
    @Override
    public int updateBlogComment(BlogComment blogComment)
    {
        blogComment.setUpdateBy(SecurityUtils.getUsername());
        blogComment.setUpdateTime(DateUtils.getNowDate());
        return blogCommentMapper.updateBlogComment(blogComment);
    }

    /**
     * 批量删除文章评论
     *
     * @param ids 需要删除的文章评论主键
     * @return 结果
     */
    @Override
    public int deleteBlogCommentByIds(Long[] ids)
    {
        return blogCommentMapper.deleteBlogCommentByIds(ids);
    }

    /**
     * 删除文章评论信息
     *
     * @param id 文章评论主键
     * @return 结果
     */
    @Override
    public int deleteBlogCommentById(Long id)
    {
        return blogCommentMapper.deleteBlogCommentById(id);
    }
}
