package com.threedagpl.system.service.impl;

import java.util.List;
import com.threedagpl.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.threedagpl.system.mapper.ModelTasksMapper;
import com.threedagpl.system.domain.ModelTasks;
import com.threedagpl.system.service.IModelTasksService;

/**
 * 三维模型处理任务Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-03-31
 */
@Service
public class ModelTasksServiceImpl implements IModelTasksService 
{
    @Autowired
    private ModelTasksMapper modelTasksMapper;

    /**
     * 查询三维模型处理任务
     * 
     * @param id 三维模型处理任务主键
     * @return 三维模型处理任务
     */
    @Override
    public ModelTasks selectModelTasksById(Long id)
    {
        return modelTasksMapper.selectModelTasksById(id);
    }

    /**
     * 查询三维模型处理任务列表
     * 
     * @param modelTasks 三维模型处理任务
     * @return 三维模型处理任务
     */
    @Override
    public List<ModelTasks> selectModelTasksList(ModelTasks modelTasks)
    {
        return modelTasksMapper.selectModelTasksList(modelTasks);
    }

    /**
     * 新增三维模型处理任务
     * 
     * @param modelTasks 三维模型处理任务
     * @return 结果
     */
    @Override
    public int insertModelTasks(ModelTasks modelTasks)
    {
        modelTasks.setCreateTime(DateUtils.getNowDate());
        return modelTasksMapper.insertModelTasks(modelTasks);
    }

    /**
     * 修改三维模型处理任务
     * 
     * @param modelTasks 三维模型处理任务
     * @return 结果
     */
    @Override
    public int updateModelTasks(ModelTasks modelTasks)
    {
        modelTasks.setUpdateTime(DateUtils.getNowDate());
        return modelTasksMapper.updateModelTasks(modelTasks);
    }

    /**
     * 批量删除三维模型处理任务
     * 
     * @param ids 需要删除的三维模型处理任务主键
     * @return 结果
     */
    @Override
    public int deleteModelTasksByIds(Long[] ids)
    {
        return modelTasksMapper.deleteModelTasksByIds(ids);
    }

    /**
     * 删除三维模型处理任务信息
     * 
     * @param id 三维模型处理任务主键
     * @return 结果
     */
    @Override
    public int deleteModelTasksById(Long id)
    {
        return modelTasksMapper.deleteModelTasksById(id);
    }
}
