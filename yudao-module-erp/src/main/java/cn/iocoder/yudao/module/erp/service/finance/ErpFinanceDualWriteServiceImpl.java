package cn.iocoder.yudao.module.erp.service.finance;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.dualwrite.ErpFinanceDualWriteLogPageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceDualLedgerDiffConfigDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceDualLedgerAmountDiffLogDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceDualWriteLogDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceDualWriteConfigDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceLedgerMappingDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceVoucherDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceVoucherEntryDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionInboundDO;
import cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpFinanceDualLedgerAmountDiffLogMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpFinanceDualWriteLogMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpFinanceVoucherMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpFinanceVoucherEntryMapper;
import cn.iocoder.yudao.module.erp.enums.ErpFinanceDualWriteStatusEnum;
import cn.iocoder.yudao.module.erp.enums.ErpFinanceDualLedgerDiffItemTypeEnum;
import cn.iocoder.yudao.module.erp.enums.ErpFinanceDualLedgerDiffSourceTypeEnum;
import cn.iocoder.yudao.module.erp.enums.common.ErpBizTypeEnum;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.cost.ErpProductionCostDetailRespVO;
import cn.iocoder.yudao.module.erp.service.mrp.ErpProductionCostService;
import cn.iocoder.yudao.module.erp.service.mrp.ErpProductionInboundService;
import cn.iocoder.yudao.module.erp.service.finance.diffcalc.AmountDiffCalculatorFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstantsFinanceDualWrite.*;

/**
 * ERP 双写服务实现
 */
@Slf4j
@Service
public class ErpFinanceDualWriteServiceImpl implements ErpFinanceDualWriteService {

    @Resource
    private ErpFinanceDualWriteLogMapper dualWriteLogMapper;

    @Resource
    private ErpFinanceLedgerMappingService ledgerMappingService;

    @Resource
    private ErpFinanceDualWriteConfigService dualWriteConfigService;

    @Resource
    private ErpFinanceVoucherService voucherService;

    @Resource
    private ErpFinanceVoucherEntryMapper voucherEntryMapper;

    @Resource
    private ErpFinanceVoucherMapper voucherMapper;

    @Resource
    private ErpFinanceDualLedgerAmountDiffLogMapper dualLedgerAmountDiffLogMapper;

    @Resource
    private ErpFinanceLedgerService financeLedgerService;

    @Resource
    private ErpFinanceDualLedgerDiffConfigService dualLedgerDiffConfigService;

    @Resource
    private AmountDiffCalculatorFactory amountDiffCalculatorFactory;

    @Resource
    private ErpProductionInboundService productionInboundService;

