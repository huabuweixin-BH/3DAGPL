package com.threedagpl.web.controller.system;

import com.threedagpl.common.annotation.Log;
import com.threedagpl.common.core.controller.BaseController;
import com.threedagpl.common.core.domain.AjaxResult;
import com.threedagpl.common.enums.BusinessType;
import com.threedagpl.system.domain.BlogComment;
import com.threedagpl.system.service.IBlogCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 文章评论 Controller
 *
 * @author ruoyi
 * @date 2026-04-02
 */
@RestController
@RequestMapping("/system/comment")
public class BlogCommentController extends BaseController
{
    @Autowired
    private IBlogCommentService blogCommentService;

    /**
     * 查询文章的评论列表
     */
    @GetMapping("/list")
    public AjaxResult list(Long articleId)
    {
        List<BlogComment> list = blogCommentService.selectCommentsByArticleId(articleId);
        return success(list);
    }

    /**
     * 新增文章评论
     */
    @Log(title = "文章评论", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BlogComment blogComment)
    {
        return toAjax(blogCommentService.insertBlogComment(blogComment));
    }

    /**
     * 删除文章评论
     */
    @Log(title = "文章评论", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('system:comment:remove')")
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(blogCommentService.deleteBlogCommentByIds(ids));
    }
}
