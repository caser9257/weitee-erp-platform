package cn.weitee.erp.module.erp.controller.admin.mrp.vo.device;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ErpDeviceSaveReqVO {

    private Long id;

    @NotBlank(message = "设备编码不能为空")
    private String deviceCode;

    @NotBlank(message = "设备名称不能为空")
    private String deviceName;

    private Long workCenterId;

    private String specification;

    private Integer deviceStatus;

    private Integer maintenanceCycleDay;

    private Integer checkCycleDay;

    private LocalDate purchaseDate;

    private LocalDate startUseDate;

    private String manufacturer;

    private String remark;
}
