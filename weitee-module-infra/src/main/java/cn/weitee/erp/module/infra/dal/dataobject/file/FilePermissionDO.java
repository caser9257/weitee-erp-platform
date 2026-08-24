package cn.weitee.erp.module.infra.dal.dataobject.file;

import cn.weitee.erp.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 文件权限 DO
 *
 * @author weitee
 */
@TableName("infra_file_permission")
@KeySequence("infra_file_permission_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FilePermissionDO extends BaseDO {

    @TableId
    private Long id;

    /**
     * 文件ID
     */
    private Long fileId;

    /**
     * 授权类型：USER-用户, ROLE-角色, DEPT-部门
     */
    private String grantType;

    /**
     * 授权目标ID（用户ID/角色ID/部门ID锛?
     */
    private Long grantTargetId;

    /**
     * 授权目标名称
     */
    private String grantTargetName;

    /**
     * 权限类型：VIEW-查看, DOWNLOAD-下载, EDIT-编辑, DELETE-删除, SHARE-分享
     * 多个权限用逗号分隔
     */
    private String permissions;

    /**
     * 过期时间（null表示永不过期锛?
     */
    private LocalDateTime expireTime;

    /**
     * 授权人ID
     */
    private Long grantUserId;

    /**
     * 授权人名绉?
     */
    private String grantUserName;

    /**
     * 备注
     */
    private String remark;

}
