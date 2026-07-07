package cn.weitee.erp.module.erp.service.finance;

import cn.hutool.core.collection.CollUtil;
import cn.weitee.erp.framework.common.enums.CommonStatusEnum;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.voucher.ErpFinanceVoucherIntegrityCheckRespVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.voucher.ErpFinanceVoucherIntegrityCheckRespVO.MissingVoucherItem;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.voucher.ErpFinanceVoucherIntegrityCheckRespVO.UnbalancedVoucherItem;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceLedgerDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceVoucherDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceVoucherEntryDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseInDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseReturnDO;
import cn.weitee.erp.module.erp.dal.dataobject.sale.ErpSaleOutDO;
import cn.weitee.erp.module.erp.dal.dataobject.sale.ErpSaleReturnDO;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceVoucherEntryMapper;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceVoucherMapper;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseInMapper;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseReturnMapper;
import cn.weitee.erp.module.erp.dal.mysql.sale.ErpSaleOutMapper;
import cn.weitee.erp.module.erp.dal.mysql.sale.ErpSaleReturnMapper;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.enums.ErpFinanceVoucherStatusEnum;
import cn.weitee.erp.module.erp.enums.common.ErpBizTypeEnum;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 凭证完整性检查 Service 实现类
 */
@Service
@Validated
@Slf4j
public class ErpFinanceVoucherIntegrityServiceImpl implements ErpFinanceVoucherIntegrityService {

    @Resource
    private ErpFinanceVoucherMapper voucherMapper;
    @Resource
    private ErpFinanceVoucherEntryMapper voucherEntryMapper;
    @Resource
    private ErpFinanceLedgerService financeLedgerService;
    @Resource
    private ErpFinanceVoucherService financeVoucherService;

    @Resource
    private ErpSaleOutMapper saleOutMapper;
    @Resource
    private ErpSaleReturnMapper saleReturnMapper;
    @Resource
    private ErpPurchaseInMapper purchaseInMapper;
    @Resource
    private ErpPurchaseReturnMapper purchaseReturnMapper;

    @Override
    public ErpFinanceVoucherIntegrityCheckRespVO checkIntegrity(Integer bizType, Long ledgerId,
                                                                 LocalDate startDate, LocalDate endDate) {
        List<MissingVoucherItem> missingVouchers = new ArrayList<>();
        List<UnbalancedVoucherItem> unbalancedVouchers = new ArrayList<>();

        // 1. 检查缺失凭证
        missingVouchers.addAll(checkMissingVouchers(bizType, ledgerId, startDate, endDate));

        // 2. 检查借贷不平衡
        unbalancedVouchers.addAll(checkUnbalancedVouchers(ledgerId));

        // 3. 构建结果
        ErpFinanceVoucherIntegrityCheckRespVO result = new ErpFinanceVoucherIntegrityCheckRespVO();
        result.setMissingVouchers(missingVouchers);
        result.setUnbalancedVouchers(unbalancedVouchers);
        result.setMissingCount(missingVouchers.size());
        result.setUnbalancedCount(unbalancedVouchers.size());
        return result;
    }

    /**
     * 检查缺失凭证的业务单据
     */
    private List<MissingVoucherItem> checkMissingVouchers(Integer bizType, Long ledgerId,
                                                           LocalDate startDate, LocalDate endDate) {
        List<MissingVoucherItem> missingList = new ArrayList<>();
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        // 获取要检查的账簿列表
        List<Long> ledgerIds = resolveLedgerIds(ledgerId);

        switch (bizType) {
            case 21: // SALE_OUT
                missingList.addAll(checkSaleOutMissingVouchers(ledgerIds, start, end));
                break;
            case 22: // SALE_RETURN
                missingList.addAll(checkSaleReturnMissingVouchers(ledgerIds, start, end));
                break;
            case 11: // PURCHASE_IN
                missingList.addAll(checkPurchaseInMissingVouchers(ledgerIds, start, end));
                break;
            case 12: // PURCHASE_RETURN
                missingList.addAll(checkPurchaseReturnMissingVouchers(ledgerIds, start, end));
                break;
            default:
                log.warn("[checkMissingVouchers] 不支持的业务类型: {}", bizType);
                break;
        }
        return missingList;
    }

