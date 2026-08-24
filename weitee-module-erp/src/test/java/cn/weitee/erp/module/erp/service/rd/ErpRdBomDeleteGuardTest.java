package cn.weitee.erp.module.erp.service.rd;

import cn.weitee.erp.framework.common.exception.ServiceException;
import cn.weitee.erp.framework.test.core.ut.BaseMockitoUnitTest;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import cn.weitee.erp.module.erp.dal.dataobject.rd.ErpRdBomDO;
import cn.weitee.erp.module.erp.dal.mysql.rd.ErpRdBomItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.rd.ErpRdBomMapper;
import cn.weitee.erp.module.erp.enums.rd.ErpRdBomStatusEnum;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 研发 BOM 删除闸门测试：版本历史保护（已审批 / 已发布 / 被派生引用 均禁止删除）
 */
class ErpRdBomDeleteGuardTest extends BaseMockitoUnitTest {

    @Mock
    private ErpRdBomMapper erpRdBomMapper;
    @Mock
    private ErpRdBomItemMapper erpRdBomItemMapper;
    @Mock
    private cn.weitee.erp.module.erp.dal.mysql.rd.ErpRdBomItemSubstituteMapper erpRdBomItemSubstituteMapper;
    @Mock
    private cn.weitee.erp.module.erp.service.product.ErpProductService productService;
    @Mock
    private ErpRdBomChangeLogService changeLogService;

    @InjectMocks
    private ErpRdBomServiceImpl service;

    @Test
    void delete_approvedBom_forbidden() {
        when(erpRdBomMapper.selectById(1L)).thenReturn(new ErpRdBomDO()
                .setId(1L).setStatus(ErpRdBomStatusEnum.APPROVE.getStatus()));

        ServiceException ex = assertThrows(ServiceException.class, () -> service.deleteRdBom(1L));
        verify(erpRdBomMapper, never()).deleteById(1L);
        assertEquals(1_030_700_080, ex.getCode());
    }

    @Test
    void delete_publishedBom_forbidden() {
        when(erpRdBomMapper.selectById(2L)).thenReturn(new ErpRdBomDO()
                .setId(2L).setStatus(ErpRdBomStatusEnum.PROCESS.getStatus()).setPublishedBomId(88L));

        ServiceException ex = assertThrows(ServiceException.class, () -> service.deleteRdBom(2L));
        verify(erpRdBomMapper, never()).deleteById(2L);
        assertEquals(1_030_700_081, ex.getCode());
    }

    @Test
    void delete_referencedByDerivedVersion_forbidden() {
        when(erpRdBomMapper.selectById(3L)).thenReturn(new ErpRdBomDO()
                .setId(3L).setStatus(ErpRdBomStatusEnum.PROCESS.getStatus()));
        when(erpRdBomMapper.selectCount(any(SFunction.class), eq(3L))).thenReturn(2L);

        ServiceException ex = assertThrows(ServiceException.class, () -> service.deleteRdBom(3L));
        verify(erpRdBomMapper, never()).deleteById(3L);
        assertEquals(1_030_700_082, ex.getCode());
    }

    @Test
    void delete_draftWithoutReference_allowed() {
        when(erpRdBomMapper.selectById(4L)).thenReturn(new ErpRdBomDO()
                .setId(4L).setStatus(ErpRdBomStatusEnum.DRAFT.getStatus()));
        when(erpRdBomMapper.selectCount(any(SFunction.class), eq(4L))).thenReturn(0L);
        when(erpRdBomItemMapper.selectListByBomId(4L)).thenReturn(List.of());

        assertDoesNotThrow(() -> service.deleteRdBom(4L));
        verify(erpRdBomMapper).deleteById(4L);
        verify(erpRdBomItemMapper).deleteByBomId(4L);
    }

}
