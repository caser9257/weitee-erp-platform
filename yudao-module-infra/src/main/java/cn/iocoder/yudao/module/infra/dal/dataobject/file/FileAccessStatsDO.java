package cn.iocoder.yudao.module.infra.dal.dataobject.file;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDate;

/**
 * 文件访问统计 DO
 *
 * @author ruoyi-vue-pro
 */
@TableName("infra_file_access_stats")
@KeySequence("infra_file_access_stats_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileAccessStatsDO extends BaseDO {

    @TableId
    private Long id;

    /**
     * 文件ID
     */
    private Long fileId;

    /**
     * 统计日期
     */
    private LocalDate statsDate;

    /**
     * 查看次数
     */
    private Integer viewCount;

    /**
     * 下载次数
     */
    private Integer downloadCount;

    /**
     * 独立访客数
     */
    private Integer uniqueVisitorCount;

}
