package cn.weitee.erp.module.erp.service.finance;

import cn.hutool.core.util.ObjectUtil;
import cn.weitee.erp.framework.common.enums.CommonStatusEnum;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.dualledger.ErpFinanceDualLedgerConfigPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.dualledger.ErpFinanceDualLedgerConfigSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceDualLedgerConfigDO;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceDualLedgerConfigMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.List;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsFinanceLedger.FINANCE_DUAL_LEDGER_CONFIG_BIZ_TYPE_DUPLICATE;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsFinanceLedger.FINANCE_DUAL_LEDGER_CONFIG_LEDGER_SAME;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsFinanceLedger.FINANCE_DUAL_LEDGER_CONFIG_NOT_EXISTS;

@Service
@Validated
public class ErpFinanceDualLedgerConfigServiceImpl implements ErpFinanceDualLedgerConfigService {

    @Resource
    private ErpFinanceDualLedgerConfigMapper erpFinanceDualLedgerConfigMapper;
    @Resource
    private ErpFinanceLedgerService financeLedgerService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createDualLedgerConfig(ErpFinanceDualLedgerConfigSaveReqVO createReqVO) {
        validateLedgerRelation(createReqVO.getExternalLedgerId(), createReqVO.getInternalLedgerId());
        validateBizTypeUnique(null, createReqVO.getBizType());
        ErpFinanceDualLedgerConfigDO config = BeanUtils.toBean(createReqVO, ErpFinanceDualLedgerConfigDO.class);
        erpFinanceDualLedgerConfigMapper.insert(config);
        return config.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDualLedgerConfig(ErpFinanceDualLedgerConfigSaveReqVO updateReqVO) {
        validateDualLedgerConfigExists(updateReqVO.getId());
        validateLedgerRelation(updateReqVO.getExternalLedgerId(), updateReqVO.getInternalLedgerId());
        validateBizTypeUnique(updateReqVO.getId(), updateReqVO.getBizType());
        ErpFinanceDualLedgerConfigDO updateObj = BeanUtils.toBean(updateReqVO, ErpFinanceDualLedgerConfigDO.class);
        erpFinanceDualLedgerConfigMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDualLedgerConfig(Long id) {
        validateDualLedgerConfigExists(id);
        erpFinanceDualLedgerConfigMapper.deleteById(id);
    }

    @Override
    public ErpFinanceDualLedgerConfigDO getDualLedgerConfig(Long id) {
        return erpFinanceDualLedgerConfigMapper.selectById(id);
    }

    @Override
    public ErpFinanceDualLedgerConfigDO getEnabledDualLedgerConfig(Integer bizType) {
        ErpFinanceDualLedgerConfigDO config = erpFinanceDualLedgerConfigMapper.selectByBizType(bizType);
        if (config == null) {
            return null;
        }
        return ObjectUtil.equal(config.getStatus(), CommonStatusEnum.ENABLE.getStatus()) ? config : null;
    }

    @Override
    public List<ErpFinanceDualLedgerConfigDO> getDualLedgerConfigListByStatus(Integer status) {
        return erpFinanceDualLedgerConfigMapper.selectListByStatus(status);
    }

    @Override
    public PageResult<ErpFinanceDualLedgerConfigDO> getDualLedgerConfigPage(ErpFinanceDualLedgerConfigPageReqVO pageReqVO) {
        return erpFinanceDualLedgerConfigMapper.selectPage(pageReqVO);
    }

    @Override
    public PageResult<ErpFinanceDualLedgerConfigDO> getDualLedgerConfigPage(
            ErpFinanceDualLedgerConfigPageReqVO pageReqVO, List<Long> visibleLedgerIds) {
        return erpFinanceDualLedgerConfigMapper.selectPageByVisibleLedgerIds(pageReqVO, visibleLedgerIds);
    }

    private ErpFinanceDualLedgerConfigDO validateDualLedgerConfigExists(Long id) {
        ErpFinanceDualLedgerConfigDO config = erpFinanceDualLedgerConfigMapper.selectById(id);
        if (config == null) {
            throw exception(FINANCE_DUAL_LEDGER_CONFIG_NOT_EXISTS);
        }
        return config;
    }

    private void validateLedgerRelation(Long externalLedgerId, Long internalLedgerId) {
        if (ObjectUtil.equal(externalLedgerId, internalLedgerId)) {
            throw exception(FINANCE_DUAL_LEDGER_CONFIG_LEDGER_SAME);
        }
        financeLedgerService.validateFinanceLedger(externalLedgerId);
        financeLedgerService.validateFinanceLedger(internalLedgerId);
    }

    private void validateBizTypeUnique(Long id, Integer bizType) {
        ErpFinanceDualLedgerConfigDO config = erpFinanceDualLedgerConfigMapper.selectByBizType(bizType);
        if (config == null) {
            return;
        }
        if (id == null || !ObjectUtil.equal(config.getId(), id)) {
            throw exception(FINANCE_DUAL_LEDGER_CONFIG_BIZ_TYPE_DUPLICATE, bizType);
        }
    }
}
