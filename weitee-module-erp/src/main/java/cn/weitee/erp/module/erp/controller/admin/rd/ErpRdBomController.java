package cn.weitee.erp.module.erp.controller.admin.rd;

import cn.weitee.erp.framework.common.pojo.CommonResult;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.weitee.erp.module.erp.controller.admin.rd.vo.bom.ErpRdBomChangeLogRespVO;
import cn.weitee.erp.module.erp.controller.admin.rd.vo.bom.ErpRdBomImportResultVO;
import cn.weitee.erp.module.erp.controller.admin.rd.vo.bom.ErpRdBomIntegrityIssueRespVO;
import cn.weitee.erp.module.erp.controller.admin.rd.vo.bom.ErpRdBomPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.rd.vo.bom.ErpRdBomRespVO;
import cn.weitee.erp.module.erp.controller.admin.rd.vo.bom.ErpRdBomSaveReqVO;
import cn.weitee.erp.module.erp.controller.admin.rd.vo.bom.ErpRdBomTreeRespVO;
import cn.weitee.erp.module.erp.controller.admin.rd.vo.bom.ErpRdBomWhereUsedRespVO;
import cn.weitee.erp.module.erp.dal.dataobject.rd.ErpRdBomDO;
import cn.weitee.erp.module.erp.dal.dataobject.rd.ErpRdBomItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.rd.ErpRdBomItemSubstituteDO;
import cn.weitee.erp.framework.security.core.util.SecurityFrameworkUtils;
import cn.weitee.erp.module.erp.service.product.ErpProductService;
import cn.weitee.erp.module.erp.service.rd.ErpRdBomBpmService;
import cn.weitee.erp.module.erp.service.rd.ErpRdBomChangeLogService;
import cn.weitee.erp.module.erp.service.rd.ErpRdBomImportService;
import cn.weitee.erp.module.erp.service.rd.ErpRdBomService;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.weitee.erp.framework.common.pojo.CommonResult.success;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertList;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - ERP 研发 BOM")
@RestController
@RequestMapping("/erp/rd-bom")
@Validated
public class ErpRdBomController {

    @Resource
    private ErpRdBomService rdBomService;
    @Resource
    private ErpProductService productService;
    @Resource
    private ErpRdBomImportService rdBomImportService;
    @Resource
    private ErpRdBomBpmService rdBomBpmService;
    @Resource
    private ErpRdBomChangeLogService changeLogService;

