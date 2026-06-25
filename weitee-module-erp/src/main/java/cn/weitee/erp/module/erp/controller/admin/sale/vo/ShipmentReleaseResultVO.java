package cn.weitee.erp.module.erp.controller.admin.sale.vo;

import lombok.Data;

import java.util.List;

/**
 * 发货放行校验结果 VO
 *
 * @author system
 */
@Data
public class ShipmentReleaseResultVO {

    /**
     * 是否可放行
     */
    private boolean releasable;

    /**
     * 放行状态
     *
     * 枚举值：PENDING, BLOCKED, RELEASED, FINANCE_REVIEW
     */
    private String releaseStatus;

    /**
     * 阻塞原因列表
     */
    private List<String> blockerReasons;

    /**
     * 待办角色
     */
    private String pendingRole;

    /**
     * 校验详情列表
     */
    private List<ReleaseCheckDetailVO> details;

    /**
     * 校验详情 VO
     */
    @Data
    public static class ReleaseCheckDetailVO {

        /**
         * 校验项名称
         */
        private String checkItem;

        /**
         * 是否通过
         */
        private boolean passed;

        /**
         * 校验说明
         */
        private String message;

    }

}
