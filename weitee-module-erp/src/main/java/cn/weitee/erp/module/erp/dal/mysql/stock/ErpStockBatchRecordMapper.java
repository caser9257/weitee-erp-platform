package cn.weitee.erp.module.erp.dal.mysql.stock;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.erp.controller.admin.stock.vo.batch.ErpStockBatchRecordPageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockBatchRecordDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ErpStockBatchRecordMapper extends BaseMapperX<ErpStockBatchRecordDO> {

    default PageResult<ErpStockBatchRecordDO> selectPage(ErpStockBatchRecordPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ErpStockBatchRecordDO>()
                .eqIfPresent(ErpStockBatchRecordDO::getStockBatchId, reqVO.getStockBatchId())
                .eqIfPresent(ErpStockBatchRecordDO::getProductId, reqVO.getProductId())
                .eqIfPresent(ErpStockBatchRecordDO::getWarehouseId, reqVO.getWarehouseId())
                .eqIfPresent(ErpStockBatchRecordDO::getBizType, reqVO.getBizType())
                .likeIfPresent(ErpStockBatchRecordDO::getBizNo, reqVO.getBizNo())
                .betweenIfPresent(ErpStockBatchRecordDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ErpStockBatchRecordDO::getId));
    }

}