    /**
     * 检查销售出库缺失凭证
     */
    private List<MissingVoucherItem> checkSaleOutMissingVouchers(List<Long> ledgerIds,
                                                                  LocalDateTime start, LocalDateTime end) {
        List<MissingVoucherItem> missingList = new ArrayList<>();
        // 查询已审核的销售出库单
        List<ErpSaleOutDO> approvedList = saleOutMapper.selectList(
                new LambdaQueryWrapper<ErpSaleOutDO>()
                        .eq(ErpSaleOutDO::getStatus, ErpAuditStatus.APPROVE.getStatus())
                        .between(ErpSaleOutDO::getOutTime, start, end)
                        .orderByAsc(ErpSaleOutDO::getId));
        Map<Long, String> ledgerNameMap = resolveLedgerNameMap(ledgerIds);
        Map<String, Boolean> existingVoucherMap = buildExistingVoucherMap(ledgerIds,
                ErpBizTypeEnum.SALE_OUT.getType(), approvedList, ErpSaleOutDO::getId);
        appendMissingVoucherItems(missingList, ledgerIds, approvedList, existingVoucherMap, ledgerNameMap,
                ErpBizTypeEnum.SALE_OUT, ErpSaleOutDO::getId, ErpSaleOutDO::getNo);
        return missingList;
    }

    /**
     * 检查销售退货缺失凭证
     */
    private List<MissingVoucherItem> checkSaleReturnMissingVouchers(List<Long> ledgerIds,
                                                                     LocalDateTime start, LocalDateTime end) {
        List<MissingVoucherItem> missingList = new ArrayList<>();
        List<ErpSaleReturnDO> approvedList = saleReturnMapper.selectList(
                new LambdaQueryWrapper<ErpSaleReturnDO>()
                        .eq(ErpSaleReturnDO::getStatus, ErpAuditStatus.APPROVE.getStatus())
                        .between(ErpSaleReturnDO::getReturnTime, start, end)
                        .orderByAsc(ErpSaleReturnDO::getId));
        Map<Long, String> ledgerNameMap = resolveLedgerNameMap(ledgerIds);
        Map<String, Boolean> existingVoucherMap = buildExistingVoucherMap(ledgerIds,
                ErpBizTypeEnum.SALE_RETURN.getType(), approvedList, ErpSaleReturnDO::getId);
        appendMissingVoucherItems(missingList, ledgerIds, approvedList, existingVoucherMap, ledgerNameMap,
                ErpBizTypeEnum.SALE_RETURN, ErpSaleReturnDO::getId, ErpSaleReturnDO::getNo);
        return missingList;
    }

    /**
     * 检查采购入库缺失凭证
     */
    private List<MissingVoucherItem> checkPurchaseInMissingVouchers(List<Long> ledgerIds,
                                                                     LocalDateTime start, LocalDateTime end) {
        List<MissingVoucherItem> missingList = new ArrayList<>();
        List<ErpPurchaseInDO> approvedList = purchaseInMapper.selectList(
                new LambdaQueryWrapper<ErpPurchaseInDO>()
                        .eq(ErpPurchaseInDO::getStatus, ErpAuditStatus.APPROVE.getStatus())
                        .between(ErpPurchaseInDO::getInTime, start, end)
                        .orderByAsc(ErpPurchaseInDO::getId));
        Map<Long, String> ledgerNameMap = resolveLedgerNameMap(ledgerIds);
        Map<String, Boolean> existingVoucherMap = buildExistingVoucherMap(ledgerIds,
                ErpBizTypeEnum.PURCHASE_IN.getType(), approvedList, ErpPurchaseInDO::getId);
        appendMissingVoucherItems(missingList, ledgerIds, approvedList, existingVoucherMap, ledgerNameMap,
                ErpBizTypeEnum.PURCHASE_IN, ErpPurchaseInDO::getId, ErpPurchaseInDO::getNo);
        return missingList;
    }

