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
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinancePaymentAllocateMapper;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseInItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseInMapper;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseInStockExecuteItemBatchMapper;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseInStockExecuteItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseInStockExecuteMapper;
import cn.weitee.erp.module.erp.dal.redis.no.ErpNoRedisDAO;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.enums.ErpFinancePaymentAllocateStatusEnum;
import cn.weitee.erp.module.erp.enums.ErpPurchaseInStockExecuteStatusEnum;
import cn.weitee.erp.module.erp.enums.ErpPurchaseInStockInStatusEnum;
import cn.weitee.erp.module.erp.enums.ErpQaStatusEnum;
import cn.weitee.erp.module.erp.enums.common.ErpBizTypeEnum;
import cn.weitee.erp.module.erp.enums.stock.ErpStockRecordBizTypeEnum;
import cn.weitee.erp.module.erp.service.finance.ErpAccountService;
import cn.weitee.erp.module.erp.service.finance.ErpFinanceAssetCandidateService;
import cn.weitee.erp.module.erp.service.finance.ErpApStatementService;
import cn.weitee.erp.module.erp.service.finance.ErpFinanceBizHookService;
import cn.weitee.erp.module.erp.service.product.ErpProductService;
import cn.weitee.erp.module.erp.service.stock.ErpStockBatchService;
import cn.weitee.erp.module.erp.service.stock.ErpStockRecordService;
import cn.weitee.erp.module.erp.service.stock.bo.ErpStockBatchChangeReqBO;
import cn.weitee.erp.module.erp.service.stock.bo.ErpStockBatchInboundReqBO;
import cn.weitee.erp.module.erp.service.stock.bo.ErpStockRecordCreateReqBO;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.*;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.*;

// TODO 鑺嬭壙锛氳褰曟搷浣滄棩蹇?

/**
 * ERP 閲囪喘鍏ュ簱 Service 瀹炵幇绫?
 *
 * @author 鑺嬮亾婧愮爜
 */
@Service
@Validated
public class ErpPurchaseInServiceImpl implements ErpPurchaseInService {

