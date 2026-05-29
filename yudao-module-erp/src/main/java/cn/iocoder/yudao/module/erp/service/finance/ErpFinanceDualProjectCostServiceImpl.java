package cn.iocoder.yudao.module.erp.service.finance;

import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.projectdualcost.ErpFinanceDualProjectCostPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.projectdualcost.ErpFinanceDualProjectCostRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.projectdualcost.ErpFinanceDualProjectCostRebuildReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceDualProjectCostResultDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceDualProjectCostItemDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceDualProjectCostRebuildLogDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceDualLedgerDiffConfigDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceExpenseDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceVoucherDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceVoucherEntryDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.project.ErpProjectDO;
import cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpFinanceDualProjectCostResultMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpFinanceDualProjectCostItemMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpFinanceDualProjectCostRebuildLogMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpFinanceExpenseMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpFinanceVoucherMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpFinanceVoucherEntryMapper;
import cn.iocoder.yudao.module.erp.service.project.ErpProjectService;
import cn.iocoder.yudao.module.erp.enums.common.ErpBizTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 项目双账成本 Service 实现
 */
@Slf4j
@Service
@Validated
public class ErpFinanceDualProjectCostServiceImpl implements ErpFinanceDualProjectCostService {

    @Resource
    private ErpFinanceDualProjectCostResultMapper projectCostResultMapper;
    @Resource
    private ErpFinanceDualProjectCostItemMapper projectCostItemMapper;
    @Resource
    private ErpFinanceDualProjectCostRebuildLogMapper rebuildLogMapper;
    @Resource
    private ErpFinanceExpenseMapper expenseMapper;
    @Resource
    private ErpFinanceVoucherMapper voucherMapper;
    @Resource
    private ErpFinanceVoucherEntryMapper voucherEntryMapper;
    @Resource
    private ErpProjectService projectService;
    @Resource
    private ErpFinanceDualLedgerConfigService dualLedgerConfigService;
    @Resource
    private ErpFinanceLedgerService financeLedgerService;
    @Resource
    private ErpFinanceDualLedgerDiffConfigService dualLedgerDiffConfigService;
    @Resource
    private cn.iocoder.yudao.module.erp.service.finance.diffcalc.AmountDiffCalculatorFactory amountDiffCalculatorFactory;

    @Override
    public PageResult<ErpFinanceDualProjectCostRespVO> getProjectDualCostPage(ErpFinanceDualProjectCostPageReqVO pageReqVO) {
        // 构建查询条件
        LambdaQueryWrapperX<ErpFinanceDualProjectCostResultDO> query = new LambdaQueryWrapperX<ErpFinanceDualProjectCostResultDO>()
                .eqIfPresent(ErpFinanceDualProjectCostResultDO::getProjectId, pageReqVO.getProjectId())
                .likeIfPresent(ErpFinanceDualProjectCostResultDO::getProjectNo, pageReqVO.getProjectNo())
                .likeIfPresent(ErpFinanceDualProjectCostResultDO::getProjectName, pageReqVO.getProjectName())
                .eqIfPresent(ErpFinanceDualProjectCostResultDO::getPeriod, pageReqVO.getPeriod())
                .eqIfPresent(ErpFinanceDualProjectCostResultDO::getCostType, pageReqVO.getCostType())
                .orderByDesc(ErpFinanceDualProjectCostResultDO::getPeriod)
                .orderByDesc(ErpFinanceDualProjectCostResultDO::getProjectId);

        PageResult<ErpFinanceDualProjectCostResultDO> pageResult = projectCostResultMapper.selectPage(pageReqVO, query);
        return new PageResult<>(convertToRespVOList(pageResult.getList()), pageResult.getTotal());
    }

    @Override
    public ErpFinanceDualProjectCostRespVO getProjectDualCost(Long id) {
        ErpFinanceDualProjectCostResultDO result = projectCostResultMapper.selectById(id);
        return result != null ? convertToRespVO(result) : null;
    }

