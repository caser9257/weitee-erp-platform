package cn.weitee.erp.module.erp.service.finance;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.voucher.ErpFinanceVoucherPageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceVoucherDO;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceVoucherMapper;
import cn.weitee.erp.module.erp.service.finance.interceptor.FinancePermissionScope;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ErpFinanceVoucherServiceImplPermissionTest {

    @Mock
    private ErpFinanceVoucherMapper financeVoucherMapper;
    @Mock
    private FinanceDataPermissionService financeDataPermissionService;
    @InjectMocks
    private ErpFinanceVoucherServiceImpl service;

    @Test
    void getVoucherPage_shouldRestrictQueryToVisibleLedgers() {
        ErpFinanceVoucherPageReqVO reqVO = new ErpFinanceVoucherPageReqVO();
        PageResult<ErpFinanceVoucherDO> expected = PageResult.empty(0L);
        when(financeDataPermissionService.getVisibleLedgerIds()).thenReturn(List.of(99603L));
        when(financeVoucherMapper.selectPageByVisibleLedgerIds(reqVO, List.of(99603L))).thenReturn(expected);

        PageResult<ErpFinanceVoucherDO> result = service.getVoucherPage(reqVO);

        assertSame(expected, result);
        verify(financeVoucherMapper).selectPageByVisibleLedgerIds(reqVO, List.of(99603L));
    }

    @Test
    void getVoucherPage_shouldExcludeVoucherContainingUnauthorizedSubject() {
        ErpFinanceVoucherPageReqVO reqVO = new ErpFinanceVoucherPageReqVO();
        PageResult<ErpFinanceVoucherDO> expected = PageResult.empty(0L);
        FinancePermissionScope permissionScope = new FinancePermissionScope(
                FinancePermissionScope.Scope.limited(Set.of(99603L)),
                FinancePermissionScope.Scope.limited(Set.of(10L)),
                Map.of(99603L, FinancePermissionScope.Scope.limited(Set.of("1001"))), false, false);
        when(financeDataPermissionService.getVisibleLedgerIds()).thenReturn(List.of(99603L));
        when(financeDataPermissionService.getPermissionScope()).thenReturn(permissionScope);
        when(financeVoucherMapper.selectPageByVisibleLedgerIdsAndDeptIdsAndSubjectCodes(
                reqVO, List.of(99603L), Set.of(10L), Map.of(99603L, Set.of("1001"))))
                .thenReturn(expected);

        PageResult<ErpFinanceVoucherDO> result = service.getVoucherPage(reqVO);

        assertSame(expected, result);
        verify(financeVoucherMapper).selectPageByVisibleLedgerIdsAndDeptIdsAndSubjectCodes(
                reqVO, List.of(99603L), Set.of(10L), Map.of(99603L, Set.of("1001")));
    }
}
