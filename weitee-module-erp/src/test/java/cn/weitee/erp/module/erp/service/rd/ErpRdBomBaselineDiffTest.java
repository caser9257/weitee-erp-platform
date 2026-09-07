package cn.weitee.erp.module.erp.service.rd;

import cn.weitee.erp.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.weitee.erp.module.erp.controller.admin.rd.vo.bom.ErpRdBomBaselineDiffVO;
import cn.weitee.erp.module.erp.dal.dataobject.rd.ErpRdBomDO;
import cn.weitee.erp.module.erp.dal.dataobject.rd.ErpRdBomItemDO;
import cn.weitee.erp.module.erp.dal.mysql.rd.ErpRdBomItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.rd.ErpRdBomMapper;
import cn.weitee.erp.module.erp.enums.rd.ErpRdBomStatusEnum;
import cn.weitee.erp.module.erp.service.product.ErpProductService;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

/**
 * 导入增量差异警示（diffImportAgainstLatest）单测：
 * 基准明细按物料去重、编码/名称回填、全覆盖不报、无基准返回 null
 */
@ExtendWith(MockitoExtension.class)
class ErpRdBomBaselineDiffTest {

    @Mock
    private ErpRdBomMapper rdBomMapper;
    @Mock
    private ErpRdBomItemMapper rdBomItemMapper;
    @Mock
    private ErpProductService productService;

    private ErpRdBomServiceImpl service;

    @BeforeEach
    void setUp() throws Exception {
        service = new ErpRdBomServiceImpl();
        setField("erpRdBomMapper", rdBomMapper);
        setField("erpRdBomItemMapper", rdBomItemMapper);
        setField("productService", productService);
    }

    @Test
    void missingItems_shouldBeDedupedAndFilledWithCodeAndName() {
        stubLatest(baseline(500L, "V2.0"));
        doReturn(List.of(
                item(501L, 11L),
                item(501L, 11L),
                item(502L, 12L))).when(rdBomItemMapper).selectListByBomId(500L);
        doReturn(productMap(productVO(11L, "MAT-A", "电阻 A"), productVO(12L, "MAT-B", "电容 B")))
                .when(productService).getProductVOMap(anyCollection());

        ErpRdBomBaselineDiffVO diff = service.diffImportAgainstLatest(10L, List.of(12L));

        assertEquals(Long.valueOf(500L), diff.getBaselineBomId());
        assertEquals("V2.0", diff.getBaselineVersion());
        assertEquals(1, diff.getMissingItems().size(), "物料 11 在基准中占两行，只报一条");
        assertEquals("MAT-A", diff.getMissingItems().get(0).getMaterialCode());
        assertEquals("电阻 A", diff.getMissingItems().get(0).getProductName());
    }

    @Test
    void fullyCoveredImport_shouldReturnEmptyMissingList() {
        stubLatest(baseline(500L, null));
        doReturn(List.of(item(501L, 11L))).when(rdBomItemMapper).selectListByBomId(500L);

        ErpRdBomBaselineDiffVO diff = service.diffImportAgainstLatest(10L, List.of(11L, 99L));

        assertEquals(0, diff.getMissingItems().size());
        assertNull(diff.getBaselineVersion());
    }

    @Test
    void noBaselineOrNoProduct_shouldReturnNull() {
        doReturn(List.of()).when(rdBomMapper)
                .selectList(any(SFunction.class), eq(10L));

        assertNull(service.diffImportAgainstLatest(10L, List.of(11L)), "该成品无非作废 BOM 时视为首次导入");
        assertNull(service.diffImportAgainstLatest(null, List.of(11L)), "无法确定成品时不对比");
    }

    // ========== 桩与工具 ==========

    private void stubLatest(ErpRdBomDO baseline) {
        doReturn(List.of(baseline)).when(rdBomMapper)
                .selectList(any(SFunction.class), eq(10L));
    }

    private void setField(String name, Object value) throws Exception {
        Field field = ErpRdBomServiceImpl.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(service, value);
    }

    private ErpRdBomDO baseline(Long id, String version) {
        return new ErpRdBomDO()
                .setId(id)
                .setProductId(10L)
                .setVersion(version)
                .setStatus(ErpRdBomStatusEnum.APPROVE.getStatus());
    }

    private ErpRdBomItemDO item(Long id, Long materialId) {
        return ErpRdBomItemDO.builder().id(id).bomId(500L).materialId(materialId).build();
    }

    private Map<Long, ErpProductRespVO> productMap(ErpProductRespVO... vos) {
        Map<Long, ErpProductRespVO> map = new HashMap<>();
        for (ErpProductRespVO vo : vos) {
            map.put(vo.getId(), vo);
        }
        return map;
    }

    private ErpProductRespVO productVO(Long id, String code, String name) {
        ErpProductRespVO vo = new ErpProductRespVO();
        vo.setId(id);
        vo.setMaterialCode(code);
        vo.setName(name);
        return vo;
    }

}
