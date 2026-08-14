package cn.weitee.erp.module.erp.dal.mysql.mrp;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.returning.ErpProductionReturnPageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionReturnDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ErpProductionReturnMapper extends BaseMapperX<ErpProductionReturnDO> {

    default PageResult<ErpProductionReturnDO> selectPage(ErpProductionReturnPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ErpProductionReturnDO>()
                .likeIfPresent(ErpProductionReturnDO::getReturnNo, reqVO.getReturnNo())
                .eqIfPresent(ErpProductionReturnDO::getProductionOrderId, reqVO.getProductionOrderId())
                .eqIfPresent(ErpProductionReturnDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(ErpProductionReturnDO::getReturnTime, reqVO.getReturnTime())
                .orderByDesc(ErpProductionReturnDO::getId));
    }
}
