package cn.iocoder.yudao.module.erp.dal.mysql.stock;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.stock.vo.batch.ErpStockBatchAdjustmentPageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpStockBatchAdjustmentDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ErpStockBatchAdjustmentMapper extends BaseMapperX<ErpStockBatchAdjustmentDO> {

    default ErpStockBatchAdjustmentDO selectByAdjustNo(String adjustNo) {
        return selectOne(ErpStockBatchAdjustmentDO::getAdjustNo, adjustNo);
    }

    default PageResult<ErpStockBatchAdjustmentDO> selectPage(ErpStockBatchAdjustmentPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ErpStockBatchAdjustmentDO>()
                .likeIfPresent(ErpStockBatchAdjustmentDO::getAdjustNo, reqVO.getAdjustNo())
                .eqIfPresent(ErpStockBatchAdjustmentDO::getStockBatchId, reqVO.getStockBatchId())
                .eqIfPresent(ErpStockBatchAdjustmentDO::getProductId, reqVO.getProductId())
                .eqIfPresent(ErpStockBatchAdjustmentDO::getWarehouseId, reqVO.getWarehouseId())
                .eqIfPresent(ErpStockBatchAdjustmentDO::getAdjustType, reqVO.getAdjustType())
                .betweenIfPresent(ErpStockBatchAdjustmentDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ErpStockBatchAdjustmentDO::getId));
    }

}
