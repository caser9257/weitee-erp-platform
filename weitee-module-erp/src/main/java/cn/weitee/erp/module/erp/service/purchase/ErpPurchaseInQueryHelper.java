package cn.weitee.erp.module.erp.service.purchase;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.purchase.vo.in.ErpPurchaseInPageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseInDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseInItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseInStockExecuteDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseInStockExecuteItemBatchDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseInStockExecuteItemDO;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinancePaymentAllocateMapper;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseInItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseInMapper;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseInStockExecuteItemBatchMapper;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseInStockExecuteItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseInStockExecuteMapper;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.enums.ErpFinancePaymentAllocateStatusEnum;
import cn.weitee.erp.module.erp.enums.ErpPurchaseInStockInStatusEnum;
import cn.weitee.erp.module.erp.enums.ErpQaStatusEnum;
import cn.weitee.erp.module.erp.enums.common.ErpBizTypeEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PURCHASE_IN_NOT_EXISTS;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PURCHASE_IN_NOT_APPROVE;

/**
 * 采购入库查询辅助类。
 * 提取自 ErpPurchaseInServiceImpl。
 */
@Component
class ErpPurchaseInQueryHelper {

    @Resource
    private ErpPurchaseInMapper erpPurchaseInMapper;
    @Resource
    private ErpPurchaseInItemMapper erpPurchaseInItemMapper;
    @Resource
    private ErpPurchaseInStockExecuteMapper erpPurchaseInStockExecuteMapper;
    @Resource
    private ErpPurchaseInStockExecuteItemMapper erpPurchaseInStockExecuteItemMapper;
    @Resource
    private ErpPurchaseInStockExecuteItemBatchMapper erpPurchaseInStockExecuteItemBatchMapper;
    @Resource
    private ErpFinancePaymentAllocateMapper erpFinancePaymentAllocateMapper;

    // region 基础查询

    public ErpPurchaseInDO getPurchaseIn(Long id) {
        return erpPurchaseInMapper.selectById(id);
    }

    public ErpPurchaseInDO validatePurchaseIn(Long id) {
        ErpPurchaseInDO purchaseIn = validatePurchaseInExists(id);
        if (ObjectUtil.notEqual(purchaseIn.getStatus(), ErpAuditStatus.APPROVE.getStatus())) {
            throw exception(PURCHASE_IN_NOT_APPROVE);
        }
        return purchaseIn;
    }

    public ErpPurchaseInDO validatePurchaseInExists(Long id) {
        ErpPurchaseInDO purchaseIn = erpPurchaseInMapper.selectById(id);
        if (purchaseIn == null) {
            throw exception(PURCHASE_IN_NOT_EXISTS);
        }
        return purchaseIn;
    }

    public PageResult<ErpPurchaseInDO> getPurchaseInPage(ErpPurchaseInPageReqVO pageReqVO) {
        return erpPurchaseInMapper.selectPage(pageReqVO);
    }

    public List<ErpPurchaseInDO> getPurchaseInListByIds(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return erpPurchaseInMapper.selectByIds(ids);
    }

    public List<ErpPurchaseInDO> getPurchaseInListByOrderIds(Collection<Long> orderIds) {
        if (CollUtil.isEmpty(orderIds)) {
            return Collections.emptyList();
        }
        return erpPurchaseInMapper.selectListByOrderIds(orderIds);
    }

    // endregion

    // region 入库明细查询

    public List<ErpPurchaseInItemDO> getPurchaseInItemListByInId(Long inId) {
        return erpPurchaseInItemMapper.selectListByInId(inId);
    }

    public List<ErpPurchaseInItemDO> getPurchaseInItemListByInIds(Collection<Long> inIds) {
        if (CollUtil.isEmpty(inIds)) {
            return Collections.emptyList();
        }
        return erpPurchaseInItemMapper.selectListByInIds(inIds);
    }

    // endregion

    // region 入库执行记录查询

