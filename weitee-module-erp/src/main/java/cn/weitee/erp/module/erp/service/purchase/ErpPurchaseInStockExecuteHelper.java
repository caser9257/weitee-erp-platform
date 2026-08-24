package cn.weitee.erp.module.erp.service.purchase;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.weitee.erp.framework.common.util.number.MoneyUtils;
import cn.weitee.erp.module.erp.controller.admin.purchase.vo.in.ErpPurchaseInStockExecuteCreateReqVO;
import cn.weitee.erp.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseInDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseInItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseInStockExecuteDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseInStockExecuteItemBatchDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseInStockExecuteItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseSourceBatchDO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockDO;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseInItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseInMapper;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseInStockExecuteItemBatchMapper;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseInStockExecuteItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseInStockExecuteMapper;
import cn.weitee.erp.module.erp.dal.mysql.stock.ErpStockMapper;
import cn.weitee.erp.module.erp.enums.ErpPurchaseInStockExecuteStatusEnum;
import cn.weitee.erp.module.erp.enums.ErpPurchaseInStockInStatusEnum;
import cn.weitee.erp.module.erp.enums.stock.ErpStockRecordBizTypeEnum;
import cn.weitee.erp.module.erp.service.stock.ErpStockBatchService;
import cn.weitee.erp.module.erp.service.stock.ErpStockRecordService;
import cn.weitee.erp.module.erp.service.stock.bo.ErpStockBatchChangeReqBO;
import cn.weitee.erp.module.erp.service.stock.bo.ErpStockBatchInboundReqBO;
import cn.weitee.erp.module.erp.service.stock.bo.ErpStockRecordCreateReqBO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertList;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertMultiMap;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.getSumValue;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PURCHASE_IN_CONFIRM_STOCK_IN_FAIL_STOCK_STATUS;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PURCHASE_IN_STOCK_BATCH_COUNT_MISMATCH;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PURCHASE_IN_STOCK_BATCH_REQUIRED;

/**
 * 采购入库执行辅助类。
 * 提取自 ErpPurchaseInServiceImpl。
 */
@Component
class ErpPurchaseInStockExecuteHelper {

    @Resource
    private ErpPurchaseInMapper erpPurchaseInMapper;
    @Resource
    private ErpPurchaseInItemMapper erpPurchaseInItemMapper;
    @Resource
    private ErpPurchaseInStockExecuteMapper erpPurchaseInStockExecuteMapper;
    @Resource
    private ErpPurchaseInStockExecuteItemMapper erpPurchaseInStockExecuteItemMapper;
    @Resource
    private ErpPurchaseInStockExecuteItemBatchMapper erpPurchaseInStockExecuteItemBatchMapper;
    @Resource
    private ErpStockMapper erpStockMapper;
    @Resource
    private ErpStockRecordService stockRecordService;
    @Resource
    private ErpStockBatchService stockBatchService;
    @Resource
    private ErpPurchaseSourceBatchService purchaseSourceBatchService;

    // region 执行项构建

    public List<ErpPurchaseInStockExecuteItemDO> buildExecuteItems(ErpPurchaseInStockExecuteCreateReqVO reqVO,
                                                                    ErpPurchaseInDO purchaseIn,
                                                                    Map<Long, ErpPurchaseInItemDO> purchaseInItemMap,
                                                                    Map<Long, ErpProductRespVO> productMap,
                                                                    ErpPurchaseInQueryHelper queryHelper) {
        return convertList(reqVO.getItems(), reqItem -> {
            ErpPurchaseInItemDO purchaseInItem = purchaseInItemMap.get(reqItem.getPurchaseInItemId());
            if (purchaseInItem == null || ObjectUtil.notEqual(purchaseInItem.getInId(), purchaseIn.getId())) {
                throw exception(cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PURCHASE_IN_QUALITY_CHECK_FAIL_ITEMS);
            }
            BigDecimal executeCount = ObjectUtil.defaultIfNull(reqItem.getCount(), BigDecimal.ZERO);
            BigDecimal remainingCount = queryHelper.calculateRemainingStockInCount(purchaseInItem.getQaPassCount(), purchaseInItem.getStockInCount());
            if (executeCount.compareTo(BigDecimal.ZERO) <= 0 || executeCount.compareTo(remainingCount) > 0) {
                throw exception(PURCHASE_IN_CONFIRM_STOCK_IN_FAIL_STOCK_STATUS);
            }
            validateExecuteItemBatches(reqItem, executeCount, productMap.get(purchaseInItem.getProductId()));
            return new ErpPurchaseInStockExecuteItemDO()
                    .setPurchaseInId(purchaseIn.getId())
                    .setPurchaseInItemId(purchaseInItem.getId())
                    .setProductId(purchaseInItem.getProductId())
                    .setWarehouseId(purchaseInItem.getWarehouseId())
                    .setCount(executeCount)
                    .setRemark(reqItem.getRemark());
        });
    }

