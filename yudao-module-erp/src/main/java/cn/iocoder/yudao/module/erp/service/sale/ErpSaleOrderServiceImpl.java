package cn.iocoder.yudao.module.erp.service.sale;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.number.MoneyUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.order.ErpSaleOrderBatchUpdateReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.order.ErpSaleOrderBatchUpdateResultVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.order.ErpSaleOrderPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.order.ErpSaleOrderSaveReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.order.ErpSaleOrderUpdateStatusReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSaleOrderAuditLogDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSaleOrderDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSaleOrderItemDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSaleOrderRejectLogDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSaleOutDO;
import cn.iocoder.yudao.module.erp.dal.mysql.mrp.ErpMrpStockReservationMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.sale.ErpSaleOrderAuditLogMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.sale.ErpSaleOrderItemMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.sale.ErpSaleOrderMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.sale.ErpSaleOrderRejectLogMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.sale.ErpSaleOutMapper;
import cn.iocoder.yudao.module.erp.dal.redis.no.ErpNoRedisDAO;
import cn.iocoder.yudao.module.erp.enums.ErpAuditStatus;
import cn.iocoder.yudao.module.erp.enums.ErpBusinessTypeConstants;
import cn.iocoder.yudao.module.erp.enums.ErpSaleOrderAuditActionTypeConstants;
import cn.iocoder.yudao.module.erp.enums.ErpSettlementTypeConstants;
import cn.iocoder.yudao.module.erp.enums.mrp.ErpMrpStockReservationStatusEnum;
import cn.iocoder.yudao.module.erp.enums.sale.ErpSaleOrderDeliveryReadyStatusEnum;
import cn.iocoder.yudao.module.erp.framework.event.ErpSaleOrderApprovedEvent;
import cn.iocoder.yudao.module.erp.service.finance.ErpAccountService;
import cn.iocoder.yudao.module.erp.service.mrp.ErpMrpStockReservationSummaryService;
import cn.iocoder.yudao.module.erp.service.product.ErpProductService;
import cn.iocoder.yudao.module.erp.service.project.ErpProjectService;
import cn.iocoder.yudao.module.erp.service.project.ErpProjectLifecycleService;
import cn.iocoder.yudao.module.erp.service.project.event.ProjectLifecycleRefreshEvent;
import cn.iocoder.yudao.module.erp.util.ErpTransactionUtils;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.*;

// TODO 芋艿：记录操作日志

/**
 * ERP 销售订单 Service 实现类
 *
 * @author WeTai
 */
@Service
@Validated
@Slf4j
public class ErpSaleOrderServiceImpl implements ErpSaleOrderService {

    private static final String BATCH_EDIT_MODE_OVERWRITE = "overwrite";
    private static final DateTimeFormatter BATCH_ORDER_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Resource
    private ErpSaleOrderMapper erpSaleOrderMapper;
    @Resource
    private ErpSaleOrderItemMapper erpSaleOrderItemMapper;
    @Resource
    private ErpSaleOrderAuditLogMapper erpSaleOrderAuditLogMapper;
    @Resource
    private ErpSaleOrderRejectLogMapper erpSaleOrderRejectLogMapper;
    @Resource
    private ErpMrpStockReservationMapper erpMrpStockReservationMapper;
    @Resource
    private ErpMrpStockReservationSummaryService mrpStockReservationSummaryService;
    @Resource
    private ErpSaleOutMapper erpSaleOutMapper;

    @Resource
    private ErpNoRedisDAO noRedisDAO;

