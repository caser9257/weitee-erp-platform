package cn.iocoder.yudao.module.erp.controller.admin.finance;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.erp.service.finance.ErpFinanceLedgerRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - ERP 账簿角色关联")
@RestController
@RequestMapping("/erp/finance/ledger-role")
@Validated
public class ErpFinanceLedgerRoleController {

    @Resource
    private ErpFinanceLedgerRoleService ledgerRoleService;

    @PostMapping("/create")
    @Operation(summary = "创建账簿角色关联")
    @PreAuthorize("@ss.hasPermission('erp:finance:ledger-role:create')")
    public CommonResult<Long> createLedgerRole(@RequestParam("ledgerId") Long ledgerId,
                                                @RequestParam("roleId") Long roleId) {
        return success(ledgerRoleService.createLedgerRole(ledgerId, roleId));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除账簿角色关联")
    @PreAuthorize("@ss.hasPermission('erp:finance:ledger-role:delete')")
    public CommonResult<Boolean> deleteLedgerRole(@RequestParam("ledgerId") Long ledgerId,
                                                   @RequestParam("roleId") Long roleId) {
        ledgerRoleService.deleteLedgerRole(ledgerId, roleId);
        return success(true);
    }

    @PostMapping("/set-roles")
    @Operation(summary = "设置账簿的可见角色列表")
    @PreAuthorize("@ss.hasPermission('erp:finance:ledger-role:update')")
    public CommonResult<Boolean> setLedgerRoles(@RequestParam("ledgerId") Long ledgerId,
                                                 @RequestParam("roleIds") List<Long> roleIds) {
        ledgerRoleService.setLedgerRoles(ledgerId, roleIds);
        return success(true);
    }

    @GetMapping("/visible-ledgers")
    @Operation(summary = "获取指定角色可见的账簿ID列表")
    @PreAuthorize("@ss.hasPermission('erp:finance:ledger-role:query')")
    public CommonResult<List<Long>> getVisibleLedgerIds(@RequestParam("roleId") Long roleId) {
        return success(ledgerRoleService.getVisibleLedgerIdsByRoleId(roleId));
    }

    @GetMapping("/ledger-roles")
    @Operation(summary = "获取指定账簿的可见角色ID列表")
    @PreAuthorize("@ss.hasPermission('erp:finance:ledger-role:query')")
    public CommonResult<List<Long>> getRoleIdsByLedgerId(@RequestParam("ledgerId") Long ledgerId) {
        return success(ledgerRoleService.getRoleIdsByLedgerId(ledgerId));
    }

}
