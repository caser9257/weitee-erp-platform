package cn.weitee.erp.module.erp.service.finance;

import cn.hutool.core.collection.CollUtil;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.asset.ErpFinanceAssetDepreciationPageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceAssetDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceAssetDepreciationDO;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceAssetDepreciationMapper;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceAssetMapper;
import cn.weitee.erp.module.erp.enums.ErpFinanceAssetDepreciationStatusEnum;
import cn.weitee.erp.module.erp.enums.ErpFinanceAssetStatusEnum;
import cn.weitee.erp.module.erp.enums.common.ErpBizTypeEnum;
import cn.weitee.erp.module.system.api.dept.DeptApi;
import cn.weitee.erp.module.system.api.dept.dto.DeptRespDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsFinanceAsset.ASSET_DEPRECIATION_PERIOD_DUPLICATE;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsFinanceAsset.ASSET_DEPRECIATION_PERIOD_INVALID;

@Service
@Validated
@Slf4j
public class ErpFinanceAssetDepreciationServiceImpl implements ErpFinanceAssetDepreciationService {

    private static final DateTimeFormatter PERIOD_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    @Resource
    private ErpFinanceAssetMapper financeAssetMapper;
    @Resource
    private ErpFinanceAssetDepreciationMapper financeAssetDepreciationMapper;
    @Resource
    private ErpFinanceVoucherService voucherService;
    @Resource
    private DeptApi deptApi;

    @Override
    public Integer generateDepreciation(String period) {
        validatePeriod(period);
        // 1. 事务外：查询资产列表
        List<ErpFinanceAssetDO> assets = financeAssetMapper.selectList(ErpFinanceAssetDO::getStatus,
                ErpFinanceAssetStatusEnum.ACTIVE.getStatus());
        if (CollUtil.isEmpty(assets)) {
            return 0;
        }

        // 2. 事务外：批量预加载部门信息（避免事务内调外部 API）
        Map<Long, DeptRespDTO> deptMap = batchLoadDeptInfo(assets);

        // 3. 事务内：执行折旧生成
        return doGenerateDepreciation(period, assets, deptMap, true);
    }

    @Override
    public Integer generateDepreciationForAssets(String period, List<Long> assetIds) {
        validatePeriod(period);
        if (CollUtil.isEmpty(assetIds)) {
            return 0;
        }

        // 1. 事务外：查询资产列表
        List<ErpFinanceAssetDO> assets = financeAssetMapper.selectBatchIds(assetIds);
        if (CollUtil.isEmpty(assets)) {
            return 0;
        }

        // 2. 事务外：批量预加载部门信息（避免事务内调外部 API）
        Map<Long, DeptRespDTO> deptMap = batchLoadDeptInfo(assets);

        // 3. 事务内：执行折旧生成
        return doGenerateDepreciation(period, assets, deptMap, false);
    }

    /**
     * 事务外批量加载部门信息
     * 避免在事务内调用 deptApi.getDept() 外部 API
     */
    private Map<Long, DeptRespDTO> batchLoadDeptInfo(List<ErpFinanceAssetDO> assets) {
        // 收集所有需要查询的部门 ID（只有无形资产需要）
        Set<Long> deptIds = assets.stream()
                .filter(asset -> asset.getAssetType() != null && asset.getAssetType() == 1) // 无形资产
                .map(ErpFinanceAssetDO::getDeptId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (CollUtil.isEmpty(deptIds)) {
            return Collections.emptyMap();
        }

        // 批量查询部门信息
        try {
            List<DeptRespDTO> depts = deptApi.getDeptList(deptIds);
            if (CollUtil.isEmpty(depts)) {
                return Collections.emptyMap();
            }
            return depts.stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(DeptRespDTO::getId, dept -> dept, (a, b) -> a));
        } catch (Exception e) {
            log.warn("[batchLoadDeptInfo] 批量获取部门信息失败，deptIds={}", deptIds, e);
            return Collections.emptyMap();
        }
    }

