package com.threedagpl.system.service.impl;

import java.util.List;

import com.pgvector.PGvector;
import com.threedagpl.system.domain.AiDocument;
import com.threedagpl.system.domain.AiEmbedding;
import com.threedagpl.system.service.IAiDocumentService;
import com.threedagpl.system.service.IAiEmbeddingService;
import com.threedagpl.common.utils.DateUtils;
import com.threedagpl.common.utils.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingModel;
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
    private static final Logger log = LoggerFactory.getLogger(BlogArticleServiceImpl.class);

    @Autowired
    private BlogArticleMapper blogArticleMapper;

    @Autowired
    private IAiDocumentService aiDocumentService;

    @Autowired
    private IAiEmbeddingService aiEmbeddingService;

    @Autowired
    private EmbeddingModel embeddingModel;

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
        blogArticle.setCreateBy(SecurityUtils.getUsername());
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
        blogArticle.setUpdateBy(SecurityUtils.getUsername());
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
        // 1. 删除知识库中对应的文档和向量
        for (Long articleId : ids) {
            try {
                removeArticleFromKnowledgeBase(articleId);
            } catch (Exception e) {
                log.error("从知识库中删除文章失败, 文章ID: {}", articleId, e);
            }
        }
        
        // 2. 删除文章
        return blogArticleMapper.deleteBlogArticleByIds(ids);
    }

    /**
     * 从AI知识库中移除文章
     * 
     * @param articleId 文章ID
     */
    private void removeArticleFromKnowledgeBase(Long articleId) {
        try {
            // 根据文章标题查找对应的AI文档
            BlogArticle article = blogArticleMapper.selectBlogArticleById(articleId);
            if (article == null) {
                return;
            }

            // 查找匹配的AI文档
            AiDocument queryDoc = new AiDocument();
            queryDoc.setTitle(article.getTitle());
            List<AiDocument> documents = aiDocumentService.selectAiDocumentList(queryDoc);
            
            // 删除匹配的文档及其向量
            for (AiDocument doc : documents) {
                // 删除向量
                aiEmbeddingService.deleteAiEmbeddingByDocumentId(doc.getId());
                // 删除文档
                aiDocumentService.deleteAiDocumentById(doc.getId());
            }
            
            log.info("成功从知识库中移除文章, 文章ID: {}, 标题: {}", articleId, article.getTitle());
        } catch (Exception e) {
            log.error("从知识库中移除文章失败, 文章ID: {}", articleId, e);
        }
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

    /**
     * 上架文章（将审核状态更改为已发布）
     *
     * @param ids 需要上架的文章主键集合
     * @return 结果
     */
    @Override
    public int publishBlogArticleByIds(Long[] ids)
    {
        // 1. 更新文章状态为已发布
        int result = blogArticleMapper.updateStatusByIds(ids, "1", SecurityUtils.getUsername());
        
        // 2. 将文章内容同步到AI知识库
        if (result > 0) {
            for (Long articleId : ids) {
                try {
                    syncArticleToKnowledgeBase(articleId);
                } catch (Exception e) {
                    log.error("将文章同步到知识库失败, 文章ID: {}", articleId, e);
                }
            }
        }
        
        return result;
    }

    /**
     * 将文章内容同步到AI知识库
     *
     * @param articleId 文章ID
     */
    private void syncArticleToKnowledgeBase(Long articleId) {
        log.info("开始同步文章到知识库, 文章ID: {}", articleId);
        
        // 查询文章详情
        BlogArticle article = blogArticleMapper.selectBlogArticleById(articleId);
        if (article == null) {
            log.warn("文章不存在, 无法同步到知识库: {}", articleId);
            return;
        }

        log.info("查询到文章: ID={}, 标题={}, contentText长度={}", 
                articleId, article.getTitle(), 
                article.getContentText() != null ? article.getContentText().length() : 0);

        // 检查文章是否有纯文本内容
        if (article.getContentText() == null || article.getContentText().trim().isEmpty()) {
            log.warn("文章纯文本内容为空, 无法同步到知识库: {}", articleId);
            return;
        }

        try {
            // 1. 创建AI文档
            AiDocument document = new AiDocument();
            document.setTitle(article.getTitle());
            document.setContent(article.getContentText());
            
            log.info("准备插入AI文档, 标题: {}, 内容长度: {}", 
                    document.getTitle(), document.getContent() != null ? document.getContent().length() : 0);

            // 2. 插入文档并获取ID
            int insertResult = aiDocumentService.insertAiDocument(document);
            Long documentId = document.getId();
            
            log.info("AI文档插入结果: {}, 生成的文档ID: {}", insertResult, documentId);
            
            if (documentId == null) {
                log.error("插入AI文档失败, 未获取到文档ID: {}", articleId);
                return;
            }

            // 3. 生成向量嵌入
            String content = article.getContentText();
            log.info("开始生成向量嵌入, 内容长度: {}", content.length());
            
            float[] floatArray = embeddingModel.embed(content);
            
            log.info("向量生成成功, 维度: {}", floatArray.length);

            // 4. 保存向量
            AiEmbedding aiEmbedding = new AiEmbedding();
            aiEmbedding.setDocumentId(documentId);
            aiEmbedding.setContent(content);
            aiEmbedding.setEmbedding(new PGvector(floatArray));
            
            int embedResult = aiEmbeddingService.insertAiEmbedding(aiEmbedding);
            log.info("向量插入结果: {}, 生成的向量ID: {}", embedResult, aiEmbedding.getId());

            log.info("成功将文章同步到知识库, 文章ID: {}, 文档ID: {}, 向量ID: {}", 
                    articleId, documentId, aiEmbedding.getId());
        } catch (Exception e) {
            log.error("将文章同步到知识库失败, 文章ID: {}", articleId, e);
            throw new RuntimeException("同步文章到知识库失败: " + e.getMessage(), e);
        }
    }

    /**
     * 下架文章（将审核状态更改为被拒绝）
     *
     * @param ids 需要下架的文章主键集合
     * @return 结果
     */
    @Override
    public int rejectBlogArticleByIds(Long[] ids)
    {
        return blogArticleMapper.updateStatusByIds(ids, "2", SecurityUtils.getUsername());
    }
}
