package cn.weitee.erp.module.erp.dal.mysql.stock;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.erp.controller.admin.stock.vo.record.ErpStockRecordPageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockRecordDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * ERP 产品库存明细 Mapper
 *
 * @author WeTai
 */
@Mapper
public interface ErpStockRecordMapper extends BaseMapperX<ErpStockRecordDO> {

    default PageResult<ErpStockRecordDO> selectPage(ErpStockRecordPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ErpStockRecordDO>()
                .eqIfPresent(ErpStockRecordDO::getProductId, reqVO.getProductId())
                .eqIfPresent(ErpStockRecordDO::getWarehouseId, reqVO.getWarehouseId())
                .eqIfPresent(ErpStockRecordDO::getBizType, reqVO.getBizType())
                .likeIfPresent(ErpStockRecordDO::getBizNo, reqVO.getBizNo())
                .betweenIfPresent(ErpStockRecordDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ErpStockRecordDO::getId));
    }

    default ErpStockRecordDO selectFirstByBiz(Integer bizType, Long bizId, Long bizItemId) {
        return selectOne(new LambdaQueryWrapperX<ErpStockRecordDO>()
                .eq(ErpStockRecordDO::getBizType, bizType)
                .eq(ErpStockRecordDO::getBizId, bizId)
                .eq(ErpStockRecordDO::getBizItemId, bizItemId)
                .last("LIMIT 1"));
    }

}