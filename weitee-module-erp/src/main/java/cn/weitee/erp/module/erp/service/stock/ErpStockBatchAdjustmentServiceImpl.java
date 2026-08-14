package cn.weitee.erp.module.erp.service.stock;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.stock.vo.batch.ErpStockBatchAdjustReqVO;
import cn.weitee.erp.module.erp.controller.admin.stock.vo.batch.ErpStockBatchAdjustmentPageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockBatchAdjustmentDO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockBatchDO;
import cn.weitee.erp.module.erp.dal.mysql.stock.ErpStockBatchAdjustmentMapper;
import cn.weitee.erp.module.erp.enums.stock.ErpStockBatchAdjustTypeEnum;
import cn.hutool.core.util.ObjectUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;

import java.math.BigDecimal;

@Service
@Validated
public class ErpStockBatchAdjustmentServiceImpl implements ErpStockBatchAdjustmentService {

    @Resource
    private ErpStockBatchAdjustmentMapper erpStockBatchAdjustmentMapper;
    @Resource
    private ErpStockBatchService stockBatchService;
    @Resource
    private ErpStockService stockService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ErpStockBatchAdjustmentDO adjustBatch(ErpStockBatchAdjustReqVO reqVO) {
        ErpStockBatchDO stockBatch = stockBatchService.adjustBatch(reqVO);
        BigDecimal stockChange = ObjectUtil.equal(reqVO.getAdjustType(), ErpStockBatchAdjustTypeEnum.INCREASE.getType())
                ? reqVO.getCount() : reqVO.getCount().negate();
        stockService.updateStockCountIncrement(stockBatch.getProductId(), stockBatch.getWarehouseId(), stockChange);
        return erpStockBatchAdjustmentMapper.selectByAdjustNo(reqVO.getAdjustNo());
    }

    @Override
    public PageResult<ErpStockBatchAdjustmentDO> getStockBatchAdjustmentPage(ErpStockBatchAdjustmentPageReqVO reqVO) {
        return erpStockBatchAdjustmentMapper.selectPage(reqVO);
    }

}
