package com.threedagpl.system.service;

import java.util.List;
import com.threedagpl.system.domain.ModelTasks;

/**
 * 三维模型处理任务Service接口
 * 
 * @author ruoyi
 * @date 2026-03-31
 */
public interface IModelTasksService 
{
    /**
     * 查询三维模型处理任务
     * 
     * @param id 三维模型处理任务主键
     * @return 三维模型处理任务
     */
    public ModelTasks selectModelTasksById(Long id);

    /**
     * 查询三维模型处理任务列表
     * 
     * @param modelTasks 三维模型处理任务
     * @return 三维模型处理任务集合
     */
    public List<ModelTasks> selectModelTasksList(ModelTasks modelTasks);

    /**
     * 新增三维模型处理任务
     * 
     * @param modelTasks 三维模型处理任务
     * @return 结果
     */
    public int insertModelTasks(ModelTasks modelTasks);

    /**
     * 修改三维模型处理任务
     * 
     * @param modelTasks 三维模型处理任务
     * @return 结果
     */
    public int updateModelTasks(ModelTasks modelTasks);

    /**
     * 批量删除三维模型处理任务
     * 
     * @param ids 需要删除的三维模型处理任务主键集合
     * @return 结果
     */
    public int deleteModelTasksByIds(Long[] ids);

    /**
     * 删除三维模型处理任务信息
     * 
     * @param id 三维模型处理任务主键
     * @return 结果
     */
    public int deleteModelTasksById(Long id);
}
