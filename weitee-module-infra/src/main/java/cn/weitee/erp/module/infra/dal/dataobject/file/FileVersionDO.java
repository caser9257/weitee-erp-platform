package cn.weitee.erp.module.infra.dal.dataobject.file;

import cn.weitee.erp.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 文件版本 DO
 *
 * @author weitee
 */
@TableName("infra_file_version")
@KeySequence("infra_file_version_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileVersionDO extends BaseDO {

    @TableId
    private Long id;

    /**
     * 文件ID
     */
    private Long fileId;

    /**
     * 版本鍙?
     */
    private Integer version;

    /**
     * 文件鍚?
     */
    private String name;

    /**
     * 文件URL
     */
    private String url;

    /**
     * 文件大小
     */
    private Long size;

    /**
     * 文件类型
     */
    private String type;

    /**
     * 版本说明
     */
    private String description;

}
