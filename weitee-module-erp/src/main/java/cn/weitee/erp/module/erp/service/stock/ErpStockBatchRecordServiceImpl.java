package cn.weitee.erp.module.erp.service.stock;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.stock.vo.batch.ErpStockBatchRecordPageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockBatchRecordDO;
import cn.weitee.erp.module.erp.dal.mysql.stock.ErpStockBatchRecordMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;

@Service
@Validated
public class ErpStockBatchRecordServiceImpl implements ErpStockBatchRecordService {

    @Resource
    private ErpStockBatchRecordMapper erpStockBatchRecordMapper;

    @Override
    public void createStockBatchRecord(ErpStockBatchRecordDO record) {
        erpStockBatchRecordMapper.insert(record);
    }

    @Override
    public PageResult<ErpStockBatchRecordDO> getStockBatchRecordPage(ErpStockBatchRecordPageReqVO reqVO) {
        return erpStockBatchRecordMapper.selectPage(reqVO);
    }

}
