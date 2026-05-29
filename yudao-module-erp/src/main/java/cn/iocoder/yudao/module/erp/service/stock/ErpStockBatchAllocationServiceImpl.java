package cn.iocoder.yudao.module.erp.service.stock;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpStockBatchAllocationDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpStockBatchDO;
import cn.iocoder.yudao.module.erp.dal.mysql.stock.ErpStockBatchAllocationMapper;
import cn.iocoder.yudao.module.erp.service.product.ErpProductService;
import cn.iocoder.yudao.module.erp.service.stock.bo.ErpStockBatchAllocateOutboundReqBO;
import cn.iocoder.yudao.module.erp.service.stock.bo.ErpStockBatchChangeReqBO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.STOCK_BATCH_ALLOCATION_EXISTS;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.STOCK_BATCH_ALLOCATION_INSUFFICIENT;

@Service
@Validated
public class ErpStockBatchAllocationServiceImpl implements ErpStockBatchAllocationService {

    @Resource
    private ErpStockBatchAllocationMapper erpStockBatchAllocationMapper;
    @Resource
    private ErpStockBatchService stockBatchService;
    @Resource
    private ErpProductService productService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<ErpStockBatchAllocationDO> allocateOutbound(ErpStockBatchAllocateOutboundReqBO reqBO) {
        if (!isBatchControlled(reqBO.getProductId())) {
            return Collections.emptyList();
        }
        List<ErpStockBatchAllocationDO> existedAllocations = reqBO.getBizItemId() == null
                ? erpStockBatchAllocationMapper.selectListByBiz(reqBO.getBizType(), reqBO.getBizId())
                : erpStockBatchAllocationMapper.selectListByBizItem(reqBO.getBizType(), reqBO.getBizId(), reqBO.getBizItemId());
        if (CollUtil.isNotEmpty(existedAllocations)) {
            throw exception(STOCK_BATCH_ALLOCATION_EXISTS, reqBO.getBizNo());
        }

        List<ErpStockBatchDO> batchList = stockBatchService.getAvailableStockBatchList(reqBO.getProductId(), reqBO.getWarehouseId());
        BigDecimal totalAvailableQty = batchList.stream()
                .map(item -> ObjectUtil.defaultIfNull(item.getAvailableQty(), BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalAvailableQty.compareTo(reqBO.getCount()) < 0) {
            throw exception(STOCK_BATCH_ALLOCATION_INSUFFICIENT, reqBO.getBizNo(), reqBO.getCount(), totalAvailableQty);
        }

        BigDecimal remainingQty = reqBO.getCount();
        List<ErpStockBatchAllocationDO> allocations = new ArrayList<>();
        for (ErpStockBatchDO batch : batchList) {
            if (remainingQty.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
            BigDecimal availableQty = ObjectUtil.defaultIfNull(batch.getAvailableQty(), BigDecimal.ZERO);
            if (availableQty.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            BigDecimal outQty = availableQty.min(remainingQty);
            stockBatchService.decreaseBatch(new ErpStockBatchChangeReqBO(
                    batch.getId(), outQty, reqBO.getBizType(), reqBO.getBizId(), reqBO.getBizItemId(),
                    reqBO.getBizNo(), reqBO.getRemark()));
            ErpStockBatchAllocationDO allocation = buildAllocation(reqBO, batch, outQty);
            erpStockBatchAllocationMapper.insert(allocation);
            allocations.add(allocation);
            remainingQty = remainingQty.subtract(outQty);
        }
        return allocations;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rollbackOutbound(Integer bizType, Long bizId, Integer rollbackBizType, String remark) {
        List<ErpStockBatchAllocationDO> allocations = erpStockBatchAllocationMapper.selectListByBiz(bizType, bizId);
        if (CollUtil.isEmpty(allocations)) {
            return;
        }
        for (ErpStockBatchAllocationDO allocation : allocations) {
            stockBatchService.increaseBatch(new ErpStockBatchChangeReqBO(
                    allocation.getStockBatchId(), allocation.getOutQty(), rollbackBizType, allocation.getBizId(),
                    allocation.getBizItemId(), allocation.getBizNo(), remark));
        }
        erpStockBatchAllocationMapper.deleteByBiz(bizType, bizId);
    }

    @Override
    public List<ErpStockBatchAllocationDO> getAllocationListByBiz(Integer bizType, Long bizId) {
        return erpStockBatchAllocationMapper.selectListByBiz(bizType, bizId);
    }

    @Override
    public List<ErpStockBatchAllocationDO> getAllocationListByStockBatchId(Long stockBatchId) {
        return erpStockBatchAllocationMapper.selectListByStockBatchId(stockBatchId);
    }

    private boolean isBatchControlled(Long productId) {
        ErpProductDO product = productService.getProduct(productId);
        return product != null && Boolean.TRUE.equals(product.getBatchControlFlag());
    }

    private ErpStockBatchAllocationDO buildAllocation(ErpStockBatchAllocateOutboundReqBO reqBO,
                                                      ErpStockBatchDO batch,
                                                      BigDecimal outQty) {
        return new ErpStockBatchAllocationDO()
                .setBizType(reqBO.getBizType())
                .setBizId(reqBO.getBizId())
                .setBizItemId(reqBO.getBizItemId())
                .setBizNo(reqBO.getBizNo())
                .setProductId(reqBO.getProductId())
                .setWarehouseId(reqBO.getWarehouseId())
                .setStockBatchId(batch.getId())
                .setBatchNo(batch.getBatchNo())
                .setOutQty(outQty)
                .setInboundTime(batch.getInboundTime())
                .setProduceDate(batch.getProduceDate())
                .setExpireDate(batch.getExpireDate())
                .setRemark(reqBO.getRemark());
    }
}
