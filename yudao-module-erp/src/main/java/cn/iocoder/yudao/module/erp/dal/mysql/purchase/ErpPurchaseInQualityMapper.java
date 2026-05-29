package cn.iocoder.yudao.module.erp.dal.mysql.purchase;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.inquality.ErpPurchaseInQualityPageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseInQualityDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Mapper
public interface ErpPurchaseInQualityMapper extends BaseMapperX<ErpPurchaseInQualityDO> {

    default PageResult<ErpPurchaseInQualityDO> selectPage(ErpPurchaseInQualityPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ErpPurchaseInQualityDO>()
                .likeIfPresent(ErpPurchaseInQualityDO::getNo, reqVO.getNo())
                .likeIfPresent(ErpPurchaseInQualityDO::getPurchaseInNo, reqVO.getPurchaseInNo())
                .eqIfPresent(ErpPurchaseInQualityDO::getStatus, reqVO.getStatus())
                .eqIfPresent(ErpPurchaseInQualityDO::getResult, reqVO.getResult())
                .eqIfPresent(ErpPurchaseInQualityDO::getAssignedCheckerUserId, reqVO.getAssignedCheckerUserId())
                .eqIfPresent(ErpPurchaseInQualityDO::getCheckerUserId, reqVO.getCheckerUserId())
                .betweenIfPresent(ErpPurchaseInQualityDO::getCheckTime, reqVO.getCheckTime())
                .orderByDesc(ErpPurchaseInQualityDO::getCreateTime)
                .orderByDesc(ErpPurchaseInQualityDO::getId));
    }

    default ErpPurchaseInQualityDO selectByPurchaseInId(Long purchaseInId) {
        return selectOne(new LambdaQueryWrapperX<ErpPurchaseInQualityDO>()
                .eq(ErpPurchaseInQualityDO::getPurchaseInId, purchaseInId)
                .ne(ErpPurchaseInQualityDO::getStatus, 60)
                .orderByDesc(ErpPurchaseInQualityDO::getId)
                .last("LIMIT 1"));
    }

    default List<ErpPurchaseInQualityDO> selectListByPurchaseInIds(Collection<Long> purchaseInIds) {
        if (purchaseInIds == null || purchaseInIds.isEmpty()) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<ErpPurchaseInQualityDO>()
                .in(ErpPurchaseInQualityDO::getPurchaseInId, purchaseInIds)
                .ne(ErpPurchaseInQualityDO::getStatus, 60)
                .orderByDesc(ErpPurchaseInQualityDO::getId));
    }

}
