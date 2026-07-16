package cn.weitee.erp.module.erp.service.finance;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.ledger.ErpFinanceLedgerPageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceLedgerDO;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceLedgerMapper;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinancePeriodMapper;
import cn.weitee.erp.module.erp.service.finance.interceptor.FinanceDataPermissionContext;
import cn.weitee.erp.module.erp.service.finance.interceptor.FinancePermissionScope;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ErpFinanceLedgerPermissionTest {

    @Mock
    private ErpFinanceLedgerMapper ledgerMapper;
    @Mock
    private ErpFinancePeriodMapper periodMapper;
    @Mock
    private FinanceDataPermissionService permissionService;
    @InjectMocks
    private ErpFinanceLedgerServiceImpl service;

    @AfterEach
    void clearPermissionContext() {
        FinanceDataPermissionContext.clear();
    }

    @Test
    void getPage_shouldPushLimitedLedgerScopeToMapper() {
        ErpFinanceLedgerPageReqVO reqVO = new ErpFinanceLedgerPageReqVO();
        PageResult<ErpFinanceLedgerDO> expected = new PageResult<>(
                List.of(new ErpFinanceLedgerDO().setId(99603L)), 1L);
        when(ledgerMapper.selectPageByVisibleLedgerIds(reqVO, Set.of(99603L))).thenReturn(expected);
        FinanceDataPermissionContext.setPermissionScope(new FinancePermissionScope(
                FinancePermissionScope.Scope.limited(Set.of(99603L)),
                FinancePermissionScope.Scope.all(), Map.of(), false, false));

        PageResult<ErpFinanceLedgerDO> actual = service.getFinanceLedgerPage(reqVO);

        assertThat(actual.getList()).extracting(ErpFinanceLedgerDO::getId).containsExactly(99603L);
        verify(ledgerMapper).selectPageByVisibleLedgerIds(reqVO, Set.of(99603L));
    }

    @Test
    void getPage_shouldReturnEmptyWhenLedgerScopeIsNone() {
        FinanceDataPermissionContext.setPermissionScope(new FinancePermissionScope(
                FinancePermissionScope.Scope.none(),
                FinancePermissionScope.Scope.all(), Map.of(), false, false));

        PageResult<ErpFinanceLedgerDO> actual = service.getFinanceLedgerPage(new ErpFinanceLedgerPageReqVO());

        assertThat(actual.getTotal()).isZero();
    }

}
