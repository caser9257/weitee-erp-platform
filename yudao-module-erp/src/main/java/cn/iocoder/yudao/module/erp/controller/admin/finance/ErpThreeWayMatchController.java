package cn.iocoder.yudao.module.erp.controller.admin.finance;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.match.ErpThreeWayMatchPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.match.ErpThreeWayMatchRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpThreeWayMatchDO;
import cn.iocoder.yudao.module.erp.service.finance.ErpThreeWayMatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 三单匹配管理")
@RestController
@RequestMapping("/erp/three-way-match")
@Validated
@Slf4j
public class ErpThreeWayMatchController {

    @Resource
    private ErpThreeWayMatchService threeWayMatchService;

    @PostMapping("/match")
    @PreAuthorize("@ss.hasPermission('erp:three-way-match:match')")
    @Operation(summary = "执行三单匹配")
    public CommonResult<Long> match(
            @RequestParam("leaseContractId") Long leaseContractId,
            @RequestParam("serviceReceiptId") Long serviceReceiptId,
            @RequestParam("invoiceNo") String invoiceNo) {
        return success(threeWayMatchService.match(leaseContractId, serviceReceiptId, invoiceNo));
    }

    @GetMapping("/page")
    @PreAuthorize("@ss.hasPermission('erp:three-way-match:query')")
    @Operation(summary = "获取匹配记录分页")
    public CommonResult<PageResult<ErpThreeWayMatchRespVO>> getMatchPage(@Valid ErpThreeWayMatchPageReqVO reqVO) {
        PageResult<ErpThreeWayMatchDO> pageResult = threeWayMatchService.getMatchPage(reqVO);
        return success(BeanUtils.toBean(pageResult, ErpThreeWayMatchRespVO.class));
    }

    @GetMapping("/list")
    @PreAuthorize("@ss.hasPermission('erp:three-way-match:query')")
    @Operation(summary = "获取匹配记录列表")
    public CommonResult<List<ErpThreeWayMatchRespVO>> getMatchList() {
        List<ErpThreeWayMatchDO> list = threeWayMatchService.getMatchList();
        return success(BeanUtils.toBean(list, ErpThreeWayMatchRespVO.class));
    }

    @GetMapping("/get")
    @PreAuthorize("@ss.hasPermission('erp:three-way-match:query')")
    @Operation(summary = "获取匹配记录")
    public CommonResult<ErpThreeWayMatchRespVO> getMatch(@RequestParam("id") Long id) {
        ErpThreeWayMatchDO match = threeWayMatchService.getMatch(id);
        return success(BeanUtils.toBean(match, ErpThreeWayMatchRespVO.class));
    }

    @PutMapping("/confirm")
    @PreAuthorize("@ss.hasPermission('erp:three-way-match:confirm')")
    @Operation(summary = "确认匹配")
    public CommonResult<Boolean> confirmMatch(@RequestParam("id") Long id) {
        threeWayMatchService.confirmMatch(id);
        return success(true);
    }

}
