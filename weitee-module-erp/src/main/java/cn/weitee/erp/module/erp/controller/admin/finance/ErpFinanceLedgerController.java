package cn.weitee.erp.module.erp.controller.admin.finance;

import cn.weitee.erp.framework.apilog.core.annotation.ApiAccessLog;
import cn.weitee.erp.framework.common.enums.CommonStatusEnum;
import cn.weitee.erp.framework.common.pojo.CommonResult;
import cn.weitee.erp.framework.common.pojo.PageParam;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.framework.excel.core.util.ExcelUtils;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.ledger.ErpFinanceLedgerPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.ledger.ErpFinanceLedgerRespVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.ledger.ErpFinanceLedgerSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceLedgerDO;
import cn.weitee.erp.module.erp.service.finance.ErpFinanceLedgerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;

import static cn.weitee.erp.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.weitee.erp.framework.common.pojo.CommonResult.success;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertList;

@Tag(name = "管理后台 - ERP 财务账簿")
@RestController
@RequestMapping("/erp/finance-ledger")
@Validated
public class ErpFinanceLedgerController {

    @Resource
    private ErpFinanceLedgerService financeLedgerService;

    @PostMapping("/create")
    @Operation(summary = "创建财务账簿")
    @PreAuthorize("@ss.hasPermission('erp:finance-ledger:create')")
    public CommonResult<Long> createFinanceLedger(@Valid @RequestBody ErpFinanceLedgerSaveReqVO createReqVO) {
        return success(financeLedgerService.createFinanceLedger(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新财务账簿")
    @PreAuthorize("@ss.hasPermission('erp:finance-ledger:update')")
    public CommonResult<Boolean> updateFinanceLedger(@Valid @RequestBody ErpFinanceLedgerSaveReqVO updateReqVO) {
        financeLedgerService.updateFinanceLedger(updateReqVO);
        return success(true);
    }

    @PutMapping("/update-default-status")
    @Operation(summary = "更新财务账簿默认状态")
    @Parameters({
            @Parameter(name = "id", description = "编号", required = true),
            @Parameter(name = "defaultStatus", description = "是否默认", required = true)
    })
    @PreAuthorize("@ss.hasPermission('erp:finance-ledger:update')")
    public CommonResult<Boolean> updateFinanceLedgerDefaultStatus(@RequestParam("id") Long id,
                                                                  @RequestParam("defaultStatus") Boolean defaultStatus) {
        financeLedgerService.updateFinanceLedgerDefaultStatus(id, defaultStatus);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除财务账簿")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('erp:finance-ledger:delete')")
    public CommonResult<Boolean> deleteFinanceLedger(@RequestParam("id") Long id) {
        financeLedgerService.deleteFinanceLedger(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得财务账簿")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('erp:finance-ledger:query')")
    public CommonResult<ErpFinanceLedgerRespVO> getFinanceLedger(@RequestParam("id") Long id) {
        ErpFinanceLedgerDO ledger = financeLedgerService.getFinanceLedger(id);
        return success(BeanUtils.toBean(ledger, ErpFinanceLedgerRespVO.class));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得启用账簿精简列表")
    @PreAuthorize("@ss.hasPermission('erp:finance-ledger:query')")
    public CommonResult<List<ErpFinanceLedgerRespVO>> getFinanceLedgerSimpleList() {
        List<ErpFinanceLedgerDO> list = financeLedgerService.getFinanceLedgerListByStatus(CommonStatusEnum.ENABLE.getStatus());
        return success(convertList(list, ledger -> new ErpFinanceLedgerRespVO()
                .setId(ledger.getId())
                .setNo(ledger.getNo())
                .setName(ledger.getName())
                .setDefaultStatus(ledger.getDefaultStatus())));
    }

    @GetMapping("/page")
    @Operation(summary = "获得财务账簿分页")
    @PreAuthorize("@ss.hasPermission('erp:finance-ledger:query')")
    public CommonResult<PageResult<ErpFinanceLedgerRespVO>> getFinanceLedgerPage(@Valid ErpFinanceLedgerPageReqVO pageReqVO) {
        PageResult<ErpFinanceLedgerDO> pageResult = financeLedgerService.getFinanceLedgerPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, ErpFinanceLedgerRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出财务账簿 Excel")
    @PreAuthorize("@ss.hasPermission('erp:finance-ledger:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportFinanceLedgerExcel(@Valid ErpFinanceLedgerPageReqVO pageReqVO,
                                         HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ErpFinanceLedgerDO> list = financeLedgerService.getFinanceLedgerPage(pageReqVO).getList();
        ExcelUtils.write(response, "财务账簿.xls", "数据", ErpFinanceLedgerRespVO.class,
                BeanUtils.toBean(list, ErpFinanceLedgerRespVO.class));
    }
}
