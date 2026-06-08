package cn.iocoder.yudao.module.erp.service.finance;

import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.productdualcost.ErpFinanceDualProductCostPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.productdualcost.ErpFinanceDualProductCostRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.productdualcost.ErpFinanceDualProductCostRebuildReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceDualProductCostResultDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceDualProductCostItemDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceDualProductCostRebuildLogDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceDualLedgerDiffConfigDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceVoucherDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceVoucherEntryDO;
import cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpFinanceDualProductCostResultMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpFinanceDualProductCostItemMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpFinanceDualProductCostRebuildLogMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpFinanceVoucherMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpFinanceVoucherEntryMapper;
import cn.iocoder.yudao.module.erp.enums.common.ErpBizTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 产品双账成本 Service 实现
 */
@Slf4j
@Service
@Validated
public class ErpFinanceDualProductCostServiceImpl implements ErpFinanceDualProductCostService {

    @Resource
    private ErpFinanceDualProductCostResultMapper productCostResultMapper;
    @Resource
    private ErpFinanceDualProductCostItemMapper productCostItemMapper;
    @Resource
    private ErpFinanceDualProductCostRebuildLogMapper rebuildLogMapper;
    @Resource
    private ErpFinanceVoucherMapper voucherMapper;
    @Resource
    private ErpFinanceVoucherEntryMapper voucherEntryMapper;
    @Resource
    private ErpFinanceDualLedgerConfigService dualLedgerConfigService;
    @Resource
    private ErpFinanceLedgerService financeLedgerService;
    @Resource
    private ErpFinanceDualLedgerDiffConfigService dualLedgerDiffConfigService;
    @Resource
    private cn.iocoder.yudao.module.erp.service.finance.diffcalc.AmountDiffCalculatorFactory amountDiffCalculatorFactory;

    @Override
    public PageResult<ErpFinanceDualProductCostRespVO> getProductDualCostPage(ErpFinanceDualProductCostPageReqVO pageReqVO) {
        LambdaQueryWrapperX<ErpFinanceDualProductCostResultDO> query = new LambdaQueryWrapperX<ErpFinanceDualProductCostResultDO>()
                .eqIfPresent(ErpFinanceDualProductCostResultDO::getProductId, pageReqVO.getProductId())
                .likeIfPresent(ErpFinanceDualProductCostResultDO::getProductNo, pageReqVO.getProductNo())
                .likeIfPresent(ErpFinanceDualProductCostResultDO::getProductName, pageReqVO.getProductName())
                .eqIfPresent(ErpFinanceDualProductCostResultDO::getProductBatchNo, pageReqVO.getProductBatchNo())
                .eqIfPresent(ErpFinanceDualProductCostResultDO::getProductionOrderId, pageReqVO.getProductionOrderId())
                .eqIfPresent(ErpFinanceDualProductCostResultDO::getPeriod, pageReqVO.getPeriod())
                .orderByDesc(ErpFinanceDualProductCostResultDO::getPeriod)
                .orderByDesc(ErpFinanceDualProductCostResultDO::getProductId);

        PageResult<ErpFinanceDualProductCostResultDO> pageResult = productCostResultMapper.selectPage(pageReqVO, query);
        return new PageResult<>(convertToRespVOList(pageResult.getList()), pageResult.getTotal());
    }

    @Override
    public ErpFinanceDualProductCostRespVO getProductDualCost(Long id) {
        ErpFinanceDualProductCostResultDO result = productCostResultMapper.selectById(id);
        return result != null ? convertToRespVO(result) : null;
    }