    @PostMapping("/create")
    @Operation(summary = "创建研发 BOM")
    @PreAuthorize("@ss.hasPermission('erp:rd-bom:create')")
    public CommonResult<Long> createRdBom(@Valid @RequestBody ErpRdBomSaveReqVO createReqVO) {
        return success(rdBomService.createRdBom(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新研发 BOM")
    @PreAuthorize("@ss.hasPermission('erp:rd-bom:update')")
    public CommonResult<Boolean> updateRdBom(@Valid @RequestBody ErpRdBomSaveReqVO updateReqVO) {
        rdBomService.updateRdBom(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除研发 BOM")
    @PreAuthorize("@ss.hasPermission('erp:rd-bom:delete')")
    public CommonResult<Boolean> deleteRdBom(@RequestParam("id") Long id) {
        rdBomService.deleteRdBom(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得研发 BOM")
    @PreAuthorize("@ss.hasPermission('erp:rd-bom:query')")
    public CommonResult<ErpRdBomRespVO> getRdBom(@RequestParam("id") Long id) {
        return success(buildRespVO(rdBomService.getRdBom(id)));
    }

    @GetMapping("/page")
    @Operation(summary = "获得研发 BOM 分页")
    @PreAuthorize("@ss.hasPermission('erp:rd-bom:query')")
    public CommonResult<PageResult<ErpRdBomRespVO>> getRdBomPage(@Valid ErpRdBomPageReqVO pageReqVO) {
        PageResult<ErpRdBomDO> pageResult = rdBomService.getRdBomPage(pageReqVO);
        return success(new PageResult<>(buildRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    @PutMapping("/publish")
    @Operation(summary = "发布研发 BOM 到制造 BOM 草稿")
    @PreAuthorize("@ss.hasPermission('erp:rd-bom:publish')")
    public CommonResult<Boolean> publishRdBom(@RequestParam("id") Long id) {
        rdBomService.publishRdBom(id);
        return success(true);
    }

    @PostMapping("/start-change")
    @Operation(summary = "发起升版式变更（仅限已审批通过 BOM，生成新 DRAFT 版本，须重新提交审批）")
    @PreAuthorize("@ss.hasPermission('erp:rd-bom:change')")
    public CommonResult<Long> startChangeRdBom(@RequestParam("id") Long id) {
        return success(rdBomService.startChangeRdBom(id));
    }

    @PostMapping("/validate")
    @Operation(summary = "校验研发 BOM 完整性（漏件/悬浮件/用量）")
    @PreAuthorize("@ss.hasPermission('erp:rd-bom:query')")
    public CommonResult<List<ErpRdBomIntegrityIssueRespVO>> validateRdBom(@RequestParam("id") Long id) {
        return success(rdBomService.validateRdBomIntegrity(id));
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "下载研发 BOM 明细导入模板")
    @PreAuthorize("@ss.hasPermission('erp:rd-bom:query')")
    public void downloadImportTemplate(HttpServletResponse response) throws IOException {
        byte[] template = rdBomImportService.downloadTemplate();
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=rd-bom-import-template.xlsx");
        response.getOutputStream().write(template);
        response.getOutputStream().flush();
    }

    @PostMapping("/import")
    @Operation(summary = "按单导入研发 BOM 明细（智能识别表头，导入即跑完整性校验）")
    @PreAuthorize("@ss.hasPermission('erp:rd-bom:create')")
    public CommonResult<ErpRdBomImportResultVO> importRdBom(
            @RequestParam(value = "productId", required = false) Long productId,
            @RequestParam(value = "bomCode", required = false) String bomCode,
            @RequestParam(value = "version", required = false) String version,
            @RequestParam(value = "remark", required = false) String remark,
            @RequestParam(value = "updateSupport", required = false, defaultValue = "false") Boolean updateSupport,
            @RequestParam("file") MultipartFile file) {
        return success(rdBomImportService.importRdBom(productId, bomCode, version, remark, updateSupport, file));
    }

    @PostMapping("/submit")
    @Operation(summary = "提交研发 BOM 审批")
    @PreAuthorize("@ss.hasPermission('erp:rd-bom:submit')")
    public CommonResult<String> submitRdBom(@RequestParam("id") Long id) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        return success(rdBomBpmService.submitRdBom(userId, id));
    }

    @PostMapping("/cancel")
    @Operation(summary = "撤回研发 BOM 审批")
    @PreAuthorize("@ss.hasPermission('erp:rd-bom:cancel')")
    public CommonResult<Boolean> cancelRdBom(@RequestParam("id") Long id,
                                             @RequestParam(value = "reason", required = false) String reason) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        rdBomBpmService.cancelRdBomApproval(userId, id, reason);
        return success(true);
    }

    @GetMapping("/tree")
    @Operation(summary = "获得研发 BOM 结构树（正向穿透，支持多级展开）")
    @PreAuthorize("@ss.hasPermission('erp:rd-bom:query')")
    public CommonResult<ErpRdBomTreeRespVO> getRdBomTree(
            @RequestParam(value = "bomId", required = false) Long bomId,
            @RequestParam(value = "productId", required = false) Long productId) {
        ErpRdBomDO root = bomId != null ? rdBomService.getRdBom(bomId)
                : productId != null ? rdBomService.getLatestRdBomByProductId(productId) : null;
        return success(buildRdBomTreeRespVO(root, 1, new HashSet<>(), new RdBomTreeContext()));
    }

    @GetMapping("/where-used")
    @Operation(summary = "反向追溯：按物料查上级研发 BOM（Where-Used，递归向上）")
    @PreAuthorize("@ss.hasPermission('erp:rd-bom:query')")
    public CommonResult<List<ErpRdBomWhereUsedRespVO>> getWhereUsed(@RequestParam("materialId") Long materialId) {
        return success(rdBomService.getWhereUsed(materialId));
    }

    @GetMapping("/change-log")
    @Operation(summary = "获得研发 BOM 变更记录")
    @PreAuthorize("@ss.hasPermission('erp:rd-bom:query')")
    public CommonResult<List<ErpRdBomChangeLogRespVO>> getChangeLog(@RequestParam("bomId") Long bomId) {
        return success(changeLogService.getChangeLogList(bomId));
    }

    private List<ErpRdBomRespVO> buildRespVOList(List<ErpRdBomDO> list) {
        return convertList(list, this::buildRespVO);
    }

    private ErpRdBomRespVO buildRespVO(ErpRdBomDO rdBom) {
        if (rdBom == null) {
            return null;
        }
        ErpRdBomRespVO respVO = BeanUtils.toBean(rdBom, ErpRdBomRespVO.class);
        fillRespVO(respVO, rdBomService.getRdBomItemList(rdBom.getId()));
        return respVO;
    }

    private void fillRespVO(ErpRdBomRespVO respVO, List<ErpRdBomItemDO> itemList) {
        if (itemList == null) {
            itemList = List.of();
        }
        Map<Long, ErpProductRespVO> productMap = productService.getProductVOMap(convertSet(itemList, ErpRdBomItemDO::getMaterialId));
        List<ErpRdBomItemSubstituteDO> substituteList = rdBomService.getRdBomItemSubstituteList(convertSet(itemList, ErpRdBomItemDO::getId));
        Map<Long, ErpProductRespVO> substituteProductMap = productService.getProductVOMap(convertSet(substituteList, ErpRdBomItemSubstituteDO::getSubstituteMaterialId));
        Map<Long, List<ErpRdBomItemSubstituteDO>> substituteMap = new java.util.HashMap<>();
        for (ErpRdBomItemSubstituteDO substitute : substituteList) {
            substituteMap.computeIfAbsent(substitute.getBomItemId(), key -> new ArrayList<>()).add(substitute);
        }
        ErpProductRespVO product = productService.getProductVOMap(List.of(respVO.getProductId())).get(respVO.getProductId());
        if (product != null) {
            respVO.setProductName(product.getName());
        }
        respVO.setItems(BeanUtils.toBean(itemList, ErpRdBomRespVO.Item.class, item -> {
            ErpProductRespVO material = productMap.get(item.getMaterialId());
            if (material != null) {
                item.setMaterialName(material.getName());
                item.setUnitName(material.getUnitName());
            }
            List<ErpRdBomItemSubstituteDO> itemSubstitutes = substituteMap.get(item.getId());
            if (itemSubstitutes == null || itemSubstitutes.isEmpty()) {
                item.setSubstitutes(List.of());
                return;
            }
            item.setSubstitutes(BeanUtils.toBean(itemSubstitutes, ErpRdBomRespVO.Item.Substitute.class, substitute -> {
                ErpProductRespVO substituteProduct = substituteProductMap.get(substitute.getSubstituteMaterialId());
                if (substituteProduct != null) {
                    substitute.setSubstituteMaterialName(substituteProduct.getName());
                }
            }));
        }));
    }

    private ErpRdBomTreeRespVO buildRdBomTreeRespVO(ErpRdBomDO bom, int level, Set<Long> pathBomIds,
                                                   RdBomTreeContext context) {
        if (bom == null) {
            return null;
        }
        ErpRdBomTreeRespVO respVO = BeanUtils.toBean(bom, ErpRdBomTreeRespVO.class);
        ErpProductRespVO product = getProduct(context, bom.getProductId());
        if (product != null) {
            respVO.setProductName(product.getName());
        }
        respVO.setLevel(level);
        if (!pathBomIds.add(bom.getId())) {
            respVO.setItems(List.of());
            return respVO;
        }
        try {
            respVO.setItems(buildRdBomTreeItems(getRdBomItems(context, bom.getId()), level, pathBomIds, context));
            return respVO;
        } finally {
            pathBomIds.remove(bom.getId());
        }
    }

    private List<ErpRdBomTreeRespVO.Item> buildRdBomTreeItems(List<ErpRdBomItemDO> itemList, int level,
                                                            Set<Long> pathBomIds, RdBomTreeContext context) {
        if (itemList == null || itemList.isEmpty()) {
            return List.of();
        }
        Map<Long, ErpProductRespVO> productMap = getProducts(context, convertSet(itemList, ErpRdBomItemDO::getMaterialId));
        Map<Long, List<ErpRdBomItemSubstituteDO>> substituteMap = getRdBomItemSubstitutesMap(context,
                convertSet(itemList, ErpRdBomItemDO::getId));
        Map<Long, ErpRdBomDO> childBomMap = getRdBomByProductIds(context, convertSet(itemList, ErpRdBomItemDO::getMaterialId));
        return BeanUtils.toBean(itemList, ErpRdBomTreeRespVO.Item.class, item -> {
            ErpProductRespVO material = productMap.get(item.getMaterialId());
            if (material != null) {
                item.setMaterialName(material.getName());
                item.setUnitName(material.getUnitName());
            }
            item.setLevel(level);
            item.setSubstitutes(buildRdBomItemSubstituteRespVO(substituteMap.get(item.getId()), context));
            ErpRdBomDO childBom = childBomMap.get(item.getMaterialId());
            boolean canExpand = Integer.valueOf(1).equals(item.getMaterialType()) && childBom != null
                    && !pathBomIds.contains(childBom.getId());
            if (!canExpand) {
                item.setHasChildrenBom(false);
                item.setChildren(List.of());
                return;
            }
            item.setHasChildrenBom(true);
            item.setChildBomId(childBom.getId());
            item.setChildBomCode(childBom.getBomCode());
            item.setChildBomVersion(childBom.getVersion());
            ErpRdBomTreeRespVO childRespVO = buildRdBomTreeRespVO(childBom, level + 1, pathBomIds, context);
            item.setChildren(childRespVO != null && childRespVO.getItems() != null ? childRespVO.getItems() : List.of());
        });
    }

    private List<ErpRdBomTreeRespVO.Item.Substitute> buildRdBomItemSubstituteRespVO(List<ErpRdBomItemSubstituteDO> substitutes,
                                                                             RdBomTreeContext context) {
        if (substitutes == null || substitutes.isEmpty()) {
            return List.of();
        }
        return BeanUtils.toBean(substitutes, ErpRdBomTreeRespVO.Item.Substitute.class, substitute -> {
            ErpProductRespVO substituteProduct = getProduct(context, substitute.getSubstituteMaterialId());
            if (substituteProduct != null) {
                substitute.setSubstituteMaterialName(substituteProduct.getName());
            }
        });
    }

    private Map<Long, ErpProductRespVO> getProducts(RdBomTreeContext context, Set<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Map.of();
        }
        Set<Long> missing = new HashSet<>(productIds);
        missing.removeAll(context.productCache.keySet());
        if (!missing.isEmpty()) {
            context.productCache.putAll(productService.getProductVOMap(missing));
        }
        return context.productCache;
    }

    private ErpProductRespVO getProduct(RdBomTreeContext context, Long productId) {
        if (productId == null) {
            return null;
        }
        if (!context.productCache.containsKey(productId)) {
            context.productCache.putAll(productService.getProductVOMap(List.of(productId)));
        }
        return context.productCache.get(productId);
    }

    private Map<Long, ErpRdBomDO> getRdBomByProductIds(RdBomTreeContext context, Set<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Map.of();
        }
        Set<Long> missing = new HashSet<>(productIds);
        missing.removeAll(context.rdBomCache.keySet());
        if (!missing.isEmpty()) {
            context.rdBomCache.putAll(rdBomService.getLatestRdBomMapByProductIds(missing));
        }
        return context.rdBomCache;
    }

    private List<ErpRdBomItemDO> getRdBomItems(RdBomTreeContext context, Long bomId) {
        if (bomId == null) {
            return List.of();
        }
        if (!context.bomItemCache.containsKey(bomId)) {
            context.bomItemCache.put(bomId, rdBomService.getRdBomItemList(bomId));
        }
        return context.bomItemCache.getOrDefault(bomId, List.of());
    }

    private Map<Long, List<ErpRdBomItemSubstituteDO>> getRdBomItemSubstitutesMap(RdBomTreeContext context, Set<Long> bomItemIds) {
        if (bomItemIds == null || bomItemIds.isEmpty()) {
            return Map.of();
        }
        Set<Long> missing = new HashSet<>(bomItemIds);
        missing.removeAll(context.substituteCache.keySet());
        if (!missing.isEmpty()) {
            List<ErpRdBomItemSubstituteDO> substitutes = rdBomService.getRdBomItemSubstituteList(missing);
            Map<Long, List<ErpRdBomItemSubstituteDO>> grouped = new HashMap<>();
            for (ErpRdBomItemSubstituteDO substitute : substitutes) {
                grouped.computeIfAbsent(substitute.getBomItemId(), key -> new ArrayList<>()).add(substitute);
            }
            for (Long bomItemId : missing) {
                context.substituteCache.put(bomItemId, grouped.getOrDefault(bomItemId, List.of()));
            }
        }
        return context.substituteCache;
    }

    private static class RdBomTreeContext {
        private final Map<Long, ErpProductRespVO> productCache = new HashMap<>();
        private final Map<Long, ErpRdBomDO> rdBomCache = new HashMap<>();
        private final Map<Long, List<ErpRdBomItemDO>> bomItemCache = new HashMap<>();
        private final Map<Long, List<ErpRdBomItemSubstituteDO>> substituteCache = new HashMap<>();
    }

}
