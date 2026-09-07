package cn.weitee.erp.module.erp.service.rd;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.weitee.erp.module.bpm.service.approval.BpmApprovalRuntimeService;
import cn.weitee.erp.module.erp.dal.dataobject.rd.ErpRdBomDO;
import cn.weitee.erp.module.erp.dal.mysql.rd.ErpRdBomMapper;
import cn.weitee.erp.module.erp.enums.ErpRdBomBpmConstants;
import cn.weitee.erp.module.erp.enums.rd.ErpRdBomChangeType;
import cn.weitee.erp.module.erp.enums.rd.ErpRdBomStatusEnum;
import cn.weitee.erp.module.erp.util.ErpTransactionUtils;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.RD_BOM_APPROVE_FAIL;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.RD_BOM_BPM_CANCEL_FAIL;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.RD_BOM_BPM_SUBMIT_FAIL;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.RD_BOM_NOT_EXISTS;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.RD_BOM_STATUS_UPDATE_ILLEGAL;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.RD_BOM_VOID_LOCKED;

@Service
@Validated
@Slf4j
public class ErpRdBomBpmServiceImpl implements ErpRdBomBpmService {

    @Resource
    private ErpRdBomMapper erpRdBomMapper;
    @Resource
    private BpmApprovalRuntimeService approvalRuntimeService;
    @Resource
    private ErpRdBomChangeLogService changeLogService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String submitRdBom(Long userId, Long bomId) {
        ErpRdBomDO bom = getRequiredBom(bomId);
        if (ObjectUtil.equal(bom.getStatus(), ErpRdBomStatusEnum.APPROVE.getStatus())) {
            throw exception(RD_BOM_APPROVE_FAIL);
        }
        if (ObjectUtil.equal(bom.getStatus(), ErpRdBomStatusEnum.VOID.getStatus())) {
            throw exception(RD_BOM_VOID_LOCKED);
        }
        if (ObjectUtil.equal(bom.getStatus(), ErpRdBomStatusEnum.PROCESS.getStatus())
                && StrUtil.isNotBlank(bom.getProcessInstanceId())) {
            throw exception(RD_BOM_BPM_SUBMIT_FAIL);
        }
        // 事务内：CAS 写本地为审批中（WHERE status=读到的旧状态），
        // 防御并发双击/重放穿透读-判-写窗口产生重复流程实例
        int updated = erpRdBomMapper.update(null, new LambdaUpdateWrapper<ErpRdBomDO>()
                .eq(ErpRdBomDO::getId, bomId)
                .eq(ErpRdBomDO::getStatus, bom.getStatus())
                .set(ErpRdBomDO::getStatus, ErpRdBomStatusEnum.PROCESS.getStatus())
                .set(ErpRdBomDO::getProcessInstanceId, null));
        if (updated == 0) {
            throw exception(RD_BOM_STATUS_UPDATE_ILLEGAL);
        }
        changeLogService.logChange(bomId, ErpRdBomChangeType.SUBMIT.getType(),
                "提交审批，当前版本=" + bom.getVersion());

        // 事务外：调 BPM 创建流程
        ErpTransactionUtils.afterCommit(() -> {
            try {
                String processInstanceId = approvalRuntimeService.submit(
                        ErpRdBomBpmConstants.SCENE_CODE, bomId, userId);
                erpRdBomMapper.updateById(new ErpRdBomDO()
                        .setId(bomId)
                        .setProcessInstanceId(processInstanceId));
            } catch (Exception e) {
                log.error("[submitRdBom] BPM 创建失败，bomId={}", bomId, e);
                erpRdBomMapper.updateById(new ErpRdBomDO()
                        .setId(bomId)
                        .setStatus(ErpRdBomStatusEnum.FAILED.getStatus())
                        .setProcessInstanceId(null));
            }
        });
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelRdBomApproval(Long userId, Long bomId, String reason) {
        ErpRdBomDO bom = getRequiredBom(bomId);
        if (!ObjectUtil.equal(bom.getStatus(), ErpRdBomStatusEnum.PROCESS.getStatus())
                || StrUtil.isBlank(bom.getProcessInstanceId())) {
            throw exception(RD_BOM_BPM_CANCEL_FAIL);
        }
        // 事务内仅校验，实际撤回在 afterCommit 中，终态由 ResultHandler 统一回写
        // BPM 底层要求取消原因必填：入口兜底默认值，避免空原因导致撤回校验失败
        String safeReason = StrUtil.isNotBlank(reason) ? reason : "发起人撤回";
        ErpTransactionUtils.afterCommit(() -> {
            try {
                approvalRuntimeService.cancel(ErpRdBomBpmConstants.SCENE_CODE, bomId, userId, safeReason);
            } catch (Exception e) {
                log.warn("[cancelRdBomApproval] BPM 撤回失败，bomId={}", bomId, e);
                throw e;
            }
        });
    }

    private ErpRdBomDO getRequiredBom(Long bomId) {
        ErpRdBomDO bom = erpRdBomMapper.selectById(bomId);
        if (bom == null) {
            throw exception(RD_BOM_NOT_EXISTS);
        }
        return bom;
    }

}
