package cn.iocoder.yudao.module.erp.controller.admin.mrp;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.cost.ErpProductionCostDetailRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.cost.ErpProductionCostEntryPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.cost.ErpProductionCostEntryRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.cost.ErpProductionCostEntrySaveReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.cost.ErpProductionCostProjectSummaryRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.cost.ErpProductionCostProductSummaryRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.cost.ErpProductionCostSummaryRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionCostEntryDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionOrderDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.project.ErpProjectDO;
import cn.iocoder.yudao.module.erp.enums.mrp.ErpProductionCostSourceTypeEnum;
import cn.iocoder.yudao.module.erp.enums.mrp.ErpProductionCostTypeEnum;
import cn.iocoder.yudao.module.erp.service.mrp.ErpProductionCostService;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.erp.util.ErpUserIdUtils.parseUserId;

@Tag(name = "管理后台 - ERP 生产成本归集")
@RestController
@RequestMapping("/erp/production-cost-entry")
@Validated
public class ErpProductionCostController {

    @Resource
    private ErpProductionCostService productionCostService;
    @Resource
    private ErpProductionOrderService productionOrderService;
    @Resource
    private ErpProductService productService;
    @Resource
    private ErpProjectService projectService;
    @Resource
    private AdminUserApi adminUserApi;

