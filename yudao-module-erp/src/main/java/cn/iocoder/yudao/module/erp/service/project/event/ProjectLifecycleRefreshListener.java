package cn.iocoder.yudao.module.erp.service.project.event;

import cn.iocoder.yudao.module.erp.service.project.ErpProjectLifecycleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * 项目生命周期刷新事件监听器
 *
 * 异步处理项目生命周期状态刷新，避免阻塞主业务流程
 *
 * @author ruoyi-vue-pro
 */
@Component
@Slf4j
public class ProjectLifecycleRefreshListener {

    @Resource
    private ErpProjectLifecycleService projectLifecycleService;

    /**
     * 异步处理项目生命周期刷新
     *
     * 使用 @Async 注解实现异步执行，不阻塞主业务线程
     * refreshProjectStatus() 已有乐观锁保护，天然支持幂等
     */
    @EventListener
    @Async("asyncExecutor")
    public void handleRefreshEvent(ProjectLifecycleRefreshEvent event) {
        Long projectId = event.getProjectId();
        String source = event.getSource();
        
        try {
            log.debug("[handleRefreshEvent] 开始异步刷新项目生命周期，projectId={}, source={}", projectId, source);
            projectLifecycleService.refreshProjectStatus(projectId);
            log.debug("[handleRefreshEvent] 异步刷新项目生命周期完成，projectId={}", projectId);
        } catch (Exception e) {
            log.warn("[handleRefreshEvent] 异步刷新项目生命周期失败，projectId={}, source={}", projectId, source, e);
        }
    }
}
