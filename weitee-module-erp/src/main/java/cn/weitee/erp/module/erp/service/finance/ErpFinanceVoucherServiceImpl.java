package cn.weitee.erp.module.erp.service.finance;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.voucher.ErpFinanceVoucherActionReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.voucher.ErpFinanceVoucherGenerateReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.voucher.ErpFinanceVoucherPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.voucher.ErpFinanceVoucherReverseReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceAssetDepreciationDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceLedgerDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceExpenseDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceDualLedgerConfigDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinancePeriodDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceVoucherDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceVoucherEntryDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceVoucherLogDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceVoucherTemplateDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceVoucherTemplateItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpOutsourceFeeDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionInboundDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseInDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseReturnDO;
import cn.weitee.erp.module.erp.dal.dataobject.sale.ErpSaleOutDO;
import cn.weitee.erp.module.erp.dal.dataobject.sale.ErpSaleReturnDO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockCheckDO;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceVoucherEntryMapper;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceVoucherMapper;
import cn.weitee.erp.module.erp.dal.mysql.stock.ErpStockCheckMapper;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceDualLedgerAmountDiffLogMapper;
import cn.weitee.erp.module.erp.dal.redis.no.ErpNoRedisDAO;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.enums.ErpFinanceExpenseAccountingTypeEnum;
import cn.weitee.erp.module.erp.enums.ErpFinanceExpenseTypeEnum;
import cn.weitee.erp.module.erp.enums.ErpFinanceVoucherAmountSourceEnum;
import cn.weitee.erp.module.erp.enums.ErpFinanceVoucherEntryDirectionEnum;
import cn.weitee.erp.module.erp.enums.ErpFinanceVoucherStatusEnum;
import cn.weitee.erp.module.erp.enums.common.ErpBizTypeEnum;
import cn.weitee.erp.module.erp.service.purchase.ErpPurchaseInService;
import cn.weitee.erp.module.erp.service.purchase.ErpPurchaseReturnService;
import cn.weitee.erp.module.erp.service.mrp.ErpProductionInboundService;
import cn.weitee.erp.module.erp.service.mrp.ErpOutsourceOrderService;
import cn.weitee.erp.module.erp.service.sale.ErpSaleOutService;
import cn.weitee.erp.module.erp.service.sale.ErpSaleReturnService;
import cn.weitee.erp.module.erp.service.finance.event.VoucherCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertList;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsFinanceVoucher.FINANCE_VOUCHER_ALREADY_EXISTS;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsFinanceVoucher.FINANCE_VOUCHER_APPROVE_FAIL_STATUS;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsFinanceVoucher.FINANCE_VOUCHER_CANCEL_APPROVE_FAIL_STATUS;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsFinanceVoucher.FINANCE_VOUCHER_CANCEL_POST_FAIL_STATUS;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsFinanceVoucher.FINANCE_VOUCHER_CANCEL_POST_REVERSE_NOT_ALLOWED;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsFinanceVoucher.FINANCE_VOUCHER_NOT_EXISTS;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsFinanceVoucher.FINANCE_VOUCHER_POST_FAIL_STATUS;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsFinanceVoucher.FINANCE_VOUCHER_REVERSE_ALREADY_EXISTS;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsFinanceVoucher.FINANCE_VOUCHER_REVERSE_FAIL_STATUS;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsFinanceVoucher.FINANCE_VOUCHER_REVERSE_OF_REVERSE_NOT_ALLOWED;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsFinanceVoucher.FINANCE_VOUCHER_SOURCE_NOT_SUPPORTED;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsFinanceVoucher.FINANCE_VOUCHER_SOURCE_STATUS_INVALID;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsFinanceVoucher.FINANCE_VOUCHER_SOURCE_TIME_REQUIRED;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsFinanceVoucher.FINANCE_VOUCHER_TEMPLATE_BIZ_MISMATCH;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsFinanceVoucher.FINANCE_VOUCHER_TEMPLATE_LEDGER_MISMATCH;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsFinanceVoucher.FINANCE_VOUCHER_UNBALANCED;

@Slf4j
@Service
@Validated
public class ErpFinanceVoucherServiceImpl implements ErpFinanceVoucherService {

