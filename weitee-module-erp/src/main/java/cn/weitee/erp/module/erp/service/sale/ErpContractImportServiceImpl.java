package cn.weitee.erp.module.erp.service.sale;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.weitee.erp.framework.excel.core.util.FileImportProtector;
import cn.weitee.erp.module.erp.controller.admin.sale.vo.ContractImportResultVO;
import cn.weitee.erp.module.crm.dal.dataobject.contract.CrmContractDO;
import cn.weitee.erp.module.crm.service.contract.CrmContractService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 合同导入服务实现
 *
 * @author system
 */
@Service
@Validated
@Slf4j
public class ErpContractImportServiceImpl implements ErpContractImportService {

    @Resource
    private FileImportProtector fileImportProtector;

    @Resource
    private CrmContractService crmContractService;

    @Override
    public byte[] downloadTemplate() {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("合同导入模板");

            // 创建表头
            Row headerRow = sheet.createRow(0);
            String[] headers = {"合同编号*", "合同名称*", "客户名称*", "负责人*", "下单日期*", 
                               "发货放行规则", "开票触发条件", "收款规则", "预付款金额", "预付款比例",
                               "需要财务审核", "需要验收", "备注"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                
                // 设置表头样式
                CellStyle headerStyle = workbook.createCellStyle();
                Font headerFont = workbook.createFont();
                headerFont.setBold(true);
                headerStyle.setFont(headerFont);
                cell.setCellStyle(headerStyle);
                
                // 设置列宽
                sheet.setColumnWidth(i, 4000);
            }

            // 创建示例数据行
            Row exampleRow = sheet.createRow(1);
            exampleRow.createCell(0).setCellValue("CT-2026-001");
            exampleRow.createCell(1).setCellValue("某某科技采购合同");
            exampleRow.createCell(2).setCellValue("某某科技有限公司");
            exampleRow.createCell(3).setCellValue("张三");
            exampleRow.createCell(4).setCellValue("2026-06-01");
            exampleRow.createCell(5).setCellValue("AFTER_PAYMENT");
            exampleRow.createCell(6).setCellValue("AFTER_SHIPMENT");
            exampleRow.createCell(7).setCellValue("BEFORE_SHIPMENT");
            exampleRow.createCell(8).setCellValue("50000");
            exampleRow.createCell(9).setCellValue("30");
            exampleRow.createCell(10).setCellValue("1");
            exampleRow.createCell(11).setCellValue("0");
            exampleRow.createCell(12).setCellValue("示例数据");

            // 创建说明 sheet
            Sheet helpSheet = workbook.createSheet("填写说明");
            Row helpRow1 = helpSheet.createRow(0);
            helpRow1.createCell(0).setCellValue("发货放行规则可选值：");
            Row helpRow2 = helpSheet.createRow(1);
            helpRow2.createCell(0).setCellValue("SIGN_AND_SHIP - 签约即发");
            Row helpRow3 = helpSheet.createRow(2);
            helpRow3.createCell(0).setCellValue("AFTER_PAYMENT - 到账后发");
            Row helpRow4 = helpSheet.createRow(3);
            helpRow4.createCell(0).setCellValue("AFTER_PREPAYMENT - 达到预付款比例后发");
            Row helpRow5 = helpSheet.createRow(4);
            helpRow5.createCell(0).setCellValue("FINANCE_APPROVAL - 财务审核后发");

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            log.error("生成导入模板失败", e);
            throw new RuntimeException("生成导入模板失败");
        }
    }

    @Override
    public ContractImportResultVO importContracts(MultipartFile file) {
        ContractImportResultVO result = new ContractImportResultVO();
        result.setTotalCount(0);
        result.setSuccessCount(0);
        result.setFailCount(0);
        result.setFailDetails(new ArrayList<>());

        try {
            byte[] plainContent = fileImportProtector.preparePlainContent(file.getBytes(), file.getOriginalFilename());
            try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(plainContent))) {
            Sheet sheet = workbook.getSheetAt(0);
            int totalRows = sheet.getLastRowNum();
            result.setTotalCount(totalRows);

            for (int i = 1; i <= totalRows; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    // 读取行数据
                    String no = getCellValue(row, 0);
                    String name = getCellValue(row, 1);
                    String customerName = getCellValue(row, 2);
                    String ownerUserName = getCellValue(row, 3);
                    String orderDate = getCellValue(row, 4);
                    String shipmentReleaseRule = getCellValue(row, 5);
                    String invoiceTrigger = getCellValue(row, 6);
                    String collectionRule = getCellValue(row, 7);
                    String prepaymentAmount = getCellValue(row, 8);
                    String prepaymentRatio = getCellValue(row, 9);
                    String financeApprovalRequired = getCellValue(row, 10);
                    String acceptanceRequired = getCellValue(row, 11);
                    String remark = getCellValue(row, 12);

                    // TODO: 实际导入逻辑
                    // 1. 校验必填字段
                    // 2. 查找客户ID
                    // 3. 查找负责人ID
                    // 4. 创建合同

                    result.setSuccessCount(result.getSuccessCount() + 1);
                } catch (Exception e) {
                    result.setFailCount(result.getFailCount() + 1);
                    ContractImportResultVO.FailDetail failDetail = new ContractImportResultVO.FailDetail();
                    failDetail.setRowNumber(i + 1);
                    failDetail.setReason(e.getMessage());
                    result.getFailDetails().add(failDetail);
                }
            }
            }
        } catch (IOException e) {
            log.error("导入合同失败", e);
            throw new RuntimeException("导入合同失败：" + e.getMessage());
        }

        return result;
    }

    private String getCellValue(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex);
        if (cell == null) return "";
        
        cell.setCellType(CellType.STRING);
        return cell.getStringCellValue().trim();
    }

}
