package cn.iocoder.yudao.module.infra.controller.admin.file;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.infra.dal.dataobject.file.FileAccessStatsDO;
import cn.iocoder.yudao.module.infra.service.file.FileAccessStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.time.LocalDate;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY;

@Tag(name = "管理后台 - 文件访问统计")
@RestController
@RequestMapping("/infra/file-access-stats")
@Validated
public class FileAccessStatsController {

    @Resource
    private FileAccessStatsService fileAccessStatsService;

    @GetMapping("/list")
    @Operation(summary = "获取文件的访问统计")
    @PreAuthorize("@ss.hasPermission('infra:file:query')")
    public CommonResult<List<FileAccessStatsDO>> getFileAccessStats(
            @RequestParam("fileId") Long fileId) {
        return success(fileAccessStatsService.getFileAccessStats(fileId));
    }

    @GetMapping("/get")
    @Operation(summary = "获取文件在指定日期的统计")
    @PreAuthorize("@ss.hasPermission('infra:file:query')")
    public CommonResult<FileAccessStatsDO> getFileAccessStatsByDate(
            @RequestParam("fileId") Long fileId,
            @RequestParam("statsDate") @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY) LocalDate statsDate) {
        return success(fileAccessStatsService.getFileAccessStatsByDate(fileId, statsDate));
    }

    @GetMapping("/date-range")
    @Operation(summary = "获取指定日期范围内的统计")
    @PreAuthorize("@ss.hasPermission('infra:file:query')")
    public CommonResult<List<FileAccessStatsDO>> getAccessStatsByDateRange(
            @RequestParam("startDate") @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY) LocalDate endDate) {
        return success(fileAccessStatsService.getAccessStatsByDateRange(startDate, endDate));
    }

    @GetMapping("/top")
    @Operation(summary = "获取访问量最多的文件")
    @PreAuthorize("@ss.hasPermission('infra:file:query')")
    public CommonResult<List<FileAccessStatsDO>> getTopFiles(
            @RequestParam(value = "limit", defaultValue = "10") int limit,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY) LocalDate endDate) {
        return success(fileAccessStatsService.getTopFiles(limit, startDate, endDate));
    }

}
