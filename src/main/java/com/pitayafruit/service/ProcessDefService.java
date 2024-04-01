package com.pitayafruit.service;

import com.pitayafruit.rest.vo.ProcessDef;

/**
 * 流程定义Service.
 */
public interface ProcessDefService {

    /**
     * 创建流程模型并部署.
     *
     * @param processDef 流程定义
     * @return 流程key
     */
    String createProcessDefApi(ProcessDef processDef);
}
