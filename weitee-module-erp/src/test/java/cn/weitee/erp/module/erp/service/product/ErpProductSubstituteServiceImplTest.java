package cn.weitee.erp.module.erp.service.product;

import cn.weitee.erp.framework.common.enums.CommonStatusEnum;
import cn.weitee.erp.framework.common.exception.ServiceException;
import cn.weitee.erp.module.erp.controller.admin.product.vo.substitute.ErpProductSubstituteSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductDO;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductSubstituteDO;
import cn.weitee.erp.module.erp.dal.mysql.product.ErpProductMapper;
import cn.weitee.erp.module.erp.dal.mysql.product.ErpProductSubstituteMapper;
import cn.weitee.erp.module.erp.enums.product.ErpProductSubstituteTypeEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ErpProductSubstituteServiceImplTest {

    @Mock
    private ErpProductSubstituteMapper substituteMapper;
    @Mock
    private ErpProductMapper productMapper;
    @Mock
    private ErpProductService productService;
    @InjectMocks
    private ErpProductSubstituteServiceImpl service;

    @Captor
    private ArgumentCaptor<ErpProductSubstituteDO> doCaptor;

    private static final Long PRODUCT_ID = 1L;
    private static final ErpProductDO PRODUCT = new ErpProductDO().setId(PRODUCT_ID).setName("主物料");
    private static final Long SUB_1 = 10L;
    private static final Long SUB_2 = 20L;
    private static final Long SUB_3 = 30L;
    private static final ErpProductDO SUB_PRODUCT_1 = new ErpProductDO().setId(SUB_1).setName("替代料1");
    private static final ErpProductDO SUB_PRODUCT_2 = new ErpProductDO().setId(SUB_2).setName("替代料2");
    private static final ErpProductDO SUB_PRODUCT_3 = new ErpProductDO().setId(SUB_3).setName("替代料3");

    private static ErpProductSubstituteSaveReqVO row(Long id, Long productId, Long subId, Integer priority) {
        ErpProductSubstituteSaveReqVO row = new ErpProductSubstituteSaveReqVO();
        row.setId(id);
        row.setProductId(productId);
        row.setSubstituteProductId(subId);
        row.setPriority(priority);
        return row;
    }

    @Test
    void batchUpdate_shouldDeleteUpdateInsert() {
        when(productMapper.selectById(PRODUCT_ID)).thenReturn(PRODUCT);
        when(productMapper.selectById(SUB_2)).thenReturn(SUB_PRODUCT_2);
        when(productMapper.selectById(40L)).thenReturn(new ErpProductDO().setId(40L).setName("替代料4"));
        // 已有 3 行：id=1(sub=10), id=2(sub=20), id=3(sub=30)
        List<ErpProductSubstituteDO> existing = List.of(
                ErpProductSubstituteDO.builder().id(1L).productId(PRODUCT_ID).substituteProductId(SUB_1).priority(1).build(),
                ErpProductSubstituteDO.builder().id(2L).productId(PRODUCT_ID).substituteProductId(SUB_2).priority(2).build(),
                ErpProductSubstituteDO.builder().id(3L).productId(PRODUCT_ID).substituteProductId(SUB_3).priority(3).build()
        );
        when(substituteMapper.selectListByProductId(PRODUCT_ID)).thenReturn(existing);

        // 传入 2 行：删 id=1、id=3（未传入）；更新 id=2（改 priority）；新增 sub=40（无 id）
        List<ErpProductSubstituteSaveReqVO> input = List.of(
                row(2L, PRODUCT_ID, SUB_2, 99),
                row(null, PRODUCT_ID, 40L, 5)
        );

        service.batchUpdateSubstitutes(PRODUCT_ID, input);

        // 验证：删除 id=1 和 id=3
        verify(substituteMapper).deleteById(1L);
        verify(substituteMapper).deleteById(3L);
        // 验证：更新 id=2
        verify(substituteMapper).updateById(doCaptor.capture());
        ErpProductSubstituteDO updated = doCaptor.getValue();
        assertEquals(2L, updated.getId());
        assertEquals(Integer.valueOf(99), updated.getPriority());
        // 验证：新增 sub=40
        verify(substituteMapper).insert(doCaptor.capture());
        ErpProductSubstituteDO inserted = doCaptor.getValue();
        assertEquals(40L, inserted.getSubstituteProductId());
        assertEquals(Integer.valueOf(5), inserted.getPriority());
    }

    @Test
    void batchUpdate_withEmptyList_shouldDeleteAll() {
        when(productMapper.selectById(PRODUCT_ID)).thenReturn(PRODUCT);
        List<ErpProductSubstituteDO> existing = List.of(
                ErpProductSubstituteDO.builder().id(1L).productId(PRODUCT_ID).substituteProductId(SUB_1).build()
        );
        when(substituteMapper.selectListByProductId(PRODUCT_ID)).thenReturn(existing);

        service.batchUpdateSubstitutes(PRODUCT_ID, List.of());

        verify(substituteMapper).deleteById(1L);
        verify(substituteMapper, never()).insert(any(ErpProductSubstituteDO.class));
        verify(substituteMapper, never()).updateById(any(ErpProductSubstituteDO.class));
    }

    @Test
    void batchUpdate_withNullList_shouldDeleteAll() {
        when(productMapper.selectById(PRODUCT_ID)).thenReturn(PRODUCT);
        List<ErpProductSubstituteDO> existing = List.of(
                ErpProductSubstituteDO.builder().id(1L).productId(PRODUCT_ID).substituteProductId(SUB_1).build()
        );
        when(substituteMapper.selectListByProductId(PRODUCT_ID)).thenReturn(existing);

        service.batchUpdateSubstitutes(PRODUCT_ID, null);

        verify(substituteMapper).deleteById(1L);
        verify(substituteMapper, never()).insert(any(ErpProductSubstituteDO.class));
        verify(substituteMapper, never()).updateById(any(ErpProductSubstituteDO.class));
    }

    @Test
    void batchUpdate_shouldRejectDuplicateSubstitute() {
        when(productMapper.selectById(PRODUCT_ID)).thenReturn(PRODUCT);
        when(productMapper.selectById(SUB_1)).thenReturn(SUB_PRODUCT_1);
        List<ErpProductSubstituteSaveReqVO> input = List.of(
                row(null, PRODUCT_ID, SUB_1, 1),
                row(null, PRODUCT_ID, SUB_1, 1)
        );
        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.batchUpdateSubstitutes(PRODUCT_ID, input));
        assertEquals(PRODUCT_SUBSTITUTE_REPEATED.getCode(), ex.getCode());
    }

    @Test
    void batchUpdate_shouldRejectWhenProductNotExists() {
        when(productMapper.selectById(99L)).thenReturn(null);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.batchUpdateSubstitutes(99L, List.of()));
        assertEquals(PRODUCT_NOT_EXISTS.getCode(), ex.getCode());
    }

    @Test
    void batchUpdate_shouldRejectSelfSubstitute() {
        when(productMapper.selectById(PRODUCT_ID)).thenReturn(PRODUCT);
        List<ErpProductSubstituteSaveReqVO> input = List.of(
                row(null, PRODUCT_ID, PRODUCT_ID, 1)
        );
        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.batchUpdateSubstitutes(PRODUCT_ID, input));
        assertEquals(PRODUCT_SUBSTITUTE_SELF.getCode(), ex.getCode());
    }

    @Test
    void batchUpdate_newRow_shouldFillDefaults() {
        when(productMapper.selectById(PRODUCT_ID)).thenReturn(PRODUCT);
        when(productMapper.selectById(SUB_1)).thenReturn(SUB_PRODUCT_1);
        when(substituteMapper.selectListByProductId(PRODUCT_ID)).thenReturn(List.of());

        List<ErpProductSubstituteSaveReqVO> input = List.of(
                row(null, PRODUCT_ID, SUB_1, null)
        );
        service.batchUpdateSubstitutes(PRODUCT_ID, input);

        verify(substituteMapper).insert(doCaptor.capture());
        ErpProductSubstituteDO inserted = doCaptor.getValue();
        assertEquals(Integer.valueOf(1), inserted.getPriority());
        assertEquals(BigDecimal.ONE, inserted.getReplaceRatio());
        assertEquals(ErpProductSubstituteTypeEnum.GLOBAL.getType(), inserted.getSubstituteType());
        assertEquals(CommonStatusEnum.ENABLE.getStatus(), inserted.getStatus());
    }

}