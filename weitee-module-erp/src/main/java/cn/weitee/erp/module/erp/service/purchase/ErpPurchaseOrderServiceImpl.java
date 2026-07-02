package cn.weitee.erp.module.erp.service.purchase;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.collection.CollectionUtils;
import cn.weitee.erp.framework.common.util.number.MoneyUtils;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.module.erp.controller.admin.purchase.vo.order.ErpPurchaseOrderBatchUpdateReqVO;
import cn.weitee.erp.module.erp.controller.admin.purchase.vo.order.ErpPurchaseOrderBatchUpdateResultVO;
import cn.weitee.erp.module.erp.controller.admin.purchase.vo.order.ErpPurchaseOrderPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.purchase.vo.order.ErpPurchaseOrderSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseInDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseOrderAuditLogDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseOrderDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseOrderItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseOrderRejectLogDO;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseInMapper;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseOrderAuditLogMapper;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseOrderItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseOrderMapper;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseOrderRejectLogMapper;
import cn.weitee.erp.module.erp.dal.redis.no.ErpNoRedisDAO;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.enums.ErpPurchaseOrderAuditActionTypeConstants;
import cn.weitee.erp.module.erp.framework.event.PurchaseOrderChangedEvent;
import cn.weitee.erp.module.erp.service.finance.ErpAccountService;
import cn.weitee.erp.module.erp.service.product.ErpProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.*;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.*;

// TODO 芋艿：记录操作日志

/**
 * ERP 采购订单 Service 实现类
 *
 * @author WeTai
 */
@Service
@Validated
@Slf4j
public class ErpPurchaseOrderServiceImpl implements ErpPurchaseOrderService {

    private static final String BATCH_EDIT_MODE_OVERWRITE = "overwrite";
    private static final DateTimeFormatter BATCH_ORDER_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Resource
    private ErpPurchaseOrderMapper erpPurchaseOrderMapper;
    @Resource
    private ErpPurchaseOrderItemMapper erpPurchaseOrderItemMapper;
    @Resource
    private ErpPurchaseOrderAuditLogMapper erpPurchaseOrderAuditLogMapper;
    @Resource
    private ErpPurchaseOrderRejectLogMapper erpPurchaseOrderRejectLogMapper;
    @Resource
    private ErpPurchaseInMapper erpPurchaseInMapper;

    @Resource
    private ErpNoRedisDAO noRedisDAO;

