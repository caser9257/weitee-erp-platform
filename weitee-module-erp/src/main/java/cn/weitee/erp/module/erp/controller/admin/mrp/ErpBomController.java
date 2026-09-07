package cn.weitee.erp.module.erp.controller.admin.mrp;

import cn.weitee.erp.framework.common.pojo.CommonResult;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.framework.security.core.util.SecurityFrameworkUtils;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.bom.ErpBomPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.bom.ErpBomPricingPreviewRespVO;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.bom.ErpBomRespVO;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.rule.ErpMaterialPlanRulePageReqVO;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.rule.ErpMaterialPlanRuleRespVO;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.rule.ErpMaterialPlanRuleSaveReqVO;
import cn.weitee.erp.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpBomDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpBomItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpBomItemSubstituteDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpMaterialPlanRuleDO;
import cn.weitee.erp.module.erp.service.mrp.ErpBomLifecycleBpmService;
import cn.weitee.erp.module.erp.service.mrp.ErpBomService;
import cn.weitee.erp.module.erp.service.mrp.ErpBomPricingService;
import cn.weitee.erp.module.erp.service.mrp.ErpMaterialPlanRuleService;
import cn.weitee.erp.module.erp.service.mrp.support.ErpMaterialPlanRuleValidationResult;
import cn.weitee.erp.module.erp.service.mrp.support.ErpBomPricingPreviewResult;
import cn.weitee.erp.module.erp.service.mrp.support.ErpMaterialPlanRuleValidator;
import cn.weitee.erp.module.erp.service.product.ErpProductService;
import cn.weitee.erp.module.erp.service.purchase.ErpSupplierService;
import cn.weitee.erp.module.erp.service.rd.ErpRdBomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.weitee.erp.framework.common.pojo.CommonResult.success;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertList;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - ERP BOM / 计划规则")
@RestController
@RequestMapping("/erp/bom")
@Validated
public class ErpBomController {

    @Resource
    private ErpBomService bomService;
    @Resource
    private ErpBomLifecycleBpmService bomLifecycleBpmService;
    @Resource
    private ErpRdBomService rdBomService;
    @Resource
    private ErpMaterialPlanRuleService materialPlanRuleService;
    @Resource
    private ErpMaterialPlanRuleValidator materialPlanRuleValidator;
    @Resource
    private ErpProductService productService;
    @Resource
    private ErpSupplierService supplierService;
    @Resource
    private ErpBomPricingService bomPricingService;

    // ==================== 结构维护入口已下线 ====================
    // 制造 BOM 为研发 BOM 审批发布的快照（publishRdBom），不支持手工创建/编辑/删除。
    // 原 /create /update /delete /update-status 直改入口已按 BPM 治理口径移除，
    // 停用/废止请走 /lifecycle/submit 申请流。

    @PostMapping("/lifecycle/submit")
    @Operation(summary = "发起制造 BOM 停用（废止）申请")
    @PreAuthorize("@ss.hasPermission('erp:bom:update-status')")
    public CommonResult<Boolean> submitDisableApproval(@RequestParam("id") Long id,
                                                       @RequestParam(value = "reason", required = false) String reason) {
        bomLifecycleBpmService.submitDisableApproval(SecurityFrameworkUtils.getLoginUserId(), id, reason);
        return success(true);
    }

    @PostMapping("/lifecycle/cancel")
    @Operation(summary = "撤回制造 BOM 停用申请")
    @PreAuthorize("@ss.hasPermission('erp:bom:update-status')")
    public CommonResult<Boolean> cancelDisableApproval(@RequestParam("id") Long id,
                                                       @RequestParam(value = "reason", required = false) String reason) {
        bomLifecycleBpmService.cancelDisableApproval(SecurityFrameworkUtils.getLoginUserId(), id, reason);
        return success(true);
    }

