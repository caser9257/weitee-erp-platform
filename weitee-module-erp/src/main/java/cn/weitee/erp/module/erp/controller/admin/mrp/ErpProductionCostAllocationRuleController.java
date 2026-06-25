package cn.weitee.erp.module.erp.controller.admin.mrp;

import cn.weitee.erp.framework.common.enums.CommonStatusEnum;
import cn.weitee.erp.framework.common.pojo.CommonResult;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.cost.ErpProductionCostAllocationRulePageReqVO;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.cost.ErpProductionCostAllocationRuleRespVO;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.cost.ErpProductionCostAllocationRuleSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionCostAllocationRuleDO;
import cn.weitee.erp.module.erp.enums.mrp.ErpProductionCostAllocationBasisTypeEnum;
import cn.weitee.erp.module.erp.enums.mrp.ErpProductionCostTypeEnum;
import cn.weitee.erp.module.erp.service.mrp.ErpProductionCostAllocationRuleService;
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
import java.util.List;

import static cn.weitee.erp.framework.common.pojo.CommonResult.success;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertList;

@Tag(name = "管理后台 - ERP 生产成本分摊规则")
@RestController
@RequestMapping("/erp/production-cost-allocation-rule")
@Validated
public class ErpProductionCostAllocationRuleController {

    @Resource
    private ErpProductionCostAllocationRuleService productionCostAllocationRuleService;

    @PostMapping("/create")
    @Operation(summary = "创建生产成本分摊规则")
    @PreAuthorize("@ss.hasPermission('erp:production-order:update')")
    public CommonResult<Long> createProductionCostAllocationRule(
            @Valid @RequestBody ErpProductionCostAllocationRuleSaveReqVO createReqVO) {
        return success(productionCostAllocationRuleService.createProductionCostAllocationRule(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新生产成本分摊规则")
    @PreAuthorize("@ss.hasPermission('erp:production-order:update')")
    public CommonResult<Boolean> updateProductionCostAllocationRule(
            @Valid @RequestBody ErpProductionCostAllocationRuleSaveReqVO updateReqVO) {
        productionCostAllocationRuleService.updateProductionCostAllocationRule(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除生产成本分摊规则")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('erp:production-order:update')")
    public CommonResult<Boolean> deleteProductionCostAllocationRule(@RequestParam("id") Long id) {
        productionCostAllocationRuleService.deleteProductionCostAllocationRule(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得生产成本分摊规则")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('erp:production-order:query')")
    public CommonResult<ErpProductionCostAllocationRuleRespVO> getProductionCostAllocationRule(@RequestParam("id") Long id) {
        ErpProductionCostAllocationRuleDO rule = productionCostAllocationRuleService.getProductionCostAllocationRule(id);
        return success(rule == null ? null : buildRespVO(rule));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得启用的生产成本分摊规则列表")
    public CommonResult<List<ErpProductionCostAllocationRuleRespVO>> getProductionCostAllocationRuleSimpleList() {
        List<ErpProductionCostAllocationRuleDO> rules =
                productionCostAllocationRuleService.getProductionCostAllocationRuleListByStatus(CommonStatusEnum.ENABLE.getStatus());
        return success(convertList(rules, this::buildRespVO));
    }

    @GetMapping("/page")
    @Operation(summary = "获得生产成本分摊规则分页")
    @PreAuthorize("@ss.hasPermission('erp:production-order:query')")
    public CommonResult<PageResult<ErpProductionCostAllocationRuleRespVO>> getProductionCostAllocationRulePage(
            @Valid ErpProductionCostAllocationRulePageReqVO pageReqVO) {
        PageResult<ErpProductionCostAllocationRuleDO> pageResult =
                productionCostAllocationRuleService.getProductionCostAllocationRulePage(pageReqVO);
        return success(new PageResult<>(convertList(pageResult.getList(), this::buildRespVO), pageResult.getTotal()));
    }

    private ErpProductionCostAllocationRuleRespVO buildRespVO(ErpProductionCostAllocationRuleDO rule) {
        ErpProductionCostAllocationRuleRespVO vo = BeanUtils.toBean(rule, ErpProductionCostAllocationRuleRespVO.class);
        vo.setCostTypeName(ErpProductionCostTypeEnum.resolveName(rule.getCostType()));
        vo.setBasisTypeName(ErpProductionCostAllocationBasisTypeEnum.resolveName(rule.getBasisType()));
        return vo;
    }

}
