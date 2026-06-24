package cn.iocoder.yudao.module.erp.controller.admin.purchase;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.inquality.ErpPurchaseInQualityAssignCheckerReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.inquality.ErpPurchaseInQualityCreateReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.inquality.ErpPurchaseInQualityPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.inquality.ErpPurchaseInQualityPrintDataRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.inquality.ErpPurchaseInQualityRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.inquality.ErpPurchaseInQualityStartRecheckReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.inquality.ErpPurchaseInQualitySubmitFirstCheckReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.inquality.ErpPurchaseInQualitySubmitRecheckReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.inquality.ErpPurchaseInQualitySubmitReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.inquality.ErpQcDefectReasonSimpleRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseInDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseInQualityDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseInQualityDefectDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseInQualityItemDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseInQualityRoundDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpSupplierDO;
import cn.iocoder.yudao.module.erp.service.product.ErpProductService;
import cn.iocoder.yudao.module.erp.service.purchase.ErpQcDefectReasonService;
import cn.iocoder.yudao.module.erp.service.purchase.ErpPurchaseInQualityService;
import cn.iocoder.yudao.module.erp.service.purchase.ErpPurchaseInService;
import cn.iocoder.yudao.module.erp.service.purchase.ErpSupplierService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
@Tag(name = "Admin - ERP Purchase In IQC")
@RestController
@RequestMapping("/erp/purchase-in-quality")
@Validated
public class ErpPurchaseInQualityController {

    @Resource
    private ErpPurchaseInQualityService purchaseInQualityService;
    @Resource
    private ErpPurchaseInService purchaseInService;
    @Resource
    private ErpProductService productService;
    @Resource
    private ErpSupplierService supplierService;
    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private ErpQcDefectReasonService qcDefectReasonService;

    @PostMapping("/create")
    @Operation(summary = "Create purchase-in IQC order")
    @PreAuthorize("@ss.hasPermission('erp:purchase-in-quality:create')")
    public CommonResult<Long> createPurchaseInQuality(@Valid @RequestBody ErpPurchaseInQualityCreateReqVO reqVO) {
        return success(purchaseInQualityService.createQualityOrderIfAbsent(reqVO.getPurchaseInId()));
    }

    @PostMapping("/submit")
    @Operation(summary = "Submit lightweight purchase-in IQC order")
    @PreAuthorize("@ss.hasPermission('erp:purchase-in-quality:first-check')")
    public CommonResult<Boolean> submitPurchaseInQuality(@Valid @RequestBody ErpPurchaseInQualitySubmitReqVO reqVO) {
        purchaseInQualityService.submitPurchaseInQuality(getLoginUserId(), reqVO);
        return success(true);
    }

    @PostMapping("/assign-checker")
    @Operation(summary = "Assign purchase-in IQC checker")
    @PreAuthorize("@ss.hasPermission('erp:purchase-in-quality:assign-checker')")
    public CommonResult<Boolean> assignChecker(@Valid @RequestBody ErpPurchaseInQualityAssignCheckerReqVO reqVO) {
        purchaseInQualityService.assignChecker(getLoginUserId(), reqVO);
        return success(true);
    }

    @PostMapping("/submit-first-check")
    @Operation(summary = "Submit purchase-in IQC first check")
    @PreAuthorize("@ss.hasPermission('erp:purchase-in-quality:first-check')")
    public CommonResult<Boolean> submitFirstCheck(
            @Valid @RequestBody ErpPurchaseInQualitySubmitFirstCheckReqVO reqVO) {
        purchaseInQualityService.submitFirstCheck(getLoginUserId(), reqVO);
        return success(true);
    }

    @PostMapping("/start-recheck")
    @Operation(summary = "Start purchase-in IQC recheck")
    @PreAuthorize("@ss.hasPermission('erp:purchase-in-quality:start-recheck')")
    public CommonResult<Boolean> startRecheck(
            @Valid @RequestBody ErpPurchaseInQualityStartRecheckReqVO reqVO) {
        purchaseInQualityService.startRecheck(getLoginUserId(), reqVO);
        return success(true);
    }

