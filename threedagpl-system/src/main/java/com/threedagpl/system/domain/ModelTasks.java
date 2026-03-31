package com.threedagpl.system.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.threedagpl.common.annotation.Excel;
import com.threedagpl.common.core.domain.BaseEntity;

/**
 * 三维模型处理任务对象 model_tasks
 * 
 * @author ruoyi
 * @date 2026-03-31
 */
public class ModelTasks extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID（自增） */
    private Long id;

    /** 任务编号（前端上传序号） */
    @Excel(name = "任务编号", readConverterExp = "前=端上传序号")
    private String taskNo;

    /** 用户ID */
    @Excel(name = "用户ID")
    private Long userId;

    /** 原始三维模型路径 */
    @Excel(name = "原始三维模型路径")
    private String inputModelPath;

    /** 处理后模型路径（Flask 返回） */
    @Excel(name = "处理后模型路径", readConverterExp = "Flask 返回")
    private String outputModelPath;

    /** 使用的算法（如QEM、LOD等） */
    @Excel(name = "使用的算法", readConverterExp = "如=QEM、LOD等")
    private String algorithm;

    /** 任务状态（0处理中 1成功 2失败） */
    @Excel(name = "任务状态", readConverterExp = "0=处理中,1=成功,2=失败")
    private Long status;

    /** 原始顶点数 */
    @Excel(name = "原始顶点数")
    private Long originalVertexCount;

    /** 目标顶点数 */
    @Excel(name = "目标顶点数")
    private Long targetVertexCount;

    /** 处理后顶点数 */
    @Excel(name = "处理后顶点数")
    private Long processedVertexCount;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }
    public void setTaskNo(String taskNo) 
    {
        this.taskNo = taskNo;
    }

    public String getTaskNo() 
    {
        return taskNo;
    }
    public void setUserId(Long userId) 
    {
        this.userId = userId;
    }

    public Long getUserId() 
    {
        return userId;
    }
    public void setInputModelPath(String inputModelPath) 
    {
        this.inputModelPath = inputModelPath;
    }

    public String getInputModelPath() 
    {
        return inputModelPath;
    }
    public void setOutputModelPath(String outputModelPath) 
    {
        this.outputModelPath = outputModelPath;
    }

    public String getOutputModelPath() 
    {
        return outputModelPath;
    }
    public void setAlgorithm(String algorithm) 
    {
        this.algorithm = algorithm;
    }

    public String getAlgorithm() 
    {
        return algorithm;
    }
    public void setStatus(Long status) 
    {
        this.status = status;
    }

    public Long getStatus() 
    {
        return status;
    }
    public void setOriginalVertexCount(Long originalVertexCount) 
    {
        this.originalVertexCount = originalVertexCount;
    }

    public Long getOriginalVertexCount() 
    {
        return originalVertexCount;
    }
    public void setTargetVertexCount(Long targetVertexCount) 
    {
        this.targetVertexCount = targetVertexCount;
    }

    public Long getTargetVertexCount() 
    {
        return targetVertexCount;
    }
    public void setProcessedVertexCount(Long processedVertexCount) 
    {
        this.processedVertexCount = processedVertexCount;
    }

    public Long getProcessedVertexCount() 
    {
        return processedVertexCount;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("taskNo", getTaskNo())
            .append("userId", getUserId())
            .append("inputModelPath", getInputModelPath())
            .append("outputModelPath", getOutputModelPath())
            .append("algorithm", getAlgorithm())
            .append("status", getStatus())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .append("originalVertexCount", getOriginalVertexCount())
            .append("targetVertexCount", getTargetVertexCount())
            .append("processedVertexCount", getProcessedVertexCount())
            .toString();
    }
}
