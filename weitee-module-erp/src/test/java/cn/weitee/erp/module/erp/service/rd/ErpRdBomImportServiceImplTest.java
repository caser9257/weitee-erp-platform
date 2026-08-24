package cn.weitee.erp.module.erp.service.rd;

import cn.weitee.erp.framework.common.enums.CommonStatusEnum;
import cn.weitee.erp.framework.excel.core.util.FileImportProtector;
import cn.weitee.erp.module.erp.controller.admin.rd.vo.bom.ErpRdBomImportResultVO;
import cn.weitee.erp.module.erp.controller.admin.rd.vo.bom.ErpRdBomPrecheckResultVO;
import cn.weitee.erp.module.erp.controller.admin.rd.vo.bom.ErpRdBomSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductDO;
import cn.weitee.erp.module.erp.dal.mysql.product.ErpProductMapper;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.service.product.ErpProductService;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

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
    private ErpRdBomService rdBomService;
    @InjectMocks
    private ErpRdBomImportServiceImpl importService;

    @BeforeEach
    void setUp() {
        // 加密保护层在测试中直通
        when(fileImportProtector.preparePlainContent(any(), anyString()))
                .thenAnswer(invocation -> invocation.getArgument(0));
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
    void import_missingMaterial_rowShouldCarryIssueType() throws Exception {
        byte[] fileBytes = standardWorkbook(builder -> builder
                .row("MAT-OK", "电阻 10K", "0", "", "5", "", "", "")
                .row("MAT-MISS", "钽电容 10uF", "0", "", "2", "", "", ""));
        MockMultipartFile file = new MockMultipartFile("file", "bom.xlsx", null, fileBytes);
        when(productMapper.selectList(any())).thenReturn(List.of(approvedProduct(101L, "MAT-OK")));
        when(productService.getProduct(10L)).thenReturn(approvedProduct(10L, "TOP-1"));
        when(rdBomService.createRdBom(any())).thenReturn(99L);
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
        when(rdBomService.createRdBom(any())).thenReturn(77L);
        when(rdBomService.validateRdBomIntegrity(77L)).thenReturn(List.of());

        ErpRdBomImportResultVO result = importService.importRdBom(10L, null, null, null, false, file);

        assertEquals(Long.valueOf(77L), result.getBomId());
        assertEquals(2, result.getSuccessCount());
        assertEquals(0, result.getFailCount());
        ArgumentCaptor<ErpRdBomSaveReqVO> captor = ArgumentCaptor.forClass(ErpRdBomSaveReqVO.class);
        org.mockito.Mockito.verify(rdBomService).createRdBom(captor.capture());
        assertEquals(Long.valueOf(10L), captor.getValue().getProductId());
        assertEquals(2, captor.getValue().getItems().size());
        assertEquals(Long.valueOf(11L), captor.getValue().getItems().get(0).getMaterialId());
        assertEquals("R101-R103", captor.getValue().getItems().get(0).getReferenceDesignator());
        assertEquals(Long.valueOf(12L), captor.getValue().getItems().get(1).getMaterialId());
    }

    // ========== 智能表头（顶层缺档）用例 ==========

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
