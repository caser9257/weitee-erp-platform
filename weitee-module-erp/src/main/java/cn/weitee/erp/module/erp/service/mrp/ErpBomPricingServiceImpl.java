package cn.weitee.erp.module.erp.service.mrp;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpBomDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpBomItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductDO;
import cn.weitee.erp.module.erp.enums.mrp.ErpBomPricingStatusEnum;
import cn.weitee.erp.module.erp.service.mrp.support.ErpBomPricingPreviewResult;
import cn.weitee.erp.module.erp.service.mrp.support.ErpBomPricingPreviewResult.Item;
import cn.weitee.erp.module.erp.service.mrp.support.ErpBomPricingPreviewResult.MissingMaterial;
import cn.weitee.erp.module.erp.service.product.ErpProductService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCT_NOT_EXISTS;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Validated
public class ErpBomPricingServiceImpl implements ErpBomPricingService {

    private static final int PRICE_SCALE = 2;
    private static final int QTY_SCALE = 6;
    private static final String SUPPLY_OWNER_CUSTOMER = "CUSTOMER";
    private static final Integer MATERIAL_TYPE_MAKE = 1;

    @Resource
    private ErpBomService bomService;
    @Resource
    private ErpProductService productService;

    @Override
    public ErpBomPricingPreviewResult previewBomPricing(Long productId) {
        PricingContext context = new PricingContext();
        ErpProductDO rootProduct = loadProduct(productId, context);

        ErpBomPricingPreviewResult result = new ErpBomPricingPreviewResult();
        result.setProductId(rootProduct.getId());
        result.setProductName(rootProduct.getName());

        ErpBomDO rootBom = getBom(context, productId);
        if (rootBom == null) {
            fillNoBomResult(result, rootProduct);
            return result;
        }

        result.setBomId(rootBom.getId());
        result.setBomCode(rootBom.getBomCode());
        result.setBomVersion(rootBom.getVersion());

        CalcState calcState = new CalcState();
        BigDecimal unitPrice = calculateNode(productId, BigDecimal.ONE, 0, context, calcState, result.getItems(),
                result.getMissingMaterials());
        if (calcState.cyclic) {
            result.setCalcStatus(ErpBomPricingStatusEnum.CYCLIC_BOM.getStatus());
            result.setCalcMessage("\u0042\u004f\u004d \u5b58\u5728\u5faa\u73af\u5f15\u7528");
            result.setMaterialUnitPrice(null);
            return result;
        }
        if (calcState.missingPurchasePrice) {
            result.setCalcStatus(ErpBomPricingStatusEnum.MISSING_PURCHASE_PRICE.getStatus());
            result.setCalcMessage("\u5b58\u5728\u672a\u7ef4\u62a4\u91c7\u8d2d\u4ef7\u7684\u7269\u6599");
            result.setMaterialUnitPrice(null);
            return result;
        }
        result.setCalcStatus(ErpBomPricingStatusEnum.SUCCESS.getStatus());
        result.setCalcMessage("\u7b97\u4ef7\u6210\u529f");
        result.setMaterialUnitPrice(scalePrice(unitPrice));
        return result;
    }

    private void fillNoBomResult(ErpBomPricingPreviewResult result, ErpProductDO rootProduct) {
        BigDecimal purchasePrice = rootProduct.getPurchasePrice();
        if (purchasePrice == null) {
            result.setCalcStatus(ErpBomPricingStatusEnum.MISSING_PURCHASE_PRICE.getStatus());
            result.setCalcMessage("\u672a\u627e\u5230\u6709\u6548BOM\uff0c\u4e14\u4ea7\u54c1\u672a\u7ef4\u62a4\u91c7\u8d2d\u4ef7");
            result.setMaterialUnitPrice(null);
            result.getMissingMaterials().add(buildMissingMaterial(rootProduct.getId(), rootProduct.getName(), null,
                    0, BigDecimal.ONE, "\u4ea7\u54c1\u672a\u7ef4\u62a4\u91c7\u8d2d\u4ef7"));
            return;
        }
        result.setCalcStatus(ErpBomPricingStatusEnum.NO_EFFECTIVE_BOM.getStatus());
        result.setCalcMessage("\u672a\u627e\u5230\u6709\u6548BOM\uff0c\u5df2\u56de\u9000\u4ea7\u54c1\u91c7\u8d2d\u4ef7");
        result.setMaterialUnitPrice(scalePrice(purchasePrice));
        Item item = new Item();
        item.setMaterialId(rootProduct.getId());
        item.setMaterialName(rootProduct.getName());
        item.setLevel(0);
        item.setRequiredQty(BigDecimal.ONE);
        item.setUnitPrice(scalePrice(purchasePrice));
        item.setLineCost(scalePrice(purchasePrice));
        result.getItems().add(item);
    }

