package com.threedagpl.web.controller.system;

import com.threedagpl.common.annotation.Log;
import com.threedagpl.common.core.controller.BaseController;
import com.threedagpl.common.core.domain.AjaxResult;
import com.threedagpl.common.core.page.TableDataInfo;
import com.threedagpl.common.enums.BusinessType;
import com.threedagpl.system.domain.AiDocument;
import com.threedagpl.system.service.IAiDocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI文档Controller
 * 
 * @author threedagpl
 */
@RestController
@RequestMapping("/system/ai/document")
public class AiDocumentController extends BaseController {
    
    @Autowired
    private IAiDocumentService aiDocumentService;

    /**
     * 查询AI文档列表
     */
    @PreAuthorize("@ss.hasPermi('system:document:list')")
    @GetMapping("/list")
    public TableDataInfo list(AiDocument aiDocument) {
        startPage();
        List<AiDocument> list = aiDocumentService.selectAiDocumentList(aiDocument);
        return getDataTable(list);
    }

    /**
     * 获取AI文档详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:document:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(aiDocumentService.selectAiDocumentById(id));
    }

    /**
     * 新增AI文档
     */
    @PreAuthorize("@ss.hasPermi('system:document:add')")
    @Log(title = "AI文档", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody AiDocument aiDocument) {
        return toAjax(aiDocumentService.insertAiDocument(aiDocument));
    }

    /**
     * 修改AI文档
     */
    @PreAuthorize("@ss.hasPermi('system:document:edit')")
    @Log(title = "AI文档", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody AiDocument aiDocument) {
        return toAjax(aiDocumentService.updateAiDocument(aiDocument));
    }

    /**
     * 删除AI文档
     */
    @PreAuthorize("@ss.hasPermi('system:document:remove')")
    @Log(title = "AI文档", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(aiDocumentService.deleteAiDocumentByIds(ids));
    }
}
