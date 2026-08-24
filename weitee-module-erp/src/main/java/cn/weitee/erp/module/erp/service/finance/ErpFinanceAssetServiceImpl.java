package cn.weitee.erp.module.erp.service.finance;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.asset.ErpFinanceAssetPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.asset.ErpFinanceAssetSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceAssetCandidateDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceAssetDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceExpenseDO;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceAssetCandidateMapper;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceAssetDepreciationMapper;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceAssetMapper;
import cn.weitee.erp.module.erp.dal.redis.no.ErpNoRedisDAO;
import cn.weitee.erp.module.erp.enums.ErpFinanceAssetCandidateStatusEnum;
import cn.weitee.erp.module.erp.enums.ErpFinanceAssetSourceTypeEnum;
import cn.weitee.erp.module.erp.enums.ErpFinanceAssetStatusEnum;
import cn.weitee.erp.module.erp.enums.ErpFinanceExpenseAccountingTypeEnum;
import cn.weitee.erp.module.erp.enums.ErpFinanceExpenseTypeEnum;
import cn.weitee.erp.module.erp.service.finance.interceptor.FinancePermissionScope;
import cn.weitee.erp.module.erp.service.finance.interceptor.FinanceDataPermissionContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.framework.common.exception.enums.GlobalErrorCodeConstants.FORBIDDEN;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsFinanceAsset.*;

@Service
@Validated
public class ErpFinanceAssetServiceImpl implements ErpFinanceAssetService {

