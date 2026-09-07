package cn.weitee.erp.module.erp.service.product;

import cn.weitee.erp.framework.common.exception.ServiceException;
import cn.weitee.erp.module.erp.controller.admin.product.vo.product.ProductSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductDO;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductPendingChangeDO;
import cn.weitee.erp.module.erp.dal.dataobject.rd.ErpRdBomItemDO;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpBomItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.product.ErpProductMapper;
import cn.weitee.erp.module.erp.dal.mysql.product.ErpProductCadenceMapper;
import cn.weitee.erp.module.erp.dal.mysql.product.ErpProductPendingChangeMapper;
import cn.weitee.erp.module.erp.dal.mysql.rd.ErpRdBomItemMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 物料待审变更暂存服务测试：
 * diff 计算（含 BigDecimal 精度）、冻结校验、审批回调幂等。
 */
@ExtendWith(MockitoExtension.class)
class ErpProductPendingChangeServiceImplTest {

    @Mock
    private ErpProductPendingChangeMapper pendingChangeMapper;
    @Mock
    private ErpRdBomItemMapper rdBomItemMapper;
    @Mock
    private ErpBomItemMapper bomItemMapper;
    @Mock
    private ErpProductMapper productMapper;
    @Mock
    private ErpProductCadenceMapper productCadenceMapper;
    @Mock
    private cn.weitee.erp.module.erp.dal.mysql.product.ErpProductCodeHistoryMapper codeHistoryMapper;
    @InjectMocks
    private ErpProductPendingChangeServiceImpl service;

    @Test
    void diff_mixedChanges_shouldDetectOnlyRealChanges() {
        ErpProductDO existed = baseProduct();
        existed.setPurchasePrice(new BigDecimal("10.30"));
        existed.setName("旧名称");
        ProductSaveReqVO reqVO = baseReqVO();
        // 10.300 与 10.30 数值相等，不应误报变更
        reqVO.setPurchasePrice(new BigDecimal("10.300"));
        reqVO.setName("新名称");

        Map<String, Object> changes = service.diffProduct(existed, reqVO);

        assertEquals(1, changes.size());
        assertEquals("新名称", changes.get("name"));
    }

    @Test
    void diff_noChangeIncludingNullScaleDifference_shouldReturnEmpty() {
        ErpProductDO existed = baseProduct();
        ProductSaveReqVO reqVO = baseReqVO();

        Map<String, Object> changes = service.diffProduct(existed, reqVO);

        assertTrue(changes.isEmpty(), "完全一致的字段集合应返回空 diff");
    }

    @Test
    void diff_absentFieldsAsNull_shouldBeSkippedNotCountedAsChange() {
        // 与落库语义对齐：请求中未提供的字段（null）不构成变更，
        // 即使主表对应字段有值——否则冻结校验会误拦未触碰字段
        ErpProductDO existed = baseProduct();
        existed.setStandard("1.2mm");
        existed.setWeight(new BigDecimal("50"));
        ProductSaveReqVO reqVO = new ProductSaveReqVO();
        reqVO.setId(1L);
        reqVO.setName("物料A");       // 与主表一致
        reqVO.setBarCode("BC-001");   // 与主表一致
        reqVO.setCategoryId(100L);
        reqVO.setUnitId(200L);
        reqVO.setStatus(1);
        // standard / weight 未提供（null）

        Map<String, Object> changes = service.diffProduct(existed, reqVO);

        assertTrue(changes.isEmpty(), "未提供的 null 字段不应被计为变更");
    }

    @Test
    void diff_pcbComponentChanged_shouldRecordChange() {
        ErpProductDO existed = baseProduct().setPcbComponent(false);
        ProductSaveReqVO reqVO = baseReqVO();
        reqVO.setPcbComponent(true);

        Map<String, Object> changes = service.diffProduct(existed, reqVO);

        assertEquals(Map.of("pcbComponent", true), changes);
    }

