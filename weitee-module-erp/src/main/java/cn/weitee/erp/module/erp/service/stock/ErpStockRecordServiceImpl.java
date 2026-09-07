package cn.weitee.erp.module.erp.service.stock;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.module.erp.controller.admin.stock.vo.record.ErpStockRecordPageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockRecordDO;
import cn.weitee.erp.module.erp.dal.mysql.stock.ErpStockRecordMapper;
import cn.weitee.erp.module.erp.service.stock.bo.ErpStockRecordCreateReqBO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.math.BigDecimal;

/**
 * ERP 产品库存明细 Service 实现类
 *
 * @author WeTai
 */
@Service
@Validated
public class ErpStockRecordServiceImpl implements ErpStockRecordService {

    @Resource
    private ErpStockRecordMapper erpStockRecordMapper;

    @Resource
    private ErpStockService stockService;

    @Override
    public ErpStockRecordDO getStockRecord(Long id) {
        return erpStockRecordMapper.selectById(id);
    }

    @Override
    public PageResult<ErpStockRecordDO> getStockRecordPage(ErpStockRecordPageReqVO pageReqVO) {
        return erpStockRecordMapper.selectPage(pageReqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createStockRecord(ErpStockRecordCreateReqBO createReqBO) {
        // 1. 更新库存：入库单价必须随流水一起进入库存成本计算（BO 契约），
        //    否则 erp_stock.average_cost/total_cost 永不更新，出库成本、盘点差异和对账快照全部失真
        BigDecimal totalCount = stockService.updateStockCountIncrement(
                createReqBO.getProductId(), createReqBO.getWarehouseId(), createReqBO.getCount(),
                createReqBO.getPrice());
        // 2. 创建库存明细
        ErpStockRecordDO stockRecord = BeanUtils.toBean(createReqBO, ErpStockRecordDO.class)
                .setTotalCount(totalCount);
        erpStockRecordMapper.insert(stockRecord);
    }

    @Override
    public boolean hasStockRecord(Integer bizType, Long bizId, Long bizItemId) {
        return erpStockRecordMapper.selectFirstByBiz(bizType, bizId, bizItemId) != null;
    }

}