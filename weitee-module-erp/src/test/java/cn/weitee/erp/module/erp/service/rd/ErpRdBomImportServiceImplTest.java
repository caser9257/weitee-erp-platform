package cn.weitee.erp.module.erp.service.rd;

import cn.weitee.erp.framework.common.enums.CommonStatusEnum;
import cn.weitee.erp.framework.common.exception.ServiceException;
import cn.weitee.erp.framework.excel.core.util.FileImportProtector;
import cn.weitee.erp.module.erp.controller.admin.rd.vo.bom.ErpRdBomBaselineDiffVO;
import cn.weitee.erp.module.erp.controller.admin.rd.vo.bom.ErpRdBomImportResultVO;
import cn.weitee.erp.module.erp.controller.admin.rd.vo.bom.ErpRdBomIntegrityIssueRespVO;
import cn.weitee.erp.module.erp.controller.admin.rd.vo.bom.ErpRdBomPrecheckResultVO;
import cn.weitee.erp.module.erp.controller.admin.rd.vo.bom.ErpRdBomSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductDO;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductCadenceDO;
import cn.weitee.erp.module.erp.dal.dataobject.rd.ErpRdBomDO;
import cn.weitee.erp.module.erp.dal.mysql.product.ErpProductCadenceMapper;
import cn.weitee.erp.module.erp.dal.mysql.product.ErpProductMapper;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.service.product.ErpProductService;
import java.util.Map;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.RD_BOM_TOP_MATERIAL_NOT_EXISTS;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.RD_BOM_BASELINE_MISSING;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.RD_BOM_DUPLICATE;

/**
 * 研发 BOM 导入与预检查测试：覆盖混合失败场景归堆、默认导入路径、顶层缺档、缺档编码合并
 */
@ExtendWith(MockitoExtension.class)
class ErpRdBomImportServiceImplTest {

    @Mock
    private FileImportProtector fileImportProtector;
    @Mock
    private ErpProductService productService;
    @Mock
    private ErpProductMapper productMapper;
    @Mock
    private cn.weitee.erp.module.erp.dal.mysql.product.ErpProductCodeHistoryMapper codeHistoryMapper;
    @Mock
    private ErpProductCadenceMapper productCadenceMapper;
    @Mock
    private ErpRdBomService rdBomService;
    @Mock
    private cn.weitee.erp.module.system.service.notify.ImportNotifyHelper importNotifyHelper;
    @InjectMocks
    private ErpRdBomImportServiceImpl importService;