    @Test
    void diff_threeDLibChanged_shouldRecordCadenceChange() {
        ErpProductDO existed = baseProduct().setThreeDLib("旧3D模型");
        ProductSaveReqVO reqVO = baseReqVO();
        reqVO.setThreeDLib("新3D模型");

        Map<String, Object> changes = service.diffProduct(existed, reqVO);

        assertEquals(Map.of("cadenceThreeDLib", "新3D模型"), changes);
    }

    @Test
    void frozenFields_whenReferencedByRdBom_shouldThrow() {
        // materialCode 已放开（编码沿革可追溯），standard 仍冻结
        when(rdBomItemMapper.selectList(any())).thenReturn(java.util.List.of(new ErpRdBomItemDO()));

        assertThrows(ServiceException.class, () -> service.validateFrozenFields(1L,
                java.util.Set.of("standard", "name")));
    }

    @Test
    void frozenFields_materialCodeNoLongerFrozen_shouldPassEvenWhenReferenced() {
        // 编码沿革机制上线：被 BOM 引用的物料改 materialCode 不再被冻结拦截，也不触发引用查询
        service.validateFrozenFields(1L, java.util.Set.of("materialCode", "name"));

        verify(rdBomItemMapper, never()).selectList(any());
        verify(bomItemMapper, never()).selectList(any());
    }

    @Test
    void frozenFields_whenReferencedButNoFrozenFieldTouched_shouldPass() {
        // 变更字段不命中冻结字段时直接放行，不应触发任何 BOM 引用查询
        service.validateFrozenFields(1L, java.util.Set.of("name", "purchasePrice"));

        verify(rdBomItemMapper, never()).selectCount(any());
        verify(bomItemMapper, never()).selectCount(any());
    }

    @Test
    void frozenFields_whenNotReferenced_shouldPass() {
        when(rdBomItemMapper.selectList(any())).thenReturn(java.util.List.of());
        when(bomItemMapper.selectList(any())).thenReturn(java.util.List.of());

        service.validateFrozenFields(1L, java.util.Set.of("standard"));
    }

    @Test
    void applyPendingChange_whenAlreadyApproved_shouldSkipIdempotently() {
        ErpProductPendingChangeDO pending = ErpProductPendingChangeDO.builder()
                .id(9L).productId(1L).status(ErpProductPendingChangeDO.STATUS_APPROVED)
                .changedFields("name").build();
        when(pendingChangeMapper.selectByProductId(1L)).thenReturn(pending);

        boolean applied = service.applyPendingChange(1L, "PI-1", "ok");

        assertFalse(applied);
        verify(productMapper, never()).updateById(any(ErpProductDO.class));
    }

    @Test
    void applyPendingChange_happyPath_shouldApplyThenFinalize() {
        Map<String, Object> changeData = Map.of(
                "name", "新名称",
                "purchasePrice", new BigDecimal("12.50"),
                "auditStatus", 99);   // 恶意/脏数据：审核状态字段不允许被快照携带
        ErpProductPendingChangeDO pending = ErpProductPendingChangeDO.builder()
                .id(9L).productId(1L).status(ErpProductPendingChangeDO.STATUS_PENDING)
                .changeData(changeData).changedFields("name,purchasePrice").build();
        when(pendingChangeMapper.selectByProductId(1L)).thenReturn(pending);
        when(productMapper.updateById(any(ErpProductDO.class))).thenReturn(1);

        boolean applied = service.applyPendingChange(1L, "PI-2", "通过");

        assertTrue(applied);
        ArgumentCaptor<ErpProductDO> captor = ArgumentCaptor.forClass(ErpProductDO.class);
        verify(productMapper).updateById(captor.capture());
        // 审核字段必须为 null（updateById NOT_NULL 策略下不会覆盖主表审批字段）
        assertNull(captor.getValue().getAuditStatus());
        assertEquals("PI-2", captor.getValue().getProcessInstanceId());
        verify(pendingChangeMapper).updateById(any(ErpProductPendingChangeDO.class));
    }

