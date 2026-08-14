package cn.weitee.erp.module.mes.dal.dataobject;

import cn.weitee.erp.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/** MES 工作日历。 */
@TableName("mes_work_calendar")
@KeySequence("mes_work_calendar_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesWorkCalendarDO extends BaseDO {

    @TableId
    private Long id;
    private String calendarName;
    private Long workCenterId;
    private LocalDate effectiveDate;
    private LocalDate expireDate;
    private String weekMask;
    private BigDecimal dailyHours;
    private Integer status;
    private String remark;
}
