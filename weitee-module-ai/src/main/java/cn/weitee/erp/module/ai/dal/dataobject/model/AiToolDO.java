package cn.weitee.erp.module.ai.dal.dataobject.model;

import cn.weitee.erp.framework.mybatis.core.dataobject.BaseDO;
import cn.weitee.erp.module.ai.tool.function.DirectoryListToolFunction;
import cn.weitee.erp.module.ai.tool.function.WeatherQueryToolFunction;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * AI 工具 DO
 *
 * @author WeTai
 */
@TableName("ai_tool")
@KeySequence("ai_tool_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写�?
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiToolDO extends BaseDO {

    /**
     * 工具编号
     */
    @TableId
    private Long id;
    /**
     * 工具名称
     *
     * 对应 Bean 的名字，例如说：
     * 1. {@link DirectoryListToolFunction} �?Bean 名字�?directory_list
     * 2. {@link WeatherQueryToolFunction} �?Bean 名字�?weather_query
     */
    private String name;
    /**
     * 工具描述
     */
    private String description;
    /**
     * 状�?
     *
     * 枚举 {@link cn.weitee.erp.framework.common.enums.CommonStatusEnum}
     */
    private Integer status;

}