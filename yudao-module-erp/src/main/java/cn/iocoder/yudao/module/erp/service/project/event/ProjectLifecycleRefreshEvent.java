package cn.iocoder.yudao.module.erp.service.project.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 项目生命周期刷新事件
 *
 * 用于异步触发项目生命周期状态刷新，避免阻塞主业务流程
 *
 * @author ruoyi-vue-pro
 */
@Getter
public class ProjectLifecycleRefreshEvent extends ApplicationEvent {

    /**
     * 项目ID
     */
    private final Long projectId;

    /**
     * 触发来源（用于日志追踪）
     */
    private final String source;

    public ProjectLifecycleRefreshEvent(Object source, Long projectId, String triggerSource) {
        super(source);
        this.projectId = projectId;
        this.source = triggerSource;
    }

    public ProjectLifecycleRefreshEvent(Long projectId, String triggerSource) {
        this(triggerSource, projectId, triggerSource);
    }
}
