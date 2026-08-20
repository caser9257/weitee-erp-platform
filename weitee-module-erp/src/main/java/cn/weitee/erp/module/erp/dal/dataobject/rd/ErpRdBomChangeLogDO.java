package cn.weitee.erp.module.erp.dal.dataobject.rd;

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

@TableName("erp_rd_bom_change_log")
@KeySequence("erp_rd_bom_change_log_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpRdBomChangeLogDO extends BaseDO {

    @TableId
    private Long id;

    private Long bomId;

    private String changeType;

    private String changeDetail;

}
