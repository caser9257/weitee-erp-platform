package cn.iocoder.yudao.module.erp.service.mrp;

import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpMrpPlanDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSaleOrderDO;
import cn.iocoder.yudao.module.erp.dal.mysql.mrp.ErpMrpPlanMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.mrp.ErpProductionSuggestMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.mrp.ErpPurchaseSuggestMapper;
import cn.iocoder.yudao.module.erp.enums.mrp.ErpMrpPlanStatusEnum;
import cn.iocoder.yudao.module.erp.service.project.ErpProjectRoleTaskService;
import cn.iocoder.yudao.module.erp.service.sale.ErpSaleOrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ErpSaleOrderMrpFlowServiceImpl implements ErpSaleOrderMrpFlowService {

    @Resource
    private ErpSaleOrderService saleOrderService;
    @Resource
    private ErpMrpPlanService mrpPlanService;
    @Resource
    private ErpMrpPlanMapper erpMrpPlanMapper;
    @Resource
    private ErpMrpCalcService mrpCalcService;
    @Resource
    private ErpProjectRoleTaskService projectRoleTaskService;
    @Resource
    private ErpPurchaseSuggestMapper erpPurchaseSuggestMapper;
    @Resource
    private ErpProductionSuggestMapper erpProductionSuggestMapper;
    @Resource
    private PlatformTransactionManager transactionManager;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void releaseApprovedSaleOrder(Long saleOrderId) {
        ErpSaleOrderDO saleOrder = saleOrderService.validateSaleOrder(saleOrderId);
        if (saleOrder.getProjectId() != null) {
            projectRoleTaskService.createOrRefreshPcTask(saleOrder.getProjectId(),
                    saleOrder.getId(), saleOrder.getDeliveryDate());
        }
        Long planId = mrpPlanService.createAutoPlanForSaleOrder(saleOrder);
        markPlanRunning(planId);
        try {
            mrpCalcService.runForSaleOrders(planId, List.of(saleOrder));
            refreshMcTaskIfNecessary(saleOrder, planId);
            markPlanStatusInNewTransaction(planId, ErpMrpPlanStatusEnum.FINISHED.getStatus());
        } catch (RuntimeException ex) {
            markPlanStatusInNewTransaction(planId, ErpMrpPlanStatusEnum.FAILED.getStatus());
            throw ex;
        }
    }

    private void refreshMcTaskIfNecessary(ErpSaleOrderDO saleOrder, Long planId) {
        if (saleOrder.getProjectId() == null) {
            return;
        }
        long purchaseSuggestCount = erpPurchaseSuggestMapper.selectCountByPlanIdAndProjectId(planId, saleOrder.getProjectId());
        long productionSuggestCount = erpProductionSuggestMapper.selectCountByPlanIdAndProjectId(planId, saleOrder.getProjectId());
        if (purchaseSuggestCount <= 0 && productionSuggestCount <= 0) {
            return;
        }
        projectRoleTaskService.createOrRefreshMcTask(saleOrder.getProjectId(), planId,
                buildMcTaskSummary(purchaseSuggestCount, productionSuggestCount));
    }

    private String buildMcTaskSummary(long purchaseSuggestCount, long productionSuggestCount) {
        return String.format("MRP 已生成 %d 条采购建议、%d 条生产建议，请 MC 确认物料准备策略",
                purchaseSuggestCount, productionSuggestCount);
    }

    private void markPlanRunning(Long planId) {
        erpMrpPlanMapper.updateById(new ErpMrpPlanDO().setId(planId)
                .setStatus(ErpMrpPlanStatusEnum.RUNNING.getStatus())
                .setRunTime(LocalDateTime.now()));
    }

    private void markPlanStatusInNewTransaction(Long planId, Integer status) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        transactionTemplate.executeWithoutResult(unused ->
                erpMrpPlanMapper.updateById(new ErpMrpPlanDO().setId(planId).setStatus(status)));
    }

}
