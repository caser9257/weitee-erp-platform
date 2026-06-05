package cn.iocoder.yudao.module.erp.controller.admin.mrp;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.cost.ErpProductionManHourProjectSummaryReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.cost.ErpProductionManHourProjectSummaryRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.cost.ErpProductionManHourPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.cost.ErpProductionManHourRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.cost.ErpProductionManHourSaveReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionManHourDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionOrderDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.project.ErpProjectDO;
import cn.iocoder.yudao.module.erp.service.mrp.ErpProductionManHourService;
import cn.iocoder.yudao.module.erp.service.mrp.ErpProductionOrderService;
import cn.iocoder.yudao.module.erp.service.product.ErpProductService;
import cn.iocoder.yudao.module.erp.service.project.ErpProjectService;
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
import jakarta.validation.Valid;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.erp.util.ErpUserIdUtils.parseUserId;

@Tag(name = "管理后台 - ERP 生产工时归集")
@RestController
@RequestMapping("/erp/production-man-hour")
@Validated
public class ErpProductionManHourController {

    @Resource
    private ErpProductionManHourService productionManHourService;
    @Resource
    private ErpProductionOrderService productionOrderService;
    @Resource
    private ErpProductService productService;
    @Resource
    private ErpProjectService projectService;
    @Resource
    private AdminUserApi adminUserApi;

