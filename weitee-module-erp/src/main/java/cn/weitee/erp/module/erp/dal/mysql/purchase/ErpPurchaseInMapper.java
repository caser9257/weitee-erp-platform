package cn.weitee.erp.module.erp.dal.mysql.purchase;


import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.weitee.erp.module.erp.controller.admin.purchase.vo.in.ErpPurchaseInPageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseInDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseInItemDO;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * ERP 采购入库 Mapper
 *
 * @author WeTai
 */
@Mapper
public interface ErpPurchaseInMapper extends BaseMapperX<ErpPurchaseInDO> {

    default PageResult<ErpPurchaseInDO> selectPage(ErpPurchaseInPageReqVO reqVO) {
        if (reqVO.getWarehouseId() == null && reqVO.getProductId() == null) {
            LambdaQueryWrapper<ErpPurchaseInDO> query = new LambdaQueryWrapper<ErpPurchaseInDO>()
                    .like(reqVO.getNo() != null && !reqVO.getNo().isEmpty(), ErpPurchaseInDO::getNo, reqVO.getNo())
                    .eq(reqVO.getSupplierId() != null, ErpPurchaseInDO::getSupplierId, reqVO.getSupplierId())
                    .between(reqVO.getInTime() != null && reqVO.getInTime().length == 2,
                            ErpPurchaseInDO::getInTime, reqVO.getInTime() != null ? reqVO.getInTime()[0] : null,
                            reqVO.getInTime() != null ? reqVO.getInTime()[1] : null)
                    .eq(reqVO.getStatus() != null, ErpPurchaseInDO::getStatus, reqVO.getStatus())
                    .eq(reqVO.getQaStatus() != null, ErpPurchaseInDO::getQaStatus, reqVO.getQaStatus())
                    .eq(reqVO.getStockInStatus() != null, ErpPurchaseInDO::getStockInStatus, reqVO.getStockInStatus())
                    .like(reqVO.getRemark() != null && !reqVO.getRemark().isEmpty(), ErpPurchaseInDO::getRemark, reqVO.getRemark())
                    .eq(reqVO.getCreator() != null && !reqVO.getCreator().isEmpty(), ErpPurchaseInDO::getCreator, reqVO.getCreator())
                    .eq(reqVO.getAccountId() != null, ErpPurchaseInDO::getAccountId, reqVO.getAccountId())
                    .like(reqVO.getOrderNo() != null && !reqVO.getOrderNo().isEmpty(), ErpPurchaseInDO::getOrderNo, reqVO.getOrderNo())
                    .orderByDesc(ErpPurchaseInDO::getCreateTime)
                    .orderByDesc(ErpPurchaseInDO::getId);
            if (Objects.equals(reqVO.getPaymentStatus(), ErpPurchaseInPageReqVO.PAYMENT_STATUS_NONE)) {
                query.eq(ErpPurchaseInDO::getPaymentPrice, 0);
            } else if (Objects.equals(reqVO.getPaymentStatus(), ErpPurchaseInPageReqVO.PAYMENT_STATUS_PART)) {
                query.gt(ErpPurchaseInDO::getPaymentPrice, 0).apply("payment_price < total_price");
            } else if (Objects.equals(reqVO.getPaymentStatus(), ErpPurchaseInPageReqVO.PAYMENT_STATUS_ALL)) {
                query.apply("payment_price = total_price");
            }
            if (Boolean.TRUE.equals(reqVO.getPaymentEnable())) {
                query.eq(ErpPurchaseInDO::getStatus, ErpAuditStatus.APPROVE.getStatus())
                        .apply("payment_price < total_price");
            }
            return selectPage(reqVO, query);
        }

        MPJLambdaWrapperX<ErpPurchaseInDO> query = new MPJLambdaWrapperX<ErpPurchaseInDO>()
                .selectAll(ErpPurchaseInDO.class)
                .likeIfPresent(ErpPurchaseInDO::getNo, reqVO.getNo())
                .eqIfPresent(ErpPurchaseInDO::getSupplierId, reqVO.getSupplierId())
                .betweenIfPresent(ErpPurchaseInDO::getInTime, reqVO.getInTime())
                .eqIfPresent(ErpPurchaseInDO::getStatus, reqVO.getStatus())
                .eqIfPresent(ErpPurchaseInDO::getQaStatus, reqVO.getQaStatus())
                .eqIfPresent(ErpPurchaseInDO::getStockInStatus, reqVO.getStockInStatus())
                .likeIfPresent(ErpPurchaseInDO::getRemark, reqVO.getRemark())
                .eqIfPresent(ErpPurchaseInDO::getCreator, reqVO.getCreator())
                .eqIfPresent(ErpPurchaseInDO::getAccountId, reqVO.getAccountId())
                .likeIfPresent(ErpPurchaseInDO::getOrderNo, reqVO.getOrderNo())
                .orderByDesc(ErpPurchaseInDO::getCreateTime)
                .orderByDesc(ErpPurchaseInDO::getId);
        // 付款状态。为什么需要 t. 的原因，是因为联表查询时，需要指定表名，不然会报字段不存在的错误
        if (Objects.equals(reqVO.getPaymentStatus(), ErpPurchaseInPageReqVO.PAYMENT_STATUS_NONE)) {
            query.eq(ErpPurchaseInDO::getPaymentPrice, 0);
        } else if (Objects.equals(reqVO.getPaymentStatus(), ErpPurchaseInPageReqVO.PAYMENT_STATUS_PART)) {
            query.gt(ErpPurchaseInDO::getPaymentPrice, 0).apply("t.payment_price < t.total_price");
        } else if (Objects.equals(reqVO.getPaymentStatus(), ErpPurchaseInPageReqVO.PAYMENT_STATUS_ALL)) {
            query.apply("t.payment_price = t.total_price");
        }
        if (Boolean.TRUE.equals(reqVO.getPaymentEnable())) {
            query.eq(ErpPurchaseInDO::getStatus, ErpAuditStatus.APPROVE.getStatus())
                    .apply("t.payment_price < t.total_price");
        }
        if (reqVO.getWarehouseId() != null || reqVO.getProductId() != null) {
            query.leftJoin(ErpPurchaseInItemDO.class, ErpPurchaseInItemDO::getInId, ErpPurchaseInDO::getId)
                    .eq(reqVO.getWarehouseId() != null, ErpPurchaseInItemDO::getWarehouseId, reqVO.getWarehouseId())
                    .eq(reqVO.getProductId() != null, ErpPurchaseInItemDO::getProductId, reqVO.getProductId())
                    .groupBy(ErpPurchaseInDO::getId); // 避免 1 对多查询，产生相同的 1
        }
        return selectJoinPage(reqVO, ErpPurchaseInDO.class, query);
    }

