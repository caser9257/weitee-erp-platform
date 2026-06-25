package cn.weitee.erp.module.bpm.api.task;

import cn.weitee.erp.module.bpm.api.task.dto.BpmProcessInstanceCreateReqDTO;
import cn.weitee.erp.module.bpm.service.task.BpmProcessInstanceService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

/**
 * Flowable 流程实例 Api 实现类
 *
 * @author WeTai
 * @author jason
 */
@Service
@Validated
public class BpmProcessInstanceApiImpl implements BpmProcessInstanceApi {

    @Resource
    private BpmProcessInstanceService processInstanceService;

    @Override
    public String createProcessInstance(Long userId, @Valid BpmProcessInstanceCreateReqDTO reqDTO) {
        return processInstanceService.createProcessInstance(userId, reqDTO);
    }

}
