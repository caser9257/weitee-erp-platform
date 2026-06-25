package cn.weitee.erp.module.erp.service.finance;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.framework.mybatis.core.util.MyBatisUtils;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.apinvoice.ErpApInvoiceCancelMatchReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.apinvoice.ErpApInvoiceMatchReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.apinvoice.ErpApInvoicePageReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.apinvoice.ErpApInvoicePendingItemPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.apinvoice.ErpApInvoicePendingItemRespVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.apinvoice.ErpApInvoiceSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpApInvoiceDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpApInvoiceMatchItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpApStatementDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpApStatementItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseInDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseInItemDO;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpApInvoiceMapper;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpApInvoiceMatchItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpApStatementItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpApStatementMapper;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseInItemMapper;
import cn.weitee.erp.module.erp.enums.ErpApInvoiceMatchItemStatusEnum;
import cn.weitee.erp.module.erp.enums.ErpApInvoiceMatchStatusEnum;
import cn.weitee.erp.module.erp.enums.ErpApInvoiceStatusEnum;
import cn.weitee.erp.module.erp.enums.ErpApStatementItemTypeEnum;
import cn.weitee.erp.module.erp.enums.ErpApStatementStatusEnum;
import cn.weitee.erp.module.erp.enums.common.ErpBizTypeEnum;
import cn.weitee.erp.module.erp.service.purchase.ErpPurchaseInService;
import cn.weitee.erp.module.erp.service.purchase.ErpSupplierService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertMultiMap;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.*;

@Service
@Validated
public class ErpApInvoiceServiceImpl implements ErpApInvoiceService {

    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(6, RoundingMode.HALF_UP);
    private static final BigDecimal DEFAULT_TOLERANCE_AMOUNT = new BigDecimal("1.00");

