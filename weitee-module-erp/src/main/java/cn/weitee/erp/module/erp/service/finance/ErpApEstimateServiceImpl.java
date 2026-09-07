package cn.weitee.erp.module.erp.service.finance;

import cn.hutool.core.collection.CollUtil;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.apestimate.ErpApEstimateActionReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.apestimate.ErpApEstimatePageReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.apestimate.ErpApEstimateScanReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpApEstimateDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpApEstimateItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpApStatementDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseInDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseInItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseOrderDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseOrderItemDO;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpApEstimateItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpApEstimateMapper;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpApStatementMapper;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseInItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseInMapper;
import cn.weitee.erp.module.erp.enums.ErpApEstimateStatusEnum;
import cn.weitee.erp.module.erp.enums.ErpApEstimateReverseTypeEnum;
import cn.weitee.erp.module.erp.enums.ErpApInvoiceStatusEnum;
import cn.weitee.erp.module.erp.enums.ErpApStatementStatusEnum;
import cn.weitee.erp.module.erp.enums.common.ErpBizTypeEnum;
import cn.weitee.erp.module.erp.service.purchase.ErpPurchaseOrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertList;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertMultiMap;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.weitee.erp.framework.common.util.object.ObjectUtils.defaultIfNull;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.AP_ESTIMATE_CONFIRM_FAIL_STATUS;

@Service
@Validated
public class ErpApEstimateServiceImpl implements ErpApEstimateService {

    private static final String DEFAULT_CURRENCY_CODE = "CNY";
    private static final BigDecimal ZERO = BigDecimal.ZERO;

