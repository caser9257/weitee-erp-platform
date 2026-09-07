package cn.weitee.erp.module.erp.service.product;

import cn.weitee.erp.framework.common.enums.CommonStatusEnum;
import cn.weitee.erp.framework.common.exception.ServiceException;
import cn.weitee.erp.framework.excel.core.util.FileImportProtector;
import cn.weitee.erp.module.erp.controller.admin.product.vo.product.ErpProductImportResultVO;
import cn.weitee.erp.module.erp.controller.admin.product.vo.product.ErpRdCadencePageReqVO;
import cn.weitee.erp.module.erp.controller.admin.product.vo.product.ProductSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductCadenceDO;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductDO;
import cn.weitee.erp.module.erp.dal.mysql.product.ErpProductCadenceMapper;
import cn.weitee.erp.module.erp.dal.mysql.product.ErpProductMapper;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.system.service.notify.ImportNotifyHelper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ErpProductRdCadenceServiceImplTest {

    private static final long RD_DEPT_ID = 103L;
    private static final long OTHER_DEPT_ID = 999L;

    @Mock
    private ErpProductMapper erpProductMapper;
    @Mock
    private ErpProductCadenceMapper productCadenceMapper;
    @Mock
    private ErpProductService productService;
    @Mock
    private ErpProductPendingChangeService pendingChangeService;
    @Mock
    private ErpProductBatchUpdateBpmService productBatchUpdateBpmService;
    @Mock
    private FileImportProtector fileImportProtector;
    @Mock
    private ImportNotifyHelper importNotifyHelper;

    @InjectMocks
    private ErpProductRdCadenceServiceImpl rdCadenceService;

    // ====================== 部门闸门 ======================

    @Test
    void getRdCadencePage_whenNotRdDept_shouldThrow() {
        ServiceException ex = assertThrows(ServiceException.class,
                () -> rdCadenceService.getRdCadencePage(new ErpRdCadencePageReqVO(), OTHER_DEPT_ID));
        assertThat(ex.getMessage()).isEqualTo("无权限");
        verify(erpProductMapper, never()).selectPage(any(ErpRdCadencePageReqVO.class), any());
    }

    @Test
    void precheckImport_whenNotRdDept_shouldThrow() {
        MultipartFile file = buildXlsx(new String[][]{
                {"Part_Number", "Schematic_Part", "PCB_Footprint"}, {"MAT-1", "R1", "F1"}});
        ServiceException ex = assertThrows(ServiceException.class,
                () -> rdCadenceService.precheckImport(file, OTHER_DEPT_ID, false));
        assertThat(ex.getMessage()).isEqualTo("无权限");
    }

    @Test
    void importCadence_whenNotRdDept_shouldThrow() {
        MultipartFile file = buildXlsx(new String[][]{
                {"Part_Number", "Schematic_Part", "PCB_Footprint"}, {"MAT-1", "R1", "F1"}});
        ServiceException ex = assertThrows(ServiceException.class,
                () -> rdCadenceService.importCadence(file, 1L, OTHER_DEPT_ID, false));
        assertThat(ex.getMessage()).isEqualTo("无权限");
    }

    // ====================== 导入预检查：逐行校验 ======================

    @Test
    void precheckImport_validRow_shouldSucceed() {
        MultipartFile file = buildXlsx(new String[][]{
                {"Part_Number", "Schematic_Part", "PCB_Footprint"}, {"MAT-1", "R1", "F1"}});
        stubProtector(file);
        when(erpProductMapper.selectOne(any(SFunction.class), any())).thenReturn(approvedPcbProduct(1L, "MAT-1"));
        when(productCadenceMapper.selectByProductId(1L)).thenReturn(null);
        stubDiffNonEmpty();

        ErpProductImportResultVO result = rdCadenceService.precheckImport(file, RD_DEPT_ID, false);

        assertThat(result.getTotalCount()).isEqualTo(1);
        assertThat(result.getSuccessCount()).isEqualTo(1);
        assertThat(result.getFailCount()).isEqualTo(0);
        verify(productBatchUpdateBpmService, never()).submitBatchUpdate(any(), any(), any());
    }

    @Test
    void precheckImport_threeDLib_shouldMapToCadenceRequest() {
        MultipartFile file = buildXlsx(new String[][]{
                {"Part_Number", "Schematic_Part", "PCB_Footprint", "3D_Lib"},
                {"MAT-1", "R1", "F1", "R0603_3D"}});
        stubProtector(file);
        when(erpProductMapper.selectOne(any(SFunction.class), any())).thenReturn(approvedPcbProduct(1L, "MAT-1"));
        when(productCadenceMapper.selectByProductId(1L)).thenReturn(null);
        stubDiffNonEmpty();

        ErpProductImportResultVO result = rdCadenceService.precheckImport(file, RD_DEPT_ID, false);

        ArgumentCaptor<ProductSaveReqVO> captor = ArgumentCaptor.forClass(ProductSaveReqVO.class);
        verify(pendingChangeService).diffProduct(any(ErpProductDO.class), captor.capture());
        assertThat(result.getSuccessCount()).isEqualTo(1);
        assertThat(captor.getValue().getThreeDLib()).isEqualTo("R0603_3D");
    }

    @Test
    void precheckImport_missingMaterial_shouldFail() {
        MultipartFile file = buildXlsx(new String[][]{
                {"Part_Number", "Schematic_Part", "PCB_Footprint"}, {"MAT-404", "R1", "F1"}});
        stubProtector(file);
        when(erpProductMapper.selectOne(any(SFunction.class), any())).thenReturn(null);

        ErpProductImportResultVO result = rdCadenceService.precheckImport(file, RD_DEPT_ID, false);

        assertThat(result.getFailCount()).isEqualTo(1);
        assertThat(result.getFailDetails().get(0).getReason()).contains("物料不存在");
    }

    @Test
    void precheckImport_nonPcbComponentWithoutFlag_shouldFailWithGuidance() {
        MultipartFile file = buildXlsx(new String[][]{
                {"Part_Number", "Schematic_Part", "PCB_Footprint"}, {"MAT-1", "R1", "F1"}});
        stubProtector(file);
        ErpProductDO product = approvedPcbProduct(1L, "MAT-1");
        product.setPcbComponent(false);
        when(erpProductMapper.selectOne(any(SFunction.class), any())).thenReturn(product);

        ErpProductImportResultVO result = rdCadenceService.precheckImport(file, RD_DEPT_ID, false);

        assertThat(result.getFailCount()).isEqualTo(1);
        assertThat(result.getFailDetails().get(0).getReason()).contains("is_pcb_component=0");
        assertThat(result.getFailDetails().get(0).getReason()).contains("PCB_Component");
    }

    @Test
    void precheckImport_nonPcbComponentWithYesFlag_shouldSucceed() {
        MultipartFile file = buildXlsx(new String[][]{
                {"Part_Number", "PCB_Component", "Schematic_Part", "PCB_Footprint"},
                {"MAT-1", "Yes", "R1", "F1"}});
        stubProtector(file);
        ErpProductDO product = approvedPcbProduct(1L, "MAT-1");
        product.setPcbComponent(false);
        when(erpProductMapper.selectOne(any(SFunction.class), any())).thenReturn(product);
        stubDiffNonEmpty();

        ErpProductImportResultVO result = rdCadenceService.precheckImport(file, RD_DEPT_ID, false);

        assertThat(result.getSuccessCount()).isEqualTo(1);
        assertThat(result.getFailCount()).isEqualTo(0);
    }

    @Test
    void precheckImport_markAllAsPcbTrue_shouldSucceedWithoutPcbColumnInFile() {
        // Excel 不含 PCB_Component 列，但 markAllAsPcb=true 时所有行视为已标记
        MultipartFile file = buildXlsx(new String[][]{
                {"Part_Number", "Schematic_Part", "PCB_Footprint"}, {"MAT-1", "R1", "F1"}});
        stubProtector(file);
        ErpProductDO product = approvedPcbProduct(1L, "MAT-1");
        product.setPcbComponent(false);
        when(erpProductMapper.selectOne(any(SFunction.class), any())).thenReturn(product);
        stubDiffNonEmpty();

        ErpProductImportResultVO result = rdCadenceService.precheckImport(file, RD_DEPT_ID, true);

        assertThat(result.getSuccessCount()).isEqualTo(1);
        assertThat(result.getFailCount()).isEqualTo(0);
    }

    @Test
    void precheckImport_notApproved_shouldFail() {
        MultipartFile file = buildXlsx(new String[][]{
                {"Part_Number", "Schematic_Part", "PCB_Footprint"}, {"MAT-1", "R1", "F1"}});
        stubProtector(file);
        ErpProductDO product = approvedPcbProduct(1L, "MAT-1");
        product.setAuditStatus(ErpAuditStatus.DRAFT.getStatus());
        when(erpProductMapper.selectOne(any(SFunction.class), any())).thenReturn(product);

        ErpProductImportResultVO result = rdCadenceService.precheckImport(file, RD_DEPT_ID, false);

        assertThat(result.getFailCount()).isEqualTo(1);
        assertThat(result.getFailDetails().get(0).getReason()).contains("未审核通过");
    }

    @Test
    void precheckImport_duplicatePartNumber_shouldFailSecondRow() {
        MultipartFile file = buildXlsx(new String[][]{
                {"Part_Number", "Schematic_Part", "PCB_Footprint"},
                {"MAT-1", "R1", "F1"},
                {"MAT-1", "R2", "F2"}});
        stubProtector(file);
        when(erpProductMapper.selectOne(any(SFunction.class), any())).thenReturn(approvedPcbProduct(1L, "MAT-1"));

        ErpProductImportResultVO result = rdCadenceService.precheckImport(file, RD_DEPT_ID, false);

        assertThat(result.getTotalCount()).isEqualTo(2);
        assertThat(result.getSuccessCount()).isEqualTo(1);
        assertThat(result.getFailCount()).isEqualTo(1);
        assertThat(result.getFailDetails().get(0).getReason()).contains("重复");
    }

    @Test
    void precheckImport_missingFootprint_shouldSucceedWithPartialData() {
        // PCB_Footprint 留空不再阻断导入：部分数据照常入库，视图按“两字段齐全”过滤可见性
        MultipartFile file = buildXlsx(new String[][]{
                {"Part_Number", "Schematic_Part", "PCB_Footprint"}, {"MAT-1", "R1", ""}});
        stubProtector(file);
        when(erpProductMapper.selectOne(any(SFunction.class), any())).thenReturn(approvedPcbProduct(1L, "MAT-1"));
        stubDiffNonEmpty();

        ErpProductImportResultVO result = rdCadenceService.precheckImport(file, RD_DEPT_ID, false);

        assertThat(result.getSuccessCount()).isEqualTo(1);
        assertThat(result.getFailCount()).isEqualTo(0);
    }

    // ====================== 导入执行：整批路由到批量修改审批 ======================

    @Test
    void importCadence_validRow_shouldRouteToBatchUpdate() {
        MultipartFile file = buildXlsx(new String[][]{
                {"Part_Number", "Schematic_Part", "PCB_Footprint"}, {"MAT-1", "R1", "F1"}});
        stubProtector(file);
        when(erpProductMapper.selectOne(any(SFunction.class), any())).thenReturn(approvedPcbProduct(1L, "MAT-1"));
        when(productCadenceMapper.selectByProductId(1L)).thenReturn(null);
        stubDiffNonEmpty();
        when(productBatchUpdateBpmService.submitBatchUpdate(any(), any(), any()))
                .thenReturn(ProductBatchSubmitResult.ok(999L));

        ErpProductImportResultVO result = rdCadenceService.importCadence(file, 1L, RD_DEPT_ID, false);

        assertThat(result.getSuccessCount()).isEqualTo(1);
        assertThat(result.getFailCount()).isEqualTo(0);
        // 一次导入合并为一个批次审批：通过整批生效、驳回整批作废
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ProductBatchUpdateItem>> captor = ArgumentCaptor.forClass(List.class);
        verify(productBatchUpdateBpmService, times(1))
                .submitBatchUpdate(eq(1L), captor.capture(), any());
        assertThat(captor.getValue()).hasSize(1);
        assertThat(captor.getValue().get(0).productId()).isEqualTo(1L);
    }

    @Test
    void importCadence_nonPcbWithYesFlag_shouldRouteToBatchWithPcbTrue() {
        MultipartFile file = buildXlsx(new String[][]{
                {"Part_Number", "PCB_Component", "Schematic_Part", "PCB_Footprint"},
                {"MAT-1", "Yes", "R1", "F1"}});
        stubProtector(file);
        ErpProductDO product = approvedPcbProduct(1L, "MAT-1");
        product.setPcbComponent(false);
        when(erpProductMapper.selectOne(any(SFunction.class), any())).thenReturn(product);
        when(productCadenceMapper.selectByProductId(1L)).thenReturn(null);
        stubDiffNonEmpty();
        when(productBatchUpdateBpmService.submitBatchUpdate(any(), any(), any()))
                .thenReturn(ProductBatchSubmitResult.ok(999L));

        ErpProductImportResultVO result = rdCadenceService.importCadence(file, 1L, RD_DEPT_ID, false);

        assertThat(result.getSuccessCount()).isEqualTo(1);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ProductBatchUpdateItem>> captor = ArgumentCaptor.forClass(List.class);
        verify(productBatchUpdateBpmService, times(1))
                .submitBatchUpdate(eq(1L), captor.capture(), any());
        // PCB 标记随批量修改审批一并生效，不直接覆盖主数据
        assertThat(captor.getValue().get(0).target().getPcbComponent()).isTrue();
    }

    @Test
    void importCadence_bpmCreateFailed_shouldSkipSuccessNotify() {
        // BPM 创建失败时失败通知已由 Service 发出，导入侧不得再发成功统计通知（避免误导）
        MultipartFile file = buildXlsx(new String[][]{
                {"Part_Number", "Schematic_Part", "PCB_Footprint"}, {"MAT-1", "R1", "F1"}});
        stubProtector(file);
        when(erpProductMapper.selectOne(any(SFunction.class), any())).thenReturn(approvedPcbProduct(1L, "MAT-1"));
        when(productCadenceMapper.selectByProductId(1L)).thenReturn(null);
        stubDiffNonEmpty();
        when(productBatchUpdateBpmService.submitBatchUpdate(any(), any(), any()))
                .thenReturn(ProductBatchSubmitResult.failed(999L, "流程创建失败"));

        ErpProductImportResultVO result = rdCadenceService.importCadence(file, 1L, RD_DEPT_ID, false);

        assertThat(result.getSuccessCount()).isEqualTo(1);
        verify(importNotifyHelper, never()).sendImportResult(any(), any(), anyInt(), anyInt(), anyInt(), any());
    }

    @Test
    void precheckImport_blankGhostRows_shouldBeExcluded() {
        // Cadence 导出文件常带数万"幽灵空行"（有格式无数据）：必须剔除统计，不得报 Part_Number 不能为空
        MultipartFile file = buildXlsx(new String[][]{
                {"Part_Number", "Schematic_Part", "PCB_Footprint"},
                {"MAT-1", "R1", "F1"},
                {"", "", ""},
                {"", "", ""}});
        stubProtector(file);
        when(erpProductMapper.selectOne(any(SFunction.class), any())).thenReturn(approvedPcbProduct(1L, "MAT-1"));
        when(productCadenceMapper.selectByProductId(1L)).thenReturn(null);
        stubDiffNonEmpty();

        ErpProductImportResultVO result = rdCadenceService.precheckImport(file, RD_DEPT_ID, false);

        assertThat(result.getTotalCount()).isEqualTo(1);
        assertThat(result.getSuccessCount()).isEqualTo(1);
        assertThat(result.getFailCount()).isEqualTo(0);
        assertThat(result.getFailDetails()).isEmpty();
    }

    // ====================== 工具方法 ======================

    private void stubDiffNonEmpty() {
        when(pendingChangeService.diffProduct(any(ErpProductDO.class), any(ProductSaveReqVO.class)))
                .thenReturn(java.util.Map.of("cadenceSchematicPart", "R1"));
    }

    private void stubProtector(MultipartFile file) {
        when(fileImportProtector.preparePlainContent(any(), any()))
                .thenAnswer(inv -> inv.getArgument(0));
    }

    private ErpProductDO approvedPcbProduct(Long id, String materialCode) {
        return new ErpProductDO()
                .setId(id)
                .setName("测试电阻")
                .setMaterialCode(materialCode)
                .setBarCode("BC-" + materialCode)
                .setCategoryId(1L)
                .setUnitId(1L)
                .setStatus(CommonStatusEnum.ENABLE.getStatus())
                .setAuditStatus(ErpAuditStatus.APPROVE.getStatus())
                .setPcbComponent(true);
    }

    private MultipartFile buildXlsx(String[][] rows) {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            org.apache.poi.ss.usermodel.Sheet sheet = wb.createSheet("Cadence 导入模板");
            for (int r = 0; r < rows.length; r++) {
                Row row = sheet.createRow(r);
                for (int c = 0; c < rows[r].length; c++) {
                    row.createCell(c).setCellValue(rows[r][c]);
                }
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return new MockMultipartFile("file", "cadence.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", out.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