    @BeforeEach
    void setUp() {
        // 加密保护层在测试中直通
        when(fileImportProtector.preparePlainContent(any(), anyString()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(productCadenceMapper.selectListByProductIds(any()))
                .thenAnswer(invocation -> ((Collection<Long>) invocation.getArgument(0)).stream()
                        .map(id -> ErpProductCadenceDO.builder()
                                .productId(id)
                                .schematicPart("RESISTOR")
                                .pcbFootprint("RES_0603")
                                .build())
                        .toList());
    }

    // ========== 标准模板（8 列）用例 ==========

    @Test
    void precheck_mixedRows_shouldClassifyAllCategories() throws Exception {
        byte[] fileBytes = standardWorkbook(builder -> builder
                .row("MAT-OK", "电阻 10K", "0", "", "5", "", "", "")
                .row("MAT-MISS", "钽电容 10uF", "0", "", "2", "", "", "")
                .row("MAT-NA", "接插件 XH", "0", "", "1", "", "", "")
                .row("MAT-DIS", "屏蔽罩", "0", "", "1", "", "", "")
                .row("MAT-OK", "电阻 10K", "0", "", "abc", "", "", ""));
        MockMultipartFile file = new MockMultipartFile("file", "bom.xlsx", null, fileBytes);
        when(productMapper.selectList(any())).thenReturn(List.of(
                approvedProduct(101L, "MAT-OK"),
                productWithAudit(102L, "MAT-NA", ErpAuditStatus.PROCESS.getStatus()),
                disabledProduct(103L, "MAT-DIS")));
        when(productService.getProduct(10L)).thenReturn(approvedProduct(10L, "TOP-1"));

        ErpRdBomPrecheckResultVO result = importService.precheckRdBom(10L, null, null, null, false, file);

        assertEquals(5, result.getTotalCount());
        assertEquals(1, result.getReadyCount());
        assertEquals(3, result.getBlockedCount());
        assertEquals(1, result.getIssueCount());
        assertFalse(result.getReadyToImport());

        assertEquals(1, result.getMissingMaterials().size());
        ErpRdBomPrecheckResultVO.MissingMaterial missing = result.getMissingMaterials().get(0);
        assertEquals("MAT-MISS", missing.getMaterialCode());
        assertEquals("钽电容 10uF", missing.getMaterialName());
        assertEquals(List.of(3), missing.getRowNumbers());
        assertFalse(missing.getTopLevel());

        assertEquals(2, result.getUnapprovedMaterials().size());
        ErpRdBomPrecheckResultVO.UnapprovedMaterial notApproved = findUnapproved(result, 102L);
        assertNotNull(notApproved);
        assertFalse(notApproved.getDisabled());
        assertEquals(Integer.valueOf(10), notApproved.getAuditStatus());
        ErpRdBomPrecheckResultVO.UnapprovedMaterial disabled = findUnapproved(result, 103L);
        assertNotNull(disabled);
        assertTrue(disabled.getDisabled());

        assertEquals(1, result.getRowIssues().size());
        assertEquals(6, result.getRowIssues().get(0).getRowNumber());
    }

    @Test
    void precheck_duplicateMissingCode_shouldMergeIntoOneEntry() throws Exception {
        byte[] fileBytes = standardWorkbook(builder -> builder
                .row("MAT-MISS", "钽电容 10uF", "0", "", "2", "", "", "")
                .row("MAT-MISS", "钽电容 10uF", "0", "", "3", "", "", ""));
        MockMultipartFile file = new MockMultipartFile("file", "bom.xlsx", null, fileBytes);
        when(productMapper.selectList(any())).thenReturn(List.of());
        when(productService.getProduct(10L)).thenReturn(approvedProduct(10L, "TOP-1"));

        ErpRdBomPrecheckResultVO result = importService.precheckRdBom(10L, null, null, null, false, file);

        assertEquals(2, result.getBlockedCount());
        assertEquals(1, result.getMissingMaterials().size());
        assertEquals(List.of(2, 3), result.getMissingMaterials().get(0).getRowNumbers());
    }

    @Test
    void precheck_duplicateMaterialCode_shouldWarnButAllowImport() throws Exception {
        byte[] fileBytes = standardWorkbook(builder -> builder
                .row("MAT-OK", "电阻 10K", "0", "R1", "1", "", "", "")
                .row("MAT-OK", "电阻 10K", "0", "R2", "1", "", "", ""));
        MockMultipartFile file = new MockMultipartFile("file", "bom.xlsx", null, fileBytes);
        when(productMapper.selectList(any())).thenReturn(List.of(approvedProduct(101L, "MAT-OK")));
        when(productService.getProduct(10L)).thenReturn(approvedProduct(10L, "TOP-1"));

        ErpRdBomPrecheckResultVO result = importService.precheckRdBom(10L, null, null, null, false, file);

        assertTrue(result.getReadyToImport(), "重复产品编码仅提醒，仍允许提交");
        assertEquals(1, result.getDuplicateMaterialCodes().size());
        assertEquals("MAT-OK", result.getDuplicateMaterialCodes().get(0).getMaterialCode());
        assertEquals(List.of(2, 3), result.getDuplicateMaterialCodes().get(0).getRowNumbers());
    }

    @Test
    void precheck_duplicateBomIdentity_shouldBlockImport() throws Exception {
        byte[] fileBytes = standardWorkbook(builder -> builder
                .row("MAT-OK", "电阻 10K", "0", "R1", "1", "", "", ""));
        MockMultipartFile file = new MockMultipartFile("file", "bom.xlsx", null, fileBytes);
        when(productMapper.selectList(any())).thenReturn(List.of(approvedProduct(101L, "MAT-OK")));
        when(productService.getProduct(10L)).thenReturn(approvedProduct(10L, "TOP-1"));
        when(rdBomService.getRdBomByIdentity(10L, "TOP-1", null))
                .thenReturn(ErpRdBomDO.builder().id(88L).productId(10L).bomCode("TOP-1").build());

        ErpRdBomPrecheckResultVO result = importService.precheckRdBom(10L, null, null, null, false, file);

        assertFalse(result.getReadyToImport());
        assertNotNull(result.getDuplicateBom());
        assertEquals(88L, result.getDuplicateBom().getId());
        assertEquals("TOP-1", result.getDuplicateBom().getBomCode());
    }

    @Test
    void import_duplicateBomIdentity_shouldBlockBeforeCreate() throws Exception {
        byte[] fileBytes = standardWorkbook(builder -> builder
                .row("MAT-OK", "电阻 10K", "0", "R1", "1", "", "", ""));
        MockMultipartFile file = new MockMultipartFile("file", "bom.xlsx", null, fileBytes);
        when(productMapper.selectList(any())).thenReturn(List.of(approvedProduct(101L, "MAT-OK")));
        when(productService.getProduct(10L)).thenReturn(approvedProduct(10L, "TOP-1"));
        when(rdBomService.getRdBomByIdentity(10L, "TOP-1", null))
                .thenReturn(ErpRdBomDO.builder().id(88L).productId(10L).bomCode("TOP-1").build());

        ServiceException exception = assertThrows(ServiceException.class,
                () -> importService.importRdBom(10L, null, null, null, false, file));

        assertEquals(RD_BOM_DUPLICATE.getCode(), exception.getCode());
        verify(rdBomService, never()).createRdBomForImport(any());
    }

    @Test
    void import_missingMaterial_rowShouldCarryIssueType() throws Exception {
        byte[] fileBytes = standardWorkbook(builder -> builder
                .row("MAT-OK", "电阻 10K", "0", "", "5", "", "", "")
                .row("MAT-MISS", "钽电容 10uF", "0", "", "2", "", "", ""));
        MockMultipartFile file = new MockMultipartFile("file", "bom.xlsx", null, fileBytes);
        when(productMapper.selectList(any())).thenReturn(List.of(approvedProduct(101L, "MAT-OK")));
        when(productService.getProduct(10L)).thenReturn(approvedProduct(10L, "TOP-1"));
        when(rdBomService.validateRdBomItemsIntegrity(any())).thenReturn(List.of());
        when(rdBomService.createRdBomForImport(any())).thenReturn(99L);
        when(rdBomService.validateRdBomIntegrity(99L)).thenReturn(List.of());

        ErpRdBomImportResultVO result = importService.importRdBom(10L, null, null, null, false, file);

        assertEquals(2, result.getTotalCount());
        assertEquals(1, result.getSuccessCount());
        assertEquals(1, result.getFailCount());
        assertEquals(Long.valueOf(99L), result.getBomId());
        assertEquals(1, result.getFailDetails().size());
        assertEquals("MISSING_MATERIAL", result.getFailDetails().get(0).getIssueType());
        assertEquals("MAT-MISS", result.getFailDetails().get(0).getMaterialCode());
    }

    @Test
    void import_allValidRows_shouldCreateDraftWithItems() throws Exception {
        byte[] fileBytes = standardWorkbook(builder -> builder
                .row("MAT-A", "物料 A", "0", "R101-R103", "3", "0.01", "7", "备注A")
                .row("MAT-B", "物料 B", "1", "", "2", "", "", ""));
        MockMultipartFile file = new MockMultipartFile("file", "bom.xlsx", null, fileBytes);
        when(productMapper.selectList(any())).thenReturn(List.of(
                approvedProduct(11L, "MAT-A"),
                approvedProduct(12L, "MAT-B")));
        when(productService.getProduct(10L)).thenReturn(approvedProduct(10L, "TOP-1"));
        when(rdBomService.validateRdBomItemsIntegrity(any())).thenReturn(List.of());
        when(rdBomService.createRdBomForImport(any())).thenReturn(77L);
        when(rdBomService.validateRdBomIntegrity(77L)).thenReturn(List.of());

        ErpRdBomImportResultVO result = importService.importRdBom(10L, null, null, null, false, file);

        assertEquals(Long.valueOf(77L), result.getBomId());
        assertEquals(2, result.getSuccessCount());
        assertEquals(0, result.getFailCount());
        ArgumentCaptor<ErpRdBomSaveReqVO> captor = ArgumentCaptor.forClass(ErpRdBomSaveReqVO.class);
        org.mockito.Mockito.verify(rdBomService).createRdBomForImport(captor.capture());
        assertEquals(Long.valueOf(10L), captor.getValue().getProductId());
        assertEquals(2, captor.getValue().getItems().size());
        assertEquals(Long.valueOf(11L), captor.getValue().getItems().get(0).getMaterialId());
        assertEquals("R101-R103", captor.getValue().getItems().get(0).getReferenceDesignator());
        assertEquals(Long.valueOf(12L), captor.getValue().getItems().get(1).getMaterialId());
    }

    @Test
    void import_duplicateMaterialCode_shouldIncludeWarningInResultAndNotification() throws Exception {
        byte[] fileBytes = standardWorkbook(builder -> builder
                .row("MAT-BAD", "无效用量", "0", "", "abc", "", "", "")
                .row("MAT-OK", "电阻 10K", "0", "R1", "1", "", "", "")
                .row("MAT-OK", "电阻 10K", "0", "R2", "1", "", "", ""));
        MockMultipartFile file = new MockMultipartFile("file", "bom.xlsx", null, fileBytes);
        when(productMapper.selectList(any())).thenReturn(List.of(
                approvedProduct(100L, "MAT-BAD"), approvedProduct(101L, "MAT-OK")));
        when(productService.getProduct(10L)).thenReturn(approvedProduct(10L, "TOP-1"));
        when(rdBomService.validateRdBomItemsIntegrity(any())).thenReturn(List.of());
        when(rdBomService.createRdBomForImport(any())).thenReturn(78L);
        when(rdBomService.validateRdBomIntegrity(78L)).thenReturn(List.of());

        ErpRdBomImportResultVO result = importService.importRdBom(10L, null, null, null, false, file);

        assertEquals(Long.valueOf(78L), result.getBomId());
        assertEquals(1, result.getDuplicateMaterialCodes().size());
        assertEquals("MAT-OK", result.getDuplicateMaterialCodes().get(0).getMaterialCode());
        assertEquals(List.of(3, 4), result.getDuplicateMaterialCodes().get(0).getRowNumbers());
        ArgumentCaptor<List<String>> samplesCaptor = ArgumentCaptor.forClass(List.class);
        org.mockito.Mockito.verify(importNotifyHelper).sendImportResultWithFullDetails(
                eq("erp_import_result_rd_bom"), eq("研发BOM导入"), eq(3), eq(2), eq(1), samplesCaptor.capture());
        assertTrue(samplesCaptor.getValue().get(0).contains("产品编码重复，请再次核对清单后进行提交"));
        assertTrue(samplesCaptor.getValue().get(0).contains("MAT-OK"));
        assertTrue(samplesCaptor.getValue().get(0).contains("第3、4行"));
    }

    // ========== 智能表头（顶层缺档）用例 ==========

    @Test
    void import_integrityErrorRow_shouldBeRejectedWithExcelRowNumber() throws Exception {
        byte[] fileBytes = standardWorkbook(builder -> builder
                .row("MAT-A", "物料 A", "0", "", "3", "", "", "")
                .row("MAT-B", "物料 B", "1", "", "2", "", "", ""));
        MockMultipartFile file = new MockMultipartFile("file", "bom.xlsx", null, fileBytes);
        when(productMapper.selectList(any())).thenReturn(List.of(
                approvedProduct(11L, "MAT-A"),
                approvedProduct(12L, "MAT-B")));
        when(productService.getProduct(10L)).thenReturn(approvedProduct(10L, "TOP-1"));
        // 行级完整性校验：items 第 1 行（MAT-A 采购件缺位号）ERROR
        when(rdBomService.validateRdBomItemsIntegrity(any()))
                .thenReturn(List.of(integrityError(1, 11L, "需位号的元器件未填写位号")));
        when(productService.getProductVOMap(any())).thenReturn(Map.of(11L, productVO(11L, "MAT-A")));
        when(rdBomService.createRdBomForImport(any())).thenReturn(88L);
        when(rdBomService.validateRdBomIntegrity(88L)).thenReturn(List.of());

        ErpRdBomImportResultVO result = importService.importRdBom(10L, null, null, null, false, file);

        assertEquals(2, result.getTotalCount());
        assertEquals(1, result.getSuccessCount());
        assertEquals(1, result.getFailCount());
        assertEquals(Long.valueOf(88L), result.getBomId());
        assertEquals(1, result.getFailDetails().size());
        assertEquals("INTEGRITY_ERROR", result.getFailDetails().get(0).getIssueType());
        assertEquals(Integer.valueOf(2), result.getFailDetails().get(0).getRowNumber());
        assertEquals("需位号的元器件未填写位号", result.getFailDetails().get(0).getReason());
        ArgumentCaptor<ErpRdBomSaveReqVO> captor = ArgumentCaptor.forClass(ErpRdBomSaveReqVO.class);
        org.mockito.Mockito.verify(rdBomService).createRdBomForImport(captor.capture());
        assertEquals(1, captor.getValue().getItems().size());
        assertEquals(Long.valueOf(12L), captor.getValue().getItems().get(0).getMaterialId());
    }

    private ErpRdBomIntegrityIssueRespVO integrityError(int rowIndex, Long materialId, String message) {
        ErpRdBomIntegrityIssueRespVO issue = new ErpRdBomIntegrityIssueRespVO();
        issue.setIssueType(5);
        issue.setSeverity("ERROR");
        issue.setRowIndex(rowIndex);
        issue.setMaterialId(materialId);
        issue.setMessage(message);
        return issue;
    }

    private cn.weitee.erp.module.erp.controller.admin.product.vo.product.ErpProductRespVO productVO(Long id, String materialCode) {
        cn.weitee.erp.module.erp.controller.admin.product.vo.product.ErpProductRespVO vo =
                new cn.weitee.erp.module.erp.controller.admin.product.vo.product.ErpProductRespVO();
        vo.setId(id);
        vo.setMaterialCode(materialCode);
        return vo;
    }

    @Test
    void precheck_topLevelMissing_shouldReportInMissingList() throws Exception {
        byte[] fileBytes = smartWorkbook();
        MockMultipartFile file = new MockMultipartFile("file", "smart-bom.xlsx", null, fileBytes);
        when(productMapper.selectList(any())).thenReturn(List.of(approvedProduct(101L, "MAT-OK")));

        ErpRdBomPrecheckResultVO result = importService.precheckRdBom(null, null, null, null, false, file);

        assertNotNull(result.getDetectedHeader());
        assertEquals("PCBA-V1.2", result.getDetectedHeader().getBomCode());
        assertEquals(Boolean.TRUE, result.getDetectedHeader().getTopLevelMissing());
        assertTrue(result.getMissingMaterials().stream()
                .anyMatch(m -> Boolean.TRUE.equals(m.getTopLevel()) && "PCBA-V1.2".equals(m.getMaterialCode())));
        assertFalse(result.getReadyToImport());
    }

    @Test
    void import_topLevelMissing_shouldThrowBusinessError() throws Exception {
        byte[] fileBytes = smartWorkbook();
        MockMultipartFile file = new MockMultipartFile("file", "smart-bom.xlsx", null, fileBytes);
        when(productMapper.selectList(any())).thenReturn(List.of(approvedProduct(101L, "MAT-OK")));

        ServiceException ex = assertThrows(ServiceException.class,
                () -> importService.importRdBom(null, null, null, null, false, file));

        assertEquals(RD_BOM_TOP_MATERIAL_NOT_EXISTS.getCode(), ex.getCode());
    }

    @Test
    void import_baselineDiff_shouldBeAttachedToResult() throws Exception {
        byte[] fileBytes = standardWorkbook(builder -> builder
                .row("MAT-A", "物料 A", "0", "", "5", "", "", ""));
        MockMultipartFile file = new MockMultipartFile("file", "bom.xlsx", null, fileBytes);
        when(productMapper.selectList(any())).thenReturn(List.of(approvedProduct(11L, "MAT-A")));
        when(productService.getProduct(10L)).thenReturn(approvedProduct(10L, "TOP-1"));
        when(rdBomService.validateRdBomItemsIntegrity(any())).thenReturn(List.of());
        when(rdBomService.createRdBomForImport(any())).thenReturn(64L);
        when(rdBomService.validateRdBomIntegrity(64L)).thenReturn(List.of());
        ErpRdBomBaselineDiffVO diff = new ErpRdBomBaselineDiffVO();
        diff.setBaselineBomId(500L);
        diff.setBaselineVersion("V2.0");
        diff.setMissingItems(new java.util.ArrayList<>());
        when(rdBomService.diffImportAgainstLatest(eq(10L), anyList())).thenReturn(diff);

        ErpRdBomImportResultVO result = importService.importRdBom(10L, null, null, null, false, false, file);

        assertNotNull(result.getBaselineDiff());
        assertEquals(Long.valueOf(500L), result.getBaselineDiff().getBaselineBomId());
    }

    /** 较上一版缺料默认硬拦：整单拒绝不落库 */
    @Test
    void import_baselineMissing_defaultShouldBeBlocked() throws Exception {
        byte[] fileBytes = standardWorkbook(builder -> builder
                .row("MAT-A", "物料 A", "0", "", "5", "", "", ""));
        MockMultipartFile file = new MockMultipartFile("file", "bom.xlsx", null, fileBytes);
        when(productMapper.selectList(any())).thenReturn(List.of(approvedProduct(11L, "MAT-A")));
        when(productService.getProduct(10L)).thenReturn(approvedProduct(10L, "TOP-1"));
        when(rdBomService.validateRdBomItemsIntegrity(any())).thenReturn(List.of());
        when(rdBomService.diffImportAgainstLatest(eq(10L), anyList()))
                .thenReturn(baselineDiffWithMissing("MAT-X"));

        ServiceException ex = assertThrows(ServiceException.class,
                () -> importService.importRdBom(10L, null, null, null, false, false, file));

        assertEquals(RD_BOM_BASELINE_MISSING.getCode(), ex.getCode());
        assertTrue(ex.getMessage().contains("MAT-X"));
    }

    /** 勾选确认后放行：正常创建 BOM */
    @Test
    void import_baselineMissing_withAllowFlag_shouldPass() throws Exception {
        byte[] fileBytes = standardWorkbook(builder -> builder
                .row("MAT-A", "物料 A", "0", "", "5", "", "", ""));
        MockMultipartFile file = new MockMultipartFile("file", "bom.xlsx", null, fileBytes);
        when(productMapper.selectList(any())).thenReturn(List.of(approvedProduct(11L, "MAT-A")));
        when(productService.getProduct(10L)).thenReturn(approvedProduct(10L, "TOP-1"));
        when(rdBomService.validateRdBomItemsIntegrity(any())).thenReturn(List.of());
        when(rdBomService.createRdBomForImport(any())).thenReturn(62L);
        when(rdBomService.validateRdBomIntegrity(62L)).thenReturn(List.of());
        when(rdBomService.diffImportAgainstLatest(eq(10L), anyList()))
                .thenReturn(baselineDiffWithMissing("MAT-X"));

        ErpRdBomImportResultVO result =
                importService.importRdBom(10L, null, null, null, false, true, file);

        assertEquals(Long.valueOf(62L), result.getBomId());
    }

    /** 预检阶段缺料即视为不可导入 */
    @Test
    void precheck_baselineMissing_shouldNotBeReady() throws Exception {
        byte[] fileBytes = standardWorkbook(builder -> builder
                .row("MAT-A", "物料 A", "0", "", "5", "", "", ""));
        MockMultipartFile file = new MockMultipartFile("file", "bom.xlsx", null, fileBytes);
        when(productMapper.selectList(any())).thenReturn(List.of(approvedProduct(11L, "MAT-A")));
        when(productService.getProduct(10L)).thenReturn(approvedProduct(10L, "TOP-1"));
        when(rdBomService.diffImportAgainstLatest(eq(10L), anyList()))
                .thenReturn(baselineDiffWithMissing("MAT-X"));

        ErpRdBomPrecheckResultVO result = importService.precheckRdBom(10L, null, null, null, false, file);

        assertFalse(result.getReadyToImport(), "预检阶段存在缺料差异即阻断");
        assertEquals(1, result.getBaselineDiff().getMissingItems().size());
    }

    private ErpRdBomBaselineDiffVO baselineDiffWithMissing(String code) {
        ErpRdBomBaselineDiffVO diff = new ErpRdBomBaselineDiffVO();
        diff.setBaselineBomId(500L);
        diff.setBaselineVersion("V2.0");
        ErpRdBomBaselineDiffVO.MissingItem missing = new ErpRdBomBaselineDiffVO.MissingItem();
        missing.setMaterialCode(code);
        diff.setMissingItems(List.of(missing));
        return diff;
    }

    @Test
    void precheck_cleanFile_shouldBeReadyToImport() throws Exception {
        byte[] fileBytes = standardWorkbook(builder -> builder
                .row("MAT-OK", "电阻 10K", "0", "", "5", "", "", ""));
        MockMultipartFile file = new MockMultipartFile("file", "bom.xlsx", null, fileBytes);
        when(productMapper.selectList(any())).thenReturn(List.of(approvedProduct(101L, "MAT-OK")));
        when(productService.getProduct(10L)).thenReturn(approvedProduct(10L, "TOP-1"));

        ErpRdBomPrecheckResultVO result = importService.precheckRdBom(10L, null, null, null, false, file);

        assertEquals(1, result.getTotalCount());
        assertEquals(0, result.getBlockedCount());
        assertEquals(0, result.getIssueCount());
        assertTrue(result.getReadyToImport());
        assertTrue(result.getMissingMaterials().isEmpty());
        assertTrue(result.getUnapprovedMaterials().isEmpty());
    }

    @Test
    void precheck_pcbComponentWithoutCadence_shouldBeBlocked() throws Exception {
        byte[] fileBytes = standardWorkbook(builder -> builder
                .row("MAT-CAD", "电阻 10K", "0", "R1", "1", "", "", ""));
        MockMultipartFile file = new MockMultipartFile("file", "bom.xlsx", null, fileBytes);
        when(productMapper.selectList(any())).thenReturn(List.of(
                approvedProduct(101L, "MAT-CAD").setPcbComponent(true)));
        doReturn(List.of()).when(productCadenceMapper).selectListByProductIds(any());
        when(productService.getProduct(10L)).thenReturn(approvedProduct(10L, "TOP-1"));

        ErpRdBomPrecheckResultVO result = importService.precheckRdBom(10L, null, null, null, false, file);

        assertEquals(1, result.getBlockedCount());
        assertFalse(result.getReadyToImport());
        assertEquals(1, result.getRowIssues().size());
        assertEquals("CADENCE_DATA_INCOMPLETE", result.getRowIssues().get(0).getIssueType());
        assertEquals(Integer.valueOf(2), result.getRowIssues().get(0).getRowNumber());
    }

    @Test
    void precheck_nonPcbComponentWithoutCadence_shouldBeReadyToImport() throws Exception {
        byte[] fileBytes = standardWorkbook(builder -> builder
                .row("MAT-MECHANICAL", "固定支架", "0", "", "1", "", "", ""));
        MockMultipartFile file = new MockMultipartFile("file", "bom.xlsx", null, fileBytes);
        when(productMapper.selectList(any())).thenReturn(List.of(
                approvedProduct(101L, "MAT-MECHANICAL").setPcbComponent(false)));
        doReturn(List.of()).when(productCadenceMapper).selectListByProductIds(any());
        when(productService.getProduct(10L)).thenReturn(approvedProduct(10L, "TOP-1"));

        ErpRdBomPrecheckResultVO result = importService.precheckRdBom(10L, null, null, null, false, file);

        assertEquals(0, result.getBlockedCount());
        assertTrue(result.getReadyToImport());
        assertTrue(result.getRowIssues().isEmpty());
    }

    // ========== 规格型号比对用例 ==========

    @Test
    void import_specMismatch_rowShouldBeRejected() throws Exception {
        byte[] fileBytes = standardWorkbookWithSpec(builder -> builder
                .row("MAT-OK", "电阻 10K", "0", "", "5", "", "", "", "10K")
                .row("MAT-BAD", "电阻 20K", "0", "", "2", "", "", "", "20K"));
        MockMultipartFile file = new MockMultipartFile("file", "bom.xlsx", null, fileBytes);
        when(productMapper.selectList(any())).thenReturn(List.of(
                approvedProduct(101L, "MAT-OK").setStandard("10K"),
                approvedProduct(102L, "MAT-BAD").setStandard("47KΩ ±5%")));
        when(productService.getProduct(10L)).thenReturn(approvedProduct(10L, "TOP-1"));
        when(rdBomService.validateRdBomItemsIntegrity(any())).thenReturn(List.of());
        when(rdBomService.createRdBomForImport(any())).thenReturn(66L);
        when(rdBomService.validateRdBomIntegrity(66L)).thenReturn(List.of());

        ErpRdBomImportResultVO result = importService.importRdBom(10L, null, null, null, false, file);

        assertEquals(2, result.getTotalCount());
        assertEquals(1, result.getSuccessCount());
        assertEquals(1, result.getFailCount());
        assertEquals("SPEC_MISMATCH", result.getFailDetails().get(0).getIssueType());
        assertEquals(Integer.valueOf(3), result.getFailDetails().get(0).getRowNumber());
        ArgumentCaptor<ErpRdBomSaveReqVO> captor = ArgumentCaptor.forClass(ErpRdBomSaveReqVO.class);
        org.mockito.Mockito.verify(rdBomService).createRdBomForImport(captor.capture());
        assertEquals(Long.valueOf(101L), captor.getValue().getItems().get(0).getMaterialId());
        assertEquals("10K", captor.getValue().getItems().get(0).getMaterialStandard());
    }

    @Test
    void import_specNormalizedDiffers_shouldPass() throws Exception {
        byte[] fileBytes = standardWorkbookWithSpec(builder -> builder
                .row("MAT-A", "电阻 10K", "0", "", "5", "", "", "", "１０ｋΩ ±１％　 "));
        MockMultipartFile file = new MockMultipartFile("file", "bom.xlsx", null, fileBytes);
        when(productMapper.selectList(any())).thenReturn(List.of(
                approvedProduct(101L, "MAT-A").setStandard("10kΩ±1%")));
        when(productService.getProduct(10L)).thenReturn(approvedProduct(10L, "TOP-1"));
        when(rdBomService.validateRdBomItemsIntegrity(any())).thenReturn(List.of());
        when(rdBomService.createRdBomForImport(any())).thenReturn(65L);
        when(rdBomService.validateRdBomIntegrity(65L)).thenReturn(List.of());

        ErpRdBomImportResultVO result = importService.importRdBom(10L, null, null, null, false, file);

        assertEquals(1, result.getSuccessCount());
        assertEquals(0, result.getFailCount());
    }

    @Test
    void import_modelCodeHeader_shouldPersistMaterialStandardSnapshot() throws Exception {
        byte[] fileBytes = standardWorkbookWithSpecHeader("型号编码", builder ->
                builder.row("MAT-A", "电阻 10K", "0", "", "5", "", "", "", "MODEL-10K"));
        MockMultipartFile file = new MockMultipartFile("file", "bom.xlsx", null, fileBytes);
        when(productMapper.selectList(any())).thenReturn(List.of(
                approvedProduct(101L, "MAT-A").setStandard("MODEL-10K")));
        when(productService.getProduct(10L)).thenReturn(approvedProduct(10L, "TOP-1"));
        when(rdBomService.validateRdBomItemsIntegrity(any())).thenReturn(List.of());
        when(rdBomService.createRdBomForImport(any())).thenReturn(64L);
        when(rdBomService.validateRdBomIntegrity(64L)).thenReturn(List.of());

        ErpRdBomImportResultVO result = importService.importRdBom(10L, null, null, null, false, file);

        assertEquals(1, result.getSuccessCount());
        assertEquals(0, result.getFailCount());
        ArgumentCaptor<ErpRdBomSaveReqVO> captor = ArgumentCaptor.forClass(ErpRdBomSaveReqVO.class);
        org.mockito.Mockito.verify(rdBomService).createRdBomForImport(captor.capture());
        assertEquals("MODEL-10K", captor.getValue().getItems().get(0).getMaterialStandard());
    }

    @Test
    void import_specInExcelButMissingInArchive_shouldBeRejected() throws Exception {
        byte[] fileBytes = standardWorkbookWithSpec(builder -> builder
                .row("MAT-A", "电阻 10K", "0", "", "5", "", "", "", "10K"));
        MockMultipartFile file = new MockMultipartFile("file", "bom.xlsx", null, fileBytes);
        when(productMapper.selectList(any())).thenReturn(List.of(
                approvedProduct(101L, "MAT-A").setStandard(null)));

        ErpRdBomImportResultVO result = importService.importRdBom(10L, null, null, null, false, file);

        assertEquals(1, result.getFailCount());
        assertEquals("SPEC_MISMATCH", result.getFailDetails().get(0).getIssueType());
        assertNull(result.getBomId());
    }

    /** 规格列存在但行为空 = 型号缺失，硬拦（用户真实场景：故意清掉某行产品型号） */
    @Test
    void import_specColumnPresentButBlankRow_shouldBeRejected() throws Exception {
        byte[] fileBytes = standardWorkbookWithSpec(builder -> builder
                .row("MAT-A", "电阻 10K", "0", "", "5", "", "", "", "10K")
                .row("MAT-B", "电容 10uF", "0", "", "2", "", "", "", ""));
        MockMultipartFile file = new MockMultipartFile("file", "bom.xlsx", null, fileBytes);
        when(productMapper.selectList(any())).thenReturn(List.of(
                approvedProduct(111L, "MAT-A").setStandard("10K"),
                approvedProduct(112L, "MAT-B").setStandard("16V")));
        when(productService.getProduct(10L)).thenReturn(approvedProduct(10L, "TOP-1"));
        when(rdBomService.validateRdBomItemsIntegrity(any())).thenReturn(List.of());
        when(rdBomService.createRdBomForImport(any())).thenReturn(61L);
        when(rdBomService.validateRdBomIntegrity(61L)).thenReturn(List.of());

        ErpRdBomImportResultVO result = importService.importRdBom(10L, null, null, null, false, file);

        // 部分成功语义：坏行剔除落失败明细，好行照常入库
        assertEquals(1, result.getFailCount());
        assertEquals("SPEC_MISMATCH", result.getFailDetails().get(0).getIssueType());
        assertTrue(result.getFailDetails().get(0).getReason().contains("产品型号缺失"),
                "空型号行的失败原因必须是「产品型号缺失」");
        assertEquals(1, result.getSuccessCount());
        assertEquals(Long.valueOf(61L), result.getBomId());
    }

    /** 8 列老模板无规格列 → 整体豁免，不触发规格校验 */
    @Test
    void import_noSpecColumn_shouldSkipSpecValidation() throws Exception {
        byte[] fileBytes = standardWorkbook(builder -> builder
                .row("MAT-A", "电阻 10K", "0", "", "5", "", "", ""));
        MockMultipartFile file = new MockMultipartFile("file", "bom.xlsx", null, fileBytes);
        when(productMapper.selectList(any())).thenReturn(List.of(
                approvedProduct(111L, "MAT-A").setStandard(null)));
        when(productService.getProduct(10L)).thenReturn(approvedProduct(10L, "TOP-1"));
        when(rdBomService.validateRdBomItemsIntegrity(any())).thenReturn(List.of());
        when(rdBomService.createRdBomForImport(any())).thenReturn(63L);
        when(rdBomService.validateRdBomIntegrity(63L)).thenReturn(List.of());

        ErpRdBomImportResultVO result = importService.importRdBom(10L, null, null, null, false, file);

        assertEquals(1, result.getSuccessCount());
        assertEquals(0, result.getFailCount());
    }

    /** 空文件诊断：0 行明细时给出明确原因而不是静默成功 0 个 */
    @Test
    void import_emptyFile_shouldExplainNoRows() throws Exception {
        byte[] fileBytes = standardWorkbook(builder -> { });
        MockMultipartFile file = new MockMultipartFile("file", "empty.xlsx", null, fileBytes);

        ErpRdBomImportResultVO result = importService.importRdBom(null, null, null, null, false, file);

        assertEquals(0, result.getTotalCount());
        assertEquals(1, result.getFailCount());
        assertEquals("FILE_EMPTY", result.getFailDetails().get(0).getIssueType());
        assertTrue(result.getFailDetails().get(0).getReason().contains("已扫描"));
        assertNull(result.getBomId());
    }

    @Test
    void precheck_emptyFile_shouldExplainNoRowsAndBlock() throws Exception {
        byte[] fileBytes = standardWorkbook(builder -> { });
        MockMultipartFile file = new MockMultipartFile("file", "empty.xlsx", null, fileBytes);

        ErpRdBomPrecheckResultVO result = importService.precheckRdBom(null, null, null, null, false, file);

        assertEquals(0, result.getTotalCount());
        assertFalse(result.getReadyToImport());
        assertEquals(1, result.getRowIssues().size());
        assertEquals("FILE_EMPTY", result.getRowIssues().get(0).getIssueType());
        assertTrue(result.getRowIssues().get(0).getReason().contains("已扫描"));
    }

    /** 明细不在第一个工作表：封面页在前、明细在第二张表 → 应自动定位明细表并识别封面页的 BOM 头 */
    @Test
    void import_multiSheet_detailOnSecondSheet_shouldBeParsed() throws Exception {
        byte[] fileBytes = multiSheetWorkbook();
        MockMultipartFile file = new MockMultipartFile("file", "multi.xlsx", null, fileBytes);
        when(productMapper.selectList(any())).thenReturn(List.of(
                approvedProduct(201L, "04.01.0009"),
                approvedProduct(202L, "04.01.0001"),
                approvedProduct(203L, "04.02.0002")));
        when(rdBomService.validateRdBomItemsIntegrity(any())).thenReturn(List.of());
        when(rdBomService.createRdBomForImport(any())).thenReturn(71L);
        when(rdBomService.validateRdBomIntegrity(71L)).thenReturn(List.of());

        ErpRdBomImportResultVO result = importService.importRdBom(null, null, null, null, false, file);

        assertEquals(2, result.getTotalCount(), "明细在第二张表也应被解析");
        assertEquals(2, result.getSuccessCount());
        assertEquals(Long.valueOf(71L), result.getBomId());
        ArgumentCaptor<ErpRdBomSaveReqVO> captor = ArgumentCaptor.forClass(ErpRdBomSaveReqVO.class);
        org.mockito.Mockito.verify(rdBomService).createRdBomForImport(captor.capture());
        assertEquals(Long.valueOf(201L), captor.getValue().getProductId(),
                "封面页的 BOM 头编码应识别出顶层成品");
    }

    /** 封面页在前但整本无明细表头、也无标准表头 → FILE_EMPTY 诊断 */
    @Test
    void precheck_multiSheet_coverOnly_shouldExplainNoRows() throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet cover = workbook.createSheet("封面");
            cover.createRow(0).createCell(0).setCellValue("文件名称");
            cover.createRow(1).createCell(0).setCellValue("版本履历");
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            MockMultipartFile file = new MockMultipartFile("file", "cover-only.xlsx", null, out.toByteArray());

            ErpRdBomPrecheckResultVO result = importService.precheckRdBom(null, null, null, null, false, file);

            assertFalse(result.getReadyToImport());
            assertEquals("FILE_EMPTY", result.getRowIssues().get(0).getIssueType());
        }
    }

