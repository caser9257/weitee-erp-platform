package cn.weitee.erp.module.erp.service.stock;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.number.MoneyUtils;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.module.erp.controller.admin.stock.vo.out.ErpStockOutPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.stock.vo.out.ErpStockOutSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductDO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockDO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockOutDO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockOutItemDO;
import cn.weitee.erp.module.erp.dal.mysql.stock.ErpStockOutItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.stock.ErpStockOutMapper;
import cn.weitee.erp.module.erp.dal.redis.no.ErpNoRedisDAO;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.enums.stock.ErpStockRecordBizTypeEnum;
import cn.weitee.erp.module.erp.service.product.ErpProductService;
import cn.weitee.erp.module.erp.service.sale.ErpCustomerService;
import cn.weitee.erp.module.erp.service.stock.ErpStockBatchAllocationService;
import cn.weitee.erp.module.erp.service.stock.ErpStockRecordService;
import cn.weitee.erp.module.erp.service.stock.ErpStockService;
import cn.weitee.erp.module.erp.service.stock.bo.ErpStockBatchAllocateOutboundReqBO;
import cn.weitee.erp.module.erp.service.stock.bo.ErpStockRecordCreateReqBO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.*;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.*;

// TODO 芋艿：记录操作日志

/**
 * ERP 其它出库单 Service 实现类
 *
 * @author WeTai
 */
@Service
@Validated
@Slf4j
public class ErpStockOutServiceImpl implements ErpStockOutService {

    @Resource
    private ErpStockOutMapper erpStockOutMapper;
    @Resource
    private ErpStockOutItemMapper erpStockOutItemMapper;

    @Resource
    private ErpNoRedisDAO noRedisDAO;

