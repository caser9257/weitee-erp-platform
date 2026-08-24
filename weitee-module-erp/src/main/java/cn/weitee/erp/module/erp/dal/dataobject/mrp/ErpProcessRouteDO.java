package cn.weitee.erp.module.erp.dal.dataobject.mrp;

import cn.weitee.erp.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDate;

/** 工艺路线主数据。 */
@TableName("erp_process_route")
@KeySequence("erp_process_route_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpProcessRouteDO extends BaseDO {

    @TableId
    private Long id;
    private String routeCode;
    private String routeName;
    private Long productId;
    private String version;
    private Boolean defaultFlag;
    private Integer status;
    private LocalDate effectiveDate;
    private LocalDate expireDate;
    private String remark;
}
