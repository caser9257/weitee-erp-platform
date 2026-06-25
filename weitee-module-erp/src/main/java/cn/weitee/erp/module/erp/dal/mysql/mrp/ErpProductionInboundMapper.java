package cn.weitee.erp.module.erp.dal.mysql.mrp;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.inbound.ErpProductionInboundPageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionInboundDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ErpProductionInboundMapper extends BaseMapperX<ErpProductionInboundDO> {

    default ErpProductionInboundDO selectByNo(String no) {
        return selectOne(ErpProductionInboundDO::getNo, no);
    }

    default ErpProductionInboundDO selectByFinishQualityId(Long finishQualityId) {
        return selectOne(ErpProductionInboundDO::getFinishQualityId, finishQualityId);
    }

    default PageResult<ErpProductionInboundDO> selectPage(ErpProductionInboundPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ErpProductionInboundDO>()
                .likeIfPresent(ErpProductionInboundDO::getNo, reqVO.getNo())
                .eqIfPresent(ErpProductionInboundDO::getFinishQualityId, reqVO.getFinishQualityId())
                .eqIfPresent(ErpProductionInboundDO::getProductionOrderId, reqVO.getProductionOrderId())
                .eqIfPresent(ErpProductionInboundDO::getStatus, reqVO.getStatus())
                .orderByDesc(ErpProductionInboundDO::getId));
    }

}
