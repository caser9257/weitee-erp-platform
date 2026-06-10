package cn.iocoder.yudao.module.bpm.dal.dataobject.approval;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 审批模板 DO
 *
 * 预置的审批流程模板，用于快速创建审批场景和方案
 */
@TableName(value = "bpm_approval_template", autoResultMap = true)
@KeySequence("bpm_approval_template_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BpmApprovalTemplateDO extends BaseDO {

    @TableId
    private Long id;

    /**
     * 模板编码
     */
    private String code;

    /**
     * 模板名称
     */
    private String name;

    /**
     * 模板分类
     */
    private String category;

    /**
     * 模板图标
     */
    private String icon;

    /**
     * 模板描述
     */
    private String description;

    /**
     * 表单配置 JSON
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> formConfig;

    /**
     * 流程配置 JSON
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> flowConfig;

    /**
     * 通知配置 JSON
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> notifyConfig;

    /**
     * 使用次数
     */
    private Long useCount;

    /**
     * 状态，0-正常 1-停用
     */
    private Integer status;

    /**
     * 排序
     */
    private Integer sort;

}
