package cn.iocoder.yudao.module.erp.dal.mysql.mrp;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.inbound.ErpProductionInboundPageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionInboundDO;
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
