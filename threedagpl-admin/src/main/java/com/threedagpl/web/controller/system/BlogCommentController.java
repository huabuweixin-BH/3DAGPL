package com.threedagpl.web.controller.system;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.threedagpl.common.annotation.Log;
import com.threedagpl.common.core.controller.BaseController;
import com.threedagpl.common.core.domain.AjaxResult;
import com.threedagpl.common.enums.BusinessType;
import com.threedagpl.system.domain.BlogComment;
import com.threedagpl.system.service.IBlogCommentService;
import com.threedagpl.common.core.page.TableDataInfo;
import com.threedagpl.common.utils.poi.ExcelUtil;

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
     * 查询文章评论列表
     */
    @PreAuthorize("@ss.hasPermi('system:comment:list')")
    @GetMapping("/list")
    public TableDataInfo list(BlogComment blogComment)
    {
        startPage();
        List<BlogComment> list = blogCommentService.selectBlogCommentList(blogComment);
        return getDataTable(list);
    }

    /**
     * 查询文章的评论列表（用于前端论坛页面）
     */
    @GetMapping("/article")
    public AjaxResult listByArticleId(Long articleId)
    {
        List<BlogComment> list = blogCommentService.selectCommentsByArticleId(articleId);
        return success(list);
    }

    /**
     * 导出文章评论列表
     */
    @PreAuthorize("@ss.hasPermi('system:comment:export')")
    @Log(title = "文章评论", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, BlogComment blogComment)
    {
        List<BlogComment> list = blogCommentService.selectBlogCommentList(blogComment);
        ExcelUtil<BlogComment> util = new ExcelUtil<BlogComment>(BlogComment.class);
        util.exportExcel(response, list, "文章评论数据");
    }

    /**
     * 获取文章评论详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:comment:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(blogCommentService.selectBlogCommentById(id));
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
     * 修改文章评论
     */
    @PreAuthorize("@ss.hasPermi('system:comment:edit')")
    @Log(title = "文章评论", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BlogComment blogComment)
    {
        return toAjax(blogCommentService.updateBlogComment(blogComment));
    }

    /**
     * 删除文章评论
     */
    @PreAuthorize("@ss.hasPermi('system:comment:remove')")
    @Log(title = "文章评论", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(blogCommentService.deleteBlogCommentByIds(ids));
    }
}
