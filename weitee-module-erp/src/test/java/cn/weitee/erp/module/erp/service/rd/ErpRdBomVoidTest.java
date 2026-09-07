package cn.weitee.erp.module.erp.service.rd;

import cn.weitee.erp.framework.common.exception.ServiceException;
import cn.weitee.erp.framework.test.core.ut.BaseMockitoUnitTest;
import cn.weitee.erp.module.erp.dal.dataobject.rd.ErpRdBomDO;
import cn.weitee.erp.module.erp.dal.mysql.rd.ErpRdBomItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.rd.ErpRdBomItemSubstituteMapper;
import cn.weitee.erp.module.erp.dal.mysql.rd.ErpRdBomMapper;
import cn.weitee.erp.module.erp.enums.rd.ErpRdBomStatusEnum;
import cn.weitee.erp.module.erp.service.product.ErpProductService;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 研发 BOM 作废状态机测试：APPROVE→VOID 终态流转、CAS 并发防护、各动作对 VOID 的封锁
 */
class ErpRdBomVoidTest extends BaseMockitoUnitTest {

    @Mock
    private ErpRdBomMapper erpRdBomMapper;
    @Mock
    private ErpRdBomItemMapper erpRdBomItemMapper;
    @Mock
    private ErpRdBomItemSubstituteMapper erpRdBomItemSubstituteMapper;
    @Mock
    private ErpProductService productService;
    @Mock
    private ErpRdBomChangeLogService changeLogService;

    @InjectMocks
    private ErpRdBomServiceImpl service;

    @Test
    void void_approvedBom_shouldSucceedWithLog() {
        when(erpRdBomMapper.selectById(1L)).thenReturn(new ErpRdBomDO()
                .setId(1L).setStatus(ErpRdBomStatusEnum.APPROVE.getStatus()));
        when(erpRdBomMapper.update(any(), any())).thenReturn(1);

        service.voidRdBom(1L, "设计废弃");

        ArgumentCaptor<ErpRdBomDO> captor = ArgumentCaptor.forClass(ErpRdBomDO.class);
        verify(erpRdBomMapper).update(captor.capture(), any());
        assertEquals(ErpRdBomStatusEnum.VOID.getStatus(), captor.getValue().getStatus());
        verify(changeLogService).logChange(eq(1L), eq("VOID"), eq("作废研发 BOM：设计废弃"));
    }

    @Test
    void void_casMiss_shouldThrowNotApproved() {
        // 并发场景：读取时仍是 APPROVE，但 CAS 更新时已被变更/发布占用 → 更新 0 行
        when(erpRdBomMapper.selectById(2L)).thenReturn(new ErpRdBomDO()
                .setId(2L).setStatus(ErpRdBomStatusEnum.APPROVE.getStatus()));
        when(erpRdBomMapper.update(any(), any())).thenReturn(0);

        ServiceException ex = assertThrows(ServiceException.class, () -> service.voidRdBom(2L, null));
        assertEquals(1_030_700_084, ex.getCode());
    }

    @Test
    void delete_voidBom_forbidden() {
        when(erpRdBomMapper.selectById(3L)).thenReturn(new ErpRdBomDO()
                .setId(3L).setStatus(ErpRdBomStatusEnum.VOID.getStatus()));

        ServiceException ex = assertThrows(ServiceException.class, () -> service.deleteRdBom(3L));
        assertEquals(1_030_700_085, ex.getCode());
    }

    @Test
    void update_voidBom_forbidden() {
        ErpRdBomServiceImpl spyService = org.mockito.Mockito.spy(service);
        cn.weitee.erp.module.erp.controller.admin.rd.vo.bom.ErpRdBomSaveReqVO reqVO =
                new cn.weitee.erp.module.erp.controller.admin.rd.vo.bom.ErpRdBomSaveReqVO();
        reqVO.setId(4L);
        when(erpRdBomMapper.selectById(4L)).thenReturn(new ErpRdBomDO()
                .setId(4L).setStatus(ErpRdBomStatusEnum.VOID.getStatus()));

        ServiceException ex = assertThrows(ServiceException.class, () -> service.updateRdBom(reqVO));
        assertEquals(1_030_700_086, ex.getCode());
    }

    @Test
    void unvoid_voidBom_shouldRestoreToApprove() {
        when(erpRdBomMapper.selectById(5L)).thenReturn(new ErpRdBomDO()
                .setId(5L).setStatus(ErpRdBomStatusEnum.VOID.getStatus()));
        when(erpRdBomMapper.update(any(), any())).thenReturn(1);

        service.unvoidRdBom(5L);

        ArgumentCaptor<ErpRdBomDO> captor = ArgumentCaptor.forClass(ErpRdBomDO.class);
        verify(erpRdBomMapper).update(captor.capture(), any());
        assertEquals(ErpRdBomStatusEnum.APPROVE.getStatus(), captor.getValue().getStatus());
        verify(changeLogService).logChange(eq(5L), eq("UNVOID"), eq("取消作废，恢复为已审批"));
    }

    @Test
    void unvoid_notVoidBom_shouldThrow() {
        when(erpRdBomMapper.selectById(6L)).thenReturn(new ErpRdBomDO()
                .setId(6L).setStatus(ErpRdBomStatusEnum.APPROVE.getStatus()));
        // CAS 命中 0 行：当前不是 VOID 态
        when(erpRdBomMapper.update(any(), any())).thenReturn(0);

        ServiceException ex = assertThrows(ServiceException.class, () -> service.unvoidRdBom(6L));
        assertEquals(1_030_700_087, ex.getCode());
    }

    @Test
    void getLatest_shouldSkipVoidVersion() {
        // 单值重载：getLatestRdBomByProductId
        when(erpRdBomMapper.selectList(any(SFunction.class), eq(100L))).thenReturn(List.of(
                new ErpRdBomDO().setId(11L).setProductId(100L).setVersion("V2.0")
                        .setStatus(ErpRdBomStatusEnum.VOID.getStatus()),
                new ErpRdBomDO().setId(12L).setProductId(100L).setVersion("V1.0")
                        .setStatus(ErpRdBomStatusEnum.APPROVE.getStatus())));
        // 集合重载：getLatestRdBomMapByProductIds
        when(erpRdBomMapper.selectList(any(SFunction.class), eq(List.of(100L)))).thenReturn(List.of(
                new ErpRdBomDO().setId(11L).setProductId(100L).setVersion("V2.0")
                        .setStatus(ErpRdBomStatusEnum.VOID.getStatus()),
                new ErpRdBomDO().setId(12L).setProductId(100L).setVersion("V1.0")
                        .setStatus(ErpRdBomStatusEnum.APPROVE.getStatus())));

        ErpRdBomDO latest = service.getLatestRdBomByProductId(100L);
        assertEquals(Long.valueOf(12L), latest.getId());

        var map = service.getLatestRdBomMapByProductIds(List.of(100L));
        assertEquals(Long.valueOf(12L), map.get(100L).getId());
    }

    @Test
    void getLatest_allVersionsVoid_shouldReturnNull() {
        when(erpRdBomMapper.selectList(any(SFunction.class), eq(200L))).thenReturn(List.of(
                new ErpRdBomDO().setId(21L).setProductId(200L).setVersion("V1.0")
                        .setStatus(ErpRdBomStatusEnum.VOID.getStatus())));

        assertNull(service.getLatestRdBomByProductId(200L));
    }

}