    @Override
    public List<ErpFinanceDualProjectCostRespVO> getProjectDualCostItems(Long resultId) {
        List<ErpFinanceDualProjectCostItemDO> items = projectCostItemMapper.selectList(
                new LambdaQueryWrapperX<ErpFinanceDualProjectCostItemDO>()
                        .eq(ErpFinanceDualProjectCostItemDO::getResultId, resultId)
                        .orderByAsc(ErpFinanceDualProjectCostItemDO::getCostType));
        return items.stream().map(this::convertItemToRespVO).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rebuildProjectDualCost(Long userId, ErpFinanceDualProjectCostRebuildReqVO reqVO) {
        Long projectId = reqVO.getProjectId();
        String period = reqVO.getPeriod();

        // 1. 记录重跑日志
        ErpFinanceDualProjectCostRebuildLogDO rebuildLog = ErpFinanceDualProjectCostRebuildLogDO.builder()
                .projectId(projectId)
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
            projectCostResultMapper.delete(new LambdaQueryWrapperX<ErpFinanceDualProjectCostResultDO>()
                    .eq(ErpFinanceDualProjectCostResultDO::getProjectId, projectId)
                    .eq(ErpFinanceDualProjectCostResultDO::getPeriod, period));
            projectCostItemMapper.delete(new LambdaQueryWrapperX<ErpFinanceDualProjectCostItemDO>()
                    .eq(ErpFinanceDualProjectCostItemDO::getProjectId, projectId)
                    .eq(ErpFinanceDualProjectCostItemDO::getPeriod, period));

            // 3. 查询该期间已审核的费用报销单（绑定该项目）
            List<ErpFinanceExpenseDO> expenses = expenseMapper.selectList(
                    new LambdaQueryWrapperX<ErpFinanceExpenseDO>()
                            .eq(ErpFinanceExpenseDO::getProjectId, projectId)
                            .eq(ErpFinanceExpenseDO::getStatus, 10) // 已审核
                            .ge(ErpFinanceExpenseDO::getExpenseTime, parsePeriodStart(period))
                            .le(ErpFinanceExpenseDO::getExpenseTime, parsePeriodEnd(period)));

            // 4. 按成本类别归集
            Map<Integer, List<ErpFinanceExpenseDO>> groupedByCostType = expenses.stream()
                    .collect(Collectors.groupingBy(e -> resolveCostType(e)));

            // 5. 加载项目成本差异配置（bizType=60 表示项目成本归集）
            //    diffItemType 映射：10-材料, 20-人工, 30-折旧, 40-电费, 50-其他
            //    costType 使用同一套枚举值，可直接复用
            final Integer PROJECT_COST_BIZ_TYPE = 60;
            List<ErpFinanceDualLedgerDiffConfigDO> diffConfigs = dualLedgerDiffConfigService
                    .getDualLedgerDiffConfigList(PROJECT_COST_BIZ_TYPE, null);
            Map<Integer, ErpFinanceDualLedgerDiffConfigDO> diffConfigMap = new HashMap<>();
            if (diffConfigs != null) {
                for (ErpFinanceDualLedgerDiffConfigDO cfg : diffConfigs) {
                    diffConfigMap.put(cfg.getDiffItemType(), cfg);
                }
            }

            // 6. 生成结果和明细
            int affectedCount = 0;
            for (Map.Entry<Integer, List<ErpFinanceExpenseDO>> entry : groupedByCostType.entrySet()) {
                Integer costType = entry.getKey();
                List<ErpFinanceExpenseDO> expenseList = entry.getValue();

                // 查找该成本类别的差异配置
                ErpFinanceDualLedgerDiffConfigDO diffConfig = diffConfigMap.get(costType);
                BigDecimal externalAmount = BigDecimal.ZERO;
                BigDecimal internalAmount = BigDecimal.ZERO;
                List<ErpFinanceDualProjectCostItemDO> items = new ArrayList<>();

                for (ErpFinanceExpenseDO expense : expenseList) {
                    BigDecimal amount = ObjectUtil.defaultIfNull(expense.getExpensePrice(), BigDecimal.ZERO);
                    internalAmount = internalAmount.add(amount);

                    // 根据差异配置计算外部账金额
                    BigDecimal itemExternalAmount;
                    if (diffConfig != null && diffConfig.getCalculationType() != null) {
                        itemExternalAmount = amountDiffCalculatorFactory.calculate(
                                diffConfig.getCalculationType(), amount,
                                diffConfig.getRatio(), diffConfig.getFixedAmount());
                    } else {
                        itemExternalAmount = amount;
                    }
                    externalAmount = externalAmount.add(itemExternalAmount);

                    // 构建明细
                    ErpFinanceDualProjectCostItemDO item = ErpFinanceDualProjectCostItemDO.builder()
                            .sourceType(10) // 费用报销
                            .sourceBizType(ErpBizTypeEnum.FINANCE_EXPENSE.getType())
                            .sourceBizId(expense.getId())
                            .sourceNo(expense.getNo())
                            .costType(costType)
                            .externalAmount(itemExternalAmount)
                            .internalAmount(amount)
                            .diffAmount(amount.subtract(itemExternalAmount))
                            .projectId(projectId)
                            .period(period)
                            .build();
                    items.add(item);
                }

                // 插入结果
                ErpFinanceDualProjectCostResultDO result = ErpFinanceDualProjectCostResultDO.builder()
                        .projectId(projectId)
                        .projectNo(resolveProjectNo(projectId))
                        .projectName(resolveProjectName(projectId))
                        .period(period)
                        .costType(costType)
                        .externalAmount(externalAmount)
                        .internalAmount(internalAmount)
                        .diffAmount(internalAmount.subtract(externalAmount))
                        .sourceCount(expenseList.size())
                        .status(0)
                        .versionNo(1)
                        .lastRebuildTime(LocalDateTime.now())
                        .lastRebuildBy(userId)
                        .build();
                projectCostResultMapper.insert(result);

                // 插入明细
                for (ErpFinanceDualProjectCostItemDO item : items) {
                    item.setResultId(result.getId());
                    projectCostItemMapper.insert(item);
                }
                affectedCount += expenseList.size();
            }

            // 6. 更新日志为成功
            rebuildLog.setStatus(10); // 成功
            rebuildLog.setFinishedAt(LocalDateTime.now());
            rebuildLog.setAffectedCount(affectedCount);
            rebuildLogMapper.updateById(rebuildLog);

        } catch (Exception e) {
            log.error("项目级重跑失败: projectId={}, period={}", projectId, period, e);
            rebuildLog.setStatus(20); // 失败
            rebuildLog.setErrorMessage(e.getMessage());
            rebuildLog.setFinishedAt(LocalDateTime.now());
            rebuildLogMapper.updateById(rebuildLog);
            throw e;
        }
    }

