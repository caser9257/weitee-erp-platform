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

@TableName("erp_stock_assemble")
@KeySequence("erp_stock_assemble_seq")
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpStockAssembleDO extends BaseDO {

    @TableId
    private Long id;
    private String no;
    private String actionType;
    private Long warehouseId;
    private Long productId;
    private Long bomId;
    private BigDecimal count;
    private BigDecimal totalCost;
    private Integer status;
    private String remark;
}
