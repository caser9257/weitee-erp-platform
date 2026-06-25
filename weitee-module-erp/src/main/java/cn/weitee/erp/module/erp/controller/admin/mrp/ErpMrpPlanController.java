package cn.weitee.erp.module.erp.controller.admin.mrp;

import cn.weitee.erp.framework.common.pojo.CommonResult;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.plan.*;
import cn.weitee.erp.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpBomDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpBomItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpBomItemSubstituteDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpMrpPlanDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpMrpResultComponentDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpMrpResultDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpMrpShortageDO;
import cn.weitee.erp.module.erp.service.mrp.ErpBomService;
import cn.weitee.erp.module.erp.service.mrp.ErpMrpPlanService;
import cn.weitee.erp.module.erp.service.product.ErpProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.weitee.erp.framework.common.pojo.CommonResult.success;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertMap;

@Tag(name = "管理后台 - ERP MRP 计划")
@RestController
@RequestMapping("/erp/mrp-plan")
@Validated
public class ErpMrpPlanController {

    @Resource
    private ErpMrpPlanService mrpPlanService;
    @Resource
    private ErpBomService bomService;
    @Resource
    private ErpProductService productService;

    @PostMapping("/create")
    @Operation(summary = "创建 MRP 计划")
    @PreAuthorize("@ss.hasPermission('erp:mrp-plan:create')")
    public CommonResult<Long> createPlan(@Valid @RequestBody ErpMrpPlanSaveReqVO createReqVO) {
        return success(mrpPlanService.createPlan(createReqVO));
    }

