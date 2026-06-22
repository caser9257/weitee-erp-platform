package cn.iocoder.yudao.module.erp.service.sale;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.ShipmentReleasePageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.ShipmentReleasePageVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.ShipmentReleaseResultVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.ShipmentReleaseStatsVO;

/**
 * 发货放行校验服务接口
 *
 * @author system
 */
public interface ErpShipmentReleaseService {

    /**
     * 校验销售订单是否可以发货
     *
     * @param orderId 销售订单编号
     * @return 放行校验结果
     */
    ShipmentReleaseResultVO checkRelease(Long orderId);

    /**
     * 提交财务审核
     *
     * @param orderId 销售订单编号
     * @param approverId 财务审核人
     */
    void submitFinanceApproval(Long orderId, Long approverId);

    /**
     * 财务审核通过
     *
     * @param orderId 销售订单编号
     * @param approverId 财务审核人
     * @param remark 审核意见
     */
    void approveFinance(Long orderId, Long approverId, String remark);

    /**
     * 财务审核驳回
     *
     * @param orderId 销售订单编号
     * @param approverId 财务审核人
     * @param reason 驳回原因
     */
    void rejectFinance(Long orderId, Long approverId, String reason);

    /**
     * 分页查询发货放行订单
     *
     * @param reqVO 分页请求参数
     * @return 发货放行订单分页结果
     */
    PageResult<ShipmentReleasePageVO> getReleasePage(ShipmentReleasePageReqVO reqVO);

    /**
     * 获取发货放行统计
     *
     * @return 发货放行统计信息
     */
    ShipmentReleaseStatsVO getReleaseStats();

}
