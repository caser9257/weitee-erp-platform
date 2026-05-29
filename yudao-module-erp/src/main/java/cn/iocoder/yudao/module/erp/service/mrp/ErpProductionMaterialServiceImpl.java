package cn.iocoder.yudao.module.erp.service.mrp;

import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.material.ErpProductionMaterialBatchCandidatesRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.material.ErpProductionMaterialRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionMaterialDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionOrderDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpStockBatchDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpWarehouseDO;
import cn.iocoder.yudao.module.erp.dal.mysql.mrp.ErpProductionMaterialMapper;
import cn.iocoder.yudao.module.erp.service.product.ErpProductService;
import cn.iocoder.yudao.module.erp.service.stock.ErpStockBatchService;
import cn.iocoder.yudao.module.erp.service.stock.ErpWarehouseService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.PRODUCTION_MATERIAL_NOT_EXISTS;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.PRODUCTION_ORDER_NOT_EXISTS;

@Service
@Validated
public class ErpProductionMaterialServiceImpl implements ErpProductionMaterialService {

    @Resource
    private ErpProductionMaterialMapper erpProductionMaterialMapper;
    @Resource
    private ErpProductionOrderService productionOrderService;
    @Resource
    private ErpProductService productService;
    @Resource
    private ErpWarehouseService warehouseService;
    @Resource
    private ErpStockBatchService stockBatchService;

    @Override
    public ErpProductionMaterialDO validateProductionMaterial(Long id) {
        ErpProductionMaterialDO material = erpProductionMaterialMapper.selectById(id);
        if (material == null) {
            throw exception(PRODUCTION_MATERIAL_NOT_EXISTS);
        }
        return material;
    }

    @Override
    public List<ErpProductionMaterialRespVO> getProductionMaterialList(Long productionOrderId) {
        ErpProductionOrderDO order = productionOrderService.getProductionOrder(productionOrderId);
        if (order == null) {
            throw exception(PRODUCTION_ORDER_NOT_EXISTS);
        }
        List<ErpProductionMaterialDO> materials = erpProductionMaterialMapper.selectListByProductionOrderId(productionOrderId);
        Map<Long, ErpProductRespVO> productMap = productService.getProductVOMap(
                convertSet(materials, ErpProductionMaterialDO::getMaterialId));
        Map<Long, ErpWarehouseDO> warehouseMap = warehouseService.getWarehouseMap(
                convertSet(materials, ErpProductionMaterialDO::getSupplyWarehouseId));
        return materials.stream().map(material -> {
            ErpProductionMaterialRespVO respVO = BeanUtils.toBean(material, ErpProductionMaterialRespVO.class);
            respVO.setNetIssuedQty(calculateNetIssuedQty(material));
            respVO.setRemainingIssueQty(calculateRemainingIssueQty(material));
            MapUtils.findAndThen(productMap, material.getMaterialId(), product -> respVO.setMaterialName(product.getName())
                    .setMaterialCode(product.getMaterialCode())
                    .setMaterialBarCode(product.getBarCode())
                    .setUnitName(product.getUnitName())
                    .setBatchControlFlag(product.getBatchControlFlag()));
            MapUtils.findAndThen(warehouseMap, material.getSupplyWarehouseId(),
                    warehouse -> respVO.setSupplyWarehouseName(warehouse.getName()));
            return respVO;
        }).toList();
    }

    @Override
    public ErpProductionMaterialBatchCandidatesRespVO getBatchCandidates(Long productionMaterialId, Long warehouseId) {
        ErpProductionMaterialDO material = validateProductionMaterial(productionMaterialId);
        List<ErpStockBatchDO> batchList = stockBatchService.getAvailableStockBatchList(material.getMaterialId(), warehouseId);
        ErpProductionMaterialBatchCandidatesRespVO respVO = new ErpProductionMaterialBatchCandidatesRespVO();
        respVO.setProductionMaterialId(material.getId());
        respVO.setMaterialId(material.getMaterialId());
        respVO.setWarehouseId(warehouseId);
        respVO.setRequiredQty(material.getRequiredQty());
        respVO.setIssuedQty(material.getIssuedQty());
        respVO.setReturnedQty(material.getReturnedQty());
        respVO.setRemainingQty(calculateRemainingIssueQty(material));
        respVO.setBatchCandidates(batchList.stream().map(batch -> {
            ErpProductionMaterialBatchCandidatesRespVO.BatchCandidate item =
                    BeanUtils.toBean(batch, ErpProductionMaterialBatchCandidatesRespVO.BatchCandidate.class);
            item.setStockBatchId(batch.getId());
            return item;
        }).toList());
        return respVO;
    }

    static BigDecimal calculateNetIssuedQty(ErpProductionMaterialDO material) {
        BigDecimal issuedQty = ObjectUtil.defaultIfNull(material.getIssuedQty(), BigDecimal.ZERO);
        BigDecimal returnedQty = ObjectUtil.defaultIfNull(material.getReturnedQty(), BigDecimal.ZERO);
        return issuedQty.subtract(returnedQty);
    }

    static BigDecimal calculateRemainingIssueQty(ErpProductionMaterialDO material) {
        BigDecimal remainingQty = ObjectUtil.defaultIfNull(material.getRequiredQty(), BigDecimal.ZERO)
                .subtract(calculateNetIssuedQty(material));
        return remainingQty.compareTo(BigDecimal.ZERO) > 0 ? remainingQty : BigDecimal.ZERO;
    }

}
