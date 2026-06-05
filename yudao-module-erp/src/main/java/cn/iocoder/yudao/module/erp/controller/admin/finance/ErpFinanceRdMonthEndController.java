package cn.iocoder.yudao.module.erp.controller.admin.finance;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.rdmonthend.ErpFinanceRdMonthEndExecuteReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.rdmonthend.ErpFinanceRdMonthEndRespVO;
import cn.iocoder.yudao.module.erp.service.finance.ErpFinanceRdMonthEndService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - ERP 研发月末处理")
@RestController
@RequestMapping("/erp/finance-rd-month-end")
@Validated
public class ErpFinanceRdMonthEndController {

    @Resource
    private ErpFinanceRdMonthEndService financeRdMonthEndService;

    @PostMapping("/execute")
    @Operation(summary = "执行研发月末处理")
    @PreAuthorize("@ss.hasPermission('erp:finance-expense:update')")
    public CommonResult<ErpFinanceRdMonthEndRespVO> execute(@Valid @RequestBody ErpFinanceRdMonthEndExecuteReqVO reqVO) {
        return success(BeanUtils.toBean(financeRdMonthEndService.executeMonthEnd(reqVO.getPeriod()),
                ErpFinanceRdMonthEndRespVO.class));
    }
}
