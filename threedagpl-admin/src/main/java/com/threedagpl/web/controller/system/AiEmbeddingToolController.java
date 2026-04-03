package com.threedagpl.web.controller.system;

import com.threedagpl.common.annotation.Log;
import com.threedagpl.common.core.controller.BaseController;
import com.threedagpl.common.core.domain.AjaxResult;
import com.threedagpl.common.enums.BusinessType;
import com.threedagpl.system.service.IAiEmbeddingToolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * AI向量嵌入工具Controller
 * 
 * @author threedagpl
 */
@RestController
@RequestMapping("/system/ai/embedding")
public class AiEmbeddingToolController extends BaseController {
    
    @Autowired
    private IAiEmbeddingToolService aiEmbeddingToolService;

    /**
     * 为指定文档生成向量嵌入
     */
    @PreAuthorize("@ss.hasPermi('system:document:edit')")
    @Log(title = "生成向量嵌入", businessType = BusinessType.UPDATE)
    @PostMapping("/generate/{documentId}")
    public AjaxResult generateEmbedding(@PathVariable Long documentId) {
        int result = aiEmbeddingToolService.generateEmbeddingForDocument(documentId);
        if (result > 0) {
            return success("向量生成成功");
        }
        return error("向量生成失败");
    }

    /**
     * 批量生成向量嵌入
     */
    @PreAuthorize("@ss.hasPermi('system:document:edit')")
    @Log(title = "批量生成向量嵌入", businessType = BusinessType.UPDATE)
    @PostMapping("/batch-generate")
    public AjaxResult batchGenerateEmbeddings(@RequestBody Long[] documentIds) {
        int result = aiEmbeddingToolService.batchGenerateEmbeddings(documentIds);
        return success("成功处理 " + result + " 个文档");
    }

    /**
     * 为所有文档生成向量嵌入
     */
    @PreAuthorize("@ss.hasPermi('system:document:edit')")
    @Log(title = "批量生成所有向量嵌入", businessType = BusinessType.UPDATE)
    @PostMapping("/generate-all")
    public AjaxResult generateAllEmbeddings() {
        int result = aiEmbeddingToolService.generateEmbeddingsForAllDocuments();
        return success("成功处理 " + result + " 个文档");
    }

    /**
     * 重新生成指定文档的向量嵌入
     */
    @PreAuthorize("@ss.hasPermi('system:document:edit')")
    @Log(title = "重新生成向量嵌入", businessType = BusinessType.UPDATE)
    @PostMapping("/regenerate/{documentId}")
    public AjaxResult regenerateEmbedding(@PathVariable Long documentId) {
        int result = aiEmbeddingToolService.regenerateEmbeddingForDocument(documentId);
        if (result > 0) {
            return success("向量重新生成成功");
        }
        return error("向量重新生成失败");
    }
}
