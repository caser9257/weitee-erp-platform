package cn.weitee.erp.module.erp.dal.mysql.mrp;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.stockreservation.ErpMrpStockReservationPageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpMrpStockReservationDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface ErpMrpStockReservationMapper extends BaseMapperX<ErpMrpStockReservationDO> {

    default PageResult<ErpMrpStockReservationDO> selectPage(ErpMrpStockReservationPageReqVO reqVO) {
        return selectPage(reqVO, buildPageQuery(reqVO));
    }

    static LambdaQueryWrapperX<ErpMrpStockReservationDO> buildPageQuery(ErpMrpStockReservationPageReqVO reqVO) {
        return new LambdaQueryWrapperX<ErpMrpStockReservationDO>()
                .eqIfPresent(ErpMrpStockReservationDO::getPlanId, reqVO.getPlanId())
                .eqIfPresent(ErpMrpStockReservationDO::getProjectId, reqVO.getProjectId())
                .eqIfPresent(ErpMrpStockReservationDO::getProductId, reqVO.getProductId())
                .eqIfPresent(ErpMrpStockReservationDO::getSourceOrderId, reqVO.getSourceOrderId())
                .eqIfPresent(ErpMrpStockReservationDO::getStatus, reqVO.getStatus())
                .orderByDesc(ErpMrpStockReservationDO::getId);
    }

    default List<ErpMrpStockReservationDO> selectActiveList() {
        return selectList(ErpMrpStockReservationDO::getStatus, 0);
    }

    default List<ErpMrpStockReservationDO> selectActiveListByProductIds(Collection<Long> productIds) {
        return selectList(new LambdaQueryWrapperX<ErpMrpStockReservationDO>()
                .inIfPresent(ErpMrpStockReservationDO::getProductId, productIds)
                .eq(ErpMrpStockReservationDO::getStatus, 0));
    }

    default List<ErpMrpStockReservationDO> selectListBySourceOrderIds(Collection<Long> sourceOrderIds) {
        return selectList(new LambdaQueryWrapperX<ErpMrpStockReservationDO>()
                .inIfPresent(ErpMrpStockReservationDO::getSourceOrderId, sourceOrderIds)
                .eq(ErpMrpStockReservationDO::getStatus, 0));
    }

    default int deleteByPlanId(Long planId) {
        return delete(ErpMrpStockReservationDO::getPlanId, planId);
    }

    default int updateStatusBySourceOrderIds(Collection<Long> sourceOrderIds, Integer fromStatus, Integer toStatus) {
        return update(new ErpMrpStockReservationDO().setStatus(toStatus),
                new LambdaQueryWrapperX<ErpMrpStockReservationDO>()
                        .inIfPresent(ErpMrpStockReservationDO::getSourceOrderId, sourceOrderIds)
                        .eqIfPresent(ErpMrpStockReservationDO::getStatus, fromStatus));
    }

}
