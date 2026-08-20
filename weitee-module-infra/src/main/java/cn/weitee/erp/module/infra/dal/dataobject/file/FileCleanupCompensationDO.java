package cn.weitee.erp.module.infra.dal.dataobject.file;

import cn.weitee.erp.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 远端文件清理补偿记录。
 */
@TableName("infra_file_cleanup_compensation")
@KeySequence("infra_file_cleanup_compensation_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileCleanupCompensationDO extends BaseDO {

    @TableId
    private Long id;

    private Long configId;

    private String path;

    private String url;

    private String status;

    private Integer retryCount;

    private Integer maxRetryCount;

    private LocalDateTime nextRetryTime;

    private String lastError;

}