    @Resource
    private ErpProductService productService;
    @Resource
    private ErpSupplierService supplierService;
    @Resource
    private ErpAccountService accountService;
    @Resource
    private ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPurchaseOrder(ErpPurchaseOrderSaveReqVO createReqVO) {
        // 1.1 校验订单项的有效性
        List<ErpPurchaseOrderItemDO> purchaseOrderItems = validatePurchaseOrderItems(createReqVO.getItems());
        // 1.2 校验供应商
        supplierService.validateSupplier(createReqVO.getSupplierId());
        // 1.3 校验结算账户
        if (createReqVO.getAccountId() != null) {
            accountService.validateAccount(createReqVO.getAccountId());
        }
        // 1.4 生成订单号，并校验唯一性
        String no = noRedisDAO.generate(ErpNoRedisDAO.PURCHASE_ORDER_NO_PREFIX);
        if (erpPurchaseOrderMapper.selectByNo(no) != null) {
            throw exception(PURCHASE_ORDER_NO_EXISTS);
        }

        // 2.1 插入订单
        ErpPurchaseOrderDO purchaseOrder = BeanUtils.toBean(createReqVO, ErpPurchaseOrderDO.class, in -> in
                .setNo(no).setStatus(ErpAuditStatus.DRAFT.getStatus()));
        calculateTotalPrice(purchaseOrder, purchaseOrderItems);
        erpPurchaseOrderMapper.insert(purchaseOrder);
        // 2.2 插入订单项
        purchaseOrderItems.forEach(o -> o.setOrderId(purchaseOrder.getId()));
        erpPurchaseOrderItemMapper.insertBatch(purchaseOrderItems);
        return purchaseOrder.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePurchaseOrder(ErpPurchaseOrderSaveReqVO updateReqVO) {
        // 1.1 校验存在
        ErpPurchaseOrderDO purchaseOrder = validatePurchaseOrderExists(updateReqVO.getId());
        if (ErpAuditStatus.APPROVE.getStatus().equals(purchaseOrder.getStatus())) {
            throw exception(PURCHASE_ORDER_UPDATE_FAIL_APPROVE, purchaseOrder.getNo());
        }
        if (isApprovalRunning(purchaseOrder)) {
            throw exception(PURCHASE_ORDER_UPDATE_FAIL_PROCESSING, purchaseOrder.getNo());
        }
        // 1.2 校验供应商
        supplierService.validateSupplier(updateReqVO.getSupplierId());
        // 1.3 校验结算账户
        if (updateReqVO.getAccountId() != null) {
            accountService.validateAccount(updateReqVO.getAccountId());
        }
        // 1.4 校验订单项的有效性
        List<ErpPurchaseOrderItemDO> purchaseOrderItems = validatePurchaseOrderItems(updateReqVO.getItems());

        // 2.1 更新订单
        ErpPurchaseOrderDO updateObj = BeanUtils.toBean(updateReqVO, ErpPurchaseOrderDO.class);
        calculateTotalPrice(updateObj, purchaseOrderItems);
        erpPurchaseOrderMapper.updateById(updateObj);
        // 2.2 更新订单项
        updatePurchaseOrderItemList(updateReqVO.getId(), purchaseOrderItems);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ErpPurchaseOrderBatchUpdateResultVO updatePurchaseOrderBatch(ErpPurchaseOrderBatchUpdateReqVO reqVO) {
        List<Long> uniqueIds = List.copyOf(new LinkedHashSet<>(reqVO.getIds()));
        List<ErpPurchaseOrderDO> purchaseOrders = erpPurchaseOrderMapper.selectByIds(uniqueIds);
        Map<Long, ErpPurchaseOrderDO> purchaseOrderMap = CollectionUtils.convertMap(purchaseOrders, ErpPurchaseOrderDO::getId);
        for (Long id : uniqueIds) {
            ErpPurchaseOrderDO purchaseOrder = purchaseOrderMap.get(id);
            if (purchaseOrder == null) {
                throw exception(PURCHASE_ORDER_NOT_EXISTS);
            }
            validatePurchaseOrderEditable(purchaseOrder);
        }

        PurchaseOrderBatchFieldUpdater updater = createBatchUpdater(reqVO);
        uniqueIds.forEach(id -> {
            ErpPurchaseOrderDO updateObj = new ErpPurchaseOrderDO().setId(id);
            updater.apply(updateObj);
            erpPurchaseOrderMapper.updateById(updateObj);
        });

        ErpPurchaseOrderBatchUpdateResultVO result = new ErpPurchaseOrderBatchUpdateResultVO();
        result.setSuccessCount(uniqueIds.size());
        result.setFailureCount(0);
        result.setUpdatedIds(uniqueIds);
        result.setFailedItems(Collections.emptyList());
        return result;
    }

    private void calculateTotalPrice(ErpPurchaseOrderDO purchaseOrder, List<ErpPurchaseOrderItemDO> purchaseOrderItems) {
        purchaseOrder.setTotalCount(getSumValue(purchaseOrderItems, ErpPurchaseOrderItemDO::getCount, BigDecimal::add));
        purchaseOrder.setTotalProductPrice(getSumValue(purchaseOrderItems, ErpPurchaseOrderItemDO::getTotalPrice, BigDecimal::add, BigDecimal.ZERO));
        purchaseOrder.setTotalTaxPrice(getSumValue(purchaseOrderItems, ErpPurchaseOrderItemDO::getTaxPrice, BigDecimal::add, BigDecimal.ZERO));
        purchaseOrder.setTotalPrice(purchaseOrder.getTotalProductPrice().add(purchaseOrder.getTotalTaxPrice()));
        // 计算优惠价格
        if (purchaseOrder.getDiscountPercent() == null) {
            purchaseOrder.setDiscountPercent(BigDecimal.ZERO);
        }
        purchaseOrder.setDiscountPrice(MoneyUtils.priceMultiplyPercent(purchaseOrder.getTotalPrice(), purchaseOrder.getDiscountPercent()));
        purchaseOrder.setTotalPrice(purchaseOrder.getTotalPrice().subtract(purchaseOrder.getDiscountPrice()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePurchaseOrderStatus(Long id, Integer status) {
        boolean approve = ErpAuditStatus.APPROVE.getStatus().equals(status);
        // 1.1 校验存在
        ErpPurchaseOrderDO purchaseOrder = validatePurchaseOrderExists(id);
        if (StrUtil.isNotBlank(purchaseOrder.getProcessInstanceId())) {
            throw exception(approve ? PURCHASE_ORDER_APPROVE_FAIL : PURCHASE_ORDER_PROCESS_FAIL);
        }
        // 1.2 校验状态
        if (purchaseOrder.getStatus().equals(status)) {
            throw exception(approve ? PURCHASE_ORDER_APPROVE_FAIL : PURCHASE_ORDER_PROCESS_FAIL);
        }
        // 1.3 存在采购入单，无法反审核
        if (!approve && purchaseOrder.getInCount().compareTo(BigDecimal.ZERO) > 0) {
            throw exception(PURCHASE_ORDER_PROCESS_FAIL_EXISTS_IN);
        }
        // 1.4 存在采购退货单，无法反审核
        if (!approve && purchaseOrder.getReturnCount().compareTo(BigDecimal.ZERO) > 0) {
            throw exception(PURCHASE_ORDER_PROCESS_FAIL_EXISTS_RETURN);
        }

        // 2. 更新状态
        int updateCount = erpPurchaseOrderMapper.updateByIdAndStatus(id, purchaseOrder.getStatus(),
                new ErpPurchaseOrderDO().setStatus(status));
        if (updateCount == 0) {
            throw exception(approve ? PURCHASE_ORDER_APPROVE_FAIL : PURCHASE_ORDER_PROCESS_FAIL);
        }

        // 3. 如果是反审核操作，发布采购订单变更事件
        if (!approve) {
            eventPublisher.publishEvent(new PurchaseOrderChangedEvent(id, PurchaseOrderChangedEvent.ChangeType.ORDER_REJECTED));
            log.info("[updatePurchaseOrderStatus] 采购订单反审核，已发布变更事件，orderId={}", id);
        }
    }

    private void validatePurchaseOrderEditable(ErpPurchaseOrderDO purchaseOrder) {
        if (ErpAuditStatus.APPROVE.getStatus().equals(purchaseOrder.getStatus())) {
            throw exception(PURCHASE_ORDER_UPDATE_FAIL_APPROVE, purchaseOrder.getNo());
        }
        if (isApprovalRunning(purchaseOrder)) {
            throw exception(PURCHASE_ORDER_UPDATE_FAIL_PROCESSING, purchaseOrder.getNo());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePurchaseOrderStatusByBpm(Long orderId, String processInstanceId, Integer status, String reason) {
        ErpPurchaseOrderDO purchaseOrder = validatePurchaseOrderExists(orderId);
        boolean reject = ErpAuditStatus.REJECT.getStatus().equals(status);
        ErpPurchaseOrderDO updateObj = new ErpPurchaseOrderDO()
                .setId(orderId)
                .setProcessInstanceId(processInstanceId)
                .setStatus(status);
        if (reject) {
            updateObj.setLastRejectReason(reason);
            updateObj.setLastRejectTime(LocalDateTime.now());
            updateObj.setLastRejectUserId(null);
        }
        int updateCount = erpPurchaseOrderMapper.updateByIdAndStatus(orderId, purchaseOrder.getStatus(), updateObj);
        if (updateCount == 0) {
            throw exception(PURCHASE_ORDER_PROCESS_FAIL);
        }
        erpPurchaseOrderAuditLogMapper.insert(new ErpPurchaseOrderAuditLogDO()
                .setOrderId(orderId)
                .setActionType(resolveAuditActionType(status))
                .setBeforeStatus(purchaseOrder.getStatus())
                .setAfterStatus(status)
                .setReason(reason));
        if (reject) {
            erpPurchaseOrderRejectLogMapper.insert(new ErpPurchaseOrderRejectLogDO()
                    .setOrderId(orderId)
                    .setReason(reason));
        }
    }

    private List<ErpPurchaseOrderItemDO> validatePurchaseOrderItems(List<ErpPurchaseOrderSaveReqVO.Item> list) {
        // 1. 校验产品存在
        List<ErpProductDO> productList = productService.validProductList(
                convertSet(list, ErpPurchaseOrderSaveReqVO.Item::getProductId));
        Map<Long, ErpProductDO> productMap = convertMap(productList, ErpProductDO::getId);
        // 2. 转化为 ErpPurchaseOrderItemDO 列表
        return convertList(list, o -> BeanUtils.toBean(o, ErpPurchaseOrderItemDO.class, item -> {
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

    private void updatePurchaseOrderItemList(Long id, List<ErpPurchaseOrderItemDO> newList) {
        // 第一步，对比新老数据，获得添加、修改、删除的列表
        List<ErpPurchaseOrderItemDO> oldList = erpPurchaseOrderItemMapper.selectListByOrderId(id);
        List<List<ErpPurchaseOrderItemDO>> diffList = diffList(oldList, newList, // id 不同，就认为是不同的记录
                (oldVal, newVal) -> oldVal.getId().equals(newVal.getId()));

        // 第二步，批量添加、修改、删除
        if (CollUtil.isNotEmpty(diffList.get(0))) {
            diffList.get(0).forEach(o -> o.setOrderId(id));
            erpPurchaseOrderItemMapper.insertBatch(diffList.get(0));
        }
        if (CollUtil.isNotEmpty(diffList.get(1))) {
            erpPurchaseOrderItemMapper.updateBatch(diffList.get(1));
        }
        if (CollUtil.isNotEmpty(diffList.get(2))) {
            erpPurchaseOrderItemMapper.deleteByIds(convertList(diffList.get(2), ErpPurchaseOrderItemDO::getId));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePurchaseOrderInCount(Long id, Map<Long, BigDecimal> inCountMap) {
        List<ErpPurchaseOrderItemDO> orderItems = erpPurchaseOrderItemMapper.selectListByOrderId(id);
        // 1. 批量查询产品信息（消除 N+1）
        Set<Long> productIds = convertSet(orderItems, ErpPurchaseOrderItemDO::getProductId);
        Map<Long, ErpProductDO> productMap = convertMap(productService.validProductList(productIds), ErpProductDO::getId);
        // 2. 更新每个采购订单项
        orderItems.forEach(item -> {
            BigDecimal inCount = inCountMap.getOrDefault(item.getId(), BigDecimal.ZERO);
            if (item.getInCount().equals(inCount)) {
                return;
            }
            if (inCount.compareTo(item.getCount()) > 0) {
                throw exception(PURCHASE_ORDER_ITEM_IN_FAIL_PRODUCT_EXCEED,
                        productMap.get(item.getProductId()).getName(), item.getCount());
            }
            erpPurchaseOrderItemMapper.updateById(new ErpPurchaseOrderItemDO().setId(item.getId()).setInCount(inCount));
        });
        // 3. 更新采购订单
        BigDecimal totalInCount = getSumValue(inCountMap.values(), value -> value, BigDecimal::add, BigDecimal.ZERO);
        erpPurchaseOrderMapper.updateById(new ErpPurchaseOrderDO().setId(id).setInCount(totalInCount));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePurchaseOrderReturnCount(Long orderId, Map<Long, BigDecimal> returnCountMap) {
        List<ErpPurchaseOrderItemDO> orderItems = erpPurchaseOrderItemMapper.selectListByOrderId(orderId);
        // 1. 批量查询产品信息（消除 N+1）
        Set<Long> productIds = convertSet(orderItems, ErpPurchaseOrderItemDO::getProductId);
        Map<Long, ErpProductDO> productMap = convertMap(productService.validProductList(productIds), ErpProductDO::getId);
        // 2. 更新每个采购订单项
        orderItems.forEach(item -> {
            BigDecimal returnCount = returnCountMap.getOrDefault(item.getId(), BigDecimal.ZERO);
            if (item.getReturnCount().equals(returnCount)) {
                return;
            }
            if (returnCount.compareTo(item.getInCount()) > 0) {
                throw exception(PURCHASE_ORDER_ITEM_RETURN_FAIL_IN_EXCEED,
                        productMap.get(item.getProductId()).getName(), item.getInCount());
            }
            erpPurchaseOrderItemMapper.updateById(new ErpPurchaseOrderItemDO().setId(item.getId()).setReturnCount(returnCount));
        });
        // 3. 更新采购订单
        BigDecimal totalReturnCount = getSumValue(returnCountMap.values(), value -> value, BigDecimal::add, BigDecimal.ZERO);
        erpPurchaseOrderMapper.updateById(new ErpPurchaseOrderDO().setId(orderId).setReturnCount(totalReturnCount));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePurchaseOrderPaymentPrice(Long orderId) {
        // 1. 查询该订单下所有已审批的入库单
        List<ErpPurchaseInDO> purchaseIns = erpPurchaseInMapper.selectListByOrderIdAndStatus(
                orderId, ErpAuditStatus.APPROVE.getStatus());

        // 2. 汇总已付金额
        BigDecimal totalPaymentPrice = purchaseIns.stream()
                .map(pi -> pi.getPaymentPrice() != null ? pi.getPaymentPrice() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 3. 获取订单总价
        ErpPurchaseOrderDO order = erpPurchaseOrderMapper.selectById(orderId);
        if (order == null) {
            return;
        }
        BigDecimal totalPrice = order.getTotalPrice() != null ? order.getTotalPrice() : BigDecimal.ZERO;

        // 4. 计算付款状态
        Integer paymentStatus;
        if (totalPaymentPrice.compareTo(BigDecimal.ZERO) <= 0) {
            paymentStatus = 0; // 未付款
        } else if (totalPaymentPrice.compareTo(totalPrice) >= 0) {
            paymentStatus = 2; // 全额付款
        } else {
            paymentStatus = 1; // 部分付款
        }

        // 5. 更新采购订单
        erpPurchaseOrderMapper.updateById(new ErpPurchaseOrderDO()
                .setId(orderId)
                .setPaymentPrice(totalPaymentPrice)
                .setPaymentStatus(paymentStatus));

        log.info("[updatePurchaseOrderPaymentPrice] 更新采购订单付款状态，orderId={}, paymentPrice={}, paymentStatus={}",
                orderId, totalPaymentPrice, paymentStatus);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePurchaseOrder(List<Long> ids) {
        // 1. 校验不处于已审批
        List<ErpPurchaseOrderDO> purchaseOrders = erpPurchaseOrderMapper.selectByIds(ids);
        if (CollUtil.isEmpty(purchaseOrders)) {
            return;
        }
        purchaseOrders.forEach(purchaseOrder -> {
            if (ErpAuditStatus.APPROVE.getStatus().equals(purchaseOrder.getStatus())) {
                throw exception(PURCHASE_ORDER_DELETE_FAIL_APPROVE, purchaseOrder.getNo());
            }
            if (isApprovalRunning(purchaseOrder)) {
                throw exception(PURCHASE_ORDER_DELETE_FAIL_PROCESSING, purchaseOrder.getNo());
            }
        });

        // 2. 遍历删除，并发布取消事件（监听器异步回退 MRP 建议）
        purchaseOrders.forEach(purchaseOrder -> {
            erpPurchaseOrderMapper.deleteById(purchaseOrder.getId());
            erpPurchaseOrderItemMapper.deleteByOrderId(purchaseOrder.getId());
            eventPublisher.publishEvent(new PurchaseOrderChangedEvent(
                    purchaseOrder.getId(), PurchaseOrderChangedEvent.ChangeType.ORDER_CANCELLED));
        });
        // 注意：MRP 建议的回退由 PurchaseOrderChangeListener 异步处理，不在此处同步操作
    }

    private ErpPurchaseOrderDO validatePurchaseOrderExists(Long id) {
        ErpPurchaseOrderDO purchaseOrder = erpPurchaseOrderMapper.selectById(id);
        if (purchaseOrder == null) {
            throw exception(PURCHASE_ORDER_NOT_EXISTS);
        }
        return purchaseOrder;
    }

    @Override
    public ErpPurchaseOrderDO getPurchaseOrder(Long id) {
        return erpPurchaseOrderMapper.selectById(id);
    }

    @Override
    public List<ErpPurchaseOrderDO> getPurchaseOrderList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return erpPurchaseOrderMapper.selectByIds(ids);
    }

    @Override
    public List<ErpPurchaseOrderAuditLogDO> getPurchaseOrderAuditLogListByOrderId(Long orderId) {
        return erpPurchaseOrderAuditLogMapper.selectListByOrderId(orderId);
    }

    @Override
    public List<ErpPurchaseOrderRejectLogDO> getPurchaseOrderRejectLogListByOrderId(Long orderId) {
        return erpPurchaseOrderRejectLogMapper.selectListByOrderId(orderId);
    }

    @Override
    public ErpPurchaseOrderDO validatePurchaseOrder(Long id) {
        ErpPurchaseOrderDO purchaseOrder = validatePurchaseOrderExists(id);
        if (ObjectUtil.notEqual(purchaseOrder.getStatus(), ErpAuditStatus.APPROVE.getStatus())) {
            throw exception(PURCHASE_ORDER_NOT_APPROVE);
        }
        return purchaseOrder;
    }

    @Override
    public PageResult<ErpPurchaseOrderDO> getPurchaseOrderPage(ErpPurchaseOrderPageReqVO pageReqVO) {
        return erpPurchaseOrderMapper.selectPage(pageReqVO);
    }

    // ==================== 订单项 ====================

    @Override
    public List<ErpPurchaseOrderItemDO> getPurchaseOrderItemListByOrderId(Long orderId) {
        return erpPurchaseOrderItemMapper.selectListByOrderId(orderId);
    }

    @Override
    public List<ErpPurchaseOrderItemDO> getPurchaseOrderItemListByOrderIds(Collection<Long> orderIds) {
        if (CollUtil.isEmpty(orderIds)) {
            return Collections.emptyList();
        }
        return erpPurchaseOrderItemMapper.selectListByOrderIds(orderIds);
    }

    private boolean isApprovalRunning(ErpPurchaseOrderDO purchaseOrder) {
        return ErpAuditStatus.PROCESS.getStatus().equals(purchaseOrder.getStatus())
                && StrUtil.isNotBlank(purchaseOrder.getProcessInstanceId());
    }

    PurchaseOrderBatchFieldUpdater createBatchUpdater(ErpPurchaseOrderBatchUpdateReqVO reqVO) {
        if (!BATCH_EDIT_MODE_OVERWRITE.equals(reqVO.getMode())) {
            throw exception(PURCHASE_ORDER_BATCH_UPDATE_FIELD_NOT_SUPPORT, reqVO.getFieldKey());
        }
        switch (reqVO.getFieldKey()) {
            case "accountId":
                return purchaseOrder -> {
                    Long accountId = parseLongValue(reqVO.getFieldKey(), reqVO.getValue());
                    accountService.validateAccount(accountId);
                    purchaseOrder.setAccountId(accountId);
                };
            case "orderTime":
                return purchaseOrder -> purchaseOrder.setOrderTime(parseDateTimeValue(reqVO.getFieldKey(), reqVO.getValue()));
            case "remark":
                return purchaseOrder -> purchaseOrder.setRemark(reqVO.getValue());
            default:
                throw exception(PURCHASE_ORDER_BATCH_UPDATE_FIELD_NOT_SUPPORT, reqVO.getFieldKey());
        }
    }

    private Long parseLongValue(String fieldKey, String value) {
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException ex) {
            throw exception(PURCHASE_ORDER_BATCH_UPDATE_FIELD_VALUE_INVALID, fieldKey, value);
        }
    }

    private LocalDateTime parseDateTimeValue(String fieldKey, String value) {
        try {
            return LocalDateTime.parse(value, BATCH_ORDER_TIME_FORMATTER);
        } catch (DateTimeParseException ex) {
            throw exception(PURCHASE_ORDER_BATCH_UPDATE_FIELD_VALUE_INVALID, fieldKey, value);
        }
    }

    @FunctionalInterface
    interface PurchaseOrderBatchFieldUpdater {
        void apply(ErpPurchaseOrderDO purchaseOrder);
    }

    private String resolveAuditActionType(Integer status) {
        if (ErpAuditStatus.APPROVE.getStatus().equals(status)) {
            return ErpPurchaseOrderAuditActionTypeConstants.APPROVE;
        }
        if (ErpAuditStatus.REJECT.getStatus().equals(status)) {
            return ErpPurchaseOrderAuditActionTypeConstants.REJECT;
        }
        return ErpPurchaseOrderAuditActionTypeConstants.CANCEL;
    }

}
