package cn.weitee.erp.module.erp.controller.admin.mrp;

import cn.weitee.erp.framework.common.pojo.CommonResult;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.workcenter.ErpWorkCenterPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.workcenter.ErpWorkCenterRespVO;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.workcenter.ErpWorkCenterSaveReqVO;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.workcenter.ErpWorkCenterSimpleRespVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpWorkCenterDO;
import cn.weitee.erp.module.erp.service.mrp.ErpWorkCenterService;
import cn.weitee.erp.module.system.api.dept.DeptApi;
import cn.weitee.erp.module.system.api.dept.dto.DeptRespDTO;
import cn.weitee.erp.module.system.api.user.AdminUserApi;
import cn.weitee.erp.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.weitee.erp.framework.common.pojo.CommonResult.success;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertList;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - ERP 工作中心")
@RestController
@RequestMapping("/erp/work-center")
@Validated
public class ErpWorkCenterController {

    @Resource
    private ErpWorkCenterService workCenterService;
    @Resource
    private DeptApi deptApi;
    @Resource
    private AdminUserApi adminUserApi;

    @PostMapping("/create")
    @Operation(summary = "创建工作中心")
    @PreAuthorize("@ss.hasPermission('erp:work-center:create')")
    public CommonResult<Long> createWorkCenter(@Valid @RequestBody ErpWorkCenterSaveReqVO createReqVO) {
        return success(workCenterService.create(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新工作中心")
    @PreAuthorize("@ss.hasPermission('erp:work-center:update')")
    public CommonResult<Boolean> updateWorkCenter(@Valid @RequestBody ErpWorkCenterSaveReqVO updateReqVO) {
        workCenterService.update(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除工作中心")
    @PreAuthorize("@ss.hasPermission('erp:work-center:delete')")
    public CommonResult<Boolean> deleteWorkCenter(@RequestParam("id") Long id) {
        workCenterService.delete(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得工作中心")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('erp:work-center:query')")
    public CommonResult<ErpWorkCenterRespVO> getWorkCenter(@RequestParam("id") Long id) {
        return success(buildWorkCenterRespVO(workCenterService.get(id)));
    }

    @GetMapping("/page")
    @Operation(summary = "获得工作中心分页")
    @PreAuthorize("@ss.hasPermission('erp:work-center:query')")
    public CommonResult<PageResult<ErpWorkCenterRespVO>> getWorkCenterPage(@Valid ErpWorkCenterPageReqVO pageReqVO) {
        PageResult<ErpWorkCenterDO> pageResult = workCenterService.getPage(pageReqVO);
        return success(new PageResult<>(buildWorkCenterRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得启用中的工作中心精简列表")
    public CommonResult<List<ErpWorkCenterSimpleRespVO>> getWorkCenterSimpleList() {
        return success(BeanUtils.toBean(workCenterService.getEnabledList(), ErpWorkCenterSimpleRespVO.class));
    }

    private List<ErpWorkCenterRespVO> buildWorkCenterRespVOList(List<ErpWorkCenterDO> list) {
        if (list == null || list.isEmpty()) {
            return List.of();
        }
        Set<Long> deptIds = convertSet(list, ErpWorkCenterDO::getDeptId);
        deptIds.remove(null);
        Map<Long, DeptRespDTO> deptMap = deptIds.isEmpty() ? Map.of() : deptApi.getDeptMap(deptIds);
        Set<Long> userIds = convertSet(list, ErpWorkCenterDO::getManagerUserId);
        userIds.remove(null);
        Map<Long, AdminUserRespDTO> userMap = userIds.isEmpty() ? Map.of() : adminUserApi.getUserMap(userIds);
        return convertList(list, item -> {
            ErpWorkCenterRespVO respVO = BeanUtils.toBean(item, ErpWorkCenterRespVO.class);
            if (item.getDeptId() != null) {
                DeptRespDTO dept = deptMap.get(item.getDeptId());
                if (dept != null) {
                    respVO.setDeptName(dept.getName());
                }
            }
            if (item.getManagerUserId() != null) {
                AdminUserRespDTO user = userMap.get(item.getManagerUserId());
                if (user != null) {
                    respVO.setManagerUserName(user.getNickname());
                }
            }
            return respVO;
        });
    }

    private ErpWorkCenterRespVO buildWorkCenterRespVO(ErpWorkCenterDO workCenter) {
        if (workCenter == null) {
            return null;
        }
        return buildWorkCenterRespVOList(List.of(workCenter)).getFirst();
    }

}
