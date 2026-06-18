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
        List<ErpLeaseContractDO> list = leaseContractService.getLeaseContractList();
        return success(BeanUtils.toBean(list, ErpLeaseContractRespVO.class));
    }

}