    @PostMapping("/re-publish")
    @Operation(summary = "从研发 BOM 重新发布（管理动作，刷新 MBOM 快照）")
    @Parameter(name = "rdBomId", description = "研发 BOM 编号", required = true)
    @PreAuthorize("@ss.hasPermission('erp:bom:republish')")
    public CommonResult<Boolean> rePublishFromRdBom(@RequestParam("rdBomId") Long rdBomId) {
        rdBomService.publishRdBom(rdBomId);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得 BOM")
    @PreAuthorize("@ss.hasPermission('erp:bom:query')")
    public CommonResult<ErpBomRespVO> getBom(@RequestParam("id") Long id) {
        return success(buildBomRespVO(bomService.getBom(id)));
    }

    @GetMapping("/page")
    @Operation(summary = "获得 BOM 分页")
    @PreAuthorize("@ss.hasPermission('erp:bom:query')")
    public CommonResult<PageResult<ErpBomRespVO>> getBomPage(@Valid ErpBomPageReqVO pageReqVO) {
        PageResult<ErpBomDO> pageResult = bomService.getBomPage(pageReqVO);
        return success(new PageResult<>(buildBomRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/tree")
    @Operation(summary = "按产品获得有效 BOM")
    @PreAuthorize("@ss.hasPermission('erp:bom:query')")
    public CommonResult<ErpBomRespVO> getBomTree(@RequestParam(value = "productId", required = false) Long productId,
                                                 @RequestParam(value = "bomId", required = false) Long bomId) {
        ErpBomDO rootBom = bomId != null ? bomService.getBom(bomId)
                : productId != null ? bomService.getEffectiveBom(productId) : null;
        return success(buildBomTreeRespVO(rootBom));
    }

    @GetMapping("/pricing-preview")
    @Operation(summary = "根据产品获取 BOM 计价预览")
    @PreAuthorize("@ss.hasPermission('erp:bom:query')")
    public CommonResult<ErpBomPricingPreviewRespVO> getBomPricingPreview(@RequestParam("productId") Long productId) {
        ErpBomPricingPreviewResult result = bomPricingService.previewBomPricing(productId);
        return success(buildBomPricingPreviewRespVO(result));
    }

    @PostMapping("/rule/create")
    @Operation(summary = "创建计划规则")
    @PreAuthorize("@ss.hasPermission('erp:mrp-plan-rule:create')")
    public CommonResult<Long> createRule(@Valid @RequestBody ErpMaterialPlanRuleSaveReqVO createReqVO) {
        return success(materialPlanRuleService.createRule(createReqVO));
    }

    @PutMapping("/rule/update")
    @Operation(summary = "更新计划规则")
    @PreAuthorize("@ss.hasPermission('erp:mrp-plan-rule:update')")
    public CommonResult<Boolean> updateRule(@Valid @RequestBody ErpMaterialPlanRuleSaveReqVO updateReqVO) {
        materialPlanRuleService.updateRule(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/rule/delete")
    @Operation(summary = "删除计划规则")
    @PreAuthorize("@ss.hasPermission('erp:mrp-plan-rule:delete')")
    public CommonResult<Boolean> deleteRule(@RequestParam("id") Long id) {
        materialPlanRuleService.deleteRule(id);
        return success(true);
    }

    @GetMapping("/rule/get")
    @Operation(summary = "获得计划规则")
    @Parameter(name = "id", required = true)
    @PreAuthorize("@ss.hasPermission('erp:mrp-plan-rule:query')")
    public CommonResult<ErpMaterialPlanRuleRespVO> getRule(@RequestParam("id") Long id) {
        return success(buildRuleRespVO(materialPlanRuleService.getRule(id)));
    }

    @GetMapping("/rule/page")
    @Operation(summary = "获得计划规则分页")
    @PreAuthorize("@ss.hasPermission('erp:mrp-plan-rule:query')")
    public CommonResult<PageResult<ErpMaterialPlanRuleRespVO>> getRulePage(@Valid ErpMaterialPlanRulePageReqVO pageReqVO) {
        PageResult<ErpMaterialPlanRuleDO> pageResult = materialPlanRuleService.getRulePage(pageReqVO);
        return success(new PageResult<>(buildRuleRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    private List<ErpBomRespVO> buildBomRespVOList(List<ErpBomDO> list) {
        return convertList(list, bom -> buildBomRespVO(bom));
    }

    private ErpBomRespVO buildBomRespVO(ErpBomDO bom) {
        if (bom == null) {
            return null;
        }
        ErpBomRespVO respVO = BeanUtils.toBean(bom, ErpBomRespVO.class);
        fillBomRespVO(respVO, bomService.getBomItemList(bom.getId()));
        return respVO;
    }

    private ErpBomRespVO buildBomTreeRespVO(ErpBomDO bom) {
        if (bom == null) {
            return null;
        }
        return buildBomTreeRespVO(bom, 1, new HashSet<>(), new BomTreeBuildContext());
    }

    private ErpBomRespVO buildBomTreeRespVO(ErpBomDO bom, int level, Set<Long> pathBomIds, BomTreeBuildContext context) {
        if (bom == null) {
            return null;
        }
        ErpBomRespVO respVO = BeanUtils.toBean(bom, ErpBomRespVO.class);
        ErpProductRespVO product = getProduct(context, bom.getProductId());
        if (product != null) {
            respVO.setProductName(product.getName());
        }
        if (!pathBomIds.add(bom.getId())) {
            respVO.setItems(List.of());
            return respVO;
        }
        try {
            respVO.setItems(buildBomTreeItems(getBomItems(context, bom.getId()), level, pathBomIds, context));
            return respVO;
        } finally {
            pathBomIds.remove(bom.getId());
        }
    }

    private void fillBomRespVO(ErpBomRespVO respVO, List<ErpBomItemDO> itemList) {
        if (itemList == null) {
            itemList = List.of();
        }
        Map<Long, ErpProductRespVO> productMap = productService.getProductVOMap(convertSet(itemList, ErpBomItemDO::getMaterialId));
        List<ErpBomItemSubstituteDO> substituteList = bomService.getBomItemSubstituteList(convertSet(itemList, ErpBomItemDO::getId));
        Map<Long, ErpProductRespVO> substituteProductMap = productService.getProductVOMap(convertSet(substituteList, ErpBomItemSubstituteDO::getSubstituteMaterialId));
        Map<Long, List<ErpBomItemSubstituteDO>> substituteMap = new java.util.HashMap<>();
        for (ErpBomItemSubstituteDO substitute : substituteList) {
            substituteMap.computeIfAbsent(substitute.getBomItemId(), key -> new ArrayList<>()).add(substitute);
        }
        ErpProductRespVO product = productService.getProductVOMap(List.of(respVO.getProductId())).get(respVO.getProductId());
        if (product != null) {
            respVO.setProductName(product.getName());
        }
        respVO.setItems(BeanUtils.toBean(itemList, ErpBomRespVO.Item.class, item -> {
            ErpProductRespVO productRespVO = productMap.get(item.getMaterialId());
            if (productRespVO != null) {
                item.setMaterialName(productRespVO.getName());
                item.setUnitName(productRespVO.getUnitName());
            }
            List<ErpBomItemSubstituteDO> itemSubstitutes = substituteMap.get(item.getId());
            if (itemSubstitutes == null || itemSubstitutes.isEmpty()) {
                item.setSubstitutes(List.of());
                return;
            }
            item.setSubstitutes(BeanUtils.toBean(itemSubstitutes, ErpBomRespVO.Item.Substitute.class, substitute -> {
                ErpProductRespVO substituteProduct = substituteProductMap.get(substitute.getSubstituteMaterialId());
                if (substituteProduct != null) {
                    substitute.setSubstituteMaterialName(substituteProduct.getName());
                }
            }));
        }));
    }

    private List<ErpBomRespVO.Item> buildBomTreeItems(List<ErpBomItemDO> itemList, int level, Set<Long> pathBomIds,
                                                      BomTreeBuildContext context) {
        if (itemList == null || itemList.isEmpty()) {
            return List.of();
        }
        Map<Long, ErpProductRespVO> productMap = getProducts(context, convertSet(itemList, ErpBomItemDO::getMaterialId));
        Map<Long, List<ErpBomItemSubstituteDO>> substituteMap = getBomItemSubstitutesMap(context, convertSet(itemList, ErpBomItemDO::getId));
        Map<Long, ErpBomDO> childBomMap = getEffectiveBoms(context, convertSet(itemList, ErpBomItemDO::getMaterialId));
        return BeanUtils.toBean(itemList, ErpBomRespVO.Item.class, item -> {
            ErpProductRespVO product = productMap.get(item.getMaterialId());
            if (product != null) {
                item.setMaterialName(product.getName());
                item.setUnitName(product.getUnitName());
            }
            item.setLevel(level);
            item.setSubstitutes(buildBomItemSubstituteRespVO(substituteMap.get(item.getId()), context));

            ErpBomDO childBom = childBomMap.get(item.getMaterialId());
            if (childBom == null || pathBomIds.contains(childBom.getId())) {
                item.setHasChildrenBom(false);
                item.setChildren(List.of());
                return;
            }
            item.setHasChildrenBom(true);
            item.setChildBomId(childBom.getId());
            item.setChildBomCode(childBom.getBomCode());
            item.setChildBomVersion(childBom.getVersion());
            ErpBomRespVO childRespVO = buildBomTreeRespVO(childBom, level + 1, pathBomIds, context);
            item.setChildren(childRespVO != null && childRespVO.getItems() != null ? childRespVO.getItems() : List.of());
        });
    }

    private List<ErpBomRespVO.Item.Substitute> buildBomItemSubstituteRespVO(List<ErpBomItemSubstituteDO> substitutes,
                                                                            BomTreeBuildContext context) {
        if (substitutes == null || substitutes.isEmpty()) {
            return List.of();
        }
        return BeanUtils.toBean(substitutes, ErpBomRespVO.Item.Substitute.class, substitute -> {
            ErpProductRespVO substituteProduct = getProduct(context, substitute.getSubstituteMaterialId());
            if (substituteProduct != null) {
                substitute.setSubstituteMaterialName(substituteProduct.getName());
            }
        });
    }

    private Map<Long, ErpProductRespVO> getProducts(BomTreeBuildContext context, Set<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Map.of();
        }
        Set<Long> missingProductIds = new HashSet<>(productIds);
        missingProductIds.removeAll(context.productCache.keySet());
        if (!missingProductIds.isEmpty()) {
            context.productCache.putAll(productService.getProductVOMap(missingProductIds));
        }
        return context.productCache;
    }

    private ErpProductRespVO getProduct(BomTreeBuildContext context, Long productId) {
        if (productId == null) {
            return null;
        }
        if (!context.productCache.containsKey(productId)) {
            context.productCache.putAll(productService.getProductVOMap(List.of(productId)));
        }
        return context.productCache.get(productId);
    }

    private ErpBomDO getEffectiveBom(BomTreeBuildContext context, Long productId) {
        if (productId == null) {
            return null;
        }
        if (!context.effectiveBomCache.containsKey(productId)) {
            context.effectiveBomCache.put(productId, bomService.getEffectiveBom(productId));
        }
        return context.effectiveBomCache.get(productId);
    }

    private Map<Long, ErpBomDO> getEffectiveBoms(BomTreeBuildContext context, Set<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Map.of();
        }
        Set<Long> missingProductIds = new HashSet<>(productIds);
        missingProductIds.removeAll(context.effectiveBomCache.keySet());
        if (!missingProductIds.isEmpty()) {
            Map<Long, ErpBomDO> bomMap = convertMap(bomService.getEffectiveBomList(missingProductIds), ErpBomDO::getProductId);
            for (Long productId : missingProductIds) {
                context.effectiveBomCache.put(productId, bomMap.get(productId));
            }
        }
        return context.effectiveBomCache;
    }

    private List<ErpBomItemDO> getBomItems(BomTreeBuildContext context, Long bomId) {
        if (bomId == null) {
            return List.of();
        }
        if (!context.bomItemCache.containsKey(bomId)) {
            context.bomItemCache.put(bomId, bomService.getBomItemList(bomId));
        }
        return context.bomItemCache.getOrDefault(bomId, List.of());
    }

    private Map<Long, List<ErpBomItemSubstituteDO>> getBomItemSubstitutesMap(BomTreeBuildContext context, Set<Long> bomItemIds) {
        if (bomItemIds == null || bomItemIds.isEmpty()) {
            return Map.of();
        }
        Set<Long> missingBomItemIds = new HashSet<>(bomItemIds);
        missingBomItemIds.removeAll(context.substituteCache.keySet());
        if (!missingBomItemIds.isEmpty()) {
            Map<Long, List<ErpBomItemSubstituteDO>> groupedMap = new HashMap<>();
            for (ErpBomItemSubstituteDO substitute : bomService.getBomItemSubstituteList(missingBomItemIds)) {
                groupedMap.computeIfAbsent(substitute.getBomItemId(), key -> new ArrayList<>()).add(substitute);
            }
            for (Long bomItemId : missingBomItemIds) {
                context.substituteCache.put(bomItemId, groupedMap.getOrDefault(bomItemId, List.of()));
            }
        }
        return context.substituteCache;
    }

    private List<ErpMaterialPlanRuleRespVO> buildRuleRespVOList(List<ErpMaterialPlanRuleDO> list) {
        Map<Long, ErpProductRespVO> productMap = productService.getProductVOMap(convertSet(list, ErpMaterialPlanRuleDO::getProductId));
        Set<Long> supplierIds = new HashSet<>(convertSet(list, ErpMaterialPlanRuleDO::getDefaultSupplierId));
        supplierIds.remove(null);
        Map<Long, String> supplierNameMap = convertMap(supplierService.getSupplierList(supplierIds),
                supplier -> supplier.getId(), supplier -> supplier.getName());
        return BeanUtils.toBean(list, ErpMaterialPlanRuleRespVO.class, item -> {
            ErpProductRespVO product = productMap.get(item.getProductId());
            if (product != null) {
                item.setProductName(product.getName());
            }
            item.setDefaultSupplierName(supplierNameMap.get(item.getDefaultSupplierId()));
            ErpMaterialPlanRuleValidationResult validationResult = materialPlanRuleValidator.validate(
                    BeanUtils.toBean(item, ErpMaterialPlanRuleDO.class));
            item.setValidationStatus(validationResult.getStatus());
            item.setValidationMessage(validationResult.getMessage());
        });
    }

    private ErpMaterialPlanRuleRespVO buildRuleRespVO(ErpMaterialPlanRuleDO rule) {
        if (rule == null) {
            return null;
        }
        return buildRuleRespVOList(List.of(rule)).getFirst();
    }

    private ErpBomPricingPreviewRespVO buildBomPricingPreviewRespVO(ErpBomPricingPreviewResult result) {
        if (result == null) {
            return null;
        }
        ErpBomPricingPreviewRespVO respVO = BeanUtils.toBean(result, ErpBomPricingPreviewRespVO.class);
        respVO.setItems(convertList(result.getItems(), item -> BeanUtils.toBean(item, ErpBomPricingPreviewRespVO.Item.class)));
        respVO.setMissingMaterials(convertList(result.getMissingMaterials(),
                item -> BeanUtils.toBean(item, ErpBomPricingPreviewRespVO.MissingMaterial.class)));
        return respVO;
    }

    private static class BomTreeBuildContext {

        private final Map<Long, ErpProductRespVO> productCache = new HashMap<>();

        private final Map<Long, ErpBomDO> effectiveBomCache = new HashMap<>();

        private final Map<Long, List<ErpBomItemDO>> bomItemCache = new HashMap<>();

        private final Map<Long, List<ErpBomItemSubstituteDO>> substituteCache = new HashMap<>();

    }

}
