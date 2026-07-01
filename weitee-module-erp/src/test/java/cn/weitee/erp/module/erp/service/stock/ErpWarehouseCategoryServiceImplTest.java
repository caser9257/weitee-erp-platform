package cn.weitee.erp.module.erp.service.stock;

import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpWarehouseCategoryDO;
import cn.weitee.erp.module.erp.dal.mysql.stock.ErpWarehouseCategoryMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ErpWarehouseCategoryServiceImplTest {

    @Mock
    private ErpWarehouseCategoryMapper erpWarehouseCategoryMapper;

    private ErpWarehouseCategoryServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ErpWarehouseCategoryServiceImpl();
        org.springframework.test.util.ReflectionTestUtils.setField(service, "erpWarehouseCategoryMapper",
                erpWarehouseCategoryMapper);
    }

    @Test
    void getWarehouseCategoryList_shouldReturnEmptyListWhenIdsBlank() {
        assertThat(service.getWarehouseCategoryList(List.of())).isEmpty();

        verify(erpWarehouseCategoryMapper, never()).selectByIds(List.of());
    }

    @Test
    void getWarehouseCategoryList_shouldQueryMapperWhenIdsPresent() {
        List<Long> ids = List.of(1L);
        ErpWarehouseCategoryDO category = new ErpWarehouseCategoryDO().setId(1L).setName("原料仓分类");
        when(erpWarehouseCategoryMapper.selectByIds(ids)).thenReturn(List.of(category));

        List<ErpWarehouseCategoryDO> result = service.getWarehouseCategoryList(ids);

        assertThat(result).containsExactly(category);
        verify(erpWarehouseCategoryMapper).selectByIds(ids);
    }

}
