package cn.weitee.erp.module.erp.dal.dataobject.finance;

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
import lombok.experimental.Accessors;

@TableName("erp_finance_subject")
@KeySequence("erp_finance_subject_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class ErpFinanceSubjectDO extends BaseDO {

    @TableId
    private Long id;

    private Long ledgerId;

    private Long parentId;

    private String subjectCode;

    private String subjectName;

    private Integer subjectType;

    private Integer balanceDirection;

    private Boolean leaf;

    private Integer status;

    private Integer sort;

    private String remark;

}
