package cn.weitee.erp.module.erp.controller.admin.mrp.vo.device;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ErpDeviceRespVO {

    private Long id;
    private String deviceCode;
    private String deviceName;
    private Long workCenterId;
    private String workCenterName;
    private String specification;
    private Integer deviceStatus;
    private Integer maintenanceCycleDay;
    private Integer checkCycleDay;
    private LocalDate purchaseDate;
    private LocalDate startUseDate;
    private String manufacturer;
    private String remark;
    private LocalDateTime createTime;
}
