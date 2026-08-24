package cn.weitee.erp.module.mes.controller.admin;

import cn.weitee.erp.framework.common.pojo.CommonResult;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.mes.controller.admin.vo.workcalendar.MesWorkCalendarPageReqVO;
import cn.weitee.erp.module.mes.controller.admin.vo.workcalendar.MesWorkCalendarSaveReqVO;
import cn.weitee.erp.module.mes.dal.dataobject.MesWorkCalendarDO;
import cn.weitee.erp.module.mes.service.MesWorkCalendarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import static cn.weitee.erp.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - MES 工作日历")
@RestController
@RequestMapping("/mes/work-calendar")
@Validated
public class MesWorkCalendarController {

    @Resource
    private MesWorkCalendarService mesWorkCalendarService;

    @GetMapping("/page")
    @Operation(summary = "获得工作日历分页")
    @PreAuthorize("@ss.hasPermission('mes:work-calendar:query')")
    public CommonResult<PageResult<MesWorkCalendarDO>> getCalendarPage(@Valid MesWorkCalendarPageReqVO pageReqVO) {
        return success(mesWorkCalendarService.getCalendarPage(pageReqVO));
    }

    @GetMapping("/get")
    @Operation(summary = "获得工作日历")
    @PreAuthorize("@ss.hasPermission('mes:work-calendar:query')")
    public CommonResult<MesWorkCalendarDO> getCalendar(@RequestParam("id") Long id) {
        return success(mesWorkCalendarService.getCalendar(id));
    }

    @PostMapping("/create")
    @Operation(summary = "创建工作日历")
    @PreAuthorize("@ss.hasPermission('mes:work-calendar:create')")
    public CommonResult<Long> createCalendar(@Valid @RequestBody MesWorkCalendarSaveReqVO reqVO) {
        return success(mesWorkCalendarService.createCalendar(reqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新工作日历")
    @PreAuthorize("@ss.hasPermission('mes:work-calendar:update')")
    public CommonResult<Boolean> updateCalendar(@Valid @RequestBody MesWorkCalendarSaveReqVO reqVO) {
        mesWorkCalendarService.updateCalendar(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除工作日历")
    @PreAuthorize("@ss.hasPermission('mes:work-calendar:delete')")
    public CommonResult<Boolean> deleteCalendar(@RequestParam("id") Long id) {
        mesWorkCalendarService.deleteCalendar(id);
        return success(true);
    }

}
