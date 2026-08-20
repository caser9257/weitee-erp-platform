package cn.weitee.erp.module.erp.service.rd;

import cn.hutool.core.util.StrUtil;
import cn.weitee.erp.framework.excel.core.util.FileImportProtector;
import cn.weitee.erp.module.erp.controller.admin.rd.vo.bom.ErpRdBomImportResultVO;
import cn.weitee.erp.module.erp.controller.admin.rd.vo.bom.ErpRdBomIntegrityIssueRespVO;
import cn.weitee.erp.module.erp.controller.admin.rd.vo.bom.ErpRdBomSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductDO;
import cn.weitee.erp.module.erp.dal.mysql.product.ErpProductMapper;
import cn.weitee.erp.module.erp.enums.ErrorCodeConstants;
import cn.weitee.erp.module.erp.service.product.ErpProductService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * 研发 BOM Excel 导入 Service 实现（复用产品导入模式，导入即跑 P0 位号/用量/悬浮件校验）
 */
@Service
@Validated
@Slf4j
public class ErpRdBomImportServiceImpl implements ErpRdBomImportService {

    @Resource
    private FileImportProtector fileImportProtector;
    @Resource
    private ErpProductService productService;
    @Resource
    private ErpProductMapper productMapper;
    @Resource
    private ErpRdBomService rdBomService;

    /** 模板列定义 */
    private static final String[] HEADERS = {
            "物料编号*", "物料名称", "物料类型(1=自制/装配体,0/空=采购件)", "位号", "用量*", "损耗率", "提前期(天)", "备注"
    };

    private static final int COL_MATERIAL_CODE = 0;
    private static final int COL_MATERIAL_NAME = 1;
    private static final int COL_MATERIAL_TYPE = 2;
    private static final int COL_REFERENCE_DESIGNATOR = 3;
    private static final int COL_USAGE_QTY = 4;
    private static final int COL_LOSS_RATE = 5;
    private static final int COL_LEAD_TIME_DAY = 6;
    private static final int COL_REMARK = 7;

    private static final DataFormatter DATA_FORMATTER = new DataFormatter();

    @Override
    public byte[] downloadTemplate() {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("研发BOM明细导入模板");
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                sheet.setColumnWidth(i, 5200);
            }
            Row exampleRow = sheet.createRow(1);
            exampleRow.createCell(COL_MATERIAL_CODE).setCellValue("MAT-1001");
            exampleRow.createCell(COL_MATERIAL_NAME).setCellValue("电阻 10K");
            exampleRow.createCell(COL_MATERIAL_TYPE).setCellValue("0");
            exampleRow.createCell(COL_REFERENCE_DESIGNATOR).setCellValue("R101-R105, R108");
            exampleRow.createCell(COL_USAGE_QTY).setCellValue("7");
            exampleRow.createCell(COL_LOSS_RATE).setCellValue("");
            exampleRow.createCell(COL_LEAD_TIME_DAY).setCellValue("30");
            exampleRow.createCell(COL_REMARK).setCellValue("示例数据，导入前请删除");