    @Test
    void precheck_specMismatch_shouldBeBlockedInRowIssues() throws Exception {
        byte[] fileBytes = standardWorkbookWithSpec(builder -> builder
                .row("MAT-BAD", "电阻 20K", "0", "", "2", "", "", "", "33K"));
        MockMultipartFile file = new MockMultipartFile("file", "bom.xlsx", null, fileBytes);
        when(productMapper.selectList(any())).thenReturn(List.of(
                approvedProduct(102L, "MAT-BAD").setStandard("47K")));
        when(productService.getProduct(10L)).thenReturn(approvedProduct(10L, "TOP-1"));

        ErpRdBomPrecheckResultVO result = importService.precheckRdBom(10L, null, null, null, false, file);

        assertEquals(1, result.getBlockedCount());
        assertFalse(result.getReadyToImport());
        assertEquals(1, result.getRowIssues().size());
        assertEquals("SPEC_MISMATCH", result.getRowIssues().get(0).getIssueType());
        assertEquals(Integer.valueOf(2), result.getRowIssues().get(0).getRowNumber());
        assertTrue(result.getRowIssues().get(0).getReason().contains("文件=\"33K\""));
        assertTrue(result.getRowIssues().get(0).getReason().contains("产品档案=\"47K\""));
    }

    // ========== 编码为空防错配用例 ==========

