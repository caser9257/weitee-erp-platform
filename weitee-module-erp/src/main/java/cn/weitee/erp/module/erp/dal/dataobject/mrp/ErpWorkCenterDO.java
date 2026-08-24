package cn.weitee.erp.module.erp.dal.dataobject.mrp;

import cn.weitee.erp.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/** 工作中心主数据。 */
@TableName("erp_work_center")
@KeySequence("erp_work_center_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpWorkCenterDO extends BaseDO {

    @TableId
    private Long id;
    private String centerCode;
    private String centerName;
    private Long deptId;
    private Long managerUserId;
    private Boolean enableDeviceDispatch;
    private Integer status;
    private String remark;
}