    @PostMapping("/submit-recheck")
    @Operation(summary = "Submit purchase-in IQC recheck")
    @PreAuthorize("@ss.hasPermission('erp:purchase-in-quality:recheck')")
    public CommonResult<Boolean> submitRecheck(
            @Valid @RequestBody ErpPurchaseInQualitySubmitRecheckReqVO reqVO) {
        purchaseInQualityService.submitRecheck(getLoginUserId(), reqVO);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "Get purchase-in IQC order")
    @Parameter(name = "id", description = "ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('erp:purchase-in-quality:query')")
    public CommonResult<ErpPurchaseInQualityRespVO> getPurchaseInQuality(@RequestParam("id") Long id) {
        return success(buildQualityRespVO(purchaseInQualityService.getPurchaseInQuality(id)));
    }

    @GetMapping("/get-by-purchase-in-id")
    @Operation(summary = "Get IQC order by purchase-in ID")
    @Parameter(name = "purchaseInId", description = "Purchase-in ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('erp:purchase-in-quality:query')")
    public CommonResult<ErpPurchaseInQualityRespVO> getPurchaseInQualityByPurchaseInId(
            @RequestParam("purchaseInId") Long purchaseInId) {
        return success(buildQualityRespVO(purchaseInQualityService.getPurchaseInQualityByPurchaseInId(purchaseInId)));
    }

    @GetMapping("/page")
    @Operation(summary = "Get purchase-in IQC page")
    @PreAuthorize("@ss.hasPermission('erp:purchase-in-quality:query')")
    public CommonResult<PageResult<ErpPurchaseInQualityRespVO>> getPurchaseInQualityPage(
            @Valid ErpPurchaseInQualityPageReqVO pageReqVO) {
        PageResult<ErpPurchaseInQualityDO> pageResult = purchaseInQualityService.getPurchaseInQualityPage(pageReqVO);
        return success(buildQualityPageRespVO(pageResult));
    }

    @GetMapping("/defect-reason-simple-list")
    @Operation(summary = "Get IQC defect reason simple list")
    @PreAuthorize("@ss.hasPermission('erp:purchase-in-quality:query')")
    public CommonResult<List<ErpQcDefectReasonSimpleRespVO>> getDefectReasonSimpleList() {
        return success(convertList(
                qcDefectReasonService.getDefectReasonListByStatus(CommonStatusEnum.ENABLE.getStatus()),
                defectReason -> BeanUtils.toBean(defectReason, ErpQcDefectReasonSimpleRespVO.class)));
    }

