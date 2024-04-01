package com.pitayafruit.rest.vo;

import lombok.Data;
import java.io.Serializable;

/**
 * 流程定义.
 */
@Data
public class ProcessDef implements Serializable {

    /**
     * 流程定义id.
     */
    private String processDefKey;

    /**
     * 流程定义名称.
     */
    private String processName;

    /**
     * 流程定义描述.
     */
    private String description;

    /**
     * 创建人id.
     */
    private Long creatorId;

    /**
     * 流程定义节点.
     */
    private ProcessNode processNode;

}
