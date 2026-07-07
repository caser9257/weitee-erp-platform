package cn.weitee.erp.module.erp.service.finance;

import cn.weitee.erp.module.erp.controller.admin.finance.vo.voucher.ErpFinanceVoucherIntegrityCheckRespVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceLedgerDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceVoucherDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseInDO;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceVoucherEntryMapper;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceVoucherMapper;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseInMapper;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseReturnMapper;
import cn.weitee.erp.module.erp.dal.mysql.sale.ErpSaleOutMapper;
import cn.weitee.erp.module.erp.dal.mysql.sale.ErpSaleReturnMapper;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.enums.common.ErpBizTypeEnum;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static cn.weitee.erp.framework.common.enums.CommonStatusEnum.ENABLE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ErpFinanceVoucherIntegrityServiceImplTest {

    @InjectMocks
    private ErpFinanceVoucherIntegrityServiceImpl service;

    @Mock
    private ErpFinanceVoucherMapper voucherMapper;
    @Mock
    private ErpFinanceVoucherEntryMapper voucherEntryMapper;
    @Mock
    private ErpFinanceLedgerService financeLedgerService;
    @Mock
    private ErpFinanceVoucherService financeVoucherService;
    @Mock
    private ErpSaleOutMapper saleOutMapper;
    @Mock
    private ErpSaleReturnMapper saleReturnMapper;
    @Mock
    private ErpPurchaseInMapper purchaseInMapper;
    @Mock
    private ErpPurchaseReturnMapper purchaseReturnMapper;

    @Test
    void checkIntegrity_shouldBatchResolvePurchaseInMissingVouchers() {
        List<Long> ledgerIds = List.of(1L, 2L);
        List<ErpPurchaseInDO> approvedList = List.of(
                purchaseIn(101L, "PI-101"),
                purchaseIn(102L, "PI-102")
        );
        when(financeLedgerService.getFinanceLedgerListByStatus(ENABLE.getStatus())).thenReturn(List.of(
                ledger(1L, "主账簿"),
                ledger(2L, "辅助账簿")
        ));
        when(financeLedgerService.getFinanceLedgerMap(ledgerIds)).thenReturn(java.util.Map.of(
                1L, ledger(1L, "主账簿"),
                2L, ledger(2L, "辅助账簿")
        ));
        when(purchaseInMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(approvedList);
        when(voucherMapper.selectListByLedgerIdsAndBizTypeAndBizIds(
                eq(ledgerIds),
                eq(ErpBizTypeEnum.PURCHASE_IN.getType()),
                eq(Set.of(101L, 102L))
        )).thenReturn(List.of(
                voucher(1L, ErpBizTypeEnum.PURCHASE_IN.getType(), 101L)
        ));
        when(voucherMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());

        ErpFinanceVoucherIntegrityCheckRespVO result = service.checkIntegrity(
                ErpBizTypeEnum.PURCHASE_IN.getType(),
                null,
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 31)
        );

        assertEquals(3, result.getMissingCount());
        assertEquals(0, result.getUnbalancedCount());
        assertEquals(List.of("辅助账簿", "主账簿", "辅助账簿"),
                result.getMissingVouchers().stream().map(ErpFinanceVoucherIntegrityCheckRespVO.MissingVoucherItem::getLedgerName).toList());
        verify(voucherMapper, times(1)).selectListByLedgerIdsAndBizTypeAndBizIds(
                eq(ledgerIds),
                eq(ErpBizTypeEnum.PURCHASE_IN.getType()),
                eq(Set.of(101L, 102L))
        );
        verify(voucherMapper, never()).selectByLedgerIdAndBiz(
                ArgumentMatchers.anyLong(),
                eq(ErpBizTypeEnum.PURCHASE_IN.getType()),
                ArgumentMatchers.anyLong()
        );
    }

    @Test
    void checkIntegrity_shouldSkipBatchVoucherLookupWhenNoApprovedPurchaseInExists() {
        when(financeLedgerService.getFinanceLedgerListByStatus(ENABLE.getStatus())).thenReturn(List.of(
                ledger(1L, "主账簿")
        ));
        when(financeLedgerService.getFinanceLedgerMap(List.of(1L))).thenReturn(java.util.Map.of(
                1L, ledger(1L, "主账簿")
        ));
        when(purchaseInMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());
        when(voucherMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());

        ErpFinanceVoucherIntegrityCheckRespVO result = service.checkIntegrity(
                ErpBizTypeEnum.PURCHASE_IN.getType(),
                null,
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 31)
        );

        assertEquals(0, result.getMissingCount());
        verify(voucherMapper, never()).selectListByLedgerIdsAndBizTypeAndBizIds(any(), any(), any());
    }

    private ErpPurchaseInDO purchaseIn(Long id, String no) {
        return new ErpPurchaseInDO()
                .setId(id)
                .setNo(no)
                .setStatus(ErpAuditStatus.APPROVE.getStatus())
                .setInTime(LocalDateTime.of(2026, 7, 2, 10, 0));
    }

    private ErpFinanceVoucherDO voucher(Long ledgerId, Integer bizType, Long bizId) {
        return new ErpFinanceVoucherDO()
                .setLedgerId(ledgerId)
                .setBizType(bizType)
                .setBizId(bizId);
    }

    private ErpFinanceLedgerDO ledger(Long id, String name) {
        return new ErpFinanceLedgerDO().setId(id).setName(name);
    }
}