    private ErpPurchaseInQualityRespVO buildQualityRespVO(ErpPurchaseInQualityDO quality) {
        if (quality == null) {
            return null;
        }
        ErpPurchaseInDO purchaseIn = purchaseInService.getPurchaseIn(quality.getPurchaseInId());
        List<ErpPurchaseInQualityItemDO> qualityItems =
                purchaseInQualityService.getPurchaseInQualityItemListByQualityId(quality.getId());
        List<ErpPurchaseInQualityRoundDO> roundList =
                purchaseInQualityService.getRoundDOListByQualityId(quality.getId());
        List<ErpPurchaseInQualityDefectDO> defectList =
                purchaseInQualityService.getDefectDOListByQualityId(quality.getId());

        Map<Long, ErpProductRespVO> productMap = productService.getProductVOMap(
                convertSet(qualityItems, ErpPurchaseInQualityItemDO::getProductId));
        Map<Long, ErpSupplierDO> supplierMap = purchaseIn == null || purchaseIn.getSupplierId() == null ? Map.of()
                : supplierService.getSupplierMap(List.of(purchaseIn.getSupplierId()));

        List<Long> userIds = new ArrayList<>();
        if (quality.getAssignedCheckerUserId() != null) {
            userIds.add(quality.getAssignedCheckerUserId());
        }
        if (quality.getCheckerUserId() != null) {
            userIds.add(quality.getCheckerUserId());
        }
        if (quality.getRecheckApplyUserId() != null) {
            userIds.add(quality.getRecheckApplyUserId());
        }
        roundList.stream().map(ErpPurchaseInQualityRoundDO::getCheckerUserId)
                .filter(Objects::nonNull).forEach(userIds::add);
        userIds = userIds.stream().distinct().toList();
        Map<Long, AdminUserRespDTO> userMap = userIds.isEmpty() ? Map.of() : adminUserApi.getUserMap(userIds);

        ErpPurchaseInQualityRespVO respVO = BeanUtils.toBean(quality, ErpPurchaseInQualityRespVO.class);
        if (purchaseIn != null) {
            respVO.setPurchaseInStatus(purchaseIn.getStatus());
            respVO.setQaStatus(purchaseIn.getQaStatus());
            respVO.setStockInStatus(purchaseIn.getStockInStatus());
            respVO.setStockInCount(purchaseIn.getStockInCount());
            respVO.setRemainingStockInCount(calculateRemainingStockInCount(
                    purchaseIn.getQaPassCount(), purchaseIn.getStockInCount()));
            respVO.setOrderNo(purchaseIn.getOrderNo());
            MapUtils.findAndThen(supplierMap, purchaseIn.getSupplierId(),
                    supplier -> respVO.setSupplierName(supplier.getName()));
        }
        MapUtils.findAndThen(userMap, quality.getAssignedCheckerUserId(),
                user -> respVO.setAssignedCheckerUserNickname(user.getNickname()));
        MapUtils.findAndThen(userMap, quality.getCheckerUserId(),
                user -> respVO.setCheckerUserNickname(user.getNickname()));
        MapUtils.findAndThen(userMap, quality.getRecheckApplyUserId(),
                user -> respVO.setRecheckApplyUserNickname(user.getNickname()));

        respVO.setItems(convertList(qualityItems, item -> {
            ErpPurchaseInQualityRespVO.Item itemVO = BeanUtils.toBean(item, ErpPurchaseInQualityRespVO.Item.class);
            MapUtils.findAndThen(productMap, item.getProductId(), product -> itemVO
                    .setProductName(product.getName())
                    .setProductBarCode(product.getBarCode())
                    .setProductUnitName(product.getUnitName()));
            return itemVO;
        }));
        respVO.setRounds(convertList(roundList, round -> {
            ErpPurchaseInQualityRespVO.Round roundVO = BeanUtils.toBean(round, ErpPurchaseInQualityRespVO.Round.class);
            MapUtils.findAndThen(userMap, round.getCheckerUserId(),
                    user -> roundVO.setCheckerUserNickname(user.getNickname()));
            return roundVO;
        }));
        respVO.setDefects(convertList(defectList, defect -> new ErpPurchaseInQualityRespVO.Defect()
                .setId(defect.getId())
                .setRoundId(defect.getRoundId())
                .setQualityItemId(defect.getQualityItemId())
                .setDefectReasonId(defect.getDefectReasonId())
                .setDefectReasonName(defect.getDefectReasonName())
                .setDefectCount(defect.getDefectCount())
                .setDefectRemark(defect.getRemark())));
        return respVO;
    }

