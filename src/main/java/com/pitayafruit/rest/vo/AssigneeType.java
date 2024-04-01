package com.pitayafruit.rest.vo;

/**
 * 办理人类型.
 */
public enum AssigneeType {

    /**
     * 节点处理人类型， 用户本人，直接上级，上级部门管理员，隔级上级，隔级部门管理员.
     */
    USER_ASSESSED, LEADER, DEPT_MANAGER, SUPERIOR_LEADER, SUPERIOR_DEPT_MANAGER
}
