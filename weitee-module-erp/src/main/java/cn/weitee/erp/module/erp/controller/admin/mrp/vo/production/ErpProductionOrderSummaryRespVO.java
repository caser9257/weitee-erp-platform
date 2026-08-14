package cn.weitee.erp.module.erp.controller.admin.mrp.vo.production;

import lombok.Data;

@Data
public class ErpProductionOrderSummaryRespVO {

    /** 工单总数 */
    private Long total;

    /** 已创建 */
    private Long created;

    /** 已下达 */
    private Long released;

    /** 已完工 */
    private Long finished;

    /** 已关闭 */
    private Long closed;

}
