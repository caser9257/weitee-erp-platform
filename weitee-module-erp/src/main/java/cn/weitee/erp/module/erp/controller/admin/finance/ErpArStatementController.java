package cn.weitee.erp.module.erp.controller.admin.finance;

import cn.hutool.core.collection.CollUtil;
import cn.weitee.erp.framework.apilog.core.annotation.ApiAccessLog;
import cn.weitee.erp.framework.common.pojo.CommonResult;
import cn.weitee.erp.framework.common.pojo.PageParam;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.excel.core.util.ExcelUtils;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.arstatement.ErpArStatementExportRespVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.arstatement.ErpArStatementPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.arstatement.ErpArStatementRespVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.arstatement.ErpArStatementSummaryRespVO;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpArStatementMapper;
import cn.weitee.erp.module.erp.enums.common.ErpBizTypeEnum;
import cn.weitee.erp.module.erp.service.finance.ErpArStatementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static cn.weitee.erp.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.weitee.erp.framework.common.pojo.CommonResult.success;

/**
 * 应收台账 Controller
 *
 * @author system
 */
@Tag(name = "ERP - 应收台账")
@RestController
@RequestMapping("/erp/ar-statement")
@Validated
public class ErpArStatementController {

    @Resource
    private ErpArStatementService erpArStatementService;
    @Resource
    private ErpArStatementMapper erpArStatementMapper;

    @GetMapping("/page")
    @PreAuthorize("@ss.hasPermission('erp:ar-statement:query')")
    @Operation(summary = "分页查询应收台账")
    public CommonResult<PageResult<ErpArStatementRespVO>> getStatementPage(@Validated ErpArStatementPageReqVO reqVO) {
        return success(erpArStatementService.getStatementPage(reqVO));
    }

    @GetMapping("/get")
    @PreAuthorize("@ss.hasPermission('erp:ar-statement:query')")
    @Operation(summary = "获取应收台账详情")
    @Parameter(name = "id", description = "台账ID", required = true, example = "1024")
    public CommonResult<ErpArStatementRespVO> getStatement(@RequestParam("id") Long id) {
        return success(erpArStatementService.getStatement(id));
    }

    @GetMapping("/summary")
    @PreAuthorize("@ss.hasPermission('erp:ar-statement:query')")
    @Operation(summary = "获取应收台账汇总（按客户）")
    @Parameter(name = "customerId", description = "客户ID（可选）", example = "1024")
    public CommonResult<List<ErpArStatementSummaryRespVO>> getStatementSummary(
            @RequestParam(value = "customerId", required = false) Long customerId) {
        return success(erpArStatementService.getStatementSummary(customerId));
    }

    @GetMapping("/summary-by-order")
    @PreAuthorize("@ss.hasPermission('erp:ar-statement:query')")
    @Operation(summary = "按订单汇总应收台账")
    @Parameter(name = "orderId", description = "销售订单ID", required = true, example = "1024")
    public CommonResult<ErpArStatementSummaryRespVO> getStatementSummaryByOrderId(
            @RequestParam("orderId") Long orderId) {
        // 使用 SQL 聚合查询，避免全量加载到内存
        Map<String, BigDecimal> summaryMap = erpArStatementMapper.selectSummaryBySourceOrderId(orderId);

        ErpArStatementSummaryRespVO summary = new ErpArStatementSummaryRespVO();
        summary.setCustomerId(null); // 按订单维度，不需要客户ID
        summary.setStatementCount(summaryMap.get("count").longValue());
        summary.setTotalAmount(summaryMap.get("totalAmount"));
        summary.setTotalReceivedAmount(summaryMap.get("totalReceivedAmount"));
        summary.setTotalRemainAmount(summaryMap.get("totalRemainAmount"));
        return success(summary);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出应收台账 Excel")
    @PreAuthorize("@ss.hasAnyPermissions('erp:ar-statement:export', 'erp:ar-statement:query')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportArStatementExcel(@Validated ErpArStatementPageReqVO reqVO,
                                       HttpServletResponse response) throws IOException {
        reqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ErpArStatementRespVO> list = erpArStatementService.getStatementPage(reqVO).getList();
        // 转换为导出 VO
        List<ErpArStatementExportRespVO> exportList = new ArrayList<>(list.size());
        for (ErpArStatementRespVO item : list) {
            ErpArStatementExportRespVO exportVO = new ErpArStatementExportRespVO();
            exportVO.setStatementNo(item.getStatementNo());
            exportVO.setBizType(item.getBizType());
            exportVO.setBizNo(item.getBizNo());
            exportVO.setSourceOrderNo(item.getSourceOrderNo());
            exportVO.setCustomerName(item.getCustomerName());
            exportVO.setAccountName(item.getAccountName());
            exportVO.setAmount(item.getAmount());
            exportVO.setReceivedAmount(item.getReceivedAmount());
            exportVO.setRemainAmount(item.getRemainAmount());
            exportVO.setCurrencyCode(item.getCurrencyCode());
            exportVO.setBizDate(item.getBizDate());
            exportVO.setDueDate(item.getDueDate());
            exportVO.setInvoiceStatus(item.getInvoiceStatus());
            exportVO.setInvoiceNo(item.getInvoiceNo());
            exportVO.setInvoiceAmount(item.getInvoiceAmount());
            exportVO.setStatus(item.getStatus());
            exportVO.setRemark(item.getRemark());
            exportVO.setCreateTime(item.getCreateTime());
            // 填充业务类型名称
            if (item.getBizType() != null) {
                for (ErpBizTypeEnum bizTypeEnum : ErpBizTypeEnum.values()) {
                    if (bizTypeEnum.getType().equals(item.getBizType())) {
                        exportVO.setBizTypeName(bizTypeEnum.getName());
                        break;
                    }
                }
            }
            // 填充状态名称
            if (item.getStatus() != null) {
                exportVO.setStatusName(getStatusName(item.getStatus()));
            }
            // 填充发票状态名称
            if (item.getInvoiceStatus() != null) {
                exportVO.setInvoiceStatusName(getInvoiceStatusName(item.getInvoiceStatus()));
            }
            exportList.add(exportVO);
        }
        ExcelUtils.write(response, "应收台账.xls", "数据", ErpArStatementExportRespVO.class, exportList);
    }

    private String getStatusName(Integer status) {
        return switch (status) {
            case 0 -> "待收";
            case 1 -> "部分收";
            case 2 -> "已结清";
            case 3 -> "已关闭";
            default -> "未知";
        };
    }

    private String getInvoiceStatusName(Integer invoiceStatus) {
        return switch (invoiceStatus) {
            case 0 -> "未开票";
            case 1 -> "部分开票";
            case 2 -> "已开票";
            default -> "未知";
        };
    }

}
