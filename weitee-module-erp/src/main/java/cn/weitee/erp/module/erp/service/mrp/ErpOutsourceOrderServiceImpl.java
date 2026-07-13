package cn.weitee.erp.module.erp.service.mrp;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.number.MoneyUtils;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.outsource.*;
import cn.weitee.erp.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.*;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseInItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockBatchDO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockDO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpWarehouseDO;
import cn.weitee.erp.module.erp.dal.mysql.mrp.*;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseInItemMapper;
import cn.weitee.erp.module.erp.dal.redis.no.ErpNoRedisDAO;
import cn.weitee.erp.module.erp.enums.mrp.ErpOutsourceIssueTypeEnum;
import cn.weitee.erp.module.erp.enums.mrp.ErpOutsourceOrderStatusEnum;
import cn.weitee.erp.module.erp.enums.mrp.ErpOutsourceOrderTypeEnum;
import cn.weitee.erp.module.erp.enums.stock.ErpStockRecordBizTypeEnum;
import cn.weitee.erp.module.erp.service.finance.ErpApStatementService;
import cn.weitee.erp.module.erp.service.finance.ErpFinanceBizHookService;
import cn.weitee.erp.module.erp.service.product.ErpProductService;
import cn.weitee.erp.module.erp.service.purchase.ErpSupplierService;
import cn.weitee.erp.module.erp.service.stock.ErpStockBatchService;
import cn.weitee.erp.module.erp.service.stock.ErpStockRecordService;
import cn.weitee.erp.module.erp.service.stock.ErpStockService;
import cn.weitee.erp.module.erp.service.stock.ErpWarehouseService;
import cn.weitee.erp.module.erp.service.stock.bo.ErpStockBatchChangeReqBO;
import cn.weitee.erp.module.erp.service.stock.bo.ErpStockBatchInboundReqBO;
import cn.weitee.erp.module.erp.service.stock.bo.ErpStockRecordCreateReqBO;
import cn.weitee.erp.framework.security.core.util.SecurityFrameworkUtils;
import cn.weitee.erp.module.erp.enums.common.ErpBizTypeEnum;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpApStatementDO;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertList;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.*;

@Service
@Slf4j
@Validated
public class ErpOutsourceOrderServiceImpl implements ErpOutsourceOrderService {

    private static final Integer DONE_STATUS = 20;
    private static final String PURCHASE_IN_SOURCE_BIZ_TYPE = "PURCHASE_IN";
    private static final String OUTSOURCE_INBOUND_SOURCE_BIZ_TYPE = "OUTSOURCE_INBOUND";

