package cn.weitee.erp.module.erp.dal.mysql.mrp;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.outsource.ErpOutsourceFeePageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpOutsourceFeeDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ErpOutsourceFeeMapper extends BaseMapperX<ErpOutsourceFeeDO> {
    default PageResult<ErpOutsourceFeeDO> selectPage(ErpOutsourceFeePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ErpOutsourceFeeDO>()
                .likeIfPresent(ErpOutsourceFeeDO::getFeeNo, reqVO.getFeeNo())
                .eqIfPresent(ErpOutsourceFeeDO::getOrderId, reqVO.getOrderId())
                .eqIfPresent(ErpOutsourceFeeDO::getStatus, reqVO.getStatus())
                .orderByDesc(ErpOutsourceFeeDO::getId));
    }
    default List<ErpOutsourceFeeDO> selectListByOrderId(Long orderId) {
        return selectList(ErpOutsourceFeeDO::getOrderId, orderId);
    }
}
