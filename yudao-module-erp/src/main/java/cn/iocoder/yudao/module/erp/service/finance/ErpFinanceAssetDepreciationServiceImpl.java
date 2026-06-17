package cn.iocoder.yudao.module.erp.service.finance;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.asset.ErpFinanceAssetDepreciationPageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceAssetDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceAssetDepreciationDO;
import cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpFinanceAssetDepreciationMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpFinanceAssetMapper;
import cn.iocoder.yudao.module.erp.enums.ErpFinanceAssetDepreciationStatusEnum;
import cn.iocoder.yudao.module.erp.enums.ErpFinanceAssetStatusEnum;
import cn.iocoder.yudao.module.erp.enums.common.ErpBizTypeEnum;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
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
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstantsFinanceAsset.ASSET_DEPRECIATION_PERIOD_DUPLICATE;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstantsFinanceAsset.ASSET_DEPRECIATION_PERIOD_INVALID;

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
    @Transactional(rollbackFor = Exception.class)
    public Integer generateDepreciation(String period) {
        validatePeriod(period);
        if (CollUtil.isNotEmpty(financeAssetDepreciationMapper.selectListByPeriod(period))) {
            throw exception(ASSET_DEPRECIATION_PERIOD_DUPLICATE, period);
        }
        return generateDepreciationInternal(period, financeAssetMapper.selectList(ErpFinanceAssetDO::getStatus,
                ErpFinanceAssetStatusEnum.ACTIVE.getStatus()), true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer generateDepreciationForAssets(String period, List<Long> assetIds) {
        validatePeriod(period);
        if (CollUtil.isEmpty(assetIds)) {
            return 0;
        }
        List<ErpFinanceAssetDO> assets = financeAssetMapper.selectBatchIds(assetIds);
        return generateDepreciationInternal(period, assets == null ? Collections.emptyList() : assets, false);
    }

    private Integer generateDepreciationInternal(String period, List<ErpFinanceAssetDO> assets, boolean failOnPeriodDuplicate) {
        if (failOnPeriodDuplicate && CollUtil.isNotEmpty(financeAssetDepreciationMapper.selectListByPeriod(period))) {
            throw exception(ASSET_DEPRECIATION_PERIOD_DUPLICATE, period);
        }
        int created = 0;
        for (ErpFinanceAssetDO asset : assets) {
            if (CollUtil.isNotEmpty(financeAssetDepreciationMapper.selectListByAssetId(asset.getId()).stream()
                    .filter(item -> period.equals(item.getPeriod()))
                    .toList())) {
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

            // 自动生成凭证
            try {
                Integer bizType = resolveDepreciationBizType(asset);
                Long voucherId = voucherService.autoGenerateVoucher(bizType, depreciation.getId());
                if (voucherId != null) {
                    financeAssetDepreciationMapper.updateById(new ErpFinanceAssetDepreciationDO()
                            .setId(depreciation.getId())
                            .setVoucherId(voucherId));
                }
            } catch (Exception e) {
                log.error("[generateDepreciation] 凭证生成失败，assetId={}, period={}", asset.getId(), period, e);
                // 凭证生成失败不影响主流程
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
     *
     * 规则：
     * - 固定资产 → ASSET_DEPRECIATION (70)
     * - 无形资产 + 研发部门(cost_type=4) → ASSET_AMORTIZATION_RD (72)
     * - 无形资产 + 其他部门 → ASSET_AMORTIZATION (71)
     */
    private Integer resolveDepreciationBizType(ErpFinanceAssetDO asset) {
        // 固定资产
        if (asset.getAssetType() == null || asset.getAssetType() == 0) {
            return ErpBizTypeEnum.ASSET_DEPRECIATION.getType();
        }
        // 无形资产：根据部门成本类型判断
        if (asset.getDeptId() != null) {
            try {
                DeptRespDTO dept = deptApi.getDept(asset.getDeptId());
                if (dept != null && dept.getCostType() != null && dept.getCostType() == 4) {
                    // 研发部门
                    return ErpBizTypeEnum.ASSET_AMORTIZATION_RD.getType();
                }
            } catch (Exception e) {
                log.warn("[resolveDepreciationBizType] 获取部门信息失败，deptId={}", asset.getDeptId(), e);
            }
        }
        // 默认无形资产摊销
        return ErpBizTypeEnum.ASSET_AMORTIZATION.getType();
    }
}
