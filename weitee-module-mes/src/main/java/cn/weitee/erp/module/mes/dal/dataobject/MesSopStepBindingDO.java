package cn.weitee.erp.module.mes.dal.dataobject;

import cn.weitee.erp.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/** MES SOP 工序绑定。 */
@TableName("mes_sop_step_binding")
@KeySequence("mes_sop_step_binding_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesSopStepBindingDO extends BaseDO {

    @TableId
    private Long id;
    private Long sopId;
    private Long routeStepId;
}
