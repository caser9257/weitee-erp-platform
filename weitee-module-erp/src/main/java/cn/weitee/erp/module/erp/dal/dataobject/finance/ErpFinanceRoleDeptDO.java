package cn.weitee.erp.module.erp.dal.dataobject.finance;

import cn.weitee.erp.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@TableName("erp_finance_role_dept")
@KeySequence("erp_finance_role_dept_seq")
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class ErpFinanceRoleDeptDO extends BaseDO {

    @TableId
    private Long id;

    private Long roleId;

    private Long deptId;

    private Integer status;
}