    @PostMapping("/create")
    @Operation(summary = "创建生产成本归集明细")
    @PreAuthorize("@ss.hasPermission('erp:production-order:update')")
    public CommonResult<Long> createProductionCostEntry(@Valid @RequestBody ErpProductionCostEntrySaveReqVO createReqVO) {
        return success(productionCostService.createProductionCostEntry(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新生产成本归集明细")
    @PreAuthorize("@ss.hasPermission('erp:production-order:update')")
    public CommonResult<Boolean> updateProductionCostEntry(@Valid @RequestBody ErpProductionCostEntrySaveReqVO updateReqVO) {
        productionCostService.updateProductionCostEntry(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除生产成本归集明细")
    @Parameter(name = "id", description = "ID", required = true)
    @PreAuthorize("@ss.hasPermission('erp:production-order:update')")
    public CommonResult<Boolean> deleteProductionCostEntry(@RequestParam("id") Long id) {
        productionCostService.deleteProductionCostEntry(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取生产成本归集明细")
    @Parameter(name = "id", description = "ID", required = true)
    @PreAuthorize("@ss.hasAnyPermissions('erp:finance-report:query', 'erp:finance-voucher:query')")
    public CommonResult<ErpProductionCostEntryRespVO> getProductionCostEntry(@RequestParam("id") Long id) {
        ErpProductionCostEntryDO entry = productionCostService.getProductionCostEntry(id);
        if (entry == null) {
            return success(null);
        }
        Map<Long, ErpProductionOrderDO> orderMap = entry.getProductionOrderId() == null ? Collections.emptyMap()
                : productionOrderService.getProductionOrderList(List.of(entry.getProductionOrderId())).stream()
                .collect(Collectors.toMap(ErpProductionOrderDO::getId, item -> item));
        Map<Long, ErpProductRespVO> productMap = buildProductMap(orderMap);
        Map<Long, ErpProjectDO> projectMap = buildProjectMap(orderMap);
        Map<Long, AdminUserRespDTO> userMap = buildUserMap(entry.getCreator());
        return success(buildEntryVO(entry, orderMap, productMap, projectMap, userMap));
    }

    @GetMapping("/page")
    @Operation(summary = "获取生产成本归集明细分页")
    @PreAuthorize("@ss.hasAnyPermissions('erp:finance-report:query', 'erp:finance-voucher:query')")
    public CommonResult<PageResult<ErpProductionCostSummaryRespVO>> getProductionCostEntryPage(
            @Valid ErpProductionCostEntryPageReqVO pageReqVO) {
        return success(productionCostService.getProductionCostSummaryPage(pageReqVO));
    }

    @GetMapping("/detail")
    @Operation(summary = "获取生产工单成本穿透明细")
    @Parameter(name = "productionOrderId", description = "生产工单编号", required = true)
    @PreAuthorize("@ss.hasAnyPermissions('erp:finance-report:query', 'erp:finance-voucher:query')")
    public CommonResult<ErpProductionCostDetailRespVO> getCostDetail(@RequestParam("productionOrderId") Long productionOrderId) {
        return success(productionCostService.getCostDetail(productionOrderId));
    }

    @GetMapping("/project-summary")
    @Operation(summary = "获取生产成本项目汇总")
    @Parameter(name = "accountingMonth", description = "归集月份", required = true)
    @PreAuthorize("@ss.hasAnyPermissions('erp:finance-report:query', 'erp:finance-voucher:query')")
    public CommonResult<List<ErpProductionCostProjectSummaryRespVO>> getProjectSummary(
            @RequestParam("accountingMonth") String accountingMonth) {
        return success(productionCostService.getProjectSummary(accountingMonth));
    }

    @GetMapping("/product-summary")
    @Operation(summary = "获取生产成本产品汇总")
    @Parameter(name = "accountingMonth", description = "归集月份（可选，为空则汇总所有月份）", required = false)
    @PreAuthorize("@ss.hasAnyPermissions('erp:finance-report:query', 'erp:finance-voucher:query')")
    public CommonResult<List<ErpProductionCostProductSummaryRespVO>> getProductSummary(
            @RequestParam(value = "accountingMonth", required = false) String accountingMonth) {
        return success(productionCostService.getProductSummary(accountingMonth));
    }

    private PageResult<ErpProductionCostEntryRespVO> buildEntryVOPageResult(PageResult<ErpProductionCostEntryDO> pageResult) {
        if (CollUtil.isEmpty(pageResult.getList())) {
            return PageResult.empty(pageResult.getTotal());
        }
        Map<Long, ErpProductionOrderDO> orderMap = productionOrderService.getProductionOrderList(
                        filterNullIds(convertSet(pageResult.getList(), ErpProductionCostEntryDO::getProductionOrderId))).stream()
                .collect(Collectors.toMap(ErpProductionOrderDO::getId, item -> item));
        Map<Long, ErpProductRespVO> productMap = buildProductMap(orderMap);
        Map<Long, ErpProjectDO> projectMap = buildProjectMap(orderMap);
        List<Long> creatorIds = new ArrayList<>(convertList(pageResult.getList(), item -> parseUserId(item.getCreator())));
        creatorIds.removeIf(Objects::isNull);
        Map<Long, AdminUserRespDTO> userMap = creatorIds.isEmpty() ? Collections.emptyMap() : adminUserApi.getUserMap(creatorIds);
        return new PageResult<>(convertList(pageResult.getList(), item -> buildEntryVO(item, orderMap, productMap, projectMap, userMap)),
                pageResult.getTotal());
    }

    private ErpProductionCostEntryRespVO buildEntryVO(ErpProductionCostEntryDO entry,
                                                      Map<Long, ErpProductionOrderDO> orderMap,
                                                      Map<Long, ErpProductRespVO> productMap,
                                                      Map<Long, ErpProjectDO> projectMap,
                                                      Map<Long, AdminUserRespDTO> userMap) {
        ErpProductionCostEntryRespVO vo = BeanUtils.toBean(entry, ErpProductionCostEntryRespVO.class);
        MapUtils.findAndThen(orderMap, entry.getProductionOrderId(), order -> {
            vo.setProductionOrderNo(order.getOrderNo());
            vo.setProductId(order.getProductId());
            MapUtils.findAndThen(productMap, order.getProductId(), product -> vo.setProductName(product.getName()));
            vo.setProjectId(order.getProjectId());
            MapUtils.findAndThen(projectMap, order.getProjectId(), project -> {
                vo.setProjectNo(project.getNo());
                vo.setProjectName(project.getName());
            });
        });
        MapUtils.findAndThen(userMap, parseUserId(entry.getCreator()),
                user -> vo.setCreatorName(user.getNickname()));
        vo.setCostTypeName(ErpProductionCostTypeEnum.resolveName(entry.getCostType()));
        vo.setSourceTypeName(ErpProductionCostSourceTypeEnum.resolveName(entry.getSourceType()));
        return vo;
    }

    private Map<Long, ErpProductRespVO> buildProductMap(Map<Long, ErpProductionOrderDO> orderMap) {
        List<Long> productIds = filterNullIds(convertSet(orderMap.values(), ErpProductionOrderDO::getProductId));
        return productIds.isEmpty() ? Collections.emptyMap() : productService.getProductVOMap(productIds);
    }

    private Map<Long, ErpProjectDO> buildProjectMap(Map<Long, ErpProductionOrderDO> orderMap) {
        List<Long> projectIds = filterNullIds(convertSet(orderMap.values(), ErpProductionOrderDO::getProjectId));
        return projectIds.isEmpty() ? Collections.emptyMap() : projectService.getProjectMap(projectIds);
    }

    private Map<Long, AdminUserRespDTO> buildUserMap(String creator) {
        Long creatorId = parseUserId(creator);
        return creatorId == null ? Collections.emptyMap() : adminUserApi.getUserMap(List.of(creatorId));
    }

    private List<Long> filterNullIds(Iterable<Long> ids) {
        List<Long> result = new ArrayList<>();
        for (Long id : ids) {
            if (id != null) {
                result.add(id);
            }
        }
        return result;
    }

}
