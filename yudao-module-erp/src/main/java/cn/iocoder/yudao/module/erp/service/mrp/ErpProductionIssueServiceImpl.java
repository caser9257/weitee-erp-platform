package cn.iocoder.yudao.module.erp.service.mrp;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.number.MoneyUtils;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.issue.ErpProductionIssueCreateReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.issue.ErpProductionIssuePageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.issue.ErpProductionIssueRecommendReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.issue.ErpProductionIssueRecommendRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.issue.ErpProductionIssueRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionIssueBatchDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionIssueDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionIssueItemDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionMaterialDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionOrderDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseInItemDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpStockBatchDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpStockDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpWarehouseDO;
import cn.iocoder.yudao.module.erp.dal.mysql.mrp.ErpProductionIssueBatchMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.mrp.ErpProductionIssueItemMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.mrp.ErpProductionIssueMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.mrp.ErpProductionMaterialMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseInItemMapper;
import cn.iocoder.yudao.module.erp.dal.redis.no.ErpNoRedisDAO;
import cn.iocoder.yudao.module.erp.enums.stock.ErpStockRecordBizTypeEnum;
import cn.iocoder.yudao.module.erp.service.product.ErpProductService;
import cn.iocoder.yudao.module.erp.service.stock.ErpStockBatchService;
import cn.iocoder.yudao.module.erp.service.stock.ErpStockRecordService;
import cn.iocoder.yudao.module.erp.service.stock.ErpStockService;
import cn.iocoder.yudao.module.erp.service.stock.ErpWarehouseService;
import cn.iocoder.yudao.module.erp.service.stock.bo.ErpStockBatchChangeReqBO;
import cn.iocoder.yudao.module.erp.service.stock.bo.ErpStockRecordCreateReqBO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMultiMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.PRODUCTION_ISSUE_BATCH_COUNT_MISMATCH;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.PRODUCTION_MATERIAL_ORDER_MISMATCH;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.PRODUCTION_MATERIAL_QTY_INVALID;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.PRODUCTION_ORDER_NOT_EXISTS;

@Service
@Validated
public class ErpProductionIssueServiceImpl implements ErpProductionIssueService {

    private static final Integer DONE_STATUS = 20;
    private static final String PURCHASE_IN_SOURCE_BIZ_TYPE = "PURCHASE_IN";

    @Resource
    private ErpProductionMaterialService productionMaterialService;
    @Resource
    private ErpProductionOrderService productionOrderService;
    @Resource
    private ErpProductionMaterialMapper erpProductionMaterialMapper;
    @Resource
    private ErpProductionIssueMapper erpProductionIssueMapper;
    @Resource
    private ErpProductionIssueItemMapper erpProductionIssueItemMapper;
    @Resource
    private ErpProductionIssueBatchMapper erpProductionIssueBatchMapper;
    @Resource
    private ErpPurchaseInItemMapper erpPurchaseInItemMapper;
    @Resource
    private ErpStockBatchService stockBatchService;
    @Resource
    private ErpStockRecordService stockRecordService;
    @Resource
    private ErpStockService stockService;
    @Resource
    private ErpProductService productService;
    @Resource
    private ErpWarehouseService warehouseService;
    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private ErpNoRedisDAO noRedisDAO;
    @Resource
    private ErpProductionIssueVoucherService productionIssueVoucherService;