            Sheet helpSheet = workbook.createSheet("填写说明");
            String[] helps = {
                    "带 * 号的列为必填列：物料编号、用量",
                    "物料编号必须与系统中已存在的物料编号完全一致，否则该行导入失败",
                    "物料类型：1=自制件/装配体（不需要位号），0 或留空=采购件/元器件（需要位号）",
                    "位号示例：R101-R105, R108, C12（区间会自动展开计数，用量需与位号数量一致）",
                    "用量必须为大于 0 的数字",
                    "示例数据行（第 2 行）仅作格式参考，导入时会被当作真实数据处理，请先删除"
            };
            for (int i = 0; i < helps.length; i++) {
                Row helpRow = helpSheet.createRow(i);
                helpRow.createCell(0).setCellValue(helps[i]);
                helpSheet.setColumnWidth(0, 8000);
            }
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            log.error("[downloadTemplate] 生成研发 BOM 导入模板失败", e);
            throw new RuntimeException("生成研发 BOM 导入模板失败");
        }
    }

    @Override
    public ErpRdBomImportResultVO importRdBom(Long productId, String bomCode, String version, String remark,
                                              Boolean updateSupport, MultipartFile file) {
        ErpRdBomImportResultVO result = new ErpRdBomImportResultVO();
        result.setTotalCount(0);
        result.setSuccessCount(0);
        result.setFailCount(0);
        result.setFailDetails(new ArrayList<>());
        result.setValidationIssues(new ArrayList<>());

        // 1. 成品必须存在
        ErpProductDO product = productService.getProduct(productId);
        if (product == null) {
            throw exception(ErrorCodeConstants.PRODUCT_NOT_EXISTS);
        }

        // 2. 预加载物料编号映射（避免 N+1）
        Map<String, ErpProductDO> materialCodeMap = buildMaterialCodeMap();

        List<ErpRdBomSaveReqVO.Item> validItems = new ArrayList<>();
        try {
            byte[] plainContent = fileImportProtector.preparePlainContent(file.getBytes(), file.getOriginalFilename());
            try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(plainContent))) {
                Sheet sheet = workbook.getSheetAt(0);
                int lastRow = sheet.getLastRowNum(); // 0-based，第 0 行为表头
                result.setTotalCount(lastRow);
                for (int i = 1; i <= lastRow; i++) {
                    Row row = sheet.getRow(i);
                    if (row == null) {
                        continue;
                    }
                    try {
                        validItems.add(parseItemRow(row, materialCodeMap));
                        result.setSuccessCount(result.getSuccessCount() + 1);
                    } catch (Exception e) {
                        result.setFailCount(result.getFailCount() + 1);
                        ErpRdBomImportResultVO.FailDetail failDetail = new ErpRdBomImportResultVO.FailDetail();
                        failDetail.setRowNumber(i + 1);
                        failDetail.setMaterialCode(getCellString(row, COL_MATERIAL_CODE));
                        failDetail.setReason(e.getMessage());
                        result.getFailDetails().add(failDetail);
                    }
                }
            }
        } catch (IOException e) {
            log.error("[importRdBom] 读取导入文件失败", e);
            throw new RuntimeException("读取导入文件失败：" + e.getMessage());
        }

        // 没有任何可导入的明细行，不创建 BOM
        if (validItems.isEmpty()) {
            return result;
        }

        // 3. 创建研发 BOM 草稿（复用既有 createRdBom）
        ErpRdBomSaveReqVO saveReq = new ErpRdBomSaveReqVO();
        saveReq.setProductId(productId);
        saveReq.setBomCode(StrUtil.isBlank(bomCode) ? "" : bomCode.trim());
        saveReq.setVersion(StrUtil.isBlank(version) ? null : version.trim());
        saveReq.setRemark(StrUtil.isBlank(remark) ? null : remark.trim());
        saveReq.setItems(validItems);
        Long bomId = rdBomService.createRdBom(saveReq);
        result.setBomId(bomId);

        // 4. 接 P0 校验：导入即跑位号/用量/悬浮件校验
        List<ErpRdBomIntegrityIssueRespVO> issues = rdBomService.validateRdBomIntegrity(bomId);
        result.setValidationIssues(issues);
        return result;
    }

    private ErpRdBomSaveReqVO.Item parseItemRow(Row row, Map<String, ErpProductDO> materialCodeMap) {
        String materialCode = getCellString(row, COL_MATERIAL_CODE);
        if (StrUtil.isBlank(materialCode)) {
            throw new IllegalArgumentException("物料编号不能为空");
        }
        ErpProductDO material = materialCodeMap.get(materialCode.trim());
        if (material == null) {
            throw new IllegalArgumentException("物料编号不存在：" + materialCode);
        }

        String usageStr = getCellString(row, COL_USAGE_QTY);
        if (StrUtil.isBlank(usageStr)) {
            throw new IllegalArgumentException("用量不能为空");
        }
        BigDecimal usageQty;
        try {
            usageQty = new BigDecimal(usageStr.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("用量格式不正确：" + usageStr);
        }
        if (usageQty.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("用量必须大于 0");
        }

        // 物料类型：默认采购件(0)
        Integer materialType = 0;
        String typeStr = getCellString(row, COL_MATERIAL_TYPE);
        if (StrUtil.isNotBlank(typeStr)) {
            try {
                materialType = Integer.valueOf(typeStr.trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("物料类型只支持 0 或 1：" + typeStr);
            }
        }

        ErpRdBomSaveReqVO.Item item = new ErpRdBomSaveReqVO.Item();
        item.setMaterialId(material.getId());
        item.setMaterialType(materialType);
        item.setUnitId(material.getUnitId());
        item.setUsageQty(usageQty);

        String lossStr = getCellString(row, COL_LOSS_RATE);
        if (StrUtil.isNotBlank(lossStr)) {
            try {
                item.setLossRate(new BigDecimal(lossStr.trim()));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("损耗率格式不正确：" + lossStr);
            }
        }
        String leadStr = getCellString(row, COL_LEAD_TIME_DAY);
        if (StrUtil.isNotBlank(leadStr)) {
            try {
                item.setLeadTimeDay(Integer.valueOf(leadStr.trim()));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("提前期格式不正确：" + leadStr);
            }
        }
        String designator = getCellString(row, COL_REFERENCE_DESIGNATOR);
        item.setReferenceDesignator(StrUtil.isBlank(designator) ? null : designator.trim());
        String remarkStr = getCellString(row, COL_REMARK);
        item.setRemark(StrUtil.isBlank(remarkStr) ? null : remarkStr.trim());
        return item;
    }

    private Map<String, ErpProductDO> buildMaterialCodeMap() {
        List<ErpProductDO> products = productMapper.selectList();
        Map<String, ErpProductDO> map = new HashMap<>();
        for (ErpProductDO product : products) {
            if (StrUtil.isNotBlank(product.getMaterialCode())) {
                map.put(product.getMaterialCode().trim(), product);
            }
        }
        return map;
    }

    private String getCellString(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex);
        if (cell == null) {
            return "";
        }
        String value = DATA_FORMATTER.formatCellValue(cell);
        return value == null ? "" : value.trim();
    }

}
