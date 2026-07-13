package cn.weitee.erp.module.erp.dal.dataobject.stock;

import cn.weitee.erp.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@TableName("erp_stock_assemble_item")
@KeySequence("erp_stock_assemble_item_seq")
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpStockAssembleItemDO extends BaseDO {

    @TableId
    private Long id;
    private Long assembleId;
    private Long productId;
    private BigDecimal count;
    private BigDecimal unitCost;
    private Integer stockDirection;
    private String remark;
}