    @Resource
    private ErpProductionCostService productionCostService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void dualWriteVoucher(ErpFinanceVoucherDO sourceVoucher, List<ErpFinanceVoucherEntryDO> entries,
                                 Long sourceLedgerId) {
        // 1. 检查双写是否启用
        ErpFinanceDualWriteConfigDO config = dualWriteConfigService.getConfigByLedgerId(sourceLedgerId);
        if (config == null || !ObjectUtil.equal(config.getEnableStatus(), ErpFinanceDualWriteConfigDO.ENABLE_YES)) {
            log.info("双写未启用，跳过。sourceLedgerId={}", sourceLedgerId);
            return;
        }

        // 2. 获取账簿映射关系
        List<ErpFinanceLedgerMappingDO> mappings = ledgerMappingService.getLedgerMappingsByExternalLedger(sourceLedgerId);
        if (CollUtil.isEmpty(mappings)) {
            log.info("未找到账簿映射关系，跳过双写。sourceLedgerId={}", sourceLedgerId);
            return;
        }

        // 3. 遍历映射关系，生成内部账簿凭证
        for (ErpFinanceLedgerMappingDO mapping : mappings) {
            try {
                doDualWrite(sourceVoucher, entries, mapping);
            } catch (Exception e) {
                log.error("双写失败。sourceVoucherId={}, targetLedgerId={}",
                        sourceVoucher.getId(), mapping.getInternalLedgerId(), e);
                // 记录失败日志
                saveDualWriteLog(sourceVoucher, mapping, ErpFinanceDualWriteStatusEnum.FAILED, e.getMessage());
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncDualWriteBySourceVoucherId(Long sourceVoucherId) {
        if (sourceVoucherId == null) {
            return;
        }
        ErpFinanceVoucherDO sourceVoucher = voucherService.getVoucher(sourceVoucherId);
        if (sourceVoucher == null) {
            log.warn("同步双写失败，源凭证不存在。sourceVoucherId={}", sourceVoucherId);
            return;
        }
        List<ErpFinanceVoucherEntryDO> entries = voucherService.getVoucherEntryListByVoucherId(sourceVoucherId);
        if (CollUtil.isEmpty(entries)) {
            log.warn("同步双写失败，源凭证分录为空。sourceVoucherId={}", sourceVoucherId);
            return;
        }
        dualWriteVoucher(sourceVoucher, entries, sourceVoucher.getLedgerId());
    }

    private void doDualWrite(ErpFinanceVoucherDO sourceVoucher, List<ErpFinanceVoucherEntryDO> entries,
                             ErpFinanceLedgerMappingDO mapping) {
        Long targetLedgerId = mapping.getInternalLedgerId();

        // 检查目标账簿是否存在
        financeLedgerService.validateFinanceLedger(targetLedgerId);

        // 检查是否已存在双写凭证（幂等性）
        ErpFinanceVoucherDO existingVoucher = voucherService.getVoucherByLedgerAndBiz(
                targetLedgerId, sourceVoucher.getBizType(), sourceVoucher.getBizId());
        if (existingVoucher != null) {
            log.info("目标账簿已存在凭证，执行差异重算。targetLedgerId={}, bizType={}, bizId={}",
                    targetLedgerId, sourceVoucher.getBizType(), sourceVoucher.getBizId());
            recalculateAmountDiff(sourceVoucher, existingVoucher.getId(), entries);
            saveDualWriteLog(sourceVoucher, mapping, existingVoucher.getId(),
                    ErpFinanceDualWriteStatusEnum.SUCCESS, null);
            return;
        }

        // 生成目标凭证
        Long targetVoucherId = generateTargetVoucher(sourceVoucher, entries, targetLedgerId, mapping);

        // 记录成功日志
        saveDualWriteLog(sourceVoucher, mapping, targetVoucherId,
                ErpFinanceDualWriteStatusEnum.SUCCESS, null);
    }

    private Long generateTargetVoucher(ErpFinanceVoucherDO sourceVoucher, List<ErpFinanceVoucherEntryDO> entries,
                                       Long targetLedgerId, ErpFinanceLedgerMappingDO mapping) {
        // 获取目标账簿的默认模板
        cn.iocoder.yudao.module.erp.controller.admin.finance.vo.voucher.ErpFinanceVoucherGenerateReqVO reqVO =
                new cn.iocoder.yudao.module.erp.controller.admin.finance.vo.voucher.ErpFinanceVoucherGenerateReqVO();
        reqVO.setLedgerId(targetLedgerId);
        reqVO.setTemplateId(sourceVoucher.getTemplateId());
        reqVO.setBizType(sourceVoucher.getBizType());
        reqVO.setBizId(sourceVoucher.getBizId());
        reqVO.setVoucherTime(sourceVoucher.getVoucherTime());
        reqVO.setRemark("双写凭证，来源账簿：" + sourceVoucher.getLedgerId() +
                "，来源凭证：" + sourceVoucher.getVoucherNo());

        // 生成目标凭证
        Long targetVoucherId = voucherService.generateVoucher(reqVO);

        // 根据 DiffConfig 重算金额差异
        recalculateAmountDiff(sourceVoucher, targetVoucherId, entries);

        return targetVoucherId;
    }

    /**
     * 根据差异配置重算目标凭证金额
     */
    private void recalculateAmountDiff(ErpFinanceVoucherDO sourceVoucher, Long targetVoucherId,
                                       List<ErpFinanceVoucherEntryDO> sourceEntries) {
        // 查询该业务类型的差异配置
        List<ErpFinanceDualLedgerDiffConfigDO> diffConfigs = dualLedgerDiffConfigService
                .getDualLedgerDiffConfigList(sourceVoucher.getBizType(), null);
        if (CollUtil.isEmpty(diffConfigs)) {
            log.debug("未找到差异配置，跳过金额重算。bizType={}", sourceVoucher.getBizType());
            return;
        }

        // 获取目标凭证分录
        List<ErpFinanceVoucherEntryDO> targetEntries = voucherService.getVoucherEntryListByVoucherId(targetVoucherId);
        if (CollUtil.isEmpty(targetEntries)) {
            log.warn("目标凭证分录为空，跳过金额重算。targetVoucherId={}", targetVoucherId);
            return;
        }

        dualLedgerAmountDiffLogMapper.deleteByBizTypeAndBizId(sourceVoucher.getBizType(), sourceVoucher.getBizId());
        List<ErpFinanceDualLedgerAmountDiffLogDO> diffLogs = new ArrayList<>();

        if (ObjectUtil.equal(sourceVoucher.getBizType(), ErpBizTypeEnum.PRODUCTION_INBOUND.getType())) {
            applyProductionInboundAmountDiff(sourceVoucher, targetVoucherId, sourceEntries, targetEntries, diffConfigs, diffLogs);
            if (CollUtil.isNotEmpty(diffLogs)) {
                dualLedgerAmountDiffLogMapper.insertBatch(diffLogs);
            }
            refreshVoucherTotalAmount(targetVoucherId, targetEntries);
            return;
        }

        // 遍历差异配置，重算每个差异项的金额
        for (ErpFinanceDualLedgerDiffConfigDO diffConfig : diffConfigs) {
            try {
                applyAmountDiff(diffConfig, sourceVoucher, sourceEntries, targetEntries, targetVoucherId, diffLogs);
            } catch (Exception e) {
                log.error("金额差异重算失败。diffConfigId={}, targetVoucherId={}",
                        diffConfig.getId(), targetVoucherId, e);
            }
        }

        if (CollUtil.isNotEmpty(diffLogs)) {
            dualLedgerAmountDiffLogMapper.insertBatch(diffLogs);
        }

        refreshVoucherTotalAmount(targetVoucherId, targetEntries);
    }

    private void applyProductionInboundAmountDiff(ErpFinanceVoucherDO sourceVoucher, Long targetVoucherId,
                                                  List<ErpFinanceVoucherEntryDO> sourceEntries,
                                                  List<ErpFinanceVoucherEntryDO> targetEntries,
                                                  List<ErpFinanceDualLedgerDiffConfigDO> diffConfigs,
                                                  List<ErpFinanceDualLedgerAmountDiffLogDO> diffLogs) {
        ErpProductionInboundDO inbound = productionInboundService == null ? null
                : productionInboundService.getProductionInbound(sourceVoucher.getBizId());
        if (inbound == null || inbound.getProductionOrderId() == null || productionCostService == null) {
            log.warn("自制入库未找到生产成本明细，跳过金额重算。bizId={}", sourceVoucher.getBizId());
            return;
        }
        ErpProductionCostDetailRespVO costDetail = productionCostService.getCostDetail(inbound.getProductionOrderId());
        if (costDetail == null) {
            log.warn("自制入库生产成本明细为空，跳过金额重算。bizId={}, productionOrderId={}",
                    sourceVoucher.getBizId(), inbound.getProductionOrderId());
            return;
        }

        BigDecimal recalculatedTotal = defaultAmount(inbound.getTotalCost());
        for (ErpFinanceDualLedgerDiffConfigDO diffConfig : diffConfigs) {
            BigDecimal internalAmount = resolveProductionCostInternalAmount(costDetail, diffConfig.getDiffItemType());
            if (internalAmount.compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }
            BigDecimal externalAmount = amountDiffCalculatorFactory.calculate(
                    diffConfig.getCalculationType(),
                    internalAmount,
                    diffConfig.getRatio(),
                    diffConfig.getFixedAmount());
            externalAmount = defaultAmount(externalAmount).setScale(2, RoundingMode.HALF_UP);
            if (externalAmount.compareTo(internalAmount.setScale(2, RoundingMode.HALF_UP)) == 0) {
                continue;
            }
            recalculatedTotal = recalculatedTotal
                    .subtract(internalAmount)
                    .add(externalAmount);
            diffLogs.add(ErpFinanceDualLedgerAmountDiffLogDO.builder()
                    .sourceVoucherId(sourceVoucher.getId())
                    .targetVoucherId(targetVoucherId)
                    .diffItemType(diffConfig.getDiffItemType())
                    .calculationType(diffConfig.getCalculationType())
                    .internalAmount(internalAmount.setScale(2, RoundingMode.HALF_UP))
                    .externalAmount(externalAmount)
                    .diffAmount(internalAmount.setScale(2, RoundingMode.HALF_UP).subtract(externalAmount))
                    .ratio(diffConfig.getRatio())
                    .fixedAmount(diffConfig.getFixedAmount())
                    .bizType(sourceVoucher.getBizType())
                    .bizId(sourceVoucher.getBizId())
                    .remark("productionOrderId=" + inbound.getProductionOrderId())
                    .build());
        }

        if (CollUtil.isEmpty(diffLogs)) {
            return;
        }

        updateProductionInboundTargetEntries(sourceEntries, targetEntries, recalculatedTotal.setScale(2, RoundingMode.HALF_UP));
    }

    private BigDecimal resolveProductionCostInternalAmount(ErpProductionCostDetailRespVO costDetail, Integer diffItemType) {
        if (ObjectUtil.equal(diffItemType, ErpFinanceDualLedgerDiffItemTypeEnum.LABOR.getType())) {
            return defaultAmount(costDetail.getLaborCost());
        }
        if (ObjectUtil.equal(diffItemType, ErpFinanceDualLedgerDiffItemTypeEnum.DEPRECIATION.getType())) {
            return defaultAmount(costDetail.getDepreciationCost());
        }
        if (ObjectUtil.equal(diffItemType, ErpFinanceDualLedgerDiffItemTypeEnum.POWER.getType())) {
            return defaultAmount(costDetail.getPowerCost());
        }
        if (ObjectUtil.equal(diffItemType, ErpFinanceDualLedgerDiffItemTypeEnum.OTHER.getType())
                || ObjectUtil.equal(diffItemType, ErpFinanceDualLedgerDiffItemTypeEnum.MANUFACTURING_OVERHEAD.getType())) {
            return defaultAmount(costDetail.getOtherCost());
        }
        return BigDecimal.ZERO;
    }

    private void updateProductionInboundTargetEntries(List<ErpFinanceVoucherEntryDO> sourceEntries,
                                                      List<ErpFinanceVoucherEntryDO> targetEntries,
                                                      BigDecimal totalAmount) {
        if (CollUtil.isEmpty(sourceEntries) || CollUtil.isEmpty(targetEntries)) {
            return;
        }
        updateProductionInboundTargetEntriesBySide(sourceEntries, targetEntries, totalAmount, true);
        updateProductionInboundTargetEntriesBySide(sourceEntries, targetEntries, totalAmount, false);
    }

    private void updateProductionInboundTargetEntriesBySide(List<ErpFinanceVoucherEntryDO> sourceEntries,
                                                            List<ErpFinanceVoucherEntryDO> targetEntries,
                                                            BigDecimal totalAmount,
                                                            boolean debitSide) {
        List<ErpFinanceVoucherEntryDO> matchedSourceEntries = new ArrayList<>();
        List<BigDecimal> sourceAmounts = new ArrayList<>();
        for (ErpFinanceVoucherEntryDO sourceEntry : sourceEntries) {
            BigDecimal sourceAmount = debitSide ? defaultAmount(sourceEntry.getDebitAmount()) : defaultAmount(sourceEntry.getCreditAmount());
            if (sourceAmount.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            ErpFinanceVoucherEntryDO targetEntry = findMatchingProductionInboundEntry(targetEntries, sourceEntry);
            if (targetEntry == null || matchedSourceEntries.contains(sourceEntry)) {
                continue;
            }
            matchedSourceEntries.add(sourceEntry);
            sourceAmounts.add(sourceAmount);
        }
        if (matchedSourceEntries.isEmpty()) {
            return;
        }

        BigDecimal sourceSideTotal = sourceAmounts.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        if (sourceSideTotal.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        BigDecimal allocatedAmount = BigDecimal.ZERO;
        for (int i = 0; i < matchedSourceEntries.size(); i++) {
            ErpFinanceVoucherEntryDO sourceEntry = matchedSourceEntries.get(i);
            ErpFinanceVoucherEntryDO targetEntry = findMatchingProductionInboundEntry(targetEntries, sourceEntry);
            if (targetEntry == null) {
                continue;
            }
            BigDecimal sourceAmount = sourceAmounts.get(i);
            BigDecimal newAmount;
            if (i == matchedSourceEntries.size() - 1) {
                newAmount = totalAmount.subtract(allocatedAmount).setScale(2, RoundingMode.HALF_UP);
            } else {
                newAmount = totalAmount.multiply(sourceAmount)
                        .divide(sourceSideTotal, 2, RoundingMode.HALF_UP);
                allocatedAmount = allocatedAmount.add(newAmount);
            }
            updateProductionInboundEntryAmount(targetEntry, newAmount, debitSide);
        }
    }

    private ErpFinanceVoucherEntryDO findMatchingProductionInboundEntry(List<ErpFinanceVoucherEntryDO> targetEntries,
                                                                        ErpFinanceVoucherEntryDO sourceEntry) {
        ErpFinanceVoucherEntryDO matchedEntry = null;
        for (ErpFinanceVoucherEntryDO targetEntry : targetEntries) {
            if (ObjectUtil.equal(targetEntry.getSubjectCode(), sourceEntry.getSubjectCode())
                    && ObjectUtil.equal(normalizeText(targetEntry.getSummary()), normalizeText(sourceEntry.getSummary()))) {
                return targetEntry;
            }
            if (ObjectUtil.equal(targetEntry.getSubjectCode(), sourceEntry.getSubjectCode())) {
                matchedEntry = targetEntry;
            }
        }
        if (matchedEntry != null) {
            return matchedEntry;
        }
        for (ErpFinanceVoucherEntryDO targetEntry : targetEntries) {
            if (ObjectUtil.equal(targetEntry.getEntryNo(), sourceEntry.getEntryNo())) {
                return targetEntry;
            }
        }
        return null;
    }

    private void updateProductionInboundEntryAmount(ErpFinanceVoucherEntryDO entry, BigDecimal newAmount, boolean debitSide) {
        if (debitSide && entry.getDebitAmount() != null && entry.getDebitAmount().compareTo(BigDecimal.ZERO) > 0) {
            entry.setDebitAmount(newAmount);
            voucherEntryMapper.updateById(entry);
            return;
        }
        if (!debitSide && entry.getCreditAmount() != null && entry.getCreditAmount().compareTo(BigDecimal.ZERO) > 0) {
            entry.setCreditAmount(newAmount);
            voucherEntryMapper.updateById(entry);
        }
    }

    /**
     * 应用单个差异项的金额重算
     */
    private void applyAmountDiff(ErpFinanceDualLedgerDiffConfigDO diffConfig,
                                 ErpFinanceVoucherDO sourceVoucher,
                                 List<ErpFinanceVoucherEntryDO> sourceEntries,
                                 List<ErpFinanceVoucherEntryDO> targetEntries,
                                 Long targetVoucherId,
                                 List<ErpFinanceDualLedgerAmountDiffLogDO> diffLogs) {
        // 查找匹配的源分录和目标分录
        for (ErpFinanceVoucherEntryDO sourceEntry : sourceEntries) {
            if (!matchesDiffConfig(diffConfig, sourceEntry)) {
                continue;
            }
            // 匹配条件：科目相同或摘要包含差异项类型
            ErpFinanceVoucherEntryDO targetEntry = findMatchingEntry(targetEntries, sourceEntry);
            if (targetEntry == null) {
                continue;
            }

            // 计算外部账金额
            BigDecimal internalAmount = sourceEntry.getDebitAmount() != null
                    ? sourceEntry.getDebitAmount() : sourceEntry.getCreditAmount();
            BigDecimal externalAmount = amountDiffCalculatorFactory.calculate(
                    diffConfig.getCalculationType(),
                    internalAmount,
                    diffConfig.getRatio(),
                    diffConfig.getFixedAmount());

            // 更新目标分录金额
            if (externalAmount != null && externalAmount.compareTo(internalAmount) != 0) {
                updateEntryAmount(targetEntry, externalAmount);
                diffLogs.add(buildDiffLog(sourceVoucher, targetVoucherId, diffConfig, sourceEntry, externalAmount));
                log.info("金额差异重算完成。diffItemType={}, internalAmount={}, externalAmount={}",
                        diffConfig.getDiffItemType(), internalAmount, externalAmount);
            }
        }
    }

    /**
     * 查找匹配的目标分录
     */
    private ErpFinanceVoucherEntryDO findMatchingEntry(List<ErpFinanceVoucherEntryDO> targetEntries,
                                                       ErpFinanceVoucherEntryDO sourceEntry) {
        // 优先按科目代码匹配
        for (ErpFinanceVoucherEntryDO targetEntry : targetEntries) {
            if (ObjectUtil.equal(targetEntry.getSubjectCode(), sourceEntry.getSubjectCode())) {
                return targetEntry;
            }
        }
        // 其次按分录序号匹配
        for (ErpFinanceVoucherEntryDO targetEntry : targetEntries) {
            if (ObjectUtil.equal(targetEntry.getEntryNo(), sourceEntry.getEntryNo())) {
                return targetEntry;
            }
        }
        return null;
    }

    /**
     * 更新分录金额
     */
    private void updateEntryAmount(ErpFinanceVoucherEntryDO entry, BigDecimal newAmount) {
        if (entry.getDebitAmount() != null && entry.getDebitAmount().compareTo(BigDecimal.ZERO) != 0) {
            entry.setDebitAmount(newAmount);
        }
        if (entry.getCreditAmount() != null && entry.getCreditAmount().compareTo(BigDecimal.ZERO) != 0) {
            entry.setCreditAmount(newAmount);
        }
        // 更新数据库
        voucherEntryMapper.updateById(entry);
    }

    private boolean matchesDiffConfig(ErpFinanceDualLedgerDiffConfigDO diffConfig, ErpFinanceVoucherEntryDO sourceEntry) {
        if (diffConfig.getInternalSourceValue() == null) {
            return true;
        }
        Integer sourceType = diffConfig.getInternalSourceType();
        if (sourceType == null) {
            return ObjectUtil.equal(String.valueOf(diffConfig.getInternalSourceValue()), sourceEntry.getSubjectCode());
        }
        if (ObjectUtil.equal(sourceType, ErpFinanceDualLedgerDiffSourceTypeEnum.COST_ITEM.getType())) {
            return matchesCostItemSource(diffConfig.getInternalSourceValue(), sourceEntry);
        }
        if (ObjectUtil.equal(sourceType, ErpFinanceDualLedgerDiffSourceTypeEnum.ASSET_DEPRECIATION.getType())) {
            return matchesKeywords(sourceEntry, "折旧");
        }
        return false;
    }

    private boolean matchesCostItemSource(Integer sourceValue, ErpFinanceVoucherEntryDO sourceEntry) {
        if (sourceValue == null) {
            return true;
        }
        // 兼容早期错误配置：把来源值直接当成科目代码使用
        if (!ErpFinanceDualLedgerDiffItemTypeEnum.isSupported(sourceValue)) {
            return ObjectUtil.equal(String.valueOf(sourceValue), sourceEntry.getSubjectCode());
        }
        if (ObjectUtil.equal(sourceValue, ErpFinanceDualLedgerDiffItemTypeEnum.LABOR.getType())) {
            return matchesKeywords(sourceEntry, "人工", "工资", "薪酬");
        }
        if (ObjectUtil.equal(sourceValue, ErpFinanceDualLedgerDiffItemTypeEnum.DEPRECIATION.getType())) {
            return matchesKeywords(sourceEntry, "折旧");
        }
        if (ObjectUtil.equal(sourceValue, ErpFinanceDualLedgerDiffItemTypeEnum.POWER.getType())) {
            return matchesKeywords(sourceEntry, "电费", "电力", "动力");
        }
        if (ObjectUtil.equal(sourceValue, ErpFinanceDualLedgerDiffItemTypeEnum.OTHER.getType())) {
            return matchesKeywords(sourceEntry, "其他制造费用", "制造费用");
        }
        if (ObjectUtil.equal(sourceValue, ErpFinanceDualLedgerDiffItemTypeEnum.MANUFACTURING_OVERHEAD.getType())) {
            return matchesKeywords(sourceEntry, "制造费用");
        }
        return false;
    }

    private boolean matchesKeywords(ErpFinanceVoucherEntryDO sourceEntry, String... keywords) {
        String subjectName = normalizeText(sourceEntry.getSubjectName());
        String summary = normalizeText(sourceEntry.getSummary());
        return Arrays.stream(keywords).map(this::normalizeText)
                .anyMatch(keyword -> subjectName.contains(keyword) || summary.contains(keyword));
    }

    private String normalizeText(String text) {
        return text == null ? "" : text.replace(" ", "").replace("-", "").replace("_", "");
    }

    private ErpFinanceDualLedgerAmountDiffLogDO buildDiffLog(ErpFinanceVoucherDO sourceVoucher, Long targetVoucherId,
                                                             ErpFinanceDualLedgerDiffConfigDO diffConfig,
                                                             ErpFinanceVoucherEntryDO sourceEntry,
                                                             BigDecimal externalAmount) {
        BigDecimal internalAmount = sourceEntry.getDebitAmount() != null
                ? defaultAmount(sourceEntry.getDebitAmount()) : defaultAmount(sourceEntry.getCreditAmount());
        return ErpFinanceDualLedgerAmountDiffLogDO.builder()
                .sourceVoucherId(sourceVoucher.getId())
                .targetVoucherId(targetVoucherId)
                .diffItemType(diffConfig.getDiffItemType())
                .calculationType(diffConfig.getCalculationType())
                .internalAmount(internalAmount)
                .externalAmount(defaultAmount(externalAmount))
                .diffAmount(internalAmount.subtract(defaultAmount(externalAmount)))
                .ratio(diffConfig.getRatio())
                .fixedAmount(diffConfig.getFixedAmount())
                .bizType(sourceVoucher.getBizType())
                .bizId(sourceVoucher.getBizId())
                .remark("sourceType=" + diffConfig.getInternalSourceType()
                        + ",sourceValue=" + diffConfig.getInternalSourceValue()
                        + ",sourceSubjectCode=" + sourceEntry.getSubjectCode())
                .build();
    }

    private void refreshVoucherTotalAmount(Long targetVoucherId, List<ErpFinanceVoucherEntryDO> targetEntries) {
        BigDecimal totalDebitAmount = BigDecimal.ZERO;
        BigDecimal totalCreditAmount = BigDecimal.ZERO;
        for (ErpFinanceVoucherEntryDO entry : targetEntries) {
            totalDebitAmount = totalDebitAmount.add(defaultAmount(entry.getDebitAmount()));
            totalCreditAmount = totalCreditAmount.add(defaultAmount(entry.getCreditAmount()));
        }
        voucherMapper.updateById(new ErpFinanceVoucherDO()
                .setId(targetVoucherId)
                .setTotalDebitAmount(totalDebitAmount.setScale(2, RoundingMode.HALF_UP))
                .setTotalCreditAmount(totalCreditAmount.setScale(2, RoundingMode.HALF_UP)));
    }

    private BigDecimal defaultAmount(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void retryDualWrite(Long logId) {
        ErpFinanceDualWriteLogDO dualWriteLog = dualWriteLogMapper.selectById(logId);
        if (dualWriteLog == null) {
            throw exception(DUAL_WRITE_LOG_NOT_EXISTS);
        }
        if (!ErpFinanceDualWriteStatusEnum.isFailed(dualWriteLog.getStatus())) {
            throw exception(DUAL_WRITE_RETRY_STATUS_INVALID);
        }

        // 获取源凭证
        ErpFinanceVoucherDO sourceVoucher = voucherService.getVoucher(dualWriteLog.getSourceVoucherId());
        if (sourceVoucher == null) {
            throw exception(cn.iocoder.yudao.module.erp.enums.ErrorCodeConstantsFinanceVoucher.FINANCE_VOUCHER_NOT_EXISTS);
        }

        // 获取源凭证分录
        List<ErpFinanceVoucherEntryDO> entries = voucherService.getVoucherEntryListByVoucherId(sourceVoucher.getId());

        // 获取映射关系
        ErpFinanceLedgerMappingDO mapping = new ErpFinanceLedgerMappingDO();
        mapping.setExternalLedgerId(dualWriteLog.getSourceLedgerId());
        mapping.setInternalLedgerId(dualWriteLog.getTargetLedgerId());

        try {
            doDualWrite(sourceVoucher, entries, mapping);
            // 更新日志状态为成功
            dualWriteLog.setStatus(ErpFinanceDualWriteStatusEnum.SUCCESS.getStatus());
            dualWriteLog.setErrorMessage(null);
            dualWriteLog.setRetryCount(dualWriteLog.getRetryCount() + 1);
            dualWriteLogMapper.updateById(dualWriteLog);
        } catch (Exception e) {
            log.error("重试双写失败。logId={}", logId, e);
            // 更新重试次数和错误信息
            dualWriteLog.setRetryCount(dualWriteLog.getRetryCount() + 1);
            dualWriteLog.setErrorMessage(e.getMessage());
            dualWriteLogMapper.updateById(dualWriteLog);
            throw exception(DUAL_WRITE_RETRY_FAILED, e.getMessage());
        }
    }

    @Override
    public PageResult<ErpFinanceDualWriteLogDO> getDualWriteLogPage(ErpFinanceDualWriteLogPageReqVO pageReqVO) {
        return dualWriteLogMapper.selectPage(pageReqVO);
    }

    @Override
    public ErpFinanceDualWriteLogDO getDualWriteLog(Long id) {
        return dualWriteLogMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int recomputeDualLedgerVouchers(Integer bizType) {
        // 查询该业务类型的所有双写日志
        List<ErpFinanceDualWriteLogDO> logs = dualWriteLogMapper.selectList(
                new cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX<ErpFinanceDualWriteLogDO>()
                        .eq(ErpFinanceDualWriteLogDO::getBizType, bizType)
                        .eq(ErpFinanceDualWriteLogDO::getStatus, ErpFinanceDualWriteStatusEnum.SUCCESS.getStatus()));
        if (CollUtil.isEmpty(logs)) {
            log.info("未找到需要重算的双写日志。bizType={}", bizType);
            return 0;
        }

        int count = 0;
        for (ErpFinanceDualWriteLogDO logDO : logs) {
            try {
                recomputeSingleVoucher(logDO);
                count++;
            } catch (Exception e) {
                log.error("重算失败。logId={}, bizId={}", logDO.getId(), logDO.getBizId(), e);
            }
        }
        log.info("批量重算完成。bizType={}, total={}, success={}", bizType, logs.size(), count);
        return count;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean recomputeByBizId(Integer bizType, Long bizId) {
        // 查询该业务单据的双写日志
        ErpFinanceDualWriteLogDO logDO = dualWriteLogMapper.selectLatestByBizTypeAndBizId(
                bizType, bizId, ErpFinanceDualWriteStatusEnum.SUCCESS.getStatus());
        if (logDO == null) {
            log.info("未找到需要重算的双写日志。bizType={}, bizId={}", bizType, bizId);
            return false;
        }

        try {
            recomputeSingleVoucher(logDO);
            return true;
        } catch (Exception e) {
            log.error("重算失败。bizType={}, bizId={}", bizType, bizId, e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recomputeByBizIdStrict(Integer bizType, Long bizId) {
        // 查询该业务单据的双写日志
        ErpFinanceDualWriteLogDO logDO = dualWriteLogMapper.selectLatestByBizTypeAndBizId(
                bizType, bizId, ErpFinanceDualWriteStatusEnum.SUCCESS.getStatus());
        if (logDO == null) {
            log.warn("recomputeByBizIdStrict: 未找到需要重算的双写日志。bizType={}, bizId={}", bizType, bizId);
            return;
        }
        // 直接执行，失败向上抛异常，不吞掉
        recomputeSingleVoucher(logDO);
    }

    /**
     * 重算单个凭证的金额差异
     */
    private void recomputeSingleVoucher(ErpFinanceDualWriteLogDO logDO) {
        // 获取源凭证
        ErpFinanceVoucherDO sourceVoucher = voucherService.getVoucher(logDO.getSourceVoucherId());
        ErpFinanceVoucherDO targetVoucher = logDO.getTargetVoucherId() == null ? null
                : voucherService.getVoucher(logDO.getTargetVoucherId());
        if (sourceVoucher == null || targetVoucher == null) {
            sourceVoucher = voucherService.getVoucherByLedgerAndBiz(
                    logDO.getSourceLedgerId(), logDO.getBizType(), logDO.getBizId());
            targetVoucher = voucherService.getVoucherByLedgerAndBiz(
                    logDO.getTargetLedgerId(), logDO.getBizType(), logDO.getBizId());
            if (sourceVoucher == null || targetVoucher == null) {
                log.warn("双写日志指向的凭证已失效，且未找到当前账簿凭证。logId={}, bizType={}, bizId={}",
                        logDO.getId(), logDO.getBizType(), logDO.getBizId());
                return;
            }
            refreshLogVoucherPointers(logDO, sourceVoucher, targetVoucher);
        }

        // 获取源凭证分录
        List<ErpFinanceVoucherEntryDO> sourceEntries = voucherService.getVoucherEntryListByVoucherId(sourceVoucher.getId());
        if (CollUtil.isEmpty(sourceEntries)) {
            log.warn("源凭证分录为空，跳过重算。sourceVoucherId={}", logDO.getSourceVoucherId());
            return;
        }

        // 重新计算金额差异
        recalculateAmountDiff(sourceVoucher, targetVoucher.getId(), sourceEntries);
    }

    private void refreshLogVoucherPointers(ErpFinanceDualWriteLogDO logDO, ErpFinanceVoucherDO sourceVoucher,
                                           ErpFinanceVoucherDO targetVoucher) {
        boolean sourceChanged = !ObjectUtil.equal(logDO.getSourceVoucherId(), sourceVoucher.getId());
        boolean targetChanged = !ObjectUtil.equal(logDO.getTargetVoucherId(), targetVoucher.getId());
        if (!sourceChanged && !targetChanged) {
            return;
        }
        dualWriteLogMapper.updateById(new ErpFinanceDualWriteLogDO()
                .setId(logDO.getId())
                .setSourceVoucherId(sourceVoucher.getId())
                .setTargetVoucherId(targetVoucher.getId())
                .setErrorMessage(null));
        logDO.setSourceVoucherId(sourceVoucher.getId());
        logDO.setTargetVoucherId(targetVoucher.getId());
    }

    private void saveDualWriteLog(ErpFinanceVoucherDO sourceVoucher, ErpFinanceLedgerMappingDO mapping,
                                  ErpFinanceDualWriteStatusEnum status, String errorMessage) {
        saveDualWriteLog(sourceVoucher, mapping, null, status, errorMessage);
    }

    private void saveDualWriteLog(ErpFinanceVoucherDO sourceVoucher, ErpFinanceLedgerMappingDO mapping,
                                  Long targetVoucherId, ErpFinanceDualWriteStatusEnum status, String errorMessage) {
        ErpFinanceDualWriteLogDO logDO = ErpFinanceDualWriteLogDO.builder()
                .sourceVoucherId(sourceVoucher.getId())
                .targetVoucherId(targetVoucherId)
                .sourceLedgerId(mapping.getExternalLedgerId())
                .targetLedgerId(mapping.getInternalLedgerId())
                .bizType(sourceVoucher.getBizType())
                .bizId(sourceVoucher.getBizId())
                .status(status.getStatus())
                .errorMessage(errorMessage)
                .retryCount(0)
                .build();
        dualWriteLogMapper.insert(logDO);
    }
}
