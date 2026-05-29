package cn.iocoder.yudao.module.erp.dal.dataobject.purchase;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 采购入库 IQC 不良明细 DO
 */
@TableName("erp_purchase_in_quality_defect")
@KeySequence("erp_purchase_in_quality_defect_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class ErpPurchaseInQualityDefectDO extends BaseDO {

    @TableId
    private Long id;

    /**
     * 质检单编号
     */
    private Long qualityId;

    /**
     * 轮次记录编号
     */
    private Long roundId;

    /**
     * 质检单明细编号
     */
    private Long qualityItemId;

    /**
     * 采购入库明细编号
     */
    private Long purchaseInItemId;

    /**
     * 不良原因编号
     */
    private Long defectReasonId;

    /**
     * 不良原因名称快照
     */
    private String defectReasonName;

    /**
     * 不良数量
     */
    private BigDecimal defectCount;

    /**
     * 备注
     */
    private String remark;

}