    @Resource
    private ErpProductService productService;
    @Resource
    private ErpCustomerService customerService;
    @Resource
    private ErpProjectService projectService;
    @Resource
    private ErpProjectLifecycleService projectLifecycleService;
    @Resource
    private ErpAccountService accountService;
    @Resource
    private ErpSaleOrderDeliveryReadyService saleOrderDeliveryReadyService;

    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSaleOrder(ErpSaleOrderSaveReqVO createReqVO) {
        // 1.1 校验订单项的有效性
        List<ErpSaleOrderItemDO> saleOrderItems = validateSaleOrderItems(createReqVO.getItems());
        validateSaleOrderBusiness(createReqVO);
        // 1.2 校验客户
        customerService.validateCustomer(createReqVO.getCustomerId());
        if (ErpBusinessTypeConstants.SELF_RESEARCH.equals(createReqVO.getBusinessType())
                && createReqVO.getProjectId() == null) {
            createReqVO.setProjectId(projectService.createDeliveryProjectFromSource(
                    createReqVO.getSourceProjectId(), createReqVO));
        }
        if (createReqVO.getProjectId() != null) {
            projectService.validateProject(createReqVO.getProjectId());
        }
        // 1.3 校验结算账户
        if (createReqVO.getAccountId() != null) {
            accountService.validateAccount(createReqVO.getAccountId());
        }
        // 1.4 校验销售人员
        if (createReqVO.getSaleUserId() != null) {
            adminUserApi.validateUser(createReqVO.getSaleUserId());
        }
        // 1.5 生成订单号，并校验唯一性
        String no = noRedisDAO.generate(ErpNoRedisDAO.SALE_ORDER_NO_PREFIX);
        if (erpSaleOrderMapper.selectByNo(no) != null) {
            throw exception(SALE_ORDER_NO_EXISTS);
        }

        // 2.1 插入订单
        ErpSaleOrderDO saleOrder = BeanUtils.toBean(createReqVO, ErpSaleOrderDO.class, in -> in
                .setNo(no)
                .setStatus(ErpAuditStatus.PROCESS.getStatus())
                .setDeliveryReadyStatus(ErpSaleOrderDeliveryReadyStatusEnum.NOT_READY.getStatus())
                .setDeliveryDate(convertDeliveryDate(createReqVO.getDeliveryDate())));
        calculateTotalPrice(saleOrder, saleOrderItems);
        erpSaleOrderMapper.insert(saleOrder);
        if (saleOrder.getProjectId() != null) {
            projectService.bindSaleOrder(saleOrder.getProjectId(), saleOrder.getId());
        }
        // 2.2 插入订单项
        saleOrderItems.forEach(o -> o.setOrderId(saleOrder.getId()));
        erpSaleOrderItemMapper.insertBatch(saleOrderItems);
        return saleOrder.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSaleOrder(ErpSaleOrderSaveReqVO updateReqVO) {
        // 1.1 校验存在
        ErpSaleOrderDO saleOrder = validateSaleOrderExists(updateReqVO.getId());
        if (ErpAuditStatus.APPROVE.getStatus().equals(saleOrder.getStatus())) {
            throw exception(SALE_ORDER_UPDATE_FAIL_APPROVE, saleOrder.getNo());
        }
        if (isApprovalRunning(saleOrder)) {
            throw exception(SALE_ORDER_UPDATE_FAIL_PROCESSING, saleOrder.getNo());
        }
        validateSaleOrderBusiness(updateReqVO);
        // 1.2 校验客户
        customerService.validateCustomer(updateReqVO.getCustomerId());
        if (ErpBusinessTypeConstants.SELF_RESEARCH.equals(updateReqVO.getBusinessType())
                && updateReqVO.getProjectId() == null) {
            updateReqVO.setProjectId(projectService.createDeliveryProjectFromSource(
                    updateReqVO.getSourceProjectId(), updateReqVO));
        }
        if (updateReqVO.getProjectId() != null) {
            projectService.validateProject(updateReqVO.getProjectId());
        }
        // 1.3 校验结算账户
        if (updateReqVO.getAccountId() != null) {
            accountService.validateAccount(updateReqVO.getAccountId());
        }
        // 1.4 校验销售人员
        if (updateReqVO.getSaleUserId() != null) {
            adminUserApi.validateUser(updateReqVO.getSaleUserId());
        }
        // 1.5 校验订单项的有效性
        List<ErpSaleOrderItemDO> saleOrderItems = validateSaleOrderItems(updateReqVO.getItems());

        // 2.1 更新订单
        ErpSaleOrderDO updateObj = BeanUtils.toBean(updateReqVO, ErpSaleOrderDO.class,
                item -> item.setDeliveryDate(convertDeliveryDate(updateReqVO.getDeliveryDate())));
        calculateTotalPrice(updateObj, saleOrderItems);
        erpSaleOrderMapper.updateById(updateObj);
        if (updateObj.getProjectId() != null) {
            projectService.bindSaleOrder(updateObj.getProjectId(), updateObj.getId());
        }
        // 2.2 更新订单项
        updateSaleOrderItemList(updateReqVO.getId(), saleOrderItems);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ErpSaleOrderBatchUpdateResultVO updateSaleOrderBatch(ErpSaleOrderBatchUpdateReqVO reqVO) {
        List<Long> uniqueIds = List.copyOf(new LinkedHashSet<>(reqVO.getIds()));
        List<ErpSaleOrderDO> saleOrders = erpSaleOrderMapper.selectByIds(uniqueIds);
        Map<Long, ErpSaleOrderDO> saleOrderMap = convertMap(saleOrders, ErpSaleOrderDO::getId);
        for (Long id : uniqueIds) {
            ErpSaleOrderDO saleOrder = saleOrderMap.get(id);
            if (saleOrder == null) {
                throw exception(SALE_ORDER_NOT_EXISTS);
            }
            validateSaleOrderEditable(saleOrder);
        }

        SaleOrderBatchFieldUpdater updater = createBatchUpdater(reqVO);
        uniqueIds.forEach(id -> {
            ErpSaleOrderDO updateObj = new ErpSaleOrderDO().setId(id);
            updater.apply(updateObj);
            erpSaleOrderMapper.updateById(updateObj);
        });

        ErpSaleOrderBatchUpdateResultVO result = new ErpSaleOrderBatchUpdateResultVO();
        result.setSuccessCount(uniqueIds.size());
        result.setFailureCount(0);
        result.setUpdatedIds(uniqueIds);
        result.setFailedItems(Collections.emptyList());
        return result;
    }

    private void validateSaleOrderBusiness(ErpSaleOrderSaveReqVO reqVO) {
        if (StrUtil.isBlank(reqVO.getBusinessType())) {
            throw exception(SALE_ORDER_BUSINESS_TYPE_REQUIRED);
        }
        if (ErpBusinessTypeConstants.SELF_RESEARCH.equals(reqVO.getBusinessType())
                && reqVO.getProjectId() == null && reqVO.getSourceProjectId() == null) {
            throw exception(SALE_ORDER_SOURCE_PROJECT_REQUIRED);
        }
        if (ErpBusinessTypeConstants.TOLL_MANUFACTURING.equals(reqVO.getBusinessType())
                && !ErpSettlementTypeConstants.PROCESSING_FEE.equals(reqVO.getSettlementType())) {
            throw exception(SALE_ORDER_SETTLEMENT_TYPE_INVALID);
        }
    }

    private void calculateTotalPrice(ErpSaleOrderDO saleOrder, List<ErpSaleOrderItemDO> saleOrderItems) {
        saleOrder.setTotalCount(getSumValue(saleOrderItems, ErpSaleOrderItemDO::getCount, BigDecimal::add));
        saleOrder.setTotalProductPrice(getSumValue(saleOrderItems, ErpSaleOrderItemDO::getTotalPrice, BigDecimal::add, BigDecimal.ZERO));
        saleOrder.setTotalTaxPrice(getSumValue(saleOrderItems, ErpSaleOrderItemDO::getTaxPrice, BigDecimal::add, BigDecimal.ZERO));
        saleOrder.setTotalPrice(saleOrder.getTotalProductPrice().add(saleOrder.getTotalTaxPrice()));
        // 计算优惠价格
        if (saleOrder.getDiscountPercent() == null) {
            saleOrder.setDiscountPercent(BigDecimal.ZERO);
        }
        saleOrder.setDiscountPrice(MoneyUtils.priceMultiplyPercent(saleOrder.getTotalPrice(), saleOrder.getDiscountPercent()));
        saleOrder.setTotalPrice(saleOrder.getTotalPrice().subtract(saleOrder.getDiscountPrice()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSaleOrderStatus(ErpSaleOrderUpdateStatusReqVO reqVO) {
        ErpSaleOrderDO saleOrder = validateSaleOrderExists(reqVO.getId());
        // 1.1 校验存在
        validateSaleOrderStatusTransition(saleOrder, reqVO);
        // 1.2 校验状态
        boolean approve = ErpAuditStatus.APPROVE.getStatus().equals(reqVO.getStatus());
        boolean reject = ErpAuditStatus.REJECT.getStatus().equals(reqVO.getStatus());
        String actionType = resolveAuditActionType(saleOrder.getStatus(), reqVO.getStatus());
        ErpSaleOrderDO updateObj = new ErpSaleOrderDO().setStatus(reqVO.getStatus());
        // 1.3 存在销售出库单，无法反审核
        if (reject) {
            updateObj.setLastRejectReason(reqVO.getReason());
            updateObj.setLastRejectTime(LocalDateTime.now());
            updateObj.setLastRejectUserId(SecurityFrameworkUtils.getLoginUserId());
        }
        // 1.4 存在销售退货单，无法反审核
        int updateCount = erpSaleOrderMapper.updateByIdAndStatus(reqVO.getId(), saleOrder.getStatus(), updateObj);

        // 2. 更新状态
        if (updateCount == 0) {
            throw exception(SALE_ORDER_STATUS_UPDATE_ILLEGAL);
        }
        erpSaleOrderAuditLogMapper.insert(new ErpSaleOrderAuditLogDO()
                .setOrderId(reqVO.getId())
                .setActionType(actionType)
                .setBeforeStatus(saleOrder.getStatus())
                .setAfterStatus(reqVO.getStatus())
                .setReason(reqVO.getReason()));
        if (ErpAuditStatus.APPROVE.getStatus().equals(saleOrder.getStatus())
                && ErpAuditStatus.PROCESS.getStatus().equals(reqVO.getStatus())) {
            List<Long> changedProductIds = convertList(
                    erpMrpStockReservationMapper.selectListBySourceOrderIds(Collections.singleton(reqVO.getId())),
                    reservation -> reservation.getProductId());
            erpMrpStockReservationMapper.updateStatusBySourceOrderIds(Collections.singleton(reqVO.getId()),
                    ErpMrpStockReservationStatusEnum.ACTIVE.getStatus(),
                    ErpMrpStockReservationStatusEnum.RELEASED.getStatus());
            mrpStockReservationSummaryService.refreshSummaryByProductIds(changedProductIds);
        }
        if (reject) {
            erpSaleOrderRejectLogMapper.insert(new ErpSaleOrderRejectLogDO()
                    .setOrderId(reqVO.getId())
                    .setReason(reqVO.getReason()));
            return;
        }
        if (approve) {
            eventPublisher.publishEvent(new ErpSaleOrderApprovedEvent(reqVO.getId()));
            // 事务提交后异步触发项目生命周期刷新
            if (saleOrder.getProjectId() != null) {
                Long projectId = saleOrder.getProjectId();
                ErpTransactionUtils.afterCommit(() -> {
                    eventPublisher.publishEvent(new ProjectLifecycleRefreshEvent(
                            projectId, "销售订单审批通过"));
                });
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSaleOrderStatusByBpm(Long orderId, String processInstanceId, Integer status, String reason) {
        ErpSaleOrderDO saleOrder = validateSaleOrderExists(orderId);
        boolean approve = ErpAuditStatus.APPROVE.getStatus().equals(status);
        boolean reject = ErpAuditStatus.REJECT.getStatus().equals(status);
        String actionType = resolveAuditActionType(saleOrder.getStatus(), status);
        ErpSaleOrderDO updateObj = new ErpSaleOrderDO()
                .setId(orderId)
                .setProcessInstanceId(processInstanceId)
                .setStatus(status);
        if (reject) {
            updateObj.setLastRejectReason(reason);
            updateObj.setLastRejectTime(LocalDateTime.now());
            updateObj.setLastRejectUserId(null);
        }
        int updateCount = erpSaleOrderMapper.updateByIdAndStatus(orderId, saleOrder.getStatus(), updateObj);
        if (updateCount == 0) {
            throw exception(SALE_ORDER_STATUS_UPDATE_ILLEGAL);
        }
        erpSaleOrderAuditLogMapper.insert(new ErpSaleOrderAuditLogDO()
                .setOrderId(orderId)
                .setActionType(actionType)
                .setBeforeStatus(saleOrder.getStatus())
                .setAfterStatus(status)
                .setReason(reason));
        if (reject) {
            erpSaleOrderRejectLogMapper.insert(new ErpSaleOrderRejectLogDO()
                    .setOrderId(orderId)
                    .setReason(reason));
            return;
        }
        if (approve) {
            eventPublisher.publishEvent(new ErpSaleOrderApprovedEvent(orderId));
        }
    }

    private void validateSaleOrderStatusTransition(ErpSaleOrderDO saleOrder, ErpSaleOrderUpdateStatusReqVO reqVO) {
        if (StrUtil.isNotBlank(saleOrder.getProcessInstanceId())) {
            throw exception(SALE_ORDER_STATUS_UPDATE_ILLEGAL);
        }
        Integer currentStatus = saleOrder.getStatus();
        Integer targetStatus = reqVO.getStatus();
        if (ObjectUtil.equal(currentStatus, targetStatus)) {
            throw exception(SALE_ORDER_STATUS_UPDATE_ILLEGAL);
        }
        if (ErpAuditStatus.PROCESS.getStatus().equals(currentStatus)) {
            if (ErpAuditStatus.APPROVE.getStatus().equals(targetStatus)
                    || ErpAuditStatus.REJECT.getStatus().equals(targetStatus)) {
                validateRejectReason(reqVO, targetStatus);
                return;
            }
            throw exception(SALE_ORDER_STATUS_UPDATE_ILLEGAL);
        }
        if (ErpAuditStatus.REJECT.getStatus().equals(currentStatus)) {
            if (ErpAuditStatus.PROCESS.getStatus().equals(targetStatus)) {
                return;
            }
            throw exception(SALE_ORDER_STATUS_UPDATE_ILLEGAL);
        }
        if (ErpAuditStatus.APPROVE.getStatus().equals(currentStatus)) {
            if (!ErpAuditStatus.PROCESS.getStatus().equals(targetStatus)) {
                throw exception(SALE_ORDER_STATUS_UPDATE_ILLEGAL);
            }
            if (saleOrder.getOutCount().compareTo(BigDecimal.ZERO) > 0) {
                throw exception(SALE_ORDER_PROCESS_FAIL_EXISTS_OUT);
            }
            if (saleOrder.getReturnCount().compareTo(BigDecimal.ZERO) > 0) {
                throw exception(SALE_ORDER_PROCESS_FAIL_EXISTS_RETURN);
            }
            return;
        }
        throw exception(SALE_ORDER_STATUS_UPDATE_ILLEGAL);
    }

    private void validateSaleOrderEditable(ErpSaleOrderDO saleOrder) {
        if (ErpAuditStatus.APPROVE.getStatus().equals(saleOrder.getStatus())) {
            throw exception(SALE_ORDER_UPDATE_FAIL_APPROVE, saleOrder.getNo());
        }
        if (isApprovalRunning(saleOrder)) {
            throw exception(SALE_ORDER_UPDATE_FAIL_PROCESSING, saleOrder.getNo());
        }
    }

    SaleOrderBatchFieldUpdater createBatchUpdater(ErpSaleOrderBatchUpdateReqVO reqVO) {
        if (!BATCH_EDIT_MODE_OVERWRITE.equals(reqVO.getMode())) {
            throw exception(SALE_ORDER_BATCH_UPDATE_FIELD_NOT_SUPPORT, reqVO.getFieldKey());
        }
        switch (reqVO.getFieldKey()) {
            case "accountId":
                return saleOrder -> saleOrder.setAccountId(parseLongValue(reqVO.getFieldKey(), reqVO.getValue()));
            case "orderTime":
                return saleOrder -> saleOrder.setOrderTime(parseDateTimeValue(reqVO.getFieldKey(), reqVO.getValue()));
            case "deliveryDate":
                return saleOrder -> saleOrder.setDeliveryDate(parseDateValue(reqVO.getFieldKey(), reqVO.getValue()));
            case "remark":
                return saleOrder -> saleOrder.setRemark(reqVO.getValue());
            default:
                throw exception(SALE_ORDER_BATCH_UPDATE_FIELD_NOT_SUPPORT, reqVO.getFieldKey());
        }
    }

    private Long parseLongValue(String fieldKey, String value) {
        try {
            Long accountId = Long.valueOf(value);
            if (accountId != null) {
                accountService.validateAccount(accountId);
            }
            return accountId;
        } catch (NumberFormatException ex) {
            throw exception(SALE_ORDER_BATCH_UPDATE_FIELD_VALUE_INVALID, fieldKey, value);
        }
    }

    private LocalDateTime parseDateTimeValue(String fieldKey, String value) {
        try {
            return LocalDateTime.parse(value, BATCH_ORDER_TIME_FORMATTER);
        } catch (DateTimeParseException ex) {
            throw exception(SALE_ORDER_BATCH_UPDATE_FIELD_VALUE_INVALID, fieldKey, value);
        }
    }

    private LocalDate parseDateValue(String fieldKey, String value) {
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException ex) {
            throw exception(SALE_ORDER_BATCH_UPDATE_FIELD_VALUE_INVALID, fieldKey, value);
        }
    }

    @FunctionalInterface
    interface SaleOrderBatchFieldUpdater {
        void apply(ErpSaleOrderDO saleOrder);
    }

    private void validateRejectReason(ErpSaleOrderUpdateStatusReqVO reqVO, Integer targetStatus) {
        if (ErpAuditStatus.REJECT.getStatus().equals(targetStatus) && StrUtil.isBlank(reqVO.getReason())) {
            throw exception(SALE_ORDER_REJECT_REASON_REQUIRED);
        }
    }

    private String resolveAuditActionType(Integer beforeStatus, Integer afterStatus) {
        if (ErpAuditStatus.PROCESS.getStatus().equals(beforeStatus)
                && ErpAuditStatus.APPROVE.getStatus().equals(afterStatus)) {
            return ErpSaleOrderAuditActionTypeConstants.APPROVE;
        }
        if (ErpAuditStatus.PROCESS.getStatus().equals(beforeStatus)
                && ErpAuditStatus.REJECT.getStatus().equals(afterStatus)) {
            return ErpSaleOrderAuditActionTypeConstants.REJECT;
        }
        if (ErpAuditStatus.REJECT.getStatus().equals(beforeStatus)
                && ErpAuditStatus.PROCESS.getStatus().equals(afterStatus)) {
            return ErpSaleOrderAuditActionTypeConstants.RESUBMIT;
        }
        if (ErpAuditStatus.APPROVE.getStatus().equals(beforeStatus)
                && ErpAuditStatus.PROCESS.getStatus().equals(afterStatus)) {
            return ErpSaleOrderAuditActionTypeConstants.REVERSE_APPROVE;
        }
        throw exception(SALE_ORDER_STATUS_UPDATE_ILLEGAL);
    }

    private List<ErpSaleOrderItemDO> validateSaleOrderItems(List<ErpSaleOrderSaveReqVO.Item> list) {
        // 1. 校验产品存在
        List<ErpProductDO> productList = productService.validProductList(
                convertSet(list, ErpSaleOrderSaveReqVO.Item::getProductId));
        Map<Long, ErpProductDO> productMap = convertMap(productList, ErpProductDO::getId);
        // 2. 转化为 ErpSaleOrderItemDO 列表
        return convertList(list, o -> BeanUtils.toBean(o, ErpSaleOrderItemDO.class, item -> {
            item.setProductUnitId(productMap.get(item.getProductId()).getUnitId());
            item.setTotalPrice(MoneyUtils.priceMultiply(item.getProductPrice(), item.getCount()));
            if (item.getTotalPrice() == null) {
                return;
            }
            if (item.getTaxPercent() != null) {
                item.setTaxPrice(MoneyUtils.priceMultiplyPercent(item.getTotalPrice(), item.getTaxPercent()));
            }
        }));
    }

    private void updateSaleOrderItemList(Long id, List<ErpSaleOrderItemDO> newList) {
        // 第一步，对比新老数据，获得添加、修改、删除的列表
        List<ErpSaleOrderItemDO> oldList = erpSaleOrderItemMapper.selectListByOrderId(id);
        List<List<ErpSaleOrderItemDO>> diffList = diffList(oldList, newList, // id 不同，就认为是不同的记录
                (oldVal, newVal) -> oldVal.getId().equals(newVal.getId()));

        // 第二步，批量添加、修改、删除
        if (CollUtil.isNotEmpty(diffList.get(0))) {
            diffList.get(0).forEach(o -> o.setOrderId(id));
            erpSaleOrderItemMapper.insertBatch(diffList.get(0));
        }
        if (CollUtil.isNotEmpty(diffList.get(1))) {
            erpSaleOrderItemMapper.updateBatch(diffList.get(1));
        }
        if (CollUtil.isNotEmpty(diffList.get(2))) {
            erpSaleOrderItemMapper.deleteByIds(convertList(diffList.get(2), ErpSaleOrderItemDO::getId));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSaleOrderOutCount(Long id, Map<Long, BigDecimal> outCountMap) {
        List<ErpSaleOrderItemDO> orderItems = erpSaleOrderItemMapper.selectListByOrderId(id);
        // 1. 更新每个销售订单项
        orderItems.forEach(item -> {
            BigDecimal outCount = outCountMap.getOrDefault(item.getId(), BigDecimal.ZERO);
            if (item.getOutCount().equals(outCount)) {
                return;
            }
            if (outCount.compareTo(item.getCount()) > 0) {
                throw exception(SALE_ORDER_ITEM_OUT_FAIL_PRODUCT_EXCEED,
                        productService.getProduct(item.getProductId()).getName(), item.getCount());
            }
            erpSaleOrderItemMapper.updateById(new ErpSaleOrderItemDO().setId(item.getId()).setOutCount(outCount));
        });
        // 2. 更新销售订单
        BigDecimal totalOutCount = getSumValue(outCountMap.values(), value -> value, BigDecimal::add, BigDecimal.ZERO);
        erpSaleOrderMapper.updateById(new ErpSaleOrderDO().setId(id).setOutCount(totalOutCount));
        saleOrderDeliveryReadyService.recalculate(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSaleOrderReturnCount(Long orderId, Map<Long, BigDecimal> returnCountMap) {
        List<ErpSaleOrderItemDO> orderItems = erpSaleOrderItemMapper.selectListByOrderId(orderId);
        // 1. 更新每个销售订单项
        orderItems.forEach(item -> {
            BigDecimal returnCount = returnCountMap.getOrDefault(item.getId(), BigDecimal.ZERO);
            if (item.getReturnCount().equals(returnCount)) {
                return;
            }
            if (returnCount.compareTo(item.getOutCount()) > 0) {
                throw exception(SALE_ORDER_ITEM_RETURN_FAIL_OUT_EXCEED,
                        productService.getProduct(item.getProductId()).getName(), item.getOutCount());
            }
            erpSaleOrderItemMapper.updateById(new ErpSaleOrderItemDO().setId(item.getId()).setReturnCount(returnCount));
        });
        // 2. 更新销售订单
        BigDecimal totalReturnCount = getSumValue(returnCountMap.values(), value -> value, BigDecimal::add, BigDecimal.ZERO);
        erpSaleOrderMapper.updateById(new ErpSaleOrderDO().setId(orderId).setReturnCount(totalReturnCount));
        saleOrderDeliveryReadyService.recalculate(orderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSaleOrderReceiptPrice(Long orderId) {
        // 1. 查询该订单下所有已审批的出库单
        List<ErpSaleOutDO> saleOuts = erpSaleOutMapper.selectListByOrderIdAndStatus(
                orderId, ErpAuditStatus.APPROVE.getStatus());

        // 2. 汇总已收金额
        BigDecimal totalReceiptPrice = saleOuts.stream()
                .map(saleOut -> saleOut.getReceiptPrice() != null ? saleOut.getReceiptPrice() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 3. 获取订单总价
        ErpSaleOrderDO order = erpSaleOrderMapper.selectById(orderId);
        if (order == null) {
            return;
        }
        BigDecimal totalPrice = order.getTotalPrice() != null ? order.getTotalPrice() : BigDecimal.ZERO;

        // 4. 计算收款状态
        Integer receiptStatus;
        if (totalReceiptPrice.compareTo(BigDecimal.ZERO) <= 0) {
            receiptStatus = 0; // 未收款
        } else if (totalReceiptPrice.compareTo(totalPrice) >= 0) {
            receiptStatus = 2; // 全额收款
        } else {
            receiptStatus = 1; // 部分收款
        }

        // 5. 更新销售订单
        erpSaleOrderMapper.updateById(new ErpSaleOrderDO()
                .setId(orderId)
                .setReceiptPrice(totalReceiptPrice)
                .setReceiptStatus(receiptStatus));

        log.info("[updateSaleOrderReceiptPrice] 更新销售订单收款状态，orderId={}, receiptPrice={}, receiptStatus={}",
                orderId, totalReceiptPrice, receiptStatus);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSaleOrder(List<Long> ids) {
        // 1. 校验不处于已审批
        List<ErpSaleOrderDO> saleOrders = erpSaleOrderMapper.selectByIds(ids);
        if (CollUtil.isEmpty(saleOrders)) {
            return;
        }
        saleOrders.forEach(saleOrder -> {
            if (ErpAuditStatus.APPROVE.getStatus().equals(saleOrder.getStatus())) {
                throw exception(SALE_ORDER_DELETE_FAIL_APPROVE, saleOrder.getNo());
            }
            if (isApprovalRunning(saleOrder)) {
                throw exception(SALE_ORDER_DELETE_FAIL_PROCESSING, saleOrder.getNo());
            }
        });

        // 2. 遍历删除，并记录操作日志
        saleOrders.forEach(saleOrder -> {
            // 2.1 删除订单
            erpSaleOrderMapper.deleteById(saleOrder.getId());
            // 2.2 删除订单项
            erpSaleOrderItemMapper.deleteByOrderId(saleOrder.getId());
        });
    }

    private ErpSaleOrderDO validateSaleOrderExists(Long id) {
        ErpSaleOrderDO saleOrder = erpSaleOrderMapper.selectById(id);
        if (saleOrder == null) {
            throw exception(SALE_ORDER_NOT_EXISTS);
        }
        return saleOrder;
    }

    private boolean isApprovalRunning(ErpSaleOrderDO saleOrder) {
        return ErpAuditStatus.PROCESS.getStatus().equals(saleOrder.getStatus())
                && StrUtil.isNotBlank(saleOrder.getProcessInstanceId());
    }

    @Override
    public ErpSaleOrderDO getSaleOrder(Long id) {
        return erpSaleOrderMapper.selectById(id);
    }

    @Override
    public List<ErpSaleOrderDO> getSaleOrderListByIds(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return erpSaleOrderMapper.selectByIds(ids);
    }

    @Override
    public List<ErpSaleOrderDO> getSaleOrderDisplayListByIds(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return erpSaleOrderMapper.selectDisplayListByIds(ids);
    }

    @Override
    public List<ErpSaleOrderAuditLogDO> getSaleOrderAuditLogListByOrderId(Long orderId) {
        return erpSaleOrderAuditLogMapper.selectListByOrderId(orderId);
    }

    @Override
    public List<ErpSaleOrderRejectLogDO> getSaleOrderRejectLogListByOrderId(Long orderId) {
        return erpSaleOrderRejectLogMapper.selectListByOrderId(orderId);
    }

    @Override
    public ErpSaleOrderDO validateSaleOrder(Long id) {
        ErpSaleOrderDO saleOrder = validateSaleOrderExists(id);
        if (ObjectUtil.notEqual(saleOrder.getStatus(), ErpAuditStatus.APPROVE.getStatus())) {
            throw exception(SALE_ORDER_NOT_APPROVE);
        }
        return saleOrder;
    }

    @Override
    public PageResult<ErpSaleOrderDO> getSaleOrderPage(ErpSaleOrderPageReqVO pageReqVO) {
        return erpSaleOrderMapper.selectPage(pageReqVO);
    }

    private LocalDate convertDeliveryDate(LocalDateTime deliveryDate) {
        return deliveryDate == null ? null : deliveryDate.toLocalDate();
    }

    // ==================== 订单项 ====================

    @Override
    public List<ErpSaleOrderItemDO> getSaleOrderItemListByOrderId(Long orderId) {
        return erpSaleOrderItemMapper.selectListByOrderId(orderId);
    }

    @Override
    public List<ErpSaleOrderItemDO> getSaleOrderItemListByOrderIds(Collection<Long> orderIds) {
        if (CollUtil.isEmpty(orderIds)) {
            return Collections.emptyList();
        }
        return erpSaleOrderItemMapper.selectListByOrderIds(orderIds);
    }

}