    @PostMapping("/run")
    @Operation(summary = "运行 MRP")
    @PreAuthorize("@ss.hasPermission('erp:mrp-plan:run')")
    public CommonResult<Boolean> runPlan(@RequestParam("id") Long id) {
        mrpPlanService.runPlan(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得 MRP 计划")
    @PreAuthorize("@ss.hasPermission('erp:mrp-plan:query')")
    public CommonResult<ErpMrpPlanRespVO> getPlan(@RequestParam("id") Long id) {
        return success(BeanUtils.toBean(mrpPlanService.getPlan(id), ErpMrpPlanRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得 MRP 计划分页")
    @PreAuthorize("@ss.hasPermission('erp:mrp-plan:query')")
    public CommonResult<PageResult<ErpMrpPlanRespVO>> getPlanPage(@Valid ErpMrpPlanPageReqVO pageReqVO) {
        PageResult<ErpMrpPlanDO> pageResult = mrpPlanService.getPlanPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, ErpMrpPlanRespVO.class));
    }

    @GetMapping("/result")
    @Operation(summary = "获得 MRP 运算结果")
    @PreAuthorize("@ss.hasPermission('erp:mrp-plan:query')")
    public CommonResult<List<ErpMrpResultRespVO>> getResultList(@RequestParam("planId") Long planId) {
        List<ErpMrpResultDO> list = mrpPlanService.getResultList(planId);
        Set<Long> productIds = new HashSet<>(convertSet(list, ErpMrpResultDO::getMaterialId));
        productIds.addAll(convertSet(list, ErpMrpResultDO::getRootProductId));
        Map<Long, ErpProductRespVO> productMap = productService.getProductVOMap(productIds);
        return success(BeanUtils.toBean(list, ErpMrpResultRespVO.class, item -> {
            ErpProductRespVO material = productMap.get(item.getMaterialId());
            if (material != null) {
                item.setMaterialName(material.getName());
            }
            ErpProductRespVO rootProduct = productMap.get(item.getRootProductId());
            if (rootProduct != null) {
                item.setRootProductName(rootProduct.getName());
            }
        }));
    }

    @GetMapping("/result-component/list")
    @Operation(summary = "获得净需求解释明细")
    @PreAuthorize("@ss.hasPermission('erp:mrp-plan:query')")
    public CommonResult<List<ErpMrpResultComponentRespVO>> getResultComponentList(@RequestParam("resultId") Long resultId) {
        List<ErpMrpResultComponentDO> list = mrpPlanService.getResultComponentList(resultId);
        return success(BeanUtils.toBean(list, ErpMrpResultComponentRespVO.class));
    }

    @GetMapping("/shortage")
    @Operation(summary = "获得 MRP 缺料清单")
    @PreAuthorize("@ss.hasPermission('erp:mrp-plan:query')")
    public CommonResult<List<ErpMrpShortageRespVO>> getShortageList(@RequestParam("planId") Long planId) {
        List<ErpMrpShortageDO> list = mrpPlanService.getShortageList(planId);
        Set<Long> productIds = new HashSet<>(convertSet(list, ErpMrpShortageDO::getMaterialId));
        productIds.addAll(convertSet(list, ErpMrpShortageDO::getRootProductId));
        Set<Long> rootProductIds = convertSet(list, ErpMrpShortageDO::getRootProductId);
        List<ErpBomDO> bomList = bomService.getEffectiveBomList(rootProductIds);
        Map<Long, ErpBomDO> bomMap = convertMap(bomList, ErpBomDO::getProductId);
        Set<Long> bomIds = convertSet(bomList, ErpBomDO::getId);
        List<ErpBomItemDO> bomItemList = bomService.getBomItemListByBomIds(bomIds);
        Map<Long, Map<Long, ErpBomItemDO>> bomItemMap = new java.util.HashMap<>();
        Map<Long, ErpBomItemDO> bomItemByIdMap = new java.util.HashMap<>();
        for (ErpBomItemDO bomItem : bomItemList) {
            bomItemMap.computeIfAbsent(bomItem.getBomId(), key -> new java.util.HashMap<>())
                    .put(bomItem.getMaterialId(), bomItem);
            bomItemByIdMap.put(bomItem.getId(), bomItem);
        }
        List<ErpBomItemSubstituteDO> substituteList = bomService.getBomItemSubstituteList(convertSet(bomItemList, ErpBomItemDO::getId));
        Map<Long, List<ErpBomItemSubstituteDO>> substituteMap = new java.util.HashMap<>();
        for (ErpBomItemSubstituteDO substitute : substituteList) {
            substituteMap.computeIfAbsent(substitute.getBomItemId(), key -> new ArrayList<>()).add(substitute);
        }
        productIds.addAll(convertSet(substituteList, ErpBomItemSubstituteDO::getSubstituteMaterialId));
        Map<Long, ErpProductRespVO> productMap = productService.getProductVOMap(productIds);
        return success(BeanUtils.toBean(list, ErpMrpShortageRespVO.class, item -> {
            ErpProductRespVO material = productMap.get(item.getMaterialId());
            if (material != null) {
                item.setMaterialName(material.getName());
            }
            ErpProductRespVO rootProduct = productMap.get(item.getRootProductId());
            if (rootProduct != null) {
                item.setRootProductName(rootProduct.getName());
            }
            item.setSubstitutes(buildShortageSubstitutes(item, bomMap, bomItemMap, bomItemByIdMap, substituteMap, productMap));
        }));
    }

    private List<ErpMrpShortageRespVO.Substitute> buildShortageSubstitutes(ErpMrpShortageRespVO shortage,
                                                                            Map<Long, ErpBomDO> bomMap,
                                                                            Map<Long, Map<Long, ErpBomItemDO>> bomItemMap,
                                                                            Map<Long, ErpBomItemDO> bomItemByIdMap,
                                                                            Map<Long, List<ErpBomItemSubstituteDO>> substituteMap,
                                                                            Map<Long, ErpProductRespVO> productMap) {
        ErpBomDO bom = bomMap.get(shortage.getRootProductId());
        if (bom == null) {
            return List.of();
        }
        ErpBomItemDO bomItem = shortage.getBomItemId() == null ? null : bomItemByIdMap.get(shortage.getBomItemId());
        if (bomItem == null) {
            Map<Long, ErpBomItemDO> bomItemByMaterialMap = bomItemMap.get(bom.getId());
            if (bomItemByMaterialMap == null) {
                return List.of();
            }
            bomItem = bomItemByMaterialMap.get(shortage.getMaterialId());
        }
        if (bomItem == null) {
            return List.of();
        }
        List<ErpBomItemSubstituteDO> substitutes = substituteMap.get(bomItem.getId());
        if (substitutes == null || substitutes.isEmpty()) {
            return List.of();
        }
        List<ErpBomItemSubstituteDO> sortedSubstitutes = new ArrayList<>(substitutes);
        sortedSubstitutes.sort(Comparator
                .comparing(ErpBomItemSubstituteDO::getPriority, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(ErpBomItemSubstituteDO::getSort, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(ErpBomItemSubstituteDO::getId, Comparator.nullsLast(Long::compareTo)));
        return BeanUtils.toBean(sortedSubstitutes, ErpMrpShortageRespVO.Substitute.class, substitute -> {
            ErpProductRespVO substituteProduct = productMap.get(substitute.getSubstituteMaterialId());
            if (substituteProduct != null) {
                substitute.setSubstituteMaterialName(substituteProduct.getName());
            }
        });
    }

}
