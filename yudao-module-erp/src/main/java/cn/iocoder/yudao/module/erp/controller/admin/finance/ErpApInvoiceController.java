package cn.iocoder.yudao.module.erp.controller.admin.finance;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.apinvoice.ErpApInvoiceCancelMatchReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.apinvoice.ErpApInvoiceMatchReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.apinvoice.ErpApInvoicePageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.apinvoice.ErpApInvoicePendingItemPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.apinvoice.ErpApInvoicePendingItemRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.apinvoice.ErpApInvoiceRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.apinvoice.ErpApInvoiceSaveReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpApInvoiceDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpApInvoiceMatchItemDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpSupplierDO;
import cn.iocoder.yudao.module.erp.enums.ErpApInvoiceMatchItemStatusEnum;
import cn.iocoder.yudao.module.erp.enums.ErpApInvoiceMatchStatusEnum;
import cn.iocoder.yudao.module.erp.enums.ErpApInvoiceTypeEnum;
import cn.iocoder.yudao.module.erp.service.finance.ErpApInvoiceService;
import cn.iocoder.yudao.module.erp.service.product.ErpProductService;
import cn.iocoder.yudao.module.erp.service.purchase.ErpSupplierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - ERP 采购发票")
@RestController
@RequestMapping("/erp/ap-invoice")
@Validated
public class ErpApInvoiceController {

    @Resource
    private ErpApInvoiceService apInvoiceService;
    @Resource
    private ErpSupplierService supplierService;
    @Resource
    private ErpProductService productService;

