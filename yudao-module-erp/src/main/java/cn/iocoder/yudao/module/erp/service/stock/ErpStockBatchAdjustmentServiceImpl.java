package cn.iocoder.yudao.module.erp.service.stock;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.stock.vo.batch.ErpStockBatchAdjustReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.stock.vo.batch.ErpStockBatchAdjustmentPageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpStockBatchAdjustmentDO;
import cn.iocoder.yudao.module.erp.dal.mysql.stock.ErpStockBatchAdjustmentMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;

@Service
@Validated
public class ErpStockBatchAdjustmentServiceImpl implements ErpStockBatchAdjustmentService {

    @Resource
    private ErpStockBatchAdjustmentMapper erpStockBatchAdjustmentMapper;
    @Resource
    private ErpStockBatchService stockBatchService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ErpStockBatchAdjustmentDO adjustBatch(ErpStockBatchAdjustReqVO reqVO) {
        stockBatchService.adjustBatch(reqVO);
        return erpStockBatchAdjustmentMapper.selectByAdjustNo(reqVO.getAdjustNo());
    }

    @Override
    public PageResult<ErpStockBatchAdjustmentDO> getStockBatchAdjustmentPage(ErpStockBatchAdjustmentPageReqVO reqVO) {
        return erpStockBatchAdjustmentMapper.selectPage(reqVO);
    }

}
