package cn.iocoder.yudao.module.erp.dal.mysql.purchase;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.sourcebatch.ErpPurchaseSourceBatchPageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseSourceBatchDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ErpPurchaseSourceBatchMapper extends BaseMapperX<ErpPurchaseSourceBatchDO> {

    default PageResult<ErpPurchaseSourceBatchDO> selectPage(ErpPurchaseSourceBatchPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ErpPurchaseSourceBatchDO>()
                .likeIfPresent(ErpPurchaseSourceBatchDO::getBatchNo, reqVO.getBatchNo())
                .eqIfPresent(ErpPurchaseSourceBatchDO::getProductId, reqVO.getProductId())
                .eqIfPresent(ErpPurchaseSourceBatchDO::getPurchaseOrderId, reqVO.getPurchaseOrderId())
                .eqIfPresent(ErpPurchaseSourceBatchDO::getPurchaseOrderItemId, reqVO.getPurchaseOrderItemId())
                .eqIfPresent(ErpPurchaseSourceBatchDO::getSupplierId, reqVO.getSupplierId())
                .eqIfPresent(ErpPurchaseSourceBatchDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(ErpPurchaseSourceBatchDO::getBizDate, reqVO.getBizDate())
                .orderByDesc(ErpPurchaseSourceBatchDO::getBizDate)
                .orderByDesc(ErpPurchaseSourceBatchDO::getId));
    }

    default ErpPurchaseSourceBatchDO selectByBatchNo(String batchNo) {
        return selectOne(ErpPurchaseSourceBatchDO::getBatchNo, batchNo);
    }
}
