package cn.weitee.erp.module.erp.dal.mysql.mrp;

import cn.hutool.core.collection.CollUtil;
import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionOrderStepDO;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Mapper
public interface ErpProductionOrderStepMapper extends BaseMapperX<ErpProductionOrderStepDO> {

    default List<ErpProductionOrderStepDO> selectListByOrderId(Long productionOrderId) {
        return selectList(Wrappers.<ErpProductionOrderStepDO>lambdaQuery()
                .eq(ErpProductionOrderStepDO::getProductionOrderId, productionOrderId)
                .orderByAsc(ErpProductionOrderStepDO::getStepNo));
    }

    default List<ErpProductionOrderStepDO> selectListByIds(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return selectList(Wrappers.<ErpProductionOrderStepDO>lambdaQuery()
                .in(ErpProductionOrderStepDO::getId, ids));
    }

    /**
     * 报工数量 CAS 累加：仅当工序处于进行中且累计数量未超计划数量时更新，返回影响行数。
     */
    int updateStepQtyByCas(@Param("id") Long id,
                           @Param("reportedQty") BigDecimal reportedQty,
                           @Param("qualifiedQty") BigDecimal qualifiedQty,
                           @Param("scrapQty") BigDecimal scrapQty);

    /**
     * 工序状态 CAS 流转：仅当当前状态匹配期望值时更新，返回影响行数。
     */
    @org.apache.ibatis.annotations.Update("UPDATE erp_production_order_step SET step_status = #{targetStatus} " +
            "WHERE id = #{id} AND deleted = 0 AND step_status = #{expectStatus}")
    int updateStepStatusByCas(@Param("id") Long id,
                              @Param("expectStatus") Integer expectStatus,
                              @Param("targetStatus") Integer targetStatus);

}
