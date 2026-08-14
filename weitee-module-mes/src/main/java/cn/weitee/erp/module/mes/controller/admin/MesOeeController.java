package cn.weitee.erp.module.mes.controller.admin;

import cn.weitee.erp.framework.common.pojo.CommonResult;
import cn.weitee.erp.module.mes.controller.admin.vo.oee.MesOeeSummaryRespVO;
import cn.weitee.erp.module.mes.service.MesOeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.time.LocalDate;
import java.util.List;

import static cn.weitee.erp.framework.common.pojo.CommonResult.success;
import static cn.weitee.erp.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY;

@Tag(name = "管理后台 - MES OEE 统计")
@RestController
@RequestMapping("/mes/oee")
@Validated
public class MesOeeController {

    @Resource
    private MesOeeService mesOeeService;

    @GetMapping("/summary")
    @Operation(summary = "获得 OEE 汇总（按工作中心+日期）")
    @PreAuthorize("@ss.hasPermission('mes:oee:query')")
    public CommonResult<List<MesOeeSummaryRespVO>> getOeeSummary(
            @RequestParam(value = "workCenterId", required = false) Long workCenterId,
            @RequestParam(value = "startDate", required = false)
            @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY) LocalDate startDate,
            @RequestParam(value = "endDate", required = false)
            @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY) LocalDate endDate) {
        return success(mesOeeService.getOeeSummary(workCenterId, startDate, endDate));
    }

}
