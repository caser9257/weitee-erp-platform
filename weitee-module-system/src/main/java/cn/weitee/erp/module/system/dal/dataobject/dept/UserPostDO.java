package cn.weitee.erp.module.system.dal.dataobject.dept;

import cn.weitee.erp.framework.mybatis.core.dataobject.BaseDO;
import cn.weitee.erp.module.system.dal.dataobject.user.AdminUserDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户和岗位关联
 *
 * @author ruoyi
 */
@TableName("system_user_post")
@KeySequence("system_user_post_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
public class UserPostDO extends BaseDO {

    /**
     * 自增主键
     */
    @TableId
    private Long id;
    /**
     * 用户 ID
     *
     * 关联 {@link AdminUserDO#getId()}
     */
    private Long userId;
    /**
     * 角色 ID
     *
     * 关联 {@link PostDO#getId()}
     */
    private Long postId;
    /**
     * 是否主岗
     */
    private Boolean isPrimary;
    /**
     * 任职开始时间
     */
    private LocalDateTime startDate;
    /**
     * 任职结束时间
     */
    private LocalDateTime endDate;
    /**
     * 备注
     */
    private String remark;

}
