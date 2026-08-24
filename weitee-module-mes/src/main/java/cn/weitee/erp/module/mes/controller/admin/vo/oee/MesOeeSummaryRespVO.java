package cn.weitee.erp.module.mes.controller.admin.vo.oee;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class MesOeeSummaryRespVO {

    private Long workCenterId;

    private String workCenterName;

    /** 统计日期（yyyy-MM-dd，避免 LocalDate 默认数组序列化） */
    private String statDate;

    private Long taskCount;

    private BigDecimal planQty;

    private BigDecimal reportedQty;

    private BigDecimal qualifiedQty;

    private Long planMinutes;

    private Long actualMinutes;

    /** 可用率（实际时长/计划时长） */
    private BigDecimal availabilityRate;

    /** 达成率（报工数/计划数，第一版替代性能率） */
    private BigDecimal achievementRate;

    /** 良品率（合格数/报工数） */
    private BigDecimal qualityRate;

    /** 综合 OEE = 可用率 × 达成率 × 良品率 */
    private BigDecimal oee;

}
