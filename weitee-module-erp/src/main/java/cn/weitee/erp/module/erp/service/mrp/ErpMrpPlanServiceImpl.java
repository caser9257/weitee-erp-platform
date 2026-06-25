package cn.weitee.erp.module.erp.service.mrp;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.framework.security.core.util.SecurityFrameworkUtils;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.plan.ErpMrpPlanPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.plan.ErpMrpPlanSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpMrpPlanDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpMrpResultComponentDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpMrpResultDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpMrpShortageDO;
import cn.weitee.erp.module.erp.dal.dataobject.sale.ErpSaleOrderDO;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpMrpPlanMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpMrpResultMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpMrpShortageMapper;
import cn.weitee.erp.module.erp.dal.redis.no.ErpNoRedisDAO;
import cn.weitee.erp.module.erp.enums.mrp.ErpMrpPlanStatusEnum;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.MRP_PLAN_DATE_INVALID;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.MRP_PLAN_NOT_EXISTS;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.MRP_PLAN_STATUS_INVALID;

@Service
@Validated
public class ErpMrpPlanServiceImpl implements ErpMrpPlanService {

    @Resource
    private ErpMrpPlanMapper erpMrpPlanMapper;
    @Resource
    private ErpMrpResultMapper erpMrpResultMapper;
    @Resource
    private ErpMrpShortageMapper erpMrpShortageMapper;
    @Resource
    private ErpMrpResultComponentService resultComponentService;
    @Resource
    private ErpNoRedisDAO noRedisDAO;
    @Resource
    private ErpMrpPlanAsyncService mrpPlanAsyncService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPlan(ErpMrpPlanSaveReqVO createReqVO) {
        if (createReqVO.getPlanEndDate().isBefore(createReqVO.getPlanStartDate())) {
            throw exception(MRP_PLAN_DATE_INVALID);
        }
        ErpMrpPlanDO plan = BeanUtils.toBean(createReqVO, ErpMrpPlanDO.class, item -> item
                .setPlanNo(noRedisDAO.generate(ErpNoRedisDAO.MRP_PLAN_NO_PREFIX))
                .setStatus(ErpMrpPlanStatusEnum.DRAFT.getStatus())
                .setOperatorId(SecurityFrameworkUtils.getLoginUserId()));
        erpMrpPlanMapper.insert(plan);
        return plan.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createAutoPlanForSaleOrder(ErpSaleOrderDO saleOrder) {
        LocalDate demandDate = saleOrder.getDeliveryDate();
        if (demandDate == null && saleOrder.getOrderTime() != null) {
            demandDate = saleOrder.getOrderTime().toLocalDate();
        }
        if (demandDate == null) {
            throw new IllegalStateException("Sale order " + saleOrder.getId()
                    + " must have deliveryDate or orderTime before auto plan creation");
        }
        ErpMrpPlanDO plan = new ErpMrpPlanDO()
                .setPlanNo(noRedisDAO.generate(ErpNoRedisDAO.MRP_PLAN_NO_PREFIX))
                .setPlanName("AUTO-SO-" + saleOrder.getNo())
                .setPlanStartDate(demandDate)
                .setPlanEndDate(demandDate)
                .setStatus(ErpMrpPlanStatusEnum.DRAFT.getStatus())
                .setOperatorId(SecurityFrameworkUtils.getLoginUserId());
        erpMrpPlanMapper.insert(plan);
        return plan.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void runPlan(Long id) {
        ErpMrpPlanDO plan = validatePlanExists(id);
        Integer status = plan.getStatus();
        boolean canRun = ErpMrpPlanStatusEnum.DRAFT.getStatus().equals(status)
                || ErpMrpPlanStatusEnum.FAILED.getStatus().equals(status);
        if (!canRun) {
            throw exception(MRP_PLAN_STATUS_INVALID);
        }
        markPlanRunning(id, SecurityFrameworkUtils.getLoginUserId());
        mrpPlanAsyncService.submitRunPlan(id);
    }

    @Override
    public ErpMrpPlanDO getPlan(Long id) {
        return erpMrpPlanMapper.selectById(id);
    }

    @Override
    public PageResult<ErpMrpPlanDO> getPlanPage(ErpMrpPlanPageReqVO pageReqVO) {
        return erpMrpPlanMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ErpMrpResultDO> getResultList(Long planId) {
        validatePlanExists(planId);
        return erpMrpResultMapper.selectListByPlanId(planId);
    }

    @Override
    public List<ErpMrpResultComponentDO> getResultComponentList(Long resultId) {
        return resultComponentService.getListByResultId(resultId);
    }

    @Override
    public List<ErpMrpShortageDO> getShortageList(Long planId) {
        validatePlanExists(planId);
        return erpMrpShortageMapper.selectListByPlanId(planId);
    }

    private ErpMrpPlanDO validatePlanExists(Long id) {
        ErpMrpPlanDO plan = erpMrpPlanMapper.selectById(id);
        if (plan == null) {
            throw exception(MRP_PLAN_NOT_EXISTS);
        }
        return plan;
    }

    private void markPlanRunning(Long id, Long operatorId) {
        erpMrpPlanMapper.updateById(new ErpMrpPlanDO().setId(id)
                .setStatus(ErpMrpPlanStatusEnum.RUNNING.getStatus())
                .setRunTime(LocalDateTime.now())
                .setOperatorId(operatorId));
    }

}
