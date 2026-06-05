package cn.iocoder.yudao.module.erp.service.finance;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.period.ErpFinancePeriodCreateYearReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.period.ErpFinancePeriodPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.period.ErpFinancePeriodSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceLedgerDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinancePeriodDO;
import cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpFinancePeriodMapper;
import cn.iocoder.yudao.module.erp.enums.ErpFinancePeriodStatusEnum;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstantsFinanceLedger.FINANCE_PERIOD_ALREADY_EXISTS;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstantsFinanceLedger.FINANCE_PERIOD_CLOSE_FAIL_ALREADY_CLOSED;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstantsFinanceLedger.FINANCE_PERIOD_CLOSE_FAIL_EARLIER_OPEN;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstantsFinanceLedger.FINANCE_PERIOD_CURRENT_OPEN_NOT_EXISTS;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstantsFinanceLedger.FINANCE_PERIOD_NOT_EXISTS;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstantsFinanceLedger.FINANCE_PERIOD_REOPEN_FAIL_ALREADY_OPEN;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstantsFinanceLedger.FINANCE_PERIOD_REOPEN_FAIL_LATER_CLOSED;

@Service
@Validated
public class ErpFinancePeriodServiceImpl implements ErpFinancePeriodService {

    @Resource
    private ErpFinancePeriodMapper erpFinancePeriodMapper;
    @Resource
    private ErpFinanceLedgerService financeLedgerService;

