package com.pitayafruit.rest.vo;

import java.io.Serializable;
import lombok.Data;

/**
 * FormConfig 表单配置.
 */
@Data
public class FormConfig implements Serializable {

    /**
     * 分组名称.
     */
    private String group;

    /**
     * 分组权重.
     */
    private Double weight;

    /**
     * 分类：评分SCORE、评语COMMENT.
     */
    private FormCategory category;

}

