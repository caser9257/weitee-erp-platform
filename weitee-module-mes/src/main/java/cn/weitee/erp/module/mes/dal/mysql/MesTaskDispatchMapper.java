package cn.weitee.erp.module.mes.dal.mysql;

import cn.weitee.erp.module.mes.dal.dataobject.MesTaskDispatchDO;
import cn.weitee.erp.module.mes.enums.MesTaskDispatchStatusEnum;
import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Mapper
public interface MesTaskDispatchMapper extends BaseMapperX<MesTaskDispatchDO> {

    default MesTaskDispatchDO selectActiveByTaskId(Long taskId) {
        return selectOne(Wrappers.<MesTaskDispatchDO>lambdaQuery()
                .eq(MesTaskDispatchDO::getTaskId, taskId)
                .eq(MesTaskDispatchDO::getActiveFlag, 1)
                .eq(MesTaskDispatchDO::getDispatchStatus, MesTaskDispatchStatusEnum.ASSIGNED.getStatus()));
    }

    default List<MesTaskDispatchDO> selectActiveListByTaskIds(Collection<Long> taskIds) {
        if (taskIds == null || taskIds.isEmpty()) {
            return List.of();
        }
        return selectList(new LambdaQueryWrapperX<MesTaskDispatchDO>()
                .in(MesTaskDispatchDO::getTaskId, taskIds)
                .eq(MesTaskDispatchDO::getActiveFlag, 1)
                .eq(MesTaskDispatchDO::getDispatchStatus, MesTaskDispatchStatusEnum.ASSIGNED.getStatus())
                .orderByDesc(MesTaskDispatchDO::getDispatchTime));
    }

    /**
     * 查询任务最新一条派工记录，包含已撤销记录，用于工作台展示当前派工状态。
     */
    @Select("<script>SELECT d.* FROM mes_work_task_dispatch d "
            + "WHERE d.deleted = 0 AND d.id = ("
            + "SELECT MAX(latest.id) FROM mes_work_task_dispatch latest "
            + "WHERE latest.task_id = d.task_id AND latest.deleted = 0"
            + ") AND d.task_id IN "
            + "<foreach collection='taskIds' item='taskId' open='(' separator=',' close=')'>"
            + "#{taskId}</foreach></script>")
    List<MesTaskDispatchDO> selectLatestListByTaskIds(@Param("taskIds") Collection<Long> taskIds);

    /**
     * 查询最新派工状态匹配的任务编号，供任务表分页前置筛选使用。
     */
    @Select("SELECT d.task_id FROM mes_work_task_dispatch d "
            + "WHERE d.deleted = 0 AND d.dispatch_status = #{dispatchStatus} AND d.id = ("
            + "SELECT MAX(latest.id) FROM mes_work_task_dispatch latest "
            + "WHERE latest.task_id = d.task_id AND latest.deleted = 0"
            + ")")
    List<Long> selectTaskIdsByLatestStatus(@Param("dispatchStatus") Integer dispatchStatus);

    default List<MesTaskDispatchDO> selectHistoryByTaskId(Long taskId) {
        return selectList(new LambdaQueryWrapperX<MesTaskDispatchDO>()
                .eq(MesTaskDispatchDO::getTaskId, taskId)
                .orderByDesc(MesTaskDispatchDO::getDispatchTime)
                .orderByDesc(MesTaskDispatchDO::getId));
    }

    @Update("UPDATE mes_work_task_dispatch SET dispatch_status = 2, active_flag = NULL, "
            + "revoke_time = #{revokeTime} WHERE task_id = #{taskId} AND active_flag = 1 "
            + "AND dispatch_status = 1 AND deleted = 0")
    int revokeActiveByTaskId(@Param("taskId") Long taskId, @Param("revokeTime") LocalDateTime revokeTime);

    @Update("UPDATE mes_work_task_dispatch SET remark = #{remark} "
            + "WHERE id = #{id} AND active_flag = 1 AND dispatch_status = 1 AND deleted = 0")
    int updateActiveRemarkById(@Param("id") Long id, @Param("remark") String remark);
}
