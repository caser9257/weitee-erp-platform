package cn.weitee.erp.module.erp.service.stock;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.stock.vo.record.ErpStockRecordPageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockRecordDO;
import cn.weitee.erp.module.erp.service.stock.bo.ErpStockRecordCreateReqBO;

import jakarta.validation.Valid;

/**
 * ERP 产品库存明细 Service 接口
 *
 * @author WeTai
 */
public interface ErpStockRecordService {

    /**
     * 获得产品库存明细
     *
     * @param id 编号
     * @return 产品库存明细
     */
    ErpStockRecordDO getStockRecord(Long id);

    /**
     * 获得产品库存明细分页
     *
     * @param pageReqVO 分页查询
     * @return 产品库存明细分页
     */
    PageResult<ErpStockRecordDO> getStockRecordPage(ErpStockRecordPageReqVO pageReqVO);

    /**
     * 创建库存明细
     *
     * @param createReqBO 创建库存明细 BO
     */
    void createStockRecord(@Valid ErpStockRecordCreateReqBO createReqBO);

    /**
     * 判断指定业务单据的库存明细是否已存在（用于库存变更幂等防重）
     *
     * @param bizType   业务类型
     * @param bizId     业务单据编号
     * @param bizItemId 业务明细编号
     * @return 是否已存在
     */
    boolean hasStockRecord(Integer bizType, Long bizId, Long bizItemId);

}