    public List<ErpPurchaseInStockExecuteDO> getPurchaseInStockExecuteListByPurchaseInId(Long purchaseInId) {
        return erpPurchaseInStockExecuteMapper.selectListByPurchaseInId(purchaseInId);
    }

    public List<ErpPurchaseInStockExecuteDO> getPurchaseInStockExecuteListByIds(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return erpPurchaseInStockExecuteMapper.selectBatchIds(ids);
    }

    public List<ErpPurchaseInStockExecuteItemDO> getPurchaseInStockExecuteItemListByExecuteIds(Collection<Long> executeIds) {
        return erpPurchaseInStockExecuteItemMapper.selectListByExecuteIds(executeIds);
    }

    public List<ErpPurchaseInStockExecuteItemDO> getPurchaseInStockExecuteItemListByIds(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return erpPurchaseInStockExecuteItemMapper.selectBatchIds(ids);
    }

    public List<ErpPurchaseInStockExecuteItemBatchDO> getPurchaseInStockExecuteItemBatchListByExecuteItemIds(Collection<Long> executeItemIds) {
        return erpPurchaseInStockExecuteItemBatchMapper.selectListByExecuteItemIds(executeItemIds);
    }

    public List<ErpPurchaseInStockExecuteItemBatchDO> getPurchaseInStockExecuteItemBatchListByPurchaseSourceBatchId(Long purchaseSourceBatchId) {
        return erpPurchaseInStockExecuteItemBatchMapper.selectListByPurchaseSourceBatchId(purchaseSourceBatchId);
    }

    // endregion

    // region 状态判断

    public boolean isApprovalRunning(ErpPurchaseInDO purchaseIn) {
        return ErpAuditStatus.PROCESS.getStatus().equals(purchaseIn.getStatus())
                && StrUtil.isNotBlank(purchaseIn.getProcessInstanceId());
    }

    public boolean hasApprovedAllocate(Long bizId) {
        return ObjectUtil.defaultIfNull(erpFinancePaymentAllocateMapper.selectCountByBizTypeAndBizIdAndStatus(
                ErpBizTypeEnum.PURCHASE_IN.getType(), bizId,
                ErpFinancePaymentAllocateStatusEnum.APPROVED.getStatus()), 0L) > 0;
    }

    public boolean isQualityChecked(Integer qaStatus) {
        return qaStatus != null && !ErpQaStatusEnum.TO_INSPECT.getStatus().equals(qaStatus);
    }

    public boolean canConfirmStockInByQaStatus(Integer qaStatus) {
        return ErpQaStatusEnum.PARTIAL.getStatus().equals(qaStatus)
                || ErpQaStatusEnum.PASSED.getStatus().equals(qaStatus);
    }

    public boolean canExecuteStockInByStatus(Integer stockInStatus) {
        return ErpPurchaseInStockInStatusEnum.TO_STOCK_IN.getStatus().equals(stockInStatus)
                || ErpPurchaseInStockInStatusEnum.PARTIAL_STOCKED_IN.getStatus().equals(stockInStatus);
    }

    public boolean isStockedIn(Integer stockInStatus) {
        return ErpPurchaseInStockInStatusEnum.STOCKED_IN.getStatus().equals(stockInStatus);
    }

    public boolean hasStockedCount(ErpPurchaseInDO purchaseIn) {
        return ObjectUtil.defaultIfNull(purchaseIn.getStockInCount(), BigDecimal.ZERO).compareTo(BigDecimal.ZERO) > 0;
    }

    public BigDecimal calculateRemainingStockInCount(BigDecimal qaPassCount, BigDecimal stockInCount) {
        BigDecimal remainingCount = ObjectUtil.defaultIfNull(qaPassCount, BigDecimal.ZERO)
                .subtract(ObjectUtil.defaultIfNull(stockInCount, BigDecimal.ZERO));
        return remainingCount.compareTo(BigDecimal.ZERO) > 0 ? remainingCount : BigDecimal.ZERO;
    }

    // endregion
}