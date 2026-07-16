package cn.weitee.erp.module.erp.controller.admin.finance;

import cn.weitee.erp.framework.common.exception.ServiceException;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.period.ErpFinancePeriodSaveReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.reportitem.ErpFinanceReportItemSaveReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.subject.ErpFinanceSubjectSaveReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.voucher.ErpFinanceVoucherTemplateSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinancePeriodDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceReportItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceReportItemSubjectDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceSubjectDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceVoucherTemplateDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceVoucherTemplateItemDO;
import cn.weitee.erp.module.erp.service.finance.ErpFinanceLedgerService;
import cn.weitee.erp.module.erp.service.finance.ErpFinancePeriodService;
import cn.weitee.erp.module.erp.service.finance.ErpFinanceReportItemService;
import cn.weitee.erp.module.erp.service.finance.ErpFinanceSubjectService;
import cn.weitee.erp.module.erp.service.finance.ErpFinanceVoucherTemplateService;
import cn.weitee.erp.module.erp.service.finance.FinanceDataPermissionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ErpFinanceLedgerScopedControllerPermissionTest {

    private static final Long HIDDEN_LEDGER_ID = 99604L;
    private static final Long RECORD_ID = 1L;

    @Mock
    private FinanceDataPermissionService financeDataPermissionService;
    @Mock
    private ErpFinanceLedgerService financeLedgerService;
    @Mock
    private ErpFinancePeriodService financePeriodService;
    @Mock
    private ErpFinanceSubjectService financeSubjectService;
    @Mock
    private ErpFinanceReportItemService financeReportItemService;
    @Mock
    private ErpFinanceVoucherTemplateService voucherTemplateService;
    @InjectMocks
    private ErpFinancePeriodController periodController;
    @InjectMocks
    private ErpFinanceSubjectController subjectController;
    @InjectMocks
    private ErpFinanceReportItemController reportItemController;
    @InjectMocks
    private ErpFinanceVoucherTemplateController voucherTemplateController;

    @Test
    void periodGet_whenLedgerIsHidden_throwsForbiddenBeforeBuildingResponse() {
        when(financePeriodService.getFinancePeriod(RECORD_ID))
                .thenReturn(new ErpFinancePeriodDO().setId(RECORD_ID).setLedgerId(HIDDEN_LEDGER_ID));
        when(financeDataPermissionService.canAccessLedger(HIDDEN_LEDGER_ID)).thenReturn(false);

        assertThrows(ServiceException.class, () -> periodController.getFinancePeriod(RECORD_ID));
    }

    @Test
    void periodCreate_whenLedgerIsHidden_throwsForbiddenBeforeWrite() {
        when(financeDataPermissionService.canAccessLedger(HIDDEN_LEDGER_ID)).thenReturn(false);

        assertThrows(ServiceException.class, () -> periodController.createFinancePeriod(
                new ErpFinancePeriodSaveReqVO().setLedgerId(HIDDEN_LEDGER_ID)));

        verify(financePeriodService, never()).createFinancePeriod(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void periodClose_whenLedgerIsHidden_throwsForbiddenBeforeWrite() {
        when(financePeriodService.getFinancePeriod(RECORD_ID))
                .thenReturn(new ErpFinancePeriodDO().setId(RECORD_ID).setLedgerId(HIDDEN_LEDGER_ID));
        when(financeDataPermissionService.canAccessLedger(HIDDEN_LEDGER_ID)).thenReturn(false);

        assertThrows(ServiceException.class, () -> periodController.closeFinancePeriod(RECORD_ID));

        verify(financePeriodService, never()).closeFinancePeriod(RECORD_ID);
    }

    @Test
    void subjectGet_whenLedgerIsHidden_throwsForbiddenBeforeResponse() {
        when(financeSubjectService.getFinanceSubject(RECORD_ID))
                .thenReturn(new ErpFinanceSubjectDO().setId(RECORD_ID).setLedgerId(HIDDEN_LEDGER_ID));
        when(financeDataPermissionService.canAccessLedger(HIDDEN_LEDGER_ID)).thenReturn(false);

        assertThrows(ServiceException.class, () -> subjectController.getFinanceSubject(RECORD_ID));
    }

    @Test
    void subjectGet_whenSubjectIsHidden_throwsForbiddenBeforeResponse() {
        when(financeSubjectService.getFinanceSubject(RECORD_ID))
                .thenReturn(new ErpFinanceSubjectDO().setId(RECORD_ID).setLedgerId(99603L).setSubjectCode("660201"));
        when(financeDataPermissionService.canAccessLedger(99603L)).thenReturn(true);
        when(financeDataPermissionService.canAccessSubject(99603L, "660201")).thenReturn(false);

        assertThrows(ServiceException.class, () -> subjectController.getFinanceSubject(RECORD_ID));
    }

    @Test
    void subjectCreate_whenLedgerIsHidden_throwsForbiddenBeforeWrite() {
        when(financeDataPermissionService.canAccessLedger(HIDDEN_LEDGER_ID)).thenReturn(false);

        assertThrows(ServiceException.class, () -> subjectController.createFinanceSubject(
                new ErpFinanceSubjectSaveReqVO().setLedgerId(HIDDEN_LEDGER_ID)));

        verify(financeSubjectService, never()).createFinanceSubject(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void subjectCreate_whenSubjectIsHidden_throwsForbiddenBeforeWrite() {
        when(financeDataPermissionService.canAccessLedger(99603L)).thenReturn(true);
        when(financeDataPermissionService.canAccessSubject(99603L, "660201")).thenReturn(false);

        assertThrows(ServiceException.class, () -> subjectController.createFinanceSubject(
                new ErpFinanceSubjectSaveReqVO().setLedgerId(99603L).setSubjectCode("660201")));

        verify(financeSubjectService, never()).createFinanceSubject(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void subjectUpdate_whenExistingLedgerIsHidden_throwsForbiddenBeforeWrite() {
        when(financeSubjectService.getFinanceSubject(RECORD_ID))
                .thenReturn(new ErpFinanceSubjectDO().setId(RECORD_ID).setLedgerId(HIDDEN_LEDGER_ID));
        when(financeDataPermissionService.canAccessLedger(99603L)).thenReturn(true);
        when(financeDataPermissionService.canAccessLedger(HIDDEN_LEDGER_ID)).thenReturn(false);

        assertThrows(ServiceException.class, () -> subjectController.updateFinanceSubject(
                new ErpFinanceSubjectSaveReqVO().setId(RECORD_ID).setLedgerId(99603L)));

        verify(financeSubjectService, never()).updateFinanceSubject(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void subjectDelete_whenLedgerIsHidden_throwsForbiddenBeforeWrite() {
        when(financeSubjectService.getFinanceSubject(RECORD_ID))
                .thenReturn(new ErpFinanceSubjectDO().setId(RECORD_ID).setLedgerId(HIDDEN_LEDGER_ID));
        when(financeDataPermissionService.canAccessLedger(HIDDEN_LEDGER_ID)).thenReturn(false);

        assertThrows(ServiceException.class, () -> subjectController.deleteFinanceSubject(RECORD_ID));

        verify(financeSubjectService, never()).deleteFinanceSubject(RECORD_ID);
    }

    @Test
    void subjectSimpleList_whenLedgerIsHidden_throwsForbiddenBeforeQuery() {
        when(financeDataPermissionService.canAccessLedger(HIDDEN_LEDGER_ID)).thenReturn(false);

        assertThrows(ServiceException.class, () -> subjectController.getFinanceSubjectSimpleList(HIDDEN_LEDGER_ID));

        verify(financeSubjectService, never()).getFinanceSubjectListByLedgerId(HIDDEN_LEDGER_ID);
    }

    @Test
    void reportItemGet_whenLedgerIsHidden_throwsForbiddenBeforeResponse() {
        when(financeReportItemService.getFinanceReportItem(RECORD_ID))
                .thenReturn(new ErpFinanceReportItemDO().setId(RECORD_ID).setLedgerId(HIDDEN_LEDGER_ID));
        when(financeDataPermissionService.canAccessLedger(HIDDEN_LEDGER_ID)).thenReturn(false);

        assertThrows(ServiceException.class, () -> reportItemController.getFinanceReportItem(RECORD_ID));
    }

    @Test
    void reportItemCreate_whenLedgerIsHidden_throwsForbiddenBeforeWrite() {
        when(financeDataPermissionService.canAccessLedger(HIDDEN_LEDGER_ID)).thenReturn(false);

        assertThrows(ServiceException.class, () -> reportItemController.createFinanceReportItem(
                new ErpFinanceReportItemSaveReqVO().setLedgerId(HIDDEN_LEDGER_ID)));

        verify(financeReportItemService, never()).createFinanceReportItem(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void reportItemCreate_whenMappedSubjectIsHidden_throwsForbiddenBeforeWrite() {
        when(financeDataPermissionService.canAccessLedger(99603L)).thenReturn(true);
        when(financeDataPermissionService.canAccessSubject(99603L, "660201")).thenReturn(false);

        assertThrows(ServiceException.class, () -> reportItemController.createFinanceReportItem(
                new ErpFinanceReportItemSaveReqVO().setLedgerId(99603L)
                        .setSubjects(java.util.List.of(new ErpFinanceReportItemSaveReqVO.SubjectMapping()
                                .setSubjectCode("660201")))));

        verify(financeReportItemService, never()).createFinanceReportItem(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void reportItemGet_whenMappedSubjectIsHidden_throwsForbiddenBeforeResponse() {
        when(financeReportItemService.getFinanceReportItem(RECORD_ID))
                .thenReturn(new ErpFinanceReportItemDO().setId(RECORD_ID).setLedgerId(99603L));
        when(financeReportItemService.getFinanceReportItemSubjectListByItemId(RECORD_ID))
                .thenReturn(java.util.List.of(new ErpFinanceReportItemSubjectDO().setItemId(RECORD_ID)
                        .setSubjectCode("660201")));
        when(financeDataPermissionService.canAccessLedger(99603L)).thenReturn(true);
        when(financeDataPermissionService.canAccessSubject(99603L, "660201")).thenReturn(false);

        assertThrows(ServiceException.class, () -> reportItemController.getFinanceReportItem(RECORD_ID));
    }

    @Test
    void reportItemUpdate_whenExistingLedgerIsHidden_throwsForbiddenBeforeWrite() {
        when(financeReportItemService.getFinanceReportItem(RECORD_ID))
                .thenReturn(new ErpFinanceReportItemDO().setId(RECORD_ID).setLedgerId(HIDDEN_LEDGER_ID));
        when(financeDataPermissionService.canAccessLedger(99603L)).thenReturn(true);
        when(financeDataPermissionService.canAccessLedger(HIDDEN_LEDGER_ID)).thenReturn(false);

        assertThrows(ServiceException.class, () -> reportItemController.updateFinanceReportItem(
                new ErpFinanceReportItemSaveReqVO().setId(RECORD_ID).setLedgerId(99603L)));

        verify(financeReportItemService, never()).updateFinanceReportItem(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void reportItemDelete_whenLedgerIsHidden_throwsForbiddenBeforeWrite() {
        when(financeReportItemService.getFinanceReportItem(RECORD_ID))
                .thenReturn(new ErpFinanceReportItemDO().setId(RECORD_ID).setLedgerId(HIDDEN_LEDGER_ID));
        when(financeDataPermissionService.canAccessLedger(HIDDEN_LEDGER_ID)).thenReturn(false);

        assertThrows(ServiceException.class, () -> reportItemController.deleteFinanceReportItem(RECORD_ID));

        verify(financeReportItemService, never()).deleteFinanceReportItem(RECORD_ID);
    }

    @Test
    void voucherTemplateGet_whenLedgerIsHidden_throwsForbiddenBeforeResponse() {
        when(voucherTemplateService.getVoucherTemplate(RECORD_ID))
                .thenReturn(new ErpFinanceVoucherTemplateDO().setId(RECORD_ID).setLedgerId(HIDDEN_LEDGER_ID));
        when(financeDataPermissionService.canAccessLedger(HIDDEN_LEDGER_ID)).thenReturn(false);

        assertThrows(ServiceException.class, () -> voucherTemplateController.getVoucherTemplate(RECORD_ID));
    }

    @Test
    void voucherTemplateCreate_whenLedgerIsHidden_throwsForbiddenBeforeWrite() {
        when(financeDataPermissionService.canAccessLedger(HIDDEN_LEDGER_ID)).thenReturn(false);

        assertThrows(ServiceException.class, () -> voucherTemplateController.createVoucherTemplate(
                new ErpFinanceVoucherTemplateSaveReqVO().setLedgerId(HIDDEN_LEDGER_ID)));

        verify(voucherTemplateService, never()).createVoucherTemplate(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void voucherTemplateCreate_whenAnySubjectIsHidden_throwsForbiddenBeforeWrite() {
        ErpFinanceVoucherTemplateSaveReqVO request = new ErpFinanceVoucherTemplateSaveReqVO()
                .setLedgerId(99603L)
                .setItems(java.util.List.of(new ErpFinanceVoucherTemplateSaveReqVO.Item().setSubjectCode("660201")));
        when(financeDataPermissionService.canAccessLedger(99603L)).thenReturn(true);
        when(financeDataPermissionService.canAccessSubject(99603L, "660201")).thenReturn(false);

        assertThrows(ServiceException.class, () -> voucherTemplateController.createVoucherTemplate(request));

        verify(voucherTemplateService, never()).createVoucherTemplate(request);
    }

    @Test
    void voucherTemplateGet_whenAnySubjectIsHidden_throwsForbiddenBeforeResponse() {
        when(voucherTemplateService.getVoucherTemplate(RECORD_ID))
                .thenReturn(new ErpFinanceVoucherTemplateDO().setId(RECORD_ID).setLedgerId(99603L));
        when(voucherTemplateService.getVoucherTemplateItemListByTemplateId(RECORD_ID))
                .thenReturn(java.util.List.of(new ErpFinanceVoucherTemplateItemDO().setTemplateId(RECORD_ID)
                        .setSubjectCode("660201")));
        when(financeDataPermissionService.canAccessLedger(99603L)).thenReturn(true);
        when(financeDataPermissionService.canAccessSubject(99603L, "660201")).thenReturn(false);

        assertThrows(ServiceException.class, () -> voucherTemplateController.getVoucherTemplate(RECORD_ID));
    }

    @Test
    void voucherTemplateUpdate_whenExistingLedgerIsHidden_throwsForbiddenBeforeWrite() {
        when(voucherTemplateService.getVoucherTemplate(RECORD_ID))
                .thenReturn(new ErpFinanceVoucherTemplateDO().setId(RECORD_ID).setLedgerId(HIDDEN_LEDGER_ID));
        when(financeDataPermissionService.canAccessLedger(99603L)).thenReturn(true);
        when(financeDataPermissionService.canAccessLedger(HIDDEN_LEDGER_ID)).thenReturn(false);

        assertThrows(ServiceException.class, () -> voucherTemplateController.updateVoucherTemplate(
                new ErpFinanceVoucherTemplateSaveReqVO().setId(RECORD_ID).setLedgerId(99603L)));

        verify(voucherTemplateService, never()).updateVoucherTemplate(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void voucherTemplateDelete_whenLedgerIsHidden_throwsForbiddenBeforeWrite() {
        when(voucherTemplateService.getVoucherTemplate(RECORD_ID))
                .thenReturn(new ErpFinanceVoucherTemplateDO().setId(RECORD_ID).setLedgerId(HIDDEN_LEDGER_ID));
        when(financeDataPermissionService.canAccessLedger(HIDDEN_LEDGER_ID)).thenReturn(false);

        assertThrows(ServiceException.class, () -> voucherTemplateController.deleteVoucherTemplate(RECORD_ID));

        verify(voucherTemplateService, never()).deleteVoucherTemplate(RECORD_ID);
    }

    @Test
    void voucherTemplateSimpleList_whenLedgerIsHidden_throwsForbiddenBeforeQuery() {
        when(financeDataPermissionService.canAccessLedger(HIDDEN_LEDGER_ID)).thenReturn(false);

        assertThrows(ServiceException.class,
                () -> voucherTemplateController.getVoucherTemplateSimpleList(HIDDEN_LEDGER_ID, 11));

        verify(voucherTemplateService, never()).getVoucherTemplateListByLedgerAndBizType(HIDDEN_LEDGER_ID, 11);
    }

    @Test
    void voucherTemplateSimpleList_shouldExcludeTemplateContainingHiddenSubject() {
        when(financeDataPermissionService.canAccessLedger(99603L)).thenReturn(true);
        when(voucherTemplateService.getVoucherTemplateListByLedgerAndBizType(99603L, 11)).thenReturn(java.util.List.of(
                new ErpFinanceVoucherTemplateDO().setId(RECORD_ID).setLedgerId(99603L)
                        .setStatus(cn.weitee.erp.framework.common.enums.CommonStatusEnum.ENABLE.getStatus())));
        when(voucherTemplateService.getVoucherTemplateItemListByTemplateIds(org.mockito.ArgumentMatchers.anyCollection())).thenReturn(java.util.List.of(
                new ErpFinanceVoucherTemplateItemDO().setTemplateId(RECORD_ID).setSubjectCode("660201")));
        when(financeDataPermissionService.canAccessSubject(99603L, "660201")).thenReturn(false);

        java.util.List<?> result = voucherTemplateController.getVoucherTemplateSimpleList(99603L, 11).getData();

        assertTrue(result.isEmpty());
    }
}
