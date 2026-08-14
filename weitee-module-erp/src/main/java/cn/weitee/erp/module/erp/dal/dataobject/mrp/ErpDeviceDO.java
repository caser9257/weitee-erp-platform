package cn.weitee.erp.module.erp.dal.dataobject.mrp;

import cn.weitee.erp.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDate;

/** 制造设备台账。 */
@TableName("erp_device")
@KeySequence("erp_device_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpDeviceDO extends BaseDO {

    @TableId
    private Long id;
    private String deviceCode;
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
