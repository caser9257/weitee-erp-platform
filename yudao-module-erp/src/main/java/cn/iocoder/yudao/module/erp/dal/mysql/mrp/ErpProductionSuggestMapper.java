package cn.iocoder.yudao.module.erp.dal.mysql.mrp;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.suggest.ErpProductionSuggestPageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionSuggestDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface ErpProductionSuggestMapper extends BaseMapperX<ErpProductionSuggestDO> {

    default void deleteByPlanId(Long planId) {
        delete(ErpProductionSuggestDO::getPlanId, planId);
    }

    default PageResult<ErpProductionSuggestDO> selectPage(ErpProductionSuggestPageReqVO reqVO) {
        return selectPage(reqVO, buildPageQuery(reqVO));
    }

    static LambdaQueryWrapperX<ErpProductionSuggestDO> buildPageQuery(ErpProductionSuggestPageReqVO reqVO) {
        return new LambdaQueryWrapperX<ErpProductionSuggestDO>()
                .eqIfPresent(ErpProductionSuggestDO::getPlanId, reqVO.getPlanId())
                .eqIfPresent(ErpProductionSuggestDO::getProductId, reqVO.getProductId())
                .eqIfPresent(ErpProductionSuggestDO::getSourceOrderId, reqVO.getSourceOrderId())
                .eqIfPresent(ErpProductionSuggestDO::getStatus, reqVO.getStatus())
                .orderByDesc(ErpProductionSuggestDO::getId);
    }

    default List<ErpProductionSuggestDO> selectListByIds(Collection<Long> ids) {
        return selectBatchIds(ids);
    }

    default int updateStatusByIdsAndStatus(Collection<Long> ids, Integer status, ErpProductionSuggestDO updateObj) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        return update(updateObj, new LambdaUpdateWrapper<ErpProductionSuggestDO>()
                .in(ErpProductionSuggestDO::getId, ids)
                .eq(ErpProductionSuggestDO::getStatus, status));
    }

    default List<ErpProductionSuggestDO> selectListBySourceOrderIds(Collection<Long> sourceOrderIds) {
        if (sourceOrderIds == null || sourceOrderIds.isEmpty()) {
            return List.of();
        }
        return selectList(new LambdaQueryWrapperX<ErpProductionSuggestDO>()
                .in(ErpProductionSuggestDO::getSourceOrderId, sourceOrderIds)
                .orderByAsc(ErpProductionSuggestDO::getId));
    }

    default Long selectCountByPlanIdAndProjectId(Long planId, Long projectId) {
        return selectCount(new LambdaQueryWrapperX<ErpProductionSuggestDO>()
                .eq(ErpProductionSuggestDO::getPlanId, planId)
                .eq(ErpProductionSuggestDO::getProjectId, projectId));
    }

    default Long selectCountByProjectIdAndStatus(Long projectId, Integer status) {
        return selectCount(new LambdaQueryWrapperX<ErpProductionSuggestDO>()
                .eq(ErpProductionSuggestDO::getProjectId, projectId)
                .eq(ErpProductionSuggestDO::getStatus, status));
    }

}
