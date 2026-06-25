package cn.weitee.erp.module.erp.dal.mysql.mrp;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.production.ErpProductionOrderPageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionOrderDO;
import cn.hutool.core.collection.CollUtil;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Collection;
import java.util.Collections;

@Mapper
public interface ErpProductionOrderMapper extends BaseMapperX<ErpProductionOrderDO> {

    default PageResult<ErpProductionOrderDO> selectPage(ErpProductionOrderPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ErpProductionOrderDO>()
                .likeIfPresent(ErpProductionOrderDO::getOrderNo, reqVO.getOrderNo())
                .eqIfPresent(ErpProductionOrderDO::getProductId, reqVO.getProductId())
                .eqIfPresent(ErpProductionOrderDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(ErpProductionOrderDO::getPlanStartTime, reqVO.getPlanTime())
                .orderByDesc(ErpProductionOrderDO::getId));
    }

    default ErpProductionOrderDO selectByOrderNo(String orderNo) {
        return selectOne(ErpProductionOrderDO::getOrderNo, orderNo);
    }

    default List<ErpProductionOrderDO> selectListByIds(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<ErpProductionOrderDO>().in(ErpProductionOrderDO::getId, ids));
    }

    default List<ErpProductionOrderDO> selectListForWip() {
        return selectList(new LambdaQueryWrapperX<ErpProductionOrderDO>()
                .in(ErpProductionOrderDO::getStatus, 0, 10));
    }

}