    public void validateExecuteItemBatches(ErpPurchaseInStockExecuteCreateReqVO.Item reqItem,
                                           BigDecimal executeCount,
                                           ErpProductRespVO product) {
        boolean batchManaged = product != null && Boolean.TRUE.equals(product.getBatchControlFlag());
        if (!batchManaged) {
            return;
        }
        if (CollUtil.isEmpty(reqItem.getBatches())) {
            throw exception(PURCHASE_IN_STOCK_BATCH_REQUIRED);
        }
        BigDecimal batchTotalCount = getSumValue(reqItem.getBatches(),
                ErpPurchaseInStockExecuteCreateReqVO.Batch::getCount, BigDecimal::add, BigDecimal.ZERO);
        if (batchTotalCount.compareTo(executeCount) != 0) {
            throw exception(PURCHASE_IN_STOCK_BATCH_COUNT_MISMATCH);
        }
    }

    public List<ErpPurchaseInStockExecuteItemBatchDO> buildExecuteItemBatches(ErpPurchaseInStockExecuteCreateReqVO reqVO,
                                                                               ErpPurchaseInDO purchaseIn,
                                                                               List<ErpPurchaseInStockExecuteItemDO> executeItems,
                                                                               Map<Long, ErpPurchaseInItemDO> purchaseInItemMap,
                                                                               Map<Long, ErpProductRespVO> productMap) {
        Map<Long, ErpPurchaseInStockExecuteCreateReqVO.Item> reqItemMap = convertMap(
                reqVO.getItems(), ErpPurchaseInStockExecuteCreateReqVO.Item::getPurchaseInItemId);
        Map<Long, ErpPurchaseSourceBatchDO> sourceBatchMap = purchaseSourceBatchService.getPurchaseSourceBatchMap(
                purchaseInItemMap.values().stream()
                        .map(ErpPurchaseInItemDO::getPurchaseSourceBatchId)
                        .filter(java.util.Objects::nonNull)
                        .collect(java.util.stream.Collectors.toSet()));
        return executeItems.stream().flatMap(executeItem -> {
            ErpProductRespVO product = productMap.get(executeItem.getProductId());
            ErpPurchaseInStockExecuteCreateReqVO.Item reqItem = reqItemMap.get(executeItem.getPurchaseInItemId());
            ErpPurchaseInItemDO purchaseInItem = purchaseInItemMap.get(executeItem.getPurchaseInItemId());
            ErpPurchaseSourceBatchDO sourceBatch = purchaseInItem == null || purchaseInItem.getPurchaseSourceBatchId() == null
                    ? null : sourceBatchMap.get(purchaseInItem.getPurchaseSourceBatchId());
            if (product == null || !Boolean.TRUE.equals(product.getBatchControlFlag()) || CollUtil.isEmpty(reqItem.getBatches())) {
                return java.util.stream.Stream.empty();
            }
            return reqItem.getBatches().stream().map(batch -> {
                ErpPurchaseInStockExecuteItemBatchDO executeItemBatch = new ErpPurchaseInStockExecuteItemBatchDO()
                        .setExecuteItemId(executeItem.getId())
                        .setPurchaseInItemId(executeItem.getPurchaseInItemId())
                        .setPurchaseSourceBatchId(sourceBatch != null ? sourceBatch.getId() : null)
                        .setProductId(executeItem.getProductId())
                        .setWarehouseId(executeItem.getWarehouseId())
                        .setBatchNo(batch.getBatchNo().trim())
                        .setPurchaseSourceBatchNo(sourceBatch != null ? sourceBatch.getBatchNo() : null)
                        .setCount(batch.getCount())
                        .setInboundTime(batch.getInboundTime())
                        .setProduceDate(batch.getProduceDate())
                        .setExpireDate(batch.getExpireDate())
                        .setRemark(batch.getRemark());
                ErpStockBatchInboundReqBO inboundReqBO = new ErpStockBatchInboundReqBO(
                        executeItem.getProductId(), executeItem.getWarehouseId(), executeItemBatch.getBatchNo(),
                        executeItemBatch.getInboundTime(), executeItemBatch.getProduceDate(), executeItemBatch.getExpireDate(),
                        executeItemBatch.getCount(), Boolean.FALSE, ErpStockRecordBizTypeEnum.PURCHASE_IN.getType(),
                        purchaseIn.getId(), executeItem.getPurchaseInItemId(), purchaseIn.getNo(), "PURCHASE_IN",
                        executeItemBatch.getPurchaseSourceBatchId(), executeItemBatch.getPurchaseSourceBatchNo(),
                        executeItemBatch.getRemark());
                executeItemBatch.setStockBatchId(stockBatchService.createOrIncreaseBatch(inboundReqBO).getId());
                return executeItemBatch;
            });
        }).toList();
    }

