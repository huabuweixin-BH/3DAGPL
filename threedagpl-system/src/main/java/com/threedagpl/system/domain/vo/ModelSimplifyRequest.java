package com.threedagpl.system.domain.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 三维模型简化请求参数
 * 
 * @author ruoyi
 * @date 2026-03-31
 */
public class ModelSimplifyRequest
{
    /** 输入文件路径 (必需) */
    private String input;
    
    /** 目标顶点数 (可选) */
    private Integer v;
    
    /** 简化率 (可选，默认 0.5) */
    private Double p;
    
    /** 启用价感知简化 (可选，默认 false) */
    @JsonProperty("optim")
    private Boolean optim;
    
    /** 启用各向同性简化 (可选，默认 false) */
    private Boolean isotropic;

    public String getInput()
    {
        return input;
    }

    public void setInput(String input)
    {
        this.input = input;
    }

    public Integer getV()
    {
        return v;
    }

    public void setV(Integer v)
    {
        this.v = v;
    }

    public Double getP()
    {
        return p;
    }

    public void setP(Double p)
    {
        this.p = p;
    }

    public Boolean getOptim()
    {
        return optim != null ? optim : false;
    }

    public void setOptim(Boolean optim)
    {
        this.optim = optim;
    }

    public Boolean getIsotropic()
    {
        return isotropic != null ? isotropic : false;
    }

    public void setIsotropic(Boolean isotropic)
    {
        this.isotropic = isotropic;
    }
}