    @Override
    public Long createFinancePeriod(ErpFinancePeriodSaveReqVO createReqVO) {
        ErpFinanceLedgerDO ledger = financeLedgerService.validateFinanceLedger(createReqVO.getLedgerId());
        Integer periodSort = buildPeriodSort(createReqVO.getPeriodYear(), createReqVO.getPeriodMonth());
        validateFinancePeriodUnique(ledger, periodSort, buildPeriodCode(createReqVO.getPeriodYear(), createReqVO.getPeriodMonth()));
        ErpFinancePeriodDO period = BeanUtils.toBean(createReqVO, ErpFinancePeriodDO.class, item -> fillPeriodCalendarFields(
                item, createReqVO.getPeriodYear(), createReqVO.getPeriodMonth()));
        period.setStatus(ErpFinancePeriodStatusEnum.OPEN.getStatus());
        erpFinancePeriodMapper.insert(period);
        return period.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer createFinancePeriodsByYear(ErpFinancePeriodCreateYearReqVO createReqVO) {
        financeLedgerService.validateFinanceLedger(createReqVO.getLedgerId());
        List<ErpFinancePeriodDO> existedPeriods = erpFinancePeriodMapper.selectListByLedgerIdAndYear(
                createReqVO.getLedgerId(), createReqVO.getPeriodYear());
        Set<Integer> existedSorts = convertSet(existedPeriods, ErpFinancePeriodDO::getPeriodSort);
        List<ErpFinancePeriodDO> createList = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            Integer periodSort = buildPeriodSort(createReqVO.getPeriodYear(), month);
            if (existedSorts.contains(periodSort)) {
                continue;
            }
            ErpFinancePeriodDO period = new ErpFinancePeriodDO()
                    .setLedgerId(createReqVO.getLedgerId())
                    .setRemark(createReqVO.getRemark())
                    .setStatus(ErpFinancePeriodStatusEnum.OPEN.getStatus());
            fillPeriodCalendarFields(period, createReqVO.getPeriodYear(), month);
            createList.add(period);
        }
        if (CollUtil.isEmpty(createList)) {
            return 0;
        }
        erpFinancePeriodMapper.insertBatch(createList);
        return createList.size();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closeFinancePeriod(Long id) {
        ErpFinancePeriodDO period = validateFinancePeriodExists(id);
        if (ErpFinancePeriodStatusEnum.isClosed(period.getStatus())) {
            throw exception(FINANCE_PERIOD_CLOSE_FAIL_ALREADY_CLOSED, period.getPeriodCode());
        }
        if (erpFinancePeriodMapper.selectEarlierOpenCount(period.getLedgerId(), period.getPeriodSort()) > 0) {
            throw exception(FINANCE_PERIOD_CLOSE_FAIL_EARLIER_OPEN, period.getPeriodCode());
        }
        erpFinancePeriodMapper.updateById(new ErpFinancePeriodDO()
                .setId(id)
                .setStatus(ErpFinancePeriodStatusEnum.CLOSED.getStatus())
                .setCloseTime(java.time.LocalDateTime.now())
                .setCloseUserId(getLoginUserId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reopenFinancePeriod(Long id) {
        ErpFinancePeriodDO period = validateFinancePeriodExists(id);
        if (!ErpFinancePeriodStatusEnum.isClosed(period.getStatus())) {
            throw exception(FINANCE_PERIOD_REOPEN_FAIL_ALREADY_OPEN, period.getPeriodCode());
        }
        if (erpFinancePeriodMapper.selectLaterClosedCount(period.getLedgerId(), period.getPeriodSort()) > 0) {
            throw exception(FINANCE_PERIOD_REOPEN_FAIL_LATER_CLOSED, period.getPeriodCode());
        }
        erpFinancePeriodMapper.updateById(new ErpFinancePeriodDO()
                .setId(id)
                .setStatus(ErpFinancePeriodStatusEnum.OPEN.getStatus())
                .setCloseTime(null)
                .setCloseUserId(null));
    }

    @Override
    public ErpFinancePeriodDO getFinancePeriod(Long id) {
        return erpFinancePeriodMapper.selectById(id);
    }

    @Override
    public PageResult<ErpFinancePeriodDO> getFinancePeriodPage(ErpFinancePeriodPageReqVO pageReqVO) {
        return erpFinancePeriodMapper.selectPage(pageReqVO);
    }

    @Override
    public ErpFinancePeriodDO getCurrentOpenPeriod(Long ledgerId, LocalDate bizDate) {
        ErpFinanceLedgerDO ledger = financeLedgerService.validateFinanceLedger(ledgerId);
        LocalDate actualBizDate = bizDate == null ? LocalDate.now() : bizDate;
        ErpFinancePeriodDO period = erpFinancePeriodMapper.selectCurrentOpenByDate(ledgerId, actualBizDate);
        if (period == null) {
            throw exception(FINANCE_PERIOD_CURRENT_OPEN_NOT_EXISTS, ledger.getName(), actualBizDate);
        }
        return period;
    }

    private ErpFinancePeriodDO validateFinancePeriodExists(Long id) {
        ErpFinancePeriodDO period = erpFinancePeriodMapper.selectById(id);
        if (period == null) {
            throw exception(FINANCE_PERIOD_NOT_EXISTS);
        }
        return period;
    }

    private void validateFinancePeriodUnique(ErpFinanceLedgerDO ledger, Integer periodSort, String periodCode) {
        ErpFinancePeriodDO existed = erpFinancePeriodMapper.selectByLedgerIdAndPeriodSort(ledger.getId(), periodSort);
        if (existed != null) {
            throw exception(FINANCE_PERIOD_ALREADY_EXISTS, ledger.getName(), periodCode);
        }
    }

    private void fillPeriodCalendarFields(ErpFinancePeriodDO period, Integer year, Integer month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        period.setPeriodYear(year)
                .setPeriodMonth(month)
                .setPeriodCode(buildPeriodCode(year, month))
                .setPeriodSort(buildPeriodSort(year, month))
                .setStartDate(yearMonth.atDay(1))
                .setEndDate(yearMonth.atEndOfMonth());
    }

    private Integer buildPeriodSort(Integer year, Integer month) {
        return year * 100 + month;
    }

    private String buildPeriodCode(Integer year, Integer month) {
        return String.format("%04d-%02d", year, month);
    }
}
