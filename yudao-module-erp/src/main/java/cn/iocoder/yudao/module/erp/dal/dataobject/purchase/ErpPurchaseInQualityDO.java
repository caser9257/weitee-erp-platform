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

@TableName("erp_purchase_in_quality")
@KeySequence("erp_purchase_in_quality_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class ErpPurchaseInQualityDO extends BaseDO {

    @TableId
    private Long id;

    private String no;

    private Long purchaseInId;

    private String purchaseInNo;

    private Integer status;

    private Integer result;

    /**
     * 当前轮次
     */
    private Integer currentRoundNo;

    /**
     * 是否需要复检
     */
    private Boolean recheckRequired;

    /**
     * 复检原因
     */
    private String recheckReason;

    /**
     * 复检发起人
     */
    private Long recheckApplyUserId;

    /**
     * 复检发起时间
     */
    private LocalDateTime recheckApplyTime;

    /**
     * 抽检方案编号
     */
    private Long samplingSchemeId;

    /**
     * 抽检方案名称快照
     */
    private String samplingSchemeName;

    /**
     * 抽检方案类型
     */
    private Integer samplingSchemeType;

    /**
     * 抽检比例快照
     */
    private BigDecimal samplingRatio;

    /**
     * 固定抽检数快照
     */
    private BigDecimal samplingFixedCount;

    /**
     * 最小抽检数快照
     */
    private BigDecimal minSampleCount;

    /**
     * 最大抽检数快照
     */
    private BigDecimal maxSampleCount;

    /**
     * 褰撳墠鎸囨淳璐ㄦ浜?     */
    private Long assignedCheckerUserId;

    /**
     * 鎸囨淳鏃堕棿
     */
    private LocalDateTime assignedCheckerTime;

    private Long checkerUserId;

    private LocalDateTime checkTime;

    private String remark;

    private BigDecimal passCount;

    private BigDecimal rejectCount;

}
