package cn.weitee.erp.module.erp.controller.admin.mrp;

import cn.weitee.erp.framework.common.pojo.CommonResult;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.plan.ErpMrpShortageRespVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpBomDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpBomItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpBomItemSubstituteDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpMrpShortageDO;
import cn.weitee.erp.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.weitee.erp.module.erp.service.mrp.ErpBomService;
import cn.weitee.erp.module.erp.service.mrp.ErpMrpPlanService;
import cn.weitee.erp.module.erp.service.product.ErpProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ErpMrpPlanControllerTest {

    @InjectMocks
    private ErpMrpPlanController controller;

    @Mock
    private ErpMrpPlanService mrpPlanService;
    @Mock
    private ErpBomService bomService;
    @Mock
    private ErpProductService productService;

    @Test
    void getShortageList_shouldUseMatchedBomItemSubstitutes() {
        ErpMrpShortageDO shortage = new ErpMrpShortageDO()
                .setId(1L)
                .setRootProductId(100L)
                .setMaterialId(200L)
                .setBomItemId(12L)
                .setShortageQty(new BigDecimal("5"));
        when(mrpPlanService.getShortageList(1L)).thenReturn(List.of(shortage));
        when(bomService.getEffectiveBomList(Set.of(100L))).thenReturn(List.of(
                new ErpBomDO().setId(10L).setProductId(100L)
        ));
        when(bomService.getBomItemListByBomIds(Set.of(10L))).thenReturn(List.of(
                new ErpBomItemDO().setId(11L).setBomId(10L).setMaterialId(200L),
                new ErpBomItemDO().setId(12L).setBomId(10L).setMaterialId(200L)
        ));
        when(bomService.getBomItemSubstituteList(Set.of(11L, 12L))).thenReturn(List.of(
                new ErpBomItemSubstituteDO().setId(101L).setBomItemId(11L).setSubstituteMaterialId(301L).setPriority(1),
                new ErpBomItemSubstituteDO().setId(102L).setBomItemId(12L).setSubstituteMaterialId(302L).setPriority(1)
        ));
        when(productService.getProductVOMap(anyCollection())).thenReturn(Map.of(
                100L, new ErpProductRespVO().setId(100L).setName("母项A"),
                200L, new ErpProductRespVO().setId(200L).setName("缺料件B"),
                301L, new ErpProductRespVO().setId(301L).setName("分支A替代"),
                302L, new ErpProductRespVO().setId(302L).setName("分支B替代")
        ));

        CommonResult<List<ErpMrpShortageRespVO>> result = controller.getShortageList(1L);

        assertNotNull(result.getData());
        assertEquals(1, result.getData().size());
        ErpMrpShortageRespVO row = result.getData().get(0);
        assertEquals(12L, row.getBomItemId());
        assertEquals(1, row.getSubstitutes().size());
        assertEquals(302L, row.getSubstitutes().get(0).getSubstituteMaterialId());
        assertEquals("分支B替代", row.getSubstitutes().get(0).getSubstituteMaterialName());
    }

    @Test
    void getShortageList_shouldPropagateUnexpectedFailure() {
        when(mrpPlanService.getShortageList(eq(1L))).thenThrow(new IllegalStateException("boom"));

        assertThrows(IllegalStateException.class, () -> controller.getShortageList(1L));
    }
}
