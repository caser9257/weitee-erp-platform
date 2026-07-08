package cn.weitee.erp.module.erp.controller.admin.statistics;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.weitee.erp.framework.common.pojo.CommonResult;
import cn.weitee.erp.framework.common.util.date.LocalDateTimeUtils;
import cn.weitee.erp.module.erp.controller.admin.statistics.vo.purchase.ErpPurchaseSummaryRespVO;
import cn.weitee.erp.module.erp.controller.admin.statistics.vo.purchase.ErpPurchaseTimeSummaryRespVO;
import cn.weitee.erp.module.erp.service.statistics.ErpPurchaseStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static cn.hutool.core.date.DatePattern.NORM_MONTH_PATTERN;
import static cn.weitee.erp.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - ERP 采购统计")
@RestController
@RequestMapping("/erp/purchase-statistics")
@Validated
public class ErpPurchaseStatisticsController {

    @Resource
    private ErpPurchaseStatisticsService purchaseStatisticsService;

    @GetMapping("/summary")
    @Operation(summary = "获得采购统计")
    @PreAuthorize("@ss.hasPermission('erp:statistics:query')")
    public CommonResult<ErpPurchaseSummaryRespVO> getPurchaseSummary() {
        LocalDateTime today = LocalDateTimeUtils.getToday();
        LocalDateTime yesterday = LocalDateTimeUtils.getYesterday();
        LocalDateTime month = LocalDateTimeUtils.getMonth();
        LocalDateTime year = LocalDateTimeUtils.getYear();
        ErpPurchaseSummaryRespVO summary = new ErpPurchaseSummaryRespVO()
                .setTodayPrice(purchaseStatisticsService.getPurchasePrice(today, null))
                .setYesterdayPrice(purchaseStatisticsService.getPurchasePrice(yesterday, today))
                .setMonthPrice(purchaseStatisticsService.getPurchasePrice(month, null))
                .setYearPrice(purchaseStatisticsService.getPurchasePrice(year, null));
        return success(summary);
    }

    @GetMapping("/time-summary")
    @Operation(summary = "获得采购时间段统计")
    @Parameter(name = "count", description = "时间段数量", example = "6")
    @PreAuthorize("@ss.hasPermission('erp:statistics:query')")
    public CommonResult<List<ErpPurchaseTimeSummaryRespVO>> getPurchaseTimeSummary(
            @RequestParam(value = "count", defaultValue = "6") Integer count) {
        int actualCount = normalizeCount(count);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime beginTime = LocalDateTimeUtils.beginOfMonth(now.minusMonths(actualCount - 1L));
        LocalDateTime endTime = LocalDateTimeUtils.beginOfMonth(now.plusMonths(1));
        Map<String, BigDecimal> priceMap = purchaseStatisticsService.getPurchasePriceMapByMonth(beginTime, endTime);
        List<ErpPurchaseTimeSummaryRespVO> summaryList = new ArrayList<>();
        for (int i = actualCount - 1; i >= 0; i--) {
            LocalDateTime startTime = LocalDateTimeUtils.beginOfMonth(now.minusMonths(i));
            String month = LocalDateTimeUtil.format(startTime, NORM_MONTH_PATTERN);
            summaryList.add(new ErpPurchaseTimeSummaryRespVO()
                    .setTime(month)
                    .setPrice(priceMap.getOrDefault(month, BigDecimal.ZERO)));
        }
        return success(summaryList);
    }

    private int normalizeCount(Integer count) {
        if (count == null) {
            return 6;
        }
        return Math.max(1, Math.min(count, 24));
    }

}