    /** 智能表头路径：明细行物料编码被清空后，绝不允许靠扫描同行其他格子救活（否则会错配成替代料/相邻物料） */
    @Test
    void import_smartPath_blankMaterialCode_mustNotBeRescuedByRowScan() throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Sheet1");
            Row bomHeader = sheet.createRow(0);
            bomHeader.createCell(0).setCellValue("物料编码");
            bomHeader.createCell(1).setCellValue("04.01.0009");
            bomHeader.createCell(2).setCellValue("版本");
            bomHeader.createCell(3).setCellValue("V1.0");
            Row detailHeader = sheet.createRow(1);
            detailHeader.createCell(0).setCellValue("物料编码");
            detailHeader.createCell(1).setCellValue("产品名称");
            detailHeader.createCell(2).setCellValue("数量");
            detailHeader.createCell(3).setCellValue("替代物料编码");
            detailHeader.createCell(4).setCellValue("层级");
            // 行 2：编码被用户删除置空，仅剩名称/数量；替代料编码 MAT-ALT(在库)与层级 1.1 同行共存
            Row deletedCodeRow = sheet.createRow(2);
            deletedCodeRow.createCell(1).setCellValue("电阻 10K");
            deletedCodeRow.createCell(2).setCellValue("5");
            deletedCodeRow.createCell(3).setCellValue("04.02.0002");
            deletedCodeRow.createCell(4).setCellValue("1.1");
            // 行 3：正常行作为对照组
            Row okRow = sheet.createRow(3);
            okRow.createCell(0).setCellValue("04.01.0001");
            okRow.createCell(1).setCellValue("电容 10uF");
            okRow.createCell(2).setCellValue("2");
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            MockMultipartFile file = new MockMultipartFile("file", "smart-bom.xlsx", null, out.toByteArray());
            when(productMapper.selectList(any())).thenReturn(List.of(
                    approvedProduct(201L, "04.01.0009"),
                    approvedProduct(202L, "04.01.0001"),
                    approvedProduct(203L, "04.02.0002")));
            when(rdBomService.validateRdBomItemsIntegrity(any())).thenReturn(List.of());
            when(rdBomService.createRdBomForImport(any())).thenReturn(55L);
            when(rdBomService.validateRdBomIntegrity(55L)).thenReturn(List.of());