    private static final String BATCH_EDIT_MODE_OVERWRITE = "overwrite";
    private static final String BATCH_FIELD_ACCOUNT_ID = "accountId";
    private static final String BATCH_FIELD_IN_TIME = "inTime";
    private static final String BATCH_FIELD_REMARK = "remark";
    private static final java.time.format.DateTimeFormatter BATCH_IN_TIME_FORMATTER =
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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
    @Lazy // 寤惰繜鍔犺浇锛岄伩鍏嶅惊鐜緷璧?
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
    private ErpFinancePaymentAllocateMapper erpFinancePaymentAllocateMapper;
    @Resource
    private ErpStockRecordService stockRecordService;
    @Resource
    private ErpStockBatchService stockBatchService;
    @Resource
    private ErpPurchaseInQualityService purchaseInQualityService;
    @Resource
    private ErpPurchaseSourceBatchService purchaseSourceBatchService;
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPurchaseIn(ErpPurchaseInSaveReqVO createReqVO) {
        // 1.1 鏍￠獙閲囪喘璁㈠崟宸插鏍?
        ErpPurchaseOrderDO purchaseOrder = purchaseOrderService.validatePurchaseOrder(createReqVO.getOrderId());
        // 1.2 鏍￠獙鍏ュ簱椤圭殑鏈夋晥鎬?
        List<ErpPurchaseInItemDO> purchaseInItems = validatePurchaseInItems(createReqVO.getItems());
        validatePurchaseOrderItemRemainingCount(createReqVO.getOrderId(), purchaseInItems);
        // 1.3 鏍￠獙缁撶畻璐︽埛
        accountService.validateAccount(createReqVO.getAccountId());
        // 1.4 鐢熸垚鍏ュ簱鍗曞彿锛屽苟鏍￠獙鍞竴鎬?
        String no = noRedisDAO.generate(ErpNoRedisDAO.PURCHASE_IN_NO_PREFIX);
        if (erpPurchaseInMapper.selectByNo(no) != null) {
            throw exception(PURCHASE_IN_NO_EXISTS);
        }

        // 2.1 鎻掑叆鍏ュ簱
        ErpPurchaseInDO purchaseIn = BeanUtils.toBean(createReqVO, ErpPurchaseInDO.class, in -> in
                .setNo(no).setStatus(ErpAuditStatus.PROCESS.getStatus())
                .setQaStatus(ErpQaStatusEnum.TO_INSPECT.getStatus())
                .setQaPassCount(BigDecimal.ZERO)
                .setQaRejectCount(BigDecimal.ZERO)
                .setStockInCount(BigDecimal.ZERO))
                .setOrderNo(purchaseOrder.getNo()).setSupplierId(purchaseOrder.getSupplierId());
        calculateTotalPrice(purchaseIn, purchaseInItems);
        erpPurchaseInMapper.insert(purchaseIn);
        // 2.2 鎻掑叆鍏ュ簱椤?
        purchaseInItems.forEach(o -> o.setInId(purchaseIn.getId()).setStockInCount(BigDecimal.ZERO));
        erpPurchaseInItemMapper.insertBatch(purchaseInItems);

        // 3. 鏇存柊閲囪喘璁㈠崟鐨勫叆搴撴暟閲?
        updatePurchaseOrderInCount(createReqVO.getOrderId());
        return purchaseIn.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePurchaseIn(ErpPurchaseInSaveReqVO updateReqVO) {
        // 1.1 鏍￠獙瀛樺湪
        ErpPurchaseInDO purchaseIn = validatePurchaseInExists(updateReqVO.getId());
        if (ErpAuditStatus.APPROVE.getStatus().equals(purchaseIn.getStatus())) {
            throw exception(PURCHASE_IN_UPDATE_FAIL_APPROVE, purchaseIn.getNo());
        }
        if (isApprovalRunning(purchaseIn)) {
            throw exception(PURCHASE_IN_UPDATE_FAIL_PROCESSING, purchaseIn.getNo());
        }
        // 1.2 鏍￠獙閲囪喘璁㈠崟宸插鏍?
        ErpPurchaseOrderDO purchaseOrder = purchaseOrderService.validatePurchaseOrder(updateReqVO.getOrderId());
        // 1.3 鏍￠獙缁撶畻璐︽埛
        accountService.validateAccount(updateReqVO.getAccountId());
        // 1.4 鏍￠獙璁㈠崟椤圭殑鏈夋晥鎬?
        List<ErpPurchaseInItemDO> purchaseInItems = validatePurchaseInItems(updateReqVO.getItems());
        validatePurchaseOrderItemRemainingCount(updateReqVO.getOrderId(), purchaseInItems);

        // 2.1 鏇存柊鍏ュ簱
        ErpPurchaseInDO updateObj = BeanUtils.toBean(updateReqVO, ErpPurchaseInDO.class)
                .setOrderNo(purchaseOrder.getNo()).setSupplierId(purchaseOrder.getSupplierId());
        calculateTotalPrice(updateObj, purchaseInItems);
        erpPurchaseInMapper.updateById(updateObj);
        // 2.2 鏇存柊鍏ュ簱椤?
        updatePurchaseInItemList(updateReqVO.getId(), purchaseInItems);

        // 3.1 鏇存柊閲囪喘璁㈠崟鐨勫叆搴撴暟閲?
        updatePurchaseOrderInCount(updateObj.getOrderId());
        // 3.2 娉ㄦ剰锛氬鏋滈噰璐鍗曠紪鍙峰彉鏇翠簡锛岄渶瑕佹洿鏂扳€滆€佲€濋噰璐鍗曠殑鍏ュ簱鏁伴噺
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
            if (isApprovalRunning(purchaseIn)) {
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

    private void calculateTotalPrice(ErpPurchaseInDO purchaseIn, List<ErpPurchaseInItemDO> purchaseInItems) {
        purchaseIn.setTotalCount(getSumValue(purchaseInItems, ErpPurchaseInItemDO::getCount, BigDecimal::add));
        purchaseIn.setTotalProductPrice(getSumValue(purchaseInItems, ErpPurchaseInItemDO::getTotalPrice, BigDecimal::add, BigDecimal.ZERO));
        purchaseIn.setTotalTaxPrice(getSumValue(purchaseInItems, ErpPurchaseInItemDO::getTaxPrice, BigDecimal::add, BigDecimal.ZERO));
        purchaseIn.setTotalPrice(purchaseIn.getTotalProductPrice().add(purchaseIn.getTotalTaxPrice()));
        // 璁＄畻浼樻儬浠锋牸
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePurchaseInStatus(Long id, Integer status) {
        boolean approve = ErpAuditStatus.APPROVE.getStatus().equals(status);
        ErpPurchaseInDO purchaseIn = validatePurchaseInExists(id);
        if (StrUtil.isNotBlank(purchaseIn.getProcessInstanceId())) {
            throw exception(approve ? PURCHASE_IN_APPROVE_FAIL : PURCHASE_IN_PROCESS_FAIL);
        }
        if (purchaseIn.getStatus().equals(status)) {
            throw exception(approve ? PURCHASE_IN_APPROVE_FAIL : PURCHASE_IN_PROCESS_FAIL);
        }
        if (!approve && hasApprovedAllocate(id)) {
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
        if (!approve && isQualityChecked(purchaseIn.getQaStatus())) {
            List<ErpPurchaseInItemDO> purchaseInItems = erpPurchaseInItemMapper.selectListByInId(id);
            if (hasStockedCount(purchaseIn)) {
                reverseExecutedStockRecords(purchaseIn);
                erpPurchaseInStockExecuteMapper.updateStatusByPurchaseInId(id,
                        ErpPurchaseInStockExecuteStatusEnum.EXECUTED.getStatus(),
                        ErpPurchaseInStockExecuteStatusEnum.VOID.getStatus());
            }
            resetPurchaseInItemQualityCheck(id, purchaseInItems);
            erpPurchaseInMapper.updateById(new ErpPurchaseInDO().setId(id)
                    .setQaStatus(ErpQaStatusEnum.TO_INSPECT.getStatus())
                    .setQaTime(null).setQaUserId(null).setQaRemark(null)
                    .setQaPassCount(BigDecimal.ZERO).setQaRejectCount(BigDecimal.ZERO)
                    .setStockInCount(BigDecimal.ZERO)
                    .setStockInStatus(null).setStockInTime(null).setStockInUserId(null));
            clearPurchaseInItemStockInCount(purchaseInItems);
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
        ErpPurchaseInDO purchaseIn = validatePurchaseInExists(id);
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
            throw exception(PURCHASE_IN_PROCESS_FAIL);
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
    public void qualityCheckPurchaseIn(Long userId, ErpPurchaseInQualityCheckReqVO reqVO) {
        purchaseInQualityService.submitPurchaseInQualityByPurchaseIn(userId, reqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmPurchaseInStockIn(Long userId, ErpPurchaseInConfirmStockInReqVO reqVO) {
        ErpPurchaseInDO purchaseIn = validatePurchaseInExists(reqVO.getId());
        if (!ErpAuditStatus.APPROVE.getStatus().equals(purchaseIn.getStatus())) {
            throw exception(PURCHASE_IN_CONFIRM_STOCK_IN_FAIL_STATUS);
        }
        if (!canConfirmStockInByQaStatus(purchaseIn.getQaStatus())) {
            throw exception(PURCHASE_IN_CONFIRM_STOCK_IN_FAIL_QA_STATUS);
        }
        if (!canExecuteStockInByStatus(purchaseIn.getStockInStatus())) {
            throw exception(PURCHASE_IN_CONFIRM_STOCK_IN_FAIL_STOCK_STATUS);
        }

        List<ErpPurchaseInItemDO> purchaseInItems = erpPurchaseInItemMapper.selectListByInId(reqVO.getId());
        Map<Long, ErpProductRespVO> productMap = productService.getProductVOMap(
                convertSet(purchaseInItems, ErpPurchaseInItemDO::getProductId));
        boolean hasBatchManagedProduct = purchaseInItems.stream()
                .filter(item -> calculateRemainingStockInCount(item.getQaPassCount(), item.getStockInCount())
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
        executeReqVO.setRemark("兼容旧版整单确认入库");
        executeReqVO.setItems(convertList(purchaseInItems, item -> {
            BigDecimal remainingCount = calculateRemainingStockInCount(item.getQaPassCount(), item.getStockInCount());
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
        ErpPurchaseInDO purchaseIn = validatePurchaseInExists(reqVO.getPurchaseInId());
        validatePurchaseInCanStockExecute(purchaseIn);
        List<ErpPurchaseInItemDO> purchaseInItems = erpPurchaseInItemMapper.selectListByInId(reqVO.getPurchaseInId());
        Map<Long, ErpPurchaseInItemDO> purchaseInItemMap = convertMap(purchaseInItems, ErpPurchaseInItemDO::getId);
        Map<Long, ErpProductRespVO> productMap = productService.getProductVOMap(
                convertSet(purchaseInItems, ErpPurchaseInItemDO::getProductId));
        List<ErpPurchaseInStockExecuteItemDO> executeItems = buildExecuteItems(reqVO, purchaseIn, purchaseInItemMap, productMap);
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
                buildExecuteItemBatches(reqVO, purchaseIn, executeItems, purchaseInItemMap, productMap);
        if (CollUtil.isNotEmpty(executeItemBatches)) {
            executeItemBatches.forEach(erpPurchaseInStockExecuteItemBatchMapper::insert);
        }
        createExecuteStockRecords(purchaseIn, executeItems);
        recalculatePurchaseInStockSummary(purchaseIn.getId(), userId);
        updatePurchaseOrderInCount(purchaseIn.getOrderId());
    }

    @Override
    public void updatePurchaseInPaymentPrice(Long id, BigDecimal paymentPrice) {
        ErpPurchaseInDO purchaseIn = selectPurchaseInById(id);
        if (purchaseIn.getPaymentPrice().equals(paymentPrice)) {
            return;
        }
        if (paymentPrice.compareTo(purchaseIn.getTotalPrice()) > 0) {
            throw exception(PURCHASE_IN_FAIL_PAYMENT_PRICE_EXCEED, paymentPrice, purchaseIn.getTotalPrice());
        }
        erpPurchaseInMapper.updateById(new ErpPurchaseInDO().setId(id).setPaymentPrice(paymentPrice));
    }

    private List<ErpPurchaseInItemDO> validatePurchaseInItems(List<ErpPurchaseInSaveReqVO.Item> list) {
        // 1. 鏍￠獙浜у搧瀛樺湪
        List<ErpProductDO> productList = productService.validProductList(
                convertSet(list, ErpPurchaseInSaveReqVO.Item::getProductId));
        Map<Long, ErpProductDO> productMap = convertMap(productList, ErpProductDO::getId);
        // 2. 杞寲涓?ErpPurchaseInItemDO 鍒楄〃
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
        // 绗竴姝ワ紝瀵规瘮鏂拌€佹暟鎹紝鑾峰緱娣诲姞銆佷慨鏀广€佸垹闄ょ殑鍒楄〃
        List<ErpPurchaseInItemDO> oldList = erpPurchaseInItemMapper.selectListByInId(id);
        List<List<ErpPurchaseInItemDO>> diffList = diffList(oldList, newList, // id 涓嶅悓锛屽氨璁や负鏄笉鍚岀殑璁板綍
                (oldVal, newVal) -> oldVal.getId().equals(newVal.getId()));

        // 绗簩姝ワ紝鎵归噺娣诲姞銆佷慨鏀广€佸垹闄?
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePurchaseIn(List<Long> ids) {
        // 1. 鏍￠獙涓嶅浜庡凡瀹℃壒
        List<ErpPurchaseInDO> purchaseIns = erpPurchaseInMapper.selectByIds(ids);
        if (CollUtil.isEmpty(purchaseIns)) {
            return;
        }
        purchaseIns.forEach(purchaseIn -> {
            if (ErpAuditStatus.APPROVE.getStatus().equals(purchaseIn.getStatus())) {
                throw exception(PURCHASE_IN_DELETE_FAIL_APPROVE, purchaseIn.getNo());
            }
            if (isApprovalRunning(purchaseIn)) {
                throw exception(PURCHASE_IN_DELETE_FAIL_PROCESSING, purchaseIn.getNo());
            }
        });

        // 2. 閬嶅巻鍒犻櫎锛屽苟璁板綍鎿嶄綔鏃ュ織
        purchaseIns.forEach(purchaseIn -> {
            // 2.1 鍒犻櫎璁㈠崟
            erpPurchaseInMapper.deleteById(purchaseIn.getId());
            // 2.2 鍒犻櫎璁㈠崟椤?
            erpPurchaseInItemMapper.deleteByInId(purchaseIn.getId());

            // 2.3 鏇存柊閲囪喘璁㈠崟鐨勫叆搴撴暟閲?
            updatePurchaseOrderInCount(purchaseIn.getOrderId());
        });

    }

    private ErpPurchaseInDO validatePurchaseInExists(Long id) {
        ErpPurchaseInDO purchaseIn = selectPurchaseInById(id);
        if (purchaseIn == null) {
            throw exception(PURCHASE_IN_NOT_EXISTS);
        }
        return purchaseIn;
    }

    @Override
    public ErpPurchaseInDO getPurchaseIn(Long id) {
        return selectPurchaseInById(id);
    }

    @Override
    public ErpPurchaseInDO validatePurchaseIn(Long id) {
        ErpPurchaseInDO purchaseIn = validatePurchaseInExists(id);
        if (ObjectUtil.notEqual(purchaseIn.getStatus(), ErpAuditStatus.APPROVE.getStatus())) {
            throw exception(PURCHASE_IN_NOT_APPROVE);
        }
        return purchaseIn;
    }

    @Override
    public PageResult<ErpPurchaseInDO> getPurchaseInPage(ErpPurchaseInPageReqVO pageReqVO) {
        return erpPurchaseInMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ErpPurchaseInDO> getPurchaseInListByIds(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return selectPurchaseInListByIds(ids);
    }

    private ErpPurchaseInDO selectPurchaseInById(Long id) {
        try {
            return erpPurchaseInMapper.selectById(id);
        } catch (RuntimeException ex) {
            if (isMissingStockInCountColumn(ex)) {
                return erpPurchaseInMapper.selectByIdCompatible(id);
            }
            throw ex;
        }
    }

    private List<ErpPurchaseInDO> selectPurchaseInListByIds(Collection<Long> ids) {
        try {
            return erpPurchaseInMapper.selectByIds(ids);
        } catch (RuntimeException ex) {
            if (isMissingStockInCountColumn(ex)) {
                return erpPurchaseInMapper.selectListByIdsCompatible(ids);
            }
            throw ex;
        }
    }

    private boolean isMissingStockInCountColumn(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            String message = current.getMessage();
            if (message != null && message.contains("stock_in_count")) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    @Override
    public List<ErpPurchaseInDO> getPurchaseInListByOrderIds(Collection<Long> orderIds) {
        if (CollUtil.isEmpty(orderIds)) {
            return Collections.emptyList();
        }
        return erpPurchaseInMapper.selectListByOrderIds(orderIds);
    }

    // ==================== 閲囪喘鍏ュ簱椤?====================

    @Override
    public List<ErpPurchaseInItemDO> getPurchaseInItemListByInId(Long inId) {
        return erpPurchaseInItemMapper.selectListByInId(inId);
    }

    @Override
    public List<ErpPurchaseInItemDO> getPurchaseInItemListByInIds(Collection<Long> inIds) {
        if (CollUtil.isEmpty(inIds)) {
            return Collections.emptyList();
        }
        return erpPurchaseInItemMapper.selectListByInIds(inIds);
    }

    @Override
    public List<ErpPurchaseInStockExecuteDO> getPurchaseInStockExecuteListByPurchaseInId(Long purchaseInId) {
        return erpPurchaseInStockExecuteMapper.selectListByPurchaseInId(purchaseInId);
    }

    @Override
    public List<ErpPurchaseInStockExecuteDO> getPurchaseInStockExecuteListByIds(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return erpPurchaseInStockExecuteMapper.selectBatchIds(ids);
    }

    @Override
    public List<ErpPurchaseInStockExecuteItemDO> getPurchaseInStockExecuteItemListByExecuteIds(Collection<Long> executeIds) {
        return erpPurchaseInStockExecuteItemMapper.selectListByExecuteIds(executeIds);
    }

    @Override
    public List<ErpPurchaseInStockExecuteItemDO> getPurchaseInStockExecuteItemListByIds(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return erpPurchaseInStockExecuteItemMapper.selectBatchIds(ids);
    }

    @Override
    public List<ErpPurchaseInStockExecuteItemBatchDO> getPurchaseInStockExecuteItemBatchListByExecuteItemIds(Collection<Long> executeItemIds) {
        return erpPurchaseInStockExecuteItemBatchMapper.selectListByExecuteItemIds(executeItemIds);
    }

    @Override
    public List<ErpPurchaseInStockExecuteItemBatchDO> getPurchaseInStockExecuteItemBatchListByPurchaseSourceBatchId(Long purchaseSourceBatchId) {
        return erpPurchaseInStockExecuteItemBatchMapper.selectListByPurchaseSourceBatchId(purchaseSourceBatchId);
    }

    private boolean hasApprovedAllocate(Long bizId) {
        return ObjectUtil.defaultIfNull(erpFinancePaymentAllocateMapper.selectCountByBizTypeAndBizIdAndStatus(
                ErpBizTypeEnum.PURCHASE_IN.getType(), bizId,
                ErpFinancePaymentAllocateStatusEnum.APPROVED.getStatus()), 0L) > 0;
    }

    private boolean isApprovalRunning(ErpPurchaseInDO purchaseIn) {
        return ErpAuditStatus.PROCESS.getStatus().equals(purchaseIn.getStatus())
                && StrUtil.isNotBlank(purchaseIn.getProcessInstanceId());
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

    private List<ErpPurchaseInItemDO> buildQualityCheckItems(List<ErpPurchaseInItemDO> purchaseInItems,
                                                             ErpPurchaseInQualityCheckReqVO reqVO) {
        if (CollUtil.isEmpty(purchaseInItems) || purchaseInItems.size() != reqVO.getItems().size()) {
            throw exception(PURCHASE_IN_QUALITY_CHECK_FAIL_ITEMS);
        }
        Map<Long, ErpPurchaseInItemDO> itemMap = convertMap(purchaseInItems, ErpPurchaseInItemDO::getId);
        return convertList(reqVO.getItems(), reqItem -> {
            ErpPurchaseInItemDO item = itemMap.get(reqItem.getId());
            if (item == null) {
                throw exception(PURCHASE_IN_QUALITY_CHECK_FAIL_ITEMS);
            }
            validateQualityCheckCount(item, reqItem);
            return new ErpPurchaseInItemDO().setId(item.getId())
                    .setInId(item.getInId())
                    .setProductId(item.getProductId())
                    .setWarehouseId(item.getWarehouseId())
                    .setQaPassCount(reqItem.getQaPassCount())
                    .setQaRejectCount(reqItem.getQaRejectCount())
                    .setQaRemark(reqItem.getQaRemark());
        });
    }

    private void validateQualityCheckCount(ErpPurchaseInItemDO item, ErpPurchaseInQualityCheckReqVO.Item reqItem) {
        if (reqItem.getQaPassCount().compareTo(BigDecimal.ZERO) < 0
                || reqItem.getQaRejectCount().compareTo(BigDecimal.ZERO) < 0) {
            throw exception(PURCHASE_IN_QUALITY_CHECK_FAIL_NEGATIVE, item.getId());
        }
        BigDecimal totalCount = reqItem.getQaPassCount().add(reqItem.getQaRejectCount());
        if (totalCount.compareTo(item.getCount()) != 0) {
            throw exception(PURCHASE_IN_QUALITY_CHECK_FAIL_COUNT, item.getId(), item.getCount());
        }
    }

    private ErpQaStatusEnum resolveQaStatus(BigDecimal qaPassCount, BigDecimal qaRejectCount) {
        if (qaRejectCount.compareTo(BigDecimal.ZERO) == 0) {
            return ErpQaStatusEnum.PASSED;
        }
        if (qaPassCount.compareTo(BigDecimal.ZERO) == 0) {
            return ErpQaStatusEnum.REJECTED;
        }
        return ErpQaStatusEnum.PARTIAL;
    }

    private boolean isQualityChecked(Integer qaStatus) {
        return qaStatus != null && !ErpQaStatusEnum.TO_INSPECT.getStatus().equals(qaStatus);
    }

    public Integer resolveStockInStatus(BigDecimal qaPassCount, BigDecimal stockInCount) {
        BigDecimal safeQaPassCount = ObjectUtil.defaultIfNull(qaPassCount, BigDecimal.ZERO);
        BigDecimal safeStockInCount = ObjectUtil.defaultIfNull(stockInCount, BigDecimal.ZERO);
        if (safeQaPassCount.compareTo(BigDecimal.ZERO) <= 0) {
            return ErpPurchaseInStockInStatusEnum.NO_NEED_STOCK_IN.getStatus();
        }
        if (safeStockInCount.compareTo(BigDecimal.ZERO) <= 0) {
            return ErpPurchaseInStockInStatusEnum.TO_STOCK_IN.getStatus();
        }
        if (safeStockInCount.compareTo(safeQaPassCount) < 0) {
            return ErpPurchaseInStockInStatusEnum.PARTIAL_STOCKED_IN.getStatus();
        }
        return ErpPurchaseInStockInStatusEnum.STOCKED_IN.getStatus();
    }

    private boolean canConfirmStockInByQaStatus(Integer qaStatus) {
        return ErpQaStatusEnum.PARTIAL.getStatus().equals(qaStatus)
                || ErpQaStatusEnum.PASSED.getStatus().equals(qaStatus);
    }

    private boolean canExecuteStockInByStatus(Integer stockInStatus) {
        return ErpPurchaseInStockInStatusEnum.TO_STOCK_IN.getStatus().equals(stockInStatus)
                || ErpPurchaseInStockInStatusEnum.PARTIAL_STOCKED_IN.getStatus().equals(stockInStatus);
    }

    private boolean isStockedIn(Integer stockInStatus) {
        return ErpPurchaseInStockInStatusEnum.STOCKED_IN.getStatus().equals(stockInStatus);
    }

    private boolean hasStockedCount(ErpPurchaseInDO purchaseIn) {
        return ObjectUtil.defaultIfNull(purchaseIn.getStockInCount(), BigDecimal.ZERO).compareTo(BigDecimal.ZERO) > 0;
    }

    private BigDecimal calculateRemainingStockInCount(BigDecimal qaPassCount, BigDecimal stockInCount) {
        BigDecimal remainingCount = ObjectUtil.defaultIfNull(qaPassCount, BigDecimal.ZERO)
                .subtract(ObjectUtil.defaultIfNull(stockInCount, BigDecimal.ZERO));
        return remainingCount.compareTo(BigDecimal.ZERO) > 0 ? remainingCount : BigDecimal.ZERO;
    }

    private void validatePurchaseInCanStockExecute(ErpPurchaseInDO purchaseIn) {
        if (!ErpAuditStatus.APPROVE.getStatus().equals(purchaseIn.getStatus())) {
            throw exception(PURCHASE_IN_CONFIRM_STOCK_IN_FAIL_STATUS);
        }
        if (!canConfirmStockInByQaStatus(purchaseIn.getQaStatus())) {
            throw exception(PURCHASE_IN_CONFIRM_STOCK_IN_FAIL_QA_STATUS);
        }
        if (!canExecuteStockInByStatus(purchaseIn.getStockInStatus())) {
            throw exception(PURCHASE_IN_CONFIRM_STOCK_IN_FAIL_STOCK_STATUS);
        }
    }

    private List<ErpPurchaseInStockExecuteItemDO> buildExecuteItems(ErpPurchaseInStockExecuteCreateReqVO reqVO,
                                                                    ErpPurchaseInDO purchaseIn,
                                                                    Map<Long, ErpPurchaseInItemDO> purchaseInItemMap,
                                                                    Map<Long, ErpProductRespVO> productMap) {
        return convertList(reqVO.getItems(), reqItem -> {
            ErpPurchaseInItemDO purchaseInItem = purchaseInItemMap.get(reqItem.getPurchaseInItemId());
            if (purchaseInItem == null || ObjectUtil.notEqual(purchaseInItem.getInId(), purchaseIn.getId())) {
                throw exception(PURCHASE_IN_QUALITY_CHECK_FAIL_ITEMS);
            }
            BigDecimal executeCount = ObjectUtil.defaultIfNull(reqItem.getCount(), BigDecimal.ZERO);
            BigDecimal remainingCount = calculateRemainingStockInCount(purchaseInItem.getQaPassCount(), purchaseInItem.getStockInCount());
            if (executeCount.compareTo(BigDecimal.ZERO) <= 0 || executeCount.compareTo(remainingCount) > 0) {
                throw exception(PURCHASE_IN_CONFIRM_STOCK_IN_FAIL_STOCK_STATUS);
            }
            validateExecuteItemBatches(reqItem, executeCount, productMap.get(purchaseInItem.getProductId()));
            return new ErpPurchaseInStockExecuteItemDO()
                    .setPurchaseInId(purchaseIn.getId())
                    .setPurchaseInItemId(purchaseInItem.getId())
                    .setProductId(purchaseInItem.getProductId())
                    .setWarehouseId(purchaseInItem.getWarehouseId())
                    .setCount(executeCount)
                    .setRemark(reqItem.getRemark());
        });
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

    private void validateExecuteItemBatches(ErpPurchaseInStockExecuteCreateReqVO.Item reqItem,
                                            BigDecimal executeCount,
                                            ErpProductRespVO product) {
        boolean batchManaged = product != null && Boolean.TRUE.equals(product.getBatchControlFlag());
        if (!batchManaged) {
            return;
        }
        if (CollUtil.isEmpty(reqItem.getBatches())) {
            throw exception(PURCHASE_IN_STOCK_BATCH_REQUIRED);
        }
        BigDecimal batchTotalCount = getSumValue(reqItem.getBatches(),
                ErpPurchaseInStockExecuteCreateReqVO.Batch::getCount, BigDecimal::add, BigDecimal.ZERO);
        if (batchTotalCount.compareTo(executeCount) != 0) {
            throw exception(PURCHASE_IN_STOCK_BATCH_COUNT_MISMATCH);
        }
    }

    private List<ErpPurchaseInStockExecuteItemBatchDO> buildExecuteItemBatches(ErpPurchaseInStockExecuteCreateReqVO reqVO,
                                                                                ErpPurchaseInDO purchaseIn,
                                                                                List<ErpPurchaseInStockExecuteItemDO> executeItems,
                                                                                Map<Long, ErpPurchaseInItemDO> purchaseInItemMap,
                                                                                Map<Long, ErpProductRespVO> productMap) {
        Map<Long, ErpPurchaseInStockExecuteCreateReqVO.Item> reqItemMap = convertMap(
                reqVO.getItems(), ErpPurchaseInStockExecuteCreateReqVO.Item::getPurchaseInItemId);
        Map<Long, ErpPurchaseSourceBatchDO> sourceBatchMap = purchaseSourceBatchService.getPurchaseSourceBatchMap(
                purchaseInItemMap.values().stream()
                        .map(ErpPurchaseInItemDO::getPurchaseSourceBatchId)
                        .filter(java.util.Objects::nonNull)
                        .collect(java.util.stream.Collectors.toSet()));
        return executeItems.stream().flatMap(executeItem -> {
            ErpProductRespVO product = productMap.get(executeItem.getProductId());
            ErpPurchaseInStockExecuteCreateReqVO.Item reqItem = reqItemMap.get(executeItem.getPurchaseInItemId());
            ErpPurchaseInItemDO purchaseInItem = purchaseInItemMap.get(executeItem.getPurchaseInItemId());
            ErpPurchaseSourceBatchDO sourceBatch = purchaseInItem == null || purchaseInItem.getPurchaseSourceBatchId() == null
                    ? null : sourceBatchMap.get(purchaseInItem.getPurchaseSourceBatchId());
            if (product == null || !Boolean.TRUE.equals(product.getBatchControlFlag()) || CollUtil.isEmpty(reqItem.getBatches())) {
                return java.util.stream.Stream.empty();
            }
            return reqItem.getBatches().stream().map(batch -> {
                ErpPurchaseInStockExecuteItemBatchDO executeItemBatch = new ErpPurchaseInStockExecuteItemBatchDO()
                        .setExecuteItemId(executeItem.getId())
                        .setPurchaseInItemId(executeItem.getPurchaseInItemId())
                        .setPurchaseSourceBatchId(sourceBatch != null ? sourceBatch.getId() : null)
                        .setProductId(executeItem.getProductId())
                        .setWarehouseId(executeItem.getWarehouseId())
                        .setBatchNo(batch.getBatchNo().trim())
                        .setPurchaseSourceBatchNo(sourceBatch != null ? sourceBatch.getBatchNo() : null)
                        .setCount(batch.getCount())
                        .setInboundTime(batch.getInboundTime())
                        .setProduceDate(batch.getProduceDate())
                        .setExpireDate(batch.getExpireDate())
                        .setRemark(batch.getRemark());
                ErpStockBatchInboundReqBO inboundReqBO = new ErpStockBatchInboundReqBO(
                        executeItem.getProductId(), executeItem.getWarehouseId(), executeItemBatch.getBatchNo(),
                        executeItemBatch.getInboundTime(), executeItemBatch.getProduceDate(), executeItemBatch.getExpireDate(),
                        executeItemBatch.getCount(), Boolean.FALSE, ErpStockRecordBizTypeEnum.PURCHASE_IN.getType(),
                        purchaseIn.getId(), executeItem.getPurchaseInItemId(), purchaseIn.getNo(), "PURCHASE_IN",
                        executeItemBatch.getPurchaseSourceBatchId(), executeItemBatch.getPurchaseSourceBatchNo(),
                        executeItemBatch.getRemark());
                executeItemBatch.setStockBatchId(stockBatchService.createOrIncreaseBatch(inboundReqBO).getId());
                return executeItemBatch;
            });
        }).toList();
    }

    private void createExecuteStockRecords(ErpPurchaseInDO purchaseIn, List<ErpPurchaseInStockExecuteItemDO> executeItems) {
        // 获取采购入库项的价格信息
        List<ErpPurchaseInItemDO> purchaseInItems = erpPurchaseInItemMapper.selectListByInId(purchaseIn.getId());
        Map<Long, ErpPurchaseInItemDO> purchaseInItemMap = convertMap(purchaseInItems, ErpPurchaseInItemDO::getId);
        executeItems.forEach(item -> {
            ErpPurchaseInItemDO purchaseInItem = purchaseInItemMap.get(item.getPurchaseInItemId());
            BigDecimal price = purchaseInItem != null ? purchaseInItem.getProductPrice() : null;
            BigDecimal amount = price != null ? price.multiply(item.getCount()) : null;
            stockRecordService.createStockRecord(new ErpStockRecordCreateReqBO(
                    item.getProductId(), item.getWarehouseId(), item.getCount(),
                    ErpStockRecordBizTypeEnum.PURCHASE_IN.getType(),
                    purchaseIn.getId(), item.getPurchaseInItemId(), purchaseIn.getNo(),
                    price, amount));
        });
    }

    private void reverseExecutedStockRecords(ErpPurchaseInDO purchaseIn) {
        List<ErpPurchaseInStockExecuteDO> executeList = erpPurchaseInStockExecuteMapper.selectListByPurchaseInId(purchaseIn.getId());
        Map<Long, ErpPurchaseInStockExecuteDO> executeMap = convertMap(executeList, ErpPurchaseInStockExecuteDO::getId);
        List<ErpPurchaseInStockExecuteItemDO> executeItems = erpPurchaseInStockExecuteItemMapper.selectListByPurchaseInId(purchaseIn.getId());
        // 获取采购入库项的价格信息
        List<ErpPurchaseInItemDO> purchaseInItems = erpPurchaseInItemMapper.selectListByInId(purchaseIn.getId());
        Map<Long, ErpPurchaseInItemDO> purchaseInItemMap = convertMap(purchaseInItems, ErpPurchaseInItemDO::getId);
        Map<Long, List<ErpPurchaseInStockExecuteItemBatchDO>> batchMap = convertMultiMap(
                erpPurchaseInStockExecuteItemBatchMapper.selectListByExecuteItemIds(
                        convertSet(executeItems, ErpPurchaseInStockExecuteItemDO::getId)),
                ErpPurchaseInStockExecuteItemBatchDO::getExecuteItemId);
        executeItems.stream()
                .filter(item -> {
                    ErpPurchaseInStockExecuteDO execute = executeMap.get(item.getExecuteId());
                    return execute != null && ErpPurchaseInStockExecuteStatusEnum.EXECUTED.getStatus().equals(execute.getStatus());
                })
                .forEach(item -> {
                    // 获取采购入库项的价格信息
                    ErpPurchaseInItemDO purchaseInItem = purchaseInItemMap.get(item.getPurchaseInItemId());
                    BigDecimal price = purchaseInItem != null ? purchaseInItem.getProductPrice() : null;
                    BigDecimal amount = price != null ? price.multiply(item.getCount()) : null;
                    stockRecordService.createStockRecord(new ErpStockRecordCreateReqBO(
                            item.getProductId(), item.getWarehouseId(), item.getCount().negate(),
                            ErpStockRecordBizTypeEnum.PURCHASE_IN_CANCEL.getType(),
                            purchaseIn.getId(), item.getPurchaseInItemId(), purchaseIn.getNo(),
                            price, amount));
                    List<ErpPurchaseInStockExecuteItemBatchDO> executeItemBatches = batchMap.get(item.getId());
                    if (CollUtil.isNotEmpty(executeItemBatches)) {
                        executeItemBatches.forEach(batch -> stockBatchService.decreaseBatch(new ErpStockBatchChangeReqBO(
                                batch.getStockBatchId(), batch.getCount(), ErpStockRecordBizTypeEnum.PURCHASE_IN_CANCEL.getType(),
                                purchaseIn.getId(), item.getPurchaseInItemId(), purchaseIn.getNo(), "采购入库作废回退批次库存")));
                    }
                });
    }

    private void recalculatePurchaseInStockSummary(Long purchaseInId, Long stockInUserId) {
        List<ErpPurchaseInItemDO> purchaseInItems = erpPurchaseInItemMapper.selectListByInId(purchaseInId);
        List<ErpPurchaseInStockExecuteDO> executeList = erpPurchaseInStockExecuteMapper.selectListByPurchaseInId(purchaseInId);
        Map<Long, ErpPurchaseInStockExecuteDO> executeMap = convertMap(executeList, ErpPurchaseInStockExecuteDO::getId);
        Map<Long, BigDecimal> itemStockInCountMap = new LinkedHashMap<>();
        erpPurchaseInStockExecuteItemMapper.selectListByPurchaseInId(purchaseInId).forEach(item -> {
            ErpPurchaseInStockExecuteDO execute = executeMap.get(item.getExecuteId());
            if (execute == null || !ErpPurchaseInStockExecuteStatusEnum.EXECUTED.getStatus().equals(execute.getStatus())) {
                return;
            }
            itemStockInCountMap.merge(item.getPurchaseInItemId(), ObjectUtil.defaultIfNull(item.getCount(), BigDecimal.ZERO), BigDecimal::add);
        });
        List<ErpPurchaseInItemDO> updateItems = convertList(purchaseInItems, item -> new ErpPurchaseInItemDO()
                .setId(item.getId())
                .setStockInCount(ObjectUtil.defaultIfNull(itemStockInCountMap.get(item.getId()), BigDecimal.ZERO)));
        if (CollUtil.isNotEmpty(updateItems)) {
            erpPurchaseInItemMapper.updateBatch(updateItems);
        }
        BigDecimal stockInCount = getSumValue(updateItems, ErpPurchaseInItemDO::getStockInCount, BigDecimal::add, BigDecimal.ZERO);
        ErpPurchaseInDO purchaseIn = validatePurchaseInExists(purchaseInId);
        ErpPurchaseInStockExecuteDO latestExecute = erpPurchaseInStockExecuteMapper
                .selectLatestExecutedByPurchaseInId(purchaseInId, ErpPurchaseInStockExecuteStatusEnum.EXECUTED.getStatus());
        erpPurchaseInMapper.updateById(new ErpPurchaseInDO()
                .setId(purchaseInId)
                .setStockInCount(stockInCount)
                .setStockInStatus(resolveStockInStatus(purchaseIn.getQaPassCount(), stockInCount))
                .setStockInTime(latestExecute != null ? latestExecute.getCreateTime() : null)
                .setStockInUserId(stockInCount.compareTo(BigDecimal.ZERO) > 0 ? stockInUserId : null));
    }

    private void clearPurchaseInItemStockInCount(List<ErpPurchaseInItemDO> purchaseInItems) {
        if (CollUtil.isEmpty(purchaseInItems)) {
            return;
        }
        erpPurchaseInItemMapper.updateBatch(convertList(purchaseInItems, item -> new ErpPurchaseInItemDO()
                .setId(item.getId())
                .setStockInCount(BigDecimal.ZERO)));
    }

    private void resetPurchaseInItemQualityCheck(Long inId, List<ErpPurchaseInItemDO> purchaseInItems) {
        if (CollUtil.isEmpty(purchaseInItems)) {
            return;
        }
        List<ErpPurchaseInItemDO> updateItems = convertList(purchaseInItems, item -> new ErpPurchaseInItemDO()
                .setId(item.getId()).setInId(inId)
                .setQaPassCount(null).setQaRejectCount(null).setQaRemark(null));
        erpPurchaseInItemMapper.updateBatch(updateItems);
    }

    private LocalDateTime defaultTime(LocalDateTime... values) {
        for (LocalDateTime value : values) {
            if (value != null) {
                return value;
            }
        }
        return LocalDateTime.now();
    }
}

