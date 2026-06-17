package cn.iocoder.yudao.module.erp.dal.dataobject.stock;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ERP 库存盘点单 DO
 *
 * @author 芋道源码
 */
@TableName("erp_stock_check")
@KeySequence("erp_stock_check_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpStockCheckDO extends BaseDO {

    /**
     * 盘点编号
     */
    @TableId
    private Long id;
    /**
     * 盘点单号
     */
    private String no;
    /**
     * 盘点时间
     */
    private LocalDateTime checkTime;
    /**
     * 合计数量
     */
    private BigDecimal totalCount;
    /**
     * 合计金额，单位：元
     */
    private BigDecimal totalPrice;
    /**
     * 状态
     *
     * 枚举 {@link cn.iocoder.yudao.module.erp.enums.stock.ErpStockCheckStatusEnum}
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;
    /**
     * 附件 URL
     */
    private String fileUrl;
    /**
     * 快照时间（盘点开始时的库存快照时间）
     */
    private LocalDateTime snapshotTime;
    /**
     * 是否盲盘模式
     */
    private Boolean blindCount;
    /**
     * 是否年末盘点
     */
    private Boolean yearEndFlag;
    /**
     * 生成的凭证ID
     *
     * 关联 {@link cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceVoucherDO#getId()}
     */
    private Long voucherId;

}