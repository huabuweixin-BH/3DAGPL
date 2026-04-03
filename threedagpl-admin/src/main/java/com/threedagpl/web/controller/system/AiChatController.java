package com.threedagpl.web.controller.system;

import com.threedagpl.common.annotation.Log;
import com.threedagpl.common.core.controller.BaseController;
import com.threedagpl.common.core.domain.AjaxResult;
import com.threedagpl.common.core.page.TableDataInfo;
import com.threedagpl.common.enums.BusinessType;
import com.threedagpl.system.domain.AiChatHistory;
import com.threedagpl.system.domain.vo.AiChatRequestVO;
import com.threedagpl.system.domain.vo.AiChatResponseVO;
import com.threedagpl.system.service.IAiChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI问答Controller
 * 
 * @author threedagpl
 */
@RestController
@RequestMapping("/system/ai/chat")
public class AiChatController extends BaseController {
    
    @Autowired
    private IAiChatService aiChatService;

    /**
     * AI问答
     */
    @PreAuthorize("@ss.hasPermi('system:aichat:chat')")
    @Log(title = "AI问答", businessType = BusinessType.OTHER)
    @PostMapping("/chat")
    public AjaxResult chat(@RequestBody AiChatRequestVO request) {
        AiChatResponseVO response = aiChatService.chat(request);
        return success(response);
    }

    /**
     * 查询对话历史列表
     */
    @PreAuthorize("@ss.hasPermi('system:aichat:list')")
    @GetMapping("/history/list")
    public TableDataInfo list(AiChatHistory aiChatHistory) {
        startPage();
        List<AiChatHistory> list = aiChatService.selectAiChatHistoryList(aiChatHistory);
        return getDataTable(list);
    }

    /**
     * 查询用户的对话历史
     */
    @PreAuthorize("@ss.hasPermi('system:aichat:list')")
    @GetMapping("/history/user/{userId}")
    public AjaxResult getUserHistory(@PathVariable("userId") Long userId) {
        List<AiChatHistory> list = aiChatService.selectAiChatHistoryByUserId(userId);
        return success(list);
    }

    /**
     * 获取对话历史详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:aichat:query')")
    @GetMapping(value = "/history/{id}")
    public AjaxResult getHistoryInfo(@PathVariable("id") Long id) {
        return success(aiChatService.selectAiChatHistoryById(id));
    }

    /**
     * 删除对话历史
     */
    @PreAuthorize("@ss.hasPermi('system:aichat:remove')")
    @Log(title = "对话历史", businessType = BusinessType.DELETE)
    @DeleteMapping("/history/{ids}")
    public AjaxResult removeHistory(@PathVariable Long[] ids) {
        return toAjax(aiChatService.deleteAiChatHistoryByIds(ids));
    }
}
