package cn.weitee.erp.module.erp.service.purchase;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.number.MoneyUtils;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.module.erp.controller.admin.purchase.vo.in.ErpPurchaseInBatchUpdateReqVO;
import cn.weitee.erp.module.erp.controller.admin.purchase.vo.in.ErpPurchaseInBatchUpdateResultVO;
import cn.weitee.erp.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.weitee.erp.module.erp.controller.admin.purchase.vo.in.ErpPurchaseInPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.purchase.vo.in.ErpPurchaseInConfirmStockInReqVO;
import cn.weitee.erp.module.erp.controller.admin.purchase.vo.in.ErpPurchaseInQualityCheckReqVO;
import cn.weitee.erp.module.erp.controller.admin.purchase.vo.in.ErpPurchaseInSaveReqVO;
import cn.weitee.erp.module.erp.controller.admin.purchase.vo.in.ErpPurchaseInStockExecuteCreateReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseInDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseInItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseInStockExecuteDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseInStockExecuteItemBatchDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseInStockExecuteItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseOrderDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseOrderItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseSourceBatchDO;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseInItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseInMapper;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseInStockExecuteItemBatchMapper;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseInStockExecuteItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseInStockExecuteMapper;
import cn.weitee.erp.module.erp.dal.redis.no.ErpNoRedisDAO;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.enums.ErpPurchaseInStockExecuteStatusEnum;
import cn.weitee.erp.module.erp.enums.ErpPurchaseInStockInStatusEnum;
import cn.weitee.erp.module.erp.enums.ErpQaStatusEnum;
import cn.weitee.erp.module.erp.enums.common.ErpBizTypeEnum;
import cn.weitee.erp.module.erp.service.finance.ErpAccountService;
import cn.weitee.erp.module.erp.service.finance.ErpFinanceAssetCandidateService;
import cn.weitee.erp.module.erp.service.finance.ErpApStatementService;
import cn.weitee.erp.module.erp.service.finance.ErpFinanceBizHookService;
import cn.weitee.erp.module.erp.service.product.ErpProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.*;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.*;

@Service
@Validated
@Slf4j
public class ErpPurchaseInServiceImpl implements ErpPurchaseInService {

    private static final String BATCH_EDIT_MODE_OVERWRITE = "overwrite";
    private static final String BATCH_FIELD_ACCOUNT_ID = "accountId";
    private static final String BATCH_FIELD_IN_TIME = "inTime";
    private static final String BATCH_FIELD_REMARK = "remark";
    private static final java.time.format.DateTimeFormatter BATCH_IN_TIME_FORMATTER =
            java.time.format.DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss")
                    .withResolverStyle(java.time.format.ResolverStyle.STRICT);

    // region 直接依赖

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
    private ErpNoRedisDAO noRedisDAO;
    @Resource
    private ErpProductService productService;
    @Resource
    @Lazy
    private ErpPurchaseOrderService purchaseOrderService;
    @Resource
    private ErpAccountService accountService;
    @Resource
    @Lazy
    private ErpApStatementService apStatementService;
    @Resource
    @Lazy
    private ErpFinanceBizHookService financeBizHookService;
    @Resource
    @Lazy
    private ErpFinanceAssetCandidateService financeAssetCandidateService;
    @Resource
    private ErpPurchaseInQualityService purchaseInQualityService;
    @Resource
    private ErpPurchaseSourceBatchService purchaseSourceBatchService;

    // endregion

    // region Helper 依赖

    @Resource
    private ErpPurchaseInQueryHelper queryHelper;
    @Resource
    private ErpPurchaseInStockExecuteHelper stockExecuteHelper;

    // endregion