            cn.weitee.erp.module.erp.controller.admin.rd.vo.bom.ErpRdBomImportResultVO result =
                    importService.importRdBom(null, null, null, null, false, file);

            assertEquals(2, result.getTotalCount(), "顶层行不计入总数之外，明细共 2 行");
            assertEquals(1, result.getSuccessCount(), "编码为空的行必须失败，只允许对照组 1 行成功");
            assertEquals(1, result.getFailCount());
            ArgumentCaptor<ErpRdBomSaveReqVO> captor = ArgumentCaptor.forClass(ErpRdBomSaveReqVO.class);
            org.mockito.Mockito.verify(rdBomService).createRdBomForImport(captor.capture());
            assertEquals(1, captor.getValue().getItems().size(),
                    "绝不允许把替代料 04.02.0002 或层级号 1.1 错配为主物料");
            assertEquals(Long.valueOf(202L), captor.getValue().getItems().get(0).getMaterialId());
        }
    }

    /** 智能表头路径：顶层行自身出现在明细中，型号被改错 → 必须拦截（顶层行不得豁免规格校验） */
    @Test
    void import_topRowSpecMismatch_shouldBeRejected() throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Sheet1");
            Row bomHeader = sheet.createRow(0);
            bomHeader.createCell(0).setCellValue("物料编码");
            bomHeader.createCell(1).setCellValue("04.01.0009");
            Row detailHeader = sheet.createRow(1);
            detailHeader.createCell(0).setCellValue("物料编码");
            detailHeader.createCell(1).setCellValue("产品名称");
            detailHeader.createCell(2).setCellValue("产品型号");
            detailHeader.createCell(3).setCellValue("数量");
            Row topRow = sheet.createRow(2);
            topRow.createCell(0).setCellValue("04.01.0009");
            topRow.createCell(1).setCellValue("顶层装配");
            topRow.createCell(2).setCellValue("WRONG-SPEC");
            topRow.createCell(3).setCellValue("1");
            Row childRow = sheet.createRow(3);
            childRow.createCell(0).setCellValue("04.02.0002");
            childRow.createCell(1).setCellValue("子件");
            childRow.createCell(2).setCellValue("SUB-SPEC");
            childRow.createCell(3).setCellValue("2");
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            MockMultipartFile file = new MockMultipartFile("file", "smart-bom.xlsx", null, out.toByteArray());

            when(productMapper.selectList(any())).thenReturn(List.of(
                    approvedProduct(201L, "04.01.0009").setStandard("TOP-SPEC"),
                    approvedProduct(203L, "04.02.0002").setStandard("SUB-SPEC")));
            when(rdBomService.validateRdBomItemsIntegrity(any())).thenReturn(List.of());
            when(rdBomService.createRdBomForImport(any())).thenReturn(81L);
            when(rdBomService.validateRdBomIntegrity(81L)).thenReturn(List.of());

            ErpRdBomImportResultVO result = importService.importRdBom(null, null, null, null, false, file);

            assertEquals(1, result.getFailCount(), "顶层行型号改错必须被规格校验拦截");
            assertEquals("SPEC_MISMATCH", result.getFailDetails().get(0).getIssueType());
            assertEquals(1, result.getSuccessCount(), "顶层行不计入明细，仅子件入库");
            ArgumentCaptor<ErpRdBomSaveReqVO> captor = ArgumentCaptor.forClass(ErpRdBomSaveReqVO.class);
            org.mockito.Mockito.verify(rdBomService).createRdBomForImport(captor.capture());
            assertEquals(Long.valueOf(203L), captor.getValue().getItems().get(0).getMaterialId(),
                    "明细里不应出现顶层自引用");
        }
    }

    /** 智能表头路径：顶层行规格与档案一致 → 正常跳过不计入明细，子件照常入库 */
    @Test
    void import_topRowSpecMatched_shouldBeSkippedFromItems() throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Sheet1");
            Row bomHeader = sheet.createRow(0);
            bomHeader.createCell(0).setCellValue("物料编码");
            bomHeader.createCell(1).setCellValue("04.01.0009");
            Row detailHeader = sheet.createRow(1);
            detailHeader.createCell(0).setCellValue("物料编码");
            detailHeader.createCell(1).setCellValue("产品名称");
            detailHeader.createCell(2).setCellValue("产品型号");
            detailHeader.createCell(3).setCellValue("数量");
            Row topRow = sheet.createRow(2);
            topRow.createCell(0).setCellValue("04.01.0009");
            topRow.createCell(1).setCellValue("顶层装配");
            topRow.createCell(2).setCellValue("TOP-SPEC");
            topRow.createCell(3).setCellValue("1");
            Row childRow = sheet.createRow(3);
            childRow.createCell(0).setCellValue("04.02.0002");
            childRow.createCell(1).setCellValue("子件");
            childRow.createCell(2).setCellValue("SUB-SPEC");
            childRow.createCell(3).setCellValue("2");
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            MockMultipartFile file = new MockMultipartFile("file", "smart-bom.xlsx", null, out.toByteArray());

            when(productMapper.selectList(any())).thenReturn(List.of(
                    approvedProduct(201L, "04.01.0009").setStandard("TOP-SPEC"),
                    approvedProduct(203L, "04.02.0002").setStandard("SUB-SPEC")));
            when(rdBomService.validateRdBomItemsIntegrity(any())).thenReturn(List.of());
            when(rdBomService.createRdBomForImport(any())).thenReturn(82L);
            when(rdBomService.validateRdBomIntegrity(82L)).thenReturn(List.of());

            ErpRdBomImportResultVO result = importService.importRdBom(null, null, null, null, false, file);

            assertEquals(2, result.getTotalCount(), "顶层行计入总行数");
            assertEquals(2, result.getSuccessCount(), "顶层行跳过仍计入可导入行数");
            assertEquals(0, result.getFailCount());
            ArgumentCaptor<ErpRdBomSaveReqVO> captor = ArgumentCaptor.forClass(ErpRdBomSaveReqVO.class);
            org.mockito.Mockito.verify(rdBomService).createRdBomForImport(captor.capture());
            assertEquals(1, captor.getValue().getItems().size(), "顶层行不落入明细，避免自引用");
        }
    }

    // ========== 测试数据构造 ==========

    private ErpProductDO approvedProduct(Long id, String materialCode) {
        return new ErpProductDO()
                .setId(id)
                .setName(materialCode + "-name")
                .setMaterialCode(materialCode)
                .setUnitId(1000L)
                .setStatus(CommonStatusEnum.ENABLE.getStatus())
                .setAuditStatus(ErpAuditStatus.APPROVE.getStatus());
    }

    private ErpProductDO productWithAudit(Long id, String materialCode, Integer auditStatus) {
        return approvedProduct(id, materialCode).setAuditStatus(auditStatus);
    }

    private ErpProductDO disabledProduct(Long id, String materialCode) {
        return approvedProduct(id, materialCode).setStatus(CommonStatusEnum.DISABLE.getStatus());
    }

    private ErpRdBomPrecheckResultVO.UnapprovedMaterial findUnapproved(ErpRdBomPrecheckResultVO result, Long materialId) {
        return result.getUnapprovedMaterials().stream()
                .filter(m -> materialId.equals(m.getMaterialId()))
                .findFirst().orElse(null);
    }

    @FunctionalInterface
    interface RowBuilder {
        void accept(TestWorkbookBuilder builder) throws Exception;
    }

    private byte[] standardWorkbook(RowBuilder rows) throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Sheet1");
            Row header = sheet.createRow(0);
            String[] headers = {"物料编号*", "物料名称", "物料类型(1=自制/装配体,0/空=采购件)", "位号", "用量*", "损耗率", "提前期(天)", "备注"};
            for (int i = 0; i < headers.length; i++) {
                header.createCell(i).setCellValue(headers[i]);
            }
            TestWorkbookBuilder builder = new TestWorkbookBuilder(sheet, 1);
            rows.accept(builder);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    /** 第 9 列为「规格型号」的标准模板变体，用于规格比对链路测试 */
    private byte[] standardWorkbookWithSpec(RowBuilder rows) throws Exception {
        return standardWorkbookWithSpecHeader("规格型号", rows);
    }

    private byte[] standardWorkbookWithSpecHeader(String specHeader, RowBuilder rows) throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Sheet1");
            Row header = sheet.createRow(0);
            String[] headers = {"物料编号*", "物料名称", "物料类型(1=自制/装配体,0/空=采购件)", "位号", "用量*", "损耗率", "提前期(天)", "备注", specHeader};
            for (int i = 0; i < headers.length; i++) {
                header.createCell(i).setCellValue(headers[i]);
            }
            TestWorkbookBuilder builder = new TestWorkbookBuilder(sheet, 1);
            rows.accept(builder);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    /**
     * 智能表头样例：第 1 行为 BOM 头（编码 + 版本），第 2 行为明细表头，第 3 行起为明细
     */
    private byte[] smartWorkbook() throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Sheet1");
            Row bomHeader = sheet.createRow(0);
            bomHeader.createCell(0).setCellValue("物料编码");
            bomHeader.createCell(1).setCellValue("PCBA-V1.2");
            bomHeader.createCell(2).setCellValue("版本");
            bomHeader.createCell(3).setCellValue("V1.2");
            Row detailHeader = sheet.createRow(1);
            detailHeader.createCell(0).setCellValue("物料编码");
            detailHeader.createCell(1).setCellValue("产品名称");
            detailHeader.createCell(2).setCellValue("数量");
            Row dataRow = sheet.createRow(2);
            dataRow.createCell(0).setCellValue("MAT-OK");
            dataRow.createCell(1).setCellValue("电阻 10K");
            dataRow.createCell(2).setCellValue("5");
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    /** 多工作表样例：第一张为封面页（产品信息 + BOM 头编码），第二张才是明细 */
    private byte[] multiSheetWorkbook() throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet cover = workbook.createSheet("封面");
            Row coverRow0 = cover.createRow(0);
            coverRow0.createCell(0).setCellValue("产品名称");
            coverRow0.createCell(1).setCellValue("无线AP电源模块组件");
            Row coverRow1 = cover.createRow(1);
            coverRow1.createCell(0).setCellValue("物料编码");
            coverRow1.createCell(1).setCellValue("04.01.0009");
            Row coverRow2 = cover.createRow(2);
            coverRow2.createCell(0).setCellValue("版本");
            coverRow2.createCell(1).setCellValue("V1.0");
            Sheet detail = workbook.createSheet("BOM明细");
            Row detailHeader = detail.createRow(0);
            detailHeader.createCell(0).setCellValue("物料编码");
            detailHeader.createCell(1).setCellValue("产品名称");
            detailHeader.createCell(2).setCellValue("数量");
            Row dataRow0 = detail.createRow(1);
            dataRow0.createCell(0).setCellValue("04.01.0001");
            dataRow0.createCell(1).setCellValue("电容 10uF");
            dataRow0.createCell(2).setCellValue("2");
            Row dataRow1 = detail.createRow(2);
            dataRow1.createCell(0).setCellValue("04.02.0002");
            dataRow1.createCell(1).setCellValue("电阻 10K");
            dataRow1.createCell(2).setCellValue("3");
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    private static class TestWorkbookBuilder {

        private final Sheet sheet;
        private int nextRow;

        private TestWorkbookBuilder(Sheet sheet, int startRow) {
            this.sheet = sheet;
            this.nextRow = startRow;
        }

        private TestWorkbookBuilder row(String... values) {
            Row row = sheet.createRow(nextRow++);
            for (int i = 0; i < values.length; i++) {
                row.createCell(i).setCellValue(values[i]);
            }
            return this;
        }
    }

}
