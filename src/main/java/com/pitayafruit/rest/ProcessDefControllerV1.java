package com.pitayafruit.rest;

import com.pitayafruit.base.BaseResponse;
import com.pitayafruit.rest.vo.ProcessDef;
import com.pitayafruit.service.ProcessDefService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 流程Controller.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/process-def/v1")
public class ProcessDefControllerV1 {

    private final ProcessDefService processDefService;

    /**
     * 创建流程模型并部署.
     *
     * @param processDef 流程定义
     * @return 流程key
     */
    @PostMapping
    public ResponseEntity<Object> createProcessDef(@RequestBody ProcessDef processDef) {
        //1,创建并部署流程模型
        String processDefKey = processDefService.createProcessDefApi(processDef);
        //2.返回流程模型key
        return new ResponseEntity<>(new BaseResponse<>(processDefKey), HttpStatus.OK);
    }


}
