package cn.weitee.erp.module.erp.service.mrp;

import cn.hutool.core.collection.CollUtil;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionOrderStepDO;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProductionOrderStepMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProductionStepQualityMapper;
import cn.weitee.erp.module.erp.enums.mrp.ErpProductionOrderStepStatusEnum;
import cn.weitee.erp.module.erp.framework.event.ErpProductionOrderStepFinishedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.List;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCTION_ORDER_STEP_NOT_EXISTS;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCTION_ORDER_STEP_PRECEDENT_UNFINISHED;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCTION_ORDER_STEP_STATUS_INVALID;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCTION_STEP_QUALITY_PENDING_UNFINISHED;

@Service
@Validated
public class ErpProductionOrderStepServiceImpl implements ErpProductionOrderStepService {

    @Resource
    private ErpProductionOrderStepMapper productionOrderStepMapper;
    @Resource
    private ErpProductionStepQualityMapper productionStepQualityMapper;
    @Resource
    private ApplicationEventPublisher eventPublisher;

    @Override
    public List<ErpProductionOrderStepDO> getStepList(Long productionOrderId) {
        return productionOrderStepMapper.selectListByOrderId(productionOrderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void startStep(Long stepId) {
        ErpProductionOrderStepDO step = validateStepExists(stepId);
        if (!ErpProductionOrderStepStatusEnum.WAIT.getStatus().equals(step.getStepStatus())) {
            throw exception(PRODUCTION_ORDER_STEP_STATUS_INVALID);
        }
        // 前序依赖：同工单 stepNo 更小的工序必须全部完工
        List<ErpProductionOrderStepDO> orderSteps =
                productionOrderStepMapper.selectListByOrderId(step.getProductionOrderId());
        if (CollUtil.isNotEmpty(orderSteps)) {
            boolean precedentUnfinished = orderSteps.stream()
                    .filter(item -> item.getStepNo() < step.getStepNo())
                    .anyMatch(item -> !ErpProductionOrderStepStatusEnum.FINISHED.getStatus()
                            .equals(item.getStepStatus()));
            if (precedentUnfinished) {
                throw exception(PRODUCTION_ORDER_STEP_PRECEDENT_UNFINISHED);
            }
        }
        updateStepStatusByCas(stepId, ErpProductionOrderStepStatusEnum.WAIT,
                ErpProductionOrderStepStatusEnum.PROCESSING);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pauseStep(Long stepId) {
        updateStepStatusByCas(stepId, ErpProductionOrderStepStatusEnum.PROCESSING,
                ErpProductionOrderStepStatusEnum.PAUSED);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resumeStep(Long stepId) {
        updateStepStatusByCas(stepId, ErpProductionOrderStepStatusEnum.PAUSED,
                ErpProductionOrderStepStatusEnum.PROCESSING);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void finishStep(Long stepId) {
        ErpProductionOrderStepDO step = validateStepExists(stepId);
        // 工序要求质检时，存在待检质检单则禁止完工（质量门禁）
        if (Boolean.TRUE.equals(step.getQcFlag())
                && productionStepQualityMapper.selectPendingCount(stepId) > 0) {
            throw exception(PRODUCTION_STEP_QUALITY_PENDING_UNFINISHED);
        }
        updateStepStatusByCas(stepId, ErpProductionOrderStepStatusEnum.PROCESSING,
                ErpProductionOrderStepStatusEnum.FINISHED);
        // 发布工序完工事件：供 MES 模块回写任务实际结束时间与状态
        eventPublisher.publishEvent(new ErpProductionOrderStepFinishedEvent(stepId));
    }

    /**
     * CAS 状态流转：仅当当前状态匹配期望值时更新，避免并发状态漂移。
     */
    private void updateStepStatusByCas(Long stepId, ErpProductionOrderStepStatusEnum expect,
                                       ErpProductionOrderStepStatusEnum target) {
        validateStepExists(stepId);
        int count = productionOrderStepMapper.updateStepStatusByCas(stepId, expect.getStatus(), target.getStatus());
        if (count == 0) {
            throw exception(PRODUCTION_ORDER_STEP_STATUS_INVALID);
        }
    }

    private ErpProductionOrderStepDO validateStepExists(Long stepId) {
        ErpProductionOrderStepDO step = productionOrderStepMapper.selectById(stepId);
        if (step == null) {
            throw exception(PRODUCTION_ORDER_STEP_NOT_EXISTS);
        }
        return step;
    }

}