    @Override
    public List<ErpFinanceDualProductCostRespVO> getProductDualCostItems(Long resultId) {
        List<ErpFinanceDualProductCostItemDO> items = productCostItemMapper.selectList(
                new LambdaQueryWrapperX<ErpFinanceDualProductCostItemDO>()
                        .eq(ErpFinanceDualProductCostItemDO::getResultId, resultId)
                        .orderByAsc(ErpFinanceDualProductCostItemDO::getCostComponentType));
        return items.stream().map(this::convertItemToRespVO).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rebuildProductDualCost(Long userId, ErpFinanceDualProductCostRebuildReqVO reqVO) {
        Long productId = reqVO.getProductId();
        String period = reqVO.getPeriod();

        // 1. 记录重跑日志
        ErpFinanceDualProductCostRebuildLogDO rebuildLog = ErpFinanceDualProductCostRebuildLogDO.builder()
                .productId(productId)
                .productBatchNo(reqVO.getProductBatchNo())
                .productionOrderId(reqVO.getProductionOrderId())
                .period(period)
                .status(30) // 进行中
                .triggerType(10) // 手动
                .remark(reqVO.getRemark())
                .startedAt(LocalDateTime.now())
                .operatorId(userId)
                .build();
        rebuildLogMapper.insert(rebuildLog);

        try {
            // 2. 删除该范围的旧结果和明细
            LambdaQueryWrapperX<ErpFinanceDualProductCostResultDO> deleteQuery = new LambdaQueryWrapperX<ErpFinanceDualProductCostResultDO>()
                    .eq(ErpFinanceDualProductCostResultDO::getProductId, productId)
                    .eq(ErpFinanceDualProductCostResultDO::getPeriod, period);
            if (reqVO.getProductionOrderId() != null) {
                deleteQuery.eq(ErpFinanceDualProductCostResultDO::getProductionOrderId, reqVO.getProductionOrderId());
            }
            if (reqVO.getProductBatchNo() != null) {
                deleteQuery.eq(ErpFinanceDualProductCostResultDO::getProductBatchNo, reqVO.getProductBatchNo());
            }
            productCostResultMapper.delete(deleteQuery);

            // 3. 查询该期间相关的自制入库/委外入库凭证
            List<ErpFinanceVoucherDO> vouchers = voucherMapper.selectList(
                    new LambdaQueryWrapperX<ErpFinanceVoucherDO>()
                            .in(ErpFinanceVoucherDO::getBizType,
                                    ErpBizTypeEnum.PRODUCTION_INBOUND.getType(),
                                    ErpBizTypeEnum.OUTSOURCE_INBOUND.getType())
                            .ge(ErpFinanceVoucherDO::getVoucherTime, parsePeriodStart(period))
                            .le(ErpFinanceVoucherDO::getVoucherTime, parsePeriodEnd(period)));

            // 4. 按料/工/费归集（基于凭证分录科目判断）
            //    加载产品成本差异配置（bizType=70 表示产品成本归集）
            final Integer PRODUCT_COST_BIZ_TYPE = 70;
            List<ErpFinanceDualLedgerDiffConfigDO> diffConfigs = dualLedgerDiffConfigService
                    .getDualLedgerDiffConfigList(PRODUCT_COST_BIZ_TYPE, null);
            Map<Integer, ErpFinanceDualLedgerDiffConfigDO> diffConfigMap = new java.util.HashMap<>();
            if (diffConfigs != null) {
                for (ErpFinanceDualLedgerDiffConfigDO cfg : diffConfigs) {
                    diffConfigMap.put(cfg.getDiffItemType(), cfg);
                }
            }

            BigDecimal materialExternal = BigDecimal.ZERO;
            BigDecimal materialInternal = BigDecimal.ZERO;
            BigDecimal laborExternal = BigDecimal.ZERO;
            BigDecimal laborInternal = BigDecimal.ZERO;
            BigDecimal overheadExternal = BigDecimal.ZERO;
            BigDecimal overheadInternal = BigDecimal.ZERO;
            List<ErpFinanceDualProductCostItemDO> items = new ArrayList<>();

            for (ErpFinanceVoucherDO voucher : vouchers) {
                // 查询该凭证的分录，按科目判断成本构成
                List<ErpFinanceVoucherEntryDO> entries = voucherEntryMapper.selectList(
                        new LambdaQueryWrapperX<ErpFinanceVoucherEntryDO>()
                                .eq(ErpFinanceVoucherEntryDO::getVoucherId, voucher.getId()));

                for (ErpFinanceVoucherEntryDO entry : entries) {
                    BigDecimal amount = ObjectUtil.defaultIfNull(entry.getDebitAmount(), BigDecimal.ZERO);
                    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                        continue;
                    }

                    // 根据科目编码判断成本构成类型
                    Integer costComponentType = resolveCostComponentType(entry.getSubjectCode());
                    Integer sourceType = resolveSourceType(costComponentType);

                    // 根据差异配置计算外部账金额
                    ErpFinanceDualLedgerDiffConfigDO diffConfig = diffConfigMap.get(costComponentType);
                    BigDecimal itemExternalAmount;
                    if (diffConfig != null && diffConfig.getCalculationType() != null) {
                        itemExternalAmount = amountDiffCalculatorFactory.calculate(
                                diffConfig.getCalculationType(), amount,
                                diffConfig.getRatio(), diffConfig.getFixedAmount());
                    } else {
                        itemExternalAmount = amount;
                    }

                    switch (costComponentType) {
                        case 10: // 材料
                            materialExternal = materialExternal.add(itemExternalAmount);
                            materialInternal = materialInternal.add(amount);
                            break;
                        case 20: // 人工
                            laborExternal = laborExternal.add(itemExternalAmount);
                            laborInternal = laborInternal.add(amount);
                            break;
                        case 30: // 制造费用
                            overheadExternal = overheadExternal.add(itemExternalAmount);
                            overheadInternal = overheadInternal.add(amount);
                            break;
                        default:
                            overheadExternal = overheadExternal.add(itemExternalAmount);
                            overheadInternal = overheadInternal.add(amount);
                            break;
                    }

                    ErpFinanceDualProductCostItemDO item = ErpFinanceDualProductCostItemDO.builder()
                            .costComponentType(costComponentType)
                            .sourceType(sourceType)
                            .sourceBizType(voucher.getBizType())
                            .sourceBizId(voucher.getBizId())
                            .sourceNo(voucher.getBizNo())
                            .productId(productId)
                            .productionOrderId(reqVO.getProductionOrderId())
                            .period(period)
                            .externalAmount(itemExternalAmount)
                            .internalAmount(amount)
                            .diffAmount(amount.subtract(itemExternalAmount))
                            .build();
                    items.add(item);
                }
            }

            // 5. 插入结果
            BigDecimal totalExternal = materialExternal.add(laborExternal).add(overheadExternal);
            BigDecimal totalInternal = materialInternal.add(laborInternal).add(overheadInternal);
            ErpFinanceDualProductCostResultDO result = ErpFinanceDualProductCostResultDO.builder()
                    .productId(productId)
                    .productBatchNo(reqVO.getProductBatchNo())
                    .productionOrderId(reqVO.getProductionOrderId())
                    .period(period)
                    .externalMaterialAmount(materialExternal)
                    .internalMaterialAmount(materialInternal)
                    .externalLaborAmount(laborExternal)
                    .internalLaborAmount(laborInternal)
                    .externalOverheadAmount(overheadExternal)
                    .internalOverheadAmount(overheadInternal)
                    .externalTotalAmount(totalExternal)
                    .internalTotalAmount(totalInternal)
                    .diffAmount(totalInternal.subtract(totalExternal))
                    .status(0)
                    .versionNo(1)
                    .lastRebuildTime(LocalDateTime.now())
                    .lastRebuildBy(userId)
                    .build();
            productCostResultMapper.insert(result);

            // 6. 插入明细
            for (ErpFinanceDualProductCostItemDO item : items) {
                item.setResultId(result.getId());
                productCostItemMapper.insert(item);
            }

            // 7. 更新日志为成功
            rebuildLog.setStatus(10); // 成功
            rebuildLog.setFinishedAt(LocalDateTime.now());
            rebuildLog.setAffectedCount(items.size());
            rebuildLogMapper.updateById(rebuildLog);

        } catch (Exception e) {
            log.error("产品级重跑失败: productId={}, period={}", productId, period, e);
            rebuildLog.setStatus(20); // 失败
            rebuildLog.setErrorMessage(e.getMessage());
            rebuildLog.setFinishedAt(LocalDateTime.now());
            rebuildLogMapper.updateById(rebuildLog);
            throw e;
        }
    }