    @Resource
    private ErpFinanceAssetMapper financeAssetMapper;
    @Resource
    private ErpFinanceAssetCandidateMapper financeAssetCandidateMapper;
    @Resource
    private ErpFinanceAssetDepreciationMapper financeAssetDepreciationMapper;
    @Resource
    private ErpNoRedisDAO noRedisDAO;
    @Resource
    @Lazy
    private ErpFinanceExpenseService financeExpenseService;
    @Resource
    private FinanceDataPermissionService financeDataPermissionService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createFinanceAsset(ErpFinanceAssetSaveReqVO createReqVO) {
        if (createReqVO.getCandidateId() != null) {
            int updateCount = financeAssetCandidateMapper.updateByIdAndStatus(createReqVO.getCandidateId(),
                    ErpFinanceAssetCandidateStatusEnum.PENDING_CONFIRM.getStatus(),
                    new ErpFinanceAssetCandidateDO()
                            .setStatus(ErpFinanceAssetCandidateStatusEnum.CONFIRMED.getStatus())
                            .setRemark(createReqVO.getRemark()));
            if (updateCount == 0) {
                throw exception(ASSET_CANDIDATE_CONFIRM_FAIL);
            }
        }
        validateDeptAccess(createReqVO.getDeptId());
        String no = noRedisDAO.generate("GDZC");
        if (financeAssetMapper.selectByNo(no) != null) {
            throw exception(ASSET_NO_EXISTS);
        }
        ErpFinanceAssetDO asset = BeanUtils.toBean(createReqVO, ErpFinanceAssetDO.class, in -> in
                .setNo(no)
                .setSalvageAmount(calculateSalvageAmount(createReqVO.getOriginalAmount(), createReqVO.getSalvageRate()))
                .setDepreciatedAmount(BigDecimal.ZERO)
                .setCurrentAmount(createReqVO.getOriginalAmount())
                .setStatus(ErpFinanceAssetStatusEnum.DRAFT.getStatus()));
        financeAssetMapper.insert(asset);
        return asset.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFinanceAsset(ErpFinanceAssetSaveReqVO updateReqVO) {
        ErpFinanceAssetDO asset = validateFinanceAssetExists(updateReqVO.getId());
        validateDeptAccess(asset.getDeptId());
        validateDeptAccess(updateReqVO.getDeptId());
        ErpFinanceAssetDO updateObj = BeanUtils.toBean(updateReqVO, ErpFinanceAssetDO.class, in -> in
                .setNo(asset.getNo())
                .setSalvageAmount(calculateSalvageAmount(updateReqVO.getOriginalAmount(), updateReqVO.getSalvageRate())));
        financeAssetMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFinanceAsset(List<Long> ids) {
        ids.forEach(id -> {
            ErpFinanceAssetDO asset = validateFinanceAssetExists(id);
            validateDeptAccess(asset.getDeptId());
            if (!ErpFinanceAssetStatusEnum.DRAFT.getStatus().equals(asset.getStatus())) {
                throw exception(ASSET_DELETE_FAIL_STATUS, asset.getNo());
            }
            if (!financeAssetDepreciationMapper.selectListByAssetId(id).isEmpty()) {
                throw exception(ASSET_DELETE_FAIL_HAS_DEPRECIATION, asset.getNo());
            }
            financeAssetMapper.deleteById(id);
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFinanceAssetStatus(Long id, Integer status) {
        ErpFinanceAssetDO asset = validateFinanceAssetExists(id);
        validateDeptAccess(asset.getDeptId());
        int updateCount = financeAssetMapper.updateByIdAndStatus(id, asset.getStatus(),
                new ErpFinanceAssetDO().setStatus(status));
        if (updateCount == 0) {
            throw exception(ASSET_STATUS_UPDATE_FAIL);
        }
    }

    @Override
    public ErpFinanceAssetDO getFinanceAsset(Long id) {
        ErpFinanceAssetDO asset = financeAssetMapper.selectById(id);
        if (asset != null) {
            validateDeptAccess(asset.getDeptId());
        }
        return asset;
    }

    @Override
    public ErpFinanceAssetCandidateDO getCandidateByAssetSource(Long candidateId, Integer sourceType, Long sourceBizId,
                                                                Long sourceItemId, String sourceBizNo, String assetName) {
        if (candidateId != null) {
            ErpFinanceAssetCandidateDO candidate = financeAssetCandidateMapper.selectById(candidateId);
            if (candidate != null) {
                return candidate;
            }
        }
        if (sourceType == null || sourceBizId == null) {
            return null;
        }
        if (sourceItemId != null) {
            ErpFinanceAssetCandidateDO itemCandidate = financeAssetCandidateMapper.selectBySource(sourceType, sourceBizId, sourceItemId);
            if (itemCandidate != null) {
                return itemCandidate;
            }
        }
        ErpFinanceAssetCandidateDO exactCandidate = financeAssetCandidateMapper.selectOne(new LambdaQueryWrapperX<ErpFinanceAssetCandidateDO>()
                .eq(ErpFinanceAssetCandidateDO::getSourceType, sourceType)
                .eq(ErpFinanceAssetCandidateDO::getSourceBizId, sourceBizId)
                .eqIfPresent(ErpFinanceAssetCandidateDO::getAssetName, assetName)
                .orderByDesc(ErpFinanceAssetCandidateDO::getId)
                .last("LIMIT 1"));
        if (exactCandidate != null) {
            return exactCandidate;
        }
        return financeAssetCandidateMapper.selectOne(new LambdaQueryWrapperX<ErpFinanceAssetCandidateDO>()
                .eq(ErpFinanceAssetCandidateDO::getSourceType, sourceType)
                .eq(ErpFinanceAssetCandidateDO::getSourceBizId, sourceBizId)
                .eqIfPresent(ErpFinanceAssetCandidateDO::getSourceBizNo, sourceBizNo)
                .orderByDesc(ErpFinanceAssetCandidateDO::getId)
                .last("LIMIT 1"));
    }

    @Override
    public PageResult<ErpFinanceAssetDO> getFinanceAssetPage(ErpFinanceAssetPageReqVO pageReqVO) {
        FinancePermissionScope.Scope<Long> deptScope = financeDataPermissionService.getPermissionScope().deptScope();
        if (deptScope.mode() == FinancePermissionScope.ScopeMode.NONE) {
            return PageResult.empty(0L);
        }
        if (deptScope.mode() == FinancePermissionScope.ScopeMode.LIMITED) {
            return financeAssetMapper.selectPageByDeptIds(pageReqVO, deptScope.values());
        }
        return financeAssetMapper.selectPage(pageReqVO);
    }

    @Override
    public List<Long> getActiveResearchCapitalizeAssetIdsByPeriod(String period) {
        List<ErpFinanceAssetDO> assetList = financeAssetMapper.selectList(new LambdaQueryWrapperX<ErpFinanceAssetDO>()
                .eq(ErpFinanceAssetDO::getStatus, ErpFinanceAssetStatusEnum.ACTIVE.getStatus())
                .eq(ErpFinanceAssetDO::getSourceType, ErpFinanceAssetSourceTypeEnum.FINANCE_EXPENSE.getType())
                .leIfPresent(ErpFinanceAssetDO::getDepreciationStartPeriod, period)
                .orderByAsc(ErpFinanceAssetDO::getId));
        if (assetList == null || assetList.isEmpty()) {
            return Collections.emptyList();
        }
        return assetList.stream()
                .filter(asset -> asset.getSourceBizId() != null)
                .filter(asset -> {
                    ErpFinanceExpenseDO expense = financeExpenseService.getFinanceExpense(asset.getSourceBizId());
                    return expense != null
                            && ErpFinanceExpenseTypeEnum.RESEARCH.getType().equals(expense.getExpenseType())
                            && ErpFinanceExpenseAccountingTypeEnum.CAPITALIZE.getType().equals(expense.getRdAccountingType());
                })
                .map(ErpFinanceAssetDO::getId)
                .collect(Collectors.toList());
    }

    private ErpFinanceAssetDO validateFinanceAssetExists(Long id) {
        ErpFinanceAssetDO asset = financeAssetMapper.selectById(id);
        if (asset == null) {
            throw exception(ASSET_NOT_EXISTS);
        }
        return asset;
    }

    private void validateDeptAccess(Long deptId) {
        FinancePermissionScope permissionScope = FinanceDataPermissionContext.getPermissionScope();
        if (permissionScope == null || permissionScope.deptScope().mode() == FinancePermissionScope.ScopeMode.ALL) {
            return;
        }
        if (deptId == null || !permissionScope.deptScope().values().contains(deptId)) {
            throw exception(FORBIDDEN);
        }
    }

    private BigDecimal calculateSalvageAmount(BigDecimal originalAmount, BigDecimal salvageRate) {
        return originalAmount.multiply(salvageRate).setScale(2, java.math.RoundingMode.HALF_UP);
    }
}