    /**
     * 检查采购退货缺失凭证
     */
    private List<MissingVoucherItem> checkPurchaseReturnMissingVouchers(List<Long> ledgerIds,
                                                                         LocalDateTime start, LocalDateTime end) {
        List<MissingVoucherItem> missingList = new ArrayList<>();
        List<ErpPurchaseReturnDO> approvedList = purchaseReturnMapper.selectList(
                new LambdaQueryWrapper<ErpPurchaseReturnDO>()
                        .eq(ErpPurchaseReturnDO::getStatus, ErpAuditStatus.APPROVE.getStatus())
                        .between(ErpPurchaseReturnDO::getReturnTime, start, end)
                        .orderByAsc(ErpPurchaseReturnDO::getId));
        Map<Long, String> ledgerNameMap = resolveLedgerNameMap(ledgerIds);
        Map<String, Boolean> existingVoucherMap = buildExistingVoucherMap(ledgerIds,
                ErpBizTypeEnum.PURCHASE_RETURN.getType(), approvedList, ErpPurchaseReturnDO::getId);
        appendMissingVoucherItems(missingList, ledgerIds, approvedList, existingVoucherMap, ledgerNameMap,
                ErpBizTypeEnum.PURCHASE_RETURN, ErpPurchaseReturnDO::getId, ErpPurchaseReturnDO::getNo);
        return missingList;
    }

    private Map<Long, String> resolveLedgerNameMap(List<Long> ledgerIds) {
        Map<Long, ErpFinanceLedgerDO> ledgerMap = financeLedgerService.getFinanceLedgerMap(ledgerIds);
        Map<Long, String> ledgerNameMap = new HashMap<>();
        for (Long ledgerId : ledgerIds) {
            ErpFinanceLedgerDO ledger = ledgerMap.get(ledgerId);
            ledgerNameMap.put(ledgerId, ledger != null ? ledger.getName() : String.valueOf(ledgerId));
        }
        return ledgerNameMap;
    }

    private <T> Map<String, Boolean> buildExistingVoucherMap(List<Long> ledgerIds,
                                                             Integer bizType,
                                                             List<T> documents,
                                                             java.util.function.Function<T, Long> idGetter) {
        if (documents == null || documents.isEmpty()) {
            return Collections.emptyMap();
        }
        Set<Long> bizIds = documents.stream()
                .map(idGetter)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
        List<ErpFinanceVoucherDO> vouchers = voucherMapper.selectListByLedgerIdsAndBizTypeAndBizIds(
                ledgerIds, bizType, bizIds);
        return vouchers.stream().collect(Collectors.toMap(
                voucher -> buildVoucherPresenceKey(voucher.getLedgerId(), voucher.getBizId()),
                voucher -> Boolean.TRUE,
                (left, right) -> left));
    }

    private <T> void appendMissingVoucherItems(List<MissingVoucherItem> missingList,
                                               List<Long> ledgerIds,
                                               List<T> documents,
                                               Map<String, Boolean> existingVoucherMap,
                                               Map<Long, String> ledgerNameMap,
                                               ErpBizTypeEnum bizTypeEnum,
                                               java.util.function.Function<T, Long> idGetter,
                                               java.util.function.Function<T, String> noGetter) {
        for (T document : documents) {
            Long bizId = idGetter.apply(document);
            if (bizId == null) {
                continue;
            }
            for (Long ledgerId : ledgerIds) {
                if (existingVoucherMap.containsKey(buildVoucherPresenceKey(ledgerId, bizId))) {
                    continue;
                }
                missingList.add(new MissingVoucherItem(
                        bizTypeEnum.getType(),
                        bizTypeEnum.getName(),
                        bizId,
                        noGetter.apply(document),
                        "已审核",
                        ledgerNameMap.getOrDefault(ledgerId, String.valueOf(ledgerId))
                ));
            }
        }
    }

    private String buildVoucherPresenceKey(Long ledgerId, Long bizId) {
        return ledgerId + ":" + bizId;
    }

