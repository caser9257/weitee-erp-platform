package cn.weitee.erp.module.erp.service.product.approval;

import cn.hutool.core.util.StrUtil;
import cn.weitee.erp.module.bpm.service.approval.provider.ApprovalContext;
import cn.weitee.erp.module.bpm.service.approval.provider.ApprovalContextProvider;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductPendingChangeDO;
import cn.weitee.erp.module.erp.enums.ErpProductBpmConstants;
import cn.weitee.erp.module.erp.service.product.ErpProductPendingChangeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCT_PENDING_CHANGE_NOT_EXISTS;

/**
 * 物料批量修改审批上下文提供者
 *
 * bizId = 批次编号（batchId），一次批量导入的暂存变更归组为一个批次审批。
 */
@Component
@Slf4j
public class ProductBatchUpdateContextProvider implements ApprovalContextProvider {

    @Resource
    private ErpProductPendingChangeService pendingChangeService;

    @Override
    public String getSceneCode() {
        return ErpProductBpmConstants.SCENE_CODE_UPDATE_BATCH;
    }

    @Override
    public ApprovalContext getContext(Long bizId) {
        List<ErpProductPendingChangeDO> pendings = pendingChangeService.getPendingChangesByBatch(bizId);
        if (pendings.isEmpty()) {
            throw exception(PRODUCT_PENDING_CHANGE_NOT_EXISTS);
        }
        int size = pendings.size();
        Map<String, Object> variables = new HashMap<>();
        variables.put("sceneName", "物料批量修改审批");
        variables.put(ErpProductBpmConstants.VARIABLE_BATCH_ID, bizId);
        variables.put(ErpProductBpmConstants.VARIABLE_BATCH_SIZE, size);
        Map<String, Object> notifyParams = new HashMap<>();
        notifyParams.put("bizTitle", "物料批量修改（" + size + " 条）");
        notifyParams.put("bizNo", String.valueOf(bizId));
        return ApprovalContext.builder()
                .bizId(bizId)
                .bizNo(String.valueOf(bizId))
                .bizTitle("物料批量修改（" + size + " 条）")
                .startUserId(parseCreatorId(pendings.get(0).getCreator()))
                .variables(variables)
                .notifyParams(notifyParams)
                .build();
    }

    private Long parseCreatorId(String creator) {
        return creator != null && StrUtil.isNumeric(creator) ? Long.valueOf(creator) : null;
    }

}