    @Resource
    private ErpProductService productService;
    @Resource
    private ErpWarehouseService warehouseService;
    @Resource
    private ErpCustomerService customerService;
    @Resource
    private ErpStockRecordService stockRecordService;
    @Resource
    private ErpStockService stockService;
    @Resource
    private ErpStockBatchAllocationService stockBatchAllocationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createStockOut(ErpStockOutSaveReqVO createReqVO) {
        // 1.1 校验出库项的有效性
        List<ErpStockOutItemDO> stockOutItems = validateStockOutItems(createReqVO.getItems());
        // 1.2 校验客户
        customerService.validateCustomer(createReqVO.getCustomerId());
        // 1.3 生成出库单号，并校验唯一性
        String no = noRedisDAO.generate(ErpNoRedisDAO.STOCK_OUT_NO_PREFIX);
        if (erpStockOutMapper.selectByNo(no) != null) {
            throw exception(STOCK_OUT_NO_EXISTS);
        }

        // 2.1 插入出库单
        ErpStockOutDO stockOut = BeanUtils.toBean(createReqVO, ErpStockOutDO.class, in -> in
                .setNo(no).setStatus(ErpAuditStatus.DRAFT.getStatus())
                .setTotalCount(getSumValue(stockOutItems, ErpStockOutItemDO::getCount, BigDecimal::add))
                .setTotalPrice(getSumValue(stockOutItems, ErpStockOutItemDO::getTotalPrice, BigDecimal::add, BigDecimal.ZERO)));
        erpStockOutMapper.insert(stockOut);
        // 2.2 插入出库单项
        stockOutItems.forEach(o -> o.setOutId(stockOut.getId()));
        erpStockOutItemMapper.insertBatch(stockOutItems);
        return stockOut.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStockOut(ErpStockOutSaveReqVO updateReqVO) {
        // 1.1 校验存在
        ErpStockOutDO stockOut = validateStockOutExists(updateReqVO.getId());
        if (ErpAuditStatus.APPROVE.getStatus().equals(stockOut.getStatus())) {
            throw exception(STOCK_OUT_UPDATE_FAIL_APPROVE, stockOut.getNo());
        }
        if (ErpAuditStatus.PROCESS.getStatus().equals(stockOut.getStatus())) {
            throw exception(STOCK_OUT_UPDATE_FAIL_PROCESSING, stockOut.getNo());
        }
        // 1.2 校验客户
        customerService.validateCustomer(updateReqVO.getCustomerId());
        // 1.3 校验出库项的有效性
        List<ErpStockOutItemDO> stockOutItems = validateStockOutItems(updateReqVO.getItems());

        // 2.1 更新出库单
        ErpStockOutDO updateObj = BeanUtils.toBean(updateReqVO, ErpStockOutDO.class, in -> in
                .setTotalCount(getSumValue(stockOutItems, ErpStockOutItemDO::getCount, BigDecimal::add))
                .setTotalPrice(getSumValue(stockOutItems, ErpStockOutItemDO::getTotalPrice, BigDecimal::add)));
        erpStockOutMapper.updateById(updateObj);
        // 2.2 更新出库单项
        updateStockOutItemList(updateReqVO.getId(), stockOutItems);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStockOutStatus(Long id, Integer status) {
        boolean approve = ErpAuditStatus.APPROVE.getStatus().equals(status);
        // 1.1 校验存在
        ErpStockOutDO stockOut = validateStockOutExists(id);
        // 1.2 校验状态
        if (stockOut.getStatus().equals(status)) {
            throw exception(approve ? STOCK_OUT_APPROVE_FAIL : STOCK_OUT_PROCESS_FAIL);
        }

        // 2. 更新状态
        int updateCount = erpStockOutMapper.updateByIdAndStatus(id, stockOut.getStatus(),
                new ErpStockOutDO().setStatus(status));
        if (updateCount == 0) {
            throw exception(approve ? STOCK_OUT_APPROVE_FAIL : STOCK_OUT_PROCESS_FAIL);
        }

        // 3. 变更库存
        List<ErpStockOutItemDO> stockOutItems = erpStockOutItemMapper.selectListByOutId(id);
        Integer bizType = approve ? ErpStockRecordBizTypeEnum.OTHER_OUT.getType()
                : ErpStockRecordBizTypeEnum.OTHER_OUT_CANCEL.getType();
        if (approve) {
            allocateStockOutBatches(stockOut, stockOutItems);
        } else {
            stockBatchAllocationService.rollbackOutbound(ErpStockRecordBizTypeEnum.OTHER_OUT.getType(), id,
                    ErpStockRecordBizTypeEnum.OTHER_OUT_CANCEL.getType(), "其它出库反审核");
        }
        // 批量查询库存（消除 N+1）
        Set<Long> outProductIds = convertSet(stockOutItems, ErpStockOutItemDO::getProductId);
        List<ErpStockDO> stockList = stockService.getStockListByProductIds(outProductIds);
        Map<String, ErpStockDO> stockMap = stockList.stream().collect(Collectors.toMap(
                s -> s.getProductId() + ":" + s.getWarehouseId(), s -> s, (a, b) -> a));
        stockOutItems.forEach(stockOutItem -> {
            BigDecimal count = approve ? stockOutItem.getCount().negate() : stockOutItem.getCount();
            // 获取加权平均成本作为出库价格
            ErpStockDO stock = stockMap.get(stockOutItem.getProductId() + ":" + stockOutItem.getWarehouseId());
            BigDecimal price = stock != null ? stock.getAverageCost() : null;
            BigDecimal amount = price != null ? price.multiply(stockOutItem.getCount()) : null;
            stockRecordService.createStockRecord(new ErpStockRecordCreateReqBO(
                    stockOutItem.getProductId(), stockOutItem.getWarehouseId(), count,
                    bizType, stockOutItem.getOutId(), stockOutItem.getId(), stockOut.getNo(),
                    price, amount));
        });
    }

    @Override
    public void updateStockOutStatusManually(Long id, Integer status) {
        throw exception(STOCK_OUT_MANUAL_STATUS_UPDATE_FORBIDDEN, id);
    }

    private void allocateStockOutBatches(ErpStockOutDO stockOut, List<ErpStockOutItemDO> stockOutItems) {
        stockOutItems.forEach(item -> stockBatchAllocationService.allocateOutbound(new ErpStockBatchAllocateOutboundReqBO()
                .setProductId(item.getProductId())
                .setWarehouseId(item.getWarehouseId())
                .setCount(item.getCount())
                .setBizType(ErpStockRecordBizTypeEnum.OTHER_OUT.getType())
                .setBizId(stockOut.getId())
                .setBizItemId(item.getId())
                .setBizNo(stockOut.getNo())
                .setRemark(item.getRemark())));
    }

    private List<ErpStockOutItemDO> validateStockOutItems(List<ErpStockOutSaveReqVO.Item> list) {
        // 1.1 校验产品存在
        List<ErpProductDO> productList = productService.validProductList(
                convertSet(list, ErpStockOutSaveReqVO.Item::getProductId));
        Map<Long, ErpProductDO> productMap = convertMap(productList, ErpProductDO::getId);
        // 1.2 校验仓库存在
        warehouseService.validWarehouseList(convertSet(list, ErpStockOutSaveReqVO.Item::getWarehouseId));
        // 2. 转化为 ErpStockOutItemDO 列表
        return convertList(list, o -> BeanUtils.toBean(o, ErpStockOutItemDO.class, item -> item
                .setProductUnitId(productMap.get(item.getProductId()).getUnitId())
                .setTotalPrice(MoneyUtils.priceMultiply(item.getProductPrice(), item.getCount()))));
    }

    private void updateStockOutItemList(Long id, List<ErpStockOutItemDO> newList) {
        // 第一步，对比新老数据，获得添加、修改、删除的列表
        List<ErpStockOutItemDO> oldList = erpStockOutItemMapper.selectListByOutId(id);
        List<List<ErpStockOutItemDO>> diffList = diffList(oldList, newList, // id 不同，就认为是不同的记录
                (oldVal, newVal) -> oldVal.getId().equals(newVal.getId()));

        // 第二步，批量添加、修改、删除
        if (CollUtil.isNotEmpty(diffList.get(0))) {
            diffList.get(0).forEach(o -> o.setOutId(id));
            erpStockOutItemMapper.insertBatch(diffList.get(0));
        }
        if (CollUtil.isNotEmpty(diffList.get(1))) {
            erpStockOutItemMapper.updateBatch(diffList.get(1));
        }
        if (CollUtil.isNotEmpty(diffList.get(2))) {
            erpStockOutItemMapper.deleteByIds(convertList(diffList.get(2), ErpStockOutItemDO::getId));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteStockOut(List<Long> ids) {
        // 1. 校验不处于已审批
        List<ErpStockOutDO> stockOuts = erpStockOutMapper.selectByIds(ids);
        if (CollUtil.isEmpty(stockOuts)) {
            return;
        }
        stockOuts.forEach(stockOut -> {
            if (ErpAuditStatus.APPROVE.getStatus().equals(stockOut.getStatus())) {
                throw exception(STOCK_OUT_DELETE_FAIL_APPROVE, stockOut.getNo());
            }
            if (ErpAuditStatus.PROCESS.getStatus().equals(stockOut.getStatus())) {
                throw exception(STOCK_OUT_DELETE_FAIL_PROCESSING, stockOut.getNo());
            }
        });

        // 2. 遍历删除，并记录操作日志
        stockOuts.forEach(stockOut -> {
            // 2.1 删除出库单
            erpStockOutMapper.deleteById(stockOut.getId());
            // 2.2 删除出库单项
            erpStockOutItemMapper.deleteByOutId(stockOut.getId());
        });
    }

    private ErpStockOutDO validateStockOutExists(Long id) {
        ErpStockOutDO stockOut = erpStockOutMapper.selectById(id);
        if (stockOut == null) {
            throw exception(STOCK_OUT_NOT_EXISTS);
        }
        return stockOut;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStockOutStatusByBpm(Long id, String processInstanceId, Integer status, String reason) {
        ErpStockOutDO stockOut = validateStockOutExists(id);
        if (!ErpAuditStatus.APPROVE.getStatus().equals(status)) {
            throw exception(STOCK_OUT_UPDATE_FAIL_PROCESSING, id);
        }
        int updateCount = erpStockOutMapper.updateByIdStatusAndProcessInstanceId(id,
                ErpAuditStatus.PROCESS.getStatus(), processInstanceId,
                new ErpStockOutDO().setStatus(ErpAuditStatus.APPROVE.getStatus()).setProcessInstanceId(null));
        if (updateCount == 0) {
            throw exception(STOCK_OUT_UPDATE_FAIL_PROCESSING, id);
        }
        List<ErpStockOutItemDO> stockOutItems = erpStockOutItemMapper.selectListByOutId(id);
        allocateStockOutBatches(stockOut, stockOutItems);
        Set<Long> outProductIds = convertSet(stockOutItems, ErpStockOutItemDO::getProductId);
        List<ErpStockDO> stockList = stockService.getStockListByProductIds(outProductIds);
        Map<String, ErpStockDO> stockMap = stockList.stream().collect(Collectors.toMap(
                s -> s.getProductId() + ":" + s.getWarehouseId(), s -> s, (a, b) -> a));
        stockOutItems.forEach(stockOutItem -> {
            BigDecimal count = stockOutItem.getCount().negate();
            ErpStockDO stock = stockMap.get(stockOutItem.getProductId() + ":" + stockOutItem.getWarehouseId());
            BigDecimal price = stock != null ? stock.getAverageCost() : null;
            BigDecimal amount = price != null ? price.multiply(stockOutItem.getCount()) : null;
            stockRecordService.createStockRecord(new ErpStockRecordCreateReqBO(
                    stockOutItem.getProductId(), stockOutItem.getWarehouseId(), count,
                    ErpStockRecordBizTypeEnum.OTHER_OUT.getType(), stockOutItem.getOutId(), stockOutItem.getId(), stockOut.getNo(),
                    price, amount));
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rollbackStockOutStatusToDraftByBpm(Long id, String processInstanceId, String reason) {
        validateStockOutExists(id);
        int updateCount = erpStockOutMapper.resetStatusToDraftByBpm(id, processInstanceId);
        if (updateCount == 0) {
            throw exception(STOCK_OUT_UPDATE_FAIL_PROCESSING, id);
        }
    }

    @Override
    public ErpStockOutDO getStockOut(Long id) {
        return erpStockOutMapper.selectById(id);
    }

    @Override
    public PageResult<ErpStockOutDO> getStockOutPage(ErpStockOutPageReqVO pageReqVO) {
        return erpStockOutMapper.selectPage(pageReqVO);
    }

    // ==================== 出库项 ====================

    @Override
    public List<ErpStockOutItemDO> getStockOutItemListByOutId(Long outId) {
        return erpStockOutItemMapper.selectListByOutId(outId);
    }

    @Override
    public List<ErpStockOutItemDO> getStockOutItemListByOutIds(Collection<Long> outIds) {
        if (CollUtil.isEmpty(outIds)) {
            return Collections.emptyList();
        }
        return erpStockOutItemMapper.selectListByOutIds(outIds);
    }

}
