package cn.weitee.erp.module.erp.service.stock;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.number.MoneyUtils;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.module.erp.controller.admin.stock.vo.in.ErpStockInPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.stock.vo.in.ErpStockInSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductDO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockDO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockInDO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockInItemDO;
import cn.weitee.erp.module.erp.dal.mysql.stock.ErpStockInItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.stock.ErpStockInMapper;
import cn.weitee.erp.module.erp.dal.redis.no.ErpNoRedisDAO;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.enums.stock.ErpStockRecordBizTypeEnum;
import cn.weitee.erp.module.erp.service.product.ErpProductService;
import cn.weitee.erp.module.erp.service.product.ErpProductUnitConversionService;
import cn.weitee.erp.module.erp.service.purchase.ErpSupplierService;
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
 * ERP 其它入库单 Service 实现类
 *
 * @author WeTai
 */
@Service
@Validated
@Slf4j
public class ErpStockInServiceImpl implements ErpStockInService {

    @Resource
    private ErpStockInMapper erpStockInMapper;
    @Resource
    private ErpStockInItemMapper erpStockInItemMapper;

    @Resource
    private ErpNoRedisDAO noRedisDAO;

    @Resource
    private ErpProductService productService;
    @Resource
    private ErpProductUnitConversionService unitConversionService;
    @Resource
    private ErpWarehouseService warehouseService;
    @Resource
    private ErpSupplierService supplierService;
    @Resource
    private ErpStockRecordService stockRecordService;
    @Resource
    private ErpStockService stockService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createStockIn(ErpStockInSaveReqVO createReqVO) {
        // 1.1 校验入库项的有效性
        List<ErpStockInItemDO> stockInItems = validateStockInItems(createReqVO.getItems());
        // 1.2 校验供应商
        supplierService.validateSupplier(createReqVO.getSupplierId());
        // 1.3 生成入库单号，并校验唯一性
        String no = noRedisDAO.generate(ErpNoRedisDAO.STOCK_IN_NO_PREFIX);
        if (erpStockInMapper.selectByNo(no) != null) {
            throw exception(STOCK_IN_NO_EXISTS);
        }

        // 2.1 插入入库单
        ErpStockInDO stockIn = BeanUtils.toBean(createReqVO, ErpStockInDO.class, in -> in
                .setNo(no).setStatus(ErpAuditStatus.DRAFT.getStatus())
                .setTotalCount(getSumValue(stockInItems, ErpStockInItemDO::getCount, BigDecimal::add))
                .setTotalPrice(getSumValue(stockInItems, ErpStockInItemDO::getTotalPrice, BigDecimal::add, BigDecimal.ZERO)));
        erpStockInMapper.insert(stockIn);
        // 2.2 插入入库单项
        stockInItems.forEach(o -> o.setInId(stockIn.getId()));
        erpStockInItemMapper.insertBatch(stockInItems);
        return stockIn.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStockIn(ErpStockInSaveReqVO updateReqVO) {
        // 1.1 校验存在
        ErpStockInDO stockIn = validateStockInExists(updateReqVO.getId());
        if (ErpAuditStatus.APPROVE.getStatus().equals(stockIn.getStatus())) {
            throw exception(STOCK_IN_UPDATE_FAIL_APPROVE, stockIn.getNo());
        }
        if (ErpAuditStatus.PROCESS.getStatus().equals(stockIn.getStatus())) {
            throw exception(STOCK_IN_UPDATE_FAIL_PROCESSING, stockIn.getNo());
        }
        // 1.2 校验供应商
        supplierService.validateSupplier(updateReqVO.getSupplierId());
        // 1.3 校验入库项的有效性
        List<ErpStockInItemDO> stockInItems = validateStockInItems(updateReqVO.getItems());

        // 2.1 更新入库单
        ErpStockInDO updateObj = BeanUtils.toBean(updateReqVO, ErpStockInDO.class, in -> in
                .setTotalCount(getSumValue(stockInItems, ErpStockInItemDO::getCount, BigDecimal::add))
                .setTotalPrice(getSumValue(stockInItems, ErpStockInItemDO::getTotalPrice, BigDecimal::add)));
        erpStockInMapper.updateById(updateObj);
        // 2.2 更新入库单项
        updateStockInItemList(updateReqVO.getId(), stockInItems);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStockInStatus(Long id, Integer status) {
        boolean approve = ErpAuditStatus.APPROVE.getStatus().equals(status);
        // 1.1 校验存在
        ErpStockInDO stockIn = validateStockInExists(id);
        // 1.2 校验状态
        if (stockIn.getStatus().equals(status)) {
            throw exception(approve ? STOCK_IN_APPROVE_FAIL : STOCK_IN_PROCESS_FAIL);
        }

        // 2. 更新状态
        int updateCount = erpStockInMapper.updateByIdAndStatus(id, stockIn.getStatus(),
                new ErpStockInDO().setStatus(status));
        if (updateCount == 0) {
            throw exception(approve ? STOCK_IN_APPROVE_FAIL : STOCK_IN_PROCESS_FAIL);
        }

        // 3. 变更库存
        List<ErpStockInItemDO> stockInItems = erpStockInItemMapper.selectListByInId(id);
        Integer bizType = approve ? ErpStockRecordBizTypeEnum.OTHER_IN.getType()
                : ErpStockRecordBizTypeEnum.OTHER_IN_CANCEL.getType();
        // 批量查询库存（消除 N+1）
        Set<Long> inProductIds = convertSet(stockInItems, ErpStockInItemDO::getProductId);
        List<ErpStockDO> stockList = stockService.getStockListByProductIds(inProductIds);
        Map<String, ErpStockDO> stockMap = stockList.stream().collect(Collectors.toMap(
                s -> s.getProductId() + ":" + s.getWarehouseId(), s -> s, (a, b) -> a));
        stockInItems.forEach(stockInItem -> {
            BigDecimal count = approve ? stockInItem.getCount() : stockInItem.getCount().negate();
            // 获取加权平均成本作为入库价格
            ErpStockDO stock = stockMap.get(stockInItem.getProductId() + ":" + stockInItem.getWarehouseId());
            BigDecimal price = stock != null ? stock.getAverageCost() : null;
            BigDecimal amount = price != null ? price.multiply(stockInItem.getCount()) : null;
            stockRecordService.createStockRecord(new ErpStockRecordCreateReqBO(
                    stockInItem.getProductId(), stockInItem.getWarehouseId(), count,
                    bizType, stockInItem.getInId(), stockInItem.getId(), stockIn.getNo(),
                    price, amount));
        });
    }

    @Override
    public void updateStockInStatusManually(Long id, Integer status) {
        throw exception(STOCK_IN_MANUAL_STATUS_UPDATE_FORBIDDEN, id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStockInStatusByBpm(Long id, String processInstanceId, Integer status, String reason) {
        ErpStockInDO stockIn = validateStockInExists(id);
        if (!ObjectUtil.equal(processInstanceId, stockIn.getProcessInstanceId())) {
            throw exception(STOCK_IN_STATUS_UPDATE_ILLEGAL);
        }
        if (!ErpAuditStatus.PROCESS.getStatus().equals(stockIn.getStatus())) {
            log.warn("[updateStockInStatusByBpm] 忽略非处理中其它入库单回调，id={}, currentStatus={}, callbackStatus={}",
                    id, stockIn.getStatus(), status);
            return;
        }
        if (!ErpAuditStatus.APPROVE.getStatus().equals(status)) {
            throw exception(STOCK_IN_STATUS_UPDATE_ILLEGAL);
        }
        int updateCount = erpStockInMapper.updateByIdStatusAndProcessInstanceId(id,
                ErpAuditStatus.PROCESS.getStatus(), processInstanceId,
                new ErpStockInDO().setStatus(ErpAuditStatus.APPROVE.getStatus()));
        if (updateCount == 0) {
            throw exception(STOCK_IN_STATUS_UPDATE_ILLEGAL);
        }
        erpStockInMapper.clearProcessInstanceId(id, processInstanceId);
        List<ErpStockInItemDO> stockInItems = erpStockInItemMapper.selectListByInId(id);
        Set<Long> inProductIds = convertSet(stockInItems, ErpStockInItemDO::getProductId);
        List<ErpStockDO> stockList = stockService.getStockListByProductIds(inProductIds);
        Map<String, ErpStockDO> stockMap = stockList.stream().collect(Collectors.toMap(
                s -> s.getProductId() + ":" + s.getWarehouseId(), s -> s, (a, b) -> a));
        stockInItems.forEach(stockInItem -> {
            BigDecimal count = stockInItem.getCount();
            ErpStockDO stock = stockMap.get(stockInItem.getProductId() + ":" + stockInItem.getWarehouseId());
            BigDecimal price = stock != null ? stock.getAverageCost() : null;
            BigDecimal amount = price != null ? price.multiply(stockInItem.getCount()) : null;
            stockRecordService.createStockRecord(new ErpStockRecordCreateReqBO(
                    stockInItem.getProductId(), stockInItem.getWarehouseId(), count,
                    ErpStockRecordBizTypeEnum.OTHER_IN.getType(), stockInItem.getInId(), stockInItem.getId(), stockIn.getNo(),
                    price, amount));
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rollbackStockInStatusToDraftByBpm(Long id, String processInstanceId, String reason) {
        ErpStockInDO stockIn = validateStockInExists(id);
        if (!ErpAuditStatus.PROCESS.getStatus().equals(stockIn.getStatus())) {
            log.warn("[rollbackStockInStatusToDraftByBpm] 忽略非处理中其它入库单回退回调，id={}, currentStatus={}",
                    id, stockIn.getStatus());
            return;
        }
        int updateCount = erpStockInMapper.resetStatusToDraftByBpm(id, processInstanceId);
        if (updateCount == 0) {
            throw exception(STOCK_IN_STATUS_UPDATE_ILLEGAL);
        }
    }

    private List<ErpStockInItemDO> validateStockInItems(List<ErpStockInSaveReqVO.Item> list) {
        // 1.1 校验产品存在
        productService.validProductList(convertSet(list, ErpStockInSaveReqVO.Item::getProductId));
        // 1.2 校验仓库存在
        warehouseService.validWarehouseList(convertSet(
                list, ErpStockInSaveReqVO.Item::getWarehouseId));
        // 2. 转化为 ErpStockInItemDO 列表
        List<ErpStockInItemDO> items = convertList(list, o -> BeanUtils.toBean(o, ErpStockInItemDO.class));
        // 3. 批量换算录入单位：count 换算为基本单位记账数量，inputCount/conversionRate 保留录入口径
        List<ErpProductUnitConversionService.ConversionResult> results = unitConversionService.convertBatch(
                convertList(items, item -> new ErpProductUnitConversionService.ConversionRequest(
                        item.getProductId(), item.getProductUnitId(), item.getCount())));
        for (int i = 0; i < items.size(); i++) {
            ErpStockInItemDO item = items.get(i);
            ErpProductUnitConversionService.ConversionResult result = results.get(i);
            item.setProductUnitId(result.getInputUnitId());
            item.setInputCount(result.getInputCount());
            item.setConversionRate(result.getConversionRate());
            item.setCount(result.getBaseCount());
            // 4. 金额计算：单价为录入单位口径，金额 = 单价 × 录入数量
            item.setTotalPrice(MoneyUtils.priceMultiply(item.getProductPrice(), result.getInputCount()));
        }
        return items;
    }

    private void updateStockInItemList(Long id, List<ErpStockInItemDO> newList) {
        // 第一步，对比新老数据，获得添加、修改、删除的列表
        List<ErpStockInItemDO> oldList = erpStockInItemMapper.selectListByInId(id);
        List<List<ErpStockInItemDO>> diffList = diffList(oldList, newList, // id 不同，就认为是不同的记录
                (oldVal, newVal) -> oldVal.getId().equals(newVal.getId()));

        // 第二步，批量添加、修改、删除
        if (CollUtil.isNotEmpty(diffList.get(0))) {
            diffList.get(0).forEach(o -> o.setInId(id));
            erpStockInItemMapper.insertBatch(diffList.get(0));
        }
        if (CollUtil.isNotEmpty(diffList.get(1))) {
            erpStockInItemMapper.updateBatch(diffList.get(1));
        }
        if (CollUtil.isNotEmpty(diffList.get(2))) {
            erpStockInItemMapper.deleteByIds(convertList(diffList.get(2), ErpStockInItemDO::getId));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteStockIn(List<Long> ids) {
        // 1. 校验不处于已审批
        List<ErpStockInDO> stockIns = erpStockInMapper.selectByIds(ids);
        if (CollUtil.isEmpty(stockIns)) {
            return;
        }
        stockIns.forEach(stockIn -> {
            if (ErpAuditStatus.APPROVE.getStatus().equals(stockIn.getStatus())) {
                throw exception(STOCK_IN_DELETE_FAIL_APPROVE, stockIn.getNo());
            }
            if (ErpAuditStatus.PROCESS.getStatus().equals(stockIn.getStatus())) {
                throw exception(STOCK_IN_DELETE_FAIL_PROCESSING, stockIn.getNo());
            }
        });

        // 2. 遍历删除，并记录操作日志
        stockIns.forEach(stockIn -> {
            // 2.1 删除入库单
            erpStockInMapper.deleteById(stockIn.getId());
            // 2.2 删除入库单项
            erpStockInItemMapper.deleteByInId(stockIn.getId());
        });
    }

    private ErpStockInDO validateStockInExists(Long id) {
        ErpStockInDO stockIn = erpStockInMapper.selectById(id);
        if (stockIn == null) {
            throw exception(STOCK_IN_NOT_EXISTS);
        }
        return stockIn;
    }

    @Override
    public ErpStockInDO getStockIn(Long id) {
        return erpStockInMapper.selectById(id);
    }

    @Override
    public PageResult<ErpStockInDO> getStockInPage(ErpStockInPageReqVO pageReqVO) {
        return erpStockInMapper.selectPage(pageReqVO);
    }

    // ==================== 入库项 ====================

    @Override
    public List<ErpStockInItemDO> getStockInItemListByInId(Long inId) {
        return erpStockInItemMapper.selectListByInId(inId);
    }

    @Override
    public List<ErpStockInItemDO> getStockInItemListByInIds(Collection<Long> inIds) {
        if (CollUtil.isEmpty(inIds)) {
            return Collections.emptyList();
        }
        return erpStockInItemMapper.selectListByInIds(inIds);
    }

}