    default int updateByIdAndStatus(Long id, Integer status, ErpPurchaseInDO updateObj) {
        return update(updateObj, new LambdaUpdateWrapper<ErpPurchaseInDO>()
                .eq(ErpPurchaseInDO::getId, id).eq(ErpPurchaseInDO::getStatus, status));
    }

    default int clearProcessInstanceId(Long id, String processInstanceId) {
        return update(null, new LambdaUpdateWrapper<ErpPurchaseInDO>()
                .eq(ErpPurchaseInDO::getId, id)
                .eq(ErpPurchaseInDO::getProcessInstanceId, processInstanceId)
                .set(ErpPurchaseInDO::getProcessInstanceId, null));
    }

    default ErpPurchaseInDO selectByNo(String no) {
        return selectOne(ErpPurchaseInDO::getNo, no);
    }

    default List<ErpPurchaseInDO> selectListByOrderId(Long orderId) {
        return selectList(ErpPurchaseInDO::getOrderId, orderId);
    }

    default List<ErpPurchaseInDO> selectListByOrderIds(Collection<Long> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapper<ErpPurchaseInDO>()
                .in(ErpPurchaseInDO::getOrderId, orderIds)
                .orderByDesc(ErpPurchaseInDO::getCreateTime)
                .orderByDesc(ErpPurchaseInDO::getId));
    }

    default List<ErpPurchaseInDO> selectApprovedListByOrderId(Long orderId) {
        return selectList(new LambdaQueryWrapper<ErpPurchaseInDO>()
                .eq(ErpPurchaseInDO::getOrderId, orderId)
                .eq(ErpPurchaseInDO::getStatus, ErpAuditStatus.APPROVE.getStatus()));
    }

    default List<ErpPurchaseInDO> selectListByOrderIdAndStatus(Long orderId, Integer status) {
        return selectList(new LambdaQueryWrapper<ErpPurchaseInDO>()
                .eq(ErpPurchaseInDO::getOrderId, orderId)
                .eq(ErpPurchaseInDO::getStatus, status));
    }

}
