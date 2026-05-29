package cn.iocoder.yudao.module.erp.dal.dataobject.rd;

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

    private Long publishedBomId;

    private LocalDateTime lastPublishedTime;

    private String remark;

}
