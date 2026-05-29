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
import java.time.LocalDateTime;

/**
 * 采购入库 IQC 轮次记录 DO
 */
@TableName("erp_purchase_in_quality_round")
@KeySequence("erp_purchase_in_quality_round_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class ErpPurchaseInQualityRoundDO extends BaseDO {

    @TableId
    private Long id;

    /**
     * 质检单编号
     */
    private Long qualityId;

    /**
     * 质检单明细编号
     */
    private Long qualityItemId;

    /**
     * 采购入库明细编号
     */
    private Long purchaseInItemId;

    /**
     * 轮次号
     */
    private Integer roundNo;

    /**
     * 轮次类型
     */
    private Integer roundType;

    /**
     * 抽检数量
     */
    private BigDecimal sampleCount;

    /**
     * 本轮合格数
     */
    private BigDecimal passCount;

    /**
     * 本轮不合格数
     */
    private BigDecimal rejectCount;

    /**
     * 本轮结果
     */
    private Integer result;

    /**
     * 检验人
     */
    private Long checkerUserId;

    /**
     * 检验时间
     */
    private LocalDateTime checkTime;

    /**
     * 备注
     */
    private String remark;

}
