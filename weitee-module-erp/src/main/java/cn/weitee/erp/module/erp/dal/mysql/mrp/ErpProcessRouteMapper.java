package cn.weitee.erp.module.erp.dal.mysql.mrp;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.route.ErpProcessRoutePageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProcessRouteDO;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface ErpProcessRouteMapper extends BaseMapperX<ErpProcessRouteDO> {

    default PageResult<ErpProcessRouteDO> selectPage(ErpProcessRoutePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ErpProcessRouteDO>()
                .likeIfPresent(ErpProcessRouteDO::getRouteCode, reqVO.getRouteCode())
                .likeIfPresent(ErpProcessRouteDO::getRouteName, reqVO.getRouteName())
                .eqIfPresent(ErpProcessRouteDO::getProductId, reqVO.getProductId())
                .eqIfPresent(ErpProcessRouteDO::getStatus, reqVO.getStatus())
                .orderByDesc(ErpProcessRouteDO::getId));
    }

    default ErpProcessRouteDO selectByRouteCode(String routeCode) {
        return selectOne(ErpProcessRouteDO::getRouteCode, routeCode);
    }

    default List<ErpProcessRouteDO> selectListByProductIds(Collection<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return List.of();
        }
        return selectList(Wrappers.<ErpProcessRouteDO>lambdaQuery()
                .in(ErpProcessRouteDO::getProductId, productIds));
    }
}
