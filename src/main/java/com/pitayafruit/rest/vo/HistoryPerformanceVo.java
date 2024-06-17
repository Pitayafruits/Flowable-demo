package com.pitayafruit.rest.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 历史绩效VO.
 */
@Data
public class HistoryPerformanceVo implements Serializable {

    /**
     * 绩效名称.
     */
    private String performanceName;

    /**
     * 绩效分数.
     */
    private String score;
}
