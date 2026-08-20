package cn.weitee.erp.module.mes.dal.mysql;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.mes.controller.admin.vo.worktask.MesWorkTaskPageReqVO;
import cn.weitee.erp.module.mes.dal.dataobject.MesWorkTaskDO;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Mapper
public interface MesWorkTaskMapper extends BaseMapperX<MesWorkTaskDO> {

    default MesWorkTaskDO selectByTaskNo(String taskNo) {
        return selectOne(Wrappers.<MesWorkTaskDO>lambdaQuery()
                .eq(MesWorkTaskDO::getTaskNo, taskNo));
    }

    default PageResult<MesWorkTaskDO> selectPage(MesWorkTaskPageReqVO reqVO) {
        return selectPage(reqVO, Collections.emptyList());
    }

    default PageResult<MesWorkTaskDO> selectPage(MesWorkTaskPageReqVO reqVO, Collection<Long> taskIds) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MesWorkTaskDO>()
                .likeIfPresent(MesWorkTaskDO::getTaskNo, reqVO.getTaskNo())
                .likeIfPresent(MesWorkTaskDO::getProductionOrderNo, reqVO.getProductionOrderNo())
                .eqIfPresent(MesWorkTaskDO::getProductionOrderId, reqVO.getProductionOrderId())
                .eqIfPresent(MesWorkTaskDO::getWorkCenterId, reqVO.getWorkCenterId())
                .eqIfPresent(MesWorkTaskDO::getStatus, reqVO.getStatus())
                .inIfPresent(MesWorkTaskDO::getId, taskIds)
                .orderByDesc(MesWorkTaskDO::getId));
    }

    @Select("SELECT * FROM mes_work_task WHERE id = #{id} AND deleted = 0 FOR UPDATE")
    MesWorkTaskDO selectByIdForUpdate(@Param("id") Long id);

    default List<MesWorkTaskDO> selectListByOrderId(Long productionOrderId) {
        return selectList(Wrappers.<MesWorkTaskDO>lambdaQuery()
                .eq(MesWorkTaskDO::getProductionOrderId, productionOrderId)
                .orderByAsc(MesWorkTaskDO::getStepNo));
    }

    /**
     * 调整计划时间 CAS：仅待排程(0)/已排程(1)可更新，返回影响行数。
     */
    @Update("UPDATE mes_work_task SET plan_start_time = #{planStartTime}, plan_end_time = #{planEndTime}, " +
            "remark = #{remark}, status = 1 " +
            "WHERE id = #{id} AND deleted = 0 AND status IN (0, 1)")
    int updatePlanTimeByCas(@Param("id") Long id,
                            @Param("planStartTime") LocalDateTime planStartTime,
                            @Param("planEndTime") LocalDateTime planEndTime,
                            @Param("remark") String remark);

    /**
     * 取消任务 CAS：仅待排程(0)/已排程(1)可取消，返回影响行数。
     */
    @Update("UPDATE mes_work_task SET status = 4 " +
            "WHERE id = #{id} AND deleted = 0 AND status IN (0, 1)")
    int cancelTaskByCas(@Param("id") Long id);

    /**
     * 更新优先级（任意状态可改，仅影响排程顺序）。
     */
    @Update("UPDATE mes_work_task SET priority = #{priority} " +
            "WHERE id = #{id} AND deleted = 0")
    int updatePriority(@Param("id") Long id, @Param("priority") Integer priority);

    /**
     * 清空某工单待排程/已排程任务的计划时间并回退待排程（重排前置）。
     */
    @Update("UPDATE mes_work_task SET plan_start_time = NULL, plan_end_time = NULL, status = 0 " +
            "WHERE production_order_id = #{productionOrderId} AND deleted = 0 AND status IN (0, 1)")
    int clearPlanTimeByOrderId(@Param("productionOrderId") Long productionOrderId);

    /**
     * 报工回写：首次报工设置实际开始时间并进入进行中（CAS：仅待排程/已排程可进入）。
     */
    @Update("UPDATE mes_work_task SET actual_start_time = COALESCE(actual_start_time, #{reportTime}), status = 2 " +
            "WHERE order_step_id = #{orderStepId} AND deleted = 0 AND status IN (0, 1)")
    int markActualStart(@Param("orderStepId") Long orderStepId, @Param("reportTime") LocalDateTime reportTime);

    /**
     * 工序完工回写：设置实际结束时间并完成（CAS：仅进行中可完成）。
     */
    @Update("UPDATE mes_work_task SET actual_end_time = #{finishTime}, status = 3 " +
            "WHERE order_step_id = #{orderStepId} AND deleted = 0 AND status = 2")
    int markActualEnd(@Param("orderStepId") Long orderStepId, @Param("finishTime") LocalDateTime finishTime);

    /**
     * 甘特数据：按工作中心 + 时间范围（可选状态）查询有计划时间窗的任务块（含已完成，便于查看历史）。
     */
    default List<MesWorkTaskDO> selectGanttList(Long workCenterId, LocalDateTime startTime, LocalDateTime endTime,
                                                Integer status) {
        return selectList(Wrappers.<MesWorkTaskDO>lambdaQuery()
                .isNotNull(MesWorkTaskDO::getPlanStartTime)
                .isNotNull(MesWorkTaskDO::getPlanEndTime)
                .eq(MesWorkTaskDO::getWorkCenterId, workCenterId)
                .eq(status != null, MesWorkTaskDO::getStatus, status)
                .le(MesWorkTaskDO::getPlanStartTime, endTime)
                .ge(MesWorkTaskDO::getPlanEndTime, startTime)
                .orderByAsc(MesWorkTaskDO::getPlanStartTime));
    }
}
