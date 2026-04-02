package com.threedagpl.system.service;

import java.util.List;
import com.threedagpl.system.domain.BlogArticle;

/**
 * 文章Service接口
 *
 * @author ruoyi
 * @date 2026-04-01
 */
public interface IBlogArticleService
{
    /**
     * 查询文章
     *
     * @param id 文章主键
     * @return 文章
     */
    public BlogArticle selectBlogArticleById(Long id);

    /**
     * 查询文章列表
     *
     * @param blogArticle 文章
     * @return 文章集合
     */
    public List<BlogArticle> selectBlogArticleList(BlogArticle blogArticle);

    /**
     * 新增文章
     *
     * @param blogArticle 文章
     * @return 结果
     */
    public int insertBlogArticle(BlogArticle blogArticle);

    /**
     * 修改文章
     *
     * @param blogArticle 文章
     * @return 结果
     */
    public int updateBlogArticle(BlogArticle blogArticle);

    /**
     * 批量删除文章
     *
     * @param ids 需要删除的文章主键集合
     * @return 结果
     */
    public int deleteBlogArticleByIds(Long[] ids);

    /**
     * 删除文章信息
     *
     * @param id 文章主键
     * @return 结果
     */
    public int deleteBlogArticleById(Long id);

    /**
     * 上架文章（将审核状态更改为已发布）
     *
     * @param ids 需要上架的文章主键集合
     * @return 结果
     */
    public int publishBlogArticleByIds(Long[] ids);

    /**
     * 下架文章（将审核状态更改为被拒绝）
     *
     * @param ids 需要下架的文章主键集合
     * @return 结果
     */
    public int rejectBlogArticleByIds(Long[] ids);
}
