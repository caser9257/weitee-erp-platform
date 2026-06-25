package cn.weitee.erp.module.erp.service.finance;

import cn.weitee.erp.framework.common.enums.CommonStatusEnum;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.ledger.ErpFinanceLedgerPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.ledger.ErpFinanceLedgerSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceLedgerDO;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceLedgerMapper;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinancePeriodMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.Collections;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsFinanceLedger.FINANCE_LEDGER_DEFAULT_DUPLICATE;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsFinanceLedger.FINANCE_LEDGER_DEFAULT_MUST_ENABLE;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsFinanceLedger.FINANCE_LEDGER_DELETE_FAIL_PERIOD_EXISTS;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsFinanceLedger.FINANCE_LEDGER_NOT_ENABLE;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsFinanceLedger.FINANCE_LEDGER_NOT_EXISTS;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsFinanceLedger.FINANCE_LEDGER_NO_DUPLICATE;

@Service
@Validated
public class ErpFinanceLedgerServiceImpl implements ErpFinanceLedgerService {

    @Resource
    private ErpFinanceLedgerMapper erpFinanceLedgerMapper;
    @Resource
    private ErpFinancePeriodMapper erpFinancePeriodMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createFinanceLedger(ErpFinanceLedgerSaveReqVO createReqVO) {
        validateLedgerNoUnique(null, createReqVO.getNo());
        validateDefaultLedgerStatus(createReqVO.getStatus(), createReqVO.getDefaultStatus());
        clearExistingDefaultLedgerIfNeeded(null, createReqVO.getDefaultStatus());
        ErpFinanceLedgerDO ledger = BeanUtils.toBean(createReqVO, ErpFinanceLedgerDO.class, item ->
                item.setDefaultStatus(Boolean.TRUE.equals(createReqVO.getDefaultStatus())));
        erpFinanceLedgerMapper.insert(ledger);
        return ledger.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFinanceLedger(ErpFinanceLedgerSaveReqVO updateReqVO) {
        ErpFinanceLedgerDO ledger = validateFinanceLedgerExists(updateReqVO.getId());
        validateLedgerNoUnique(updateReqVO.getId(), updateReqVO.getNo());
        validateDefaultLedgerStatus(updateReqVO.getStatus(), updateReqVO.getDefaultStatus());
        clearExistingDefaultLedgerIfNeeded(updateReqVO.getId(), updateReqVO.getDefaultStatus());
        ErpFinanceLedgerDO updateObj = BeanUtils.toBean(updateReqVO, ErpFinanceLedgerDO.class, item ->
                item.setDefaultStatus(Boolean.TRUE.equals(updateReqVO.getDefaultStatus())));
        erpFinanceLedgerMapper.updateById(updateObj);
        if (Boolean.TRUE.equals(ledger.getDefaultStatus()) && CommonStatusEnum.isDisable(updateReqVO.getStatus())) {
            erpFinanceLedgerMapper.updateById(new ErpFinanceLedgerDO().setId(updateReqVO.getId()).setDefaultStatus(false));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFinanceLedgerDefaultStatus(Long id, Boolean defaultStatus) {
        ErpFinanceLedgerDO ledger = validateFinanceLedgerExists(id);
        validateDefaultLedgerStatus(ledger.getStatus(), defaultStatus);
        clearExistingDefaultLedgerIfNeeded(id, defaultStatus);
        erpFinanceLedgerMapper.updateById(new ErpFinanceLedgerDO().setId(id).setDefaultStatus(Boolean.TRUE.equals(defaultStatus)));
    }

    @Override
    public void deleteFinanceLedger(Long id) {
        ErpFinanceLedgerDO ledger = validateFinanceLedgerExists(id);
        if (erpFinancePeriodMapper.selectCountByLedgerId(id) > 0) {
            throw exception(FINANCE_LEDGER_DELETE_FAIL_PERIOD_EXISTS, ledger.getName());
        }
        erpFinanceLedgerMapper.deleteById(id);
    }

    @Override
    public ErpFinanceLedgerDO getFinanceLedger(Long id) {
        return erpFinanceLedgerMapper.selectById(id);
    }

    @Override
    public ErpFinanceLedgerDO validateFinanceLedger(Long id) {
        ErpFinanceLedgerDO ledger = validateFinanceLedgerExists(id);
        if (CommonStatusEnum.isDisable(ledger.getStatus())) {
            throw exception(FINANCE_LEDGER_NOT_ENABLE, ledger.getName());
        }
        return ledger;
    }

    @Override
    public ErpFinanceLedgerDO getDefaultFinanceLedger() {
        List<ErpFinanceLedgerDO> defaultLedgers = erpFinanceLedgerMapper.selectListByStatus(CommonStatusEnum.ENABLE.getStatus()).stream()
                .filter(item -> Boolean.TRUE.equals(item.getDefaultStatus()))
                .collect(Collectors.toList());
        if (defaultLedgers.isEmpty()) {
            return null;
        }
        if (defaultLedgers.size() > 1) {
            throw exception(FINANCE_LEDGER_DEFAULT_DUPLICATE);
        }
        return defaultLedgers.get(0);
    }

    @Override
    public List<ErpFinanceLedgerDO> getFinanceLedgerListByStatus(Integer status) {
        return erpFinanceLedgerMapper.selectListByStatus(status);
    }

    @Override
    public List<ErpFinanceLedgerDO> getFinanceLedgerList(Collection<Long> ids) {
        return erpFinanceLedgerMapper.selectByIds(ids);
    }

    @Override
    public PageResult<ErpFinanceLedgerDO> getFinanceLedgerPage(ErpFinanceLedgerPageReqVO pageReqVO) {
        return erpFinanceLedgerMapper.selectPage(pageReqVO);
    }

    private ErpFinanceLedgerDO validateFinanceLedgerExists(Long id) {
        ErpFinanceLedgerDO ledger = erpFinanceLedgerMapper.selectById(id);
        if (ledger == null) {
            throw exception(FINANCE_LEDGER_NOT_EXISTS);
        }
        return ledger;
    }

    private void validateLedgerNoUnique(Long id, String no) {
        ErpFinanceLedgerDO ledger = erpFinanceLedgerMapper.selectByNo(no);
        if (ledger == null) {
            return;
        }
        if (id == null || !ledger.getId().equals(id)) {
            throw exception(FINANCE_LEDGER_NO_DUPLICATE, no);
        }
    }

    private void validateDefaultLedgerStatus(Integer status, Boolean defaultStatus) {
        if (Boolean.TRUE.equals(defaultStatus) && CommonStatusEnum.isDisable(status)) {
            throw exception(FINANCE_LEDGER_DEFAULT_MUST_ENABLE);
        }
    }

    private void clearExistingDefaultLedgerIfNeeded(Long selfId, Boolean defaultStatus) {
        if (!Boolean.TRUE.equals(defaultStatus)) {
            return;
        }
        List<ErpFinanceLedgerDO> ledgers = erpFinanceLedgerMapper.selectListByStatus(CommonStatusEnum.ENABLE.getStatus());
        if (ledgers == null) {
            ledgers = Collections.emptyList();
        }
        ledgers.stream()
                .filter(item -> Boolean.TRUE.equals(item.getDefaultStatus()))
                .filter(item -> selfId == null || !item.getId().equals(selfId))
                .forEach(item -> erpFinanceLedgerMapper.updateById(
                        new ErpFinanceLedgerDO().setId(item.getId()).setDefaultStatus(false)));
    }
}
