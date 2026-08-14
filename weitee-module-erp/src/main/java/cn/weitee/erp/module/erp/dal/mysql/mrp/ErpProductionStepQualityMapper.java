package cn.weitee.erp.module.erp.dal.mysql.mrp;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.quality.ErpProductionStepQualityPageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionStepQualityDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ErpProductionStepQualityMapper extends BaseMapperX<ErpProductionStepQualityDO> {

    default PageResult<ErpProductionStepQualityDO> selectPage(ErpProductionStepQualityPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ErpProductionStepQualityDO>()
                .likeIfPresent(ErpProductionStepQualityDO::getNo, reqVO.getNo())
                .eqIfPresent(ErpProductionStepQualityDO::getProductionOrderId, reqVO.getProductionOrderId())
                .eqIfPresent(ErpProductionStepQualityDO::getProductionOrderStepId, reqVO.getProductionOrderStepId())
                .eqIfPresent(ErpProductionStepQualityDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(ErpProductionStepQualityDO::getCheckTime, reqVO.getCheckTime())
                .orderByDesc(ErpProductionStepQualityDO::getId));
    }

    default long selectPendingCount(Long productionOrderStepId) {
        return selectCount(new LambdaQueryWrapperX<ErpProductionStepQualityDO>()
                .eq(ErpProductionStepQualityDO::getProductionOrderStepId, productionOrderStepId)
                .eq(ErpProductionStepQualityDO::getStatus, cn.weitee.erp.module.erp.enums.ErpQaStatusEnum.TO_INSPECT.getStatus()));
    }
}
