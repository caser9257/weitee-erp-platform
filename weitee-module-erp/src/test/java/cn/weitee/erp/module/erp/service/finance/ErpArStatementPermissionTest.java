package cn.weitee.erp.module.erp.service.finance;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.arstatement.ErpArStatementPageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpArStatementDO;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpArStatementItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpArStatementMapper;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceDualLedgerConfigMapper;
import cn.weitee.erp.module.erp.service.finance.interceptor.FinanceDataPermissionContext;
import cn.weitee.erp.module.erp.service.finance.interceptor.FinancePermissionScope;
import cn.weitee.erp.module.erp.service.sale.ErpCustomerService;
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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ErpArStatementPermissionTest {

    @Mock
    private ErpArStatementMapper statementMapper;
    @Mock
    private ErpArStatementItemMapper statementItemMapper;
    @Mock
    private ErpFinanceDualLedgerConfigMapper dualLedgerConfigMapper;
    @Mock
    private ErpCustomerService customerService;
    @InjectMocks
    private ErpArStatementServiceImpl service;

    @AfterEach
    void clearPermissionContext() {
        FinanceDataPermissionContext.clear();
    }

    @Test
    void page_shouldPushLimitedLedgerScopeToMapper() {
        ErpArStatementPageReqVO request = new ErpArStatementPageReqVO();
        PageResult<ErpArStatementDO> expected = new PageResult<>(
                List.of(new ErpArStatementDO().setId(1L).setLedgerId(99603L)), 1L);
        when(statementMapper.selectPageByVisibleLedgerIds(request, Set.of(99603L))).thenReturn(expected);
        when(customerService.getCustomerMap(Set.of())).thenReturn(Map.of());
        FinanceDataPermissionContext.setPermissionScope(new FinancePermissionScope(
                FinancePermissionScope.Scope.limited(Set.of(99603L)),
                FinancePermissionScope.Scope.all(), Map.of(), true, true));

        PageResult<?> actual = service.getStatementPage(request);

        assertThat(actual.getTotal()).isEqualTo(1L);
        verify(statementMapper).selectPageByVisibleLedgerIds(request, Set.of(99603L));
    }

    @Test
    void page_shouldReturnEmptyWhenLedgerScopeIsNone() {
        FinanceDataPermissionContext.setPermissionScope(new FinancePermissionScope(
                FinancePermissionScope.Scope.none(),
                FinancePermissionScope.Scope.all(), Map.of(), true, true));

        PageResult<?> actual = service.getStatementPage(new ErpArStatementPageReqVO());

        assertThat(actual.getTotal()).isZero();
        verifyNoInteractions(statementMapper);
    }
}
