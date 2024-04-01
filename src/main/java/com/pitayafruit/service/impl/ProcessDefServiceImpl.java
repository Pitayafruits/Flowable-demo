package com.pitayafruit.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.pitayafruit.rest.vo.AssigneeProps;
import com.pitayafruit.rest.vo.ProcessDef;
import com.pitayafruit.rest.vo.ProcessNode;
import com.pitayafruit.service.ProcessDefService;
import com.pitayafruit.utils.UUIDUtil;
import lombok.RequiredArgsConstructor;
import org.flowable.bpmn.model.*;
import org.flowable.bpmn.model.Process;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.validation.ValidationError;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 流程定义ServiceImpl.
 */
@Service
@RequiredArgsConstructor
public class ProcessDefServiceImpl implements ProcessDefService {

    private final RepositoryService repositoryService;

    /**
     * 创建流程模型并部署.
     *
     * @param processDef 流程定义
     * @return 流程key
     */
    @Override
    public String createProcessDefApi(ProcessDef processDef) {
        //1.设置流程定义Key
        processDef.setProcessDefKey("processDef" + UUIDUtil.getUUID());
        //2.设置流程定义创建人id
        processDef.setCreatorId(1L);
        //3.设置流程定义名称
        processDef.setProcessName("绩效流程");
        //4.创建流程模型
        BpmnModel bpmnModel = toBpmn(processDef);
        //5.部署流程模型
        repositoryService.createDeployment()
                //5-1.流程定义key
                .key(processDef.getProcessDefKey())
                //5-2.流程定义名称
                .name(processDef.getProcessName())
                //5-3.添加流程模型
                .addBpmnModel(processDef.getProcessDefKey() + ".bpmn", bpmnModel)
                //5-4.部署
                .deploy();
        //6.返回流程定义key
        return processDef.getProcessDefKey();
    }

    /**
     * 将流程定义转换为BpmnModel.
     *
     * @param processDef 流程定义
     * @return Bpmn模型
     */
    private BpmnModel toBpmn(ProcessDef processDef) {
        //1.创建流程
        //1-1.声明流程对象
        Process process = new Process();
        //1-2.设置流程id
        process.setId(processDef.getProcessDefKey());
        //1-3.设置流程名称
        process.setName(processDef.getProcessName());
        //2.创建开始事件
        //2-1.声明开始事件对象
        StartEvent startEvent = new StartEvent();
        //2-2.设置开始事件id
        startEvent.setId("startEvent" + UUIDUtil.getUUID());
        //2-3.设置开始事件名称
        startEvent.setName("开始");
        //2-4.将开始事件添加到流程中
        process.addFlowElement(startEvent);
        //创建用户节点
        ProcessNode processNode = processDef.getProcessNode();
        buildTask(startEvent, processNode, process);
        //创建流程模型
        BpmnModel bpmnModel = new BpmnModel();
        bpmnModel.addProcess(process);
        // 验证BPMN模型
        List<ValidationError> validationErrors = repositoryService.validateProcess(bpmnModel);
        if (ObjectUtil.isNotEmpty(validationErrors)) {
            //打印失败日志
            validationErrors.forEach(validationError -> System.out.println(validationError.toString()));
            throw new IllegalArgumentException("验证失败");
        }
        return bpmnModel;
    }