    @PostMapping("/create")
    @Operation(summary = "创建采购发票")
    @PreAuthorize("@ss.hasPermission('erp:ap-invoice:create')")
    public CommonResult<Long> createApInvoice(@Valid @RequestBody ErpApInvoiceSaveReqVO createReqVO) {
        return success(apInvoiceService.createApInvoice(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新采购发票")
    @PreAuthorize("@ss.hasPermission('erp:ap-invoice:update')")
    public CommonResult<Boolean> updateApInvoice(@Valid @RequestBody ErpApInvoiceSaveReqVO updateReqVO) {
        apInvoiceService.updateApInvoice(updateReqVO);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得采购发票详情")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('erp:ap-invoice:query')")
    public CommonResult<ErpApInvoiceRespVO> getApInvoice(@RequestParam("id") Long id) {
        ErpApInvoiceDO invoice = apInvoiceService.getApInvoice(id);
        if (invoice == null) {
            return success(null);
        }
        return success(buildInvoiceRespVO(invoice, apInvoiceService.getApInvoiceMatchItemListByInvoiceId(id)));
    }

    @GetMapping("/page")
    @Operation(summary = "获得采购发票分页")
    @PreAuthorize("@ss.hasPermission('erp:ap-invoice:query')")
    public CommonResult<PageResult<ErpApInvoiceRespVO>> getApInvoicePage(@Valid ErpApInvoicePageReqVO pageReqVO) {
        return success(buildInvoicePageResult(apInvoiceService.getApInvoicePage(pageReqVO)));
    }

    @GetMapping("/pending-item-page")
    @Operation(summary = "获得采购发票待匹配明细分页")
    @PreAuthorize("@ss.hasPermission('erp:ap-invoice:query')")
    public CommonResult<PageResult<ErpApInvoicePendingItemRespVO>> getPendingItemPage(
            @Valid ErpApInvoicePendingItemPageReqVO pageReqVO) {
        PageResult<ErpApInvoicePendingItemRespVO> pageResult = apInvoiceService.getPendingItemPage(pageReqVO);
        fillPendingItemProductNames(pageResult.getList());
        return success(pageResult);
    }

    @PostMapping("/confirm-match")
    @Operation(summary = "确认发票匹配")
    @PreAuthorize("@ss.hasPermission('erp:ap-invoice:update')")
    public CommonResult<Boolean> confirmMatch(@Valid @RequestBody ErpApInvoiceMatchReqVO reqVO) {
        apInvoiceService.confirmMatch(reqVO);
        return success(true);
    }

    @PostMapping("/cancel-match")
    @Operation(summary = "撤销发票匹配")
    @PreAuthorize("@ss.hasPermission('erp:ap-invoice:update')")
    public CommonResult<Boolean> cancelMatch(@Valid @RequestBody ErpApInvoiceCancelMatchReqVO reqVO) {
        apInvoiceService.cancelMatch(reqVO);
        return success(true);
    }

    private PageResult<ErpApInvoiceRespVO> buildInvoicePageResult(PageResult<ErpApInvoiceDO> pageResult) {
        if (CollUtil.isEmpty(pageResult.getList())) {
            return PageResult.empty(pageResult.getTotal());
        }
        Map<Long, ErpSupplierDO> supplierMap = supplierService.getSupplierMap(convertSet(pageResult.getList(), ErpApInvoiceDO::getSupplierId));
        return BeanUtils.toBean(pageResult, ErpApInvoiceRespVO.class, respVO -> {
            MapUtils.findAndThen(supplierMap, respVO.getSupplierId(), supplier -> respVO.setSupplierName(supplier.getName()));
            respVO.setInvoiceTypeName(resolveInvoiceTypeName(respVO.getInvoiceType()));
            respVO.setMatchStatusName(resolveMatchStatusName(respVO.getMatchStatus()));
        });
    }

    private ErpApInvoiceRespVO buildInvoiceRespVO(ErpApInvoiceDO invoice, List<ErpApInvoiceMatchItemDO> itemList) {
        ErpApInvoiceRespVO respVO = BeanUtils.toBean(invoice, ErpApInvoiceRespVO.class);
        Map<Long, ErpSupplierDO> supplierMap = invoice.getSupplierId() == null
                ? Collections.emptyMap()
                : supplierService.getSupplierMap(List.of(invoice.getSupplierId()));
        MapUtils.findAndThen(supplierMap, invoice.getSupplierId(), supplier -> respVO.setSupplierName(supplier.getName()));
        respVO.setInvoiceTypeName(resolveInvoiceTypeName(invoice.getInvoiceType()));
        respVO.setMatchStatusName(resolveMatchStatusName(invoice.getMatchStatus()));
        if (CollUtil.isEmpty(itemList)) {
            respVO.setItems(List.of());
            return respVO;
        }
        Map<Long, ErpProductRespVO> productMap = productService.getProductVOMap(convertSet(itemList, ErpApInvoiceMatchItemDO::getProductId));
        respVO.setItems(BeanUtils.toBean(itemList, ErpApInvoiceRespVO.Item.class, item -> {
            item.setStatusName(resolveMatchItemStatusName(item.getStatus()));
            MapUtils.findAndThen(productMap, item.getProductId(), product -> item.setProductName(product.getName()));
        }));
        return respVO;
    }

    private void fillPendingItemProductNames(List<ErpApInvoicePendingItemRespVO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        Map<Long, ErpProductRespVO> productMap = productService.getProductVOMap(convertSet(list, ErpApInvoicePendingItemRespVO::getProductId));
        list.forEach(item -> MapUtils.findAndThen(productMap, item.getProductId(), product -> item.setProductName(product.getName())));
    }

    private String resolveInvoiceTypeName(Integer invoiceType) {
        return Arrays.stream(ErpApInvoiceTypeEnum.values())
                .filter(item -> ObjectUtil.equal(item.getStatus(), invoiceType))
                .map(ErpApInvoiceTypeEnum::getName)
                .findFirst()
                .orElse("");
    }

    private String resolveMatchStatusName(Integer status) {
        return Arrays.stream(ErpApInvoiceMatchStatusEnum.values())
                .filter(item -> ObjectUtil.equal(item.getStatus(), status))
                .map(ErpApInvoiceMatchStatusEnum::getName)
                .findFirst()
                .orElse("");
    }

    private String resolveMatchItemStatusName(Integer status) {
        return Arrays.stream(ErpApInvoiceMatchItemStatusEnum.values())
                .filter(item -> ObjectUtil.equal(item.getStatus(), status))
                .map(ErpApInvoiceMatchItemStatusEnum::getName)
                .findFirst()
                .orElse("");
    }

}
