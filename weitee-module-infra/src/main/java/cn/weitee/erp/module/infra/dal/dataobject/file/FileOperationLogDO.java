package cn.weitee.erp.module.infra.dal.dataobject.file;

import cn.weitee.erp.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 文件操作日志 DO
 *
 * @author weitee
 */
@TableName("infra_file_operation_log")
@KeySequence("infra_file_operation_log_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileOperationLogDO extends BaseDO {

    @TableId
    private Long id;

    /**
     * 文件ID
     */
    private Long fileId;

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 操作类型：UPLOAD-上传, DOWNLOAD-下载, DELETE-删除, VIEW-查看, RESTORE-恢复, ROLLBACK-回滚
     */
    private String operation;

    /**
     * 操作人ID
     */
    private Long userId;

    /**
     * 操作人名�?
     */
    private String userName;

    /**
     * 操作IP
     */
    private String ip;

    /**
     * 浏览器信�?
     */
    private String userAgent;

    /**
     * 操作说明
     */
    private String description;

    /**
     * 操作结果�?-成功, 1-失败
     */
    private Integer result;

    /**
     * 失败原因
     */
    private String failReason;

}
