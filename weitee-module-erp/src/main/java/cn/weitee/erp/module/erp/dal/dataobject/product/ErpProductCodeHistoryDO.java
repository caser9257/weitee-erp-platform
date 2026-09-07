package cn.weitee.erp.module.erp.dal.dataobject.product;

import cn.weitee.erp.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 物料编码沿革 DO
 *
 * 改码追溯：每次物料编码变更写一行（append-only，业务不删除不更新）；
 * old_code 全局唯一 = 历史旧码占用不释放，禁止新物料复用旧码。
 */
@TableName("erp_product_code_history")
@KeySequence("erp_product_code_history_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpProductCodeHistoryDO extends BaseDO {

    @TableId
    private Long id;

    /**
     * 物料 ID（改码不改主键，单据/BOM 引用不受影响）
     */
    private Long productId;

    /**
     * 变更前编码（旧码）
     */
    private String oldCode;

    /**
     * 变更后编码（新码）
     */
    private String newCode;

    /**
     * 关联审批流程实例 ID（直改路径为 NULL）
     */
    private String processInstanceId;

    /**
     * 变更原因（审批理由）
     */
    private String reason;

}
