package cn.weitee.erp.module.erp.dal.mysql.mrp;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.stockreservation.ErpMrpStockReservationSummaryPageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpMrpStockReservationSummaryDO;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface ErpMrpStockReservationSummaryMapper extends BaseMapperX<ErpMrpStockReservationSummaryDO> {

    default PageResult<ErpMrpStockReservationSummaryDO> selectPage(ErpMrpStockReservationSummaryPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ErpMrpStockReservationSummaryDO>()
                .eqIfPresent(ErpMrpStockReservationSummaryDO::getProductId, reqVO.getProductId())
                .gt(ErpMrpStockReservationSummaryDO::getActiveReservedQty, BigDecimal.ZERO)
                .orderByDesc(ErpMrpStockReservationSummaryDO::getActiveReservedQty)
                .orderByDesc(ErpMrpStockReservationSummaryDO::getLastReservedTime));
    }

    default List<ErpMrpStockReservationSummaryDO> selectActiveList() {
        return selectList(new LambdaQueryWrapperX<ErpMrpStockReservationSummaryDO>()
                .gt(ErpMrpStockReservationSummaryDO::getActiveReservedQty, BigDecimal.ZERO));
    }

    default ErpMrpStockReservationSummaryDO selectByProductId(Long productId) {
        return selectOne(ErpMrpStockReservationSummaryDO::getProductId, productId);
    }

}
