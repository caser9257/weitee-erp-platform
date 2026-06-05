package cn.iocoder.yudao.module.erp.service.finance;

import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.dualledger.ErpFinanceDualLedgerDiffConfigPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.dualledger.ErpFinanceDualLedgerDiffConfigSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceDualLedgerDiffConfigDO;
import cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpFinanceDualLedgerDiffConfigMapper;
import cn.iocoder.yudao.module.erp.enums.ErpFinanceDualLedgerDiffItemTypeEnum;
import cn.iocoder.yudao.module.erp.enums.ErpFinanceDualLedgerDiffSourceTypeEnum;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstantsFinanceLedger.FINANCE_DUAL_LEDGER_DIFF_CONFIG_BIZ_ITEM_DUPLICATE;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstantsFinanceLedger.FINANCE_DUAL_LEDGER_DIFF_CONFIG_DEPRECIATION_SOURCE_ITEM_INVALID;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstantsFinanceLedger.FINANCE_DUAL_LEDGER_DIFF_CONFIG_NOT_EXISTS;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstantsFinanceLedger.FINANCE_DUAL_LEDGER_DIFF_CONFIG_SOURCE_VALUE_FORBIDDEN;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstantsFinanceLedger.FINANCE_DUAL_LEDGER_DIFF_CONFIG_SOURCE_VALUE_REQUIRED;

@Service
@Validated
public class ErpFinanceDualLedgerDiffConfigServiceImpl implements ErpFinanceDualLedgerDiffConfigService {

    @Resource
    private ErpFinanceDualLedgerDiffConfigMapper erpFinanceDualLedgerDiffConfigMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createDualLedgerDiffConfig(ErpFinanceDualLedgerDiffConfigSaveReqVO createReqVO) {
        validateSourceConfig(createReqVO);
        validateBizItemUnique(null, createReqVO.getBizType(), createReqVO.getDiffItemType());
        ErpFinanceDualLedgerDiffConfigDO config = BeanUtils.toBean(createReqVO, ErpFinanceDualLedgerDiffConfigDO.class);
        erpFinanceDualLedgerDiffConfigMapper.insert(config);
        return config.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDualLedgerDiffConfig(ErpFinanceDualLedgerDiffConfigSaveReqVO updateReqVO) {
        validateDualLedgerDiffConfigExists(updateReqVO.getId());
        validateSourceConfig(updateReqVO);
        validateBizItemUnique(updateReqVO.getId(), updateReqVO.getBizType(), updateReqVO.getDiffItemType());
        erpFinanceDualLedgerDiffConfigMapper.updateById(BeanUtils.toBean(updateReqVO, ErpFinanceDualLedgerDiffConfigDO.class));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDualLedgerDiffConfig(Long id) {
        validateDualLedgerDiffConfigExists(id);
        erpFinanceDualLedgerDiffConfigMapper.deleteById(id);
    }

    @Override
    public ErpFinanceDualLedgerDiffConfigDO getDualLedgerDiffConfig(Long id) {
        return erpFinanceDualLedgerDiffConfigMapper.selectById(id);
    }

    @Override
    public List<ErpFinanceDualLedgerDiffConfigDO> getDualLedgerDiffConfigList(Integer bizType, Integer status) {
        return erpFinanceDualLedgerDiffConfigMapper.selectListByBizTypeAndStatus(bizType, status);
    }

    @Override
    public PageResult<ErpFinanceDualLedgerDiffConfigDO> getDualLedgerDiffConfigPage(ErpFinanceDualLedgerDiffConfigPageReqVO pageReqVO) {
        return erpFinanceDualLedgerDiffConfigMapper.selectPage(pageReqVO);
    }

    private void validateSourceConfig(ErpFinanceDualLedgerDiffConfigSaveReqVO reqVO) {
        validateSingleSource(reqVO.getDiffItemType(), reqVO.getExternalSourceType(), reqVO.getExternalSourceValue());
        validateSingleSource(reqVO.getDiffItemType(), reqVO.getInternalSourceType(), reqVO.getInternalSourceValue());
    }

    private void validateSingleSource(Integer diffItemType, Integer sourceType, Integer sourceValue) {
        if (ErpFinanceDualLedgerDiffSourceTypeEnum.requiresSourceValue(sourceType)) {
            if (sourceValue == null || !ErpFinanceDualLedgerDiffItemTypeEnum.isSupported(sourceValue)) {
                throw exception(FINANCE_DUAL_LEDGER_DIFF_CONFIG_SOURCE_VALUE_REQUIRED);
            }
            return;
        }
        if (ErpFinanceDualLedgerDiffSourceTypeEnum.ASSET_DEPRECIATION.getType().equals(sourceType)) {
            if (sourceValue != null) {
                throw exception(FINANCE_DUAL_LEDGER_DIFF_CONFIG_SOURCE_VALUE_FORBIDDEN);
            }
            if (!ObjectUtil.equal(diffItemType, ErpFinanceDualLedgerDiffItemTypeEnum.DEPRECIATION.getType())) {
                throw exception(FINANCE_DUAL_LEDGER_DIFF_CONFIG_DEPRECIATION_SOURCE_ITEM_INVALID);
            }
        }
    }

    private void validateBizItemUnique(Long id, Integer bizType, Integer diffItemType) {
        ErpFinanceDualLedgerDiffConfigDO config =
                erpFinanceDualLedgerDiffConfigMapper.selectByBizTypeAndDiffItemType(bizType, diffItemType);
        if (config == null) {
            return;
        }
        if (id == null || !ObjectUtil.equal(config.getId(), id)) {
            throw exception(FINANCE_DUAL_LEDGER_DIFF_CONFIG_BIZ_ITEM_DUPLICATE, bizType, diffItemType);
        }
    }

    private void validateDualLedgerDiffConfigExists(Long id) {
        if (erpFinanceDualLedgerDiffConfigMapper.selectById(id) == null) {
            throw exception(FINANCE_DUAL_LEDGER_DIFF_CONFIG_NOT_EXISTS);
        }
    }

}