    @Override
    public int rebuildBatchByPeriod(Long userId, String period, String remark) {
        // 查出该期间所有有自制入库/委外入库凭证的产品
        List<ErpFinanceVoucherDO> vouchers = voucherMapper.selectList(
                new LambdaQueryWrapperX<ErpFinanceVoucherDO>()
                        .in(ErpFinanceVoucherDO::getBizType,
                                ErpBizTypeEnum.PRODUCTION_INBOUND.getType(),
                                ErpBizTypeEnum.OUTSOURCE_INBOUND.getType())
                        .ge(ErpFinanceVoucherDO::getVoucherTime, parsePeriodStart(period))
                        .le(ErpFinanceVoucherDO::getVoucherTime, parsePeriodEnd(period)));
        Set<Long> productIds = vouchers.stream()
                .map(v -> (Long) null) // 凭证上暂无 productId，用 null 触发全量重跑
                .collect(Collectors.toSet());
        productIds.add(null); // 确保至少执行一次

        int successCount = 0;
        for (Long pid : productIds) {
            try {
                ErpFinanceDualProductCostRebuildReqVO reqVO = new ErpFinanceDualProductCostRebuildReqVO();
                reqVO.setProductId(pid != null ? pid : 0L);
                reqVO.setPeriod(period);
                reqVO.setRemark(remark != null ? remark : "批量重跑");
                rebuildProductDualCost(userId, reqVO);
                successCount++;
            } catch (Exception e) {
                log.warn("批量重跑跳过产品: productId={}, period={}, error={}", pid, period, e.getMessage());
            }
        }
        return successCount;
    }