    // region 创建 / 更新 / 删除

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPurchaseIn(ErpPurchaseInSaveReqVO createReqVO) {
        ErpPurchaseOrderDO purchaseOrder = purchaseOrderService.validatePurchaseOrder(createReqVO.getOrderId());
        List<ErpPurchaseInItemDO> purchaseInItems = validatePurchaseInItems(createReqVO.getItems());
        validatePurchaseOrderItemRemainingCount(createReqVO.getOrderId(), purchaseInItems);
        accountService.validateAccount(createReqVO.getAccountId());
        String no = noRedisDAO.generate(ErpNoRedisDAO.PURCHASE_IN_NO_PREFIX);
        if (erpPurchaseInMapper.selectByNo(no) != null) {
            throw exception(PURCHASE_IN_NO_EXISTS);
        }

        ErpPurchaseInDO purchaseIn = BeanUtils.toBean(createReqVO, ErpPurchaseInDO.class, in -> in
                .setNo(no).setStatus(ErpAuditStatus.DRAFT.getStatus())
                .setQaStatus(ErpQaStatusEnum.TO_INSPECT.getStatus())
                .setQaPassCount(BigDecimal.ZERO)
                .setQaRejectCount(BigDecimal.ZERO)
                .setStockInCount(BigDecimal.ZERO))
                .setOrderNo(purchaseOrder.getNo()).setSupplierId(purchaseOrder.getSupplierId());
        calculateTotalPrice(purchaseIn, purchaseInItems);
        erpPurchaseInMapper.insert(purchaseIn);
        purchaseInItems.forEach(o -> o.setInId(purchaseIn.getId()).setStockInCount(BigDecimal.ZERO));
        erpPurchaseInItemMapper.insertBatch(purchaseInItems);

        updatePurchaseOrderInCount(createReqVO.getOrderId());
        return purchaseIn.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePurchaseIn(ErpPurchaseInSaveReqVO updateReqVO) {
        ErpPurchaseInDO purchaseIn = queryHelper.validatePurchaseInExists(updateReqVO.getId());
        if (ErpAuditStatus.APPROVE.getStatus().equals(purchaseIn.getStatus())) {
            throw exception(PURCHASE_IN_UPDATE_FAIL_APPROVE, purchaseIn.getNo());
        }
        if (queryHelper.isApprovalRunning(purchaseIn)) {
            throw exception(PURCHASE_IN_UPDATE_FAIL_PROCESSING, purchaseIn.getNo());
        }
        ErpPurchaseOrderDO purchaseOrder = purchaseOrderService.validatePurchaseOrder(updateReqVO.getOrderId());
        accountService.validateAccount(updateReqVO.getAccountId());
        List<ErpPurchaseInItemDO> purchaseInItems = validatePurchaseInItems(updateReqVO.getItems());
        validatePurchaseOrderItemRemainingCount(updateReqVO.getOrderId(), purchaseInItems);

        ErpPurchaseInDO updateObj = BeanUtils.toBean(updateReqVO, ErpPurchaseInDO.class)
                .setOrderNo(purchaseOrder.getNo()).setSupplierId(purchaseOrder.getSupplierId());
        calculateTotalPrice(updateObj, purchaseInItems);
        erpPurchaseInMapper.updateById(updateObj);
        updatePurchaseInItemList(updateReqVO.getId(), purchaseInItems);

        updatePurchaseOrderInCount(updateObj.getOrderId());
        if (ObjectUtil.notEqual(purchaseIn.getOrderId(), updateObj.getOrderId())) {
            updatePurchaseOrderInCount(purchaseIn.getOrderId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ErpPurchaseInBatchUpdateResultVO updatePurchaseInBatch(ErpPurchaseInBatchUpdateReqVO reqVO) {
        if (!BATCH_EDIT_MODE_OVERWRITE.equals(reqVO.getMode())) {
            throw exception(PURCHASE_IN_BATCH_UPDATE_FIELD_NOT_SUPPORT, reqVO.getFieldKey());
        }
        String fieldKey = normalizeBatchFieldKey(reqVO.getFieldKey());
        Object value = parseBatchValue(fieldKey, reqVO.getValue());
        List<Long> uniqueIds = List.copyOf(new java.util.LinkedHashSet<>(reqVO.getIds()));
        List<ErpPurchaseInDO> purchaseIns = erpPurchaseInMapper.selectByIds(uniqueIds);
        Map<Long, ErpPurchaseInDO> purchaseInMap = convertMap(purchaseIns, ErpPurchaseInDO::getId);
        for (Long id : uniqueIds) {
            ErpPurchaseInDO purchaseIn = purchaseInMap.get(id);
            if (purchaseIn == null) {
                throw exception(PURCHASE_IN_NOT_EXISTS);
            }
            if (ErpAuditStatus.APPROVE.getStatus().equals(purchaseIn.getStatus())) {
                throw exception(PURCHASE_IN_UPDATE_FAIL_APPROVE, purchaseIn.getNo());
            }
            if (queryHelper.isApprovalRunning(purchaseIn)) {
                throw exception(PURCHASE_IN_UPDATE_FAIL_PROCESSING, purchaseIn.getNo());
            }
        }

        uniqueIds.forEach(id -> {
            ErpPurchaseInDO updateObj = new ErpPurchaseInDO().setId(id);
            applyBatchValue(updateObj, fieldKey, value);
            erpPurchaseInMapper.updateById(updateObj);
        });

        ErpPurchaseInBatchUpdateResultVO result = new ErpPurchaseInBatchUpdateResultVO();
        result.setSuccessCount(uniqueIds.size());
        result.setFailureCount(0);
        result.setUpdatedIds(uniqueIds);
        result.setFailedItems(Collections.emptyList());
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePurchaseIn(List<Long> ids) {
        List<ErpPurchaseInDO> purchaseIns = erpPurchaseInMapper.selectByIds(ids);
        if (CollUtil.isEmpty(purchaseIns)) {
            return;
        }
        purchaseIns.forEach(purchaseIn -> {
            if (ErpAuditStatus.APPROVE.getStatus().equals(purchaseIn.getStatus())) {
                throw exception(PURCHASE_IN_DELETE_FAIL_APPROVE, purchaseIn.getNo());
            }
            if (queryHelper.isApprovalRunning(purchaseIn)) {
                throw exception(PURCHASE_IN_DELETE_FAIL_PROCESSING, purchaseIn.getNo());
            }
        });

        purchaseIns.forEach(purchaseIn -> {
            erpPurchaseInMapper.deleteById(purchaseIn.getId());
            erpPurchaseInItemMapper.deleteByInId(purchaseIn.getId());
            updatePurchaseOrderInCount(purchaseIn.getOrderId());
        });
    }

    // endregion

    // region 状态变更

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePurchaseInStatus(Long id, Integer status) {
        boolean approve = ErpAuditStatus.APPROVE.getStatus().equals(status);
        ErpPurchaseInDO purchaseIn = queryHelper.validatePurchaseInExists(id);
        if (StrUtil.isNotBlank(purchaseIn.getProcessInstanceId())) {
            throw exception(approve ? PURCHASE_IN_APPROVE_FAIL : PURCHASE_IN_PROCESS_FAIL);
        }
        if (purchaseIn.getStatus().equals(status)) {
            throw exception(approve ? PURCHASE_IN_APPROVE_FAIL : PURCHASE_IN_PROCESS_FAIL);
        }
        if (!approve && queryHelper.hasApprovedAllocate(id)) {
            throw exception(PURCHASE_IN_PROCESS_FAIL_EXISTS_PAYMENT);
        }

        ErpPurchaseInDO updateObj = new ErpPurchaseInDO().setStatus(status);
        if (approve) {
            updateObj.setQaStatus(ErpQaStatusEnum.TO_INSPECT.getStatus())
                    .setQaTime(null).setQaUserId(null).setQaRemark(null)
                    .setQaPassCount(BigDecimal.ZERO).setQaRejectCount(BigDecimal.ZERO)
                    .setStockInCount(BigDecimal.ZERO)
                    .setStockInStatus(null).setStockInTime(null).setStockInUserId(null);
        }
        int updateCount = erpPurchaseInMapper.updateByIdAndStatus(id, purchaseIn.getStatus(), updateObj);
        if (updateCount == 0) {
            throw exception(approve ? PURCHASE_IN_APPROVE_FAIL : PURCHASE_IN_PROCESS_FAIL);
        }

        if (approve) {
            purchaseInQualityService.createQualityOrderIfAbsent(id);
            apStatementService.createStatementForPurchaseIn(purchaseIn);
            financeBizHookService.handleApprovedBiz(ErpBizTypeEnum.PURCHASE_IN.getType(), id,
                    defaultTime(purchaseIn.getInTime(), purchaseIn.getCreateTime(), purchaseIn.getUpdateTime()).toLocalDate());
            financeAssetCandidateService.createCandidateFromPurchaseIn(id);
        }
        if (!approve && queryHelper.isQualityChecked(purchaseIn.getQaStatus())) {
            List<ErpPurchaseInItemDO> purchaseInItems = erpPurchaseInItemMapper.selectListByInId(id);
            if (queryHelper.hasStockedCount(purchaseIn)) {
                stockExecuteHelper.reverseExecutedStockRecords(purchaseIn);
                erpPurchaseInStockExecuteMapper.updateStatusByPurchaseInId(id,
                        ErpPurchaseInStockExecuteStatusEnum.EXECUTED.getStatus(),
                        ErpPurchaseInStockExecuteStatusEnum.VOID.getStatus());
            }
            stockExecuteHelper.resetPurchaseInItemQualityCheck(id, purchaseInItems);
            erpPurchaseInMapper.updateById(new ErpPurchaseInDO().setId(id)
                    .setQaStatus(ErpQaStatusEnum.TO_INSPECT.getStatus())
                    .setQaTime(null).setQaUserId(null).setQaRemark(null)
                    .setQaPassCount(BigDecimal.ZERO).setQaRejectCount(BigDecimal.ZERO)
                    .setStockInCount(BigDecimal.ZERO)
                    .setStockInStatus(null).setStockInTime(null).setStockInUserId(null));
            stockExecuteHelper.clearPurchaseInItemStockInCount(purchaseInItems);
        }
        if (!approve) {
            purchaseInQualityService.voidQualityOrderByPurchaseIn(id, "采购入库反审核");
        }
        if (!approve) {
            apStatementService.closeStatementByBiz(ErpBizTypeEnum.PURCHASE_IN.getType(), id, "采购入库反审核关闭台账");
            financeBizHookService.handleRollbackBiz(ErpBizTypeEnum.PURCHASE_IN.getType(), id,
                    null, "采购入库反审核关闭双账套凭证");
        }
        updatePurchaseOrderInCount(purchaseIn.getOrderId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePurchaseInStatusByBpm(Long id, String processInstanceId, Integer status, String reason) {
        ErpPurchaseInDO purchaseIn = queryHelper.validatePurchaseInExists(id);
        if (!StrUtil.equals(processInstanceId, purchaseIn.getProcessInstanceId())) {
            throw exception(PURCHASE_IN_STATUS_UPDATE_ILLEGAL);
        }
        if (!ErpAuditStatus.PROCESS.getStatus().equals(purchaseIn.getStatus())) {
            log.warn("[updatePurchaseInStatusByBpm] 忽略非处理中采购入库单回调，id={}, currentStatus={}, callbackStatus={}",
                    id, purchaseIn.getStatus(), status);
            return;
        }
        boolean reject = ErpAuditStatus.REJECT.getStatus().equals(status);
        ErpPurchaseInDO updateObj = new ErpPurchaseInDO()
                .setId(id)
                .setProcessInstanceId(processInstanceId)
                .setStatus(status);
        if (ErpAuditStatus.APPROVE.getStatus().equals(status)) {
            updateObj.setQaStatus(ErpQaStatusEnum.TO_INSPECT.getStatus())
                    .setQaTime(null).setQaUserId(null).setQaRemark(null)
                    .setQaPassCount(BigDecimal.ZERO).setQaRejectCount(BigDecimal.ZERO)
                    .setStockInCount(BigDecimal.ZERO)
                    .setStockInStatus(null).setStockInTime(null).setStockInUserId(null);
        }
        if (reject) {
            updateObj.setLastRejectReason(reason);
            updateObj.setLastRejectTime(LocalDateTime.now());
            updateObj.setLastRejectUserId(null);
        }
        int updateCount = erpPurchaseInMapper.updateByIdAndStatus(id, purchaseIn.getStatus(), updateObj);
        if (updateCount == 0) {
            throw exception(PURCHASE_IN_STATUS_UPDATE_ILLEGAL);
        }
        if (ErpAuditStatus.APPROVE.getStatus().equals(status)) {
            purchaseInQualityService.createQualityOrderIfAbsent(id);
            apStatementService.createStatementForPurchaseIn(purchaseIn);
            updatePurchaseOrderInCount(purchaseIn.getOrderId());
            financeBizHookService.handleApprovedBiz(ErpBizTypeEnum.PURCHASE_IN.getType(), id,
                    defaultTime(purchaseIn.getInTime(), purchaseIn.getCreateTime(), purchaseIn.getUpdateTime()).toLocalDate());
            financeAssetCandidateService.createCandidateFromPurchaseIn(id);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rollbackPurchaseInStatusToDraftByBpm(Long id, String processInstanceId, String reason) {
        ErpPurchaseInDO purchaseIn = queryHelper.validatePurchaseInExists(id);
        if (!StrUtil.equals(processInstanceId, purchaseIn.getProcessInstanceId())) {
            throw exception(PURCHASE_IN_STATUS_UPDATE_ILLEGAL);
        }
        if (!ErpAuditStatus.PROCESS.getStatus().equals(purchaseIn.getStatus())) {
            log.warn("[rollbackPurchaseInStatusToDraftByBpm] 忽略非处理中采购入库单回退回调，id={}, currentStatus={}",
                    id, purchaseIn.getStatus());
            return;
        }
        ErpPurchaseInDO updateObj = new ErpPurchaseInDO()
                .setId(id)
                .setStatus(ErpAuditStatus.DRAFT.getStatus())
                .setProcessInstanceId(null);
        int updateCount = erpPurchaseInMapper.updateByIdAndStatus(id, purchaseIn.getStatus(), updateObj);
        if (updateCount == 0) {
            throw exception(PURCHASE_IN_STATUS_UPDATE_ILLEGAL);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void qualityCheckPurchaseIn(Long userId, ErpPurchaseInQualityCheckReqVO reqVO) {
        purchaseInQualityService.submitPurchaseInQualityByPurchaseIn(userId, reqVO);
    }

    // endregion

    // region 入库执行

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmPurchaseInStockIn(Long userId, ErpPurchaseInConfirmStockInReqVO reqVO) {
        ErpPurchaseInDO purchaseIn = queryHelper.validatePurchaseInExists(reqVO.getId());
        if (!ErpAuditStatus.APPROVE.getStatus().equals(purchaseIn.getStatus())) {
            throw exception(PURCHASE_IN_CONFIRM_STOCK_IN_FAIL_STATUS);
        }
        if (!queryHelper.canConfirmStockInByQaStatus(purchaseIn.getQaStatus())) {
            throw exception(PURCHASE_IN_CONFIRM_STOCK_IN_FAIL_QA_STATUS);
        }
        if (!queryHelper.canExecuteStockInByStatus(purchaseIn.getStockInStatus())) {
            throw exception(PURCHASE_IN_CONFIRM_STOCK_IN_FAIL_STOCK_STATUS);
        }

        List<ErpPurchaseInItemDO> purchaseInItems = erpPurchaseInItemMapper.selectListByInId(reqVO.getId());
        Map<Long, ErpProductRespVO> productMap = productService.getProductVOMap(
                convertSet(purchaseInItems, ErpPurchaseInItemDO::getProductId));
        boolean hasBatchManagedProduct = purchaseInItems.stream()
                .filter(item -> queryHelper.calculateRemainingStockInCount(item.getQaPassCount(), item.getStockInCount())
                        .compareTo(BigDecimal.ZERO) > 0)
                .anyMatch(item -> {
                    ErpProductRespVO product = productMap.get(item.getProductId());
                    return product != null && Boolean.TRUE.equals(product.getBatchControlFlag());
                });
        if (hasBatchManagedProduct) {
            throw exception(PURCHASE_IN_CONFIRM_STOCK_IN_FAIL_BATCH_REQUIRED);
        }
        ErpPurchaseInStockExecuteCreateReqVO executeReqVO = new ErpPurchaseInStockExecuteCreateReqVO();
        executeReqVO.setPurchaseInId(reqVO.getId());
        executeReqVO.setRemark("整单确认入库");
        executeReqVO.setItems(convertList(purchaseInItems, item -> {
            BigDecimal remainingCount = queryHelper.calculateRemainingStockInCount(item.getQaPassCount(), item.getStockInCount());
            if (remainingCount.compareTo(BigDecimal.ZERO) <= 0) {
                return null;
            }
            ErpPurchaseInStockExecuteCreateReqVO.Item executeItem = new ErpPurchaseInStockExecuteCreateReqVO.Item();
            executeItem.setPurchaseInItemId(item.getId());
            executeItem.setCount(remainingCount);
            return executeItem;
        }).stream().filter(java.util.Objects::nonNull).toList());
        createPurchaseInStockExecute(userId, executeReqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createPurchaseInStockExecute(Long userId, ErpPurchaseInStockExecuteCreateReqVO reqVO) {
        ErpPurchaseInDO purchaseIn = queryHelper.validatePurchaseInExists(reqVO.getPurchaseInId());
        validatePurchaseInCanStockExecute(purchaseIn);
        List<ErpPurchaseInItemDO> purchaseInItems = erpPurchaseInItemMapper.selectListByInId(reqVO.getPurchaseInId());
        Map<Long, ErpPurchaseInItemDO> purchaseInItemMap = convertMap(purchaseInItems, ErpPurchaseInItemDO::getId);
        Map<Long, ErpProductRespVO> productMap = productService.getProductVOMap(
                convertSet(purchaseInItems, ErpPurchaseInItemDO::getProductId));
        List<ErpPurchaseInStockExecuteItemDO> executeItems = stockExecuteHelper.buildExecuteItems(reqVO, purchaseIn, purchaseInItemMap, productMap, queryHelper);
        if (CollUtil.isEmpty(executeItems)) {
            throw exception(PURCHASE_IN_CONFIRM_STOCK_IN_FAIL_STOCK_STATUS);
        }

        String executeNo = noRedisDAO.generate(ErpNoRedisDAO.PURCHASE_IN_NO_PREFIX + "SE");
        ErpPurchaseInStockExecuteDO executeDO = new ErpPurchaseInStockExecuteDO()
                .setNo(executeNo)
                .setPurchaseInId(purchaseIn.getId())
                .setStatus(ErpPurchaseInStockExecuteStatusEnum.EXECUTED.getStatus())
                .setRemark(reqVO.getRemark());
        erpPurchaseInStockExecuteMapper.insert(executeDO);
        executeItems.forEach(item -> {
            item.setExecuteId(executeDO.getId());
            erpPurchaseInStockExecuteItemMapper.insert(item);
        });
        List<ErpPurchaseInStockExecuteItemBatchDO> executeItemBatches =
                stockExecuteHelper.buildExecuteItemBatches(reqVO, purchaseIn, executeItems, purchaseInItemMap, productMap);
        if (CollUtil.isNotEmpty(executeItemBatches)) {
            executeItemBatches.forEach(erpPurchaseInStockExecuteItemBatchMapper::insert);
        }
        stockExecuteHelper.createExecuteStockRecords(purchaseIn, executeItems);
        stockExecuteHelper.recalculatePurchaseInStockSummary(purchaseIn.getId(), userId);
        updatePurchaseOrderInCount(purchaseIn.getOrderId());
    }

    @Override
    public void updatePurchaseInPaymentPrice(Long id, BigDecimal paymentPrice) {
        ErpPurchaseInDO purchaseIn = erpPurchaseInMapper.selectById(id);
        if (purchaseIn.getPaymentPrice().equals(paymentPrice)) {
            return;
        }
        if (paymentPrice.compareTo(purchaseIn.getTotalPrice()) > 0) {
            throw exception(PURCHASE_IN_FAIL_PAYMENT_PRICE_EXCEED, paymentPrice, purchaseIn.getTotalPrice());
        }
        erpPurchaseInMapper.updateById(new ErpPurchaseInDO().setId(id).setPaymentPrice(paymentPrice));
    }

    // endregion

    // region 查询方法（委托给 QueryHelper）

    @Override
    public ErpPurchaseInDO getPurchaseIn(Long id) {
        return queryHelper.getPurchaseIn(id);
    }

    @Override
    public ErpPurchaseInDO validatePurchaseIn(Long id) {
        return queryHelper.validatePurchaseIn(id);
    }

    @Override
    public PageResult<ErpPurchaseInDO> getPurchaseInPage(ErpPurchaseInPageReqVO pageReqVO) {
        return queryHelper.getPurchaseInPage(pageReqVO);
    }

    @Override
    public List<ErpPurchaseInDO> getPurchaseInListByIds(Collection<Long> ids) {
        return queryHelper.getPurchaseInListByIds(ids);
    }

    @Override
    public List<ErpPurchaseInDO> getPurchaseInListByOrderIds(Collection<Long> orderIds) {
        return queryHelper.getPurchaseInListByOrderIds(orderIds);
    }

    @Override
    public List<ErpPurchaseInItemDO> getPurchaseInItemListByInId(Long inId) {
        return queryHelper.getPurchaseInItemListByInId(inId);
    }

    @Override
    public List<ErpPurchaseInItemDO> getPurchaseInItemListByInIds(Collection<Long> inIds) {
        return queryHelper.getPurchaseInItemListByInIds(inIds);
    }

    @Override
    public List<ErpPurchaseInStockExecuteDO> getPurchaseInStockExecuteListByPurchaseInId(Long purchaseInId) {
        return queryHelper.getPurchaseInStockExecuteListByPurchaseInId(purchaseInId);
    }

    @Override
    public List<ErpPurchaseInStockExecuteDO> getPurchaseInStockExecuteListByIds(Collection<Long> ids) {
        return queryHelper.getPurchaseInStockExecuteListByIds(ids);
    }

    @Override
    public List<ErpPurchaseInStockExecuteItemDO> getPurchaseInStockExecuteItemListByExecuteIds(Collection<Long> executeIds) {
        return queryHelper.getPurchaseInStockExecuteItemListByExecuteIds(executeIds);
    }

    @Override
    public List<ErpPurchaseInStockExecuteItemDO> getPurchaseInStockExecuteItemListByIds(Collection<Long> ids) {
        return queryHelper.getPurchaseInStockExecuteItemListByIds(ids);
    }

    @Override
    public List<ErpPurchaseInStockExecuteItemBatchDO> getPurchaseInStockExecuteItemBatchListByExecuteItemIds(Collection<Long> executeItemIds) {
        return queryHelper.getPurchaseInStockExecuteItemBatchListByExecuteItemIds(executeItemIds);
    }

    @Override
    public List<ErpPurchaseInStockExecuteItemBatchDO> getPurchaseInStockExecuteItemBatchListByPurchaseSourceBatchId(Long purchaseSourceBatchId) {
        return queryHelper.getPurchaseInStockExecuteItemBatchListByPurchaseSourceBatchId(purchaseSourceBatchId);
    }

    // endregion

    // region 内部方法

    private void calculateTotalPrice(ErpPurchaseInDO purchaseIn, List<ErpPurchaseInItemDO> purchaseInItems) {
        purchaseIn.setTotalCount(getSumValue(purchaseInItems, ErpPurchaseInItemDO::getCount, BigDecimal::add));
        purchaseIn.setTotalProductPrice(getSumValue(purchaseInItems, ErpPurchaseInItemDO::getTotalPrice, BigDecimal::add, BigDecimal.ZERO));
        purchaseIn.setTotalTaxPrice(getSumValue(purchaseInItems, ErpPurchaseInItemDO::getTaxPrice, BigDecimal::add, BigDecimal.ZERO));
        purchaseIn.setTotalPrice(purchaseIn.getTotalProductPrice().add(purchaseIn.getTotalTaxPrice()));
        if (purchaseIn.getDiscountPercent() == null) {
            purchaseIn.setDiscountPercent(BigDecimal.ZERO);
        }
        purchaseIn.setDiscountPrice(MoneyUtils.priceMultiplyPercent(purchaseIn.getTotalPrice(), purchaseIn.getDiscountPercent()));
        purchaseIn.setTotalPrice(purchaseIn.getTotalPrice().subtract(purchaseIn.getDiscountPrice()).add(purchaseIn.getOtherPrice()));
    }

    private void updatePurchaseOrderInCount(Long orderId) {
        List<ErpPurchaseInDO> purchaseIns = erpPurchaseInMapper.selectApprovedListByOrderId(orderId);
        if (CollUtil.isEmpty(purchaseIns)) {
            purchaseOrderService.updatePurchaseOrderInCount(orderId, Collections.emptyMap());
            return;
        }
        Map<Long, ErpPurchaseInDO> purchaseInMap = convertMap(purchaseIns, ErpPurchaseInDO::getId);
        List<ErpPurchaseInItemDO> purchaseInItems = erpPurchaseInItemMapper.selectListByInIds(
                convertList(purchaseIns, ErpPurchaseInDO::getId));
        Map<Long, BigDecimal> returnCountMap = new HashMap<>();
        purchaseInItems.forEach(item -> returnCountMap.merge(item.getOrderItemId(),
                resolveQualifiedCount(item, purchaseInMap.get(item.getInId())), BigDecimal::add));
        purchaseOrderService.updatePurchaseOrderInCount(orderId, returnCountMap);
    }

    private BigDecimal resolveQualifiedCount(ErpPurchaseInItemDO item, ErpPurchaseInDO purchaseIn) {
        if (purchaseIn == null || purchaseIn.getQaStatus() == null) {
            return BigDecimal.ZERO;
        }
        if (ErpQaStatusEnum.TO_INSPECT.getStatus().equals(purchaseIn.getQaStatus())) {
            return BigDecimal.ZERO;
        }
        return ObjectUtil.defaultIfNull(item.getStockInCount(), BigDecimal.ZERO);
    }

    private void validatePurchaseInCanStockExecute(ErpPurchaseInDO purchaseIn) {
        if (!ErpAuditStatus.APPROVE.getStatus().equals(purchaseIn.getStatus())) {
            throw exception(PURCHASE_IN_CONFIRM_STOCK_IN_FAIL_STATUS);
        }
        if (!queryHelper.canConfirmStockInByQaStatus(purchaseIn.getQaStatus())) {
            throw exception(PURCHASE_IN_CONFIRM_STOCK_IN_FAIL_QA_STATUS);
        }
        if (!queryHelper.canExecuteStockInByStatus(purchaseIn.getStockInStatus())) {
            throw exception(PURCHASE_IN_CONFIRM_STOCK_IN_FAIL_STOCK_STATUS);
        }
    }

    private LocalDateTime defaultTime(LocalDateTime... values) {
        for (LocalDateTime value : values) {
            if (value != null) {
                return value;
            }
        }
        return LocalDateTime.now();
    }

    private List<ErpPurchaseInItemDO> validatePurchaseInItems(List<ErpPurchaseInSaveReqVO.Item> list) {
        List<ErpProductDO> productList = productService.validProductList(
                convertSet(list, ErpPurchaseInSaveReqVO.Item::getProductId));
        Map<Long, ErpProductDO> productMap = convertMap(productList, ErpProductDO::getId);
        return convertList(list, o -> BeanUtils.toBean(o, ErpPurchaseInItemDO.class, item -> {
            item.setProductUnitId(productMap.get(item.getProductId()).getUnitId());
            BigDecimal materialTotalPrice = MoneyUtils.priceMultiply(item.getProductPrice(), item.getCount());
            BigDecimal engineeringFee = ObjectUtil.defaultIfNull(item.getEngineeringFee(), BigDecimal.ZERO);
            if (materialTotalPrice == null) {
                if (engineeringFee.compareTo(BigDecimal.ZERO) == 0) {
                    return;
                }
                item.setTotalPrice(engineeringFee);
            } else {
                item.setTotalPrice(materialTotalPrice.add(engineeringFee));
            }
            if (item.getTaxPercent() != null) {
                item.setTaxPrice(MoneyUtils.priceMultiplyPercent(item.getTotalPrice(), item.getTaxPercent()));
            }
        }));
    }

    private void validatePurchaseOrderItemRemainingCount(Long orderId, List<ErpPurchaseInItemDO> purchaseInItems) {
        List<ErpPurchaseOrderItemDO> orderItems = purchaseOrderService.getPurchaseOrderItemListByOrderId(orderId);
        Map<Long, ErpPurchaseOrderItemDO> orderItemMap = convertMap(orderItems, ErpPurchaseOrderItemDO::getId);
        Map<Long, ErpPurchaseSourceBatchDO> sourceBatchMap = purchaseSourceBatchService.getPurchaseSourceBatchMap(
                purchaseInItems.stream()
                        .map(ErpPurchaseInItemDO::getPurchaseSourceBatchId)
                        .filter(java.util.Objects::nonNull)
                        .collect(java.util.stream.Collectors.toSet()));
        Map<Long, BigDecimal> requestCountMap = new HashMap<>();
        purchaseInItems.forEach(item -> {
            ErpPurchaseOrderItemDO orderItem = orderItemMap.get(item.getOrderItemId());
            if (orderItem == null || ObjectUtil.notEqual(orderItem.getProductId(), item.getProductId())) {
                throw exception(PURCHASE_IN_ITEM_ORDER_MISMATCH, item.getOrderItemId());
            }
            if (item.getPurchaseSourceBatchId() != null) {
                ErpPurchaseSourceBatchDO sourceBatch = sourceBatchMap.get(item.getPurchaseSourceBatchId());
                if (sourceBatch == null) {
                    throw exception(PURCHASE_SOURCE_BATCH_NOT_EXISTS);
                }
                if (ObjectUtil.notEqual(sourceBatch.getPurchaseOrderItemId(), item.getOrderItemId())) {
                    throw exception(PURCHASE_SOURCE_BATCH_ORDER_ITEM_MISMATCH);
                }
                if (ObjectUtil.notEqual(sourceBatch.getProductId(), item.getProductId())) {
                    throw exception(PURCHASE_SOURCE_BATCH_PRODUCT_MISMATCH);
                }
            }
            requestCountMap.merge(item.getOrderItemId(),
                    ObjectUtil.defaultIfNull(item.getCount(), BigDecimal.ZERO), BigDecimal::add);
        });
        requestCountMap.forEach((orderItemId, requestCount) -> {
            ErpPurchaseOrderItemDO orderItem = orderItemMap.get(orderItemId);
            BigDecimal totalCount = ObjectUtil.defaultIfNull(orderItem.getCount(), BigDecimal.ZERO);
            BigDecimal inCount = ObjectUtil.defaultIfNull(orderItem.getInCount(), BigDecimal.ZERO);
            BigDecimal remainingCount = totalCount.subtract(inCount);
            if (remainingCount.compareTo(BigDecimal.ZERO) < 0) {
                remainingCount = BigDecimal.ZERO;
            }
            if (requestCount.compareTo(remainingCount) > 0) {
                throw exception(PURCHASE_IN_ITEM_COUNT_EXCEED_REMAINING, orderItemId, remainingCount);
            }
        });
    }

    private void updatePurchaseInItemList(Long id, List<ErpPurchaseInItemDO> newList) {
        List<ErpPurchaseInItemDO> oldList = erpPurchaseInItemMapper.selectListByInId(id);
        List<List<ErpPurchaseInItemDO>> diffList = diffList(oldList, newList,
                (oldVal, newVal) -> oldVal.getId().equals(newVal.getId()));

        if (CollUtil.isNotEmpty(diffList.get(0))) {
            diffList.get(0).forEach(o -> o.setInId(id));
            erpPurchaseInItemMapper.insertBatch(diffList.get(0));
        }
        if (CollUtil.isNotEmpty(diffList.get(1))) {
            erpPurchaseInItemMapper.updateBatch(diffList.get(1));
        }
        if (CollUtil.isNotEmpty(diffList.get(2))) {
            erpPurchaseInItemMapper.deleteByIds(convertList(diffList.get(2), ErpPurchaseInItemDO::getId));
        }
    }

    private String normalizeBatchFieldKey(String fieldKey) {
        if (!BATCH_FIELD_ACCOUNT_ID.equals(fieldKey)
                && !BATCH_FIELD_IN_TIME.equals(fieldKey)
                && !BATCH_FIELD_REMARK.equals(fieldKey)) {
            throw exception(PURCHASE_IN_BATCH_UPDATE_FIELD_NOT_SUPPORT, fieldKey);
        }
        return fieldKey;
    }

    private Object parseBatchValue(String fieldKey, String value) {
        try {
            if (BATCH_FIELD_ACCOUNT_ID.equals(fieldKey)) {
                Long accountId = Long.valueOf(value);
                accountService.validateAccount(accountId);
                return accountId;
            }
            if (BATCH_FIELD_IN_TIME.equals(fieldKey)) {
                return LocalDateTime.parse(value, BATCH_IN_TIME_FORMATTER);
            }
            return value;
        } catch (Exception ex) {
            throw exception(PURCHASE_IN_BATCH_UPDATE_FIELD_VALUE_INVALID, fieldKey, value);
        }
    }

    private void applyBatchValue(ErpPurchaseInDO updateObj, String fieldKey, Object value) {
        if (BATCH_FIELD_ACCOUNT_ID.equals(fieldKey)) {
            updateObj.setAccountId((Long) value);
            return;
        }
        if (BATCH_FIELD_IN_TIME.equals(fieldKey)) {
            updateObj.setInTime((LocalDateTime) value);
            return;
        }
        if (BATCH_FIELD_REMARK.equals(fieldKey)) {
            updateObj.setRemark((String) value);
        }
    }

    // endregion
}