    private BigDecimal calculateNode(Long productId,
                                     BigDecimal requiredQty,
                                     int level,
                                     PricingContext context,
                                     CalcState calcState,
                                     List<Item> detailItems,
                                     List<MissingMaterial> missingMaterials) {
        if (context.path.contains(productId)) {
            calcState.cyclic = true;
            return BigDecimal.ZERO;
        }
        context.path.add(productId);

        ErpProductDO product = loadProduct(productId, context);
        ErpBomDO bom = getBom(context, productId);
        if (bom == null) {
            BigDecimal purchasePrice = product.getPurchasePrice();
            if (purchasePrice == null) {
                calcState.missingPurchasePrice = true;
                missingMaterials.add(buildMissingMaterial(productId, product.getName(), null, level, requiredQty,
                        "\u4ea7\u54c1\u672a\u7ef4\u62a4\u91c7\u8d2d\u4ef7"));
                context.path.remove(productId);
                return BigDecimal.ZERO;
            }
            BigDecimal lineCost = multiply(requiredQty, purchasePrice);
            detailItems.add(buildItem(productId, product.getName(), null, null, level, requiredQty, purchasePrice,
                    lineCost, null, null, null));
            context.path.remove(productId);
            return lineCost;
        }

        BigDecimal totalCost = BigDecimal.ZERO;
        List<ErpBomItemDO> bomItems = getBomItems(context, bom.getId());
        if (CollUtil.isEmpty(bomItems)) {
            context.path.remove(productId);
            return BigDecimal.ZERO;
        }
        for (ErpBomItemDO bomItem : bomItems) {
            BigDecimal childQty = calculateChildQty(requiredQty, bomItem);
            if (SUPPLY_OWNER_CUSTOMER.equals(bomItem.getSupplyOwner())) {
                ErpProductDO childProduct = loadProduct(bomItem.getMaterialId(), context);
                detailItems.add(buildItem(childProduct.getId(), childProduct.getName(), null, null, level + 1, childQty,
                        childProduct.getPurchasePrice() == null ? null : scalePrice(childProduct.getPurchasePrice()),
                        BigDecimal.ZERO, bomItem.getMaterialType(), bomItem.getSupplyOwner(),
                        "\u5ba2\u4f9b\u6599\u4e0d\u8ba1\u6210\u672c"));
                continue;
            }

            ErpBomDO childBom = getBom(context, bomItem.getMaterialId());
            if (childBom != null) {
                totalCost = totalCost.add(calculateNode(bomItem.getMaterialId(), childQty, level + 1, context, calcState,
                        detailItems, missingMaterials));
                continue;
            }

            if (ObjectUtil.equals(bomItem.getMaterialType(), MATERIAL_TYPE_MAKE)) {
                calcState.missingPurchasePrice = true;
                missingMaterials.add(buildMissingMaterial(bomItem.getMaterialId(), resolveProductName(bomItem.getMaterialId(),
                        context), bom.getId(), level + 1, childQty,
                        "\u7269\u6599\u6807\u8bb0\u4e3a\u81ea\u5236\uff0c\u4f46\u672a\u627e\u5230\u6709\u6548BOM"));
                continue;
            }

            ErpProductDO childProduct = loadProduct(bomItem.getMaterialId(), context);
            BigDecimal childPurchasePrice = childProduct.getPurchasePrice();
            if (childPurchasePrice == null) {
                calcState.missingPurchasePrice = true;
                missingMaterials.add(buildMissingMaterial(childProduct.getId(), childProduct.getName(), bom.getId(),
                        level + 1, childQty, "\u7269\u6599\u672a\u7ef4\u62a4\u91c7\u8d2d\u4ef7"));
                continue;
            }
            BigDecimal lineCost = multiply(childQty, childPurchasePrice);
            totalCost = totalCost.add(lineCost);
            detailItems.add(buildItem(childProduct.getId(), childProduct.getName(), bom.getId(), bom.getVersion(), level + 1,
                    childQty, scalePrice(childPurchasePrice), lineCost, bomItem.getMaterialType(), bomItem.getSupplyOwner(),
                    bomItem.getRemark()));
        }

        context.path.remove(productId);
        return totalCost;
    }