    @Override
    public int rebuildBatchByPeriod(Long userId, String period, String remark) {
        // 查出该期间所有有费用报销的项目
        List<ErpFinanceExpenseDO> expenses = expenseMapper.selectList(
                new LambdaQueryWrapperX<ErpFinanceExpenseDO>()
                        .eq(ErpFinanceExpenseDO::getStatus, 10)
                        .isNotNull(ErpFinanceExpenseDO::getProjectId)
                        .ge(ErpFinanceExpenseDO::getExpenseTime, parsePeriodStart(period))
                        .le(ErpFinanceExpenseDO::getExpenseTime, parsePeriodEnd(period)));
        Set<Long> projectIds = expenses.stream()
                .map(ErpFinanceExpenseDO::getProjectId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());

        int successCount = 0;
        for (Long pid : projectIds) {
            try {
                ErpFinanceDualProjectCostRebuildReqVO reqVO = new ErpFinanceDualProjectCostRebuildReqVO();
                reqVO.setProjectId(pid);
                reqVO.setPeriod(period);
                reqVO.setRemark(remark != null ? remark : "批量重跑");
                rebuildProjectDualCost(userId, reqVO);
                successCount++;
            } catch (Exception e) {
                log.warn("批量重跑跳过项目: projectId={}, period={}, error={}", pid, period, e.getMessage());
            }
        }
        return successCount;
    }

    @Override
    public void exportExternalProjectCost(ErpFinanceDualProjectCostPageReqVO pageReqVO,
                                          javax.servlet.http.HttpServletResponse response) throws java.io.IOException {
        List<ErpFinanceDualProjectCostResultDO> list = projectCostResultMapper.selectList(
                new LambdaQueryWrapperX<ErpFinanceDualProjectCostResultDO>()
                        .eqIfPresent(ErpFinanceDualProjectCostResultDO::getProjectId, pageReqVO.getProjectId())
                        .likeIfPresent(ErpFinanceDualProjectCostResultDO::getProjectNo, pageReqVO.getProjectNo())
                        .likeIfPresent(ErpFinanceDualProjectCostResultDO::getProjectName, pageReqVO.getProjectName())
                        .eqIfPresent(ErpFinanceDualProjectCostResultDO::getPeriod, pageReqVO.getPeriod())
                        .eqIfPresent(ErpFinanceDualProjectCostResultDO::getCostType, pageReqVO.getCostType())
                        .orderByDesc(ErpFinanceDualProjectCostResultDO::getPeriod)
                        .orderByAsc(ErpFinanceDualProjectCostResultDO::getProjectId));
        writeProjectCostExcel(list, "外部账", response);
    }

