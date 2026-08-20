package cn.weitee.erp.module.erp.service.rd;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.weitee.erp.module.bpm.service.approval.BpmApprovalRuntimeService;
import cn.weitee.erp.module.erp.dal.dataobject.rd.ErpRdBomDO;
import cn.weitee.erp.module.erp.dal.mysql.rd.ErpRdBomMapper;
import cn.weitee.erp.module.erp.enums.ErpRdBomBpmConstants;
import cn.weitee.erp.module.erp.enums.rd.ErpRdBomStatusEnum;
import cn.weitee.erp.module.erp.util.ErpTransactionUtils;
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

@Service
@Validated
@Slf4j
public class ErpRdBomBpmServiceImpl implements ErpRdBomBpmService {

    @Resource
    private ErpRdBomMapper erpRdBomMapper;
    @Resource
    private BpmApprovalRuntimeService approvalRuntimeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String submitRdBom(Long userId, Long bomId) {
        ErpRdBomDO bom = getRequiredBom(bomId);
        if (ObjectUtil.equal(bom.getStatus(), ErpRdBomStatusEnum.APPROVE.getStatus())) {
            throw exception(RD_BOM_APPROVE_FAIL);
        }
        if (ObjectUtil.equal(bom.getStatus(), ErpRdBomStatusEnum.PROCESS.getStatus())
                && StrUtil.isNotBlank(bom.getProcessInstanceId())) {
            throw exception(RD_BOM_BPM_SUBMIT_FAIL);
        }
        // 事务内：只写本地为审批中
        erpRdBomMapper.updateById(new ErpRdBomDO()
                .setId(bomId)
                .setStatus(ErpRdBomStatusEnum.PROCESS.getStatus())
                .setProcessInstanceId(null));

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
        ErpTransactionUtils.afterCommit(() -> {
            try {
                approvalRuntimeService.cancel(ErpRdBomBpmConstants.SCENE_CODE, bomId, userId, reason);
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
