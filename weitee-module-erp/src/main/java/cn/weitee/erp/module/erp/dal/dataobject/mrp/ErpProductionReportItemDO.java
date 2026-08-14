package cn.weitee.erp.module.erp.dal.dataobject.mrp;

import cn.weitee.erp.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;

/** 生产报工明细。 */
@TableName("erp_production_report_item")
@KeySequence("erp_production_report_item_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpProductionReportItemDO extends BaseDO {

    @TableId
    private Long id;
    private Long reportId;
    private Long productionOrderStepId;
    private Long deviceId;
    private Long workerUserId;
    private BigDecimal reportedQty;
    private BigDecimal qualifiedQty;
    private BigDecimal scrapQty;
    private BigDecimal workHour;
    private String batchNo;
    private String remark;
}