    @Test
    void rejectPendingChange_whenAlreadyRejected_shouldSkipIdempotently() {
        ErpProductPendingChangeDO pending = ErpProductPendingChangeDO.builder()
                .id(9L).productId(1L).status(ErpProductPendingChangeDO.STATUS_REJECTED)
                .build();
        when(pendingChangeMapper.selectByProductId(1L)).thenReturn(pending);

        boolean rejected = service.rejectPendingChange(1L, "PI-3", "驳回");

        assertFalse(rejected);
        verify(pendingChangeMapper, never()).updateById(any(ErpProductPendingChangeDO.class));
    }

    @Test
    void applyPendingChange_codeChanged_shouldSetPrevCodeAndWriteHistory() {
        Map<String, Object> changeData = Map.of("materialCode", "MAT-NEW");
        ErpProductPendingChangeDO pending = ErpProductPendingChangeDO.builder()
                .id(9L).productId(1L).status(ErpProductPendingChangeDO.STATUS_PENDING)
                .changeData(changeData).changedFields("materialCode").build();
        when(pendingChangeMapper.selectByProductId(1L)).thenReturn(pending);
        when(productMapper.selectById(1L)).thenReturn(baseProduct()); // 当前码 MAT-001
        when(productMapper.updateById(any(ErpProductDO.class))).thenReturn(1);
        when(codeHistoryMapper.existsByOldCode("MAT-NEW")).thenReturn(false);

        boolean applied = service.applyPendingChange(1L, "PI-4", "改码");

        assertTrue(applied);
        ArgumentCaptor<ErpProductDO> captor = ArgumentCaptor.forClass(ErpProductDO.class);
        verify(productMapper).updateById(captor.capture());
        assertEquals("MAT-001", captor.getValue().getPrevMaterialCode());
        verify(codeHistoryMapper).existsByOldCode("MAT-NEW");
        ArgumentCaptor<cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductCodeHistoryDO> historyCaptor =
                ArgumentCaptor.forClass(cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductCodeHistoryDO.class);
        verify(codeHistoryMapper).insert(historyCaptor.capture());
        assertEquals(1L, historyCaptor.getValue().getProductId());
        assertEquals("MAT-001", historyCaptor.getValue().getOldCode());
        assertEquals("MAT-NEW", historyCaptor.getValue().getNewCode());
        assertEquals("PI-4", historyCaptor.getValue().getProcessInstanceId());
    }

    @Test
    void recordMaterialCodeChange_whenCodeOccupiedByHistory_shouldThrow() {
        when(codeHistoryMapper.existsByOldCode("MAT-OLD-OTHER")).thenReturn(true);

        assertThrows(ServiceException.class, () -> service.recordMaterialCodeChange(
                1L, "MAT-001", "MAT-OLD-OTHER", null, null));
        verify(codeHistoryMapper, never()).insert(any(
                cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductCodeHistoryDO.class));
    }

    @Test
    void recordMaterialCodeChange_whenSameCodeOrBlank_shouldSkip() {
        service.recordMaterialCodeChange(1L, "MAT-001", "MAT-001", null, null);
        service.recordMaterialCodeChange(1L, "MAT-001", null, null, null);

        verify(codeHistoryMapper, never()).existsByOldCode(any());
        verify(codeHistoryMapper, never()).insert(any(
                cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductCodeHistoryDO.class));
    }

    private static void assertNull(Object value) {
        org.junit.jupiter.api.Assertions.assertNull(value);
    }

    private static ErpProductDO baseProduct() {
        return new ErpProductDO()
                .setId(1L)
                .setName("物料A")
                .setMaterialCode("MAT-001")
                .setBarCode("BC-001")
                .setCategoryId(100L)
                .setUnitId(200L)
                .setStatus(1)
                .setStandard("1.2mm")
                .setRemark(null)
                .setWeight(null);
    }

    private static ProductSaveReqVO baseReqVO() {
        ProductSaveReqVO vo = new ProductSaveReqVO();
        vo.setId(1L);
        vo.setName("物料A");
        vo.setMaterialCode("MAT-001");
        vo.setBarCode("BC-001");
        vo.setCategoryId(100L);
        vo.setUnitId(200L);
        vo.setStatus(1);
        vo.setStandard("1.2mm");
        vo.setRemark(null);
        return vo;
    }

}
