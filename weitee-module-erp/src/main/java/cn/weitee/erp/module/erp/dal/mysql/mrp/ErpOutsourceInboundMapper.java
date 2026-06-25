package cn.weitee.erp.module.erp.dal.mysql.mrp;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.outsource.ErpOutsourceInboundPageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpOutsourceInboundDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ErpOutsourceInboundMapper extends BaseMapperX<ErpOutsourceInboundDO> {
    default PageResult<ErpOutsourceInboundDO> selectPage(ErpOutsourceInboundPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ErpOutsourceInboundDO>()
                .likeIfPresent(ErpOutsourceInboundDO::getInboundNo, reqVO.getInboundNo())
                .eqIfPresent(ErpOutsourceInboundDO::getOrderId, reqVO.getOrderId())
                .eqIfPresent(ErpOutsourceInboundDO::getStatus, reqVO.getStatus())
                .orderByDesc(ErpOutsourceInboundDO::getId));
    }
    default List<ErpOutsourceInboundDO> selectListByOrderId(Long orderId) {
        return selectList(ErpOutsourceInboundDO::getOrderId, orderId);
    }
}