    @Resource
    private ErpApStatementMapper erpApStatementMapper;
    @Resource
    private ErpApEstimateMapper erpApEstimateMapper;
    @Resource
    private ErpApEstimateItemMapper erpApEstimateItemMapper;
    @Resource
    private ErpPurchaseInMapper erpPurchaseInMapper;
    @Resource
    private ErpPurchaseInItemMapper erpPurchaseInItemMapper;
    @Resource
    private ErpPurchaseOrderService purchaseOrderService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int generateMonthEstimate(ErpApEstimateScanReqVO reqVO) {
        YearMonth estimateMonth = YearMonth.parse(reqVO.getEstimateMonth());
        LocalDateTime endTime = estimateMonth.atEndOfMonth().atTime(LocalTime.MAX);

        List<ErpApStatementDO> statements = erpApStatementMapper.selectList(new LambdaQueryWrapperX<ErpApStatementDO>()
                .eq(ErpApStatementDO::getBizType, ErpBizTypeEnum.PURCHASE_IN.getType())
                .eq(ErpApStatementDO::getInvoiceStatus, ErpApInvoiceStatusEnum.NONE.getStatus())
                .ne(ErpApStatementDO::getStatus, ErpApStatementStatusEnum.CLOSED.getStatus())
                .le(ErpApStatementDO::getBizDate, endTime)
                .orderByAsc(ErpApStatementDO::getId));
        if (CollUtil.isEmpty(statements)) {
            return 0;
        }

        List<Long> purchaseInIds = convertList(statements, ErpApStatementDO::getBizId);
        List<ErpPurchaseInDO> purchaseInList = erpPurchaseInMapper.selectBatchIds(purchaseInIds);
        if (CollUtil.isEmpty(purchaseInList)) {
            return 0;
        }
        Map<Long, ErpPurchaseInDO> purchaseInMap = convertMap(purchaseInList, ErpPurchaseInDO::getId);

        List<Long> orderIds = new ArrayList<>(convertList(purchaseInList, ErpPurchaseInDO::getOrderId));
        orderIds.removeIf(item -> item == null);
        List<ErpPurchaseOrderDO> purchaseOrderList = orderIds.isEmpty()
                ? Collections.emptyList()
                : purchaseOrderService.getPurchaseOrderList(orderIds);
        Map<Long, ErpPurchaseOrderDO> purchaseOrderMap = convertMap(purchaseOrderList, ErpPurchaseOrderDO::getId);

        List<ErpPurchaseInItemDO> purchaseInItems = erpPurchaseInItemMapper.selectList(new LambdaQueryWrapperX<ErpPurchaseInItemDO>()
                .in(ErpPurchaseInItemDO::getInId, purchaseInIds)
                .orderByAsc(ErpPurchaseInItemDO::getId));
        Map<Long, List<ErpPurchaseInItemDO>> purchaseInItemMap = convertMultiMap(purchaseInItems, ErpPurchaseInItemDO::getInId);
        List<ErpPurchaseOrderItemDO> purchaseOrderItems = orderIds.isEmpty()
                ? Collections.emptyList()
                : purchaseOrderService.getPurchaseOrderItemListByOrderIds(orderIds);
        Map<Long, List<ErpPurchaseOrderItemDO>> purchaseOrderItemMap = convertMultiMap(purchaseOrderItems, ErpPurchaseOrderItemDO::getOrderId);

        int generatedCount = 0;
        for (ErpApStatementDO statement : statements) {
            ErpPurchaseInDO purchaseIn = purchaseInMap.get(statement.getBizId());
            if (purchaseIn == null) {
                continue;
            }
            if (erpApEstimateMapper.selectBySourceBizTypeAndSourceBizId(statement.getBizType(), statement.getBizId()) != null) {
                continue;
            }
            createEstimateByPurchaseIn(
                    statement,
                    purchaseIn,
                    purchaseInItemMap.getOrDefault(purchaseIn.getId(), Collections.emptyList()),
                    purchaseOrderItemMap.getOrDefault(purchaseIn.getOrderId(), Collections.emptyList()),
                    purchaseOrderMap.get(purchaseIn.getOrderId()),
                    reqVO.getEstimateMonth());
            generatedCount++;
        }
        return generatedCount;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmApEstimate(Long userId, ErpApEstimateActionReqVO reqVO) {
        List<ErpApEstimateDO> estimateList = erpApEstimateMapper.selectBatchIds(reqVO.getIds());
        if (CollUtil.isEmpty(estimateList)) {
            return;
        }
        estimateList.forEach(estimate -> {
            if (!ErpApEstimateStatusEnum.GENERATED.getStatus().equals(estimate.getStatus())) {
                throw exception(AP_ESTIMATE_CONFIRM_FAIL_STATUS, estimate.getEstimateNo());
            }
        });
        estimateList.forEach(estimate -> erpApEstimateMapper.updateById(new ErpApEstimateDO()
                .setId(estimate.getId())
                .setStatus(ErpApEstimateStatusEnum.CONFIRMED.getStatus())
                .setConfirmUserId(userId)
                .setConfirmTime(LocalDateTime.now())));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reverseApEstimate(Long userId, ErpApEstimateActionReqVO reqVO) {
        List<ErpApEstimateDO> estimateList = erpApEstimateMapper.selectBatchIds(reqVO.getIds());
        if (CollUtil.isEmpty(estimateList)) {
            return;
        }
        estimateList.forEach(estimate -> {
            if (ErpApEstimateStatusEnum.REVERSED.getStatus().equals(estimate.getStatus())) {
                return;
            }
            erpApEstimateMapper.updateReverseInfoById(estimate.getId(),
                    ErpApEstimateStatusEnum.REVERSED.getStatus(),
                    userId,
                    LocalDateTime.now(),
                    ErpApEstimateReverseTypeEnum.MANUAL.getStatus(),
                    null,
                    null,
                    reqVO.getRemark());
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reverseBySourceBiz(Integer sourceBizType, Long sourceBizId, Long userId, String remark) {
        reverseBySourceBiz(sourceBizType, sourceBizId, userId,
                ErpApEstimateReverseTypeEnum.MANUAL.getStatus(), null, null, remark);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reverseBySourceBiz(Integer sourceBizType, Long sourceBizId, Long userId, Integer reverseType,
                                   Long reverseSourceId, String reverseSourceNo, String remark) {
        ErpApEstimateDO estimate = erpApEstimateMapper.selectBySourceBizTypeAndSourceBizId(sourceBizType, sourceBizId);
        if (estimate == null || ErpApEstimateStatusEnum.REVERSED.getStatus().equals(estimate.getStatus())) {
            return;
        }
        erpApEstimateMapper.updateReverseInfoById(estimate.getId(),
                ErpApEstimateStatusEnum.REVERSED.getStatus(),
                userId,
                LocalDateTime.now(),
                reverseType,
                reverseSourceId,
                reverseSourceNo,
                remark);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restoreByStatementReopen(Integer sourceBizType, Long sourceBizId) {
        ErpApEstimateDO estimate = erpApEstimateMapper.selectBySourceBizTypeAndSourceBizId(sourceBizType, sourceBizId);
        if (estimate == null
                || !ErpApEstimateStatusEnum.REVERSED.getStatus().equals(estimate.getStatus())
                || !ErpApEstimateReverseTypeEnum.STATEMENT_CLOSED.getStatus().equals(estimate.getReverseType())) {
            return;
        }
        Integer restoreStatus = estimate.getConfirmTime() != null
                ? ErpApEstimateStatusEnum.CONFIRMED.getStatus()
                : ErpApEstimateStatusEnum.GENERATED.getStatus();
        erpApEstimateMapper.restoreOpenStatusById(estimate.getId(), restoreStatus);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncByStatementInvoiceChange(ErpApStatementDO statement, Integer oldInvoiceStatus, Integer newInvoiceStatus,
                                             Long userId, Long reverseSourceId, String reverseSourceNo) {
        if (statement == null
                || !ErpBizTypeEnum.PURCHASE_IN.getType().equals(statement.getBizType())
                || statement.getBizId() == null) {
            return;
        }
        Integer beforeStatus = defaultIfNull(oldInvoiceStatus, ErpApInvoiceStatusEnum.NONE.getStatus());
        Integer afterStatus = defaultIfNull(newInvoiceStatus, ErpApInvoiceStatusEnum.NONE.getStatus());
        if (ErpApInvoiceStatusEnum.NONE.getStatus().equals(beforeStatus)
                && !ErpApInvoiceStatusEnum.NONE.getStatus().equals(afterStatus)) {
            reverseBySourceBiz(statement.getBizType(), statement.getBizId(), userId,
                    ErpApEstimateReverseTypeEnum.INVOICE.getStatus(),
                    reverseSourceId,
                    reverseSourceNo,
                    "收票联动自动冲回暂估");
            return;
        }
        if (!ErpApInvoiceStatusEnum.NONE.getStatus().equals(beforeStatus)
                && ErpApInvoiceStatusEnum.NONE.getStatus().equals(afterStatus)
                && !ErpApStatementStatusEnum.CLOSED.getStatus().equals(statement.getStatus())) {
            restoreBySourceBizIfNeeded(statement.getBizType(), statement.getBizId());
        }
    }

    @Override
    public ErpApEstimateDO getApEstimate(Long id) {
        return erpApEstimateMapper.selectById(id);
    }

    @Override
    public List<ErpApEstimateDO> getApEstimateListByIds(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return erpApEstimateMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<ErpApEstimateDO> getApEstimatePage(ErpApEstimatePageReqVO pageReqVO) {
        return erpApEstimateMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ErpApEstimateItemDO> getApEstimateItemListByEstimateId(Long estimateId) {
        return erpApEstimateItemMapper.selectListByEstimateId(estimateId);
    }

    @Override
    public List<ErpApEstimateItemDO> getApEstimateItemListByEstimateIds(Collection<Long> estimateIds) {
        return erpApEstimateItemMapper.selectListByEstimateIds(estimateIds);
    }

    private void createEstimateByPurchaseIn(ErpApStatementDO statement, ErpPurchaseInDO purchaseIn,
                                            List<ErpPurchaseInItemDO> purchaseInItems,
                                            List<ErpPurchaseOrderItemDO> purchaseOrderItems,
                                            ErpPurchaseOrderDO purchaseOrder,
                                            String estimateMonth) {
        BigDecimal sourceAmount = defaultAmount(purchaseIn.getTotalProductPrice());
        BigDecimal estimateAmount = sourceAmount
                .subtract(defaultAmount(purchaseIn.getDiscountPrice()))
                .add(defaultAmount(purchaseIn.getOtherPrice()));
        List<BigDecimal> itemSourceAmountList = purchaseInItems.stream()
                .map(item -> defaultAmount(item.getTotalPrice()))
                .collect(Collectors.toList());
        List<BigDecimal> itemEstimateAmountList = allocateAmountList(itemSourceAmountList, estimateAmount);
        Map<Long, ErpPurchaseOrderItemDO> purchaseOrderItemMap = convertMap(purchaseOrderItems, ErpPurchaseOrderItemDO::getId);

        ErpApEstimateDO estimate = new ErpApEstimateDO()
                .setEstimateNo(buildEstimateNo(purchaseIn.getNo()))
                .setEstimateMonth(estimateMonth)
                .setSourceBizType(statement.getBizType())
                .setSourceBizId(statement.getBizId())
                .setSourceBizNo(statement.getBizNo())
                .setSourcePurchaseInId(purchaseIn.getId())
                .setSourcePurchaseInNo(purchaseIn.getNo())
                .setSourceOrderId(purchaseOrder != null ? purchaseOrder.getId() : purchaseIn.getOrderId())
                .setSourceOrderNo(purchaseOrder != null ? purchaseOrder.getNo() : null)
                .setSupplierId(purchaseIn.getSupplierId())
                .setAccountId(purchaseIn.getAccountId())
                .setCurrencyCode(DEFAULT_CURRENCY_CODE)
                .setSourceAmount(sourceAmount)
                .setAmount(estimateAmount)
                .setStatus(ErpApEstimateStatusEnum.GENERATED.getStatus())
                .setRemark(purchaseIn.getRemark());
        erpApEstimateMapper.insert(estimate);

        List<ErpApEstimateItemDO> estimateItemList = new ArrayList<>(purchaseInItems.size());
        for (int i = 0; i < purchaseInItems.size(); i++) {
            ErpPurchaseInItemDO purchaseInItem = purchaseInItems.get(i);
            ErpPurchaseOrderItemDO orderItem = purchaseOrderItemMap.get(purchaseInItem.getOrderItemId());
            estimateItemList.add(new ErpApEstimateItemDO()
                    .setEstimateId(estimate.getId())
                    .setSourcePurchaseInItemId(purchaseInItem.getId())
                    .setSourcePurchaseInId(purchaseIn.getId())
                    .setSourcePurchaseInNo(purchaseIn.getNo())
                    .setSourceOrderId(orderItem != null ? orderItem.getOrderId() : purchaseIn.getOrderId())
                    .setSourceOrderItemId(purchaseInItem.getOrderItemId())
                    .setSourceOrderNo(purchaseOrder != null ? purchaseOrder.getNo() : null)
                    .setProductId(purchaseInItem.getProductId())
                    .setWarehouseId(purchaseInItem.getWarehouseId())
                    .setProjectId(orderItem != null ? orderItem.getProjectId() : null)
                    .setCount(purchaseInItem.getCount())
                    .setSourceAmount(defaultAmount(purchaseInItem.getTotalPrice()))
                    .setTaxAmount(defaultAmount(purchaseInItem.getTaxPrice()))
                    .setAmount(itemEstimateAmountList.get(i))
                    .setRemark(purchaseInItem.getRemark()));
        }
        if (CollUtil.isNotEmpty(estimateItemList)) {
            erpApEstimateItemMapper.insertBatch(estimateItemList);
        }
    }

    private List<BigDecimal> allocateAmountList(List<BigDecimal> sourceAmountList, BigDecimal totalAmount) {
        if (CollUtil.isEmpty(sourceAmountList)) {
            return Collections.emptyList();
        }
        BigDecimal totalSourceAmount = sourceAmountList.stream().reduce(ZERO, BigDecimal::add);
        List<BigDecimal> result = new ArrayList<>(sourceAmountList.size());
        BigDecimal remain = totalAmount;
        if (totalSourceAmount.compareTo(ZERO) == 0) {
            BigDecimal equalAmount = totalAmount.divide(BigDecimal.valueOf(sourceAmountList.size()), 6, RoundingMode.HALF_UP);
            for (int i = 0; i < sourceAmountList.size(); i++) {
                if (i == sourceAmountList.size() - 1) {
                    result.add(remain);
                } else {
                    result.add(equalAmount);
                    remain = remain.subtract(equalAmount);
                }
            }
            return result;
        }

        for (int i = 0; i < sourceAmountList.size(); i++) {
            BigDecimal sourceAmount = sourceAmountList.get(i);
            if (i == sourceAmountList.size() - 1) {
                result.add(remain);
            } else {
                BigDecimal amount = totalAmount.multiply(sourceAmount)
                        .divide(totalSourceAmount, 6, RoundingMode.HALF_UP);
                result.add(amount);
                remain = remain.subtract(amount);
            }
        }
        return result;
    }

    private BigDecimal defaultAmount(BigDecimal amount) {
        return amount == null ? ZERO : amount;
    }

    private void restoreBySourceBizIfNeeded(Integer sourceBizType, Long sourceBizId) {
        ErpApEstimateDO estimate = erpApEstimateMapper.selectBySourceBizTypeAndSourceBizId(sourceBizType, sourceBizId);
        if (estimate == null
                || !ErpApEstimateStatusEnum.REVERSED.getStatus().equals(estimate.getStatus())
                || !ErpApEstimateReverseTypeEnum.INVOICE.getStatus().equals(estimate.getReverseType())) {
            return;
        }
        Integer restoreStatus = estimate.getConfirmTime() != null
                ? ErpApEstimateStatusEnum.CONFIRMED.getStatus()
                : ErpApEstimateStatusEnum.GENERATED.getStatus();
        erpApEstimateMapper.restoreOpenStatusById(estimate.getId(), restoreStatus);
    }

    private String buildEstimateNo(String sourceNo) {
        return "ZG-" + sourceNo;
    }

}