    private PageResult<ErpPurchaseInQualityRespVO> buildQualityPageRespVO(PageResult<ErpPurchaseInQualityDO> pageResult) {
        if (CollUtil.isEmpty(pageResult.getList())) {
            return PageResult.empty(pageResult.getTotal());
        }
        Map<Long, ErpPurchaseInDO> purchaseInMap = convertMap(
                purchaseInService.getPurchaseInListByIds(convertSet(pageResult.getList(), ErpPurchaseInQualityDO::getPurchaseInId)),
                ErpPurchaseInDO::getId);
        Map<Long, ErpSupplierDO> supplierMap = supplierService.getSupplierMap(convertSet(
                purchaseInMap.values().stream().filter(Objects::nonNull).toList(), ErpPurchaseInDO::getSupplierId));
        List<Long> checkerUserIds = pageResult.getList().stream()
                .flatMap(item -> Stream.of(item.getAssignedCheckerUserId(), item.getCheckerUserId()))
                .filter(Objects::nonNull)
                .toList();
        Map<Long, AdminUserRespDTO> userMap = checkerUserIds.isEmpty() ? Map.of()
                : adminUserApi.getUserMap(checkerUserIds);
        return BeanUtils.toBean(pageResult, ErpPurchaseInQualityRespVO.class, respVO -> {
            ErpPurchaseInDO purchaseIn = purchaseInMap.get(respVO.getPurchaseInId());
            if (purchaseIn != null) {
                respVO.setPurchaseInStatus(purchaseIn.getStatus());
                respVO.setQaStatus(purchaseIn.getQaStatus());
                respVO.setStockInStatus(purchaseIn.getStockInStatus());
                respVO.setStockInCount(purchaseIn.getStockInCount());
                respVO.setRemainingStockInCount(calculateRemainingStockInCount(
                        purchaseIn.getQaPassCount(), purchaseIn.getStockInCount()));
                respVO.setOrderNo(purchaseIn.getOrderNo());
                MapUtils.findAndThen(supplierMap, purchaseIn.getSupplierId(),
                        supplier -> respVO.setSupplierName(supplier.getName()));
            }
            MapUtils.findAndThen(userMap, respVO.getAssignedCheckerUserId(),
                    user -> respVO.setAssignedCheckerUserNickname(user.getNickname()));
            MapUtils.findAndThen(userMap, respVO.getCheckerUserId(),
                    user -> respVO.setCheckerUserNickname(user.getNickname()));
        });
    }

    private BigDecimal calculateRemainingStockInCount(BigDecimal qaPassCount, BigDecimal stockInCount) {
        BigDecimal remainingCount = (qaPassCount == null ? BigDecimal.ZERO : qaPassCount)
                .subtract(stockInCount == null ? BigDecimal.ZERO : stockInCount);
        return remainingCount.compareTo(BigDecimal.ZERO) > 0 ? remainingCount : BigDecimal.ZERO;
    }

    @PostMapping("/create-return")
    @Operation(summary = "从质检创建退货单")
    @PreAuthorize("@ss.hasPermission('erp:purchase-in-quality:update')")
    public CommonResult<Long> createReturnFromQuality(@RequestParam("qualityId") Long qualityId) {
        Long returnId = purchaseInQualityService.createReturnFromQuality(qualityId, getLoginUserId());
        return success(returnId);
    }

    @GetMapping("/get-print-data")
    @Operation(summary = "获取质检单套打数据")
    @PreAuthorize("@ss.hasPermission('erp:purchase-in-quality:query')")
    public CommonResult<ErpPurchaseInQualityPrintDataRespVO> getPurchaseInQualityPrintData(@RequestParam("id") Long id) {
        ErpPurchaseInQualityDO quality = purchaseInQualityService.getPurchaseInQuality(id);
        ErpPurchaseInQualityPrintDataRespVO printData = new ErpPurchaseInQualityPrintDataRespVO();
        // 复用完整组装逻辑，包含 items、rounds、defects、供应商、检验员等信息
        ErpPurchaseInQualityRespVO qualityRespVO = buildQualityRespVO(quality);
        printData.setPurchaseInQuality(qualityRespVO);
        // 来源附件
        ErpPurchaseInDO purchaseIn = purchaseInService.getPurchaseIn(quality.getPurchaseInId());
        if (purchaseIn != null && purchaseIn.getFileUrl() != null) {
            ErpPurchaseInQualityPrintDataRespVO.SourceAttachment attachment =
                    new ErpPurchaseInQualityPrintDataRespVO.SourceAttachment();
            attachment.setName("采购入库单附件");
            attachment.setUrl(purchaseIn.getFileUrl());
            printData.setSourceAttachments(java.util.List.of(attachment));
        } else {
            printData.setSourceAttachments(java.util.List.of());
        }
        return success(printData);
    }

}
