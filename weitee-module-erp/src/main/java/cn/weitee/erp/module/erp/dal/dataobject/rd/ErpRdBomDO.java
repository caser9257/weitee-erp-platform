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

import java.time.LocalDateTime;

@TableName("erp_rd_bom")
@KeySequence("erp_rd_bom_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpRdBomDO extends BaseDO {

    @TableId
    private Long id;

    private String bomCode;

    private Long productId;

    private String version;

    private Integer status;

    private String processInstanceId;

    private Long publishedBomId;

    private LocalDateTime lastPublishedTime;

    /**
     * 源版本 BOM 编号（发起升版变更时指向被变更的旧版本，形成版本链；首建为 NULL）
     */
    private Long sourceBomId;

    private String remark;

}
