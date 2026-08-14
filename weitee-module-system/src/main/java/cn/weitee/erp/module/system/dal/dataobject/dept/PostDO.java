package cn.weitee.erp.module.system.dal.dataobject.dept;

import cn.weitee.erp.framework.common.enums.CommonStatusEnum;
import cn.weitee.erp.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 岗位琛?
 *
 * @author weitee
 */
@TableName("system_post")
@KeySequence("system_post_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写銆?
@Data
@EqualsAndHashCode(callSuper = true)
public class PostDO extends BaseDO {

    /**
     * 岗位序号
     */
    @TableId
    private Long id;
    /**
     * 岗位名称
     */
    private String name;
    /**
     * 岗位编码
     */
    private String code;
    /**
     * 岗位层级
     */
    private String level;
    /**
     * 所属部闂?
     */
    private Long deptId;
    /**
     * 编制人数
     */
    private Integer staffQuota;
    /**
     * 是否关键岗位
     */
    private Boolean keyPosition;
    /**
     * 是否允许兼岗
     */
    private Boolean allowPartTime;
    /**
     * 岗位说明
     */
    private String jobDescription;
    /**
     * 岗位排序
     */
    private Integer sort;
    /**
     * 状鎬?
     *
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;

}
