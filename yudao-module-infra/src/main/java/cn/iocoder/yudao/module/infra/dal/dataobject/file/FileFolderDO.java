package cn.iocoder.yudao.module.infra.dal.dataobject.file;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 文件夹 DO
 *
 * @author ruoyi-vue-pro
 */
@TableName("infra_file_folder")
@KeySequence("infra_file_folder_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileFolderDO extends BaseDO {

    @TableId
    private Long id;

    /**
     * 文件夹名称
     */
    private String name;

    /**
     * 父文件夹ID（0=根目录）
     */
    private Long parentId;

    /**
     * 文件夹路径
     */
    private String path;

    /**
     * 图标
     */
    private String icon;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 状态：0-正常 1-停用
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 子文件夹列表（非数据库字段）
     */
    @TableField(exist = false)
    private java.util.List<FileFolderDO> children;

}