    @Override
    public void exportExternalProductCost(ErpFinanceDualProductCostPageReqVO pageReqVO,
                                          jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
        List<ErpFinanceDualProductCostResultDO> list = productCostResultMapper.selectList(
                new LambdaQueryWrapperX<ErpFinanceDualProductCostResultDO>()
                        .eqIfPresent(ErpFinanceDualProductCostResultDO::getProductId, pageReqVO.getProductId())
                        .likeIfPresent(ErpFinanceDualProductCostResultDO::getProductNo, pageReqVO.getProductNo())
                        .likeIfPresent(ErpFinanceDualProductCostResultDO::getProductName, pageReqVO.getProductName())
                        .eqIfPresent(ErpFinanceDualProductCostResultDO::getPeriod, pageReqVO.getPeriod())
                        .orderByDesc(ErpFinanceDualProductCostResultDO::getPeriod)
                        .orderByAsc(ErpFinanceDualProductCostResultDO::getProductId));
        writeProductCostExcel(list, "外部账", response);
    }

    @Override
    public void exportInternalProductCost(ErpFinanceDualProductCostPageReqVO pageReqVO,
                                          jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
        List<ErpFinanceDualProductCostResultDO> list = productCostResultMapper.selectList(
                new LambdaQueryWrapperX<ErpFinanceDualProductCostResultDO>()
                        .eqIfPresent(ErpFinanceDualProductCostResultDO::getProductId, pageReqVO.getProductId())
                        .likeIfPresent(ErpFinanceDualProductCostResultDO::getProductNo, pageReqVO.getProductNo())
                        .likeIfPresent(ErpFinanceDualProductCostResultDO::getProductName, pageReqVO.getProductName())
                        .eqIfPresent(ErpFinanceDualProductCostResultDO::getPeriod, pageReqVO.getPeriod())
                        .orderByDesc(ErpFinanceDualProductCostResultDO::getPeriod)
                        .orderByAsc(ErpFinanceDualProductCostResultDO::getProductId));
        writeProductCostExcel(list, "内部账", response);
    }

