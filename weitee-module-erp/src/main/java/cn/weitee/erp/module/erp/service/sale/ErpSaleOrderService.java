package cn.weitee.erp.module.erp.service.sale;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.sale.vo.order.ErpSaleOrderBatchUpdateReqVO;
import cn.weitee.erp.module.erp.controller.admin.sale.vo.order.ErpSaleOrderBatchUpdateResultVO;
import cn.weitee.erp.module.erp.controller.admin.sale.vo.order.ErpSaleOrderPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.sale.vo.order.ErpSaleOrderSaveReqVO;
import cn.weitee.erp.module.erp.controller.admin.sale.vo.order.ErpSaleOrderUpdateStatusReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.sale.ErpSaleOrderAuditLogDO;
import cn.weitee.erp.module.erp.dal.dataobject.sale.ErpSaleOrderDO;
import cn.weitee.erp.module.erp.dal.dataobject.sale.ErpSaleOrderItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.sale.ErpSaleOrderRejectLogDO;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * ERP 销售订单 Service 接口
 *
 * @author WeTai
 */
public interface ErpSaleOrderService {

    /**
     * 创建销售订单
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createSaleOrder(@Valid ErpSaleOrderSaveReqVO createReqVO);

    /**
     * 更新销售订单
     *
     * @param updateReqVO 更新信息
     */
    void updateSaleOrder(@Valid ErpSaleOrderSaveReqVO updateReqVO);

    /**
     * 批量修改销售订单
     *
     * @param reqVO 批量修改信息
     * @return 修改结果
     */
    ErpSaleOrderBatchUpdateResultVO updateSaleOrderBatch(@Valid ErpSaleOrderBatchUpdateReqVO reqVO);

    /**
     * 更新销售订单的状态
     *
     * @param id 编号
     * @param status 状态
     */
    void updateSaleOrderStatus(@Valid ErpSaleOrderUpdateStatusReqVO reqVO);

    void updateSaleOrderStatusByBpm(Long orderId, String processInstanceId, Integer status, String reason);

    void rollbackSaleOrderStatusToDraftByBpm(Long orderId, String processInstanceId, String reason);

    /**
     * 更新销售订单的出库数量
     *
     * @param id 编号
     * @param outCountMap 出库数量 Map：key 销售订单项编号；value 出库数量
     */
    void updateSaleOrderOutCount(Long id, Map<Long, BigDecimal> outCountMap);

    /**
     * 更新销售订单的退货数量
     *
     * @param orderId 编号
     * @param returnCountMap 退货数量 Map：key 销售订单项编号；value 退货数量
     */
    void updateSaleOrderReturnCount(Long orderId, Map<Long, BigDecimal> returnCountMap);

    /**
     * 更新销售订单的收款金额和状态
     *
     * @param orderId 销售订单编号
     */
    void updateSaleOrderReceiptPrice(Long orderId);

    /**
     * 删除销售订单
     *
     * @param ids 编号数组
     */
    void deleteSaleOrder(List<Long> ids);

    /**
     * 获得销售订单
     *
     * @param id 编号
     * @return 销售订单
     */
    ErpSaleOrderDO getSaleOrder(Long id);

    List<ErpSaleOrderDO> getSaleOrderListByIds(Collection<Long> ids);

    List<ErpSaleOrderDO> getSaleOrderDisplayListByIds(Collection<Long> ids);

    List<ErpSaleOrderAuditLogDO> getSaleOrderAuditLogListByOrderId(Long orderId);

    List<ErpSaleOrderRejectLogDO> getSaleOrderRejectLogListByOrderId(Long orderId);

    /**
     * 校验销售订单，已经审核通过
     *
     * @param id 编号
     * @return 销售订单
     */
    ErpSaleOrderDO validateSaleOrder(Long id);

    /**
     * 获得销售订单分页
     *
     * @param pageReqVO 分页查询
     * @return 销售订单分页
     */
    PageResult<ErpSaleOrderDO> getSaleOrderPage(ErpSaleOrderPageReqVO pageReqVO);

    // ==================== 销售订单项 ====================

    /**
     * 获得销售订单项列表
     *
     * @param orderId 销售订单编号
     * @return 销售订单项列表
     */
    List<ErpSaleOrderItemDO> getSaleOrderItemListByOrderId(Long orderId);

    /**
     * 获得销售订单项 List
     *
     * @param orderIds 销售订单编号数组
     * @return 销售订单项 List
     */
    List<ErpSaleOrderItemDO> getSaleOrderItemListByOrderIds(Collection<Long> orderIds);

}
