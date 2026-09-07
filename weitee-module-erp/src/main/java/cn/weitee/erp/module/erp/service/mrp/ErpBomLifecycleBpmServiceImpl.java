package cn.weitee.erp.module.erp.service.mrp;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.weitee.erp.module.bpm.service.approval.BpmApprovalRuntimeService;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpBomDO;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpBomMapper;
import cn.weitee.erp.module.erp.enums.ErpBomBpmConstants;
import cn.weitee.erp.module.erp.enums.mrp.ErpBomStatusEnum;
import cn.weitee.erp.module.erp.util.ErpTransactionUtils;
import lombok.extern.slf4j.Slf4j;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.BOM_LIFECYCLE_CANCEL_FAIL;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.BOM_LIFECYCLE_SUBMIT_FAIL;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.BOM_NOT_EXISTS;

@Service
@Validated
@Slf4j
public class ErpBomLifecycleBpmServiceImpl implements ErpBomLifecycleBpmService {

    @Resource
    private ErpBomMapper bomMapper;
    @Resource
    private BpmApprovalRuntimeService approvalRuntimeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String submitDisableApproval(Long userId, Long bomId, String reason) {
        ErpBomDO bom = bomMapper.selectById(bomId);
        if (bom == null) {
            throw exception(BOM_NOT_EXISTS);
        }
        // 防重：仅启用中的 BOM 可发起停用；重复发起由 BPM 快照 sceneCode+bizId 幂等拦截
        if (!ErpBomStatusEnum.ENABLE.getStatus().equals(bom.getStatus())) {
            throw exception(BOM_LIFECYCLE_SUBMIT_FAIL, bom.getBomCode());
        }
        // 事务内无本地状态变更（停用动作在审批通过后由 Handler 执行），
        // afterCommit 创建流程并将流程实例 ID 落库（在途标记，供前端展示「停用审批中」+撤回）；
        // 失败时快照被 Runtime 标记 FAILED，可重新提交。
        ErpTransactionUtils.afterCommit(() -> {
            try {
                String processInstanceId = approvalRuntimeService.submit(ErpBomBpmConstants.SCENE_CODE_DISABLE, bomId, userId);
                bomMapper.update(null, new LambdaUpdateWrapper<ErpBomDO>()
                        .eq(ErpBomDO::getId, bomId)
                        .eq(ErpBomDO::getStatus, ErpBomStatusEnum.ENABLE.getStatus())
                        .set(ErpBomDO::getProcessInstanceId, processInstanceId));
            } catch (Exception e) {
                log.error("[submitDisableApproval] BPM 创建失败，bomId={}", bomId, e);
                throw e;
            }
        });
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelDisableApproval(Long userId, Long bomId, String reason) {
        ErpBomDO bom = bomMapper.selectById(bomId);
        if (bom == null) {
            throw exception(BOM_NOT_EXISTS);
        }
        // 停用申请在途时 BOM 本身仍为 ENABLE（审批通过才落 DISABLE），无法从主表判断在途；
        // 直接触发 BPM 撤回，无在途流程时由 Runtime 抛出明确异常
        if (ObjectUtil.equal(bom.getStatus(), ErpBomStatusEnum.DISABLE.getStatus())) {
            throw exception(BOM_LIFECYCLE_CANCEL_FAIL, bom.getBomCode());
        }
        // BPM 底层要求取消原因必填：入口兜底默认值
        String safeReason = StrUtil.isNotBlank(reason) ? reason : "发起人撤回";
        ErpTransactionUtils.afterCommit(() -> {
            try {
                approvalRuntimeService.cancel(ErpBomBpmConstants.SCENE_CODE_DISABLE, bomId, userId, safeReason);
            } catch (Exception e) {
                log.warn("[cancelDisableApproval] BPM 撤回失败，bomId={}", bomId, e);
                throw e;
            }
        });
    }

}
