package cn.weitee.erp.module.erp.controller.admin.mrp;

import cn.weitee.erp.framework.common.pojo.CommonResult;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.route.ErpProcessRoutePageReqVO;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.route.ErpProcessRouteRespVO;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.route.ErpProcessRouteSaveReqVO;
import cn.weitee.erp.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProcessRouteDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProcessRouteStepDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpWorkCenterDO;
import cn.weitee.erp.module.erp.service.mrp.ErpProcessRouteService;
import cn.weitee.erp.module.erp.service.mrp.ErpWorkCenterService;
import cn.weitee.erp.module.erp.service.product.ErpProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.weitee.erp.framework.common.pojo.CommonResult.success;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertList;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - ERP 工艺路线")
@RestController
@RequestMapping("/erp/process-route")
@Validated
public class ErpProcessRouteController {

    @Resource
    private ErpProcessRouteService processRouteService;
    @Resource
    private ErpProductService productService;
    @Resource
    private ErpWorkCenterService workCenterService;

    @PostMapping("/create")
    @Operation(summary = "创建工艺路线")
    @PreAuthorize("@ss.hasPermission('erp:process-route:create')")
    public CommonResult<Long> createProcessRoute(@Valid @RequestBody ErpProcessRouteSaveReqVO createReqVO) {
        return success(processRouteService.create(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新工艺路线")
    @PreAuthorize("@ss.hasPermission('erp:process-route:update')")
    public CommonResult<Boolean> updateProcessRoute(@Valid @RequestBody ErpProcessRouteSaveReqVO updateReqVO) {
        processRouteService.update(updateReqVO);
        return success(true);
    }

    @PutMapping("/update-status")
    @Operation(summary = "启用/停用/退回草稿")
    @PreAuthorize("@ss.hasPermission('erp:process-route:update')")
    public CommonResult<Boolean> updateProcessRouteStatus(@RequestParam("id") Long id,
                                                          @RequestParam("status") Integer status) {
        processRouteService.updateStatus(id, status);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除工艺路线")
    @PreAuthorize("@ss.hasPermission('erp:process-route:delete')")
    public CommonResult<Boolean> deleteProcessRoute(@RequestParam("id") Long id) {
        processRouteService.delete(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得工艺路线")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('erp:process-route:query')")
    public CommonResult<ErpProcessRouteRespVO> getProcessRoute(@RequestParam("id") Long id) {
        return success(buildProcessRouteRespVO(processRouteService.get(id)));
    }

    @GetMapping("/page")
    @Operation(summary = "获得工艺路线分页")
    @PreAuthorize("@ss.hasPermission('erp:process-route:query')")
    public CommonResult<PageResult<ErpProcessRouteRespVO>> getProcessRoutePage(@Valid ErpProcessRoutePageReqVO pageReqVO) {
        PageResult<ErpProcessRouteDO> pageResult = processRouteService.getPage(pageReqVO);
        return success(new PageResult<>(buildProcessRouteRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    private List<ErpProcessRouteRespVO> buildProcessRouteRespVOList(List<ErpProcessRouteDO> list) {
        if (list == null || list.isEmpty()) {
            return List.of();
        }
        Map<Long, ErpProductRespVO> productMap = productService.getProductVOMap(convertSet(list, ErpProcessRouteDO::getProductId));
        return convertList(list, route -> {
            ErpProcessRouteRespVO respVO = BeanUtils.toBean(route, ErpProcessRouteRespVO.class);
            ErpProductRespVO product = productMap.get(route.getProductId());
            if (product != null) {
                respVO.setProductName(product.getName());
            }
            fillSteps(respVO, processRouteService.getStepList(route.getId()));
            return respVO;
        });
    }

    private ErpProcessRouteRespVO buildProcessRouteRespVO(ErpProcessRouteDO route) {
        if (route == null) {
            return null;
        }
        ErpProcessRouteRespVO respVO = BeanUtils.toBean(route, ErpProcessRouteRespVO.class);
        ErpProductRespVO product = productService.getProductVOMap(List.of(route.getProductId())).get(route.getProductId());
        if (product != null) {
            respVO.setProductName(product.getName());
        }
        fillSteps(respVO, processRouteService.getStepList(route.getId()));
        return respVO;
    }

    private void fillSteps(ErpProcessRouteRespVO respVO, List<ErpProcessRouteStepDO> stepList) {
        if (stepList == null || stepList.isEmpty()) {
            respVO.setSteps(List.of());
            return;
        }
        Set<Long> centerIds = new HashSet<>(convertSet(stepList, ErpProcessRouteStepDO::getWorkCenterId));
        centerIds.remove(null);
        Map<Long, ErpWorkCenterDO> centerMap = centerIds.isEmpty() ? Map.of()
                : workCenterService.getWorkCenterList(centerIds).stream()
                        .collect(Collectors.toMap(ErpWorkCenterDO::getId, center -> center));
        respVO.setSteps(BeanUtils.toBean(stepList, ErpProcessRouteRespVO.Step.class, step -> {
            if (step.getWorkCenterId() != null) {
                ErpWorkCenterDO center = centerMap.get(step.getWorkCenterId());
                if (center != null) {
                    step.setWorkCenterName(center.getCenterName());
                }
            }
        }));
    }

}
