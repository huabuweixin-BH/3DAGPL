package com.threedagpl.system.mapper;

import java.util.List;
import com.threedagpl.system.domain.BlogArticle;

/**
 * 文章Mapper接口
 *
 * @author ruoyi
 * @date 2026-04-01
 */
public interface BlogArticleMapper
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
     * 删除文章
     *
     * @param id 文章主键
     * @return 结果
     */
    public int deleteBlogArticleById(Long id);

    /**
     * 批量删除文章
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteBlogArticleByIds(Long[] ids);

    /**
     * 批量更新文章状态
     *
     * @param ids 需要更新的数据主键集合
     * @param status 新的状态值
     * @return 结果
     */
    public int updateStatusByIds(Long[] ids, String status);
}
