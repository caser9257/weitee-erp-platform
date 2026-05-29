package cn.iocoder.yudao.module.erp.dal.mysql.mrp;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.outsource.ErpOutsourceInboundPageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpOutsourceInboundDO;
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
