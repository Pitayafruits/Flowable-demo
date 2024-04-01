package com.pitayafruit.rest;


import com.pitayafruit.base.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    /**
     * 部署流程模型.
     *
     * @return 流程key
     */
    @PostMapping("/deploy")
    public ResponseEntity<Object> createProcessDef() {
        //1,创建部署对象
        Deployment deployment = repositoryService.createDeployment()
                //2.添加流程定义文件
                .addClasspathResource("process/performance.bpmn20.xml")
                //3.设置流程名称
                .name("绩效流程")
                //4.部署
                .deploy();
        //5.返回部署的流程id
        return new ResponseEntity<>(new BaseResponse<>(deployment.getId()), HttpStatus.OK);
    }

    /**
     * 启动流程.
     *
     * @return 流程key
     */
    @PostMapping("/start")
    public ResponseEntity<Object> startProcessDef() {
        //1.声明流程id
        String processId = "performance-001:1:a9d432fd-ec57-11ee-980b-c85ea9014af0";
        //2.启动流程
        ProcessInstance processInstance = runtimeService.startProcessInstanceById(processId);
        //3.返回流程实例对象id
        return new ResponseEntity<>(new BaseResponse<>(processInstance.getId()), HttpStatus.OK);
    }

    /**
     * 完成节点任务.
     *
     * @return 流程key
     */
    @PostMapping("/complete/{agentUser}")
    public ResponseEntity<Object> completeTask(@PathVariable String agentUser) {
        //1.查询指定用户的待办任务
        //String agentTaskId = findUserAgentTask(agentUser);
        //2.完成指定任务的审批
        taskService.complete("d30f9ffd-eeb2-11ee-9654-c85ea9014af0");
        //3.返回响应
        return new ResponseEntity<>(new BaseResponse<>(null), HttpStatus.OK);
    }

    /**
     * 查询指定用户的待办任务.
     *
     * @param user 待办用户
     * @return 待办任务id
     */
    private String findUserAgentTask(String user) {
        //1.查找指定用户的一个待办任务
        Task agentTask = taskService.createTaskQuery()
                .taskAssignee(user)
                .singleResult();
        //2.返回待办任务的id
        return agentTask.getId();
    }

}
