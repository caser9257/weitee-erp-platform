package cn.weitee.erp.module.system.dal.dataobject.dict;

import cn.weitee.erp.framework.common.enums.CommonStatusEnum;
import cn.weitee.erp.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典数据�?
 *
 * @author weitee
 */
@TableName("system_dict_data")
@KeySequence("system_dict_data_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写�?
@Data
@EqualsAndHashCode(callSuper = true)
public class DictDataDO extends BaseDO {

    /**
     * 字典数据编号
     */
    @TableId
    private Long id;
    /**
     * 字典排序
     */
    private Integer sort;
    /**
     * 字典标签
     */
    private String label;
    /**
     * 字典�?
     */
    private String value;
    /**
     * 字典类型
     *
     * 冗余 {@link DictDataDO#getDictType()}
     */
    private String dictType;
    /**
     * 状�?
     *
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;
    /**
     * 颜色类型
     *
     * 对应�?element-ui �?default、primary、success、info、warning、danger
     */
    private String colorType;
    /**
     * css 样式
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String cssClass;
    /**
     * 备注
     */
    private String remark;

    /**
     * 业务属性JSON
     * 用于存储扩展的业务配置信息，如费用类型的特殊属�?
     */
    private String bizAttributes;

}