    /**
     * 通用产品双账成本 Excel 导出
     */
    private void writeProductCostExcel(List<ErpFinanceDualProductCostResultDO> list, String ledgerLabel,
                                       jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
        String exportTime = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String fileName = "产品" + ledgerLabel + "成本-" + exportTime.substring(0, 10) + ".xlsx";
        String encodedFileName = java.net.URLEncoder.encode(fileName, "UTF-8").replace("+", "%20");

        try (cn.idev.excel.ExcelWriter writer = cn.idev.excel.FastExcel.write(response.getOutputStream()).build()) {
            cn.idev.excel.write.metadata.WriteSheet sheet = cn.idev.excel.FastExcel.writerSheet("产品成本").build();

            // 上下文区
            cn.idev.excel.write.metadata.WriteTable ctxTable = cn.idev.excel.FastExcel.writerTable().build();
            List<List<String>> ctxHead = new ArrayList<>();
            ctxHead.add(java.util.Collections.singletonList("产品" + ledgerLabel + "成本导出"));
            List<List<Object>> ctxData = new ArrayList<>();
            ctxData.add(java.util.Collections.singletonList("导出时间：" + exportTime));
            ctxData.add(java.util.Collections.singletonList("数据口径：" + ledgerLabel));
            ctxData.add(java.util.Collections.singletonList("记录数：" + list.size()));
            writer.write(ctxData, sheet, ctxTable);

            // 空行
            cn.idev.excel.write.metadata.WriteTable emptyTable = cn.idev.excel.FastExcel.writerTable().build();
            writer.write(java.util.Collections.singletonList(java.util.Collections.singletonList("")), sheet, emptyTable);

            // 数据表
            cn.idev.excel.write.metadata.WriteTable dataTable = cn.idev.excel.FastExcel.writerTable().build();
            List<List<String>> head = new ArrayList<>();
            head.add(java.util.Collections.singletonList("产品编号"));
            head.add(java.util.Collections.singletonList("产品名称"));
            head.add(java.util.Collections.singletonList("工单编号"));
            head.add(java.util.Collections.singletonList("批次号"));
            head.add(java.util.Collections.singletonList("期间"));
            head.add(java.util.Collections.singletonList("材料成本(外部)"));
            head.add(java.util.Collections.singletonList("材料成本(内部)"));
            head.add(java.util.Collections.singletonList("人工成本(外部)"));
            head.add(java.util.Collections.singletonList("人工成本(内部)"));
            head.add(java.util.Collections.singletonList("制造费用(外部)"));
            head.add(java.util.Collections.singletonList("制造费用(内部)"));
            head.add(java.util.Collections.singletonList("总成本(外部)"));
            head.add(java.util.Collections.singletonList("总成本(内部)"));
            head.add(java.util.Collections.singletonList("差异金额"));

            List<List<Object>> data = new ArrayList<>();
            for (ErpFinanceDualProductCostResultDO row : list) {
                List<Object> r = new ArrayList<>();
                r.add(row.getProductNo() != null ? row.getProductNo() : "");
                r.add(row.getProductName() != null ? row.getProductName() : "");
                r.add(row.getProductionOrderNo() != null ? row.getProductionOrderNo() : "");
                r.add(row.getProductBatchNo() != null ? row.getProductBatchNo() : "");
                r.add(row.getPeriod() != null ? row.getPeriod() : "");
                r.add(row.getExternalMaterialAmount() != null ? row.getExternalMaterialAmount() : java.math.BigDecimal.ZERO);
                r.add(row.getInternalMaterialAmount() != null ? row.getInternalMaterialAmount() : java.math.BigDecimal.ZERO);
                r.add(row.getExternalLaborAmount() != null ? row.getExternalLaborAmount() : java.math.BigDecimal.ZERO);
                r.add(row.getInternalLaborAmount() != null ? row.getInternalLaborAmount() : java.math.BigDecimal.ZERO);
                r.add(row.getExternalOverheadAmount() != null ? row.getExternalOverheadAmount() : java.math.BigDecimal.ZERO);
                r.add(row.getInternalOverheadAmount() != null ? row.getInternalOverheadAmount() : java.math.BigDecimal.ZERO);
                r.add(row.getExternalTotalAmount() != null ? row.getExternalTotalAmount() : java.math.BigDecimal.ZERO);
                r.add(row.getInternalTotalAmount() != null ? row.getInternalTotalAmount() : java.math.BigDecimal.ZERO);
                r.add(row.getDiffAmount() != null ? row.getDiffAmount() : java.math.BigDecimal.ZERO);
                data.add(r);
            }
            writer.write(data, sheet, dataTable);
        }

        response.setContentType("application/vnd.ms-excel;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);
    }

    // ========== 私有方法 ==========

    private List<ErpFinanceDualProductCostRespVO> convertToRespVOList(List<ErpFinanceDualProductCostResultDO> list) {
        return list.stream().map(this::convertToRespVO).collect(Collectors.toList());
    }

    private ErpFinanceDualProductCostRespVO convertToRespVO(ErpFinanceDualProductCostResultDO result) {
        ErpFinanceDualProductCostRespVO respVO = new ErpFinanceDualProductCostRespVO();
        respVO.setId(result.getId());
        respVO.setProductId(result.getProductId());
        respVO.setProductNo(result.getProductNo());
        respVO.setProductName(result.getProductName());
        respVO.setProductBatchNo(result.getProductBatchNo());
        respVO.setProductionOrderId(result.getProductionOrderId());
        respVO.setProductionOrderNo(result.getProductionOrderNo());
        respVO.setPeriod(result.getPeriod());
        respVO.setExternalMaterialAmount(result.getExternalMaterialAmount());
        respVO.setInternalMaterialAmount(result.getInternalMaterialAmount());
        respVO.setExternalLaborAmount(result.getExternalLaborAmount());
        respVO.setInternalLaborAmount(result.getInternalLaborAmount());
        respVO.setExternalOverheadAmount(result.getExternalOverheadAmount());
        respVO.setInternalOverheadAmount(result.getInternalOverheadAmount());
        respVO.setExternalTotalAmount(result.getExternalTotalAmount());
        respVO.setInternalTotalAmount(result.getInternalTotalAmount());
        respVO.setDiffAmount(result.getDiffAmount());
        respVO.setStatus(result.getStatus());
        respVO.setVersionNo(result.getVersionNo());
        respVO.setLastRebuildTime(result.getLastRebuildTime());
        respVO.setRemark(result.getRemark());
        respVO.setCreateTime(result.getCreateTime());
        return respVO;
    }

    private ErpFinanceDualProductCostRespVO convertItemToRespVO(ErpFinanceDualProductCostItemDO item) {
        ErpFinanceDualProductCostRespVO respVO = new ErpFinanceDualProductCostRespVO();
        respVO.setId(item.getId());
        respVO.setExternalMaterialAmount(item.getExternalAmount());
        respVO.setInternalMaterialAmount(item.getInternalAmount());
        respVO.setDiffAmount(item.getDiffAmount());
        return respVO;
    }

    private LocalDateTime parsePeriodStart(String period) {
        YearMonth ym = YearMonth.parse(period);
        return ym.atDay(1).atStartOfDay();
    }

    private LocalDateTime parsePeriodEnd(String period) {
        YearMonth ym = YearMonth.parse(period);
        return ym.atEndOfMonth().atTime(23, 59, 59);
    }

    /**
     * 根据科目编码判断成本构成类型
     * 10-材料（原材料、包装物等）
     * 20-人工（应付职工薪酬等）
     * 30-制造费用（折旧、电费、其他制造费用等）
     */
    private Integer resolveCostComponentType(String subjectCode) {
        if (subjectCode == null || subjectCode.isEmpty()) {
            return 30; // 默认归入制造费用
        }
        // 材料类科目：1403原材料、1405库存商品、1406包装物
        if (subjectCode.startsWith("1403") || subjectCode.startsWith("1405") || subjectCode.startsWith("1406")) {
            return 10;
        }
        // 人工类科目：2211应付职工薪酬
        if (subjectCode.startsWith("2211")) {
            return 20;
        }
        // 制造费用类科目：5101制造费用
        if (subjectCode.startsWith("5101")) {
            return 30;
        }
        return 30; // 默认归入制造费用
    }

    /**
     * 根据成本构成类型判断来源类型
     * 10-材料 → 10（材料领用）
     * 20-人工 → 20（人工工时）
     * 30-制造费用 → 30（折旧）/ 40（电费）/ 50（其他）
     */
    private Integer resolveSourceType(Integer costComponentType) {
        if (costComponentType == null) {
            return 50;
        }
        switch (costComponentType) {
            case 10: return 10; // 材料领用
            case 20: return 20; // 人工工时
            case 30: return 30; // 折旧（简化处理，后续可按子科目细分）
            default: return 50; // 其他
        }
    }
}
