package cn.weitee.erp.module.erp.dal.dataobject.mrp;

import cn.weitee.erp.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@TableName("erp_production_issue_batch")
@KeySequence("erp_production_issue_batch_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpProductionIssueBatchDO extends BaseDO {

    @TableId
    private Long id;

    private Long issueItemId;

    private Long stockBatchId;

    private String batchNo;

    private BigDecimal issueQty;

    private LocalDateTime inboundTime;

    private LocalDate produceDate;

    private LocalDate expireDate;

}
