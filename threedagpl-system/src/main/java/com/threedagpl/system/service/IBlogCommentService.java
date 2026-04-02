package com.threedagpl.system.service;

import com.threedagpl.system.domain.BlogComment;

import java.util.List;

/**
 * 文章评论 Service 接口
 *
 * @author ruoyi
 * @date 2026-04-02
 */
public interface IBlogCommentService
{
    /**
     * 查询文章评论
     *
     * @param id 文章评论主键
     * @return 文章评论
     */
    public BlogComment selectBlogCommentById(Long id);

    /**
     * 查询文章评论列表
     *
     * @param blogComment 文章评论
     * @return 文章评论集合
     */
    public List<BlogComment> selectBlogCommentList(BlogComment blogComment);

    /**
     * 查询文章的评论列表（包含回复）
     *
     * @param articleId 文章 ID
     * @return 评论列表
     */
    public List<BlogComment> selectCommentsByArticleId(Long articleId);

    /**
     * 新增文章评论
     *
     * @param blogComment 文章评论
     * @return 结果
     */
    public int insertBlogComment(BlogComment blogComment);

    /**
     * 修改文章评论
     *
     * @param blogComment 文章评论
     * @return 结果
     */
    public int updateBlogComment(BlogComment blogComment);

    /**
     * 批量删除文章评论
     *
     * @param ids 需要删除的文章评论主键集合
     * @return 结果
     */
    public int deleteBlogCommentByIds(Long[] ids);

    /**
     * 删除文章评论信息
     *
     * @param id 文章评论主键
     * @return 结果
     */
    public int deleteBlogCommentById(Long id);
}