    /**
     * 事务内执行折旧生成（核心逻辑）
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer doGenerateDepreciation(String period, List<ErpFinanceAssetDO> assets,
                                          Map<Long, DeptRespDTO> deptMap, boolean failOnPeriodDuplicate) {
        // 检查期间是否已存在折旧记录
        if (failOnPeriodDuplicate && CollUtil.isNotEmpty(financeAssetDepreciationMapper.selectListByPeriod(period))) {
            throw exception(ASSET_DEPRECIATION_PERIOD_DUPLICATE, period);
        }

        // 批量查询该期间已存在的折旧记录（避免 N+1 查询）
        Set<Long> assetIds = assets.stream()
                .map(ErpFinanceAssetDO::getId)
                .collect(Collectors.toSet());
        Set<Long> existingAssetIds = financeAssetDepreciationMapper.selectExistingAssetIdsByPeriod(assetIds, period);

        int created = 0;
        for (ErpFinanceAssetDO asset : assets) {
            // 使用 Set.contains() 替代循环查询（O(1) vs O(n)）
            if (existingAssetIds.contains(asset.getId())) {
                continue;
            }
            if (asset.getCurrentAmount() == null || asset.getOriginalAmount() == null
                    || asset.getDepreciationPeriodMonths() == null || asset.getDepreciationPeriodMonths() <= 0) {
                continue;
            }
            if (asset.getDepreciationStartPeriod() != null && asset.getDepreciationStartPeriod().compareTo(period) > 0) {
                continue;
            }
            BigDecimal depreciableAmount = asset.getOriginalAmount().subtract(defaultAmount(asset.getSalvageAmount()));
            if (depreciableAmount.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            BigDecimal monthlyAmount = depreciableAmount
                    .divide(BigDecimal.valueOf(asset.getDepreciationPeriodMonths()), 2, RoundingMode.HALF_UP);
            BigDecimal beforeDepreciatedAmount = defaultAmount(asset.getDepreciatedAmount());
            BigDecimal beforeCurrentAmount = defaultAmount(asset.getCurrentAmount());
            BigDecimal targetCurrentAmount = asset.getOriginalAmount().subtract(defaultAmount(asset.getSalvageAmount()));
            if (beforeDepreciatedAmount.compareTo(targetCurrentAmount) >= 0 || beforeCurrentAmount.compareTo(defaultAmount(asset.getSalvageAmount())) <= 0) {
                continue;
            }
            BigDecimal allowedAmount = beforeCurrentAmount.subtract(defaultAmount(asset.getSalvageAmount()));
            BigDecimal depreciationAmount = monthlyAmount.min(allowedAmount);
            ErpFinanceAssetDepreciationDO depreciation = new ErpFinanceAssetDepreciationDO()
                    .setAssetId(asset.getId())
                    .setAssetNo(asset.getNo())
                    .setPeriod(period)
                    .setDepreciationAmount(depreciationAmount)
                    .setBeforeDepreciatedAmount(beforeDepreciatedAmount)
                    .setAfterDepreciatedAmount(beforeDepreciatedAmount.add(depreciationAmount))
                    .setBeforeCurrentAmount(beforeCurrentAmount)
                    .setAfterCurrentAmount(beforeCurrentAmount.subtract(depreciationAmount))
                    .setStatus(ErpFinanceAssetDepreciationStatusEnum.CREATED.getStatus());
            financeAssetDepreciationMapper.insert(depreciation);
            financeAssetMapper.updateById(new ErpFinanceAssetDO()
                    .setId(asset.getId())
                    .setDepreciatedAmount(depreciation.getAfterDepreciatedAmount())
                    .setCurrentAmount(depreciation.getAfterCurrentAmount())
                    .setLastDepreciationPeriod(period));

            // 自动生成凭证（使用预加载的部门信息，不调外部 API）
            try {
                Integer bizType = resolveDepreciationBizType(asset, deptMap);
                Long voucherId = voucherService.autoGenerateVoucher(bizType, depreciation.getId());
                if (voucherId != null) {
                    financeAssetDepreciationMapper.updateById(new ErpFinanceAssetDepreciationDO()
                            .setId(depreciation.getId())
                            .setVoucherId(voucherId));
                }
            } catch (Exception e) {
                // M1 修复：增强错误日志，记录完整上下文便于后续补偿
                log.error("[generateDepreciation] 凭证生成失败，assetId={}, assetNo={}, period={}, bizType={}, 错误将在折旧记录中保留，可通过 recomputeAutoGeneratedVoucher 重试",
                        asset.getId(), asset.getNo(), period, resolveDepreciationBizType(asset, deptMap), e);
                // M2 说明：使用 REQUIRES_NEW 确保凭证生成独立于折旧事务
                // 如果凭证生成成功但折旧回滚，孤立凭证可通过 rollbackAutoGeneratedVoucher 清理
            }

            created++;
        }
        return created;
    }

    @Override
    public ErpFinanceAssetDepreciationDO getFinanceAssetDepreciation(Long id) {
        return financeAssetDepreciationMapper.selectById(id);
    }

    @Override
    public PageResult<ErpFinanceAssetDepreciationDO> getFinanceAssetDepreciationPage(ErpFinanceAssetDepreciationPageReqVO pageReqVO) {
        return financeAssetDepreciationMapper.selectPage(pageReqVO);
    }

    private void validatePeriod(String period) {
        try {
            YearMonth.parse(period, PERIOD_FORMATTER);
        } catch (DateTimeParseException ex) {
            throw exception(ASSET_DEPRECIATION_PERIOD_INVALID);
        }
    }

    private BigDecimal defaultAmount(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }

    /**
     * 根据资产类型和所属部门的成本类型，解析折旧/摊销的业务类型
     * 使用预加载的部门信息，不调用外部 API
     *
     * 规则：
     * - 固定资产 → ASSET_DEPRECIATION (70)
     * - 无形资产 + 研发部门(cost_type=4) → ASSET_AMORTIZATION_RD (72)
     * - 无形资产 + 其他部门 → ASSET_AMORTIZATION (71)
     *
     * @param asset 资产信息
     * @param deptMap 预加载的部门信息 Map (deptId -> DeptRespDTO)
     */
    private Integer resolveDepreciationBizType(ErpFinanceAssetDO asset, Map<Long, DeptRespDTO> deptMap) {
        // 固定资产
        if (asset.getAssetType() == null || asset.getAssetType() == 0) {
            return ErpBizTypeEnum.ASSET_DEPRECIATION.getType();
        }
        // 无形资产：根据部门成本类型判断（使用预加载的 Map，不调外部 API）
        if (asset.getDeptId() != null && deptMap != null) {
            DeptRespDTO dept = deptMap.get(asset.getDeptId());
            if (dept != null && dept.getCostType() != null && dept.getCostType() == 4) {
                // 研发部门
                return ErpBizTypeEnum.ASSET_AMORTIZATION_RD.getType();
            }
        }
        // 默认无形资产摊销
        return ErpBizTypeEnum.ASSET_AMORTIZATION.getType();
    }
}
