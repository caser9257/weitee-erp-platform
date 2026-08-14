package cn.weitee.erp.module.mes.dal.mysql;

import cn.weitee.erp.module.mes.controller.admin.vo.oee.MesOeeSummaryRespVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface MesOeeMapper {

    /**
     * OEE 聚合：按工作中心 + 计划日期分组。
     * 数据源：mes_work_task（计划/实际时间、计划数量）+ erp_production_report_item（报工/合格数，按工序步骤关联）。
     */
    @Select("""
            <script>
            SELECT t.work_center_id AS workCenterId,
                   DATE_FORMAT(t.plan_start_time, '%Y-%m-%d') AS statDate,
                   COUNT(t.id) AS taskCount,
                   IFNULL(SUM(t.plan_qty), 0) AS planQty,
                   IFNULL(SUM(r.reported_qty), 0) AS reportedQty,
                   IFNULL(SUM(r.qualified_qty), 0) AS qualifiedQty,
                   IFNULL(SUM(TIMESTAMPDIFF(MINUTE, t.plan_start_time, t.plan_end_time)), 0) AS planMinutes,
                   IFNULL(SUM(CASE WHEN t.actual_start_time IS NOT NULL AND t.actual_end_time IS NOT NULL
                       THEN TIMESTAMPDIFF(MINUTE, t.actual_start_time, t.actual_end_time) ELSE 0 END), 0) AS actualMinutes
            FROM mes_work_task t
            LEFT JOIN erp_production_report_item r
                   ON r.production_order_step_id = t.order_step_id AND r.deleted = 0
            WHERE t.deleted = 0
              AND t.plan_start_time IS NOT NULL
              <if test="workCenterId != null">
              AND t.work_center_id = #{workCenterId}
              </if>
              <if test="startDate != null">
              AND DATE(t.plan_start_time) &gt;= #{startDate}
              </if>
              <if test="endDate != null">
              AND DATE(t.plan_start_time) &lt;= #{endDate}
              </if>
            GROUP BY t.work_center_id, DATE_FORMAT(t.plan_start_time, '%Y-%m-%d')
            ORDER BY t.work_center_id, DATE_FORMAT(t.plan_start_time, '%Y-%m-%d') DESC
            </script>
            """)
    List<MesOeeSummaryRespVO> selectOeeSummary(@Param("workCenterId") Long workCenterId,
                                               @Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);

}
