package cn.iocoder.yudao.module.erp.dal.dataobject.mrp;

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

import java.math.BigDecimal;

@TableName("erp_production_issue_voucher_item")
@KeySequence("erp_production_issue_voucher_item_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpProductionIssueVoucherItemDO extends BaseDO {

    @TableId
    private Long id;

    private Long voucherId;

    private Long issueItemId;

    private Long materialId;

    private Long warehouseId;

    private BigDecimal issueQty;

    private BigDecimal issueAmount;

    private String remark;

}
