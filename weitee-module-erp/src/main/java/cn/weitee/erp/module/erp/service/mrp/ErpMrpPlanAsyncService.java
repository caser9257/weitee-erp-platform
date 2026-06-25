package cn.weitee.erp.module.erp.service.mrp;

import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpMrpPlanDO;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpMrpPlanMapper;
import cn.weitee.erp.module.erp.enums.mrp.ErpMrpPlanStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import jakarta.annotation.Resource;
import java.util.concurrent.Executor;

@Service
@Slf4j
public class ErpMrpPlanAsyncService {

    @Resource
    private ErpMrpCalcService mrpCalcService;
    @Resource
    private ErpMrpPlanMapper erpMrpPlanMapper;
    @Resource
    private PlatformTransactionManager transactionManager;
    @Resource(name = "erpMrpRunExecutor")
    private Executor erpMrpRunExecutor;

    public void submitRunPlan(Long planId) {
        erpMrpRunExecutor.execute(() -> executeRunPlan(planId));
    }

    private void executeRunPlan(Long planId) {
        try {
            mrpCalcService.run(planId);
            markPlanStatusInNewTransaction(planId, ErpMrpPlanStatusEnum.FINISHED.getStatus());
        } catch (Exception ex) {
            log.error("[executeRunPlan][planId({}) 执行 MRP 失败]", planId, ex);
            markPlanStatusInNewTransaction(planId, ErpMrpPlanStatusEnum.FAILED.getStatus());
        }
    }

    private void markPlanStatusInNewTransaction(Long planId, Integer status) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        transactionTemplate.executeWithoutResult(unused ->
                erpMrpPlanMapper.updateById(new ErpMrpPlanDO().setId(planId).setStatus(status)));
    }

}
