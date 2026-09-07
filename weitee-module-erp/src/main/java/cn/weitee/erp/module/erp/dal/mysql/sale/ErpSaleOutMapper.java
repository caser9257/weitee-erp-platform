package cn.weitee.erp.module.erp.dal.mysql.sale;


import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.weitee.erp.module.erp.controller.admin.sale.vo.out.ErpSaleOutPageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.sale.ErpSaleOutDO;
import cn.weitee.erp.module.erp.dal.dataobject.sale.ErpSaleOutItemDO;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * ERP 销售出库 Mapper
 *
 * @author WeTai
 */
@Mapper
public interface ErpSaleOutMapper extends BaseMapperX<ErpSaleOutDO> {

    default PageResult<ErpSaleOutDO> selectPage(ErpSaleOutPageReqVO reqVO) {
        MPJLambdaWrapperX<ErpSaleOutDO> query = new MPJLambdaWrapperX<ErpSaleOutDO>()
                .likeIfPresent(ErpSaleOutDO::getNo, reqVO.getNo())
                .eqIfPresent(ErpSaleOutDO::getCustomerId, reqVO.getCustomerId())
                .betweenIfPresent(ErpSaleOutDO::getOutTime, reqVO.getOutTime())
                .eqIfPresent(ErpSaleOutDO::getStatus, reqVO.getStatus())
                .likeIfPresent(ErpSaleOutDO::getRemark, reqVO.getRemark())
                .eqIfPresent(ErpSaleOutDO::getCreator, reqVO.getCreator())
                .eqIfPresent(ErpSaleOutDO::getAccountId, reqVO.getAccountId())
                .likeIfPresent(ErpSaleOutDO::getOrderNo, reqVO.getOrderNo())
                .orderByDesc(ErpSaleOutDO::getId);
        // 收款状态。为什么需要 t. 的原因，是因为联表查询时，需要指定表名，不然会报字段不存在的错误
        if (Objects.equals(reqVO.getReceiptStatus(), ErpSaleOutPageReqVO.RECEIPT_STATUS_NONE)) {
            query.eq(ErpSaleOutDO::getReceiptPrice, 0);
        } else if (Objects.equals(reqVO.getReceiptStatus(), ErpSaleOutPageReqVO.RECEIPT_STATUS_PART)) {
            query.gt(ErpSaleOutDO::getReceiptPrice, 0).apply("t.receipt_price < t.total_price");
        } else if (Objects.equals(reqVO.getReceiptStatus(), ErpSaleOutPageReqVO.RECEIPT_STATUS_ALL)) {
            query.apply("t.receipt_price = t.total_price");
        }
        if (Boolean.TRUE.equals(reqVO.getReceiptEnable())) {
            query.eq(ErpSaleOutDO::getStatus, ErpAuditStatus.APPROVE.getStatus())
                    .apply("t.receipt_price < t.total_price");
        }
        if (reqVO.getWarehouseId() != null || reqVO.getProductId() != null) {
            query.leftJoin(ErpSaleOutItemDO.class, ErpSaleOutItemDO::getOutId, ErpSaleOutDO::getId)
                    .eq(reqVO.getWarehouseId() != null, ErpSaleOutItemDO::getWarehouseId, reqVO.getWarehouseId())
                    .eq(reqVO.getProductId() != null, ErpSaleOutItemDO::getProductId, reqVO.getProductId())
                    .groupBy(ErpSaleOutDO::getId); // 避免 1 对多查询，产生相同的 1
        }
        return selectJoinPage(reqVO, ErpSaleOutDO.class, query);
    }

    default int updateByIdAndStatus(Long id, Integer status, ErpSaleOutDO updateObj) {
        return update(updateObj, new LambdaUpdateWrapper<ErpSaleOutDO>()
                .eq(ErpSaleOutDO::getId, id).eq(ErpSaleOutDO::getStatus, status));
    }

    /**
     * 反审核专用 CAS：仅当出库单仍处于指定状态且尚未产生任何收款（receipt_price = 0）时才更新。
     * 把「已收款不可反审核」的校验下沉到 WHERE 条件，关闭「读校验→并发收款回写→改状态」的脏状态窗口。
     */
    default int updateByIdAndStatusAndNoReceipt(Long id, Integer status, ErpSaleOutDO updateObj) {
        return update(updateObj, new LambdaUpdateWrapper<ErpSaleOutDO>()
                .eq(ErpSaleOutDO::getId, id)
                .eq(ErpSaleOutDO::getStatus, status)
                .eq(ErpSaleOutDO::getReceiptPrice, BigDecimal.ZERO));
    }

    default ErpSaleOutDO selectByNo(String no) {
        return selectOne(ErpSaleOutDO::getNo, no);
    }

    default List<ErpSaleOutDO> selectListByOrderId(Long orderId) {
        return selectList(ErpSaleOutDO::getOrderId, orderId);
    }

    default List<ErpSaleOutDO> selectListByOrderIds(Collection<Long> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapper<ErpSaleOutDO>()
                .in(ErpSaleOutDO::getOrderId, orderIds));
    }

    default List<ErpSaleOutDO> selectListByOrderIdAndStatus(Long orderId, Integer status) {
        return selectList(new LambdaQueryWrapper<ErpSaleOutDO>()
                .eq(ErpSaleOutDO::getOrderId, orderId)
                .eq(ErpSaleOutDO::getStatus, status));
    }

    /**
     * 汇总所有出库单的已收款金额（DB 层 SUM，避免把全部出库单载入内存）
     */
    default java.math.BigDecimal sumTotalReceiptPrice() {
        java.util.List<java.util.Map<String, Object>> maps = selectMaps(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ErpSaleOutDO>()
                        .select("COALESCE(SUM(receipt_price), 0) AS total"));
        if (maps == null || maps.isEmpty() || maps.get(0).get("total") == null) {
            return java.math.BigDecimal.ZERO;
        }
        Object total = maps.get(0).get("total");
        return total instanceof java.math.BigDecimal ? (java.math.BigDecimal) total
                : new java.math.BigDecimal(total.toString());
    }

    default List<ErpSaleOutDO> selectApprovedListForBatchRebuild(Long id, Integer limit) {
        return selectList(new LambdaQueryWrapper<ErpSaleOutDO>()
                .eq(ErpSaleOutDO::getStatus, ErpAuditStatus.APPROVE.getStatus())
                .eq(id != null, ErpSaleOutDO::getId, id)
                .orderByAsc(ErpSaleOutDO::getId)
                .last("LIMIT " + limit));
    }

}
