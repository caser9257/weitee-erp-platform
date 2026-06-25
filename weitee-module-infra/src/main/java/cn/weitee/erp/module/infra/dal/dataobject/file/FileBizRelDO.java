package cn.weitee.erp.module.infra.dal.dataobject.file;

import cn.weitee.erp.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 文件-业务关联 DO
 *
 * @author ruoyi-vue-pro
 */
@TableName("infra_file_biz_rel")
@KeySequence("infra_file_biz_rel_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileBizRelDO extends BaseDO {

    @TableId
    private Long id;

    /**
     * 文件ID
     */
    private Long fileId;

    /**
     * 业务类型
     */
    private String bizType;

    /**
     * 业务ID
     */
    private Long bizId;

    /**
     * 业务单号
     */
    private String bizNo;

    /**
     * 备注
     */
    private String remark;

}
