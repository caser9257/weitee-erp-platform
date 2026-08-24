package cn.weitee.erp.module.erp.controller.admin.mrp.vo.workcenter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ErpWorkCenterSaveReqVO {

    private Long id;

    @NotBlank(message = "工作中心编码不能为空")
    private String centerCode;

    @NotBlank(message = "工作中心名称不能为空")
    private String centerName;

    private Long deptId;

    private Long managerUserId;

    private Boolean enableDeviceDispatch;

    @NotNull(message = "状态不能为空")
    private Integer status;

    private String remark;
}
