package com.pitayafruit.rest;


import com.pitayafruit.base.BaseResponse;
import com.pitayafruit.rest.vo.HistoryPerformanceVo;
import lombok.RequiredArgsConstructor;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.variable.api.history.HistoricVariableInstance;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 流程Controller.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/process/v1")
public class ProcessControllerV1 {

    private final RepositoryService repositoryService;

    private final RuntimeService runtimeService;

    private final TaskService taskService;

    private final HistoryService historyService;

    /**
     * 部署流程模型.
     *
     * @return 流程key
     */
    @PostMapping("/deploy")
    public ResponseEntity<Object> createProcessDef() {
        //1.创建部署对象
        Deployment deployment = repositoryService.createDeployment()
                //2.添加流程定义文件
                .addClasspathResource("process/performance.bpmn20.xml")
                //3.设置流程名称
                .name("2024年6月绩效")
                //4.部署
                .deploy();
        //5.通过流程部署id查询流程定义id
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .deploymentId(deployment.getId()).singleResult();
        //6.返回流程定义id
        return new ResponseEntity<>(new BaseResponse<>(processDefinition.getId()), HttpStatus.OK);
    }

    /**
     * 启动流程.
     *
     * @return 流程key
     */
    @PostMapping("/start/{processId}")
    public ResponseEntity<Object> startProcessDef(@PathVariable String processId) {
        //1.启动流程
        ProcessInstance processInstance = runtimeService.startProcessInstanceById(processId);
        //2.维护该流程的分数变量
        runtimeService.setVariable(processInstance.getId(), "score", 0);
        //3.维护该流程的所属用户
        runtimeService.setVariable(processInstance.getId(),"ownerId", 88);
        //4.返回流程实例对象id
        return new ResponseEntity<>(new BaseResponse<>(processInstance.getId()), HttpStatus.OK);
    }

    /**
     * 完成节点任务.
     *
     * @return 流程key
     */
    @PostMapping("/complete/procDefId/{procDefId}/score/{score}/processInstanceId/{processInstanceId}")
    public ResponseEntity<Object> completeTask(@PathVariable String procDefId,
                                               @PathVariable String score,
                                               @PathVariable String processInstanceId) {
        //1.查询指定用户的待办任务
        String agentTaskId = findUserAgentTask(procDefId);
        //2.更新绩效流程变量
        runtimeService.setVariable(processInstanceId, "score", score);
        //3.完成指定任务的审批
        taskService.complete(agentTaskId);
        //4.返回响应
        return new ResponseEntity<>(new BaseResponse<>(null), HttpStatus.OK);
    }

    /**
     * 查询指定用户的历史绩效.
     *
     * @param userId 用户id
     * @return 历史流程集合
     */
    @PostMapping("/history-process-def/userId/{userId}")
    public ResponseEntity<Object> getHistoryProcessDef(@PathVariable Long userId) {
        //1.根据用户id查询历史流程
        List<HistoricVariableInstance> historicVariableInstanceList = historyService
                .createHistoricVariableInstanceQuery()
                .variableValueEquals("ownerId", userId.intValue())
                .list();
        //2.抽出流程插入ID
        Set<String> processInstanceIds = historicVariableInstanceList.stream()
                .map(HistoricVariableInstance::getProcessInstanceId)
                .collect(Collectors.toSet());
        //3.根据流程插入ID查询历史流程
        List<HistoricProcessInstance> historicProcessInstanceList = historyService
                .createHistoricProcessInstanceQuery()
                .processInstanceIds(processInstanceIds)
                .list();
        //4.转为map，键为流程定义id,值为流程插入id
        Map<String, String> processInstanceIdMap = historicProcessInstanceList
                .stream()
                .collect(Collectors.toMap(HistoricProcessInstance::getProcessDefinitionId,
                        HistoricProcessInstance::getId));
        //5.抽出流程定义ID集合
        Set<String> processDefinitionIds = historicProcessInstanceList.stream()
                .map(HistoricProcessInstance::getProcessDefinitionId)
                .collect(Collectors.toSet());
        //6.根据流程定义ID获取流程定义
        List<ProcessDefinition> processDefinitionList = repositoryService
                .createProcessDefinitionQuery()
                .processDefinitionIds(processDefinitionIds)
                .list();
        //7.抽出流程部署id
        List<String> deploymentIds = processDefinitionList.stream()
                .map(ProcessDefinition::getDeploymentId)
                .collect(Collectors.toList());
        //8.转为map,键为流程部署id,值为流程定义id
        Map<String, String> deploymentIdMap = processDefinitionList
                .stream()
                .collect(Collectors.toMap(ProcessDefinition::getDeploymentId,
                        ProcessDefinition::getId));
        //9.查询流程部署信息
        List<Deployment> deploymentList = repositoryService
                .createDeploymentQuery()
                .deploymentIds(deploymentIds)
                .list();
        //10.声明结果集
        List<HistoryPerformanceVo> historyPerformanceVos = new ArrayList<>();
        //11.遍历流程部署集合，为返回对象赋值
        deploymentList.forEach(deployment -> {
            //11-1.声明历史流程对象
            HistoryPerformanceVo historyPerformanceVo = new HistoryPerformanceVo();
            //11-2.设置绩效名称
            historyPerformanceVo.setPerformanceName(deployment.getName());
            //11-3.设置绩效分数
            String processDefinitionId = deploymentIdMap.get(deployment.getId());
            String processInstanceId = processInstanceIdMap.get(processDefinitionId);
            List<HistoricVariableInstance> historicVariableInstances = historyService
                    .createHistoricVariableInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .list();
            List<HistoricVariableInstance> score = historicVariableInstances.stream()
                    .filter(historicVariableInstance -> historicVariableInstance
                            .getVariableName().equals("score"))
                    .toList();
            historyPerformanceVo.setScore((String) score.get(0).getValue());
            //11-4.添加进集合
            historyPerformanceVos.add(historyPerformanceVo);
        });
        //12.返回流程绩效对象
        return new ResponseEntity<>(new BaseResponse<>(historyPerformanceVos), HttpStatus.OK);
    }

    /**
     * 查询流程的待办任务.
     *
     * @param procDefId 流程定义ID
     * @return 待办任务id
     */
    private String findUserAgentTask(String procDefId) {
        //1.查询流程的待办任务
        Task agentTask = taskService.createTaskQuery()
                .processDefinitionId(procDefId)
                .singleResult();
        //2.返回待办任务的id
        return agentTask.getId();
    }

}
