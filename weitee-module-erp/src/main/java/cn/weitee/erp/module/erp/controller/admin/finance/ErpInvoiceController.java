package cn.weitee.erp.module.erp.controller.admin.finance;

import cn.weitee.erp.framework.common.pojo.CommonResult;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.invoice.ErpInvoicePageReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.invoice.ErpInvoiceSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpInvoiceDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpInvoiceItemDO;
import cn.weitee.erp.module.erp.service.finance.ErpInvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

import static cn.weitee.erp.framework.common.pojo.CommonResult.success;

/**
 * ERP 销项发票 Controller
 *
 * @author system
 */
@Tag(name = "ERP - 销项发票")
@RestController
@RequestMapping("/erp/invoice")
@Validated
@Slf4j
public class ErpInvoiceController {

    @Resource
    private ErpInvoiceService erpInvoiceService;

    @PostMapping("/create")
    @PreAuthorize("@ss.hasPermission('erp:invoice:create')")
    @Operation(summary = "创建销项发票")
    public CommonResult<Long> createInvoice(@Validated @RequestBody ErpInvoiceSaveReqVO createReqVO) {
        Long id = erpInvoiceService.createInvoice(createReqVO);
        return success(id);
    }

    @PutMapping("/update")
    @PreAuthorize("@ss.hasPermission('erp:invoice:update')")
    @Operation(summary = "更新销项发票")
    public CommonResult<Boolean> updateInvoice(@Validated @RequestBody ErpInvoiceSaveReqVO updateReqVO) {
        erpInvoiceService.updateInvoice(updateReqVO);
        return success(true);
    }

    @PutMapping("/update-status")
    @PreAuthorize("@ss.hasPermission('erp:invoice:update')")
    @Operation(summary = "更新销项发票状态")
    public CommonResult<Boolean> updateInvoiceStatus(@RequestParam("id") Long id, @RequestParam("status") String status) {
        erpInvoiceService.updateInvoiceStatus(id, status);
        return success(true);
    }

    @DeleteMapping("/delete")
    @PreAuthorize("@ss.hasPermission('erp:invoice:delete')")
    @Operation(summary = "删除销项发票")
    public CommonResult<Boolean> deleteInvoice(@RequestParam("ids") List<Long> ids) {
        erpInvoiceService.deleteInvoice(ids);
        return success(true);
    }

    @GetMapping("/get")
    @PreAuthorize("@ss.hasPermission('erp:invoice:query')")
    @Operation(summary = "获得销项发票")
    public CommonResult<ErpInvoiceDO> getInvoice(@RequestParam("id") Long id) {
        ErpInvoiceDO invoice = erpInvoiceService.getInvoice(id);
        return success(invoice);
    }

    @GetMapping("/page")
    @PreAuthorize("@ss.hasPermission('erp:invoice:query')")
    @Operation(summary = "获得销项发票分页")
    public CommonResult<PageResult<ErpInvoiceDO>> getInvoicePage(@Validated ErpInvoicePageReqVO pageReqVO) {
        PageResult<ErpInvoiceDO> pageResult = erpInvoiceService.getInvoicePage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/list-items")
    @PreAuthorize("@ss.hasPermission('erp:invoice:query')")
    @Operation(summary = "获得销项发票明细列表")
    public CommonResult<List<ErpInvoiceItemDO>> getInvoiceItemList(@RequestParam("invoiceId") Long invoiceId) {
        List<ErpInvoiceItemDO> items = erpInvoiceService.getInvoiceItemListByInvoiceId(invoiceId);
        return success(items);
    }

    @GetMapping("/invoiced-amount")
    @PreAuthorize("@ss.hasPermission('erp:invoice:query')")
    @Operation(summary = "获得订单已开票金额")
    public CommonResult<BigDecimal> getInvoicedAmount(@RequestParam("orderId") Long orderId) {
        BigDecimal amount = erpInvoiceService.getInvoicedAmountByOrderId(orderId);
        return success(amount);
    }

    @GetMapping("/uninvoiced-items")
    @PreAuthorize("@ss.hasPermission('erp:invoice:query')")
    @Operation(summary = "获得订单可开票明细（排除已开票数量）")
    public CommonResult<List<ErpInvoiceService.UninvoicedItemVO>> getUninvoicedItems(@RequestParam("orderId") Long orderId) {
        return success(erpInvoiceService.getUninvoicedItems(orderId));
    }

}
