package cn.weitee.erp.module.erp.controller.admin.mrp;

import cn.hutool.core.collection.CollUtil;
import cn.weitee.erp.framework.common.pojo.CommonResult;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.number.NumberUtils;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.cost.ErpProductionCostAllocationDetailRespVO;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.cost.ErpProductionCostAllocationExecuteReqVO;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.cost.ErpProductionCostAllocationPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.cost.ErpProductionCostAllocationRespVO;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.cost.ErpProductionCostAllocationSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionCostAllocationDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionCostAllocationRuleDO;
import cn.weitee.erp.module.erp.enums.mrp.ErpProductionCostAllocationStatusEnum;
import cn.weitee.erp.module.erp.enums.mrp.ErpProductionCostTypeEnum;
import cn.weitee.erp.module.erp.service.mrp.ErpProductionCostAllocationRuleService;
import cn.weitee.erp.module.erp.service.mrp.ErpProductionCostAllocationService;
import cn.weitee.erp.module.system.api.user.AdminUserApi;
import cn.weitee.erp.module.system.api.user.dto.AdminUserRespDTO;
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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.weitee.erp.framework.common.pojo.CommonResult.success;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertList;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.weitee.erp.module.erp.util.ErpUserIdUtils.parseUserId;

@Tag(name = "管理后台 - ERP 生产成本分摊单")
@RestController
@RequestMapping("/erp/production-cost-allocation")
@Validated
public class ErpProductionCostAllocationController {

    @Resource
    private ErpProductionCostAllocationService productionCostAllocationService;
    @Resource
    private ErpProductionCostAllocationRuleService productionCostAllocationRuleService;
    @Resource
    private AdminUserApi adminUserApi;

    @PostMapping("/create")
    @Operation(summary = "创建生产成本分摊单")
    @PreAuthorize("@ss.hasPermission('erp:production-order:update')")
    public CommonResult<Long> createProductionCostAllocation(
            @Valid @RequestBody ErpProductionCostAllocationSaveReqVO createReqVO) {
        return success(productionCostAllocationService.createProductionCostAllocation(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新生产成本分摊单")
    @PreAuthorize("@ss.hasPermission('erp:production-order:update')")
    public CommonResult<Boolean> updateProductionCostAllocation(
            @Valid @RequestBody ErpProductionCostAllocationSaveReqVO updateReqVO) {
        productionCostAllocationService.updateProductionCostAllocation(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除生产成本分摊单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('erp:production-order:update')")
    public CommonResult<Boolean> deleteProductionCostAllocation(@RequestParam("id") Long id) {
        productionCostAllocationService.deleteProductionCostAllocation(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得生产成本分摊单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('erp:production-order:query')")
    public CommonResult<ErpProductionCostAllocationRespVO> getProductionCostAllocation(@RequestParam("id") Long id) {
        ErpProductionCostAllocationDO allocation = productionCostAllocationService.getProductionCostAllocation(id);
        if (allocation == null) {
            return success(null);
        }
        Map<Long, ErpProductionCostAllocationRuleDO> ruleMap = allocation.getRuleId() == null
                ? Collections.emptyMap()
                : productionCostAllocationRuleService.getProductionCostAllocationRuleList(List.of(allocation.getRuleId()))
                .stream().collect(Collectors.toMap(ErpProductionCostAllocationRuleDO::getId, item -> item));
        Map<Long, AdminUserRespDTO> userMap = allocation.getCreator() == null ? Collections.emptyMap()
                : adminUserApi.getUserMap(List.of(parseUserId(allocation.getCreator())));
        return success(buildRespVO(allocation, ruleMap, userMap));
    }

    @GetMapping("/page")
    @Operation(summary = "获得生产成本分摊单分页")
    @PreAuthorize("@ss.hasPermission('erp:production-order:query')")
    public CommonResult<PageResult<ErpProductionCostAllocationRespVO>> getProductionCostAllocationPage(
            @Valid ErpProductionCostAllocationPageReqVO pageReqVO) {
        PageResult<ErpProductionCostAllocationDO> pageResult =
                productionCostAllocationService.getProductionCostAllocationPage(pageReqVO);
        return success(buildRespVOPageResult(pageResult));
    }

    @PostMapping("/execute")
    @Operation(summary = "执行生产成本分摊单")
    @PreAuthorize("@ss.hasPermission('erp:production-order:update')")
    public CommonResult<Boolean> executeProductionCostAllocation(
            @Valid @RequestBody ErpProductionCostAllocationExecuteReqVO executeReqVO) {
        productionCostAllocationService.executeProductionCostAllocation(executeReqVO.getId());
        return success(true);
    }

    @GetMapping("/detail")
    @Operation(summary = "获得生产成本分摊单明细")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('erp:production-order:query')")
    public CommonResult<ErpProductionCostAllocationDetailRespVO> getProductionCostAllocationDetail(
            @RequestParam("id") Long id) {
        return success(productionCostAllocationService.getProductionCostAllocationDetail(id));
    }

    private PageResult<ErpProductionCostAllocationRespVO> buildRespVOPageResult(PageResult<ErpProductionCostAllocationDO> pageResult) {
        if (CollUtil.isEmpty(pageResult.getList())) {
            return PageResult.empty(pageResult.getTotal());
        }
        Map<Long, ErpProductionCostAllocationRuleDO> ruleMap = productionCostAllocationRuleService
                .getProductionCostAllocationRuleList(convertSet(pageResult.getList(), ErpProductionCostAllocationDO::getRuleId)).stream()
                .collect(Collectors.toMap(ErpProductionCostAllocationRuleDO::getId, item -> item, (left, right) -> left));
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(convertList(pageResult.getList(),
                item -> parseUserId(item.getCreator())));
        return new PageResult<>(convertList(pageResult.getList(), item -> buildRespVO(item, ruleMap, userMap)),
                pageResult.getTotal());
    }

    private ErpProductionCostAllocationRespVO buildRespVO(ErpProductionCostAllocationDO allocation,
                                                          Map<Long, ErpProductionCostAllocationRuleDO> ruleMap,
                                                          Map<Long, AdminUserRespDTO> userMap) {
        ErpProductionCostAllocationRespVO vo = BeanUtils.toBean(allocation, ErpProductionCostAllocationRespVO.class);
        vo.setCostTypeName(ErpProductionCostTypeEnum.resolveName(allocation.getCostType()));
        vo.setStatusName(ErpProductionCostAllocationStatusEnum.resolveName(allocation.getStatus()));
        ErpProductionCostAllocationRuleDO rule = ruleMap.get(allocation.getRuleId());
        if (rule != null) {
            vo.setRuleName(rule.getRuleName());
        }
        AdminUserRespDTO user = userMap.get(parseUserId(allocation.getCreator()));
        if (user != null) {
            vo.setCreatorName(user.getNickname());
        }
        return vo;
    }

}
