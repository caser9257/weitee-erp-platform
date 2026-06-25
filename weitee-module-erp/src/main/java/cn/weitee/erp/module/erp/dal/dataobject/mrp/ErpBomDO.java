package cn.weitee.erp.module.erp.dal.dataobject.mrp;

import cn.weitee.erp.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

@TableName("erp_bom")
@KeySequence("erp_bom_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpBomDO extends BaseDO {

    @TableId
    private Long id;

    private String bomCode;

    private Long productId;

    private String version;

    private Integer status;

    private Long sourceRdBomId;

    private String remark;

}