    /**
     * 检查借贷不平衡的凭证
     */
    private List<UnbalancedVoucherItem> checkUnbalancedVouchers(Long ledgerId) {
        List<UnbalancedVoucherItem> unbalancedList = new ArrayList<>();

        // 查询所有非作废的凭证
        LambdaQueryWrapper<ErpFinanceVoucherDO> query = new LambdaQueryWrapper<ErpFinanceVoucherDO>()
                .ne(ErpFinanceVoucherDO::getStatus, ErpFinanceVoucherStatusEnum.VOIDED.getStatus())
                .ne(ErpFinanceVoucherDO::getStatus, ErpFinanceVoucherStatusEnum.REVERSED.getStatus());
        if (ledgerId != null) {
            query.eq(ErpFinanceVoucherDO::getLedgerId, ledgerId);
        }
        List<ErpFinanceVoucherDO> voucherList = voucherMapper.selectList(query);

        // 批量查询所有凭证分录
        Set<Long> voucherIds = voucherList.stream()
                .map(ErpFinanceVoucherDO::getId)
                .collect(Collectors.toSet());
        if (CollUtil.isEmpty(voucherIds)) {
            return unbalancedList;
        }
        List<ErpFinanceVoucherEntryDO> allEntries = voucherEntryMapper.selectList(
                new LambdaQueryWrapper<ErpFinanceVoucherEntryDO>()
                        .in(ErpFinanceVoucherEntryDO::getVoucherId, voucherIds));
        Map<Long, List<ErpFinanceVoucherEntryDO>> entryMap = allEntries.stream()
                .collect(Collectors.groupingBy(ErpFinanceVoucherEntryDO::getVoucherId));

        // 检查每张凭证的借贷平衡
        for (ErpFinanceVoucherDO voucher : voucherList) {
            List<ErpFinanceVoucherEntryDO> entries = entryMap.get(voucher.getId());
            if (CollUtil.isEmpty(entries)) {
                continue;
            }
            BigDecimal totalDebit = entries.stream()
                    .map(e -> e.getDebitAmount() != null ? e.getDebitAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalCredit = entries.stream()
                    .map(e -> e.getCreditAmount() != null ? e.getCreditAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal difference = totalDebit.subtract(totalCredit);

            if (difference.compareTo(BigDecimal.ZERO) != 0) {
                unbalancedList.add(new UnbalancedVoucherItem(
                        voucher.getId(),
                        voucher.getVoucherNo(),
                        totalDebit,
                        totalCredit,
                        difference
                ));
            }
        }
        return unbalancedList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchRecomputeVouchers(Integer bizType, Long ledgerId,
                                       LocalDate startDate, LocalDate endDate,
                                       Long userId, String remark) {
        // 1. 先检查缺失凭证
        List<MissingVoucherItem> missingList = checkMissingVouchers(bizType, ledgerId, startDate, endDate);
        if (CollUtil.isEmpty(missingList)) {
            log.info("[batchRecomputeVouchers] 没有缺失的凭证，bizType={}, ledgerId={}", bizType, ledgerId);
            return 0;
        }

        // 2. 批量重算
        int successCount = 0;
        for (MissingVoucherItem item : missingList) {
            try {
                Long voucherId = financeVoucherService.recomputeAutoGeneratedVoucher(
                        item.getBizType(), item.getBizId(), userId,
                        remark != null ? remark : "批量重算补生成凭证");
                if (voucherId != null) {
                    successCount++;
                }
            } catch (Exception e) {
                log.error("[batchRecomputeVouchers] 重算失败，bizType={}, bizId={}",
                        item.getBizType(), item.getBizId(), e);
            }
        }

        log.info("[batchRecomputeVouchers] 批量重算完成，bizType={}, 总数={}, 成功={}",
                bizType, missingList.size(), successCount);
        return successCount;
    }

    /**
     * 解析要检查的账簿ID列表
     */
    private List<Long> resolveLedgerIds(Long ledgerId) {
        if (ledgerId != null) {
            return List.of(ledgerId);
        }
        // 查询所有启用的账簿
        List<ErpFinanceLedgerDO> ledgerList = financeLedgerService.getFinanceLedgerListByStatus(
                CommonStatusEnum.ENABLE.getStatus());
        return ledgerList.stream()
                .map(ErpFinanceLedgerDO::getId)
                .collect(Collectors.toList());
    }

}