    @Resource
    private ErpApInvoiceMapper erpApInvoiceMapper;
    @Resource
    private ErpApInvoiceMatchItemMapper erpApInvoiceMatchItemMapper;
    @Resource
    private ErpApStatementMapper erpApStatementMapper;
    @Resource
    private ErpApStatementItemMapper erpApStatementItemMapper;
    @Resource
    private ErpPurchaseInItemMapper erpPurchaseInItemMapper;
    @Resource
    private ErpPurchaseInService purchaseInService;
    @Resource
    private ErpSupplierService supplierService;
    @Resource
    private ErpApEstimateService apEstimateService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createApInvoice(ErpApInvoiceSaveReqVO createReqVO) {
        validateSupplierAndInvoiceNo(null, createReqVO.getSupplierId(), createReqVO.getInvoiceNo());
        supplierService.validateSupplier(createReqVO.getSupplierId());
        ErpApInvoiceDO invoice = BeanUtils.toBean(createReqVO, ErpApInvoiceDO.class, in -> in
                .setMatchedCount(null)
                .setMatchedAmount(defaultAmount(null))
                .setUnmatchedAmount(defaultAmount(createReqVO.getTotalAmount()))
                .setToleranceAmount(defaultTolerance(createReqVO.getToleranceAmount()))
                .setDifferenceAmount(defaultAmount(createReqVO.getTotalAmount()))
                .setMatchStatus(ErpApInvoiceMatchStatusEnum.UNMATCHED.getStatus()));
        erpApInvoiceMapper.insert(invoice);
        return invoice.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateApInvoice(ErpApInvoiceSaveReqVO updateReqVO) {
        ErpApInvoiceDO invoice = validateInvoiceExists(updateReqVO.getId());
        if (CollUtil.isNotEmpty(erpApInvoiceMatchItemMapper.selectActiveListByInvoiceId(invoice.getId()))) {
            throw exception(AP_INVOICE_UPDATE_FAIL_MATCHED, invoice.getInvoiceNo());
        }
        supplierService.validateSupplier(updateReqVO.getSupplierId());
        validateSupplierAndInvoiceNo(invoice.getId(), updateReqVO.getSupplierId(), updateReqVO.getInvoiceNo());
        erpApInvoiceMapper.updateById(BeanUtils.toBean(updateReqVO, ErpApInvoiceDO.class, in -> in
                .setMatchedCount(null)
                .setMatchedAmount(ZERO)
                .setUnmatchedAmount(defaultAmount(updateReqVO.getTotalAmount()))
                .setToleranceAmount(defaultTolerance(updateReqVO.getToleranceAmount()))
                .setDifferenceAmount(defaultAmount(updateReqVO.getTotalAmount()))
                .setMatchStatus(ErpApInvoiceMatchStatusEnum.UNMATCHED.getStatus())));
    }

    @Override
    public ErpApInvoiceDO getApInvoice(Long id) {
        return erpApInvoiceMapper.selectById(id);
    }

    @Override
    public PageResult<ErpApInvoiceDO> getApInvoicePage(ErpApInvoicePageReqVO pageReqVO) {
        return erpApInvoiceMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ErpApInvoiceMatchItemDO> getApInvoiceMatchItemListByInvoiceId(Long invoiceId) {
        return erpApInvoiceMatchItemMapper.selectActiveListByInvoiceId(invoiceId);
    }

    @Override
    public List<ErpApInvoiceMatchItemDO> getActiveMatchItemListByPurchaseInItemIds(Collection<Long> purchaseInItemIds) {
        if (CollUtil.isEmpty(purchaseInItemIds)) {
            return Collections.emptyList();
        }
        return erpApInvoiceMatchItemMapper.selectActiveListByPurchaseInItemIds(purchaseInItemIds);
    }

    @Override
    public PageResult<ErpApInvoicePendingItemRespVO> getPendingItemPage(ErpApInvoicePendingItemPageReqVO pageReqVO) {
        Long supplierId = pageReqVO.getSupplierId();
        if (pageReqVO.getInvoiceId() != null) {
            ErpApInvoiceDO invoice = validateInvoiceExists(pageReqVO.getInvoiceId());
            supplierId = invoice.getSupplierId();
        }
        if (supplierId == null) {
            return PageResult.empty(0L);
        }
        Page<ErpApInvoicePendingItemRespVO> page = MyBatisUtils.buildPage(pageReqVO);
        Page<ErpApInvoicePendingItemRespVO> result = erpApInvoiceMatchItemMapper.selectPendingItemPage(
                page,
                pageReqVO,
                supplierId,
                ErpApInvoiceMatchItemStatusEnum.ACTIVE.getStatus(),
                ErpBizTypeEnum.PURCHASE_IN.getType(),
                ErpApStatementStatusEnum.CLOSED.getStatus());
        return new PageResult<>(result.getRecords(), result.getTotal());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmMatch(ErpApInvoiceMatchReqVO reqVO) {
        if (CollUtil.isEmpty(reqVO.getItems())) {
            throw exception(AP_INVOICE_MATCH_ITEMS_EMPTY);
        }
        ErpApInvoiceDO invoice = validateInvoiceExists(reqVO.getInvoiceId());
        List<Long> purchaseInItemIds = reqVO.getItems().stream()
                .map(ErpApInvoiceMatchReqVO.Item::getPurchaseInItemId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        List<ErpPurchaseInItemDO> purchaseInItems = erpPurchaseInItemMapper.selectListByIds(purchaseInItemIds);
        Map<Long, ErpPurchaseInItemDO> purchaseInItemMap = convertMap(purchaseInItems, ErpPurchaseInItemDO::getId);
        List<ErpApInvoiceMatchItemDO> activeMatchedItems = erpApInvoiceMatchItemMapper.selectActiveListByPurchaseInItemIds(purchaseInItemIds);
        Map<Long, List<ErpApInvoiceMatchItemDO>> activeMatchedMap = convertMultiMap(activeMatchedItems,
                ErpApInvoiceMatchItemDO::getSourcePurchaseInItemId);

        List<Long> purchaseInIds = purchaseInItems.stream().map(ErpPurchaseInItemDO::getInId).filter(Objects::nonNull).distinct().toList();
        Map<Long, ErpPurchaseInDO> purchaseInMap = convertMap(
                purchaseInService.getPurchaseInListByIds(purchaseInIds), ErpPurchaseInDO::getId);
        Map<Long, ErpApStatementDO> statementMap = convertMap(
                erpApStatementMapper.selectListByBizTypeAndBizIds(ErpBizTypeEnum.PURCHASE_IN.getType(), purchaseInIds),
                ErpApStatementDO::getBizId);

        List<ErpApInvoiceMatchItemDO> insertList = new ArrayList<>(reqVO.getItems().size());
        Map<Long, BigDecimal> requestCountMap = new LinkedHashMap<>();
        Map<Long, BigDecimal> requestAmountMap = new LinkedHashMap<>();
        Map<Long, BigDecimal> statementDeltaAmountMap = new LinkedHashMap<>();
        Map<Long, BigDecimal> statementDeltaCountMap = new LinkedHashMap<>();
        for (ErpApInvoiceMatchReqVO.Item item : reqVO.getItems()) {
            ErpPurchaseInItemDO purchaseInItem = purchaseInItemMap.get(item.getPurchaseInItemId());
            ErpPurchaseInDO purchaseIn = purchaseInItem == null ? null : purchaseInMap.get(purchaseInItem.getInId());
            if (purchaseInItem == null || purchaseIn == null) {
                throw exception(PURCHASE_IN_NOT_EXISTS);
            }
            validatePositiveMatchFields(item, purchaseInItem.getId());
            ErpApStatementDO statement = statementMap.get(purchaseIn.getId());
            if (statement == null) {
                throw exception(AP_INVOICE_MATCH_STATEMENT_NOT_EXISTS, purchaseIn.getNo());
            }
            if (!ObjectUtil.equal(statement.getSupplierId(), invoice.getSupplierId())) {
                throw exception(AP_INVOICE_MATCH_SUPPLIER_MISMATCH, purchaseIn.getNo());
            }
            BigDecimal usedCount = sumMatchCount(activeMatchedMap.get(purchaseInItem.getId()));
            BigDecimal usedAmount = sumMatchAmount(activeMatchedMap.get(purchaseInItem.getId()));
            BigDecimal remainCount = safeSubtract(defaultAmount(purchaseInItem.getCount()), usedCount);
            BigDecimal remainAmount = safeSubtract(defaultAmount(purchaseInItem.getTotalPrice()), usedAmount);
            BigDecimal requestCount = requestCountMap.merge(purchaseInItem.getId(),
                    defaultAmount(item.getMatchCount()), BigDecimal::add);
            BigDecimal requestAmount = requestAmountMap.merge(purchaseInItem.getId(),
                    defaultAmount(item.getMatchAmount()), BigDecimal::add);
            if (requestCount.compareTo(remainCount) > 0) {
                throw exception(AP_INVOICE_MATCH_COUNT_EXCEED, purchaseInItem.getId(), requestCount, remainCount);
            }
            if (requestAmount.compareTo(remainAmount) > 0) {
                throw exception(AP_INVOICE_MATCH_AMOUNT_EXCEED, purchaseInItem.getId(), requestAmount, remainAmount);
            }
            ErpApInvoiceMatchItemDO matchItem = new ErpApInvoiceMatchItemDO()
                    .setInvoiceId(invoice.getId())
                    .setApStatementId(statement.getId())
                    .setSourceOrderId(statement.getSourceOrderId())
                    .setSourceOrderNo(statement.getSourceOrderNo())
                    .setSourcePurchaseInId(purchaseIn.getId())
                    .setSourcePurchaseInNo(purchaseIn.getNo())
                    .setSourcePurchaseInItemId(purchaseInItem.getId())
                    .setProductId(purchaseInItem.getProductId())
                    .setSupplierId(invoice.getSupplierId())
                    .setMatchCount(item.getMatchCount())
                    .setMatchAmount(item.getMatchAmount())
                    .setStatus(ErpApInvoiceMatchItemStatusEnum.ACTIVE.getStatus())
                    .setRemark(item.getRemark());
            insertList.add(matchItem);
            statementDeltaAmountMap.merge(statement.getId(), defaultAmount(item.getMatchAmount()), BigDecimal::add);
            statementDeltaCountMap.merge(statement.getId(), defaultAmount(item.getMatchCount()), BigDecimal::add);
        }
        validateInvoiceTotalLimit(invoice, insertList);
        erpApInvoiceMatchItemMapper.insertBatch(insertList);
        refreshInvoiceSummary(invoice.getId(), reqVO.getDifferenceReason());
        refreshStatementInvoiceSummary(statementDeltaAmountMap.keySet());
        createStatementMatchLogs(statementDeltaAmountMap, invoice, ErpApStatementItemTypeEnum.INVOICE_MATCHED.getStatus());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelMatch(ErpApInvoiceCancelMatchReqVO reqVO) {
        List<ErpApInvoiceMatchItemDO> items = erpApInvoiceMatchItemMapper.selectListByIds(reqVO.getIds());
        if (CollUtil.isEmpty(items)) {
            return;
        }
        Map<Long, BigDecimal> statementDeltaAmountMap = new LinkedHashMap<>();
        for (ErpApInvoiceMatchItemDO item : items) {
            if (!ErpApInvoiceMatchItemStatusEnum.ACTIVE.getStatus().equals(item.getStatus())) {
                continue;
            }
            erpApInvoiceMatchItemMapper.updateById(new ErpApInvoiceMatchItemDO()
                    .setId(item.getId())
                    .setStatus(ErpApInvoiceMatchItemStatusEnum.CANCELED.getStatus()));
            statementDeltaAmountMap.merge(item.getApStatementId(), defaultAmount(item.getMatchAmount()).negate(), BigDecimal::add);
        }
        for (Long invoiceId : convertSet(items, ErpApInvoiceMatchItemDO::getInvoiceId)) {
            refreshInvoiceSummary(invoiceId, null);
        }
        refreshStatementInvoiceSummary(statementDeltaAmountMap.keySet());
        Map<Long, List<ErpApInvoiceMatchItemDO>> invoiceItemMap = convertMultiMap(items, ErpApInvoiceMatchItemDO::getInvoiceId);
        invoiceItemMap.forEach((invoiceId, invoiceItems) -> {
            ErpApInvoiceDO invoice = validateInvoiceExists(invoiceId);
            Map<Long, BigDecimal> currentDeltaMap = new LinkedHashMap<>();
            invoiceItems.stream()
                    .filter(item -> ErpApInvoiceMatchItemStatusEnum.ACTIVE.getStatus().equals(item.getStatus()))
                    .forEach(item -> currentDeltaMap.merge(item.getApStatementId(),
                            defaultAmount(item.getMatchAmount()).negate(), BigDecimal::add));
            createStatementMatchLogs(currentDeltaMap, invoice, ErpApStatementItemTypeEnum.INVOICE_MATCH_CANCELED.getStatus());
        });
    }

    private void validateSupplierAndInvoiceNo(Long currentId, Long supplierId, String invoiceNo) {
        ErpApInvoiceDO exists = erpApInvoiceMapper.selectBySupplierIdAndInvoiceNo(supplierId, StrUtil.trim(invoiceNo));
        if (exists != null && !ObjectUtil.equal(exists.getId(), currentId)) {
            throw exception(AP_INVOICE_EXISTS, invoiceNo);
        }
    }

    private ErpApInvoiceDO validateInvoiceExists(Long id) {
        ErpApInvoiceDO invoice = erpApInvoiceMapper.selectById(id);
        if (invoice == null) {
            throw exception(AP_INVOICE_NOT_EXISTS);
        }
        return invoice;
    }

    private void validatePositiveMatchFields(ErpApInvoiceMatchReqVO.Item item, Long purchaseInItemId) {
        if (defaultAmount(item.getMatchCount()).compareTo(BigDecimal.ZERO) <= 0) {
            throw exception(AP_INVOICE_MATCH_COUNT_INVALID, purchaseInItemId);
        }
        if (defaultAmount(item.getMatchAmount()).compareTo(BigDecimal.ZERO) <= 0) {
            throw exception(AP_INVOICE_MATCH_AMOUNT_INVALID, purchaseInItemId);
        }
    }

    private void validateInvoiceTotalLimit(ErpApInvoiceDO invoice, List<ErpApInvoiceMatchItemDO> insertList) {
        BigDecimal currentMatchedCount = sumMatchCount(erpApInvoiceMatchItemMapper.selectActiveListByInvoiceId(invoice.getId()));
        BigDecimal currentMatchedAmount = sumMatchAmount(erpApInvoiceMatchItemMapper.selectActiveListByInvoiceId(invoice.getId()));
        BigDecimal requestCount = insertList.stream().map(ErpApInvoiceMatchItemDO::getMatchCount)
                .filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal requestAmount = insertList.stream().map(ErpApInvoiceMatchItemDO::getMatchAmount)
                .filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalMatchedCount = currentMatchedCount.add(requestCount);
        BigDecimal totalMatchedAmount = currentMatchedAmount.add(requestAmount);
        if (invoice.getTotalCount() != null && totalMatchedCount.compareTo(defaultAmount(invoice.getTotalCount())) > 0) {
            throw exception(AP_INVOICE_MATCH_TOTAL_COUNT_EXCEED, invoice.getInvoiceNo(), totalMatchedCount, invoice.getTotalCount());
        }
        BigDecimal totalLimit = defaultAmount(invoice.getTotalAmount()).add(defaultTolerance(invoice.getToleranceAmount()));
        if (totalMatchedAmount.compareTo(totalLimit) > 0) {
            throw exception(AP_INVOICE_MATCH_TOTAL_AMOUNT_EXCEED,
                    invoice.getInvoiceNo(), totalMatchedAmount, invoice.getTotalAmount(), defaultTolerance(invoice.getToleranceAmount()));
        }
    }

    private void refreshInvoiceSummary(Long invoiceId, String differenceReasonOverride) {
        ErpApInvoiceDO invoice = validateInvoiceExists(invoiceId);
        List<ErpApInvoiceMatchItemDO> activeItems = erpApInvoiceMatchItemMapper.selectActiveListByInvoiceId(invoiceId);
        BigDecimal matchedCount = sumMatchCount(activeItems);
        BigDecimal matchedAmount = sumMatchAmount(activeItems);
        BigDecimal unmatchedAmount = defaultAmount(invoice.getTotalAmount()).subtract(matchedAmount);
        BigDecimal differenceAmount = unmatchedAmount;
        Integer matchStatus = calculateInvoiceMatchStatus(invoice, matchedCount, matchedAmount,
                ObjectUtil.defaultIfNull(StrUtil.trimToNull(differenceReasonOverride), invoice.getDifferenceReason()));
        String differenceReason = matchStatus.equals(ErpApInvoiceMatchStatusEnum.MATCHED.getStatus())
                ? null
                : ObjectUtil.defaultIfNull(StrUtil.trimToNull(differenceReasonOverride), invoice.getDifferenceReason());
        erpApInvoiceMapper.updateById(new ErpApInvoiceDO()
                .setId(invoiceId)
                .setMatchedCount(activeItems.isEmpty() ? null : matchedCount)
                .setMatchedAmount(matchedAmount)
                .setUnmatchedAmount(unmatchedAmount)
                .setDifferenceAmount(differenceAmount)
                .setMatchStatus(matchStatus)
                .setDifferenceReason(differenceReason));
    }

    private Integer calculateInvoiceMatchStatus(ErpApInvoiceDO invoice, BigDecimal matchedCount, BigDecimal matchedAmount,
                                                String differenceReason) {
        if (matchedAmount.compareTo(BigDecimal.ZERO) == 0) {
            return ErpApInvoiceMatchStatusEnum.UNMATCHED.getStatus();
        }
        boolean countMatched = invoice.getTotalCount() == null
                || defaultAmount(invoice.getTotalCount()).compareTo(matchedCount) == 0;
        BigDecimal differenceAmount = defaultAmount(invoice.getTotalAmount()).subtract(matchedAmount).abs();
        if (countMatched && differenceAmount.compareTo(defaultTolerance(invoice.getToleranceAmount())) <= 0) {
            return ErpApInvoiceMatchStatusEnum.MATCHED.getStatus();
        }
        if (StrUtil.isNotBlank(differenceReason)) {
            return ErpApInvoiceMatchStatusEnum.EXCEPTION.getStatus();
        }
        return ErpApInvoiceMatchStatusEnum.PARTIAL.getStatus();
    }

    private void refreshStatementInvoiceSummary(Collection<Long> statementIds) {
        if (CollUtil.isEmpty(statementIds)) {
            return;
        }
        List<ErpApStatementDO> statements = ObjectUtil.defaultIfNull(erpApStatementMapper.selectBatchIds(statementIds), Collections.emptyList());
        if (CollUtil.isEmpty(statements)) {
            return;
        }
        Map<Long, List<ErpApInvoiceMatchItemDO>> statementItemMap = convertMultiMap(
                erpApInvoiceMatchItemMapper.selectActiveListByStatementIds(statementIds),
                ErpApInvoiceMatchItemDO::getApStatementId);
        List<ErpApInvoiceDO> invoiceList = ObjectUtil.defaultIfNull(erpApInvoiceMapper.selectBatchIds(
                convertSet(statementItemMap.values().stream().flatMap(Collection::stream).toList(),
                        ErpApInvoiceMatchItemDO::getInvoiceId)), Collections.emptyList());
        Map<Long, ErpApInvoiceDO> invoiceMap = convertMap(invoiceList, ErpApInvoiceDO::getId);
        for (ErpApStatementDO statement : statements) {
            List<ErpApInvoiceMatchItemDO> activeItems = statementItemMap.getOrDefault(statement.getId(), Collections.emptyList());
            BigDecimal matchedAmount = sumMatchAmount(activeItems);
            Integer oldInvoiceStatus = statement.getInvoiceStatus();
            Integer invoiceStatus;
            String invoiceNo;
            BigDecimal invoiceAmount;
            Long reverseSourceId;
            if (activeItems.isEmpty()) {
                invoiceStatus = ErpApInvoiceStatusEnum.NONE.getStatus();
                invoiceNo = null;
                invoiceAmount = null;
                reverseSourceId = null;
            } else {
                invoiceStatus = matchedAmount.compareTo(defaultAmount(statement.getAmount())) >= 0
                        ? ErpApInvoiceStatusEnum.RECEIVED.getStatus()
                        : ErpApInvoiceStatusEnum.PARTIAL.getStatus();
                LinkedHashSet<String> invoiceNos = activeItems.stream()
                        .map(ErpApInvoiceMatchItemDO::getInvoiceId)
                        .map(invoiceMap::get)
                        .filter(Objects::nonNull)
                        .map(ErpApInvoiceDO::getInvoiceNo)
                        .filter(StrUtil::isNotBlank)
                        .collect(Collectors.toCollection(LinkedHashSet::new));
                LinkedHashSet<Long> invoiceIds = activeItems.stream()
                        .map(ErpApInvoiceMatchItemDO::getInvoiceId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toCollection(LinkedHashSet::new));
                invoiceNo = invoiceNos.size() == 1
                        ? invoiceNos.iterator().next()
                        : (invoiceNos.isEmpty() ? null : "多张发票");
                invoiceAmount = matchedAmount;
                reverseSourceId = invoiceIds.size() == 1 ? invoiceIds.iterator().next() : null;
            }
            erpApStatementMapper.updateInvoiceById(statement.getId(), invoiceStatus, invoiceNo, invoiceAmount);
            apEstimateService.syncByStatementInvoiceChange(statement, oldInvoiceStatus, invoiceStatus,
                    null, reverseSourceId, invoiceNo);
        }
    }

    private void createStatementMatchLogs(Map<Long, BigDecimal> statementDeltaAmountMap, ErpApInvoiceDO invoice, Integer itemType) {
        if (statementDeltaAmountMap.isEmpty()) {
            return;
        }
        List<ErpApStatementDO> statements = ObjectUtil.defaultIfNull(
                erpApStatementMapper.selectBatchIds(statementDeltaAmountMap.keySet()), Collections.emptyList());
        Map<Long, ErpApStatementDO> statementMap = convertMap(statements, ErpApStatementDO::getId);
        statementDeltaAmountMap.forEach((statementId, amount) -> {
            if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) {
                return;
            }
            ErpApStatementDO statement = statementMap.get(statementId);
            if (statement == null) {
                return;
            }
            erpApStatementItemMapper.insert(new ErpApStatementItemDO()
                    .setStatementId(statementId)
                    .setItemType(itemType)
                    .setRefId(invoice.getId())
                    .setRefNo(invoice.getInvoiceNo())
                    .setAmount(amount)
                    .setAfterPaidAmount(defaultAmount(statement.getPaidAmount()))
                    .setAfterRemainAmount(defaultAmount(statement.getRemainAmount()))
                    .setRemark(buildStatementMatchRemark(itemType, invoice.getInvoiceNo())));
        });
    }

    private String buildStatementMatchRemark(Integer itemType, String invoiceNo) {
        if (ErpApStatementItemTypeEnum.INVOICE_MATCH_CANCELED.getStatus().equals(itemType)) {
            return "撤销发票匹配：" + invoiceNo;
        }
        return "发票匹配：" + invoiceNo;
    }

    private BigDecimal sumMatchCount(List<ErpApInvoiceMatchItemDO> items) {
        if (CollUtil.isEmpty(items)) {
            return BigDecimal.ZERO;
        }
        return items.stream().map(ErpApInvoiceMatchItemDO::getMatchCount)
                .filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumMatchAmount(List<ErpApInvoiceMatchItemDO> items) {
        if (CollUtil.isEmpty(items)) {
            return BigDecimal.ZERO;
        }
        return items.stream().map(ErpApInvoiceMatchItemDO::getMatchAmount)
                .filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal defaultAmount(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }

    private BigDecimal defaultTolerance(BigDecimal amount) {
        return amount == null ? DEFAULT_TOLERANCE_AMOUNT : amount;
    }

    private BigDecimal safeSubtract(BigDecimal left, BigDecimal right) {
        BigDecimal result = defaultAmount(left).subtract(defaultAmount(right));
        return result.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : result;
    }

    @Override
    public BigDecimal getAmountByInvoiceNo(String invoiceNo) {
        ErpApInvoiceDO invoice = erpApInvoiceMapper.selectByInvoiceNo(invoiceNo);
        return invoice != null ? invoice.getTotalAmount() : null;
    }

}
