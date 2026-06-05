package cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model;

import cn.iocoder.yudao.framework.common.core.KeyValue;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.simple.BpmSimpleModelNodeVO;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmAutoApproveTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmDingTalkTemplateSourceTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmInternalNotificationTemplateSourceTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmModelFormTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmModelTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmNotificationPublishCheckModeEnum;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmNotificationReceiverTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmNotificationSceneEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * BPM 流程 MetaInfo DTO
 *
 * <p>主要用于 {@code Model#setMetaInfo(String)} 的存储，同时和
 * {@link cn.iocoder.yudao.module.bpm.dal.dataobject.definition.BpmProcessDefinitionInfoDO}
 * 保持字段一致。</p>
 */
@Data
public class BpmModelMetaInfoVO {

    @Schema(description = "流程图标", example = "https://www.iocoder.cn/yudao.jpg")
    @URL(message = "流程图标格式不正确")
    private String icon;

    @Schema(description = "流程描述", example = "我是描述")
    private String description;

    @Schema(description = "流程类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @InEnum(BpmModelTypeEnum.class)
    @NotNull(message = "流程类型不能为空")
    private Integer type;

    @Schema(description = "表单类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @InEnum(BpmModelFormTypeEnum.class)
    @NotNull(message = "表单类型不能为空")
    private Integer formType;

    @Schema(description = "表单编号", example = "1024")
    private Long formId;

    @Schema(description = "自定义表单提交路径", example = "/bpm/oa/leave/create")
    private String formCustomCreatePath;

    @Schema(description = "自定义表单查看路径", example = "/bpm/oa/leave/view")
    private String formCustomViewPath;

    @Schema(description = "是否可见", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @NotNull(message = "是否可见不能为空")
    private Boolean visible;

    @Schema(description = "可发起用户编号数组", example = "[1,2,3]")
    private List<Long> startUserIds;

    @Schema(description = "可发起部门编号数组", example = "[2,4,6]")
    private List<Long> startDeptIds;

    @Schema(description = "可管理用户编号数组", requiredMode = Schema.RequiredMode.REQUIRED, example = "[2,4,6]")
    @NotEmpty(message = "可管理用户编号数组不能为空")
    private List<Long> managerUserIds;

    @Schema(description = "是否存在未发布变更", example = "true")
    private Boolean hasUnpublishedChanges;

    @Schema(description = "排序", example = "1")
    private Long sort;

    @Schema(description = "允许撤销审批中的申请", example = "true")
    private Boolean allowCancelRunningProcess;

    @Schema(description = "允许审批人撤回任务", example = "false")
    private Boolean allowWithdrawTask;

    @Schema(description = "流程 ID 规则", example = "{}")
    private ProcessIdRule processIdRule;

    @Schema(description = "自动去重类型", example = "1")
    @InEnum(BpmAutoApproveTypeEnum.class)
    private Integer autoApprovalType;

    @Schema(description = "标题设置", example = "{}")
    private TitleSetting titleSetting;

    @Schema(description = "摘要设置", example = "{}")
    private SummarySetting summarySetting;

    @Schema(description = "流程前置通知设置", example = "{}")
    private HttpRequestSetting processBeforeTriggerSetting;

    @Schema(description = "流程后置通知设置", example = "{}")
    private HttpRequestSetting processAfterTriggerSetting;

    @Schema(description = "任务前置通知设置", example = "{}")
    private HttpRequestSetting taskBeforeTriggerSetting;

    @Schema(description = "任务后置通知设置", example = "{}")
    private HttpRequestSetting taskAfterTriggerSetting;

    @Schema(description = "通知策略设置", example = "{}")
    @Valid
    private NotificationPolicySetting notificationPolicySetting;

    @Schema(description = "自定义打印模板设置", example = "{}")
    @Valid
    private PrintTemplateSetting printTemplateSetting;

    @Schema(description = "流程 ID 规则")
    @Data
    @Valid
    public static class ProcessIdRule {

        @Schema(description = "是否启用", example = "false")
        @NotNull(message = "是否启用不能为空")
        private Boolean enable;

        @Schema(description = "前缀", example = "XX")
        private String prefix;

        @Schema(description = "中缀", example = "20250120")
        private String infix;

        @Schema(description = "后缀", example = "YY")
        private String postfix;

        @Schema(description = "序列长度", example = "5")
        @NotNull(message = "序列长度不能为空")
        private Integer length;

    }

    @Schema(description = "标题设置")
    @Data
    @Valid
    public static class TitleSetting {

        @Schema(description = "是否自定义", example = "false")
        @NotNull(message = "是否自定义不能为空")
        private Boolean enable;

        @Schema(description = "标题", example = "流程标题")
        private String title;

    }

    @Schema(description = "摘要设置")
    @Data
    @Valid
    public static class SummarySetting {

        @Schema(description = "是否自定义", example = "false")
        @NotNull(message = "是否自定义不能为空")
        private Boolean enable;

        @Schema(description = "摘要字段数组", example = "[]")
        private List<String> summary;

    }

    @Schema(description = "HTTP 请求通知设置", example = "{}")
    @Data
    public static class HttpRequestSetting {

        @Schema(description = "请求路径", example = "http://127.0.0.1")
        @NotEmpty(message = "请求 URL 不能为空")
        @URL(message = "请求 URL 格式不正确")
        private String url;

        @Schema(description = "请求头参数设置", example = "[]")
        @Valid
        private List<BpmSimpleModelNodeVO.HttpRequestParam> header;

        @Schema(description = "请求体参数设置", example = "[]")
        @Valid
        private List<BpmSimpleModelNodeVO.HttpRequestParam> body;

        /**
         * 请求返回处理设置，用于修改流程表单值。
         *
         * <p>key 表示要修改的流程表单字段名，value 表示接口返回字段名。</p>
         */
        @Schema(description = "请求返回处理设置", example = "[]")
        private List<KeyValue<String, String>> response;

    }

    @Schema(description = "通知策略设置")
    @Data
    public static class NotificationPolicySetting {

        @Schema(description = "是否启用", example = "false")
        @NotNull(message = "通知策略开关不能为空")
        private Boolean enable;

        @Schema(description = "发布校验模式", example = "STRICT")
        @InEnum(BpmNotificationPublishCheckModeEnum.class)
        private String publishCheckMode;

        @Schema(description = "场景策略列表")
        @Valid
        private List<ScenePolicy> scenes;

    }

    @Schema(description = "通知场景策略")
    @Data
    public static class ScenePolicy {

        @Schema(description = "场景编码", example = "TASK_ASSIGNED")
        @InEnum(BpmNotificationSceneEnum.class)
        private String sceneCode;

        @Schema(description = "是否启用", example = "false")
        @NotNull(message = "通知场景开关不能为空")
        private Boolean enabled;

        @Schema(description = "接收对象类型", example = "ASSIGNEE")
        @InEnum(BpmNotificationReceiverTypeEnum.class)
        private String receiverType;

        @Schema(description = "站内信配置", example = "{}")
        @Valid
        private InternalTemplateSetting internalMessage;

        @Schema(description = "钉钉通知配置", example = "{}")
        @Valid
        private DingTalkTemplateSetting dingTalk;

    }

    @Schema(description = "站内信模板配置")
    @Data
    public static class InternalTemplateSetting {

        @Schema(description = "是否启用", example = "false")
        @NotNull(message = "站内信开关不能为空")
        private Boolean enabled;

        @Schema(description = "文案来源", example = "DEFAULT")
        @InEnum(BpmInternalNotificationTemplateSourceTypeEnum.class)
        private String sourceType;

        @Schema(description = "标题", example = "任务到达提醒")
        private String title;

        @Schema(description = "正文", example = "您收到一条新的审批任务")
        private String content;

    }

    @Schema(description = "钉钉通知模板配置")
    @Data
    public static class DingTalkTemplateSetting {

        @Schema(description = "是否启用", example = "false")
        @NotNull(message = "钉钉开关不能为空")
        private Boolean enabled;

        @Schema(description = "模板来源", example = "RESERVED")
        @InEnum(BpmDingTalkTemplateSourceTypeEnum.class)
        private String sourceType;

        @Schema(description = "模板编码", example = "PROC_TASK_ASSIGNED")
        private String templateCode;

    }

    @Schema(description = "自定义打印模板设置")
    @Data
    public static class PrintTemplateSetting {

        @Schema(description = "是否自定义打印模板", example = "false")
        @NotNull(message = "是否自定义打印模板不能为空")
        private Boolean enable;

        @Schema(description = "打印模板", example = "<p></p>")
        private String template;

    }

}