    // endregion

    // region 库存记录

    public void createExecuteStockRecords(ErpPurchaseInDO purchaseIn, List<ErpPurchaseInStockExecuteItemDO> executeItems) {
        List<ErpPurchaseInItemDO> purchaseInItems = erpPurchaseInItemMapper.selectListByInId(purchaseIn.getId());
        Map<Long, ErpPurchaseInItemDO> purchaseInItemMap = convertMap(purchaseInItems, ErpPurchaseInItemDO::getId);
        executeItems.forEach(item -> {
            ErpPurchaseInItemDO purchaseInItem = purchaseInItemMap.get(item.getPurchaseInItemId());
            BigDecimal price = purchaseInItem != null ? purchaseInItem.getProductPrice() : null;
            BigDecimal amount = price != null ? price.multiply(item.getCount()) : null;
            stockRecordService.createStockRecord(new ErpStockRecordCreateReqBO(
                    item.getProductId(), item.getWarehouseId(), item.getCount(),
                    ErpStockRecordBizTypeEnum.PURCHASE_IN.getType(),
                    purchaseIn.getId(), item.getPurchaseInItemId(), purchaseIn.getNo(),
                    price, amount));
            // 可用库存同事务维护：入库数量经 IQC 门禁（未过检不允许入库确认），故过检入库即可用
            increaseAvailableCount(item.getProductId(), item.getWarehouseId(), item.getCount());
        });
    }

    private void increaseAvailableCount(Long productId, Long warehouseId, BigDecimal count) {
        ErpStockDO stock = erpStockMapper.selectByProductIdAndWarehouseId(productId, warehouseId);
        if (stock == null) {
            return;
        }
        erpStockMapper.updateAvailableCountIncrement(stock.getId(), count, false);
    }

    public void reverseExecutedStockRecords(ErpPurchaseInDO purchaseIn) {
        List<ErpPurchaseInStockExecuteDO> executeList = erpPurchaseInStockExecuteMapper.selectListByPurchaseInId(purchaseIn.getId());
        Map<Long, ErpPurchaseInStockExecuteDO> executeMap = convertMap(executeList, ErpPurchaseInStockExecuteDO::getId);
        List<ErpPurchaseInStockExecuteItemDO> executeItems = erpPurchaseInStockExecuteItemMapper.selectListByPurchaseInId(purchaseIn.getId());
        List<ErpPurchaseInItemDO> purchaseInItems = erpPurchaseInItemMapper.selectListByInId(purchaseIn.getId());
        Map<Long, ErpPurchaseInItemDO> purchaseInItemMap = convertMap(purchaseInItems, ErpPurchaseInItemDO::getId);
        Map<Long, List<ErpPurchaseInStockExecuteItemBatchDO>> batchMap = convertMultiMap(
                erpPurchaseInStockExecuteItemBatchMapper.selectListByExecuteItemIds(
                        convertSet(executeItems, ErpPurchaseInStockExecuteItemDO::getId)),
                ErpPurchaseInStockExecuteItemBatchDO::getExecuteItemId);
        executeItems.stream()
                .filter(item -> {
                    ErpPurchaseInStockExecuteDO execute = executeMap.get(item.getExecuteId());
                    return execute != null && ErpPurchaseInStockExecuteStatusEnum.EXECUTED.getStatus().equals(execute.getStatus());
                })
                .forEach(item -> {
                    ErpPurchaseInItemDO purchaseInItem = purchaseInItemMap.get(item.getPurchaseInItemId());
                    BigDecimal price = purchaseInItem != null ? purchaseInItem.getProductPrice() : null;
                    BigDecimal amount = price != null ? price.multiply(item.getCount()) : null;
                    stockRecordService.createStockRecord(new ErpStockRecordCreateReqBO(
                            item.getProductId(), item.getWarehouseId(), item.getCount().negate(),
                            ErpStockRecordBizTypeEnum.PURCHASE_IN_CANCEL.getType(),
                            purchaseIn.getId(), item.getPurchaseInItemId(), purchaseIn.getNo(),
                            price, amount));
                    List<ErpPurchaseInStockExecuteItemBatchDO> executeItemBatches = batchMap.get(item.getId());
                    if (CollUtil.isNotEmpty(executeItemBatches)) {
                        executeItemBatches.forEach(batch -> stockBatchService.decreaseBatch(new ErpStockBatchChangeReqBO(
                                batch.getStockBatchId(), batch.getCount(), ErpStockRecordBizTypeEnum.PURCHASE_IN_CANCEL.getType(),
                                purchaseIn.getId(), item.getPurchaseInItemId(), purchaseIn.getNo(), "采购入库作废回退批次库存")));
                    }
                });
    }

