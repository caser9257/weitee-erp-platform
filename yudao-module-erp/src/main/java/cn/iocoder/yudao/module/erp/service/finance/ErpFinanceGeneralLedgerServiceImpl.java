package cn.iocoder.yudao.module.erp.service.finance;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.generalledger.ErpFinanceGeneralLedgerRebuildReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.generalledger.ErpFinanceGeneralLedgerRebuildRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.generalledger.ErpFinanceGeneralLedgerDetailReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.generalledger.ErpFinanceGeneralLedgerDetailRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.generalledger.ErpFinanceSubjectBalancePageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceLedgerDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinancePeriodDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceSubjectBalanceDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceVoucherDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceVoucherEntryDO;
import cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpFinanceSubjectBalanceMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpFinanceVoucherEntryMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpFinanceVoucherMapper;
import cn.iocoder.yudao.module.erp.enums.ErpFinanceVoucherStatusEnum;
import cn.iocoder.yudao.module.erp.enums.common.ErpBizTypeEnum;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstantsFinanceLedger.FINANCE_PERIOD_NOT_EXISTS;

@Service
@Validated
public class ErpFinanceGeneralLedgerServiceImpl implements ErpFinanceGeneralLedgerService {

    @Resource
    private ErpFinanceSubjectBalanceMapper erpFinanceSubjectBalanceMapper;
    @Resource
    private ErpFinanceVoucherMapper erpFinanceVoucherMapper;
    @Resource
    private ErpFinanceVoucherEntryMapper erpFinanceVoucherEntryMapper;
    @Resource
    private ErpFinanceLedgerService financeLedgerService;
    @Resource
    private ErpFinancePeriodService financePeriodService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applyPostedVoucher(ErpFinanceVoucherDO voucher, List<ErpFinanceVoucherEntryDO> entries) {
        if (voucher == null || CollUtil.isEmpty(entries)) {
            return;
        }
        applyVoucherMovement(voucher, entries, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rollbackPostedVoucher(ErpFinanceVoucherDO voucher, List<ErpFinanceVoucherEntryDO> entries) {
        if (voucher == null || CollUtil.isEmpty(entries)) {
            return;
        }
        applyVoucherMovement(voucher, entries, true);
    }

    @Override
    public PageResult<ErpFinanceSubjectBalanceDO> getSubjectBalancePage(ErpFinanceSubjectBalancePageReqVO reqVO) {
        financeLedgerService.validateFinanceLedger(reqVO.getLedgerId());
        validatePeriod(reqVO.getLedgerId(), reqVO.getPeriodId());
        return erpFinanceSubjectBalanceMapper.selectPage(reqVO);
    }

    @Override
    public ErpFinanceGeneralLedgerDetailRespVO getGeneralLedgerDetail(ErpFinanceGeneralLedgerDetailReqVO reqVO) {
        ErpFinanceLedgerDO ledger = financeLedgerService.validateFinanceLedger(reqVO.getLedgerId());
        ErpFinancePeriodDO period = validatePeriod(reqVO.getLedgerId(), reqVO.getPeriodId());
        ErpFinanceSubjectBalanceDO balance = erpFinanceSubjectBalanceMapper.selectByLedgerIdAndPeriodIdAndSubjectCode(
                reqVO.getLedgerId(), reqVO.getPeriodId(), reqVO.getSubjectCode());
        List<ErpFinanceVoucherDO> voucherList = erpFinanceVoucherMapper.selectPostedListByLedgerIdAndPeriodId(
                reqVO.getLedgerId(), reqVO.getPeriodId());
        List<LedgerEntryRow> rows = buildLedgerEntryRows(voucherList, reqVO.getSubjectCode());

        BalanceSnapshot openingSnapshot = new BalanceSnapshot(
                balance == null ? BigDecimal.ZERO : defaultAmount(balance.getOpeningDebitAmount()),
                balance == null ? BigDecimal.ZERO : defaultAmount(balance.getOpeningCreditAmount()));
        LocalDateTime startTime = reqVO.getVoucherTime() != null && reqVO.getVoucherTime().length > 0 ? reqVO.getVoucherTime()[0] : null;
        LocalDateTime endTime = reqVO.getVoucherTime() != null && reqVO.getVoucherTime().length > 1 ? reqVO.getVoucherTime()[1] : null;

        for (LedgerEntryRow row : rows) {
            if (startTime != null && row.voucher.getVoucherTime().isBefore(startTime)) {
                openingSnapshot.apply(row.entry.getDebitAmount(), row.entry.getCreditAmount());
            }
        }

        BalanceSnapshot runningSnapshot = openingSnapshot.copy();
        BigDecimal totalDebitAmount = BigDecimal.ZERO;
        BigDecimal totalCreditAmount = BigDecimal.ZERO;
        List<ErpFinanceGeneralLedgerDetailRespVO.Item> items = new ArrayList<>();
        for (LedgerEntryRow row : rows) {
            LocalDateTime voucherTime = row.voucher.getVoucherTime();
            if (startTime != null && voucherTime.isBefore(startTime)) {
                continue;
            }
            if (endTime != null && voucherTime.isAfter(endTime)) {
                continue;
            }
            runningSnapshot.apply(row.entry.getDebitAmount(), row.entry.getCreditAmount());
            totalDebitAmount = totalDebitAmount.add(defaultAmount(row.entry.getDebitAmount()));
            totalCreditAmount = totalCreditAmount.add(defaultAmount(row.entry.getCreditAmount()));
            items.add(buildDetailItem(row.voucher, row.entry, runningSnapshot));
        }

        ErpFinanceGeneralLedgerDetailRespVO respVO = new ErpFinanceGeneralLedgerDetailRespVO();
        respVO.setLedgerId(ledger.getId());
        respVO.setLedgerName(ledger.getName());
        respVO.setPeriodId(period.getId());
        respVO.setPeriodCode(period.getPeriodCode());
        respVO.setSubjectCode(reqVO.getSubjectCode());
        respVO.setSubjectName(resolveSubjectName(balance, rows));
        respVO.setOpeningDebitAmount(openingSnapshot.debitAmount);
        respVO.setOpeningCreditAmount(openingSnapshot.creditAmount);
        respVO.setTotalDebitAmount(totalDebitAmount);
        respVO.setTotalCreditAmount(totalCreditAmount);
        respVO.setEndingDebitAmount(runningSnapshot.debitAmount);
        respVO.setEndingCreditAmount(runningSnapshot.creditAmount);
        respVO.setItems(items);
        return respVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ErpFinanceGeneralLedgerRebuildRespVO rebuildSubjectBalance(ErpFinanceGeneralLedgerRebuildReqVO reqVO) {
        financeLedgerService.validateFinanceLedger(reqVO.getLedgerId());
        List<ErpFinanceVoucherDO> vouchers = erpFinanceVoucherMapper.selectPostedListByLedgerId(reqVO.getLedgerId());
        erpFinanceSubjectBalanceMapper.deleteByLedgerId(reqVO.getLedgerId());
        int entryCount = 0;
        Set<String> subjectCodes = new java.util.HashSet<>();
        for (ErpFinanceVoucherDO voucher : vouchers) {
            List<ErpFinanceVoucherEntryDO> entries = erpFinanceVoucherEntryMapper.selectListByVoucherId(voucher.getId());
            entryCount += CollUtil.size(entries);
            subjectCodes.addAll(entries.stream().map(ErpFinanceVoucherEntryDO::getSubjectCode).collect(Collectors.toSet()));
            applyPostedVoucher(voucher, entries);
        }
        ErpFinanceGeneralLedgerRebuildRespVO respVO = new ErpFinanceGeneralLedgerRebuildRespVO();
        respVO.setLedgerId(reqVO.getLedgerId());
        respVO.setVoucherCount(vouchers.size());
        respVO.setEntryCount(entryCount);
        respVO.setSubjectCount(subjectCodes.size());
        return respVO;
    }

    private void applyVoucherMovement(ErpFinanceVoucherDO voucher, List<ErpFinanceVoucherEntryDO> entries, boolean rollback) {
        ErpFinancePeriodDO period = validatePeriod(voucher.getLedgerId(), voucher.getPeriodId());
        Map<String, SubjectMovement> movementMap = new LinkedHashMap<>();
        for (ErpFinanceVoucherEntryDO entry : entries) {
            SubjectMovement movement = movementMap.computeIfAbsent(entry.getSubjectCode(),
                    key -> new SubjectMovement(key, entry.getSubjectName()));
            movement.subjectName = ObjectUtil.defaultIfBlank(movement.subjectName, entry.getSubjectName());
            movement.debitAmount = movement.debitAmount.add(defaultAmount(entry.getDebitAmount()));
            movement.creditAmount = movement.creditAmount.add(defaultAmount(entry.getCreditAmount()));
        }
        for (SubjectMovement movement : movementMap.values()) {
            if (rollback) {
                movement.debitAmount = movement.debitAmount.negate();
                movement.creditAmount = movement.creditAmount.negate();
            }
            upsertAndCascadeBalance(voucher.getLedgerId(), period, voucher.getVoucherNo(), movement);
        }
    }

    private void upsertAndCascadeBalance(Long ledgerId, ErpFinancePeriodDO period, String voucherNo, SubjectMovement movement) {
        ErpFinanceSubjectBalanceDO balance = erpFinanceSubjectBalanceMapper.selectByLedgerIdAndPeriodIdAndSubjectCode(
                ledgerId, period.getId(), movement.subjectCode);
        if (balance == null) {
            if (movement.debitAmount.compareTo(BigDecimal.ZERO) < 0 || movement.creditAmount.compareTo(BigDecimal.ZERO) < 0) {
                throw exception(cn.iocoder.yudao.module.erp.enums.ErrorCodeConstantsFinanceVoucher.FINANCE_VOUCHER_CANCEL_POST_BALANCE_NOT_EXISTS,
                        voucherNo);
            }
            ErpFinanceSubjectBalanceDO previousBalance = erpFinanceSubjectBalanceMapper.selectLatestBeforePeriod(
                    ledgerId, movement.subjectCode, period.getPeriodSort());
            balance = new ErpFinanceSubjectBalanceDO()
                    .setLedgerId(ledgerId)
                    .setPeriodId(period.getId())
                    .setPeriodSort(period.getPeriodSort())
                    .setSubjectCode(movement.subjectCode)
                    .setSubjectName(movement.subjectName)
                    .setOpeningDebitAmount(previousBalance == null ? BigDecimal.ZERO : defaultAmount(previousBalance.getEndingDebitAmount()))
                    .setOpeningCreditAmount(previousBalance == null ? BigDecimal.ZERO : defaultAmount(previousBalance.getEndingCreditAmount()))
                    .setCurrentDebitAmount(movement.debitAmount)
                    .setCurrentCreditAmount(movement.creditAmount);
            recalculateEnding(balance);
            erpFinanceSubjectBalanceMapper.insert(balance);
        } else {
            balance.setSubjectName(ObjectUtil.defaultIfBlank(movement.subjectName, balance.getSubjectName()));
            balance.setCurrentDebitAmount(defaultAmount(balance.getCurrentDebitAmount()).add(movement.debitAmount));
            balance.setCurrentCreditAmount(defaultAmount(balance.getCurrentCreditAmount()).add(movement.creditAmount));
            recalculateEnding(balance);
            erpFinanceSubjectBalanceMapper.updateById(balance);
        }
        cascadeLaterBalances(ledgerId, movement.subjectCode, period.getPeriodSort(),
                balance.getEndingDebitAmount(), balance.getEndingCreditAmount());
    }

    private void cascadeLaterBalances(Long ledgerId, String subjectCode, Integer periodSort,
                                      BigDecimal openingDebitAmount, BigDecimal openingCreditAmount) {
        List<ErpFinanceSubjectBalanceDO> laterBalances = erpFinanceSubjectBalanceMapper.selectListAfterPeriod(ledgerId, subjectCode, periodSort);
        BigDecimal nextOpeningDebitAmount = defaultAmount(openingDebitAmount);
        BigDecimal nextOpeningCreditAmount = defaultAmount(openingCreditAmount);
        for (ErpFinanceSubjectBalanceDO laterBalance : laterBalances) {
            laterBalance.setOpeningDebitAmount(nextOpeningDebitAmount);
            laterBalance.setOpeningCreditAmount(nextOpeningCreditAmount);
            recalculateEnding(laterBalance);
            erpFinanceSubjectBalanceMapper.updateById(laterBalance);
            nextOpeningDebitAmount = defaultAmount(laterBalance.getEndingDebitAmount());
            nextOpeningCreditAmount = defaultAmount(laterBalance.getEndingCreditAmount());
        }
    }

    private List<LedgerEntryRow> buildLedgerEntryRows(List<ErpFinanceVoucherDO> voucherList, String subjectCode) {
        if (CollUtil.isEmpty(voucherList)) {
            return Collections.emptyList();
        }
        List<Long> voucherIds = voucherList.stream().map(ErpFinanceVoucherDO::getId).toList();
        List<ErpFinanceVoucherEntryDO> entryList = erpFinanceVoucherEntryMapper.selectListByVoucherIdsAndSubjectCode(voucherIds, subjectCode);
        if (CollUtil.isEmpty(entryList)) {
            return Collections.emptyList();
        }
        Map<Long, List<ErpFinanceVoucherEntryDO>> entryMap = new LinkedHashMap<>();
        for (ErpFinanceVoucherEntryDO entry : entryList) {
            entryMap.computeIfAbsent(entry.getVoucherId(), key -> new ArrayList<>()).add(entry);
        }
        Map<Long, ErpFinanceVoucherDO> voucherMap = convertMap(voucherList, ErpFinanceVoucherDO::getId);
        List<LedgerEntryRow> rows = new ArrayList<>();
        for (Long voucherId : voucherIds) {
            List<ErpFinanceVoucherEntryDO> entries = entryMap.get(voucherId);
            if (CollUtil.isEmpty(entries)) {
                continue;
            }
            ErpFinanceVoucherDO voucher = voucherMap.get(voucherId);
            for (ErpFinanceVoucherEntryDO entry : entries) {
                rows.add(new LedgerEntryRow(voucher, entry));
            }
        }
        return rows;
    }

    private ErpFinanceGeneralLedgerDetailRespVO.Item buildDetailItem(ErpFinanceVoucherDO voucher,
                                                                     ErpFinanceVoucherEntryDO entry,
                                                                     BalanceSnapshot runningSnapshot) {
        ErpFinanceGeneralLedgerDetailRespVO.Item item = new ErpFinanceGeneralLedgerDetailRespVO.Item();
        item.setVoucherId(voucher.getId());
        item.setVoucherNo(voucher.getVoucherNo());
        item.setVoucherTime(voucher.getVoucherTime());
        item.setBizType(voucher.getBizType());
        item.setBizTypeName(resolveBizTypeName(voucher.getBizType()));
        item.setBizNo(voucher.getBizNo());
        item.setVoucherStatus(voucher.getStatus());
        item.setVoucherStatusName(resolveVoucherStatusName(voucher.getStatus()));
        item.setEntryNo(entry.getEntryNo());
        item.setSummary(entry.getSummary());
        item.setDebitAmount(defaultAmount(entry.getDebitAmount()));
        item.setCreditAmount(defaultAmount(entry.getCreditAmount()));
        item.setRunningDebitAmount(runningSnapshot.debitAmount);
        item.setRunningCreditAmount(runningSnapshot.creditAmount);
        return item;
    }

    private String resolveSubjectName(ErpFinanceSubjectBalanceDO balance, List<LedgerEntryRow> rows) {
        if (balance != null && ObjectUtil.isNotEmpty(balance.getSubjectName())) {
            return balance.getSubjectName();
        }
        if (CollUtil.isNotEmpty(rows)) {
            return rows.get(0).entry.getSubjectName();
        }
        return null;
    }

    private void recalculateEnding(ErpFinanceSubjectBalanceDO balance) {
        BigDecimal diffAmount = defaultAmount(balance.getOpeningDebitAmount())
                .subtract(defaultAmount(balance.getOpeningCreditAmount()))
                .add(defaultAmount(balance.getCurrentDebitAmount()))
                .subtract(defaultAmount(balance.getCurrentCreditAmount()));
        if (diffAmount.compareTo(BigDecimal.ZERO) >= 0) {
            balance.setEndingDebitAmount(diffAmount);
            balance.setEndingCreditAmount(BigDecimal.ZERO);
            return;
        }
        balance.setEndingDebitAmount(BigDecimal.ZERO);
        balance.setEndingCreditAmount(diffAmount.abs());
    }

    private ErpFinancePeriodDO validatePeriod(Long ledgerId, Long periodId) {
        ErpFinancePeriodDO period = financePeriodService.getFinancePeriod(periodId);
        if (period == null || !ObjectUtil.equal(period.getLedgerId(), ledgerId)) {
            throw exception(FINANCE_PERIOD_NOT_EXISTS);
        }
        return period;
    }

    private String resolveBizTypeName(Integer bizType) {
        for (ErpBizTypeEnum value : ErpBizTypeEnum.values()) {
            if (ObjectUtil.equal(value.getType(), bizType)) {
                return value.getName();
            }
        }
        return null;
    }

    private String resolveVoucherStatusName(Integer status) {
        for (ErpFinanceVoucherStatusEnum value : ErpFinanceVoucherStatusEnum.values()) {
            if (ObjectUtil.equal(value.getStatus(), status)) {
                return value.getName();
            }
        }
        return null;
    }

    private BigDecimal defaultAmount(BigDecimal amount) {
        return ObjectUtil.defaultIfNull(amount, BigDecimal.ZERO);
    }

    private static class SubjectMovement {
        private final String subjectCode;
        private String subjectName;
        private BigDecimal debitAmount = BigDecimal.ZERO;
        private BigDecimal creditAmount = BigDecimal.ZERO;

        private SubjectMovement(String subjectCode, String subjectName) {
            this.subjectCode = subjectCode;
            this.subjectName = subjectName;
        }
    }

    private static class BalanceSnapshot {
        private BigDecimal debitAmount;
        private BigDecimal creditAmount;

        private BalanceSnapshot(BigDecimal debitAmount, BigDecimal creditAmount) {
            this.debitAmount = debitAmount;
            this.creditAmount = creditAmount;
        }

        private void apply(BigDecimal debitDelta, BigDecimal creditDelta) {
            BigDecimal diffAmount = defaultStatic(debitAmount)
                    .subtract(defaultStatic(creditAmount))
                    .add(defaultStatic(debitDelta))
                    .subtract(defaultStatic(creditDelta));
            if (diffAmount.compareTo(BigDecimal.ZERO) >= 0) {
                this.debitAmount = diffAmount;
                this.creditAmount = BigDecimal.ZERO;
                return;
            }
            this.debitAmount = BigDecimal.ZERO;
            this.creditAmount = diffAmount.abs();
        }

        private BalanceSnapshot copy() {
            return new BalanceSnapshot(defaultStatic(debitAmount), defaultStatic(creditAmount));
        }

        private static BigDecimal defaultStatic(BigDecimal amount) {
            return amount == null ? BigDecimal.ZERO : amount;
        }
    }

    private static class LedgerEntryRow {
        private final ErpFinanceVoucherDO voucher;
        private final ErpFinanceVoucherEntryDO entry;

        private LedgerEntryRow(ErpFinanceVoucherDO voucher, ErpFinanceVoucherEntryDO entry) {
            this.voucher = voucher;
            this.entry = entry;
        }
    }

}
