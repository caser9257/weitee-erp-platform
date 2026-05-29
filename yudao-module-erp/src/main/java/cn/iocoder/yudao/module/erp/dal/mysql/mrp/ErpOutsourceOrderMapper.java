package cn.iocoder.yudao.module.erp.dal.mysql.mrp;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.outsource.ErpOutsourceOrderPageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpOutsourceOrderDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ErpOutsourceOrderMapper extends BaseMapperX<ErpOutsourceOrderDO> {
    default PageResult<ErpOutsourceOrderDO> selectPage(ErpOutsourceOrderPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ErpOutsourceOrderDO>()
                .likeIfPresent(ErpOutsourceOrderDO::getNo, reqVO.getNo())
                .eqIfPresent(ErpOutsourceOrderDO::getOrderType, reqVO.getOrderType())
                .eqIfPresent(ErpOutsourceOrderDO::getSupplierId, reqVO.getSupplierId())
                .eqIfPresent(ErpOutsourceOrderDO::getProductId, reqVO.getProductId())
                .eqIfPresent(ErpOutsourceOrderDO::getStatus, reqVO.getStatus())
                .orderByDesc(ErpOutsourceOrderDO::getId));
    }
    default ErpOutsourceOrderDO selectByNo(String no) {
        return selectOne(ErpOutsourceOrderDO::getNo, no);
    }
}
