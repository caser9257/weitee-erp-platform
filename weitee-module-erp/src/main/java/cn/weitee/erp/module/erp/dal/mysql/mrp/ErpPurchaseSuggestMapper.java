package cn.weitee.erp.module.erp.dal.mysql.mrp;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.suggest.ErpPurchaseSuggestPageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpPurchaseSuggestDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface ErpPurchaseSuggestMapper extends BaseMapperX<ErpPurchaseSuggestDO> {

    default void deleteByPlanId(Long planId) {
        delete(ErpPurchaseSuggestDO::getPlanId, planId);
    }

    default PageResult<ErpPurchaseSuggestDO> selectPage(ErpPurchaseSuggestPageReqVO reqVO) {
        return selectPage(reqVO, buildPageQuery(reqVO));
    }

    static LambdaQueryWrapperX<ErpPurchaseSuggestDO> buildPageQuery(ErpPurchaseSuggestPageReqVO reqVO) {
        return new LambdaQueryWrapperX<ErpPurchaseSuggestDO>()
                .eqIfPresent(ErpPurchaseSuggestDO::getPlanId, reqVO.getPlanId())
                .eqIfPresent(ErpPurchaseSuggestDO::getMaterialId, reqVO.getMaterialId())
                .eqIfPresent(ErpPurchaseSuggestDO::getSourceOrderId, reqVO.getSourceOrderId())
                .eqIfPresent(ErpPurchaseSuggestDO::getStatus, reqVO.getStatus())
                .orderByDesc(ErpPurchaseSuggestDO::getId);
    }

    default List<ErpPurchaseSuggestDO> selectListByIds(Collection<Long> ids) {
        return selectBatchIds(ids);
    }

    default List<ErpPurchaseSuggestDO> selectListByConvertPurchaseOrderIds(Collection<Long> convertPurchaseOrderIds) {
        return selectList(ErpPurchaseSuggestDO::getConvertPurchaseOrderId, convertPurchaseOrderIds);
    }

    default int updateStatusByIdsAndStatus(Collection<Long> ids, Integer status, ErpPurchaseSuggestDO updateObj) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        return update(updateObj, new LambdaUpdateWrapper<ErpPurchaseSuggestDO>()
                .in(ErpPurchaseSuggestDO::getId, ids)
                .eq(ErpPurchaseSuggestDO::getStatus, status));
    }

    default List<ErpPurchaseSuggestDO> selectListBySourceOrderIds(Collection<Long> sourceOrderIds) {
        if (sourceOrderIds == null || sourceOrderIds.isEmpty()) {
            return List.of();
        }
        return selectList(new LambdaQueryWrapperX<ErpPurchaseSuggestDO>()
                .in(ErpPurchaseSuggestDO::getSourceOrderId, sourceOrderIds)
                .orderByAsc(ErpPurchaseSuggestDO::getId));
    }

    default Long selectCountByPlanIdAndProjectId(Long planId, Long projectId) {
        return selectCount(new LambdaQueryWrapperX<ErpPurchaseSuggestDO>()
                .eq(ErpPurchaseSuggestDO::getPlanId, planId)
                .eq(ErpPurchaseSuggestDO::getProjectId, projectId));
    }

    default Long selectCountByProjectIdAndStatus(Long projectId, Integer status) {
        return selectCount(new LambdaQueryWrapperX<ErpPurchaseSuggestDO>()
                .eq(ErpPurchaseSuggestDO::getProjectId, projectId)
                .eq(ErpPurchaseSuggestDO::getStatus, status));
    }

}
