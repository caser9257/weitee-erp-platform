package cn.weitee.erp.module.erp.controller.admin.mrp.vo.device;

import cn.weitee.erp.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ErpDevicePageReqVO extends PageParam {

    private String deviceCode;

    private String deviceName;

    private Long workCenterId;

    private Integer deviceStatus;
}
