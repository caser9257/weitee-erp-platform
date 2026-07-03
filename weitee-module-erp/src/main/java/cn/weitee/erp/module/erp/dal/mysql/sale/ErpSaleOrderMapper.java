package cn.weitee.erp.module.erp.dal.mysql.sale;


import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.weitee.erp.module.erp.controller.admin.sale.vo.order.ErpSaleOrderPageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.sale.ErpSaleOrderDO;
import cn.weitee.erp.module.erp.dal.dataobject.sale.ErpSaleOrderItemDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * ERP 销售订单 Mapper
 *
 * @author WeTai
 */
@Mapper
public interface ErpSaleOrderMapper extends BaseMapperX<ErpSaleOrderDO> {

    default PageResult<ErpSaleOrderDO> selectPage(ErpSaleOrderPageReqVO reqVO) {
        return selectJoinPage(reqVO, ErpSaleOrderDO.class, buildPageQuery(reqVO));
    }

    static MPJLambdaWrapperX<ErpSaleOrderDO> buildPageQuery(ErpSaleOrderPageReqVO reqVO) {
        MPJLambdaWrapperX<ErpSaleOrderDO> query = new MPJLambdaWrapperX<ErpSaleOrderDO>()
                .likeIfPresent(ErpSaleOrderDO::getNo, reqVO.getNo())
                .eqIfPresent(ErpSaleOrderDO::getCustomerId, reqVO.getCustomerId())
                .eqIfPresent(ErpSaleOrderDO::getProjectId, reqVO.getProjectId())
                .eqIfPresent(ErpSaleOrderDO::getSaleUserId, reqVO.getSaleUserId())
                .eqIfPresent(ErpSaleOrderDO::getBusinessType, reqVO.getBusinessType())
                .betweenIfPresent(ErpSaleOrderDO::getOrderTime, reqVO.getOrderTime())
                .betweenIfPresent(ErpSaleOrderDO::getDeliveryDate, reqVO.getDeliveryDate())
                .eqIfPresent(ErpSaleOrderDO::getStatus, reqVO.getStatus())
                .eqIfPresent(ErpSaleOrderDO::getDeliveryReadyStatus, reqVO.getDeliveryReadyStatus())
                .likeIfPresent(ErpSaleOrderDO::getRemark, reqVO.getRemark())
                .eqIfPresent(ErpSaleOrderDO::getCreator, reqVO.getCreator())
                .orderByDesc(ErpSaleOrderDO::getId);
        // 入库状态。为什么需要 t. 的原因，是因为联表查询时，需要指定表名，不然会报 out_count 错误
        if (Objects.equals(reqVO.getOutStatus(), ErpSaleOrderPageReqVO.OUT_STATUS_NONE)) {
            query.eq(ErpSaleOrderDO::getOutCount, 0);
        } else if (Objects.equals(reqVO.getOutStatus(), ErpSaleOrderPageReqVO.OUT_STATUS_PART)) {
            query.gt(ErpSaleOrderDO::getOutCount, 0).apply("t.out_count < t.total_count");
        } else if (Objects.equals(reqVO.getOutStatus(), ErpSaleOrderPageReqVO.OUT_STATUS_ALL)) {
            query.apply("t.out_count = t.total_count");
        }
        // 退货状态
        if (Objects.equals(reqVO.getReturnStatus(), ErpSaleOrderPageReqVO.RETURN_STATUS_NONE)) {
            query.eq(ErpSaleOrderDO::getReturnCount, 0);
        } else if (Objects.equals(reqVO.getReturnStatus(), ErpSaleOrderPageReqVO.RETURN_STATUS_PART)) {
            query.gt(ErpSaleOrderDO::getReturnCount, 0).apply("t.return_count < t.total_count");
        } else if (Objects.equals(reqVO.getReturnStatus(), ErpSaleOrderPageReqVO.RETURN_STATUS_ALL)) {
            query.apply("t.return_count = t.total_count");
        }
        // 可销售出库
        if (Boolean.TRUE.equals(reqVO.getOutEnable())) {
            query.eq(ErpSaleOrderDO::getStatus, ErpAuditStatus.APPROVE.getStatus())
                    .apply("t.out_count < t.total_count");
        }
        // 可销售退货
        if (Boolean.TRUE.equals(reqVO.getReturnEnable())) {
            query.eq(ErpSaleOrderDO::getStatus, ErpAuditStatus.APPROVE.getStatus())
                    .apply("t.return_count < t.out_count");
        }
        if (reqVO.getProductId() != null) {
            query.leftJoin(ErpSaleOrderItemDO.class, ErpSaleOrderItemDO::getOrderId, ErpSaleOrderDO::getId)
                    .eq(reqVO.getProductId() != null, ErpSaleOrderItemDO::getProductId, reqVO.getProductId())
                    .groupBy(ErpSaleOrderDO::getId); // 避免 1 对多查询，产生相同的 1
        }
        return query;
    }

    default int updateByIdAndStatus(Long id, Integer status, ErpSaleOrderDO updateObj) {
        return update(updateObj, new LambdaUpdateWrapper<ErpSaleOrderDO>()
                .eq(ErpSaleOrderDO::getId, id).eq(ErpSaleOrderDO::getStatus, status));
    }

    default int clearProcessInstanceId(Long id, String processInstanceId) {
        return update(null, new LambdaUpdateWrapper<ErpSaleOrderDO>()
                .eq(ErpSaleOrderDO::getId, id)
                .eq(ErpSaleOrderDO::getProcessInstanceId, processInstanceId)
                .set(ErpSaleOrderDO::getProcessInstanceId, null));
    }

    default int resetStatusToDraftByBpm(Long id, String processInstanceId) {
        return update(new LambdaUpdateWrapper<ErpSaleOrderDO>()
                .eq(ErpSaleOrderDO::getId, id)
                .eq(ErpSaleOrderDO::getStatus, ErpAuditStatus.PROCESS.getStatus())
                .eq(ErpSaleOrderDO::getProcessInstanceId, processInstanceId)
                .set(ErpSaleOrderDO::getStatus, ErpAuditStatus.DRAFT.getStatus())
                .set(ErpSaleOrderDO::getProcessInstanceId, null));
    }

    default ErpSaleOrderDO selectByNo(String no) {
        return selectOne(ErpSaleOrderDO::getNo, no);
    }

    default List<ErpSaleOrderDO> selectDisplayListByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapper<ErpSaleOrderDO>()
                .select(ErpSaleOrderDO::getId, ErpSaleOrderDO::getNo,
                        ErpSaleOrderDO::getSaleUserId, ErpSaleOrderDO::getCreator)
                .in(ErpSaleOrderDO::getId, ids));
    }

    default Long selectCountByProjectId(Long projectId) {
        return selectCount(ErpSaleOrderDO::getProjectId, projectId);
    }

    /**
     * 查询市场执行台账统计所需的订单精简列（仅状态与金额，避免全列全表加载）
     */
    default List<ErpSaleOrderDO> selectListForMarketLedgerStats() {
        return selectList(new LambdaQueryWrapper<ErpSaleOrderDO>()
                .select(ErpSaleOrderDO::getId, ErpSaleOrderDO::getTotalPrice,
                        ErpSaleOrderDO::getShipmentReleaseStatus, ErpSaleOrderDO::getInvoiceStatus,
                        ErpSaleOrderDO::getAcceptanceStatus));
    }

}
