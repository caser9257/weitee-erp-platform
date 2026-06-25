package cn.weitee.erp.module.erp.dal.dataobject.finance;

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
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;

@TableName("erp_finance_asset_candidate")
@KeySequence("erp_finance_asset_candidate_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class ErpFinanceAssetCandidateDO extends BaseDO {

    @TableId
    private Long id;
    private Integer sourceType;
    private Long sourceBizId;
    private String sourceBizNo;
    private Long sourceItemId;
    private Long productId;
    private String assetName;
    private String categoryName;
    private BigDecimal amount;
    private LocalDate purchaseDate;
    private Long deptId;
    private Long responsibleUserId;
    private Integer status;
    private String remark;
}
