package cn.weitee.erp.module.mes.controller.admin.vo.workcalendar;

import cn.weitee.erp.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MesWorkCalendarPageReqVO extends PageParam {

    private String calendarName;

    private Long workCenterId;

    private Integer status;

}
