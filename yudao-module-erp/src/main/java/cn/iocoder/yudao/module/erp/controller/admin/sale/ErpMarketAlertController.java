package cn.iocoder.yudao.module.erp.controller.admin.sale;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.MarketAlertRuleVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.MarketAlertVO;
import cn.iocoder.yudao.module.erp.service.sale.ErpMarketAlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 市场预警 Controller
 *
 * @author system
 */
@Tag(name = "ERP - 市场预警")
@RestController
@RequestMapping("/erp/market-alert")
@Validated
@Slf4j
public class ErpMarketAlertController {

    @Resource
    private ErpMarketAlertService erpMarketAlertService;

    @GetMapping("/rules")
    @PreAuthorize("@ss.hasPermission('erp:market-alert:query')")
    @Operation(summary = "获取预警规则列表")
    public CommonResult<List<MarketAlertRuleVO>> getAlertRules() {
        List<MarketAlertRuleVO> rules = erpMarketAlertService.getAlertRules();
        return success(rules);
    }

    @PutMapping("/rules")
    @PreAuthorize("@ss.hasPermission('erp:market-alert:update')")
    @Operation(summary = "更新预警规则")
    public CommonResult<Boolean> updateAlertRule(@RequestBody MarketAlertRuleVO ruleVO) {
        erpMarketAlertService.updateAlertRule(ruleVO);
        return success(true);
    }

    @GetMapping("/list")
    @PreAuthorize("@ss.hasPermission('erp:market-alert:query')")
    @Operation(summary = "获取当前预警列表")
    public CommonResult<List<MarketAlertVO>> getCurrentAlerts() {
        List<MarketAlertVO> alerts = erpMarketAlertService.getCurrentAlerts();
        return success(alerts);
    }

    @PostMapping("/check")
    @PreAuthorize("@ss.hasPermission('erp:market-alert:check')")
    @Operation(summary = "检查并触发预警")
    public CommonResult<Integer> checkAndTriggerAlerts() {
        int count = erpMarketAlertService.checkAndTriggerAlerts();
        return success(count);
    }

}