    @Resource
    private ApplicationContext applicationContext;
    @Resource
    private ErpFinanceVoucherMapper erpFinanceVoucherMapper;
    @Resource
    private ErpFinanceVoucherEntryMapper erpFinanceVoucherEntryMapper;
    @Resource
    private ErpFinanceVoucherTemplateService voucherTemplateService;
    @Resource
    private ErpFinanceLedgerService financeLedgerService;
    @Resource
    private ErpFinanceDualLedgerConfigService dualLedgerConfigService;
    @Resource
    private ErpFinancePeriodService financePeriodService;
    @Resource
    @Lazy
    private ErpFinanceExpenseService financeExpenseService;
    @Resource
    @Lazy
    private ErpPurchaseInService purchaseInService;
    @Resource
    @Lazy
    private ErpPurchaseReturnService purchaseReturnService;
    @Resource
    @Lazy
    private ErpSaleOutService saleOutService;
    @Resource
    @Lazy
    private ErpSaleReturnService saleReturnService;
    @Resource
    @Lazy
    private ErpOutsourceOrderService outsourceOrderService;
    @Resource
    @Lazy
    private ErpProductionInboundService productionInboundService;
    @Resource
    @Lazy
    private ErpFinanceAssetDepreciationService financeAssetDepreciationService;
    @Resource
    private ErpFinanceGeneralLedgerService financeGeneralLedgerService;
    @Resource
    private ErpNoRedisDAO noRedisDAO;
    @Resource
    private ErpFinanceVoucherLogService voucherLogService;
    @Resource
    private ErpFinanceDualLedgerAmountDiffLogMapper dualLedgerAmountDiffLogMapper;
    @Resource
    private ErpStockCheckMapper stockCheckMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long generateVoucher(ErpFinanceVoucherGenerateReqVO reqVO) {
        return generateVoucher(reqVO, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long generateVoucherWithoutDualWrite(ErpFinanceVoucherGenerateReqVO reqVO) {
        return generateVoucher(reqVO, false);
    }

    private Long generateVoucher(ErpFinanceVoucherGenerateReqVO reqVO, boolean publishDualWriteEvent) {
        ErpFinanceLedgerDO ledger = financeLedgerService.validateFinanceLedger(reqVO.getLedgerId());
        ErpFinanceVoucherTemplateDO template = voucherTemplateService.validateVoucherTemplate(reqVO.getTemplateId());
        if (!ObjectUtil.equal(template.getLedgerId(), reqVO.getLedgerId())) {
            throw exception(FINANCE_VOUCHER_TEMPLATE_LEDGER_MISMATCH, template.getName());
        }
        if (!ObjectUtil.equal(template.getBizType(), reqVO.getBizType())) {
            throw exception(FINANCE_VOUCHER_TEMPLATE_BIZ_MISMATCH, template.getName());
        }

        VoucherSource source = resolveVoucherSource(reqVO.getBizType(), reqVO.getBizId());
        if (erpFinanceVoucherMapper.selectByLedgerIdAndBiz(reqVO.getLedgerId(), reqVO.getBizType(), reqVO.getBizId()) != null) {
            throw exception(FINANCE_VOUCHER_ALREADY_EXISTS, source.bizNo, ledger.getName());
        }

        LocalDateTime voucherTime = resolveVoucherTime(reqVO.getVoucherTime(), source);
        ErpFinancePeriodDO period = financePeriodService.getCurrentOpenPeriod(reqVO.getLedgerId(), voucherTime.toLocalDate());
        List<ErpFinanceVoucherTemplateItemDO> templateItems = voucherTemplateService.getVoucherTemplateItemListByTemplateId(template.getId());
        List<ErpFinanceVoucherEntryDO> entries = buildEntries(template, templateItems, source);
        BigDecimal totalDebit = sumDebit(entries);
        BigDecimal totalCredit = sumCredit(entries);
        if (totalDebit.compareTo(totalCredit) != 0) {
            throw exception(FINANCE_VOUCHER_UNBALANCED, totalDebit, totalCredit);
        }

        String voucherNo = noRedisDAO.generate(ErpNoRedisDAO.FINANCE_VOUCHER_NO_PREFIX);
        ErpFinanceVoucherDO voucher = new ErpFinanceVoucherDO()
                .setVoucherNo(voucherNo)
                .setLedgerId(reqVO.getLedgerId())
                .setPeriodId(period.getId())
                .setTemplateId(template.getId())
                .setBizType(reqVO.getBizType())
                .setBizId(reqVO.getBizId())
                .setBizNo(source.bizNo)
                .setVoucherTime(voucherTime)
                .setStatus(ErpFinanceVoucherStatusEnum.GENERATED.getStatus())
                .setTotalDebitAmount(totalDebit)
                .setTotalCreditAmount(totalCredit)
                .setRemark(ObjectUtil.defaultIfBlank(reqVO.getRemark(), source.remark));
        erpFinanceVoucherMapper.insert(voucher);
        entries.forEach(entry -> entry.setVoucherId(voucher.getId()));
        erpFinanceVoucherEntryMapper.insertBatch(entries);

        // 记录凭证生成日志
        createVoucherGenerateLog(voucher, template, source);

        if (publishDualWriteEvent) {
            applicationContext.publishEvent(new VoucherCreatedEvent(this, voucher, entries, reqVO.getLedgerId()));
        }

        return voucher.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public Long autoGenerateVoucher(Integer bizType, Long bizId) {
        List<Long> ledgerIds = resolveAutoGenerateLedgerIds(bizType);
        if (CollUtil.isEmpty(ledgerIds)) {
            return null;
        }
        Integer actualBizType = resolveVoucherBizType(bizType, bizId);
        Long firstVoucherId = null;
        for (Long ledgerId : ledgerIds) {
            Long voucherId = autoGenerateVoucherForLedger(ledgerId, bizType, actualBizType, bizId);
            if (firstVoucherId == null && voucherId != null) {
                firstVoucherId = voucherId;
            }
        }
        return firstVoucherId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rollbackAutoGeneratedVoucher(Integer bizType, Long bizId, Long userId, String remark) {
        List<ErpFinanceVoucherDO> voucherList = findAutoGeneratedVoucherList(bizType, bizId);
        if (dualLedgerAmountDiffLogMapper != null) {
            dualLedgerAmountDiffLogMapper.deleteByBizTypeAndBizId(bizType, bizId);
        }
        if (CollUtil.isEmpty(voucherList)) {
            return;
        }
        for (ErpFinanceVoucherDO voucher : voucherList) {
            if (ErpFinanceVoucherStatusEnum.isPosted(voucher.getStatus())) {
                Long reverseVoucherId = reverseVoucher(userId, new ErpFinanceVoucherReverseReqVO()
                        .setId(voucher.getId())
                        .setRemark(remark));
                clearVoucherBizRelation(voucher.getId());
                clearVoucherBizRelation(reverseVoucherId);
                continue;
            }
            clearVoucherBizRelation(voucher.getId());
            erpFinanceVoucherMapper.updateById(new ErpFinanceVoucherDO()
                    .setId(voucher.getId())
                    .setStatus(ErpFinanceVoucherStatusEnum.VOIDED.getStatus()));
            createVoucherRollbackLog(voucher, userId, remark);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long recomputeAutoGeneratedVoucher(Integer bizType, Long bizId, Long userId, String remark) {
        // 先校验业务单据是否存在，不存在则仅清理孤立凭证，不重新生成
        if (!isBizSourceExists(bizType, bizId)) {
            log.warn("[recomputeAutoGeneratedVoucher][bizType={} bizId={} 源单据不存在，仅清理孤立凭证]", bizType, bizId);
            rollbackAutoGeneratedVoucher(bizType, bizId, userId, remark);
            return null;
        }
        List<ErpFinanceVoucherDO> voucherList = findAutoGeneratedVoucherList(bizType, bizId);
        Long voucherId;
        if (CollUtil.isEmpty(voucherList)) {
            voucherId = autoGenerateVoucher(bizType, bizId);
        } else if (voucherList.stream().allMatch(voucher -> ErpFinanceVoucherStatusEnum.isGenerated(voucher.getStatus())
                || ErpFinanceVoucherStatusEnum.isApproved(voucher.getStatus()))) {
            for (ErpFinanceVoucherDO voucher : voucherList) {
                rebuildAutoGeneratedVoucher(voucher);
            }
            if (dualLedgerAmountDiffLogMapper != null) {
                dualLedgerAmountDiffLogMapper.deleteByBizTypeAndBizId(bizType, bizId);
            }
            voucherId = voucherList.get(0).getId();
        } else {
            rollbackAutoGeneratedVoucher(bizType, bizId, userId, remark);
            voucherId = autoGenerateVoucher(bizType, bizId);
        }
        bindAssetDepreciationVoucher(bizType, bizId, voucherId);
        return voucherId;
    }

    private void bindAssetDepreciationVoucher(Integer bizType, Long bizId, Long voucherId) {
        if (voucherId == null) {
            return;
        }
        if (ObjectUtil.equal(bizType, ErpBizTypeEnum.ASSET_DEPRECIATION.getType())
                || ObjectUtil.equal(bizType, ErpBizTypeEnum.ASSET_AMORTIZATION.getType())
                || ObjectUtil.equal(bizType, ErpBizTypeEnum.ASSET_AMORTIZATION_RD.getType())) {
            financeAssetDepreciationService.bindVoucher(bizId, voucherId);
        }
    }

    private void rebuildAutoGeneratedVoucher(ErpFinanceVoucherDO voucher) {
        ErpFinanceVoucherTemplateDO template = voucherTemplateService.validateVoucherTemplate(voucher.getTemplateId());
        if (!ObjectUtil.equal(template.getLedgerId(), voucher.getLedgerId())
                || !ObjectUtil.equal(template.getBizType(), voucher.getBizType())) {
            throw exception(FINANCE_VOUCHER_TEMPLATE_BIZ_MISMATCH, template.getName());
        }
        VoucherSource source = resolveVoucherSource(voucher.getBizType(), voucher.getBizId());
        List<ErpFinanceVoucherTemplateItemDO> templateItems = voucherTemplateService
                .getVoucherTemplateItemListByTemplateId(template.getId());
        List<ErpFinanceVoucherEntryDO> entries = buildEntries(template, templateItems, source);
        BigDecimal totalDebit = sumDebit(entries);
        BigDecimal totalCredit = sumCredit(entries);
        if (totalDebit.compareTo(totalCredit) != 0) {
            throw exception(FINANCE_VOUCHER_UNBALANCED, totalDebit, totalCredit);
        }
        erpFinanceVoucherEntryMapper.delete(new LambdaQueryWrapperX<ErpFinanceVoucherEntryDO>()
                .eq(ErpFinanceVoucherEntryDO::getVoucherId, voucher.getId()));
        entries.forEach(entry -> entry.setVoucherId(voucher.getId()));
        erpFinanceVoucherEntryMapper.insertBatch(entries);
        erpFinanceVoucherMapper.updateById(new ErpFinanceVoucherDO()
                .setId(voucher.getId())
                .setTotalDebitAmount(totalDebit)
                .setTotalCreditAmount(totalCredit)
                .setBizNo(source.bizNo)
                .setRemark(ObjectUtil.defaultIfBlank(voucher.getRemark(), source.remark)));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createMonthEndVoucher(Long ledgerId, String period, Map<String, BigDecimal> expenseSubjectAmounts,
                                      String profitSubjectCode) {
        if (expenseSubjectAmounts == null || expenseSubjectAmounts.isEmpty()) {
            return null;
        }
        YearMonth yearMonth = YearMonth.parse(period);
        Long bizId = buildMonthEndBizId(yearMonth);
        ErpFinanceVoucherDO existedVoucher = erpFinanceVoucherMapper.selectByLedgerIdAndBiz(
                ledgerId, ErpBizTypeEnum.FINANCE_EXPENSE_MONTH_END.getType(), bizId);
        if (existedVoucher != null) {
            return existedVoucher.getId();
        }
        ErpFinancePeriodDO periodDO = financePeriodService.getCurrentOpenPeriod(ledgerId, yearMonth.atEndOfMonth());
        List<ErpFinanceVoucherEntryDO> entries = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        int entryNo = 1;
        for (Map.Entry<String, BigDecimal> entry : new LinkedHashMap<>(expenseSubjectAmounts).entrySet()) {
            BigDecimal amount = defaultAmount(entry.getValue());
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            entries.add(new ErpFinanceVoucherEntryDO()
                    .setEntryNo(entryNo++)
                    .setSummary("研发费用月末结转")
                    .setSubjectCode(profitSubjectCode)
                    .setSubjectName("本年利润")
                    .setDebitAmount(amount)
                    .setCreditAmount(BigDecimal.ZERO));
            entries.add(new ErpFinanceVoucherEntryDO()
                    .setEntryNo(entryNo++)
                    .setSummary("研发费用月末结转")
                    .setSubjectCode(entry.getKey())
                    .setSubjectName(entry.getKey())
                    .setDebitAmount(BigDecimal.ZERO)
                    .setCreditAmount(amount));
            totalAmount = totalAmount.add(amount);
        }
        if (entries.isEmpty()) {
            return null;
        }
        ErpFinanceVoucherDO voucher = new ErpFinanceVoucherDO()
                .setVoucherNo(noRedisDAO.generate(ErpNoRedisDAO.FINANCE_VOUCHER_NO_PREFIX))
                .setLedgerId(ledgerId)
                .setPeriodId(periodDO.getId())
                .setTemplateId(null)
                .setBizType(ErpBizTypeEnum.FINANCE_EXPENSE_MONTH_END.getType())
                .setBizId(bizId)
                .setBizNo(period)
                .setVoucherTime(yearMonth.atEndOfMonth().atTime(23, 59, 59))
                .setStatus(ErpFinanceVoucherStatusEnum.GENERATED.getStatus())
                .setTotalDebitAmount(totalAmount)
                .setTotalCreditAmount(totalAmount)
                .setRemark("研发费用月末结转 " + period);
        erpFinanceVoucherMapper.insert(voucher);
        entries.forEach(item -> item.setVoucherId(voucher.getId()));
        erpFinanceVoucherEntryMapper.insertBatch(entries);
        return voucher.getId();
    }

    private ErpFinanceVoucherDO findExistingAutoVoucher(Long ledgerId, Integer sourceBizType, Integer actualBizType, Long bizId) {
        ErpFinanceVoucherDO existedVoucher = erpFinanceVoucherMapper.selectByLedgerIdAndBiz(ledgerId, actualBizType, bizId);
        if (existedVoucher != null) {
            return existedVoucher;
        }
        if (ObjectUtil.equal(sourceBizType, ErpBizTypeEnum.FINANCE_EXPENSE.getType())
                && !ObjectUtil.equal(actualBizType, sourceBizType)) {
            return erpFinanceVoucherMapper.selectByLedgerIdAndBiz(ledgerId, sourceBizType, bizId);
        }
        return null;
    }

    private List<Long> resolveAutoGenerateLedgerIds(Integer bizType) {
        ErpFinanceDualLedgerConfigDO dualLedgerConfig = dualLedgerConfigService == null
                ? null : dualLedgerConfigService.getEnabledDualLedgerConfig(bizType);
        if (dualLedgerConfig != null) {
            return List.of(dualLedgerConfig.getExternalLedgerId(), dualLedgerConfig.getInternalLedgerId());
        }
        ErpFinanceLedgerDO defaultLedger = financeLedgerService.getDefaultFinanceLedger();
        return defaultLedger == null ? List.of() : List.of(defaultLedger.getId());
    }

    private Long autoGenerateVoucherForLedger(Long ledgerId, Integer sourceBizType, Integer actualBizType, Long bizId) {
        List<ErpFinanceVoucherTemplateDO> templates = voucherTemplateService
                .getVoucherTemplateListByLedgerAndBizType(ledgerId, actualBizType);
        // 容错：如果在当前账簿找不到模板，尝试查找 ledger_id=1 的模板（兼容旧数据）
        if (CollUtil.isEmpty(templates) && !ObjectUtil.equal(ledgerId, 1L)) {
            templates = voucherTemplateService
                    .getVoucherTemplateListByLedgerAndBizType(1L, actualBizType);
            if (CollUtil.isNotEmpty(templates)) {
                log.warn("[autoGenerateVoucherForLedger] 在账簿 {} 未找到 bizType={} 的模板，回退使用 ledger_id=1 的模板", ledgerId, actualBizType);
            }
        }
        if (CollUtil.isEmpty(templates)) {
            return null;
        }
        ErpFinanceVoucherTemplateDO template;
        if (ObjectUtil.equal(sourceBizType, ErpBizTypeEnum.RESEARCH_EXPENSE.getType())) {
            template = templates.stream()
                    .filter(item -> Boolean.TRUE.equals(item.getAutoGenerate()))
                    .filter(item -> !ObjectUtil.equal(item.getStatus(), cn.weitee.erp.framework.common.enums.CommonStatusEnum.DISABLE.getStatus()))
                    .filter(item -> Boolean.TRUE.equals(item.getResearchTemplate()))
                    .findFirst()
                    .orElse(null);
        } else {
            template = templates.stream()
                    .filter(item -> Boolean.TRUE.equals(item.getAutoGenerate()))
                    .filter(item -> !ObjectUtil.equal(item.getStatus(), cn.weitee.erp.framework.common.enums.CommonStatusEnum.DISABLE.getStatus()))
                    .findFirst()
                    .orElse(null);
        }
        if (template == null) {
            return null;
        }
        ErpFinanceVoucherDO existedVoucher = findExistingAutoVoucher(ledgerId, sourceBizType, actualBizType, bizId);
        if (existedVoucher != null) {
            return existedVoucher.getId();
        }
        ErpFinanceVoucherGenerateReqVO reqVO = new ErpFinanceVoucherGenerateReqVO();
        reqVO.setLedgerId(ledgerId);
        reqVO.setTemplateId(template.getId());
        reqVO.setBizType(actualBizType);
        reqVO.setBizId(bizId);
        return generateVoucherWithoutDualWrite(reqVO);
    }

    private Long buildMonthEndBizId(YearMonth yearMonth) {
        return yearMonth.getYear() * 100L + yearMonth.getMonthValue();
    }

    private List<ErpFinanceVoucherDO> findAutoGeneratedVoucherList(Integer sourceBizType, Long bizId) {
        List<Long> ledgerIds = resolveAutoGenerateLedgerIds(sourceBizType);
        if (CollUtil.isEmpty(ledgerIds)) {
            return Collections.emptyList();
        }
        return erpFinanceVoucherMapper.selectList(new LambdaQueryWrapperX<ErpFinanceVoucherDO>()
                .in(ErpFinanceVoucherDO::getLedgerId, ledgerIds)
                .in(ErpFinanceVoucherDO::getBizType, resolveRollbackBizTypes(sourceBizType))
                .eq(ErpFinanceVoucherDO::getBizId, bizId)
                .orderByAsc(ErpFinanceVoucherDO::getLedgerId)
                .orderByAsc(ErpFinanceVoucherDO::getId));
    }

    private List<Integer> resolveRollbackBizTypes(Integer sourceBizType) {
        if (ObjectUtil.equal(sourceBizType, ErpBizTypeEnum.FINANCE_EXPENSE.getType())) {
            return List.of(ErpBizTypeEnum.FINANCE_EXPENSE.getType(),
                    ErpBizTypeEnum.FINANCE_EXPENSE_EXPENSE.getType(),
                    ErpBizTypeEnum.FINANCE_EXPENSE_CAPITALIZE.getType());
        }
        return List.of(sourceBizType);
    }

    private void clearVoucherBizRelation(Long voucherId) {
        if (voucherId == null) {
            return;
        }
        erpFinanceVoucherMapper.clearBizIdById(voucherId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveVoucher(Long userId, ErpFinanceVoucherActionReqVO reqVO) {
        List<ErpFinanceVoucherDO> voucherList = erpFinanceVoucherMapper.selectBatchIds(reqVO.getIds());
        if (CollUtil.isEmpty(voucherList)) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        voucherList.forEach(voucher -> {
            if (ErpFinanceVoucherStatusEnum.isApproved(voucher.getStatus())) {
                return;
            }
            if (!ErpFinanceVoucherStatusEnum.isGenerated(voucher.getStatus())) {
                throw exception(FINANCE_VOUCHER_APPROVE_FAIL_STATUS, voucher.getVoucherNo());
            }
            erpFinanceVoucherMapper.updateById(new ErpFinanceVoucherDO()
                    .setId(voucher.getId())
                    .setStatus(ErpFinanceVoucherStatusEnum.APPROVED.getStatus())
                    .setApproveUserId(userId)
                .setApproveTime(now));
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelApproveVoucher(Long userId, ErpFinanceVoucherActionReqVO reqVO) {
        List<ErpFinanceVoucherDO> voucherList = erpFinanceVoucherMapper.selectBatchIds(reqVO.getIds());
        if (CollUtil.isEmpty(voucherList)) {
            return;
        }
        voucherList.forEach(voucher -> {
            if (!ErpFinanceVoucherStatusEnum.isApproved(voucher.getStatus())) {
                throw exception(FINANCE_VOUCHER_CANCEL_APPROVE_FAIL_STATUS, voucher.getVoucherNo());
            }
            erpFinanceVoucherMapper.updateById(new ErpFinanceVoucherDO()
                    .setId(voucher.getId())
                    .setStatus(ErpFinanceVoucherStatusEnum.GENERATED.getStatus())
                    .setApproveUserId(null)
                    .setApproveTime(null));
            erpFinanceVoucherMapper.clearApprovalMetadataById(voucher.getId());
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void postVoucher(Long userId, ErpFinanceVoucherActionReqVO reqVO) {
        List<ErpFinanceVoucherDO> voucherList = erpFinanceVoucherMapper.selectBatchIds(reqVO.getIds());
        if (CollUtil.isEmpty(voucherList)) {
            return;
        }
        Map<Long, List<ErpFinanceVoucherEntryDO>> entryMap = new LinkedHashMap<>();
        List<ErpFinanceVoucherEntryDO> entryList = getVoucherEntryListByVoucherIds(convertSet(voucherList, ErpFinanceVoucherDO::getId));
        entryList.forEach(entry -> entryMap.computeIfAbsent(entry.getVoucherId(), key -> new ArrayList<>()).add(entry));
        LocalDateTime now = LocalDateTime.now();
        voucherList.forEach(voucher -> {
            if (!ErpFinanceVoucherStatusEnum.isApproved(voucher.getStatus())) {
                throw exception(FINANCE_VOUCHER_POST_FAIL_STATUS, voucher.getVoucherNo());
            }
            validateVoucherPeriodOpen(voucher.getLedgerId(), voucher.getPeriodId(), voucher.getVoucherTime());
            financeGeneralLedgerService.applyPostedVoucher(voucher,
                    entryMap.getOrDefault(voucher.getId(), Collections.emptyList()));
            erpFinanceVoucherMapper.updateById(new ErpFinanceVoucherDO()
                    .setId(voucher.getId())
                    .setStatus(ErpFinanceVoucherStatusEnum.POSTED.getStatus())
                    .setPostUserId(userId)
                .setPostTime(now));
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelPostVoucher(Long userId, ErpFinanceVoucherActionReqVO reqVO) {
        List<ErpFinanceVoucherDO> voucherList = erpFinanceVoucherMapper.selectBatchIds(reqVO.getIds());
        if (CollUtil.isEmpty(voucherList)) {
            return;
        }
        voucherList.forEach(voucher -> {
            if (!ErpFinanceVoucherStatusEnum.isPosted(voucher.getStatus())) {
                throw exception(FINANCE_VOUCHER_CANCEL_POST_FAIL_STATUS, voucher.getVoucherNo());
            }
            if (voucher.getReverseFromVoucherId() != null) {
                throw exception(FINANCE_VOUCHER_CANCEL_POST_REVERSE_NOT_ALLOWED, voucher.getVoucherNo());
            }
            validateVoucherPeriodOpen(voucher.getLedgerId(), voucher.getPeriodId(), voucher.getVoucherTime());
            financeGeneralLedgerService.rollbackPostedVoucher(voucher,
                    erpFinanceVoucherEntryMapper.selectListByVoucherId(voucher.getId()));
            erpFinanceVoucherMapper.updateById(new ErpFinanceVoucherDO()
                    .setId(voucher.getId())
                    .setStatus(ErpFinanceVoucherStatusEnum.APPROVED.getStatus())
                    .setPostUserId(null)
                    .setPostTime(null));
            erpFinanceVoucherMapper.clearPostingMetadataById(voucher.getId());
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long reverseVoucher(Long userId, ErpFinanceVoucherReverseReqVO reqVO) {
        ErpFinanceVoucherDO sourceVoucher = validateVoucherExists(reqVO.getId());
        if (sourceVoucher.getReverseFromVoucherId() != null) {
            throw exception(FINANCE_VOUCHER_REVERSE_OF_REVERSE_NOT_ALLOWED, sourceVoucher.getVoucherNo());
        }
        if (sourceVoucher.getReverseVoucherId() != null) {
            throw exception(FINANCE_VOUCHER_REVERSE_ALREADY_EXISTS, sourceVoucher.getVoucherNo());
        }
        if (!ErpFinanceVoucherStatusEnum.isPosted(sourceVoucher.getStatus())) {
            throw exception(FINANCE_VOUCHER_REVERSE_FAIL_STATUS, sourceVoucher.getVoucherNo());
        }
        LocalDateTime reverseTime = ObjectUtil.defaultIfNull(reqVO.getVoucherTime(), LocalDateTime.now());
        ErpFinancePeriodDO reversePeriod = validateVoucherPeriodOpen(sourceVoucher.getLedgerId(), null, reverseTime);
        List<ErpFinanceVoucherEntryDO> sourceEntries = erpFinanceVoucherEntryMapper.selectListByVoucherId(sourceVoucher.getId());
        List<ErpFinanceVoucherEntryDO> reverseEntries = buildReverseEntries(sourceEntries);
        LocalDateTime now = LocalDateTime.now();
        ErpFinanceVoucherDO reverseVoucher = new ErpFinanceVoucherDO()
                .setVoucherNo(noRedisDAO.generate(ErpNoRedisDAO.FINANCE_VOUCHER_NO_PREFIX))
                .setLedgerId(sourceVoucher.getLedgerId())
                .setPeriodId(reversePeriod.getId())
                .setTemplateId(sourceVoucher.getTemplateId())
                .setBizType(sourceVoucher.getBizType())
                .setBizId(null)
                .setBizNo(sourceVoucher.getBizNo())
                .setVoucherTime(reverseTime)
                .setStatus(ErpFinanceVoucherStatusEnum.POSTED.getStatus())
                .setTotalDebitAmount(defaultAmount(sourceVoucher.getTotalCreditAmount()))
                .setTotalCreditAmount(defaultAmount(sourceVoucher.getTotalDebitAmount()))
                .setApproveUserId(userId)
                .setApproveTime(now)
                .setPostUserId(userId)
                .setPostTime(now)
                .setReverseFromVoucherId(sourceVoucher.getId())
                .setReverseRemark(reqVO.getRemark())
                .setRemark(buildReverseRemark(sourceVoucher.getVoucherNo(), reqVO.getRemark()));
        erpFinanceVoucherMapper.insert(reverseVoucher);
        reverseEntries.forEach(entry -> entry.setVoucherId(reverseVoucher.getId()));
        if (CollUtil.isNotEmpty(reverseEntries)) {
            erpFinanceVoucherEntryMapper.insertBatch(reverseEntries);
        }
        financeGeneralLedgerService.applyPostedVoucher(reverseVoucher, reverseEntries);
        erpFinanceVoucherMapper.updateById(new ErpFinanceVoucherDO()
                .setId(sourceVoucher.getId())
                .setStatus(ErpFinanceVoucherStatusEnum.REVERSED.getStatus())
                .setReverseUserId(userId)
                .setReverseTime(now)
                .setReverseVoucherId(reverseVoucher.getId())
                .setReverseRemark(reqVO.getRemark()));
        return reverseVoucher.getId();
    }

    @Override
    public ErpFinanceVoucherDO getVoucher(Long id) {
        return erpFinanceVoucherMapper.selectById(id);
    }

    @Override
    public List<ErpFinanceVoucherDO> getVoucherListByIds(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return erpFinanceVoucherMapper.selectBatchIds(ids);
    }

    @Override
    public ErpFinanceVoucherDO getVoucherByLedgerAndBiz(Long ledgerId, Integer bizType, Long bizId) {
        return erpFinanceVoucherMapper.selectByLedgerIdAndBiz(ledgerId, bizType, bizId);
    }

    @Override
    public List<ErpFinanceVoucherDO> getVoucherListByLedgerAndBiz(Long ledgerId, Integer bizType, Collection<Long> bizIds) {
        if (ledgerId == null || bizType == null || CollUtil.isEmpty(bizIds)) {
            return Collections.emptyList();
        }
        return erpFinanceVoucherMapper.selectListByLedgerIdsAndBizTypeAndBizIds(
                List.of(ledgerId), bizType, new LinkedHashSet<>(bizIds));
    }

    @Override
    public PageResult<ErpFinanceVoucherDO> getVoucherPage(ErpFinanceVoucherPageReqVO pageReqVO) {
        return erpFinanceVoucherMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ErpFinanceVoucherEntryDO> getVoucherEntryListByVoucherId(Long voucherId) {
        return erpFinanceVoucherEntryMapper.selectListByVoucherId(voucherId);
    }

    @Override
    public List<ErpFinanceVoucherEntryDO> getVoucherEntryListByVoucherIds(Collection<Long> voucherIds) {
        return erpFinanceVoucherEntryMapper.selectListByVoucherIds(voucherIds);
    }

    /**
     * 校验业务单据是否存在，用于重算凭证前的前置校验
     * 避免源单据已删除时，rollback 成功但 regenerate 抛异常导致事务回滚
     */
    private boolean isBizSourceExists(Integer bizType, Long bizId) {
        if (ObjectUtil.equal(bizType, ErpBizTypeEnum.PURCHASE_IN.getType())) {
            return purchaseInService.getPurchaseIn(bizId) != null;
        }
        if (ObjectUtil.equal(bizType, ErpBizTypeEnum.PURCHASE_RETURN.getType())) {
            return purchaseReturnService.getPurchaseReturn(bizId) != null;
        }
        if (isFinanceExpenseVoucherBizType(bizType)) {
            return financeExpenseService.getFinanceExpense(bizId) != null;
        }
        if (ObjectUtil.equal(bizType, ErpBizTypeEnum.SALE_OUT.getType())) {
            return saleOutService.getSaleOut(bizId) != null;
        }
        if (ObjectUtil.equal(bizType, ErpBizTypeEnum.SALE_RETURN.getType())) {
            return saleReturnService.getSaleReturn(bizId) != null;
        }
        if (ObjectUtil.equal(bizType, ErpBizTypeEnum.OUTSOURCE_FEE.getType())) {
            return outsourceOrderService.getOutsourceFee(bizId) != null;
        }
        if (ObjectUtil.equal(bizType, ErpBizTypeEnum.PRODUCTION_INBOUND.getType())) {
            return productionInboundService.getProductionInbound(bizId) != null;
        }
        if (ObjectUtil.equal(bizType, ErpBizTypeEnum.RESEARCH_EXPENSE.getType())) {
            return financeExpenseService.getFinanceExpense(bizId) != null;
        }
        if (ObjectUtil.equal(bizType, ErpBizTypeEnum.STOCK_CHECK.getType())) {
            return stockCheckMapper.selectById(bizId) != null;
        }
        if (ObjectUtil.equal(bizType, ErpBizTypeEnum.ASSET_DEPRECIATION.getType())
                || ObjectUtil.equal(bizType, ErpBizTypeEnum.ASSET_AMORTIZATION.getType())
                || ObjectUtil.equal(bizType, ErpBizTypeEnum.ASSET_AMORTIZATION_RD.getType())) {
            return financeAssetDepreciationService.getFinanceAssetDepreciation(bizId) != null;
        }
        return false;
    }

    private VoucherSource resolveVoucherSource(Integer bizType, Long bizId) {
        if (ObjectUtil.equal(bizType, ErpBizTypeEnum.STOCK_CHECK.getType())) {
            ErpStockCheckDO stockCheck = stockCheckMapper.selectById(bizId);
            if (stockCheck == null) {
                throw exception(FINANCE_VOUCHER_NOT_EXISTS);
            }
            return new VoucherSource(stockCheck.getNo(),
                    defaultTime(stockCheck.getSnapshotTime(), stockCheck.getCreateTime(), stockCheck.getUpdateTime()),
                    defaultAmount(stockCheck.getTotalPrice()).abs(), stockCheck.getRemark());
        }
        if (ObjectUtil.equal(bizType, ErpBizTypeEnum.PURCHASE_IN.getType())) {
            ErpPurchaseInDO purchaseIn = purchaseInService.getPurchaseIn(bizId);
            if (purchaseIn == null) {
                throw exception(FINANCE_VOUCHER_NOT_EXISTS);
            }
            return new VoucherSource(purchaseIn.getNo(),
                    defaultTime(purchaseIn.getInTime(), purchaseIn.getCreateTime(), purchaseIn.getUpdateTime()),
                    defaultAmount(purchaseIn.getTotalPrice()), purchaseIn.getRemark());
        }
        if (ObjectUtil.equal(bizType, ErpBizTypeEnum.PURCHASE_RETURN.getType())) {
            ErpPurchaseReturnDO purchaseReturn = purchaseReturnService.getPurchaseReturn(bizId);
            if (purchaseReturn == null) {
                throw exception(FINANCE_VOUCHER_NOT_EXISTS);
            }
            return new VoucherSource(purchaseReturn.getNo(),
                    defaultTime(purchaseReturn.getCreateTime(), purchaseReturn.getUpdateTime()),
                    defaultAmount(purchaseReturn.getTotalPrice()), purchaseReturn.getRemark());
        }
        if (isFinanceExpenseVoucherBizType(bizType)) {
            ErpFinanceExpenseDO expense = financeExpenseService.getFinanceExpense(bizId);
            if (expense == null) {
                throw exception(FINANCE_VOUCHER_NOT_EXISTS);
            }
            if (!ObjectUtil.equal(expense.getStatus(), ErpAuditStatus.APPROVE.getStatus())) {
                throw exception(FINANCE_VOUCHER_SOURCE_STATUS_INVALID, expense.getNo());
            }
            validateFinanceExpenseVoucherBizType(expense, bizType);
            return new VoucherSource(expense.getNo(),
                    defaultTime(expense.getExpenseTime(), expense.getCreateTime(), expense.getUpdateTime()),
                    defaultAmount(expense.getExpensePrice()), expense.getRemark());
        }
        if (ObjectUtil.equal(bizType, ErpBizTypeEnum.SALE_OUT.getType())) {
            ErpSaleOutDO saleOut = saleOutService.getSaleOut(bizId);
            if (saleOut == null) {
                throw exception(FINANCE_VOUCHER_NOT_EXISTS);
            }
            return new VoucherSource(saleOut.getNo(),
                    defaultTime(saleOut.getOutTime(), saleOut.getCreateTime(), saleOut.getUpdateTime()),
                    defaultAmount(saleOut.getTotalPrice()), saleOut.getRemark());
        }
        if (ObjectUtil.equal(bizType, ErpBizTypeEnum.SALE_RETURN.getType())) {
            ErpSaleReturnDO saleReturn = saleReturnService.getSaleReturn(bizId);
            if (saleReturn == null) {
                throw exception(FINANCE_VOUCHER_NOT_EXISTS);
            }
            return new VoucherSource(saleReturn.getNo(),
                    defaultTime(saleReturn.getReturnTime(), saleReturn.getCreateTime(), saleReturn.getUpdateTime()),
                    defaultAmount(saleReturn.getTotalPrice()), saleReturn.getRemark());
        }
        if (ObjectUtil.equal(bizType, ErpBizTypeEnum.OUTSOURCE_FEE.getType())) {
            ErpOutsourceFeeDO fee = outsourceOrderService.getOutsourceFee(bizId);
            if (fee == null) {
                throw exception(FINANCE_VOUCHER_NOT_EXISTS);
            }
            return new VoucherSource(fee.getFeeNo(),
                    defaultTime(fee.getFeeTime(), fee.getCreateTime(), fee.getUpdateTime()),
                    defaultAmount(fee.getFeeAmount()), fee.getRemark());
        }
        if (ObjectUtil.equal(bizType, ErpBizTypeEnum.PRODUCTION_INBOUND.getType())) {
            ErpProductionInboundDO inbound = productionInboundService.getProductionInbound(bizId);
            if (inbound == null) {
                throw exception(FINANCE_VOUCHER_NOT_EXISTS);
            }
            return new VoucherSource(inbound.getNo(),
                    defaultTime(inbound.getInboundTime(), inbound.getExecutedTime(), inbound.getCreateTime(), inbound.getUpdateTime()),
                    defaultAmount(inbound.getTotalCost()), inbound.getRemark());
        }
        if (ObjectUtil.equal(bizType, ErpBizTypeEnum.RESEARCH_EXPENSE.getType())) {
            ErpFinanceExpenseDO expense = financeExpenseService.getFinanceExpense(bizId);
            if (expense == null) {
                throw exception(FINANCE_VOUCHER_NOT_EXISTS);
            }
            if (!ObjectUtil.equal(expense.getStatus(), ErpAuditStatus.APPROVE.getStatus())) {
                throw exception(FINANCE_VOUCHER_SOURCE_STATUS_INVALID, expense.getNo());
            }
            return new VoucherSource(expense.getNo(),
                    defaultTime(expense.getExpenseTime(), expense.getCreateTime(), expense.getUpdateTime()),
                    defaultAmount(expense.getExpensePrice()), expense.getRemark());
        }
        // 固定资产折旧 / 无形资产摊销 / 研发无形资产摊销
        if (ObjectUtil.equal(bizType, ErpBizTypeEnum.ASSET_DEPRECIATION.getType())
                || ObjectUtil.equal(bizType, ErpBizTypeEnum.ASSET_AMORTIZATION.getType())
                || ObjectUtil.equal(bizType, ErpBizTypeEnum.ASSET_AMORTIZATION_RD.getType())) {
            ErpFinanceAssetDepreciationDO depreciation = financeAssetDepreciationService.getFinanceAssetDepreciation(bizId);
            if (depreciation == null) {
                throw exception(FINANCE_VOUCHER_NOT_EXISTS);
            }
            String bizName;
            if (ObjectUtil.equal(bizType, ErpBizTypeEnum.ASSET_DEPRECIATION.getType())) {
                bizName = "固定资产折旧";
            } else if (ObjectUtil.equal(bizType, ErpBizTypeEnum.ASSET_AMORTIZATION.getType())) {
                bizName = "无形资产摊销";
            } else {
                bizName = "研发无形资产摊销";
            }
            // 凭证时间使用摊销期间的最后一天，确保落在正确的财务期间内
            YearMonth yearMonth = YearMonth.parse(depreciation.getPeriod());
            LocalDateTime voucherTime = yearMonth.atEndOfMonth().atTime(23, 59, 59);
            return new VoucherSource(depreciation.getAssetNo() + "/" + depreciation.getPeriod(),
                    voucherTime,
                    defaultAmount(depreciation.getDepreciationAmount()),
                    bizName + " " + depreciation.getPeriod());
        }
        throw exception(FINANCE_VOUCHER_SOURCE_NOT_SUPPORTED);
    }

    private Integer resolveVoucherBizType(Integer bizType, Long bizId) {
        if (!ObjectUtil.equal(bizType, ErpBizTypeEnum.FINANCE_EXPENSE.getType())) {
            return bizType;
        }
        ErpFinanceExpenseDO expense = financeExpenseService.getFinanceExpense(bizId);
        if (expense == null) {
            return bizType;
        }
        if (!ObjectUtil.equal(expense.getExpenseType(), ErpFinanceExpenseTypeEnum.RESEARCH.getType())) {
            return bizType;
        }
        if (ObjectUtil.equal(expense.getRdAccountingType(), ErpFinanceExpenseAccountingTypeEnum.CAPITALIZE.getType())) {
            return ErpBizTypeEnum.FINANCE_EXPENSE_CAPITALIZE.getType();
        }
        return ErpBizTypeEnum.FINANCE_EXPENSE_EXPENSE.getType();
    }

    private boolean isFinanceExpenseVoucherBizType(Integer bizType) {
        return ObjectUtil.equal(bizType, ErpBizTypeEnum.FINANCE_EXPENSE.getType())
                || ObjectUtil.equal(bizType, ErpBizTypeEnum.FINANCE_EXPENSE_EXPENSE.getType())
                || ObjectUtil.equal(bizType, ErpBizTypeEnum.FINANCE_EXPENSE_CAPITALIZE.getType());
    }

    private void validateFinanceExpenseVoucherBizType(ErpFinanceExpenseDO expense, Integer bizType) {
        if (ObjectUtil.equal(bizType, ErpBizTypeEnum.FINANCE_EXPENSE.getType())) {
            return;
        }
        boolean researchExpense = ObjectUtil.equal(expense.getExpenseType(), ErpFinanceExpenseTypeEnum.RESEARCH.getType());
        if (!researchExpense) {
            throw exception(FINANCE_VOUCHER_SOURCE_STATUS_INVALID, expense.getNo());
        }
        if (ObjectUtil.equal(bizType, ErpBizTypeEnum.FINANCE_EXPENSE_EXPENSE.getType())
                && !ObjectUtil.equal(expense.getRdAccountingType(), ErpFinanceExpenseAccountingTypeEnum.EXPENSE.getType())) {
            throw exception(FINANCE_VOUCHER_SOURCE_STATUS_INVALID, expense.getNo());
        }
        if (ObjectUtil.equal(bizType, ErpBizTypeEnum.FINANCE_EXPENSE_CAPITALIZE.getType())
                && !ObjectUtil.equal(expense.getRdAccountingType(), ErpFinanceExpenseAccountingTypeEnum.CAPITALIZE.getType())) {
            throw exception(FINANCE_VOUCHER_SOURCE_STATUS_INVALID, expense.getNo());
        }
    }

    private List<ErpFinanceVoucherEntryDO> buildEntries(ErpFinanceVoucherTemplateDO template,
                                                        List<ErpFinanceVoucherTemplateItemDO> templateItems,
                                                        VoucherSource source) {
        if (CollUtil.isEmpty(templateItems)) {
            return Collections.emptyList();
        }
        return convertList(templateItems, item -> {
            BigDecimal amount = resolveAmount(item.getAmountSource(), item.getAmountSourceValue(), source.amount);
            String summary = ObjectUtil.defaultIfBlank(item.getSummary(),
                    ObjectUtil.defaultIfBlank(template.getDefaultSummary(), source.bizNo));
            return new ErpFinanceVoucherEntryDO()
                    .setEntryNo(item.getEntryNo())
                    .setSummary(summary)
                    .setSubjectCode(item.getSubjectCode())
                    .setSubjectName(item.getSubjectName())
                    .setDebitAmount(ObjectUtil.equal(item.getEntryDirection(), ErpFinanceVoucherEntryDirectionEnum.DEBIT.getType())
                            ? amount : BigDecimal.ZERO)
                    .setCreditAmount(ObjectUtil.equal(item.getEntryDirection(), ErpFinanceVoucherEntryDirectionEnum.CREDIT.getType())
                            ? amount : BigDecimal.ZERO);
        });
    }

    private List<ErpFinanceVoucherEntryDO> buildReverseEntries(List<ErpFinanceVoucherEntryDO> sourceEntries) {
        if (CollUtil.isEmpty(sourceEntries)) {
            return Collections.emptyList();
        }
        return convertList(sourceEntries, item -> new ErpFinanceVoucherEntryDO()
                .setEntryNo(item.getEntryNo())
                .setSummary(item.getSummary())
                .setSubjectCode(item.getSubjectCode())
                .setSubjectName(item.getSubjectName())
                .setDebitAmount(defaultAmount(item.getCreditAmount()))
                .setCreditAmount(defaultAmount(item.getDebitAmount())));
    }

    private BigDecimal resolveAmount(Integer amountSource, BigDecimal amountSourceValue, BigDecimal bizAmount) {
        ErpFinanceVoucherAmountSourceEnum sourceEnum = ErpFinanceVoucherAmountSourceEnum.fromType(amountSource);
        if (sourceEnum == null) {
            return BigDecimal.ZERO;
        }
        return defaultAmount(sourceEnum.resolveAmount(amountSourceValue, defaultAmount(bizAmount)));
    }

    private ErpFinanceVoucherDO validateVoucherExists(Long id) {
        ErpFinanceVoucherDO voucher = erpFinanceVoucherMapper.selectById(id);
        if (voucher == null) {
            throw exception(FINANCE_VOUCHER_NOT_EXISTS);
        }
        return voucher;
    }

    private ErpFinancePeriodDO validateVoucherPeriodOpen(Long ledgerId, Long expectedPeriodId, LocalDateTime voucherTime) {
        ErpFinancePeriodDO period = financePeriodService.getCurrentOpenPeriod(ledgerId, voucherTime.toLocalDate());
        if (expectedPeriodId != null && !ObjectUtil.equal(period.getId(), expectedPeriodId)) {
            throw exception(cn.weitee.erp.module.erp.enums.ErrorCodeConstantsFinanceLedger.FINANCE_PERIOD_CURRENT_OPEN_NOT_EXISTS,
                    financeLedgerService.validateFinanceLedger(ledgerId).getName(), voucherTime.toLocalDate());
        }
        return period;
    }

    private BigDecimal sumDebit(List<ErpFinanceVoucherEntryDO> entries) {
        return entries.stream().map(item -> defaultAmount(item.getDebitAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumCredit(List<ErpFinanceVoucherEntryDO> entries) {
        return entries.stream().map(item -> defaultAmount(item.getCreditAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal defaultAmount(BigDecimal amount) {
        return ObjectUtil.defaultIfNull(amount, BigDecimal.ZERO);
    }

    private LocalDateTime resolveVoucherTime(LocalDateTime reqVoucherTime, VoucherSource source) {
        if (reqVoucherTime != null) {
            return reqVoucherTime;
        }
        if (source.bizTime != null) {
            return source.bizTime;
        }
        throw exception(FINANCE_VOUCHER_SOURCE_TIME_REQUIRED, source.bizNo);
    }

    private LocalDateTime defaultTime(LocalDateTime... values) {
        for (LocalDateTime value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private String buildReverseRemark(String sourceVoucherNo, String reverseRemark) {
        String prefix = "冲销凭证，来源凭证：" + sourceVoucherNo;
        return StrUtil.isBlank(reverseRemark) ? prefix : prefix + "，说明：" + reverseRemark;
    }

    private void createVoucherGenerateLog(ErpFinanceVoucherDO voucher, ErpFinanceVoucherTemplateDO template,
                                          VoucherSource source) {
        try {
            ErpFinanceVoucherLogDO log = ErpFinanceVoucherLogDO.builder()
                    .voucherId(voucher.getId())
                    .operationType("GENERATE")
                    .operationResult("SUCCESS")
                    .operationDetail(String.format("凭证号: %s, 模板: %s, 金额: %s, 业务单号: %s",
                            voucher.getVoucherNo(),
                            template.getName(),
                            voucher.getTotalDebitAmount(),
                            source.bizNo))
                    .remark(voucher.getRemark())
                    .build();
            voucherLogService.createVoucherLog(log);
        } catch (Exception e) {
            // 日志记录失败不影响主流程
            log.warn("[createVoucherGenerateLog][凭证ID: {} 日志记录失败]", voucher.getId(), e);
        }
    }

    private void createVoucherRollbackLog(ErpFinanceVoucherDO voucher, Long userId, String remark) {
        try {
            ErpFinanceVoucherLogDO log = ErpFinanceVoucherLogDO.builder()
                    .voucherId(voucher.getId())
                    .operationType("AUTO_ROLLBACK_VOID")
                    .operationResult("SUCCESS")
                    .operationDetail(String.format("自动回滚作废凭证，凭证号: %s, 业务类型: %s, 业务单号: %s",
                            voucher.getVoucherNo(), voucher.getBizType(), voucher.getBizNo()))
                    .operator(userId == null ? null : String.valueOf(userId))
                    .remark(remark)
                    .build();
            voucherLogService.createVoucherLog(log);
        } catch (Exception e) {
            log.warn("[createVoucherRollbackLog][凭证ID: {} 日志记录失败]", voucher.getId(), e);
        }
    }

    private static class VoucherSource {
        private final String bizNo;
        private final LocalDateTime bizTime;
        private final BigDecimal amount;
        private final String remark;

        private VoucherSource(String bizNo, LocalDateTime bizTime, BigDecimal amount, String remark) {
            this.bizNo = bizNo;
            this.bizTime = bizTime;
            this.amount = amount;
            this.remark = remark;
        }
    }
}
