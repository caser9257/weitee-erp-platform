package cn.weitee.erp.module.erp.controller.admin.mrp.vo.workcenter;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ErpWorkCenterRespVO {

    private Long id;
    private String centerCode;
    private String centerName;
    private Long deptId;
    private String deptName;
    private Long managerUserId;
    private String managerUserName;
    private Boolean enableDeviceDispatch;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;
}
