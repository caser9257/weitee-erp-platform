package cn.iocoder.yudao.module.erp.controller.admin.finance;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.lease.ErpLeaseContractPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.lease.ErpLeaseContractRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.lease.ErpLeaseContractSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpLeaseContractDO;
import cn.iocoder.yudao.module.erp.service.finance.ErpLeaseContractService;
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

@Tag(name = "管理后台 - 租赁合同管理")
@RestController
@RequestMapping("/erp/lease-contract")
@Validated
@Slf4j
public class ErpLeaseContractController {

    @Resource
    private ErpLeaseContractService leaseContractService;

    @PostMapping("/create")
    @PreAuthorize("@ss.hasPermission('erp:lease-contract:create')")
    @Operation(summary = "创建租赁合同")
    public CommonResult<Long> createLeaseContract(@Valid @RequestBody ErpLeaseContractSaveReqVO reqVO) {
        return success(leaseContractService.createLeaseContract(reqVO));
    }

    @PutMapping("/update")
    @PreAuthorize("@ss.hasPermission('erp:lease-contract:update')")
    @Operation(summary = "更新租赁合同")
    public CommonResult<Boolean> updateLeaseContract(@Valid @RequestBody ErpLeaseContractSaveReqVO reqVO) {
        leaseContractService.updateLeaseContract(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @PreAuthorize("@ss.hasPermission('erp:lease-contract:delete')")
    @Operation(summary = "删除租赁合同")
    public CommonResult<Boolean> deleteLeaseContract(@RequestParam("id") Long id) {
        leaseContractService.deleteLeaseContract(id);
        return success(true);
    }

    @GetMapping("/get")
    @PreAuthorize("@ss.hasPermission('erp:lease-contract:query')")
    @Operation(summary = "获取租赁合同")
    public CommonResult<ErpLeaseContractRespVO> getLeaseContract(@RequestParam("id") Long id) {
        ErpLeaseContractDO contract = leaseContractService.getLeaseContract(id);
        return success(BeanUtils.toBean(contract, ErpLeaseContractRespVO.class));
    }

    @GetMapping("/page")
    @PreAuthorize("@ss.hasPermission('erp:lease-contract:query')")
    @Operation(summary = "获取租赁合同分页")
    public CommonResult<PageResult<ErpLeaseContractRespVO>> getLeaseContractPage(@Valid ErpLeaseContractPageReqVO reqVO) {
        PageResult<ErpLeaseContractDO> pageResult = leaseContractService.getLeaseContractPage(reqVO);
        return success(BeanUtils.toBean(pageResult, ErpLeaseContractRespVO.class));
    }

    @GetMapping("/list")
    @PreAuthorize("@ss.hasPermission('erp:lease-contract:query')")
    @Operation(summary = "获取租赁合同列表")
    public CommonResult<List<ErpLeaseContractRespVO>> getLeaseContractList() {
        // 注意：此接口返回全量数据，适用于下拉选择器等场景
        // 如果数据量增长到千级以上，建议改为分页查询或添加查询条件过滤
        List<ErpLeaseContractDO> list = leaseContractService.getLeaseContractList();
        return success(BeanUtils.toBean(list, ErpLeaseContractRespVO.class));
    }

    @PutMapping("/submit-approval")
    @PreAuthorize("@ss.hasPermission('erp:lease-contract:submit-approval')")
    @Operation(summary = "提交审批")
    public CommonResult<Boolean> submitApproval(@RequestParam("id") Long id) {
        leaseContractService.submitApproval(id);
        return success(true);
    }

    @PutMapping("/approve")
    @PreAuthorize("@ss.hasPermission('erp:lease-contract:approve')")
    @Operation(summary = "审批通过")
    public CommonResult<Boolean> approve(@RequestParam("id") Long id,
                                          @RequestParam(value = "remark", required = false) String remark) {
        leaseContractService.approve(id, remark);
        return success(true);
    }

    @PutMapping("/reject")
    @PreAuthorize("@ss.hasPermission('erp:lease-contract:reject')")
    @Operation(summary = "审批驳回")
    public CommonResult<Boolean> reject(@RequestParam("id") Long id,
                                         @RequestParam(value = "remark", required = false) String remark) {
        leaseContractService.reject(id, remark);
        return success(true);
    }

}
