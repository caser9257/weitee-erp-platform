package cn.iocoder.yudao.module.erp.dal.mysql.mrp;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.quality.ErpProductionFinishQualityPageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionFinishQualityDO;
import cn.iocoder.yudao.module.erp.enums.ErpQaStatusEnum;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

@Mapper
public interface ErpProductionFinishQualityMapper extends BaseMapperX<ErpProductionFinishQualityDO> {

    default ErpProductionFinishQualityDO selectByProductionOrderId(Long productionOrderId) {
        return selectOne(ErpProductionFinishQualityDO::getProductionOrderId, productionOrderId);
    }

    default PageResult<ErpProductionFinishQualityDO> selectPage(ErpProductionFinishQualityPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ErpProductionFinishQualityDO>()
                .likeIfPresent(ErpProductionFinishQualityDO::getNo, reqVO.getNo())
                .eqIfPresent(ErpProductionFinishQualityDO::getProductionOrderId, reqVO.getProductionOrderId())
                .eqIfPresent(ErpProductionFinishQualityDO::getSourceOrderId, reqVO.getSourceOrderId())
                .eqIfPresent(ErpProductionFinishQualityDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(ErpProductionFinishQualityDO::getCheckTime, reqVO.getCheckTime())
                .orderByDesc(ErpProductionFinishQualityDO::getId));
    }

    @Select("""
            SELECT COALESCE(SUM(qualified_qty), 0)
            FROM erp_production_finish_quality
            WHERE deleted = 0
              AND source_order_id = #{sourceOrderId}
              AND status IN (#{partialStatus}, #{passedStatus})
            """)
    BigDecimal selectQualifiedQtyBySourceOrderId(@Param("sourceOrderId") Long sourceOrderId,
                                                 @Param("partialStatus") Integer partialStatus,
                                                 @Param("passedStatus") Integer passedStatus);

    default BigDecimal sumQualifiedQtyBySourceOrderId(Long sourceOrderId) {
        return selectQualifiedQtyBySourceOrderId(sourceOrderId,
                ErpQaStatusEnum.PARTIAL.getStatus(), ErpQaStatusEnum.PASSED.getStatus());
    }

    default List<ErpProductionFinishQualityDO> selectListBySourceOrderId(Long sourceOrderId) {
        return selectList(new LambdaQueryWrapperX<ErpProductionFinishQualityDO>()
                .eq(ErpProductionFinishQualityDO::getSourceOrderId, sourceOrderId)
                .orderByDesc(ErpProductionFinishQualityDO::getId));
    }

    default List<ErpProductionFinishQualityDO> selectListBySourceOrderIds(Collection<Long> sourceOrderIds) {
        if (sourceOrderIds == null || sourceOrderIds.isEmpty()) {
            return List.of();
        }
        return selectList(new LambdaQueryWrapperX<ErpProductionFinishQualityDO>()
                .in(ErpProductionFinishQualityDO::getSourceOrderId, sourceOrderIds)
                .orderByDesc(ErpProductionFinishQualityDO::getId));
    }

}
