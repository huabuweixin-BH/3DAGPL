package com.threedagpl.system.service.impl;

import java.util.List;
import com.threedagpl.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.threedagpl.system.mapper.BlogArticleMapper;
import com.threedagpl.system.domain.BlogArticle;
import com.threedagpl.system.service.IBlogArticleService;

/**
 * 文章Service业务层处理
 *
 * @author ruoyi
 * @date 2026-04-01
 */
@Service
public class BlogArticleServiceImpl implements IBlogArticleService
{
    @Autowired
    private BlogArticleMapper blogArticleMapper;

    /**
     * 查询文章
     *
     * @param id 文章主键
     * @return 文章
     */
    @Override
    public BlogArticle selectBlogArticleById(Long id)
    {
        return blogArticleMapper.selectBlogArticleById(id);
    }

    /**
     * 查询文章列表
     *
     * @param blogArticle 文章
     * @return 文章
     */
    @Override
    public List<BlogArticle> selectBlogArticleList(BlogArticle blogArticle)
    {
        return blogArticleMapper.selectBlogArticleList(blogArticle);
    }

    /**
     * 新增文章
     *
     * @param blogArticle 文章
     * @return 结果
     */
    @Override
    public int insertBlogArticle(BlogArticle blogArticle)
    {
        blogArticle.setCreateTime(DateUtils.getNowDate());
        return blogArticleMapper.insertBlogArticle(blogArticle);
    }

    /**
     * 修改文章
     *
     * @param blogArticle 文章
     * @return 结果
     */
    @Override
    public int updateBlogArticle(BlogArticle blogArticle)
    {
        blogArticle.setUpdateTime(DateUtils.getNowDate());
        return blogArticleMapper.updateBlogArticle(blogArticle);
    }

    /**
     * 批量删除文章
     *
     * @param ids 需要删除的文章主键
     * @return 结果
     */
    @Override
    public int deleteBlogArticleByIds(Long[] ids)
    {
        return blogArticleMapper.deleteBlogArticleByIds(ids);
    }

    /**
     * 删除文章信息
     *
     * @param id 文章主键
     * @return 结果
     */
    @Override
    public int deleteBlogArticleById(Long id)
    {
        return blogArticleMapper.deleteBlogArticleById(id);
    }
}