    @Resource
    private ErpOutsourceOrderMapper erpOutsourceOrderMapper;
    @Resource
    private ErpOutsourceIssueMapper erpOutsourceIssueMapper;
    @Resource
    private ErpOutsourceIssueItemMapper erpOutsourceIssueItemMapper;
    @Resource
    private ErpOutsourceIssueBatchMapper erpOutsourceIssueBatchMapper;
    @Resource
    private ErpOutsourceReturnMapper erpOutsourceReturnMapper;
    @Resource
    private ErpOutsourceReturnItemMapper erpOutsourceReturnItemMapper;
    @Resource
    private ErpOutsourceReturnBatchMapper erpOutsourceReturnBatchMapper;
    @Resource
    private ErpOutsourceInboundMapper erpOutsourceInboundMapper;
    @Resource
    private ErpOutsourceFeeMapper erpOutsourceFeeMapper;
    @Resource
    private ErpOutsourceLossDetailMapper erpOutsourceLossDetailMapper;
    @Resource
    private ErpPurchaseInItemMapper erpPurchaseInItemMapper;
    @Resource
    private ErpBomService bomService;
    @Resource
    private ErpSupplierService supplierService;
    @Resource
    private ErpProductService productService;
    @Resource
    private ErpWarehouseService warehouseService;
    @Resource
    private ErpStockBatchService stockBatchService;
    @Resource
    private ErpStockRecordService stockRecordService;
    @Resource
    private ErpStockService stockService;
    @Resource
    private ErpNoRedisDAO noRedisDAO;
    @Resource
    private ErpApStatementService apStatementService;
    @Resource
    @Lazy
    private ErpFinanceBizHookService financeBizHookService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOutsourceOrder(ErpOutsourceOrderSaveReqVO createReqVO) {
        validateOrderBase(createReqVO.getOrderType(), createReqVO.getSupplierId(), createReqVO.getProductId(),
                createReqVO.getPlannedQty(), createReqVO.getBomId());
        ErpOutsourceOrderDO order = BeanUtils.toBean(createReqVO, ErpOutsourceOrderDO.class, item -> item
                .setNo(noRedisDAO.generate(ErpNoRedisDAO.OUTSOURCE_ORDER_NO_PREFIX))
                .setIssuedQty(BigDecimal.ZERO)
                .setReturnedQty(BigDecimal.ZERO)
                .setFinishedQty(BigDecimal.ZERO)
                .setLossQty(BigDecimal.ZERO)
                .setStatus(ErpOutsourceOrderStatusEnum.CREATED.getStatus()));
        erpOutsourceOrderMapper.insert(order);
        return order.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOutsourceOrder(ErpOutsourceOrderSaveReqVO updateReqVO) {
        ErpOutsourceOrderDO order = validateOutsourceOrderExists(updateReqVO.getId());
        if (isOrderFinalStatus(order.getStatus())) {
            throw exception(OUTSOURCE_ORDER_STATUS_INVALID);
        }
        validateOrderBase(updateReqVO.getOrderType(), updateReqVO.getSupplierId(), updateReqVO.getProductId(),
                updateReqVO.getPlannedQty(), updateReqVO.getBomId());
        erpOutsourceOrderMapper.updateById(BeanUtils.toBean(updateReqVO, ErpOutsourceOrderDO.class));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closeOutsourceOrder(ErpOutsourceOrderCloseReqVO reqVO) {
        ErpOutsourceOrderDO order = validateOutsourceOrderExists(reqVO.getOrderId());
        if (ErpOutsourceOrderStatusEnum.CLOSED.getStatus().equals(order.getStatus())) {
            throw exception(OUTSOURCE_ORDER_STATUS_INVALID);
        }
        if (CollUtil.isNotEmpty(reqVO.getLossItems())) {
            validateOrderMaterialTrackable(order);
        }
        BigDecimal finishedQty = ObjectUtil.defaultIfNull(order.getFinishedQty(), BigDecimal.ZERO);
        BigDecimal plannedQty = ObjectUtil.defaultIfNull(order.getPlannedQty(), BigDecimal.ZERO);
        BigDecimal lossQty = ObjectUtil.defaultIfNull(reqVO.getLossQty(), BigDecimal.ZERO);
        if (lossQty.compareTo(BigDecimal.ZERO) < 0) {
            throw exception(OUTSOURCE_LOSS_QTY_INVALID);
        }
        BigDecimal unresolvedQty = plannedQty.subtract(finishedQty).subtract(lossQty);
        if (unresolvedQty.compareTo(BigDecimal.ZERO) > 0) {
            throw exception(OUTSOURCE_LOSS_QTY_INVALID);
        }
        List<LossWriteItem> lossWriteItems = convertList(reqVO.getLossItems(),
                item -> new LossWriteItem(item.getIssueBatchId(), item.getLossQty(), item.getRemark()));
        List<ErpOutsourceLossDetailDO> lossDetails = buildLossDetailForWrite(order.getId(), lossWriteItems, lossQty, true);
        erpOutsourceOrderMapper.updateById(new ErpOutsourceOrderDO()
                .setId(order.getId())
                .setLossQty(lossQty)
                .setStatus(ErpOutsourceOrderStatusEnum.CLOSED.getStatus())
                .setCloseRemark(reqVO.getCloseRemark())
                .setCloseTime(LocalDateTime.now()));
        if (CollUtil.isNotEmpty(lossDetails)) {
            lossDetails.forEach(erpOutsourceLossDetailMapper::insert);
        }
    }

    @Override
    public ErpOutsourceOrderDO getOutsourceOrder(Long id) {
        return erpOutsourceOrderMapper.selectById(id);
    }

    @Override
    public PageResult<ErpOutsourceOrderDO> getOutsourceOrderPage(ErpOutsourceOrderPageReqVO pageReqVO) {
        return erpOutsourceOrderMapper.selectPage(pageReqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOutsourceIssue(ErpOutsourceIssueCreateReqVO reqVO) {
        ErpOutsourceOrderDO order = validateOutsourceOrderOperable(reqVO.getOrderId());
        validateOrderMaterialTrackable(order);
        Integer issueType = validateIssueType(reqVO.getIssueType());
        productService.validProductList(reqVO.getItems().stream().map(ErpOutsourceIssueCreateReqVO.Item::getMaterialId).toList());
        warehouseService.validWarehouseList(reqVO.getItems().stream().map(ErpOutsourceIssueCreateReqVO.Item::getWarehouseId).distinct().toList());
        Set<Long> stockBatchIds = reqVO.getItems().stream().flatMap(item -> item.getBatches().stream())
                .map(ErpOutsourceIssueCreateReqVO.Batch::getStockBatchId).collect(Collectors.toSet());
        Map<Long, ErpStockBatchDO> stockBatchMap = stockBatchService.getStockBatchMap(stockBatchIds);
        Set<Long> purchaseInItemIds = stockBatchMap.values().stream()
                .filter(item -> PURCHASE_IN_SOURCE_BIZ_TYPE.equals(item.getSourceBizType()))
                .map(ErpStockBatchDO::getSourceBizItemId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, ErpPurchaseInItemDO> purchaseInItemMap = convertMap(erpPurchaseInItemMapper.selectListByIds(purchaseInItemIds), ErpPurchaseInItemDO::getId);
        Map<String, ErpStockDO> stockMap = stockService.getStockMapByProductAndWarehouseIds(
                convertSet(reqVO.getItems(), ErpOutsourceIssueCreateReqVO.Item::getMaterialId),
                convertSet(reqVO.getItems(), ErpOutsourceIssueCreateReqVO.Item::getWarehouseId));

        ErpOutsourceIssueDO issue = new ErpOutsourceIssueDO()
                .setIssueNo(noRedisDAO.generate(ErpNoRedisDAO.OUTSOURCE_ISSUE_NO_PREFIX))
                .setOrderId(order.getId())
                .setIssueType(issueType)
                .setIssueTime(LocalDateTime.now())
                .setStatus(DONE_STATUS)
                .setIssueQty(BigDecimal.ZERO)
                .setIssueAmount(BigDecimal.ZERO)
                .setRemark(reqVO.getRemark());
        erpOutsourceIssueMapper.insert(issue);

        BigDecimal totalQty = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (ErpOutsourceIssueCreateReqVO.Item item : reqVO.getItems()) {
            BigDecimal batchTotalQty = item.getBatches().stream().map(ErpOutsourceIssueCreateReqVO.Batch::getIssueQty)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (batchTotalQty.compareTo(item.getIssueQty()) != 0) {
                throw exception(OUTSOURCE_BATCH_COUNT_MISMATCH);
            }
            BigDecimal itemAmount = BigDecimal.ZERO;
            ErpOutsourceIssueItemDO issueItem = new ErpOutsourceIssueItemDO()
                    .setIssueId(issue.getId())
                    .setMaterialId(item.getMaterialId())
                    .setWarehouseId(item.getWarehouseId())
                    .setIssueQty(item.getIssueQty())
                    .setIssueAmount(BigDecimal.ZERO)
                    .setRemark(item.getRemark());
            erpOutsourceIssueItemMapper.insert(issueItem);

            for (ErpOutsourceIssueCreateReqVO.Batch batch : item.getBatches()) {
                ErpStockBatchDO stockBatch = stockBatchMap.get(batch.getStockBatchId());
                validateIssueBatch(item, batch, stockBatch);
                BigDecimal batchAmount = calculateIssueAmount(stockBatch, batch.getIssueQty(), purchaseInItemMap);
                stockBatchService.decreaseBatch(new ErpStockBatchChangeReqBO(stockBatch.getId(), batch.getIssueQty(),
                        ErpStockRecordBizTypeEnum.OUTSOURCE_ISSUE.getType(), issue.getId(), issueItem.getId(), issue.getIssueNo(), item.getRemark()));
                // 获取加权平均成本作为发料价格
                ErpStockDO stock = stockMap.get(ErpStockService.buildProductWarehouseKey(
                        item.getMaterialId(), item.getWarehouseId()));
                BigDecimal price = stock != null ? stock.getAverageCost() : null;
                BigDecimal amount = price != null ? price.multiply(batch.getIssueQty()) : null;
                stockRecordService.createStockRecord(new ErpStockRecordCreateReqBO(item.getMaterialId(), item.getWarehouseId(),
                        batch.getIssueQty().negate(), ErpStockRecordBizTypeEnum.OUTSOURCE_ISSUE.getType(), issue.getId(),
                        issueItem.getId(), issue.getIssueNo(), price, amount));
                erpOutsourceIssueBatchMapper.insert(new ErpOutsourceIssueBatchDO()
                        .setIssueItemId(issueItem.getId())
                        .setStockBatchId(stockBatch.getId())
                        .setBatchNo(stockBatch.getBatchNo())
                        .setIssueQty(batch.getIssueQty())
                        .setIssueAmount(batchAmount)
                        .setInboundTime(stockBatch.getInboundTime())
                        .setProduceDate(stockBatch.getProduceDate())
                        .setExpireDate(stockBatch.getExpireDate()));
                itemAmount = itemAmount.add(batchAmount);
            }
            issueItem.setIssueAmount(itemAmount);
            erpOutsourceIssueItemMapper.updateById(new ErpOutsourceIssueItemDO().setId(issueItem.getId()).setIssueAmount(itemAmount));
            totalQty = totalQty.add(item.getIssueQty());
            totalAmount = totalAmount.add(itemAmount);
        }
        erpOutsourceIssueMapper.updateById(new ErpOutsourceIssueDO().setId(issue.getId()).setIssueQty(totalQty).setIssueAmount(totalAmount));
        erpOutsourceOrderMapper.updateById(new ErpOutsourceOrderDO().setId(order.getId())
                .setIssuedQty(ObjectUtil.defaultIfNull(order.getIssuedQty(), BigDecimal.ZERO).add(totalQty))
                .setStatus(ErpOutsourceOrderStatusEnum.PROCESSING.getStatus()));
        return issue.getId();
    }

    @Override
    public ErpOutsourceIssueDO getOutsourceIssue(Long id) {
        return erpOutsourceIssueMapper.selectById(id);
    }

    @Override
    public PageResult<ErpOutsourceIssueDO> getOutsourceIssuePage(ErpOutsourceIssuePageReqVO pageReqVO) {
        return erpOutsourceIssueMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ErpOutsourceIssueItemDO> getOutsourceIssueItemListByIssueId(Long issueId) {
        return erpOutsourceIssueItemMapper.selectListByIssueId(issueId);
    }

    @Override
    public List<ErpOutsourceIssueItemDO> getOutsourceIssueItemListByIssueIds(Collection<Long> issueIds) {
        return erpOutsourceIssueItemMapper.selectListByIssueIds(issueIds);
    }

    @Override
    public List<ErpOutsourceIssueBatchDO> getOutsourceIssueBatchListByIssueItemIds(Collection<Long> issueItemIds) {
        return erpOutsourceIssueBatchMapper.selectListByIssueItemIds(issueItemIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOutsourceReturn(ErpOutsourceReturnCreateReqVO reqVO) {
        ErpOutsourceOrderDO order = validateOutsourceOrderOperable(reqVO.getOrderId());
        validateOrderMaterialTrackable(order);
        productService.validProductList(reqVO.getItems().stream().map(ErpOutsourceReturnCreateReqVO.Item::getMaterialId).toList());
        warehouseService.validWarehouseList(reqVO.getItems().stream().map(ErpOutsourceReturnCreateReqVO.Item::getWarehouseId).distinct().toList());

        List<ErpOutsourceIssueDO> issues = erpOutsourceIssueMapper.selectListByOrderId(order.getId());
        List<ErpOutsourceIssueItemDO> issueItems = erpOutsourceIssueItemMapper.selectListByIssueIds(convertList(issues, ErpOutsourceIssueDO::getId));
        Map<Long, ErpOutsourceIssueItemDO> issueItemMap = convertMap(issueItems, ErpOutsourceIssueItemDO::getId);
        Map<Long, ErpOutsourceIssueBatchDO> issueBatchMap = convertMap(erpOutsourceIssueBatchMapper.selectListByIssueItemIds(issueItemMap.keySet()),
                ErpOutsourceIssueBatchDO::getId);
        Set<Long> issueBatchIds = reqVO.getItems().stream().flatMap(item -> item.getBatches().stream())
                .map(ErpOutsourceReturnCreateReqVO.Batch::getIssueBatchId).collect(Collectors.toSet());
        Map<Long, BigDecimal> existingReturnedQtyMap = new HashMap<>();
        erpOutsourceReturnBatchMapper.selectListByIssueBatchIds(issueBatchIds)
                .forEach(item -> existingReturnedQtyMap.merge(item.getIssueBatchId(),
                        ObjectUtil.defaultIfNull(item.getReturnQty(), BigDecimal.ZERO), BigDecimal::add));
        Map<String, ErpStockDO> stockMap = stockService.getStockMapByProductAndWarehouseIds(
                convertSet(reqVO.getItems(), ErpOutsourceReturnCreateReqVO.Item::getMaterialId),
                convertSet(reqVO.getItems(), ErpOutsourceReturnCreateReqVO.Item::getWarehouseId));

        ErpOutsourceReturnDO returning = new ErpOutsourceReturnDO()
                .setReturnNo(noRedisDAO.generate(ErpNoRedisDAO.OUTSOURCE_RETURN_NO_PREFIX))
                .setOrderId(order.getId())
                .setReturnTime(LocalDateTime.now())
                .setStatus(DONE_STATUS)
                .setReturnQty(BigDecimal.ZERO)
                .setReturnAmount(BigDecimal.ZERO)
                .setRemark(reqVO.getRemark());
        erpOutsourceReturnMapper.insert(returning);

        BigDecimal totalQty = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;
        Map<Long, BigDecimal> requestReturnedQtyMap = new HashMap<>();
        for (ErpOutsourceReturnCreateReqVO.Item item : reqVO.getItems()) {
            BigDecimal batchTotalQty = item.getBatches().stream().map(ErpOutsourceReturnCreateReqVO.Batch::getReturnQty)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (batchTotalQty.compareTo(item.getReturnQty()) != 0) {
                throw exception(OUTSOURCE_BATCH_COUNT_MISMATCH);
            }
            BigDecimal itemAmount = BigDecimal.ZERO;
            ErpOutsourceReturnItemDO returnItem = new ErpOutsourceReturnItemDO()
                    .setReturnId(returning.getId())
                    .setMaterialId(item.getMaterialId())
                    .setWarehouseId(item.getWarehouseId())
                    .setReturnQty(item.getReturnQty())
                    .setReturnAmount(BigDecimal.ZERO)
                    .setRemark(item.getRemark());
            erpOutsourceReturnItemMapper.insert(returnItem);
            for (ErpOutsourceReturnCreateReqVO.Batch batch : item.getBatches()) {
                ErpOutsourceIssueBatchDO issueBatch = issueBatchMap.get(batch.getIssueBatchId());
                ErpOutsourceIssueItemDO issueItem = issueBatch == null ? null : issueItemMap.get(issueBatch.getIssueItemId());
                if (issueBatch == null || issueItem == null
                        || !Objects.equals(issueItem.getMaterialId(), item.getMaterialId())
                        || !Objects.equals(issueItem.getWarehouseId(), item.getWarehouseId())
                        || !Objects.equals(issueBatch.getStockBatchId(), batch.getStockBatchId())
                        || !Objects.equals(issueBatch.getBatchNo(), batch.getBatchNo())) {
                    throw exception(OUTSOURCE_BATCH_INVALID);
                }
                BigDecimal returnedQty = ObjectUtil.defaultIfNull(existingReturnedQtyMap.get(batch.getIssueBatchId()), BigDecimal.ZERO)
                        .add(ObjectUtil.defaultIfNull(requestReturnedQtyMap.get(batch.getIssueBatchId()), BigDecimal.ZERO));
                BigDecimal returnableQty = issueBatch.getIssueQty().subtract(returnedQty);
                if (batch.getReturnQty().compareTo(returnableQty) > 0) {
                    throw exception(OUTSOURCE_BATCH_INVALID);
                }
                BigDecimal batchAmount = calculateReturnAmount(issueBatch, batch.getReturnQty());
                requestReturnedQtyMap.merge(batch.getIssueBatchId(), batch.getReturnQty(), BigDecimal::add);
                stockBatchService.increaseBatch(new ErpStockBatchChangeReqBO(batch.getStockBatchId(), batch.getReturnQty(),
                        ErpStockRecordBizTypeEnum.OUTSOURCE_RETURN.getType(), returning.getId(), returnItem.getId(),
                        returning.getReturnNo(), item.getRemark()));
                // 获取加权平均成本作为退料价格
                ErpStockDO stock = stockMap.get(ErpStockService.buildProductWarehouseKey(
                        item.getMaterialId(), item.getWarehouseId()));
                BigDecimal price = stock != null ? stock.getAverageCost() : null;
                BigDecimal amount = price != null ? price.multiply(batch.getReturnQty()) : null;
                stockRecordService.createStockRecord(new ErpStockRecordCreateReqBO(item.getMaterialId(), item.getWarehouseId(),
                        batch.getReturnQty(), ErpStockRecordBizTypeEnum.OUTSOURCE_RETURN.getType(), returning.getId(),
                        returnItem.getId(), returning.getReturnNo(), price, amount));
                erpOutsourceReturnBatchMapper.insert(new ErpOutsourceReturnBatchDO()
                        .setReturnItemId(returnItem.getId())
                        .setIssueBatchId(issueBatch.getId())
                        .setStockBatchId(batch.getStockBatchId())
                        .setBatchNo(batch.getBatchNo())
                        .setReturnQty(batch.getReturnQty())
                        .setReturnAmount(batchAmount));
                itemAmount = itemAmount.add(batchAmount);
            }
            erpOutsourceReturnItemMapper.updateById(new ErpOutsourceReturnItemDO().setId(returnItem.getId()).setReturnAmount(itemAmount));
            totalQty = totalQty.add(item.getReturnQty());
            totalAmount = totalAmount.add(itemAmount);
        }
        erpOutsourceReturnMapper.updateById(new ErpOutsourceReturnDO().setId(returning.getId()).setReturnQty(totalQty).setReturnAmount(totalAmount));
        erpOutsourceOrderMapper.updateById(new ErpOutsourceOrderDO().setId(order.getId())
                .setReturnedQty(ObjectUtil.defaultIfNull(order.getReturnedQty(), BigDecimal.ZERO).add(totalQty))
                .setStatus(ErpOutsourceOrderStatusEnum.PROCESSING.getStatus()));
        return returning.getId();
    }

    @Override
    public ErpOutsourceReturnDO getOutsourceReturn(Long id) {
        return erpOutsourceReturnMapper.selectById(id);
    }

    @Override
    public PageResult<ErpOutsourceReturnDO> getOutsourceReturnPage(ErpOutsourceReturnPageReqVO pageReqVO) {
        return erpOutsourceReturnMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ErpOutsourceReturnItemDO> getOutsourceReturnItemListByReturnId(Long returnId) {
        return erpOutsourceReturnItemMapper.selectListByReturnId(returnId);
    }

    @Override
    public List<ErpOutsourceReturnItemDO> getOutsourceReturnItemListByReturnIds(Collection<Long> returnIds) {
        return erpOutsourceReturnItemMapper.selectListByReturnIds(returnIds);
    }

    @Override
    public List<ErpOutsourceReturnBatchDO> getOutsourceReturnBatchListByReturnItemIds(Collection<Long> returnItemIds) {
        return erpOutsourceReturnBatchMapper.selectListByReturnItemIds(returnItemIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOutsourceInbound(ErpOutsourceInboundCreateReqVO reqVO) {
        ErpOutsourceOrderDO order = validateOutsourceOrderOperable(reqVO.getOrderId());
        productService.validProductList(List.of(order.getProductId()));
        warehouseService.validWarehouseList(List.of(reqVO.getWarehouseId()));
        BigDecimal materialCost = calculateNetMaterialCost(order.getId());
        BigDecimal processFee = calculateProcessFee(order.getId());
        BigDecimal totalCost = materialCost.add(processFee);
        BigDecimal unitCost = reqVO.getInboundQty().compareTo(BigDecimal.ZERO) > 0
                ? totalCost.divide(reqVO.getInboundQty(), 6, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(6, RoundingMode.HALF_UP);
        ErpOutsourceInboundDO inbound = new ErpOutsourceInboundDO()
                .setInboundNo(noRedisDAO.generate(ErpNoRedisDAO.OUTSOURCE_INBOUND_NO_PREFIX))
                .setOrderId(order.getId())
                .setWarehouseId(reqVO.getWarehouseId())
                .setBatchNo(reqVO.getBatchNo())
                .setInboundTime(LocalDateTime.now())
                .setProduceDate(reqVO.getProduceDate())
                .setExpireDate(reqVO.getExpireDate())
                .setStatus(DONE_STATUS)
                .setInboundQty(reqVO.getInboundQty())
                .setMaterialCost(materialCost)
                .setProcessFee(processFee)
                .setTotalCost(totalCost)
                .setUnitCost(unitCost)
                .setRemark(reqVO.getRemark());
        erpOutsourceInboundMapper.insert(inbound);
        stockBatchService.createOrIncreaseBatch(new ErpStockBatchInboundReqBO(order.getProductId(), reqVO.getWarehouseId(),
                reqVO.getBatchNo(), inbound.getInboundTime(), reqVO.getProduceDate(), reqVO.getExpireDate(),
                reqVO.getInboundQty(), Boolean.FALSE, ErpStockRecordBizTypeEnum.OUTSOURCE_INBOUND.getType(),
                inbound.getId(), inbound.getId(), inbound.getInboundNo(), OUTSOURCE_INBOUND_SOURCE_BIZ_TYPE, null, null, reqVO.getRemark()));
        stockRecordService.createStockRecord(new ErpStockRecordCreateReqBO(order.getProductId(), reqVO.getWarehouseId(),
                reqVO.getInboundQty(), ErpStockRecordBizTypeEnum.OUTSOURCE_INBOUND.getType(), inbound.getId(), inbound.getId(), inbound.getInboundNo(),
                unitCost, totalCost));
        BigDecimal newFinishedQty = ObjectUtil.defaultIfNull(order.getFinishedQty(), BigDecimal.ZERO).add(reqVO.getInboundQty());
        erpOutsourceOrderMapper.updateById(new ErpOutsourceOrderDO().setId(order.getId())
                .setFinishedQty(newFinishedQty)
                .setStatus(newFinishedQty.compareTo(ObjectUtil.defaultIfNull(order.getPlannedQty(), BigDecimal.ZERO)) >= 0
                        ? ErpOutsourceOrderStatusEnum.COMPLETED.getStatus()
                        : ErpOutsourceOrderStatusEnum.PROCESSING.getStatus()));
        return inbound.getId();
    }

    @Override
    public ErpOutsourceInboundDO getOutsourceInbound(Long id) {
        return erpOutsourceInboundMapper.selectById(id);
    }

    @Override
    public PageResult<ErpOutsourceInboundDO> getOutsourceInboundPage(ErpOutsourceInboundPageReqVO pageReqVO) {
        return erpOutsourceInboundMapper.selectPage(pageReqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOutsourceFee(ErpOutsourceFeeSaveReqVO reqVO) {
        ErpOutsourceOrderDO order = validateOutsourceOrderFeeable(reqVO.getOrderId());
        if (reqVO.getFeeAmount() == null || reqVO.getFeeAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw exception(OUTSOURCE_FEE_AMOUNT_INVALID);
        }
        ErpOutsourceFeeDO fee = new ErpOutsourceFeeDO()
                .setFeeNo(noRedisDAO.generate(ErpNoRedisDAO.OUTSOURCE_FEE_NO_PREFIX))
                .setOrderId(reqVO.getOrderId())
                .setFeeTime(ObjectUtil.defaultIfNull(reqVO.getFeeTime(), LocalDateTime.now()))
                .setStatus(DONE_STATUS)
                .setFeeAmount(reqVO.getFeeAmount())
                .setRemark(reqVO.getRemark());
        erpOutsourceFeeMapper.insert(fee);
        apStatementService.createStatementForOutsourceFee(fee, order);
        financeBizHookService.handleApprovedBiz(cn.weitee.erp.module.erp.enums.common.ErpBizTypeEnum.OUTSOURCE_FEE.getType(),
                fee.getId(), fee.getFeeTime().toLocalDate());
        erpOutsourceOrderMapper.updateById(new ErpOutsourceOrderDO().setId(reqVO.getOrderId())
                .setStatus(resolveStatusAfterFeeCreate(order.getStatus())));
        return fee.getId();
    }

    @Override
    public ErpOutsourceFeeDO getOutsourceFee(Long id) {
        return erpOutsourceFeeMapper.selectById(id);
    }

    @Override
    public PageResult<ErpOutsourceFeeDO> getOutsourceFeePage(ErpOutsourceFeePageReqVO pageReqVO) {
        return erpOutsourceFeeMapper.selectPage(pageReqVO);
    }

    private static final Integer FEE_VOID_STATUS = 30;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void voidOutsourceFee(Long id, String reason) {
        ErpOutsourceFeeDO fee = erpOutsourceFeeMapper.selectById(id);
        if (fee == null) {
            throw exception(OUTSOURCE_FEE_NOT_EXISTS);
        }
        if (ObjectUtil.equal(fee.getStatus(), FEE_VOID_STATUS)) {
            log.warn("[voidOutsourceFee] 委外加工费已作废，幂等跳过，id={}", id);
            return;
        }
        ErpApStatementDO statement = apStatementService.getApStatementByBizTypeAndBizId(
                ErpBizTypeEnum.OUTSOURCE_FEE.getType(), id);
        if (statement != null && statement.getPaidAmount() != null
                && statement.getPaidAmount().compareTo(BigDecimal.ZERO) > 0) {
            throw exception(OUTSOURCE_FEE_VOID_FAIL_ALLOCATED, fee.getFeeNo());
        }
        erpOutsourceFeeMapper.updateById(new ErpOutsourceFeeDO()
                .setId(id)
                .setStatus(FEE_VOID_STATUS));
        if (statement != null) {
            apStatementService.closeStatementByBiz(ErpBizTypeEnum.OUTSOURCE_FEE.getType(), id,
                    reason != null ? reason : "委外加工费作废关闭台账");
        }
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        financeBizHookService.handleRollbackBiz(ErpBizTypeEnum.OUTSOURCE_FEE.getType(), id,
                userId, reason != null ? reason : "委外加工费作废回滚凭证");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createOutsourceLossEntry(ErpOutsourceLossEntryCreateReqVO reqVO) {
        ErpOutsourceOrderDO order = validateOutsourceOrderExists(reqVO.getOrderId());
        validateOrderMaterialTrackable(order);
        if (!ErpOutsourceOrderStatusEnum.CLOSED.getStatus().equals(order.getStatus())) {
            throw exception(OUTSOURCE_ORDER_STATUS_INVALID);
        }
        BigDecimal orderLossQty = ObjectUtil.defaultIfNull(order.getLossQty(), BigDecimal.ZERO);
        if (orderLossQty.compareTo(BigDecimal.ZERO) <= 0) {
            throw exception(OUTSOURCE_LOSS_QTY_INVALID);
        }
        List<LossWriteItem> lossWriteItems = convertList(reqVO.getEntries(),
                item -> new LossWriteItem(item.getIssueBatchId(), item.getLossQty(), item.getRemark()));
        BigDecimal pendingBackfillQty = calculatePendingBackfillQty(orderLossQty,
                sumLossQty(erpOutsourceLossDetailMapper.selectListByOrderId(order.getId())));
        List<ErpOutsourceLossDetailDO> lossDetails = buildLossDetailForWrite(order.getId(), lossWriteItems,
                pendingBackfillQty, false);
        if (CollUtil.isEmpty(lossDetails)) {
            throw exception(OUTSOURCE_LOSS_QTY_INVALID);
        }
        lossDetails.forEach(erpOutsourceLossDetailMapper::insert);
    }

    @Override
    public ErpOutsourceCostDetailRespVO getOutsourceCostDetail(Long orderId) {
        ErpOutsourceOrderDO order = validateOutsourceOrderExists(orderId);
        List<ErpOutsourceIssueDO> issues = erpOutsourceIssueMapper.selectListByOrderId(orderId);
        List<ErpOutsourceReturnDO> returns = erpOutsourceReturnMapper.selectListByOrderId(orderId);
        List<ErpOutsourceFeeDO> fees = erpOutsourceFeeMapper.selectListByOrderId(orderId);
        List<ErpOutsourceInboundDO> inbounds = erpOutsourceInboundMapper.selectListByOrderId(orderId);
        BigDecimal materialCost = sumIssueAmount(issues);
        BigDecimal returnMaterialCost = sumReturnAmount(returns);
        BigDecimal netMaterialCost = materialCost.subtract(returnMaterialCost);
        BigDecimal processFee = sumFeeAmount(fees);
        BigDecimal totalCost = netMaterialCost.add(processFee);
        BigDecimal finishedQty = ObjectUtil.defaultIfNull(order.getFinishedQty(), BigDecimal.ZERO);

        ErpOutsourceCostDetailRespVO respVO = new ErpOutsourceCostDetailRespVO();
        respVO.setOrderId(order.getId());
        respVO.setOrderNo(order.getNo());
        respVO.setPlannedQty(order.getPlannedQty());
        respVO.setFinishedQty(finishedQty);
        respVO.setMaterialCost(materialCost);
        respVO.setReturnMaterialCost(returnMaterialCost);
        respVO.setNetMaterialCost(netMaterialCost);
        respVO.setProcessFee(processFee);
        respVO.setTotalCost(totalCost);
        respVO.setUnitCost(finishedQty.compareTo(BigDecimal.ZERO) > 0
                ? totalCost.divide(finishedQty, 6, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(6, RoundingMode.HALF_UP));
        respVO.setIssueDetails(convertList(issues, this::buildCostIssueDetail));
        respVO.setReturnDetails(convertList(returns, item -> BeanUtils.toBean(item, ErpOutsourceCostDetailRespVO.ReturnDetail.class)));
        respVO.setFeeDetails(convertList(fees, item -> BeanUtils.toBean(item, ErpOutsourceCostDetailRespVO.FeeDetail.class)));
        respVO.setInboundDetails(convertList(inbounds, item -> BeanUtils.toBean(item, ErpOutsourceCostDetailRespVO.InboundDetail.class)));
        return respVO;
    }

    @Override
    public ErpOutsourceLossDetailRespVO getOutsourceLossDetail(Long orderId) {
        ErpOutsourceOrderDO order = validateOutsourceOrderExists(orderId);
        List<ErpOutsourceLossDetailDO> lossDetails = erpOutsourceLossDetailMapper.selectListByOrderId(orderId);
        BigDecimal orderLossQty = ObjectUtil.defaultIfNull(order.getLossQty(), BigDecimal.ZERO);
        BigDecimal recordedLossQty = sumLossQty(lossDetails);
        ErpOutsourceLossDetailRespVO respVO = new ErpOutsourceLossDetailRespVO();
        respVO.setOrderId(order.getId());
        respVO.setOrderNo(order.getNo());
        respVO.setPlannedQty(order.getPlannedQty());
        respVO.setFinishedQty(ObjectUtil.defaultIfNull(order.getFinishedQty(), BigDecimal.ZERO));
        respVO.setOrderLossQty(orderLossQty);
        respVO.setRecordedLossQty(recordedLossQty);
        respVO.setPendingBackfillQty(calculatePendingBackfillQty(orderLossQty, recordedLossQty));
        respVO.setUnresolvedQty(calculateUnresolvedQty(order.getPlannedQty(), order.getFinishedQty(), order.getLossQty()));
        respVO.setHasPendingBackfill(respVO.getPendingBackfillQty().compareTo(BigDecimal.ZERO) > 0);
        respVO.setCloseRemark(order.getCloseRemark());
        respVO.setDetails(buildOutsourceLossDetailItems(orderId, lossDetails));
        respVO.setEntries(buildOutsourceLossEntries(lossDetails));
        return respVO;
    }

    @Override
    public ErpOutsourceReconciliationRespVO getOutsourceReconciliationDetail(Long orderId) {
        ErpOutsourceOrderDO order = validateOutsourceOrderExists(orderId);
        List<ErpOutsourceIssueDO> issues = erpOutsourceIssueMapper.selectListByOrderId(orderId);
        List<ErpOutsourceReturnDO> returns = erpOutsourceReturnMapper.selectListByOrderId(orderId);
        List<ErpOutsourceFeeDO> fees = erpOutsourceFeeMapper.selectListByOrderId(orderId);
        List<ErpOutsourceInboundDO> inbounds = erpOutsourceInboundMapper.selectListByOrderId(orderId);
        BigDecimal finishedQty = ObjectUtil.defaultIfNull(order.getFinishedQty(), BigDecimal.ZERO);
        BigDecimal plannedQty = ObjectUtil.defaultIfNull(order.getPlannedQty(), BigDecimal.ZERO);
        BigDecimal lossQty = ObjectUtil.defaultIfNull(order.getLossQty(), BigDecimal.ZERO);
        BigDecimal normalIssueAmount = sumIssueAmountByType(issues, ErpOutsourceIssueTypeEnum.NORMAL.getType());
        BigDecimal supplementIssueAmount = sumIssueAmountByType(issues, ErpOutsourceIssueTypeEnum.SUPPLEMENT.getType());
        BigDecimal totalIssueAmount = normalIssueAmount.add(supplementIssueAmount);
        BigDecimal returnAmount = sumReturnAmount(returns);
        BigDecimal netMaterialCost = totalIssueAmount.subtract(returnAmount);
        BigDecimal processFee = sumFeeAmount(fees);
        BigDecimal totalCost = netMaterialCost.add(processFee);

        ErpOutsourceReconciliationRespVO respVO = new ErpOutsourceReconciliationRespVO();
        respVO.setOrderId(order.getId());
        respVO.setOrderNo(order.getNo());
        respVO.setPlannedQty(plannedQty);
        respVO.setFinishedQty(finishedQty);
        respVO.setLossQty(lossQty);
        respVO.setPendingInboundQty(plannedQty.compareTo(finishedQty) > 0 ? plannedQty.subtract(finishedQty) : BigDecimal.ZERO);
        respVO.setUnresolvedQty(plannedQty.subtract(finishedQty).subtract(lossQty).compareTo(BigDecimal.ZERO) > 0
                ? plannedQty.subtract(finishedQty).subtract(lossQty) : BigDecimal.ZERO);
        respVO.setOverInboundQty(finishedQty.compareTo(plannedQty) > 0 ? finishedQty.subtract(plannedQty) : BigDecimal.ZERO);
        respVO.setNormalIssueAmount(normalIssueAmount);
        respVO.setSupplementIssueAmount(supplementIssueAmount);
        respVO.setTotalIssueAmount(totalIssueAmount);
        respVO.setReturnAmount(returnAmount);
        respVO.setNetMaterialCost(netMaterialCost);
        respVO.setProcessFee(processFee);
        respVO.setTotalCost(totalCost);
        respVO.setUnitCost(finishedQty.compareTo(BigDecimal.ZERO) > 0
                ? totalCost.divide(finishedQty, 6, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(6, RoundingMode.HALF_UP));
        respVO.setHasSupplementIssue(supplementIssueAmount.compareTo(BigDecimal.ZERO) > 0);
        respVO.setCloseRemark(order.getCloseRemark());
        respVO.setIssueDetails(convertList(issues, this::buildReconciliationIssueDetail));
        respVO.setReturnDetails(convertList(returns, item -> BeanUtils.toBean(item, ErpOutsourceReconciliationRespVO.ReturnDetail.class)));
        respVO.setFeeDetails(convertList(fees, item -> BeanUtils.toBean(item, ErpOutsourceReconciliationRespVO.FeeDetail.class)));
        respVO.setInboundDetails(convertList(inbounds, item -> BeanUtils.toBean(item, ErpOutsourceReconciliationRespVO.InboundDetail.class)));
        return respVO;
    }

    private List<ErpOutsourceLossDetailDO> buildLossDetailForWrite(Long orderId, List<LossWriteItem> lossItems,
                                                                   BigDecimal targetLossQty, boolean requireExactTotal) {
        if (CollUtil.isEmpty(lossItems)) {
            return Collections.emptyList();
        }
        BigDecimal detailLossQty = lossItems.stream()
                .map(item -> ObjectUtil.defaultIfNull(item.lossQty(), BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if ((requireExactTotal && detailLossQty.compareTo(targetLossQty) != 0)
                || (!requireExactTotal && detailLossQty.compareTo(targetLossQty) > 0)) {
            throw exception(OUTSOURCE_LOSS_QTY_INVALID);
        }
        List<ErpOutsourceIssueDO> issues = erpOutsourceIssueMapper.selectListByOrderId(orderId);
        if (CollUtil.isEmpty(issues)) {
            throw exception(OUTSOURCE_LOSS_SOURCE_MISSING);
        }
        List<ErpOutsourceIssueItemDO> issueItems = erpOutsourceIssueItemMapper.selectListByIssueIds(convertList(issues, ErpOutsourceIssueDO::getId));
        if (CollUtil.isEmpty(issueItems)) {
            throw exception(OUTSOURCE_LOSS_SOURCE_MISSING);
        }
        Map<Long, ErpOutsourceIssueItemDO> issueItemMap = convertMap(issueItems, ErpOutsourceIssueItemDO::getId);
        List<ErpOutsourceIssueBatchDO> issueBatches = erpOutsourceIssueBatchMapper.selectListByIssueItemIds(issueItemMap.keySet());
        if (CollUtil.isEmpty(issueBatches)) {
            throw exception(OUTSOURCE_LOSS_SOURCE_MISSING);
        }
        Map<Long, ErpOutsourceIssueBatchDO> issueBatchMap = convertMap(issueBatches, ErpOutsourceIssueBatchDO::getId);
        Set<Long> issueBatchIds = convertSet(issueBatches, ErpOutsourceIssueBatchDO::getId);
        Map<Long, BigDecimal> returnedQtyMap = sumReturnQtyByIssueBatch(issueBatchIds);
        Map<Long, BigDecimal> existingLossQtyMap = sumLossQtyByIssueBatch(issueBatchIds);

        Map<Long, BigDecimal> requestLossQtyMap = new HashMap<>();
        List<ErpOutsourceLossDetailDO> results = new ArrayList<>();
        for (LossWriteItem lossItem : lossItems) {
            Long issueBatchId = lossItem.issueBatchId();
            ErpOutsourceIssueBatchDO issueBatch = issueBatchMap.get(issueBatchId);
            ErpOutsourceIssueItemDO issueItem = issueBatch == null ? null : issueItemMap.get(issueBatch.getIssueItemId());
            if (issueBatch == null || issueItem == null || requestLossQtyMap.containsKey(issueBatchId)) {
                throw exception(OUTSOURCE_BATCH_INVALID);
            }
            BigDecimal returnedQty = ObjectUtil.defaultIfNull(returnedQtyMap.get(issueBatchId), BigDecimal.ZERO);
            BigDecimal existingLossQty = ObjectUtil.defaultIfNull(existingLossQtyMap.get(issueBatchId), BigDecimal.ZERO);
            BigDecimal availableLossQty = ObjectUtil.defaultIfNull(issueBatch.getIssueQty(), BigDecimal.ZERO)
                    .subtract(returnedQty)
                    .subtract(existingLossQty);
            if (availableLossQty.compareTo(BigDecimal.ZERO) < 0
                    || lossItem.lossQty().compareTo(availableLossQty) > 0) {
                throw exception(OUTSOURCE_BATCH_INVALID);
            }
            requestLossQtyMap.put(issueBatchId, lossItem.lossQty());
            results.add(new ErpOutsourceLossDetailDO()
                    .setOrderId(orderId)
                    .setIssueBatchId(issueBatchId)
                    .setMaterialId(issueItem.getMaterialId())
                    .setWarehouseId(issueItem.getWarehouseId())
                    .setStockBatchId(issueBatch.getStockBatchId())
                    .setBatchNo(issueBatch.getBatchNo())
                    .setLossQty(lossItem.lossQty())
                    .setLossAmount(calculateReturnAmount(issueBatch, lossItem.lossQty()))
                    .setRemark(lossItem.remark()));
        }
        return results;
    }

    private List<ErpOutsourceLossDetailItemRespVO> buildOutsourceLossDetailItems(Long orderId,
                                                                                 List<ErpOutsourceLossDetailDO> lossDetails) {
        List<ErpOutsourceIssueDO> issues = erpOutsourceIssueMapper.selectListByOrderId(orderId);
        if (CollUtil.isEmpty(issues)) {
            return Collections.emptyList();
        }
        List<ErpOutsourceIssueItemDO> issueItems = erpOutsourceIssueItemMapper.selectListByIssueIds(convertList(issues, ErpOutsourceIssueDO::getId));
        if (CollUtil.isEmpty(issueItems)) {
            return Collections.emptyList();
        }
        Map<Long, ErpOutsourceIssueItemDO> issueItemMap = convertMap(issueItems, ErpOutsourceIssueItemDO::getId);
        List<ErpOutsourceIssueBatchDO> issueBatches = erpOutsourceIssueBatchMapper.selectListByIssueItemIds(issueItemMap.keySet());
        if (CollUtil.isEmpty(issueBatches)) {
            return Collections.emptyList();
        }
        Set<Long> issueBatchIds = convertSet(issueBatches, ErpOutsourceIssueBatchDO::getId);
        Map<Long, BigDecimal> returnedQtyMap = sumReturnQtyByIssueBatch(issueBatchIds);
        Map<Long, BigDecimal> returnedAmountMap = sumReturnAmountByIssueBatch(issueBatchIds);
        Map<Long, BigDecimal> lossQtyMap = new HashMap<>();
        Map<Long, BigDecimal> lossAmountMap = new HashMap<>();
        Map<Long, ErpOutsourceLossDetailDO> latestLossDetailMap = new HashMap<>();
        lossDetails.forEach(item -> {
            lossQtyMap.merge(item.getIssueBatchId(), ObjectUtil.defaultIfNull(item.getLossQty(), BigDecimal.ZERO), BigDecimal::add);
            lossAmountMap.merge(item.getIssueBatchId(), ObjectUtil.defaultIfNull(item.getLossAmount(), BigDecimal.ZERO), BigDecimal::add);
            latestLossDetailMap.put(item.getIssueBatchId(), item);
        });
        Map<Long, ErpProductRespVO> productMap = productService.getProductVOMap(convertSet(issueItems, ErpOutsourceIssueItemDO::getMaterialId));
        Map<Long, ErpWarehouseDO> warehouseMap = warehouseService.getWarehouseMap(convertSet(issueItems, ErpOutsourceIssueItemDO::getWarehouseId));

        return convertList(issueBatches, issueBatch -> {
            ErpOutsourceIssueItemDO issueItem = issueItemMap.get(issueBatch.getIssueItemId());
            ErpOutsourceLossDetailDO lossDetail = latestLossDetailMap.get(issueBatch.getId());
            BigDecimal issueQty = ObjectUtil.defaultIfNull(issueBatch.getIssueQty(), BigDecimal.ZERO);
            BigDecimal returnedQty = ObjectUtil.defaultIfNull(returnedQtyMap.get(issueBatch.getId()), BigDecimal.ZERO);
            BigDecimal lossQty = ObjectUtil.defaultIfNull(lossQtyMap.get(issueBatch.getId()), BigDecimal.ZERO);
            BigDecimal lossAmount = ObjectUtil.defaultIfNull(lossAmountMap.get(issueBatch.getId()), BigDecimal.ZERO);
            ErpOutsourceLossDetailItemRespVO itemRespVO = new ErpOutsourceLossDetailItemRespVO();
            itemRespVO.setIssueBatchId(issueBatch.getId());
            itemRespVO.setMaterialId(issueItem.getMaterialId());
            itemRespVO.setWarehouseId(issueItem.getWarehouseId());
            itemRespVO.setStockBatchId(issueBatch.getStockBatchId());
            itemRespVO.setBatchNo(issueBatch.getBatchNo());
            itemRespVO.setIssueQty(issueQty);
            itemRespVO.setReturnedQty(returnedQty);
            itemRespVO.setLossQty(lossQty);
            itemRespVO.setAvailableLossQty(issueQty.subtract(returnedQty).subtract(lossQty).compareTo(BigDecimal.ZERO) > 0
                    ? issueQty.subtract(returnedQty).subtract(lossQty) : BigDecimal.ZERO);
            itemRespVO.setIssueAmount(ObjectUtil.defaultIfNull(issueBatch.getIssueAmount(), BigDecimal.ZERO));
            itemRespVO.setReturnedAmount(ObjectUtil.defaultIfNull(returnedAmountMap.get(issueBatch.getId()), BigDecimal.ZERO));
            itemRespVO.setLossAmount(lossAmount);
            itemRespVO.setUnitPrice(issueQty.compareTo(BigDecimal.ZERO) > 0
                    ? ObjectUtil.defaultIfNull(issueBatch.getIssueAmount(), BigDecimal.ZERO).divide(issueQty, 6, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO.setScale(6, RoundingMode.HALF_UP));
            itemRespVO.setRemark(lossDetail == null ? null : lossDetail.getRemark());

            ErpProductRespVO product = productMap.get(issueItem.getMaterialId());
            if (product != null) {
                itemRespVO.setMaterialName(product.getName());
                itemRespVO.setMaterialCode(product.getMaterialCode());
                itemRespVO.setMaterialBarCode(product.getBarCode());
                itemRespVO.setProductUnitName(product.getUnitName());
            }
            ErpWarehouseDO warehouse = warehouseMap.get(issueItem.getWarehouseId());
            if (warehouse != null) {
                itemRespVO.setWarehouseName(warehouse.getName());
            }
            return itemRespVO;
        });
    }

    private List<ErpOutsourceLossEntryRespVO> buildOutsourceLossEntries(List<ErpOutsourceLossDetailDO> lossDetails) {
        if (CollUtil.isEmpty(lossDetails)) {
            return Collections.emptyList();
        }
        Map<Long, ErpProductRespVO> productMap = productService.getProductVOMap(convertSet(lossDetails, ErpOutsourceLossDetailDO::getMaterialId));
        Map<Long, ErpWarehouseDO> warehouseMap = warehouseService.getWarehouseMap(convertSet(lossDetails, ErpOutsourceLossDetailDO::getWarehouseId));
        return convertList(lossDetails, item -> {
            ErpOutsourceLossEntryRespVO respVO = BeanUtils.toBean(item, ErpOutsourceLossEntryRespVO.class);
            respVO.setUnitPrice(respVO.getLossQty() != null && respVO.getLossQty().compareTo(BigDecimal.ZERO) > 0
                    ? ObjectUtil.defaultIfNull(respVO.getLossAmount(), BigDecimal.ZERO).divide(respVO.getLossQty(), 6, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO.setScale(6, RoundingMode.HALF_UP));
            ErpProductRespVO product = productMap.get(item.getMaterialId());
            if (product != null) {
                respVO.setMaterialName(product.getName());
                respVO.setMaterialCode(product.getMaterialCode());
                respVO.setMaterialBarCode(product.getBarCode());
                respVO.setProductUnitName(product.getUnitName());
            }
            ErpWarehouseDO warehouse = warehouseMap.get(item.getWarehouseId());
            if (warehouse != null) {
                respVO.setWarehouseName(warehouse.getName());
            }
            return respVO;
        });
    }

    private Map<Long, BigDecimal> sumReturnQtyByIssueBatch(Collection<Long> issueBatchIds) {
        Map<Long, BigDecimal> result = new HashMap<>();
        erpOutsourceReturnBatchMapper.selectListByIssueBatchIds(issueBatchIds)
                .forEach(item -> result.merge(item.getIssueBatchId(),
                        ObjectUtil.defaultIfNull(item.getReturnQty(), BigDecimal.ZERO), BigDecimal::add));
        return result;
    }

    private Map<Long, BigDecimal> sumReturnAmountByIssueBatch(Collection<Long> issueBatchIds) {
        Map<Long, BigDecimal> result = new HashMap<>();
        erpOutsourceReturnBatchMapper.selectListByIssueBatchIds(issueBatchIds)
                .forEach(item -> result.merge(item.getIssueBatchId(),
                        ObjectUtil.defaultIfNull(item.getReturnAmount(), BigDecimal.ZERO), BigDecimal::add));
        return result;
    }

    private Map<Long, BigDecimal> sumLossQtyByIssueBatch(Collection<Long> issueBatchIds) {
        Map<Long, BigDecimal> result = new HashMap<>();
        erpOutsourceLossDetailMapper.selectListByIssueBatchIds(issueBatchIds)
                .forEach(item -> result.merge(item.getIssueBatchId(),
                        ObjectUtil.defaultIfNull(item.getLossQty(), BigDecimal.ZERO), BigDecimal::add));
        return result;
    }

    private BigDecimal sumLossQty(List<ErpOutsourceLossDetailDO> lossDetails) {
        if (CollUtil.isEmpty(lossDetails)) {
            return BigDecimal.ZERO;
        }
        return lossDetails.stream()
                .map(item -> ObjectUtil.defaultIfNull(item.getLossQty(), BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculatePendingBackfillQty(BigDecimal orderLossQty, BigDecimal recordedLossQty) {
        BigDecimal pendingQty = ObjectUtil.defaultIfNull(orderLossQty, BigDecimal.ZERO)
                .subtract(ObjectUtil.defaultIfNull(recordedLossQty, BigDecimal.ZERO));
        return pendingQty.compareTo(BigDecimal.ZERO) > 0 ? pendingQty : BigDecimal.ZERO;
    }

    private BigDecimal calculateUnresolvedQty(BigDecimal plannedQty, BigDecimal finishedQty, BigDecimal lossQty) {
        BigDecimal unresolvedQty = ObjectUtil.defaultIfNull(plannedQty, BigDecimal.ZERO)
                .subtract(ObjectUtil.defaultIfNull(finishedQty, BigDecimal.ZERO))
                .subtract(ObjectUtil.defaultIfNull(lossQty, BigDecimal.ZERO));
        return unresolvedQty.compareTo(BigDecimal.ZERO) > 0 ? unresolvedQty : BigDecimal.ZERO;
    }

    private record LossWriteItem(Long issueBatchId, BigDecimal lossQty, String remark) {
    }

    private void validateOrderBase(Integer orderType, Long supplierId, Long productId, BigDecimal plannedQty, Long bomId) {
        if (!Objects.equals(orderType, ErpOutsourceOrderTypeEnum.BOM.getType())
                && !Objects.equals(orderType, ErpOutsourceOrderTypeEnum.SIMPLE.getType())) {
            throw exception(OUTSOURCE_ORDER_STATUS_INVALID);
        }
        validateOrderTypeAndBom(orderType, productId, bomId);
        supplierService.validateSupplier(supplierId);
        productService.validProductList(List.of(productId));
        if (plannedQty == null || plannedQty.compareTo(BigDecimal.ZERO) <= 0) {
            throw exception(PRODUCTION_MATERIAL_QTY_INVALID);
        }
    }

    private void validateOrderTypeAndBom(Integer orderType, Long productId, Long bomId) {
        if (Objects.equals(orderType, ErpOutsourceOrderTypeEnum.BOM.getType())) {
            ErpBomDO bom = bomId == null ? null : bomService.getBom(bomId);
            if (bom == null) {
                throw exception(BOM_NOT_EXISTS);
            }
            if (!Objects.equals(bom.getProductId(), productId)) {
                throw exception(OUTSOURCE_ORDER_BOM_PRODUCT_MISMATCH);
            }
            return;
        }
        if (bomId != null) {
            throw exception(OUTSOURCE_ORDER_TYPE_BOM_INVALID);
        }
    }

    private void validateOrderMaterialTrackable(ErpOutsourceOrderDO order) {
        if (Objects.equals(order.getOrderType(), ErpOutsourceOrderTypeEnum.SIMPLE.getType())) {
            throw exception(OUTSOURCE_ORDER_MATERIAL_TRACKING_FORBIDDEN);
        }
    }

    private ErpOutsourceOrderDO validateOutsourceOrderExists(Long id) {
        ErpOutsourceOrderDO order = erpOutsourceOrderMapper.selectById(id);
        if (order == null) {
            throw exception(OUTSOURCE_ORDER_NOT_EXISTS);
        }
        return order;
    }

    private ErpOutsourceOrderDO validateOutsourceOrderOperable(Long id) {
        ErpOutsourceOrderDO order = validateOutsourceOrderExists(id);
        if (isOrderFinalStatus(order.getStatus())) {
            throw exception(OUTSOURCE_ORDER_STATUS_INVALID);
        }
        return order;
    }

    private ErpOutsourceOrderDO validateOutsourceOrderFeeable(Long id) {
        ErpOutsourceOrderDO order = validateOutsourceOrderExists(id);
        if (ErpOutsourceOrderStatusEnum.CLOSED.getStatus().equals(order.getStatus())) {
            throw exception(OUTSOURCE_ORDER_STATUS_INVALID);
        }
        return order;
    }

    private void validateIssueBatch(ErpOutsourceIssueCreateReqVO.Item item,
                                    ErpOutsourceIssueCreateReqVO.Batch batch,
                                    ErpStockBatchDO stockBatch) {
        if (stockBatch == null
                || !Objects.equals(stockBatch.getProductId(), item.getMaterialId())
                || !Objects.equals(stockBatch.getWarehouseId(), item.getWarehouseId())
                || !Objects.equals(stockBatch.getBatchNo(), batch.getBatchNo())) {
            throw exception(OUTSOURCE_BATCH_INVALID);
        }
    }

    private Integer validateIssueType(Integer issueType) {
        Integer actualIssueType = ErpOutsourceIssueTypeEnum.defaultType(issueType);
        if (!ErpOutsourceIssueTypeEnum.isValid(actualIssueType)) {
            throw exception(OUTSOURCE_ISSUE_TYPE_INVALID);
        }
        return actualIssueType;
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

    private BigDecimal calculateReturnAmount(ErpOutsourceIssueBatchDO issueBatch, BigDecimal returnQty) {
        if (issueBatch == null || issueBatch.getIssueQty() == null || issueBatch.getIssueQty().compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal unitPrice = ObjectUtil.defaultIfNull(issueBatch.getIssueAmount(), BigDecimal.ZERO)
                .divide(issueBatch.getIssueQty(), 6, RoundingMode.HALF_UP);
        return MoneyUtils.priceMultiply(unitPrice, returnQty);
    }

    private BigDecimal calculateNetMaterialCost(Long orderId) {
        return sumIssueAmount(erpOutsourceIssueMapper.selectListByOrderId(orderId))
                .subtract(sumReturnAmount(erpOutsourceReturnMapper.selectListByOrderId(orderId)));
    }

    private BigDecimal calculateProcessFee(Long orderId) {
        return sumFeeAmount(erpOutsourceFeeMapper.selectListByOrderId(orderId));
    }

    private BigDecimal sumIssueAmount(List<ErpOutsourceIssueDO> issues) {
        if (CollUtil.isEmpty(issues)) {
            return BigDecimal.ZERO;
        }
        return issues.stream().map(item -> ObjectUtil.defaultIfNull(item.getIssueAmount(), BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumIssueAmountByType(List<ErpOutsourceIssueDO> issues, Integer issueType) {
        if (CollUtil.isEmpty(issues)) {
            return BigDecimal.ZERO;
        }
        return issues.stream()
                .filter(item -> Objects.equals(ErpOutsourceIssueTypeEnum.defaultType(item.getIssueType()), issueType))
                .map(item -> ObjectUtil.defaultIfNull(item.getIssueAmount(), BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumReturnAmount(List<ErpOutsourceReturnDO> returns) {
        if (CollUtil.isEmpty(returns)) {
            return BigDecimal.ZERO;
        }
        return returns.stream().map(item -> ObjectUtil.defaultIfNull(item.getReturnAmount(), BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumFeeAmount(List<ErpOutsourceFeeDO> fees) {
        if (CollUtil.isEmpty(fees)) {
            return BigDecimal.ZERO;
        }
        return fees.stream().map(item -> ObjectUtil.defaultIfNull(item.getFeeAmount(), BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private boolean isOrderFinalStatus(Integer status) {
        return ErpOutsourceOrderStatusEnum.COMPLETED.getStatus().equals(status)
                || ErpOutsourceOrderStatusEnum.CLOSED.getStatus().equals(status);
    }

    private Integer resolveStatusAfterFeeCreate(Integer currentStatus) {
        if (ErpOutsourceOrderStatusEnum.CREATED.getStatus().equals(currentStatus)) {
            return ErpOutsourceOrderStatusEnum.PROCESSING.getStatus();
        }
        return currentStatus;
    }

    private ErpOutsourceCostDetailRespVO.IssueDetail buildCostIssueDetail(ErpOutsourceIssueDO issue) {
        ErpOutsourceCostDetailRespVO.IssueDetail detail = BeanUtils.toBean(issue, ErpOutsourceCostDetailRespVO.IssueDetail.class);
        detail.setIssueType(ErpOutsourceIssueTypeEnum.defaultType(issue.getIssueType()));
        detail.setIssueTypeName(ErpOutsourceIssueTypeEnum.resolveName(detail.getIssueType()));
        return detail;
    }

    private ErpOutsourceReconciliationRespVO.IssueDetail buildReconciliationIssueDetail(ErpOutsourceIssueDO issue) {
        ErpOutsourceReconciliationRespVO.IssueDetail detail = BeanUtils.toBean(issue, ErpOutsourceReconciliationRespVO.IssueDetail.class);
        detail.setIssueType(ErpOutsourceIssueTypeEnum.defaultType(issue.getIssueType()));
        detail.setIssueTypeName(ErpOutsourceIssueTypeEnum.resolveName(detail.getIssueType()));
        return detail;
    }

}
