package cn.iocoder.yudao.module.erp.dal.mysql.mrp;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.bom.ErpBomPageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpBomDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;

@Mapper
public interface ErpBomMapper extends BaseMapperX<ErpBomDO> {

    default PageResult<ErpBomDO> selectPage(ErpBomPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ErpBomDO>()
                .eqIfPresent(ErpBomDO::getProductId, reqVO.getProductId())
                .eqIfPresent(ErpBomDO::getStatus, reqVO.getStatus())
                .likeIfPresent(ErpBomDO::getBomCode, reqVO.getBomCode())
                .orderByDesc(ErpBomDO::getId));
    }

    default ErpBomDO selectEffectiveByProductId(Long productId) {
        return selectOne(new LambdaQueryWrapperX<ErpBomDO>()
                .eq(ErpBomDO::getProductId, productId)
                .eq(ErpBomDO::getStatus, 1)
                .orderByDesc(ErpBomDO::getId)
                .last("LIMIT 1"));
    }

    default java.util.List<ErpBomDO> selectListByProductIds(Collection<Long> productIds) {
        return selectList(new LambdaQueryWrapperX<ErpBomDO>()
                .in(ErpBomDO::getProductId, productIds)
                .eq(ErpBomDO::getStatus, 1)
                .orderByDesc(ErpBomDO::getId));
    }

}
