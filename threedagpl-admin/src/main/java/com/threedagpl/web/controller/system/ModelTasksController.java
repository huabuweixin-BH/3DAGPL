package com.threedagpl.web.controller.system;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import com.threedagpl.common.annotation.Log;
import com.threedagpl.common.core.controller.BaseController;
import com.threedagpl.common.utils.poi.ExcelUtil;
import com.threedagpl.common.core.domain.AjaxResult;
import com.threedagpl.common.core.page.TableDataInfo;
import com.threedagpl.common.enums.BusinessType;
import com.threedagpl.system.domain.ModelTasks;
import com.threedagpl.system.service.IModelTasksService;
import com.threedagpl.system.domain.vo.ModelSimplifyRequest;

/**
 * 三维模型处理任务Controller
 * 
 * @author ruoyi
 * @date 2026-03-31
 */
@RestController
@RequestMapping("/system/tasks")
public class ModelTasksController extends BaseController
{
    @Autowired
    private IModelTasksService modelTasksService;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 查询三维模型处理任务列表
     */
    @PreAuthorize("@ss.hasPermi('system:tasks:list')")
    @GetMapping("/list")
    public TableDataInfo list(ModelTasks modelTasks)
    {
        startPage();
        List<ModelTasks> list = modelTasksService.selectModelTasksList(modelTasks);
        return getDataTable(list);
    }

    /**
     * 导出三维模型处理任务列表
     */
    @PreAuthorize("@ss.hasPermi('system:tasks:export')")
    @Log(title = "三维模型处理任务", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ModelTasks modelTasks)
    {
        List<ModelTasks> list = modelTasksService.selectModelTasksList(modelTasks);
        ExcelUtil<ModelTasks> util = new ExcelUtil<ModelTasks>(ModelTasks.class);
        util.exportExcel(response, list, "三维模型处理任务数据");
    }

    /**
     * 获取三维模型处理任务详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:tasks:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(modelTasksService.selectModelTasksById(id));
    }

    /**
     * 新增三维模型处理任务
     */
    @PreAuthorize("@ss.hasPermi('system:tasks:add')")
    @Log(title = "三维模型处理任务", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ModelTasks modelTasks)
    {
        return toAjax(modelTasksService.insertModelTasks(modelTasks));
    }

    /**
     * 接收模型简化请求
     * 数据格式：{
     *   "input": "path/to/input_mesh.obj", // 必需：输入文件路径
     *   "v": 1000, // 可选：目标顶点数
     *   "p": 0.5, // 可选：简化率，默认 0.5
     *   "optim": true, // 可选：启用价感知简化，默认 false
     *   "isotropic": false // 可选：启用各向同性简化，默认 false
     * }
     */
    @PreAuthorize("@ss.hasPermi('system:tasks:add')")
    @Log(title = "三维模型处理任务", businessType = BusinessType.INSERT)
    @PostMapping("/model")
    public AjaxResult submitModel(@RequestBody ModelSimplifyRequest request)
    {
        System.out.println(request);
        // 验证必需参数
        if (request.getInput() == null || request.getInput().trim().isEmpty()) {
            return error("输入文件路径不能为空");
        }
        
        // 1. 先调用 Flask 服务进行模型处理
        Map<String, Object> flaskRequest = new HashMap<>();
        // 在文件路径前添加 /data/ 前缀
        flaskRequest.put("input", "/data/" + request.getInput());
        if (request.getV() != null) {
            flaskRequest.put("v", request.getV());
        }
        if (request.getP() != null) {
            flaskRequest.put("p", request.getP());
        }
        if (request.getOptim() != null) {
            flaskRequest.put("optim", request.getOptim());
        }
        if (request.getIsotropic() != null) {
            flaskRequest.put("isotropic", request.getIsotropic());
        }
        
        // 调用 Flask API
        ResponseEntity<Map> response = restTemplate.postForEntity(
            "http://localhost:5000/model",
            flaskRequest,
            Map.class
        );
        
        Map<String, Object> flaskResponse = response.getBody();
        
        // 检查 Flask 响应状态
        if (flaskResponse == null || !"success".equals(flaskResponse.get("status"))) {
            return error("Flask 服务调用失败：" + (flaskResponse != null ? flaskResponse.get("message") : "未知错误"));
        }
        
        // 获取输出文件路径
        String outputFilePath = (String) flaskResponse.get("output_file");
        Integer vertexCount = (Integer) flaskResponse.get("vertex_count");
        
        // 2. 创建任务对象
        ModelTasks modelTasks = new ModelTasks();
        modelTasks.setTaskNo(generateTaskNo()); // 生成唯一任务编号
        modelTasks.setUserId(getUserId()); // 设置当前登录用户 ID
        modelTasks.setInputModelPath(request.getInput());
        modelTasks.setOutputModelPath(outputFilePath); // 保存 Flask 返回的输出路径
        modelTasks.setAlgorithm("QEM"); // 默认使用 QEM 算法
        modelTasks.setStatus(1L); // 1-成功
        
        // 设置目标顶点数和处理后顶点数
        if (request.getV() != null) {
            modelTasks.setTargetVertexCount(request.getV().longValue());
        }
        if (vertexCount != null) {
            modelTasks.setProcessedVertexCount(vertexCount.longValue());
        }
        
        // 可以在这里根据 p、optim、isotropic 等参数进行更多处理
        // 例如保存到数据库的 remark 字段或其他自定义字段
        
        // 保存任务
        int result = modelTasksService.insertModelTasks(modelTasks);
        
        if (result > 0) {
            return success(modelTasks);
        }
        return error("任务创建失败");
    }

    /**
     * 生成唯一任务编号
     * @return 任务编号
     */
    private String generateTaskNo() {
        return "TASK_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }

    /**
     * 修改三维模型处理任务
     */
    @PreAuthorize("@ss.hasPermi('system:tasks:edit')")
    @Log(title = "三维模型处理任务", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ModelTasks modelTasks)
    {
        return toAjax(modelTasksService.updateModelTasks(modelTasks));
    }

    /**
     * 删除三维模型处理任务
     */
    @PreAuthorize("@ss.hasPermi('system:tasks:remove')")
    @Log(title = "三维模型处理任务", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(modelTasksService.deleteModelTasksByIds(ids));
    }
}
