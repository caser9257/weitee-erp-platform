package cn.iocoder.yudao.module.erp.controller.admin.project;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.erp.controller.admin.project.vo.project.ErpProjectMcConfirmReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.project.vo.project.ErpProjectPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.project.vo.project.ErpProjectPcConfirmReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.project.vo.project.ErpProjectRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.project.vo.project.ErpProjectRoleTaskRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.project.vo.project.ErpProjectSaveReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.project.vo.project.ErpProjectSimpleRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.project.ErpProjectDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.project.ErpProjectRoleTaskDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpCustomerDO;
import cn.iocoder.yudao.module.erp.service.project.ErpProjectRoleTaskService;
import cn.iocoder.yudao.module.erp.service.project.ErpProjectService;
import cn.iocoder.yudao.module.erp.service.sale.ErpCustomerService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - ERP 项目")
@RestController
@RequestMapping("/erp/project")
@Validated
public class ErpProjectController {

    @Resource
    private ErpProjectService projectService;
    @Resource
    private ErpCustomerService customerService;
    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private ErpProjectRoleTaskService projectRoleTaskService;

    @PostMapping("/create")
    @Operation(summary = "创建项目")
    @PreAuthorize("@ss.hasPermission('erp:project:create')")
    public CommonResult<Long> createProject(@Valid @RequestBody ErpProjectSaveReqVO createReqVO) {
        return success(projectService.createProject(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新项目")
    @PreAuthorize("@ss.hasPermission('erp:project:update')")
    public CommonResult<Boolean> updateProject(@Valid @RequestBody ErpProjectSaveReqVO updateReqVO) {
        projectService.updateProject(updateReqVO);
        return success(true);
    }

    @PostMapping("/pc-confirm")
    @Operation(summary = "PC 确认项目")
    @PreAuthorize("@ss.hasPermission('erp:project:pc-confirm')")
    public CommonResult<Boolean> confirmPc(@Valid @RequestBody ErpProjectPcConfirmReqVO reqVO) {
        projectService.confirmPc(reqVO);
        return success(true);
    }

    @PostMapping("/mc-confirm")
    @Operation(summary = "MC 确认项目")
    @PreAuthorize("@ss.hasPermission('erp:project:mc-confirm')")
    public CommonResult<Boolean> confirmMc(@Valid @RequestBody ErpProjectMcConfirmReqVO reqVO) {
        projectService.confirmMc(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除项目")
    @Parameter(name = "ids", description = "编号数组", required = true)
    @PreAuthorize("@ss.hasPermission('erp:project:delete')")
    public CommonResult<Boolean> deleteProject(@RequestParam("ids") List<Long> ids) {
        ids.forEach(projectService::deleteProject);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得项目")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:project:query')")
    public CommonResult<ErpProjectRespVO> getProject(@RequestParam("id") Long id) {
        ErpProjectDO project = projectService.getProject(id);
        if (project == null) {
            return success(null);
        }
        Map<Long, AdminUserRespDTO> userMap = buildUserMap(List.of(project));
        List<ErpProjectRoleTaskDO> todoTasks = projectRoleTaskService.getTodoTasksByProjectId(project.getId());
        return success(buildProjectRespVO(project, customerService.getCustomer(project.getCustomerId()),
                userMap, todoTasks));
    }

    @GetMapping("/page")
    @Operation(summary = "获得项目分页")
    @PreAuthorize("@ss.hasPermission('erp:project:query')")
    public CommonResult<PageResult<ErpProjectRespVO>> getProjectPage(@Valid ErpProjectPageReqVO pageReqVO) {
        PageResult<ErpProjectDO> pageResult = projectService.getProjectPage(pageReqVO);
        Map<Long, ErpCustomerDO> customerMap = customerService.getCustomerMap(
                convertSet(pageResult.getList(), ErpProjectDO::getCustomerId));
        Map<Long, AdminUserRespDTO> userMap = buildUserMap(pageResult.getList());
        List<ErpProjectRespVO> respList = convertList(pageResult.getList(),
                project -> buildProjectRespVO(project, customerMap.get(project.getCustomerId()), userMap, null));
        return success(new PageResult<>(respList, pageResult.getTotal()));
    }

    @GetMapping("/assigned-page")
    /*
    @Operation(summary = "获得当前用户负责的项目分页")
    @PreAuthorize("@ss.hasAnyPermissions('erp:project:query', 'erp:project:assigned-query')")
    */
    /*
    @Operation(summary = "获取当前用户负责的项目分页")
    */
    @Operation(summary = "Get projects assigned to current user")
    @PreAuthorize("@ss.hasAnyPermissions('erp:project:query', 'erp:project:assigned-query')")
    public CommonResult<PageResult<ErpProjectRespVO>> getAssignedProjectPage(@RequestParam("roleCode") String roleCode,
                                                                              @Valid ErpProjectPageReqVO pageReqVO) {
        PageResult<ErpProjectDO> pageResult = projectService.getAssignedProjectPage(roleCode, pageReqVO);
        Map<Long, ErpCustomerDO> customerMap = customerService.getCustomerMap(
                convertSet(pageResult.getList(), ErpProjectDO::getCustomerId));
        Map<Long, AdminUserRespDTO> userMap = buildUserMap(pageResult.getList());
        List<ErpProjectRespVO> respList = convertList(pageResult.getList(),
                project -> buildProjectRespVO(project, customerMap.get(project.getCustomerId()), userMap, null));
        return success(new PageResult<>(respList, pageResult.getTotal()));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得项目精简列表", description = "只包含已启用项目，主要用于下拉选项")
    public CommonResult<List<ErpProjectSimpleRespVO>> getProjectSimpleList() {
        List<ErpProjectDO> list = projectService.getProjectListByStatus(CommonStatusEnum.ENABLE.getStatus());
        return success(convertList(list, project -> BeanUtils.toBean(project, ErpProjectSimpleRespVO.class)));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出项目 Excel")
    @PreAuthorize("@ss.hasPermission('erp:project:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportProjectExcel(@Valid ErpProjectPageReqVO pageReqVO,
                                   HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ErpProjectDO> list = projectService.getProjectPage(pageReqVO).getList();
        Map<Long, ErpCustomerDO> customerMap = customerService.getCustomerMap(convertSet(list, ErpProjectDO::getCustomerId));
        Map<Long, AdminUserRespDTO> userMap = buildUserMap(list);
        List<ErpProjectRespVO> respList = convertList(list,
                project -> buildProjectRespVO(project, customerMap.get(project.getCustomerId()), userMap, null));
        ExcelUtils.write(response, "项目.xls", "数据", ErpProjectRespVO.class, respList);
    }

    private ErpProjectRespVO buildProjectRespVO(ErpProjectDO project, ErpCustomerDO customer,
                                                Map<Long, AdminUserRespDTO> userMap,
                                                List<ErpProjectRoleTaskDO> todoTasks) {
        return BeanUtils.toBean(project, ErpProjectRespVO.class, item -> {
            item.setDeliveryDate(convertDeliveryDate(project.getDeliveryDate()));
            fillProjectRespVO(item, customer, userMap, todoTasks);
        });
    }

    private void fillProjectRespVO(ErpProjectRespVO project, ErpCustomerDO customer,
                                   Map<Long, AdminUserRespDTO> userMap,
                                   List<ErpProjectRoleTaskDO> todoTasks) {
        if (customer != null) {
            project.setCustomerName(customer.getName());
        }
        AdminUserRespDTO planCoordinator = userMap.get(project.getPlanCoordinatorId());
        if (planCoordinator != null) {
            project.setPlanCoordinatorName(planCoordinator.getNickname());
        }
        AdminUserRespDTO materialController = userMap.get(project.getMaterialControllerId());
        if (materialController != null) {
            project.setMaterialControllerName(materialController.getNickname());
        }
        if (todoTasks != null) {
            project.setTodoTasks(convertList(todoTasks, task -> BeanUtils.toBean(task, ErpProjectRoleTaskRespVO.class)));
        }
    }

    private Map<Long, AdminUserRespDTO> buildUserMap(List<ErpProjectDO> projects) {
        if (projects == null || projects.isEmpty()) {
            return Collections.emptyMap();
        }
        Set<Long> userIds = new LinkedHashSet<>();
        projects.forEach(project -> {
            if (project.getPlanCoordinatorId() != null) {
                userIds.add(project.getPlanCoordinatorId());
            }
            if (project.getMaterialControllerId() != null) {
                userIds.add(project.getMaterialControllerId());
            }
        });
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return adminUserApi.getUserMap(userIds);
    }

    private LocalDateTime convertDeliveryDate(LocalDate deliveryDate) {
        return deliveryDate == null ? null : deliveryDate.atStartOfDay();
    }

}
