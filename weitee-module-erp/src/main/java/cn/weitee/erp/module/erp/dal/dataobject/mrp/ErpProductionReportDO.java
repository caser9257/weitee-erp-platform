package cn.weitee.erp.module.erp.dal.dataobject.mrp;

import cn.weitee.erp.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/** 生产报工主单。 */
@TableName("erp_production_report")
@KeySequence("erp_production_report_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpProductionReportDO extends BaseDO {

    @TableId
    private Long id;
    private String reportNo;
    private Long productionOrderId;
    private LocalDateTime reportDate;
    private Long reportUserId;
    private Integer reportType;
    private String batchNo;
    private Integer status;
    private String remark;
}
