package cn.weitee.erp.module.system.api.dept.dto;

import cn.weitee.erp.framework.common.enums.CommonStatusEnum;
import lombok.Data;

/**
 * 部门 Response DTO
 *
 * @author WeTai
 */
@Data
public class DeptRespDTO {

    /**
     * 部门编号
     */
    private Long id;
    /**
     * 部门名称
     */
    private String name;
    /**
     * 父部门编号
     */
    private Long parentId;
    /**
     * 负责人的用户编号
     */
    private Long leaderUserId;
    /**
     * 部门状态
     *
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;
    /**
     * 成本类型
     *
     * 枚举值：1-生产部门, 2-销售部门, 3-管理部门, 4-研发部门
     */
    private Integer costType;

}
