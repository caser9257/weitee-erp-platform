package cn.weitee.erp.module.erp.controller.admin.stock;

import cn.weitee.erp.framework.common.exception.ServiceException;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.weitee.erp.module.erp.controller.admin.stock.vo.move.ErpStockMovePageReqVO;
import cn.weitee.erp.module.erp.controller.admin.stock.vo.move.ErpStockMoveRespVO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockMoveDO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockMoveItemDO;
import cn.weitee.erp.module.erp.service.product.ErpProductService;
import cn.weitee.erp.module.erp.service.stock.ErpStockMoveService;
import cn.weitee.erp.module.erp.service.stock.ErpStockService;
import cn.weitee.erp.module.erp.service.stock.ErpWarehouseService;
import cn.weitee.erp.module.system.api.user.AdminUserApi;
import cn.weitee.erp.module.system.api.user.dto.AdminUserRespDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ErpStockMoveControllerTest {

    @Mock
    private ErpStockMoveService stockMoveService;
    @Mock
    private ErpStockService stockService;
    @Mock
    private ErpWarehouseService warehouseService;
    @Mock
    private ErpProductService productService;
    @Mock
    private AdminUserApi adminUserApi;

    @InjectMocks
    private ErpStockMoveController controller;

    @Test
    void getStockMovePage_shouldIgnoreNullItemListAndBlankCreator() {
        when(stockMoveService.getStockMovePage(any())).thenReturn(new PageResult<>(List.of(stockMove(1L, "")), 1L));
        when(stockMoveService.getStockMoveItemListByMoveIds(anyCollection())).thenReturn(null);
        when(productService.getProductVOMap(anyCollection())).thenReturn(Collections.emptyMap());

        PageResult<ErpStockMoveRespVO> result = controller.getStockMovePage(new ErpStockMovePageReqVO()).getData();

        assertThat(result.getTotal()).isEqualTo(1L);
        assertThat(result.getList()).hasSize(1);
        assertThat(result.getList().get(0).getItems()).isEmpty();
        assertThat(result.getList().get(0).getCreatorName()).isNull();
        verify(adminUserApi, never()).getUserMap(anyCollection());
    }

    @Test
    void getStockMovePage_shouldPropagateServiceException() {
        when(stockMoveService.getStockMovePage(any()))
                .thenThrow(new ServiceException(1_000_001, "查询失败"));

        assertThatThrownBy(() -> controller.getStockMovePage(new ErpStockMovePageReqVO()))
                .isInstanceOf(ServiceException.class)
                .hasMessage("查询失败");
    }

    private ErpStockMoveDO stockMove(Long id, String creator) {
        ErpStockMoveDO stockMove = new ErpStockMoveDO();
        stockMove.setId(id);
        stockMove.setNo("DBD202605120001");
        stockMove.setCreator(creator);
        stockMove.setMoveTime(LocalDateTime.of(2026, 5, 12, 10, 0));
        stockMove.setTotalCount(new BigDecimal("5"));
        stockMove.setTotalPrice(new BigDecimal("100.00"));
        stockMove.setStatus(10);
        stockMove.setRemark("测试调拨单");
        return stockMove;
    }

}
