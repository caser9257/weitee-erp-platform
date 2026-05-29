package cn.iocoder.yudao.module.erp.service.finance;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.expense.ErpFinanceResearchExpenseSummaryReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.expense.ErpFinanceResearchExpenseSummaryRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceExpenseDO;
import cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpFinanceExpenseMapper;
import cn.iocoder.yudao.module.erp.enums.ErpAuditStatus;
import cn.iocoder.yudao.module.erp.enums.ErpResearchExpenseCategoryEnum;
import cn.iocoder.yudao.module.erp.enums.common.ErpBizTypeEnum;
import cn.iocoder.yudao.module.erp.service.project.ErpProjectService;
import cn.iocoder.yudao.module.erp.dal.dataobject.project.ErpProjectDO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ErpFinanceResearchExpenseServiceImpl implements ErpFinanceResearchExpenseService {
    
    @Resource
    private ErpFinanceExpenseMapper financeExpenseMapper;
    
    @Resource
    private ErpProjectService projectService;
    
    @Resource
    private ErpFinanceVoucherService voucherService;
    
    @Override
    public List<ErpFinanceResearchExpenseSummaryRespVO> getResearchExpenseSummary(
            ErpFinanceResearchExpenseSummaryReqVO reqVO) {
        // 查询研发费用数据（只查询已审核的）
        List<ErpFinanceExpenseDO> expenses = financeExpenseMapper.selectListByResearchExpense(reqVO);
        if (CollUtil.isEmpty(expenses)) {
            return Collections.emptyList();
        }
        
        // 批量查询项目名称，避免 N+1 查询
        Set<Long> projectIds = expenses.stream()
                .map(ErpFinanceExpenseDO::getProjectId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, ErpProjectDO> projectMap = CollUtil.isEmpty(projectIds)
                ? Collections.emptyMap()
                : projectService.getProjectList(projectIds).stream()
                        .collect(Collectors.toMap(ErpProjectDO::getId, p -> p, (a, b) -> a));
        
        // 按项目和研发支出分类分组汇总
        Map<String, ErpFinanceResearchExpenseSummaryRespVO> summaryMap = new LinkedHashMap<>();
        for (ErpFinanceExpenseDO expense : expenses) {
            String key = ObjectUtil.defaultIfNull(expense.getProjectId(), 0L) + "_" + 
                         ObjectUtil.defaultIfNull(expense.getResearchCategory(), 0);
            ErpFinanceResearchExpenseSummaryRespVO summary = summaryMap.computeIfAbsent(key, item -> {
                ErpFinanceResearchExpenseSummaryRespVO respVO = new ErpFinanceResearchExpenseSummaryRespVO();
                respVO.setProjectId(expense.getProjectId());
                respVO.setResearchCategory(expense.getResearchCategory());
                respVO.setResearchCategoryName(resolveResearchCategoryName(expense.getResearchCategory()));
                respVO.setTotalAmount(BigDecimal.ZERO);
                respVO.setExpenseAmount(BigDecimal.ZERO);
                respVO.setCapitalizeAmount(BigDecimal.ZERO);
                respVO.setCount(0);
                // 设置项目名称（从预加载的缓存中获取）
                if (expense.getProjectId() != null) {
                    ErpProjectDO project = projectMap.get(expense.getProjectId());
                    if (project != null) {
                        respVO.setProjectName(project.getName());
                    }
                }
                return respVO;
            });
            
            // 累加金额
            BigDecimal expensePrice = ObjectUtil.defaultIfNull(expense.getExpensePrice(), BigDecimal.ZERO);
            summary.setTotalAmount(summary.getTotalAmount().add(expensePrice));
            summary.setCount(summary.getCount() + 1);
            
            // 根据研发支出分类累加金额
            if (ErpResearchExpenseCategoryEnum.EXPENSE.getType().equals(expense.getResearchCategory())) {
                summary.setExpenseAmount(summary.getExpenseAmount().add(expensePrice));
            } else if (ErpResearchExpenseCategoryEnum.CAPITALIZE.getType().equals(expense.getResearchCategory())) {
                summary.setCapitalizeAmount(summary.getCapitalizeAmount().add(expensePrice));
            }
        }
        
        return new ArrayList<>(summaryMap.values());
    }
    
    private String resolveResearchCategoryName(Integer researchCategory) {
        ErpResearchExpenseCategoryEnum categoryEnum = ErpResearchExpenseCategoryEnum.fromType(researchCategory);
        return categoryEnum == null ? null : categoryEnum.getName();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void monthlyCarryForward(Long ledgerId, Integer year, Integer month) {
        // 1. 查询当月研发费用汇总
        LocalDateTime beginTime = LocalDateTime.of(year, month, 1, 0, 0, 0);
        int lastDay = LocalDate.of(year, month, 1).lengthOfMonth();
        LocalDateTime endTime = LocalDateTime.of(year, month, lastDay, 23, 59, 59);
        
        ErpFinanceResearchExpenseSummaryReqVO reqVO = new ErpFinanceResearchExpenseSummaryReqVO();
        reqVO.setBeginTime(beginTime);
        reqVO.setEndTime(endTime);
        
        List<ErpFinanceResearchExpenseSummaryRespVO> summaryList = getResearchExpenseSummary(reqVO);
        if (CollUtil.isEmpty(summaryList)) {
            return; // 没有研发费用，无需结转
        }
        
        // 2. 查询当月所有已审核的研发费用，逐笔生成结转凭证
        List<ErpFinanceExpenseDO> expenses = financeExpenseMapper.selectListByResearchExpense(reqVO);
        if (CollUtil.isEmpty(expenses)) {
            return;
        }
        
        for (ErpFinanceExpenseDO expense : expenses) {
            // 为每笔费用自动生成凭证（如果已有凭证则跳过）
            voucherService.autoGenerateVoucher(ErpBizTypeEnum.RESEARCH_EXPENSE.getType(), expense.getId());
            
            // 3. 更新费用状态为已结转
            financeExpenseMapper.updateById(expense.setStatus(ErpAuditStatus.CARRY_FORWARD.getStatus()));
        }
    }
}