    @Override
    public ErpProductionIssueRecommendRespVO recommend(ErpProductionIssueRecommendReqVO reqVO) {
        ErpProductionMaterialDO material = productionMaterialService.validateProductionMaterial(reqVO.getProductionMaterialId());
        List<ErpStockBatchDO> batchList = stockBatchService.getAvailableStockBatchList(material.getMaterialId(), reqVO.getWarehouseId());
        BigDecimal totalAvailableQty = batchList.stream()
                .map(item -> ObjectUtil.defaultIfNull(item.getAvailableQty(), BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal remainingQty = reqVO.getIssueQty();
        List<ErpProductionIssueRecommendRespVO.BatchAllocation> allocations = new ArrayList<>();
        for (ErpStockBatchDO batch : batchList) {
            BigDecimal availableQty = ObjectUtil.defaultIfNull(batch.getAvailableQty(), BigDecimal.ZERO);
            BigDecimal recommendedQty = remainingQty.compareTo(BigDecimal.ZERO) > 0
                    ? availableQty.min(remainingQty) : BigDecimal.ZERO;
            remainingQty = remainingQty.subtract(recommendedQty);
            ErpProductionIssueRecommendRespVO.BatchAllocation allocation =
                    BeanUtils.toBean(batch, ErpProductionIssueRecommendRespVO.BatchAllocation.class);
            allocation.setStockBatchId(batch.getId());
            allocation.setRecommendedQty(recommendedQty);
            allocations.add(allocation);
        }
        ErpProductionIssueRecommendRespVO respVO = new ErpProductionIssueRecommendRespVO();
        respVO.setTotalAvailableQty(totalAvailableQty);
        respVO.setGapQty(remainingQty.compareTo(BigDecimal.ZERO) > 0 ? remainingQty : BigDecimal.ZERO);
        respVO.setBatchAllocations(allocations);
        return respVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createProductionIssue(ErpProductionIssueCreateReqVO reqVO) {
        ErpProductionOrderDO order = productionOrderService.getProductionOrder(reqVO.getProductionOrderId());
        if (order == null) {
            throw exception(PRODUCTION_ORDER_NOT_EXISTS);
        }
        List<ErpProductionIssueCreateReqVO.Item> reqItems = reqVO.getItems();
        Map<Long, ErpProductionMaterialDO> materialMap = convertMap(
                erpProductionMaterialMapper.selectListByIds(reqItems.stream()
                        .map(ErpProductionIssueCreateReqVO.Item::getProductionMaterialId).toList()),
                ErpProductionMaterialDO::getId);
        Set<Long> stockBatchIds = reqItems.stream()
                .flatMap(item -> item.getBatches().stream())
                .map(ErpProductionIssueCreateReqVO.Batch::getStockBatchId)
                .collect(Collectors.toSet());
        Map<Long, ErpStockBatchDO> stockBatchMap = convertMap(stockBatchService.getStockBatchList(stockBatchIds),
                ErpStockBatchDO::getId);
        Set<Long> purchaseInItemIds = stockBatchMap.values().stream()
                .filter(stockBatch -> PURCHASE_IN_SOURCE_BIZ_TYPE.equals(stockBatch.getSourceBizType()))
                .map(ErpStockBatchDO::getSourceBizItemId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, ErpPurchaseInItemDO> purchaseInItemMap = convertMap(
                erpPurchaseInItemMapper.selectListByIds(purchaseInItemIds),
                ErpPurchaseInItemDO::getId);

        ErpProductionIssueDO issue = new ErpProductionIssueDO()
                .setIssueNo(noRedisDAO.generate("SCLL"))
                .setProductionOrderId(reqVO.getProductionOrderId())
                .setIssueTime(LocalDateTime.now())
                .setStatus(DONE_STATUS)
                .setIssueAmount(BigDecimal.ZERO)
                .setRemark(reqVO.getRemark());
        erpProductionIssueMapper.insert(issue);

        BigDecimal totalIssueAmount = BigDecimal.ZERO;
        List<ErpProductionIssueItemDO> createdIssueItems = new ArrayList<>();
        for (ErpProductionIssueCreateReqVO.Item item : reqItems) {
            ErpProductionMaterialDO material = materialMap.get(item.getProductionMaterialId());
            validateIssueItem(order, material, item);
            BigDecimal itemIssueAmount = BigDecimal.ZERO;
            List<ErpStockBatchDO> batchList = new ArrayList<>(item.getBatches().size());
            for (ErpProductionIssueCreateReqVO.Batch batch : item.getBatches()) {
                ErpStockBatchDO stockBatch = stockBatchMap.get(batch.getStockBatchId());
                validateIssueBatch(item, batch, stockBatch);
                batchList.add(stockBatch);
                itemIssueAmount = itemIssueAmount.add(calculateIssueAmount(stockBatch, batch.getIssueQty(), purchaseInItemMap));
            }
            ErpProductionIssueItemDO issueItem = new ErpProductionIssueItemDO()
                    .setIssueId(issue.getId())
                    .setProductionMaterialId(item.getProductionMaterialId())
                    .setMaterialId(item.getMaterialId())
                    .setWarehouseId(item.getWarehouseId())
                    .setIssueQty(item.getIssueQty())
                    .setIssueAmount(itemIssueAmount)
                    .setRemark(item.getRemark());
            erpProductionIssueItemMapper.insert(issueItem);
            createdIssueItems.add(issueItem);

            for (int i = 0; i < item.getBatches().size(); i++) {
                ErpProductionIssueCreateReqVO.Batch batch = item.getBatches().get(i);
                ErpStockBatchDO stockBatch = batchList.get(i);
                stockBatchService.decreaseBatch(new ErpStockBatchChangeReqBO(
                        stockBatch.getId(), batch.getIssueQty(), ErpStockRecordBizTypeEnum.PRODUCTION_ISSUE.getType(),
                        issue.getId(), issueItem.getId(), issue.getIssueNo(), item.getRemark()));
                // 获取加权平均成本作为发料价格
                ErpStockDO stock = stockService.getStock(item.getMaterialId(), item.getWarehouseId());
                BigDecimal price = stock != null ? stock.getAverageCost() : null;
                BigDecimal amount = price != null ? price.multiply(batch.getIssueQty()) : null;
                stockRecordService.createStockRecord(new ErpStockRecordCreateReqBO(
                        item.getMaterialId(), item.getWarehouseId(), batch.getIssueQty().negate(),
                        ErpStockRecordBizTypeEnum.PRODUCTION_ISSUE.getType(), issue.getId(), issueItem.getId(), issue.getIssueNo(),
                        price, amount));
                erpProductionIssueBatchMapper.insert(new ErpProductionIssueBatchDO()
                        .setIssueItemId(issueItem.getId())
                        .setStockBatchId(stockBatch.getId())
                        .setBatchNo(stockBatch.getBatchNo())
                        .setIssueQty(batch.getIssueQty())
                        .setInboundTime(stockBatch.getInboundTime())
                        .setProduceDate(stockBatch.getProduceDate())
                        .setExpireDate(stockBatch.getExpireDate()));
            }
            erpProductionMaterialMapper.updateIssuedQtyIncrement(material.getId(), item.getIssueQty());
            totalIssueAmount = totalIssueAmount.add(itemIssueAmount);
        }
        issue.setIssueAmount(totalIssueAmount);
        erpProductionIssueMapper.updateById(new ErpProductionIssueDO()
                .setId(issue.getId())
                .setIssueAmount(totalIssueAmount));
        productionIssueVoucherService.createVoucher(issue, createdIssueItems);
        return issue.getId();
    }

    @Override
    public ErpProductionIssueDO getProductionIssue(Long id) {
        return erpProductionIssueMapper.selectById(id);
    }

    @Override
    public PageResult<ErpProductionIssueDO> getProductionIssuePage(ErpProductionIssuePageReqVO pageReqVO) {
        return erpProductionIssueMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ErpProductionIssueItemDO> getProductionIssueItemListByIssueId(Long issueId) {
        return erpProductionIssueItemMapper.selectListByIssueId(issueId);
    }

    @Override
    public List<ErpProductionIssueItemDO> getProductionIssueItemListByIssueIds(Collection<Long> issueIds) {
        if (CollUtil.isEmpty(issueIds)) {
            return Collections.emptyList();
        }
        return erpProductionIssueItemMapper.selectListByIssueIds(issueIds);
    }

    @Override
    public List<ErpProductionIssueBatchDO> getProductionIssueBatchListByIssueItemIds(Collection<Long> issueItemIds) {
        if (CollUtil.isEmpty(issueItemIds)) {
            return Collections.emptyList();
        }
        return erpProductionIssueBatchMapper.selectListByIssueItemIds(issueItemIds);
    }

    private void validateIssueItem(ErpProductionOrderDO order, ErpProductionMaterialDO material,
                                   ErpProductionIssueCreateReqVO.Item item) {
        if (material == null || ObjectUtil.notEqual(material.getProductionOrderId(), order.getId())
                || ObjectUtil.notEqual(material.getMaterialId(), item.getMaterialId())) {
            throw exception(PRODUCTION_MATERIAL_ORDER_MISMATCH);
        }
        if (item.getIssueQty().compareTo(BigDecimal.ZERO) <= 0) {
            throw exception(PRODUCTION_MATERIAL_QTY_INVALID);
        }
        BigDecimal remainingIssueQty = ErpProductionMaterialServiceImpl.calculateRemainingIssueQty(material);
        if (item.getIssueQty().compareTo(remainingIssueQty) > 0) {
            throw exception(PRODUCTION_MATERIAL_QTY_INVALID);
        }
        BigDecimal batchTotalQty = item.getBatches().stream()
                .map(ErpProductionIssueCreateReqVO.Batch::getIssueQty)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (batchTotalQty.compareTo(item.getIssueQty()) != 0) {
            throw exception(PRODUCTION_ISSUE_BATCH_COUNT_MISMATCH);
        }
    }

    private void validateIssueBatch(ErpProductionIssueCreateReqVO.Item item,
                                    ErpProductionIssueCreateReqVO.Batch batch,
                                    ErpStockBatchDO stockBatch) {
        if (stockBatch == null || ObjectUtil.notEqual(stockBatch.getProductId(), item.getMaterialId())
                || ObjectUtil.notEqual(stockBatch.getWarehouseId(), item.getWarehouseId())
                || ObjectUtil.notEqual(stockBatch.getBatchNo(), batch.getBatchNo())) {
            throw exception(PRODUCTION_MATERIAL_QTY_INVALID);
        }
    }

    private BigDecimal calculateIssueAmount(ErpStockBatchDO stockBatch, BigDecimal issueQty,
                                            Map<Long, ErpPurchaseInItemDO> purchaseInItemMap) {
        BigDecimal unitPrice = resolveStockBatchUnitPrice(stockBatch, purchaseInItemMap);
        if (unitPrice.compareTo(BigDecimal.ZERO) <= 0 || issueQty == null) {
            return BigDecimal.ZERO;
        }
        return MoneyUtils.priceMultiply(unitPrice, issueQty);
    }

    private BigDecimal resolveStockBatchUnitPrice(ErpStockBatchDO stockBatch,
                                                  Map<Long, ErpPurchaseInItemDO> purchaseInItemMap) {
        if (stockBatch == null || !PURCHASE_IN_SOURCE_BIZ_TYPE.equals(stockBatch.getSourceBizType())
                || stockBatch.getSourceBizItemId() == null) {
            return BigDecimal.ZERO;
        }
        ErpPurchaseInItemDO purchaseInItem = purchaseInItemMap.get(stockBatch.getSourceBizItemId());
        if (purchaseInItem == null || purchaseInItem.getCount() == null
                || purchaseInItem.getCount().compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return ObjectUtil.defaultIfNull(purchaseInItem.getTotalPrice(), BigDecimal.ZERO)
                .divide(purchaseInItem.getCount(), 6, RoundingMode.HALF_UP);
    }

    private ErpProductionIssueRespVO buildIssueVO(ErpProductionIssueDO issue, List<ErpProductionIssueItemDO> itemList) {
        Map<Long, ErpProductionOrderDO> productionOrderMap = issue.getProductionOrderId() == null
                ? Collections.emptyMap()
                : convertMap(productionOrderService.getProductionOrderList(Collections.singleton(issue.getProductionOrderId())),
                ErpProductionOrderDO::getId);
        Long creatorId = NumberUtils.parseLong(issue.getCreator());
        Map<Long, AdminUserRespDTO> userMap = creatorId == null ? Collections.emptyMap()
                : adminUserApi.getUserMap(Collections.singleton(creatorId));
        Map<Long, ErpProductRespVO> productMap = convertSet(itemList, ErpProductionIssueItemDO::getMaterialId).isEmpty()
                ? Collections.emptyMap()
                : productService.getProductVOMap(convertSet(itemList, ErpProductionIssueItemDO::getMaterialId));
        Map<Long, ErpWarehouseDO> warehouseMap = convertSet(itemList, ErpProductionIssueItemDO::getWarehouseId).isEmpty()
                ? Collections.emptyMap()
                : warehouseService.getWarehouseMap(convertSet(itemList, ErpProductionIssueItemDO::getWarehouseId));
        Map<Long, List<ErpProductionIssueBatchDO>> issueBatchMap = itemList.isEmpty()
                ? Collections.emptyMap()
                : convertMultiMap(erpProductionIssueBatchMapper.selectListByIssueItemIds(
                convertSet(itemList, ErpProductionIssueItemDO::getId)), ErpProductionIssueBatchDO::getIssueItemId);

        ErpProductionIssueRespVO vo = BeanUtils.toBean(issue, ErpProductionIssueRespVO.class);
        MapUtils.findAndThen(productionOrderMap, issue.getProductionOrderId(), order -> vo.setProductionOrderNo(order.getOrderNo()));
        MapUtils.findAndThen(userMap, creatorId, user -> vo.setCreatorName(user.getNickname()));
        vo.setStatusName(resolveStatusName(issue.getStatus()));
        vo.setItems(convertList(itemList, item -> {
            ErpProductionIssueRespVO.Item itemVO = BeanUtils.toBean(item, ErpProductionIssueRespVO.Item.class);
            MapUtils.findAndThen(productMap, item.getMaterialId(), product -> itemVO
                    .setMaterialName(product.getName())
                    .setMaterialCode(product.getMaterialCode())
                    .setMaterialBarCode(product.getBarCode())
                    .setProductUnitName(product.getUnitName()));
            MapUtils.findAndThen(warehouseMap, item.getWarehouseId(), warehouse -> itemVO.setWarehouseName(warehouse.getName()));
            itemVO.setBatches(convertList(issueBatchMap.getOrDefault(item.getId(), Collections.emptyList()),
                    batch -> BeanUtils.toBean(batch, ErpProductionIssueRespVO.Batch.class)));
            return itemVO;
        }));
        return vo;
    }

    private String resolveStatusName(Integer status) {
        return ObjectUtil.equals(status, DONE_STATUS) ? "已完成" : null;
    }

}
