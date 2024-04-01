package com.pitayafruit.rest.vo;

import java.io.Serializable;
import lombok.Data;

/**
 * 办理人Props.
 */
@Data
public class AssigneeProps implements Serializable {
    /**
     * 办理人类型.
     */
    private AssigneeType assigneeType;

    /**
     * 候选办理人类型.
     */
    private AssigneeType candidateType;
}