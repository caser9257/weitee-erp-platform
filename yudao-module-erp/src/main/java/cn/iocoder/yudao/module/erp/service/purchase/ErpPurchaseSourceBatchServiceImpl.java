package cn.iocoder.yudao.module.erp.service.purchase;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.sourcebatch.ErpPurchaseSourceBatchPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.sourcebatch.ErpPurchaseSourceBatchSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseOrderDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseOrderItemDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseSourceBatchDO;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseOrderItemMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseOrderMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseSourceBatchMapper;
import cn.iocoder.yudao.module.erp.dal.redis.no.ErpNoRedisDAO;
import cn.iocoder.yudao.module.erp.enums.ErpPurchaseSourceBatchStatusEnum;
import cn.iocoder.yudao.module.erp.service.product.ErpProductService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.PURCHASE_ORDER_NOT_EXISTS;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.PURCHASE_SOURCE_BATCH_NO_EXISTS;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.PURCHASE_SOURCE_BATCH_NOT_EXISTS;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.PURCHASE_SOURCE_BATCH_ORDER_ITEM_NOT_EXISTS;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.PURCHASE_SOURCE_BATCH_UPDATE_FAIL_CLOSED;

@Service
@Validated
public class ErpPurchaseSourceBatchServiceImpl implements ErpPurchaseSourceBatchService {

    @Resource
    private ErpPurchaseSourceBatchMapper erpPurchaseSourceBatchMapper;
    @Resource
    private ErpPurchaseOrderMapper erpPurchaseOrderMapper;
    @Resource
    private ErpPurchaseOrderItemMapper erpPurchaseOrderItemMapper;
    @Resource
    private ErpProductService productService;
    @Resource
    private ErpNoRedisDAO noRedisDAO;

    @Override
    public Long createPurchaseSourceBatch(ErpPurchaseSourceBatchSaveReqVO createReqVO) {
        PurchaseSourceContext context = validateAndBuildContext(createReqVO.getPurchaseOrderItemId());
        String batchNo = noRedisDAO.generate(ErpNoRedisDAO.PURCHASE_SOURCE_BATCH_NO_PREFIX);
        if (erpPurchaseSourceBatchMapper.selectByBatchNo(batchNo) != null) {
            throw exception(PURCHASE_SOURCE_BATCH_NO_EXISTS);
        }
        ErpPurchaseSourceBatchDO batch = BeanUtils.toBean(createReqVO, ErpPurchaseSourceBatchDO.class, item -> item
                .setBatchNo(batchNo)
                .setProductId(context.product.getId())
                .setPurchaseOrderId(context.order.getId())
                .setPurchaseOrderItemId(context.orderItem.getId())
                .setSupplierId(context.order.getSupplierId())
                .setStatus(ErpPurchaseSourceBatchStatusEnum.ACTIVE.getStatus()));
        erpPurchaseSourceBatchMapper.insert(batch);
        return batch.getId();
    }

    @Override
    public void updatePurchaseSourceBatch(ErpPurchaseSourceBatchSaveReqVO updateReqVO) {
        ErpPurchaseSourceBatchDO existed = validatePurchaseSourceBatchExists(updateReqVO.getId());
        if (ErpPurchaseSourceBatchStatusEnum.CLOSED.getStatus().equals(existed.getStatus())) {
            throw exception(PURCHASE_SOURCE_BATCH_UPDATE_FAIL_CLOSED);
        }
        PurchaseSourceContext context = validateAndBuildContext(updateReqVO.getPurchaseOrderItemId());
        ErpPurchaseSourceBatchDO updateObj = BeanUtils.toBean(updateReqVO, ErpPurchaseSourceBatchDO.class, item -> item
                .setProductId(context.product.getId())
                .setPurchaseOrderId(context.order.getId())
                .setPurchaseOrderItemId(context.orderItem.getId())
                .setSupplierId(context.order.getSupplierId()));
        erpPurchaseSourceBatchMapper.updateById(updateObj);
    }

    @Override
    public void closePurchaseSourceBatch(Long id) {
        ErpPurchaseSourceBatchDO existed = validatePurchaseSourceBatchExists(id);
        if (ErpPurchaseSourceBatchStatusEnum.CLOSED.getStatus().equals(existed.getStatus())) {
            return;
        }
        erpPurchaseSourceBatchMapper.updateById(new ErpPurchaseSourceBatchDO()
                .setId(id)
                .setStatus(ErpPurchaseSourceBatchStatusEnum.CLOSED.getStatus()));
    }

    @Override
    public ErpPurchaseSourceBatchDO getPurchaseSourceBatch(Long id) {
        return erpPurchaseSourceBatchMapper.selectById(id);
    }

    @Override
    public ErpPurchaseSourceBatchDO validatePurchaseSourceBatch(Long id) {
        return validatePurchaseSourceBatchExists(id);
    }

    @Override
    public List<ErpPurchaseSourceBatchDO> getPurchaseSourceBatchList(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return erpPurchaseSourceBatchMapper.selectByIds(ids);
    }

    @Override
    public PageResult<ErpPurchaseSourceBatchDO> getPurchaseSourceBatchPage(ErpPurchaseSourceBatchPageReqVO pageReqVO) {
        return erpPurchaseSourceBatchMapper.selectPage(pageReqVO);
    }

    private ErpPurchaseSourceBatchDO validatePurchaseSourceBatchExists(Long id) {
        ErpPurchaseSourceBatchDO batch = erpPurchaseSourceBatchMapper.selectById(id);
        if (batch == null) {
            throw exception(PURCHASE_SOURCE_BATCH_NOT_EXISTS);
        }
        return batch;
    }

    private PurchaseSourceContext validateAndBuildContext(Long purchaseOrderItemId) {
        ErpPurchaseOrderItemDO orderItem = erpPurchaseOrderItemMapper.selectById(purchaseOrderItemId);
        if (orderItem == null) {
            throw exception(PURCHASE_SOURCE_BATCH_ORDER_ITEM_NOT_EXISTS);
        }
        ErpPurchaseOrderDO order = erpPurchaseOrderMapper.selectById(orderItem.getOrderId());
        if (order == null) {
            throw exception(PURCHASE_ORDER_NOT_EXISTS);
        }
        ErpProductDO product = productService.validProductList(java.util.Set.of(orderItem.getProductId())).get(0);
        return new PurchaseSourceContext(order, orderItem, product);
    }

    private static class PurchaseSourceContext {

        private final ErpPurchaseOrderDO order;
        private final ErpPurchaseOrderItemDO orderItem;
        private final ErpProductDO product;

        private PurchaseSourceContext(ErpPurchaseOrderDO order, ErpPurchaseOrderItemDO orderItem,
                                      ErpProductDO product) {
            this.order = order;
            this.orderItem = orderItem;
            this.product = product;
        }
    }
}