    private Item buildItem(Long materialId,
                           String materialName,
                           Long bomId,
                           String bomVersion,
                           int level,
                           BigDecimal requiredQty,
                           BigDecimal unitPrice,
                           BigDecimal lineCost,
                           Integer materialType,
                           String supplyOwner,
                           String remark) {
        Item item = new Item();
        item.setMaterialId(materialId);
        item.setMaterialName(materialName);
        item.setBomId(bomId);
        item.setBomVersion(bomVersion);
        item.setLevel(level);
        item.setRequiredQty(requiredQty);
        item.setUnitPrice(unitPrice);
        item.setLineCost(scalePrice(lineCost));
        item.setMaterialType(materialType);
        item.setSupplyOwner(supplyOwner);
        item.setRemark(remark);
        return item;
    }

    private MissingMaterial buildMissingMaterial(Long materialId, String materialName, Long bomId,
                                                 int level, BigDecimal requiredQty, String reason) {
        MissingMaterial missingMaterial = new MissingMaterial();
        missingMaterial.setMaterialId(materialId);
        missingMaterial.setMaterialName(materialName);
        missingMaterial.setBomId(bomId);
        missingMaterial.setLevel(level);
        missingMaterial.setRequiredQty(requiredQty);
        missingMaterial.setReason(reason);
        return missingMaterial;
    }

    private BigDecimal calculateChildQty(BigDecimal parentQty, ErpBomItemDO bomItem) {
        BigDecimal usageQty = ObjectUtil.defaultIfNull(bomItem.getUsageQty(), BigDecimal.ZERO);
        BigDecimal lossRate = ObjectUtil.defaultIfNull(bomItem.getLossRate(), BigDecimal.ZERO);
        return parentQty.multiply(usageQty)
                .multiply(BigDecimal.ONE.add(lossRate))
                .setScale(QTY_SCALE, RoundingMode.CEILING);
    }

    private BigDecimal multiply(BigDecimal qty, BigDecimal price) {
        return qty.multiply(price).setScale(QTY_SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal scalePrice(BigDecimal price) {
        if (price == null) {
            return null;
        }
        return price.setScale(PRICE_SCALE, RoundingMode.HALF_UP);
    }

    private ErpProductDO loadProduct(Long productId, PricingContext context) {
        return context.productMap.computeIfAbsent(productId, key -> {
            // 计价取数语义：仅校验存在性，不做启停校验（停用/废除物料照常参与历史计价）。
            // 根因：原实现复用 validProductList 会抛 PRODUCT_NOT_ENABLE，导致一张 BOM 因单行停用料整单算不了价。
            ErpProductDO product = productService.getProduct(key);
            if (product == null) {
                throw exception(PRODUCT_NOT_EXISTS);
            }
            return product;
        });
    }

    private String resolveProductName(Long productId, PricingContext context) {
        ErpProductDO product = context.productMap.get(productId);
        if (product != null) {
            return product.getName();
        }
        return loadProduct(productId, context).getName();
    }

    private ErpBomDO getBom(PricingContext context, Long productId) {
        if (!context.bomByProductId.containsKey(productId)) {
            context.bomByProductId.put(productId, bomService.getEffectiveBom(productId));
        }
        return context.bomByProductId.get(productId);
    }

    private List<ErpBomItemDO> getBomItems(PricingContext context, Long bomId) {
        if (!context.bomItemsByBomId.containsKey(bomId)) {
            context.bomItemsByBomId.put(bomId, bomService.getBomItemList(bomId));
        }
        return context.bomItemsByBomId.get(bomId);
    }

    private static final class PricingContext {
        private final Map<Long, ErpProductDO> productMap = new HashMap<>();
        private final Map<Long, ErpBomDO> bomByProductId = new HashMap<>();
        private final Map<Long, List<ErpBomItemDO>> bomItemsByBomId = new HashMap<>();
        private final Set<Long> path = new HashSet<>();
    }

    private static final class CalcState {
        private boolean missingPurchasePrice;
        private boolean cyclic;
    }

    private  static  final class CalcState1{
        private boolean missingPurchasePrice;
        private boolean cyclic;
    }

}
