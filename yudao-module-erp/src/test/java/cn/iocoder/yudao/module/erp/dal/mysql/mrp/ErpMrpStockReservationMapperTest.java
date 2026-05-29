package cn.iocoder.yudao.module.erp.dal.mysql.mrp;

import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.stockreservation.ErpMrpStockReservationPageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpMrpStockReservationDO;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ErpMrpStockReservationMapperTest {

    @Test
    void buildPageQuery_shouldIncludeAllFilters() {
        ErpMrpStockReservationPageReqVO reqVO = new ErpMrpStockReservationPageReqVO();
        reqVO.setPlanId(1L);
        reqVO.setProjectId(2L);
        reqVO.setProductId(3L);
        reqVO.setSourceOrderId(4L);
        reqVO.setStatus(0);

        LambdaQueryWrapperX<ErpMrpStockReservationDO> query = ErpMrpStockReservationMapper.buildPageQuery(reqVO);

        assertThat(query.getExpression().getNormal()).hasSize(19);
    }

    @Test
    void buildPageQuery_shouldSkipMissingFilters() {
        ErpMrpStockReservationPageReqVO reqVO = new ErpMrpStockReservationPageReqVO();

        LambdaQueryWrapperX<ErpMrpStockReservationDO> query = ErpMrpStockReservationMapper.buildPageQuery(reqVO);

        assertThat(query.getExpression().getNormal()).isEmpty();
    }

}
