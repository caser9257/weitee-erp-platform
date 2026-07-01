package cn.weitee.erp.module.erp.dal.mysql.purchase;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.weitee.erp.module.erp.controller.admin.purchase.vo.order.ErpPurchaseOrderPageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseOrderDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseOrderItemDO;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.enums.ErpQaStatusEnum;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Objects;

/**
 * ERP 采购订单 Mapper
 *
 * @author WeTai
 */
@Mapper
public interface ErpPurchaseOrderMapper extends BaseMapperX<ErpPurchaseOrderDO> {

    default PageResult<ErpPurchaseOrderDO> selectPage(ErpPurchaseOrderPageReqVO reqVO) {
        return selectJoinPage(reqVO, ErpPurchaseOrderDO.class, buildPageQuery(reqVO));
    }

    static MPJLambdaWrapperX<ErpPurchaseOrderDO> buildPageQuery(ErpPurchaseOrderPageReqVO reqVO) {
        MPJLambdaWrapperX<ErpPurchaseOrderDO> query = new MPJLambdaWrapperX<ErpPurchaseOrderDO>()
                .likeIfPresent(ErpPurchaseOrderDO::getNo, reqVO.getNo())
                .eqIfPresent(ErpPurchaseOrderDO::getSupplierId, reqVO.getSupplierId())
                .betweenIfPresent(ErpPurchaseOrderDO::getOrderTime, reqVO.getOrderTime())
                .eqIfPresent(ErpPurchaseOrderDO::getStatus, reqVO.getStatus())
                .likeIfPresent(ErpPurchaseOrderDO::getRemark, reqVO.getRemark())
                .eqIfPresent(ErpPurchaseOrderDO::getCreator, reqVO.getCreator())
                .orderByDesc(ErpPurchaseOrderDO::getCreateTime)
                .orderByDesc(ErpPurchaseOrderDO::getId);
        // 入库状态。为什么需要 t. 的原因，是因为联表查询时，需要指定表名，不然会报 in_count 错误
        if (Objects.equals(reqVO.getInStatus(), ErpPurchaseOrderPageReqVO.IN_STATUS_NONE)) {
            query.eq(ErpPurchaseOrderDO::getInCount, 0);
        } else if (Objects.equals(reqVO.getInStatus(), ErpPurchaseOrderPageReqVO.IN_STATUS_PART)) {
            query.gt(ErpPurchaseOrderDO::getInCount, 0).apply("t.in_count < t.total_count");
        } else if (Objects.equals(reqVO.getInStatus(), ErpPurchaseOrderPageReqVO.IN_STATUS_ALL)) {
            query.apply("t.in_count = t.total_count");
        }
        // 退货状态
        if (Objects.equals(reqVO.getReturnStatus(), ErpPurchaseOrderPageReqVO.RETURN_STATUS_NONE)) {
            query.eq(ErpPurchaseOrderDO::getReturnCount, 0);
        } else if (Objects.equals(reqVO.getReturnStatus(), ErpPurchaseOrderPageReqVO.RETURN_STATUS_PART)) {
            query.gt(ErpPurchaseOrderDO::getReturnCount, 0).apply("t.return_count < t.total_count");
        } else if (Objects.equals(reqVO.getReturnStatus(), ErpPurchaseOrderPageReqVO.RETURN_STATUS_ALL)) {
            query.apply("t.return_count = t.total_count");
        }
        // 可采购入库
        if (Boolean.TRUE.equals(reqVO.getInEnable())) {
            query.eq(ErpPurchaseOrderDO::getStatus, ErpAuditStatus.APPROVE.getStatus())
                    .apply("t.in_count < t.total_count")
                    .apply("NOT EXISTS (SELECT 1 FROM erp_purchase_in pi " +
                                    "WHERE pi.order_id = t.id " +
                                    "AND (pi.status <> {0} OR pi.qa_status IS NULL OR pi.qa_status = {1}))",
                            ErpAuditStatus.APPROVE.getStatus(), ErpQaStatusEnum.TO_INSPECT.getStatus());
        }
        // 可采购退货
        if (Boolean.TRUE.equals(reqVO.getReturnEnable())) {
            query.eq(ErpPurchaseOrderDO::getStatus, ErpAuditStatus.APPROVE.getStatus())
                    .apply("t.return_count < t.in_count");
        }
        if (reqVO.getProductId() != null) {
            query.leftJoin(ErpPurchaseOrderItemDO.class, ErpPurchaseOrderItemDO::getOrderId, ErpPurchaseOrderDO::getId)
                    .eq(reqVO.getProductId() != null, ErpPurchaseOrderItemDO::getProductId, reqVO.getProductId())
                    .groupBy(ErpPurchaseOrderDO::getId); // 避免 1 对多查询，产生相同的 1
        }
        if (reqVO.getSourceOrderId() != null) {
            query.apply("EXISTS (SELECT 1 FROM erp_purchase_suggest s " +
                            "WHERE s.convert_purchase_order_id = t.id AND s.source_order_id = {0})",
                    reqVO.getSourceOrderId());
        }
        return query;
    }

    default int updateByIdAndStatus(Long id, Integer status, ErpPurchaseOrderDO updateObj) {
        return update(updateObj, new LambdaUpdateWrapper<ErpPurchaseOrderDO>()
                .eq(ErpPurchaseOrderDO::getId, id).eq(ErpPurchaseOrderDO::getStatus, status));
    }

    default int clearProcessInstanceId(Long id) {
        return update(null, new LambdaUpdateWrapper<ErpPurchaseOrderDO>()
                .eq(ErpPurchaseOrderDO::getId, id)
                .set(ErpPurchaseOrderDO::getProcessInstanceId, null));
    }

    default int resetStatusToDraftByBpm(Long id, String processInstanceId) {
        return update(new LambdaUpdateWrapper<ErpPurchaseOrderDO>()
                .eq(ErpPurchaseOrderDO::getId, id)
                .eq(ErpPurchaseOrderDO::getStatus, ErpAuditStatus.PROCESS.getStatus())
                .eq(ErpPurchaseOrderDO::getProcessInstanceId, processInstanceId)
                .set(ErpPurchaseOrderDO::getStatus, ErpAuditStatus.DRAFT.getStatus())
                .set(ErpPurchaseOrderDO::getProcessInstanceId, null));
    }

    default ErpPurchaseOrderDO selectByNo(String no) {
        return selectOne(ErpPurchaseOrderDO::getNo, no);
    }

}