    public void recalculatePurchaseInStockSummary(Long purchaseInId, Long stockInUserId) {
        List<ErpPurchaseInItemDO> purchaseInItems = erpPurchaseInItemMapper.selectListByInId(purchaseInId);
        List<ErpPurchaseInStockExecuteDO> executeList = erpPurchaseInStockExecuteMapper.selectListByPurchaseInId(purchaseInId);
        Map<Long, ErpPurchaseInStockExecuteDO> executeMap = convertMap(executeList, ErpPurchaseInStockExecuteDO::getId);
        Map<Long, BigDecimal> itemStockInCountMap = new LinkedHashMap<>();
        erpPurchaseInStockExecuteItemMapper.selectListByPurchaseInId(purchaseInId).forEach(item -> {
            ErpPurchaseInStockExecuteDO execute = executeMap.get(item.getExecuteId());
            if (execute == null || !ErpPurchaseInStockExecuteStatusEnum.EXECUTED.getStatus().equals(execute.getStatus())) {
                return;
            }
            itemStockInCountMap.merge(item.getPurchaseInItemId(), ObjectUtil.defaultIfNull(item.getCount(), BigDecimal.ZERO), BigDecimal::add);
        });
        List<ErpPurchaseInItemDO> updateItems = convertList(purchaseInItems, item -> new ErpPurchaseInItemDO()
                .setId(item.getId())
                .setStockInCount(ObjectUtil.defaultIfNull(itemStockInCountMap.get(item.getId()), BigDecimal.ZERO)));
        if (CollUtil.isNotEmpty(updateItems)) {
            erpPurchaseInItemMapper.updateBatch(updateItems);
        }
        BigDecimal stockInCount = getSumValue(updateItems, ErpPurchaseInItemDO::getStockInCount, BigDecimal::add, BigDecimal.ZERO);
        ErpPurchaseInDO purchaseIn = erpPurchaseInMapper.selectById(purchaseInId);
        if (purchaseIn == null) {
            return;
        }
        ErpPurchaseInStockExecuteDO latestExecute = erpPurchaseInStockExecuteMapper
                .selectLatestExecutedByPurchaseInId(purchaseInId, ErpPurchaseInStockExecuteStatusEnum.EXECUTED.getStatus());
        erpPurchaseInMapper.updateById(new ErpPurchaseInDO()
                .setId(purchaseInId)
                .setStockInCount(stockInCount)
                .setStockInStatus(ErpPurchaseInStockInStatusResolver.resolve(purchaseIn.getQaPassCount(), stockInCount))
                .setStockInTime(latestExecute != null ? latestExecute.getCreateTime() : null)
                .setStockInUserId(stockInCount.compareTo(BigDecimal.ZERO) > 0 ? stockInUserId : null));
    }

    public void clearPurchaseInItemStockInCount(List<ErpPurchaseInItemDO> purchaseInItems) {
        if (CollUtil.isEmpty(purchaseInItems)) {
            return;
        }
        erpPurchaseInItemMapper.updateBatch(convertList(purchaseInItems, item -> new ErpPurchaseInItemDO()
                .setId(item.getId())
                .setStockInCount(BigDecimal.ZERO)));
    }

    public void resetPurchaseInItemQualityCheck(Long inId, List<ErpPurchaseInItemDO> purchaseInItems) {
        if (CollUtil.isEmpty(purchaseInItems)) {
            return;
        }
        List<ErpPurchaseInItemDO> updateItems = convertList(purchaseInItems, item -> new ErpPurchaseInItemDO()
                .setId(item.getId()).setInId(inId)
                .setQaPassCount(null).setQaRejectCount(null).setQaRemark(null));
        erpPurchaseInItemMapper.updateBatch(updateItems);
    }

    // endregion
}