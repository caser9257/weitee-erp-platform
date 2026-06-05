package cn.iocoder.yudao.module.erp.service.stock;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpStockBatchAllocationDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpStockBatchDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpStockBatchReservationDO;
import cn.iocoder.yudao.module.erp.dal.mysql.stock.ErpStockBatchAllocationMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.stock.ErpStockBatchReservationMapper;
import cn.iocoder.yudao.module.erp.service.product.ErpProductService;
import cn.iocoder.yudao.module.erp.service.stock.bo.ErpStockBatchAllocateOutboundReqBO;
import cn.iocoder.yudao.module.erp.service.stock.bo.ErpStockBatchChangeReqBO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.STOCK_BATCH_ALLOCATION_EXISTS;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.STOCK_BATCH_RESERVATION_EXISTS;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.STOCK_BATCH_RESERVATION_INSUFFICIENT;

@Service
@Validated
public class ErpStockBatchReservationServiceImpl implements ErpStockBatchReservationService {

    @Resource
    private ErpStockBatchReservationMapper erpStockBatchReservationMapper;
    @Resource
    private ErpStockBatchAllocationMapper erpStockBatchAllocationMapper;
    @Resource
    private ErpStockBatchService stockBatchService;
    @Resource
    private ErpProductService productService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<ErpStockBatchReservationDO> reserveOutbound(ErpStockBatchAllocateOutboundReqBO reqBO) {
        if (!isBatchControlled(reqBO.getProductId())) {
            return Collections.emptyList();
        }
        List<ErpStockBatchReservationDO> existedReservations = reqBO.getBizItemId() == null
                ? erpStockBatchReservationMapper.selectListByBiz(reqBO.getBizType(), reqBO.getBizId())
                : erpStockBatchReservationMapper.selectListByBizItem(reqBO.getBizType(), reqBO.getBizId(), reqBO.getBizItemId());
        if (CollUtil.isNotEmpty(existedReservations)) {
            throw exception(STOCK_BATCH_RESERVATION_EXISTS, reqBO.getBizNo());
        }

        List<ErpStockBatchDO> batchList = stockBatchService.getAvailableStockBatchList(reqBO.getProductId(), reqBO.getWarehouseId());
        BigDecimal totalAvailableQty = batchList.stream()
                .map(item -> ObjectUtil.defaultIfNull(item.getAvailableQty(), BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalAvailableQty.compareTo(reqBO.getCount()) < 0) {
            throw exception(STOCK_BATCH_RESERVATION_INSUFFICIENT, reqBO.getBizNo(), reqBO.getCount(), totalAvailableQty);
        }

        BigDecimal remainingQty = reqBO.getCount();
        List<ErpStockBatchReservationDO> reservations = new ArrayList<>();
        for (ErpStockBatchDO batch : batchList) {
            if (remainingQty.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
            BigDecimal availableQty = ObjectUtil.defaultIfNull(batch.getAvailableQty(), BigDecimal.ZERO);
            if (availableQty.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            BigDecimal reservedQty = availableQty.min(remainingQty);
            stockBatchService.lockBatch(new ErpStockBatchChangeReqBO(
                    batch.getId(), reservedQty, reqBO.getBizType(), reqBO.getBizId(), reqBO.getBizItemId(),
                    reqBO.getBizNo(), reqBO.getRemark()));
            ErpStockBatchReservationDO reservation = buildReservation(reqBO, batch, reservedQty);
            erpStockBatchReservationMapper.insert(reservation);
            reservations.add(reservation);
            remainingQty = remainingQty.subtract(reservedQty);
        }
        return reservations;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void releaseReservation(Integer bizType, Long bizId, String remark) {
        List<ErpStockBatchReservationDO> reservations = erpStockBatchReservationMapper.selectListByBiz(bizType, bizId);
        if (CollUtil.isEmpty(reservations)) {
            return;
        }
        for (ErpStockBatchReservationDO reservation : reservations) {
            stockBatchService.releaseLockedBatch(new ErpStockBatchChangeReqBO(
                    reservation.getStockBatchId(), reservation.getReservedQty(), reservation.getBizType(),
                    reservation.getBizId(), reservation.getBizItemId(), reservation.getBizNo(), remark));
        }
        erpStockBatchReservationMapper.deleteByBiz(bizType, bizId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<ErpStockBatchAllocationDO> deductReservation(Integer bizType, Long bizId, String remark) {
        List<ErpStockBatchReservationDO> reservations = erpStockBatchReservationMapper.selectListByBiz(bizType, bizId);
        if (CollUtil.isEmpty(reservations)) {
            return Collections.emptyList();
        }
        if (CollUtil.isNotEmpty(erpStockBatchAllocationMapper.selectListByBiz(bizType, bizId))) {
            throw exception(STOCK_BATCH_ALLOCATION_EXISTS, reservations.get(0).getBizNo());
        }
        List<ErpStockBatchAllocationDO> allocations = new ArrayList<>();
        for (ErpStockBatchReservationDO reservation : reservations) {
            stockBatchService.deductLockedBatch(new ErpStockBatchChangeReqBO(
                    reservation.getStockBatchId(), reservation.getReservedQty(), reservation.getBizType(),
                    reservation.getBizId(), reservation.getBizItemId(), reservation.getBizNo(), remark));
            ErpStockBatchAllocationDO allocation = buildAllocation(reservation, remark);
            erpStockBatchAllocationMapper.insert(allocation);
            allocations.add(allocation);
        }
        erpStockBatchReservationMapper.deleteByBiz(bizType, bizId);
        return allocations;
    }

    @Override
    public List<ErpStockBatchReservationDO> getReservationListByBiz(Integer bizType, Long bizId) {
        return erpStockBatchReservationMapper.selectListByBiz(bizType, bizId);
    }

    @Override
    public List<ErpStockBatchReservationDO> getReservationListByStockBatchId(Long stockBatchId) {
        return erpStockBatchReservationMapper.selectListByStockBatchId(stockBatchId);
    }

    private boolean isBatchControlled(Long productId) {
        ErpProductDO product = productService.getProduct(productId);
        return product != null && Boolean.TRUE.equals(product.getBatchControlFlag());
    }

    private ErpStockBatchReservationDO buildReservation(ErpStockBatchAllocateOutboundReqBO reqBO,
                                                        ErpStockBatchDO batch,
                                                        BigDecimal reservedQty) {
        return new ErpStockBatchReservationDO()
                .setBizType(reqBO.getBizType())
                .setBizId(reqBO.getBizId())
                .setBizItemId(reqBO.getBizItemId())
                .setBizNo(reqBO.getBizNo())
                .setProductId(reqBO.getProductId())
                .setWarehouseId(reqBO.getWarehouseId())
                .setStockBatchId(batch.getId())
                .setBatchNo(batch.getBatchNo())
                .setReservedQty(reservedQty)
                .setInboundTime(batch.getInboundTime())
                .setProduceDate(batch.getProduceDate())
                .setExpireDate(batch.getExpireDate())
                .setRemark(reqBO.getRemark());
    }

    private ErpStockBatchAllocationDO buildAllocation(ErpStockBatchReservationDO reservation, String remark) {
        return new ErpStockBatchAllocationDO()
                .setBizType(reservation.getBizType())
                .setBizId(reservation.getBizId())
                .setBizItemId(reservation.getBizItemId())
                .setBizNo(reservation.getBizNo())
                .setProductId(reservation.getProductId())
                .setWarehouseId(reservation.getWarehouseId())
                .setStockBatchId(reservation.getStockBatchId())
                .setBatchNo(reservation.getBatchNo())
                .setOutQty(reservation.getReservedQty())
                .setInboundTime(reservation.getInboundTime())
                .setProduceDate(reservation.getProduceDate())
                .setExpireDate(reservation.getExpireDate())
                .setRemark(remark);
    }

}
