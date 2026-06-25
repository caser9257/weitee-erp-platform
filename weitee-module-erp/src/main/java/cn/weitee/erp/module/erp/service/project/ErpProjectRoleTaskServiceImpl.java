package cn.weitee.erp.module.erp.service.project;

import cn.hutool.core.util.StrUtil;
import cn.weitee.erp.module.erp.dal.dataobject.project.ErpProjectDO;
import cn.weitee.erp.module.erp.dal.dataobject.project.ErpProjectRoleTaskDO;
import cn.weitee.erp.module.erp.dal.mysql.project.ErpProjectMapper;
import cn.weitee.erp.module.erp.dal.mysql.project.ErpProjectRoleTaskMapper;
import cn.weitee.erp.module.erp.enums.ErpProjectRoleCodeConstants;
import cn.weitee.erp.module.erp.enums.ErpProjectRoleTaskStatusConstants;
import cn.weitee.erp.module.erp.enums.ErpProjectRoleTaskTypeConstants;
import cn.weitee.erp.module.system.service.notify.NotifySendService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PROJECT_NOT_EXISTS;

@Service
@Validated
@Slf4j
public class ErpProjectRoleTaskServiceImpl implements ErpProjectRoleTaskService {

    private static final String PC_SUMMARY = "销售订单审批已通过，请 PC 确认计划交付节点";
    private static final String DEFAULT_MC_SUMMARY = "MRP 已生成采购/生产建议，请 MC 确认物料准备策略";
    private static final String PROJECT_ROLE_TASK_NOTIFY_TEMPLATE_CODE = "erp_project_role_task_assigned";
    private static final String PROJECT_FOLLOW_UP_URL = "/pmo/project/follow-up";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Resource
    private ErpProjectRoleTaskMapper erpProjectRoleTaskMapper;
    @Resource
    private ErpProjectMapper erpProjectMapper;
    @Resource
    private NotifySendService notifySendService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createOrRefreshPcTask(Long projectId, Long saleOrderId, LocalDate dueDate) {
        ErpProjectDO project = getRequiredProject(projectId);
        ErpProjectRoleTaskDO task = erpProjectRoleTaskMapper.selectTodoTask(projectId,
                ErpProjectRoleCodeConstants.PC, ErpProjectRoleTaskTypeConstants.SALE_APPROVED_PLAN_CONFIRM);
        LocalDateTime dueTime = convertDueTime(dueDate);
        Long assigneeUserId = project.getPlanCoordinatorId();
        if (task == null) {
            erpProjectRoleTaskMapper.insert(ErpProjectRoleTaskDO.builder()
                    .projectId(projectId)
                    .roleCode(ErpProjectRoleCodeConstants.PC)
                    .taskType(ErpProjectRoleTaskTypeConstants.SALE_APPROVED_PLAN_CONFIRM)
                    .taskStatus(ErpProjectRoleTaskStatusConstants.TODO)
                    .assigneeUserId(assigneeUserId)
                    .sourceType("SALE_ORDER")
                    .sourceId(saleOrderId)
                    .summary(PC_SUMMARY)
                    .dueTime(dueTime)
                    .build());
            notifyTaskAssignedAfterCommit(assigneeUserId, project, "PC确认", PC_SUMMARY, dueTime);
            return;
        }
        boolean shouldNotify = shouldNotify(task, assigneeUserId, saleOrderId, PC_SUMMARY, dueTime);
        erpProjectRoleTaskMapper.updateById(ErpProjectRoleTaskDO.builder()
                .id(task.getId())
                .assigneeUserId(assigneeUserId)
                .sourceType("SALE_ORDER")
                .sourceId(saleOrderId)
                .summary(PC_SUMMARY)
                .dueTime(dueTime)
                .build());
        if (shouldNotify) {
            notifyTaskAssignedAfterCommit(assigneeUserId, project, "PC确认", PC_SUMMARY, dueTime);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createOrRefreshMcTask(Long projectId, Long sourceId, String summary) {
        ErpProjectDO project = getRequiredProject(projectId);
        ErpProjectRoleTaskDO task = erpProjectRoleTaskMapper.selectTodoTask(projectId,
                ErpProjectRoleCodeConstants.MC, ErpProjectRoleTaskTypeConstants.MRP_SUPPLY_CONFIRM);
        String finalSummary = StrUtil.blankToDefault(summary, DEFAULT_MC_SUMMARY);
        LocalDateTime dueTime = convertDueTime(project.getDeliveryDate());
        Long assigneeUserId = project.getMaterialControllerId();
        if (task == null) {
            erpProjectRoleTaskMapper.insert(ErpProjectRoleTaskDO.builder()
                    .projectId(projectId)
                    .roleCode(ErpProjectRoleCodeConstants.MC)
                    .taskType(ErpProjectRoleTaskTypeConstants.MRP_SUPPLY_CONFIRM)
                    .taskStatus(ErpProjectRoleTaskStatusConstants.TODO)
                    .assigneeUserId(assigneeUserId)
                    .sourceType("MRP_PLAN")
                    .sourceId(sourceId)
                    .summary(finalSummary)
                    .dueTime(dueTime)
                    .build());
            notifyTaskAssignedAfterCommit(assigneeUserId, project, "MC确认", finalSummary, dueTime);
            return;
        }
        boolean shouldNotify = shouldNotify(task, assigneeUserId, sourceId, finalSummary, dueTime);
        erpProjectRoleTaskMapper.updateById(ErpProjectRoleTaskDO.builder()
                .id(task.getId())
                .assigneeUserId(assigneeUserId)
                .sourceType("MRP_PLAN")
                .sourceId(sourceId)
                .summary(finalSummary)
                .dueTime(dueTime)
                .build());
        if (shouldNotify) {
            notifyTaskAssignedAfterCommit(assigneeUserId, project, "MC确认", finalSummary, dueTime);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completePcTask(Long projectId, String remark) {
        completeRoleTasks(projectId, ErpProjectRoleCodeConstants.PC, remark);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeMcTask(Long projectId, String remark) {
        completeRoleTasks(projectId, ErpProjectRoleCodeConstants.MC, remark);
    }

    @Override
    public List<ErpProjectRoleTaskDO> getTodoTasksByProjectId(Long projectId) {
        return erpProjectRoleTaskMapper.selectTodoListByProjectId(projectId);
    }

    private void completeRoleTasks(Long projectId, String roleCode, String remark) {
        LocalDateTime now = LocalDateTime.now();
        erpProjectRoleTaskMapper.selectTodoListByProjectId(projectId).stream()
                .filter(task -> StrUtil.equals(roleCode, task.getRoleCode()))
                .forEach(task -> erpProjectRoleTaskMapper.updateById(ErpProjectRoleTaskDO.builder()
                        .id(task.getId())
                        .taskStatus(ErpProjectRoleTaskStatusConstants.DONE)
                        .finishTime(now)
                        .remark(remark)
                        .build()));
    }

    private ErpProjectDO getRequiredProject(Long projectId) {
        ErpProjectDO project = erpProjectMapper.selectById(projectId);
        if (project == null) {
            throw exception(PROJECT_NOT_EXISTS);
        }
        return project;
    }

    private LocalDateTime convertDueTime(LocalDate dueDate) {
        return dueDate == null ? null : dueDate.atStartOfDay();
    }

    private boolean shouldNotify(ErpProjectRoleTaskDO task, Long assigneeUserId, Long sourceId, String summary,
                                 LocalDateTime dueTime) {
        if (assigneeUserId == null) {
            return false;
        }
        if (task == null) {
            return true;
        }
        return !Objects.equals(task.getAssigneeUserId(), assigneeUserId)
                || !Objects.equals(task.getSourceId(), sourceId)
                || !Objects.equals(task.getSummary(), summary)
                || !Objects.equals(task.getDueTime(), dueTime);
    }

    private void notifyTaskAssignedAfterCommit(Long assigneeUserId, ErpProjectDO project, String roleName,
                                               String summary, LocalDateTime dueTime) {
        if (assigneeUserId == null) {
            return;
        }
        Runnable action = () -> {
            try {
                Map<String, Object> templateParams = new HashMap<>(8);
                templateParams.put("projectNo", project.getNo());
                templateParams.put("projectName", project.getName());
                templateParams.put("roleName", roleName);
                templateParams.put("taskSummary", summary);
                templateParams.put("dueDate", dueTime == null ? "-" : dueTime.format(DATE_TIME_FORMATTER));
                templateParams.put("detailUrl", PROJECT_FOLLOW_UP_URL);
                notifySendService.sendSingleNotifyToAdmin(assigneeUserId,
                        PROJECT_ROLE_TASK_NOTIFY_TEMPLATE_CODE, templateParams);
            } catch (Exception ex) {
                log.warn("[notifyTaskAssignedAfterCommit][projectId({}) role({}) assignee({}) notify fail]",
                        project.getId(), roleName, assigneeUserId, ex);
            }
        };
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
            return;
        }
        action.run();
    }

}