    @PostMapping("/create")
    @Operation(summary = "创建生产工时归集")
    @PreAuthorize("@ss.hasPermission('erp:production-order:update')")
    public CommonResult<Long> createProductionManHour(@Valid @RequestBody ErpProductionManHourSaveReqVO createReqVO) {
        return success(productionManHourService.createProductionManHour(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新生产工时归集")
    @PreAuthorize("@ss.hasPermission('erp:production-order:update')")
    public CommonResult<Boolean> updateProductionManHour(@Valid @RequestBody ErpProductionManHourSaveReqVO updateReqVO) {
        productionManHourService.updateProductionManHour(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除生产工时归集")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('erp:production-order:update')")
    public CommonResult<Boolean> deleteProductionManHour(@RequestParam("id") Long id) {
        productionManHourService.deleteProductionManHour(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得生产工时归集")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('erp:production-order:query')")
    public CommonResult<ErpProductionManHourRespVO> getProductionManHour(@RequestParam("id") Long id) {
        ErpProductionManHourDO manHour = productionManHourService.getProductionManHour(id);
        if (manHour == null) {
            return success(null);
        }
        Map<Long, ErpProductionOrderDO> orderMap = manHour.getProductionOrderId() == null ? Collections.emptyMap()
                : productionOrderService.getProductionOrderList(List.of(manHour.getProductionOrderId())).stream()
                .collect(Collectors.toMap(ErpProductionOrderDO::getId, item -> item));
        Map<Long, ErpProductRespVO> productMap = buildProductMap(orderMap);
        Map<Long, ErpProjectDO> projectMap = buildProjectMap(orderMap);
        Map<Long, AdminUserRespDTO> userMap = buildUserMap(manHour.getCreator());
        return success(buildRespVO(manHour, orderMap, productMap, projectMap, userMap));
    }

    @GetMapping("/page")
    @Operation(summary = "获得生产工时归集分页")
    @PreAuthorize("@ss.hasPermission('erp:production-order:query')")
    public CommonResult<PageResult<ErpProductionManHourRespVO>> getProductionManHourPage(
            @Valid ErpProductionManHourPageReqVO pageReqVO) {
        PageResult<ErpProductionManHourDO> pageResult = productionManHourService.getProductionManHourPage(pageReqVO);
        return success(buildRespVOPageResult(pageResult));
    }

    @GetMapping("/project-summary")
    @Operation(summary = "获得项目级工时成本汇总")
    @PreAuthorize("@ss.hasPermission('erp:production-order:query')")
    public CommonResult<List<ErpProductionManHourProjectSummaryRespVO>> getProductionManHourProjectSummary(
            @Valid ErpProductionManHourProjectSummaryReqVO reqVO) {
        return success(productionManHourService.getProductionManHourProjectSummaryList(reqVO));
    }

    private PageResult<ErpProductionManHourRespVO> buildRespVOPageResult(PageResult<ErpProductionManHourDO> pageResult) {
        if (CollUtil.isEmpty(pageResult.getList())) {
            return PageResult.empty(pageResult.getTotal());
        }
        List<Long> orderIds = new ArrayList<>(convertSet(pageResult.getList(), ErpProductionManHourDO::getProductionOrderId));
        orderIds.removeIf(Objects::isNull);
        Map<Long, ErpProductionOrderDO> orderMap = orderIds.isEmpty() ? Collections.emptyMap()
                : productionOrderService.getProductionOrderList(orderIds).stream()
                .collect(Collectors.toMap(ErpProductionOrderDO::getId, item -> item));
        Map<Long, ErpProductRespVO> productMap = buildProductMap(orderMap);
        Map<Long, ErpProjectDO> projectMap = buildProjectMap(orderMap);
        List<Long> creatorIds = new ArrayList<>(convertList(pageResult.getList(), item -> parseUserId(item.getCreator())));
        creatorIds.removeIf(Objects::isNull);
        Map<Long, AdminUserRespDTO> userMap = creatorIds.isEmpty() ? Collections.emptyMap() : adminUserApi.getUserMap(creatorIds);
        return new PageResult<>(convertList(pageResult.getList(), item -> buildRespVO(item, orderMap, productMap, projectMap, userMap)),
                pageResult.getTotal());
    }

    private ErpProductionManHourRespVO buildRespVO(ErpProductionManHourDO manHour,
                                                   Map<Long, ErpProductionOrderDO> orderMap,
                                                   Map<Long, ErpProductRespVO> productMap,
                                                   Map<Long, ErpProjectDO> projectMap,
                                                   Map<Long, AdminUserRespDTO> userMap) {
        ErpProductionManHourRespVO vo = BeanUtils.toBean(manHour, ErpProductionManHourRespVO.class);
        MapUtils.findAndThen(orderMap, manHour.getProductionOrderId(), order -> {
            vo.setProductionOrderNo(order.getOrderNo());
            vo.setProductId(order.getProductId());
            MapUtils.findAndThen(productMap, order.getProductId(), product -> vo.setProductName(product.getName()));
            vo.setProjectId(order.getProjectId());
            MapUtils.findAndThen(projectMap, order.getProjectId(), project -> {
                vo.setProjectNo(project.getNo());
                vo.setProjectName(project.getName());
            });
        });
        MapUtils.findAndThen(userMap, parseUserId(manHour.getCreator()),
                user -> vo.setCreatorName(user.getNickname()));
        return vo;
    }

    private Map<Long, ErpProductRespVO> buildProductMap(Map<Long, ErpProductionOrderDO> orderMap) {
        List<Long> productIds = new ArrayList<>(convertSet(orderMap.values(), ErpProductionOrderDO::getProductId));
        productIds.removeIf(Objects::isNull);
        return productIds.isEmpty() ? Collections.emptyMap() : productService.getProductVOMap(productIds);
    }

    private Map<Long, ErpProjectDO> buildProjectMap(Map<Long, ErpProductionOrderDO> orderMap) {
        List<Long> projectIds = new ArrayList<>(convertSet(orderMap.values(), ErpProductionOrderDO::getProjectId));
        projectIds.removeIf(Objects::isNull);
        return projectIds.isEmpty() ? Collections.emptyMap() : projectService.getProjectMap(projectIds);
    }

    private Map<Long, AdminUserRespDTO> buildUserMap(String creator) {
        Long creatorId = parseUserId(creator);
        return creatorId == null ? Collections.emptyMap() : adminUserApi.getUserMap(List.of(creatorId));
    }

}