    @Override
    public void exportInternalProjectCost(ErpFinanceDualProjectCostPageReqVO pageReqVO,
                                          javax.servlet.http.HttpServletResponse response) throws java.io.IOException {
        List<ErpFinanceDualProjectCostResultDO> list = projectCostResultMapper.selectList(
                new LambdaQueryWrapperX<ErpFinanceDualProjectCostResultDO>()
                        .eqIfPresent(ErpFinanceDualProjectCostResultDO::getProjectId, pageReqVO.getProjectId())
                        .likeIfPresent(ErpFinanceDualProjectCostResultDO::getProjectNo, pageReqVO.getProjectNo())
                        .likeIfPresent(ErpFinanceDualProjectCostResultDO::getProjectName, pageReqVO.getProjectName())
                        .eqIfPresent(ErpFinanceDualProjectCostResultDO::getPeriod, pageReqVO.getPeriod())
                        .eqIfPresent(ErpFinanceDualProjectCostResultDO::getCostType, pageReqVO.getCostType())
                        .orderByDesc(ErpFinanceDualProjectCostResultDO::getPeriod)
                        .orderByAsc(ErpFinanceDualProjectCostResultDO::getProjectId));
        writeProjectCostExcel(list, "内部账", response);
    }

    /**
     * 通用项目双账成本 Excel 导出
     */
    private void writeProjectCostExcel(List<ErpFinanceDualProjectCostResultDO> list, String ledgerLabel,
                                       javax.servlet.http.HttpServletResponse response) throws java.io.IOException {
        String exportTime = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String fileName = "项目" + ledgerLabel + "成本-" + exportTime.substring(0, 10) + ".xlsx";
        String encodedFileName = java.net.URLEncoder.encode(fileName, "UTF-8").replace("+", "%20");

        try (org.apache.fesod.sheet.ExcelWriter writer = org.apache.fesod.sheet.FastExcel.write(response.getOutputStream()).build()) {
            org.apache.fesod.sheet.write.metadata.WriteSheet sheet = org.apache.fesod.sheet.FastExcel.writerSheet("项目成本").build();
            org.apache.fesod.sheet.write.metadata.WriteTable ctxTable = org.apache.fesod.sheet.FastExcel.writerTable().build();
            List<List<String>> ctxHead = new ArrayList<>();
            ctxHead.add(java.util.Collections.singletonList("项目" + ledgerLabel + "成本导出"));
            List<List<Object>> ctxData = new ArrayList<>();
            ctxData.add(java.util.Collections.singletonList("导出时间：" + exportTime));
            ctxData.add(java.util.Collections.singletonList("数据口径：" + ledgerLabel));
            ctxData.add(java.util.Collections.singletonList("记录数：" + list.size()));
            writer.write(ctxData, sheet, ctxTable);

            // 空行
            org.apache.fesod.sheet.write.metadata.WriteTable emptyTable = org.apache.fesod.sheet.FastExcel.writerTable().build();
            writer.write(java.util.Collections.singletonList(java.util.Collections.singletonList("")), sheet, emptyTable);

            // 数据表
            org.apache.fesod.sheet.write.metadata.WriteTable dataTable = org.apache.fesod.sheet.FastExcel.writerTable().build();
            List<List<String>> head = new ArrayList<>();
            head.add(java.util.Collections.singletonList("项目编号"));
            head.add(java.util.Collections.singletonList("项目名称"));
            head.add(java.util.Collections.singletonList("期间"));
            head.add(java.util.Collections.singletonList("成本类别"));
            head.add(java.util.Collections.singletonList("外部账金额"));
            head.add(java.util.Collections.singletonList("内部账金额"));
            head.add(java.util.Collections.singletonList("差异金额"));
            head.add(java.util.Collections.singletonList("来源单据数"));

            List<List<Object>> data = new ArrayList<>();
            for (ErpFinanceDualProjectCostResultDO row : list) {
                List<Object> r = new ArrayList<>();
                r.add(row.getProjectNo() != null ? row.getProjectNo() : "");
                r.add(row.getProjectName() != null ? row.getProjectName() : "");
                r.add(row.getPeriod() != null ? row.getPeriod() : "");
                r.add(resolveCostTypeName(row.getCostType()));
                r.add(row.getExternalAmount() != null ? row.getExternalAmount() : java.math.BigDecimal.ZERO);
                r.add(row.getInternalAmount() != null ? row.getInternalAmount() : java.math.BigDecimal.ZERO);
                r.add(row.getDiffAmount() != null ? row.getDiffAmount() : java.math.BigDecimal.ZERO);
                r.add(row.getSourceCount() != null ? row.getSourceCount() : 0);
                data.add(r);
            }
            writer.write(data, sheet, dataTable);
        }

        response.setContentType("application/vnd.ms-excel;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);
    }

    // ========== 私有方法 ==========

    private List<ErpFinanceDualProjectCostRespVO> convertToRespVOList(List<ErpFinanceDualProjectCostResultDO> list) {
        return list.stream().map(this::convertToRespVO).collect(Collectors.toList());
    }

    private ErpFinanceDualProjectCostRespVO convertToRespVO(ErpFinanceDualProjectCostResultDO result) {
        ErpFinanceDualProjectCostRespVO respVO = new ErpFinanceDualProjectCostRespVO();
        respVO.setId(result.getId());
        respVO.setProjectId(result.getProjectId());
        respVO.setProjectNo(result.getProjectNo());
        respVO.setProjectName(result.getProjectName());
        respVO.setPeriod(result.getPeriod());
        respVO.setCostType(result.getCostType());
        respVO.setCostTypeName(resolveCostTypeName(result.getCostType()));
        respVO.setExternalAmount(result.getExternalAmount());
        respVO.setInternalAmount(result.getInternalAmount());
        respVO.setDiffAmount(result.getDiffAmount());
        respVO.setSourceCount(result.getSourceCount());
        respVO.setStatus(result.getStatus());
        respVO.setVersionNo(result.getVersionNo());
        respVO.setLastRebuildTime(result.getLastRebuildTime());
        respVO.setLastRebuildBy(result.getLastRebuildBy());
        respVO.setRemark(result.getRemark());
        respVO.setCreateTime(result.getCreateTime());
        return respVO;
    }

    private ErpFinanceDualProjectCostRespVO convertItemToRespVO(ErpFinanceDualProjectCostItemDO item) {
        ErpFinanceDualProjectCostRespVO respVO = new ErpFinanceDualProjectCostRespVO();
        respVO.setId(item.getId());
        respVO.setCostType(item.getCostType());
        respVO.setCostTypeName(resolveCostTypeName(item.getCostType()));
        respVO.setExternalAmount(item.getExternalAmount());
        respVO.setInternalAmount(item.getInternalAmount());
        respVO.setDiffAmount(item.getDiffAmount());
        return respVO;
    }

    private Integer resolveCostType(ErpFinanceExpenseDO expense) {
        // 根据费用类型或研发分类判断成本类别
        if (expense.getResearchCategory() != null) {
            return 20; // 人工/研发
        }
        return 50; // 其他
    }

    private String resolveCostTypeName(Integer costType) {
        if (costType == null) return "未知";
        switch (costType) {
            case 10: return "材料";
            case 20: return "人工";
            case 30: return "折旧";
            case 40: return "电费";
            case 50: return "其他";
            default: return "未知";
        }
    }

    private String resolveProjectNo(Long projectId) {
        if (projectId == null) return null;
        ErpProjectDO project = projectService.getProject(projectId);
        return project != null ? project.getNo() : null;
    }

    private String resolveProjectName(Long projectId) {
        if (projectId == null) return null;
        ErpProjectDO project = projectService.getProject(projectId);
        return project != null ? project.getName() : null;
    }

    private LocalDateTime parsePeriodStart(String period) {
        YearMonth ym = YearMonth.parse(period);
        return ym.atDay(1).atStartOfDay();
    }

    private LocalDateTime parsePeriodEnd(String period) {
        YearMonth ym = YearMonth.parse(period);
        return ym.atEndOfMonth().atTime(23, 59, 59);
    }
}