    /**
     * 创建用户任务节点.
     *
     * @param parentTask  父节点
     * @param processNode 流程节点
     * @param process     流程定义
     */
    private void buildTask(FlowNode parentTask, ProcessNode processNode, Process process) {
        //如果节点启用，处理当前节点
        if (ObjectUtil.isNotNull(processNode.getEnable()) && processNode.getEnable() == 1) {
            UserTask userTask = new UserTask();
            userTask.setId("userTask" + UUIDUtil.getUUID());
            userTask.setName(processNode.getName());
            //设置节点类型
            userTask.setCategory(processNode.getCategory().toString());
            List<CustomProperty> customProperties = new ArrayList<>();
            //办理人属性
            AssigneeProps assigneeProps = processNode.getAssigneeProps();
            //设置办理人类型
            customProperties.add(
                    buildCustomProperty("assigneeType", assigneeProps.getAssigneeType().toString()));
            //设置候选办理人类型
            if (ObjectUtil.isNotNull(assigneeProps.getCandidateType())) {
                customProperties.add(buildCustomProperty("candidateType", assigneeProps.getCandidateType().toString()));
            }
            //绑定表单
            userTask.setFormProperties(buildFormProperty(processNode));
            //表单列表添加到节点扩展属性
            customProperties.add(buildCustomProperty("formProps", JSON.toJSONString(processNode.getFormProps())));
            //设置自定义属性,包括办理人类型和表单分组和权重配置
            userTask.setCustomProperties(customProperties);
            //监听器列表
            List<FlowableListener> taskListeners = new ArrayList<>();
            //任务创建时添加监听器，用于动态指定任务的办理人和设置变量
            taskListeners.add(buildTaskListener(TaskListener.EVENTNAME_CREATE, "taskCreateListener"));
            //任务完成后添加监听器，用于计算分数和保存表单
            //如果是手动填写流程，添加手动填写的监听器
            taskListeners.add(buildTaskListener(TaskListener.EVENTNAME_COMPLETE, "taskCompleteListener"));
            //设置任务监听器
            userTask.setTaskListeners(taskListeners);
            // 将用户任务节点添加到流程定义
            process.addFlowElement(userTask);
            //添加流程连线
            process.addFlowElement(new SequenceFlow(parentTask.getId(), userTask.getId()));
            //解析下一个节点，参数为当前任务，当前流程节点，流程定义，流程类型
            buildChildren(userTask, processNode, process);
        } else {
            //当前节点关闭的情况下，直接解析下一个节点，参数为父任务，当前流程节点，流程定义，流程类型
            buildChildren(parentTask, processNode, process);
        }
    }

    /**
     * 解析子节点.
     *
     * @param parentTask  下一个节点的父任务
     * @param processNode 流程节点
     * @param process     流程定义
     */
    private void buildChildren(FlowNode parentTask, ProcessNode processNode, Process process) {
        ProcessNode childrenNode = processNode.getChildren();
        if (ObjectUtil.isNotNull(childrenNode)) {
            //创建子节点
            buildTask(parentTask, childrenNode, process);
        } else {
            //创建结束事件
            EndEvent endEvent = new EndEvent();
            endEvent.setId("endEvent" + UUIDUtil.getUUID());
            endEvent.setName("结束");
            process.addFlowElement(endEvent);
            //添加流程连线
            process.addFlowElement(new SequenceFlow(parentTask.getId(), endEvent.getId()));
        }
    }


    /**
     * 创建任务监听器,用于动态分配任务的办理人.
     *
     * @param event    事件
     * @param beanName 类名
     * @return 任务监听器
     */
    private FlowableListener buildTaskListener(String event, String beanName) {
        FlowableListener flowableListener = new FlowableListener();
        flowableListener.setEvent(event);
        flowableListener.setImplementationType("delegateExpression");
        flowableListener.setImplementation("${" + beanName + "}");
        return flowableListener;
    }

    /**
     * 创建表单属性.
     *
     * @param processNode 流程节点
     */
    private List<FormProperty> buildFormProperty(ProcessNode processNode) {
        List<FormProperty> formProperties = new ArrayList<>();
        if (ObjectUtil.isNull(processNode.getFormProps())) {
            return formProperties;
        }
        processNode.getFormProps().forEach(prop -> {
            //新建表单属性对象
            FormProperty formProperty = new FormProperty();
            //设置表单属性id
            String id = "formProperty" + UUIDUtil.getUUID();
            formProperty.setId(id);
            //设置表单名称
            formProperty.setName(prop.getName());
            //设置表单变量名为表单id
            formProperty.setVariable(id);
            //设置表单变量类型
            formProperty.setType(prop.getType());
            //设置表单是否必填
            formProperty.setRequired(true);
            formProperties.add(formProperty);
            //设置表单属性id
            prop.setId(id);
            prop.setVariable(id);
        });
        return formProperties;
    }


    /**
     * 创建自定义属性.
     *
     * @param key  键
     * @param value 值
     * @return 自定义属性
     */
    private CustomProperty buildCustomProperty(String key, String value) {
        CustomProperty customProperty = new CustomProperty();
        //自定义属性名称
        customProperty.setName(key);
        //自定义属性值
        customProperty.setSimpleValue(value);
        return customProperty;
    }
}
