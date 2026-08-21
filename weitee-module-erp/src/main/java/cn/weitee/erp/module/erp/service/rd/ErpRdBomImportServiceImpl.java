package cn.weitee.erp.module.erp.service.rd;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.weitee.erp.framework.common.enums.CommonStatusEnum;
import cn.weitee.erp.framework.excel.core.util.FileImportProtector;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.controller.admin.rd.vo.bom.ErpRdBomImportResultVO;
import cn.weitee.erp.module.erp.controller.admin.rd.vo.bom.ErpRdBomIntegrityIssueRespVO;
import cn.weitee.erp.module.erp.controller.admin.rd.vo.bom.ErpRdBomSaveReqVO;
import cn.weitee.erp.module.erp.controller.admin.product.vo.product.ProductSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductCategoryDO;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductDO;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductUnitDO;
import cn.weitee.erp.module.erp.dal.mysql.product.ErpProductCategoryMapper;
import cn.weitee.erp.module.erp.dal.mysql.product.ErpProductMapper;
import cn.weitee.erp.module.erp.dal.mysql.product.ErpProductUnitMapper;
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
    private ErpProductCategoryMapper productCategoryMapper;
    @Resource
    private ErpProductUnitMapper productUnitMapper;
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

        Map<String, ErpProductDO> materialCodeMap = buildMaterialCodeMap();
        Map<String, ErpProductDO> normalizedCodeMap = new HashMap<>();
        for (Map.Entry<String, ErpProductDO> e : materialCodeMap.entrySet()) {
            String norm = e.getKey().replaceAll("\\s+", "");
            normalizedCodeMap.putIfAbsent(norm, e.getValue());
            normalizedCodeMap.putIfAbsent(e.getKey().trim(), e.getValue());
        }

        List<ErpRdBomSaveReqVO.Item> validItems = new ArrayList<>();
        String detectedBomCode = bomCode;
        Long detectedProductId = productId;
        String detectedVersion = version;
        String detectedRemark = remark;
        SmartHeader header = null;
        try {
            byte[] plainContent = fileImportProtector.preparePlainContent(file.getBytes(), file.getOriginalFilename());
            try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(plainContent))) {
                Sheet sheet = workbook.getSheetAt(0);
                header = tryParseSmartHeader(sheet, materialCodeMap, normalizedCodeMap);
                if (header != null) {
                    if (detectedProductId == null && header.productId != null) {
                        detectedProductId = header.productId;
                    }
                    if (StrUtil.isBlank(detectedBomCode) && StrUtil.isNotBlank(header.bomCode)) {
                        detectedBomCode = header.bomCode.trim();
                    }
                    if (StrUtil.isBlank(detectedVersion) && StrUtil.isNotBlank(header.version)) {
                        detectedVersion = header.version.trim();
                    }
                }
                int detailHeaderRow = findSmartDetailHeaderRow(sheet);
                if (detailHeaderRow >= 0) {
                    Map<String, Integer> colIndex = buildSmartColumnIndex(sheet.getRow(detailHeaderRow));
                    Integer levelStart = colIndex.get("层级");
                    if (levelStart == null) {
                        Integer seqCol = colIndex.get("序号");
                        if (seqCol != null) {
                            levelStart = seqCol + 1;
                        }
                    }
                    String topMaterialCodeNorm = header != null && header.bomCode != null ? header.bomCode.replaceAll("\\s+", "") : null;
                    int dataStart = detailHeaderRow + 1;
                    if (dataStart <= sheet.getLastRowNum()) {
                        Row maybeSub = sheet.getRow(dataStart);
                        if (maybeSub != null && isNumericRow(maybeSub, levelStart)) {
                            dataStart++;
                        }
                    }
                    int totalRows = 0;
                    for (int i = dataStart; i <= sheet.getLastRowNum(); i++) {
                        Row row = sheet.getRow(i);
                        if (row == null || isRowEmpty(row)) {
                            continue;
                        }
                        totalRows++;
                        try {
                            ErpRdBomSaveReqVO.Item item = parseSmartRow(row, colIndex, levelStart, materialCodeMap, normalizedCodeMap, topMaterialCodeNorm);
                            if (item == null) {
                                continue;
                            }
                            validItems.add(item);
                            result.setSuccessCount(result.getSuccessCount() + 1);
                        } catch (Exception e) {
                            result.setFailCount(result.getFailCount() + 1);
                            ErpRdBomImportResultVO.FailDetail failDetail = new ErpRdBomImportResultVO.FailDetail();
                            failDetail.setRowNumber(i + 1);
                            failDetail.setMaterialCode(getCellString(row, colIndex.getOrDefault("物料编码", COL_MATERIAL_CODE)));
                            failDetail.setReason(e.getMessage());
                            result.getFailDetails().add(failDetail);
                        }
                    }
                    result.setTotalCount(totalRows);
                } else {
                    int lastRow = sheet.getLastRowNum();
                    result.setTotalCount(lastRow);
                    for (int i = 1; i <= lastRow; i++) {
                        Row row = sheet.getRow(i);
                        if (row == null || isRowEmpty(row)) {
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
            }
        } catch (IOException e) {
            log.error("[importRdBom] 读取导入文件失败", e);
            throw new RuntimeException("读取导入文件失败：" + e.getMessage());
        }

        if (validItems.isEmpty()) {
            return result;
        }

        Long finalProductId = detectedProductId;
        ErpProductDO topProduct = null;
        if (finalProductId != null) {
            topProduct = productService.getProduct(finalProductId);
        }
        if (topProduct == null && header != null && StrUtil.isNotBlank(header.bomCode)) {
            String topCode = header.bomCode.trim();
            String topNorm = topCode.replaceAll("\\s+", "");
            topProduct = normalizedCodeMap.get(topNorm);
            if (topProduct == null) topProduct = materialCodeMap.get(topCode);
            if (topProduct == null) {
                String topName = StrUtil.isNotBlank(header.productName) ? header.productName : topCode;
                topProduct = autoCreateTopProduct(topCode, topName);
                materialCodeMap.put(topCode, topProduct);
                normalizedCodeMap.put(topNorm, topProduct);
                finalProductId = topProduct.getId();
            } else {
                finalProductId = topProduct.getId();
            }
        }
        if (finalProductId == null) {
            throw exception(ErrorCodeConstants.PRODUCT_NOT_EXISTS);
        }
        if (topProduct == null) {
            topProduct = productService.getProduct(finalProductId);
        }
        if (topProduct == null) {
            throw exception(ErrorCodeConstants.PRODUCT_NOT_EXISTS);
        }
        topProduct = ensureProductUsable(topProduct);
        if (topProduct.getMaterialCode() != null) {
            materialCodeMap.put(topProduct.getMaterialCode(), topProduct);
            String tNorm = topProduct.getMaterialCode().replaceAll("\\s+", "");
            normalizedCodeMap.put(tNorm, topProduct);
        }
        finalProductId = topProduct.getId();
        String finalBomCode = StrUtil.isBlank(detectedBomCode) ? topProduct.getMaterialCode() : detectedBomCode.trim();
        if (StrUtil.isBlank(finalBomCode)) {
            finalBomCode = "RD-BOM-" + System.currentTimeMillis();
        }

        ErpRdBomSaveReqVO saveReq = new ErpRdBomSaveReqVO();
        saveReq.setProductId(finalProductId);
        saveReq.setBomCode(finalBomCode);
        saveReq.setVersion(StrUtil.isBlank(detectedVersion) ? null : detectedVersion.trim());
        saveReq.setRemark(StrUtil.isBlank(detectedRemark) ? null : detectedRemark.trim());
        saveReq.setItems(validItems);
        Long bomId = rdBomService.createRdBom(saveReq);
        result.setBomId(bomId);

        List<ErpRdBomIntegrityIssueRespVO> issues = rdBomService.validateRdBomIntegrity(bomId);
        result.setValidationIssues(issues);
        return result;
    }

    private static class SmartHeader {
        Long productId;
        String bomCode;
        String version;
        String productName;
    }

    private SmartHeader tryParseSmartHeader(Sheet sheet, Map<String, ErpProductDO> codeMap, Map<String, ErpProductDO> normMap) {
        String foundCode = null;
        String foundVersion = null;
        String foundProductName = null;
        Long foundProductId = null;
        for (int r = 0; r < Math.min(4, sheet.getLastRowNum() + 1); r++) {
            Row row = sheet.getRow(r);
            if (row == null) continue;
            for (int c = 0; c < row.getLastCellNum(); c++) {
                String cell = getCellString(row, c);
                if (cell == null) continue;
                String t = cell.trim();
                if (t.contains("物料编码")) {
                    for (int k = 1; k <= 3; k++) {
                        String v = getCellString(row, c + k);
                        if (StrUtil.isNotBlank(v) && v.replaceAll("\\s+", "").matches(".*\\d+.*\\..*")) {
                            foundCode = v.trim();
                            String norm = foundCode.replaceAll("\\s+", "");
                            ErpProductDO prod = normMap.get(norm);
                            if (prod == null) prod = codeMap.get(foundCode.trim());
                            if (prod != null) foundProductId = prod.getId();
                            break;
                        }
                    }
                }
                if (t.equals("版本") || t.contains("版本")) {
                    for (int k = 1; k <= 2; k++) {
                        String v = getCellString(row, c + k);
                        if (StrUtil.isNotBlank(v) && v.trim().matches("[Vv].*")) {
                            foundVersion = v.trim();
                            break;
                        }
                    }
                }
                if (t.contains("产品名称")) {
                    for (int k = 1; k <= 2; k++) {
                        String v = getCellString(row, c + k);
                        if (StrUtil.isNotBlank(v) && v.trim().length() >= 2) {
                            foundProductName = v.trim();
                            break;
                        }
                    }
                }
            }
        }
        if (foundCode == null && foundProductId == null && foundVersion == null && foundProductName == null) {
            return null;
        }
        SmartHeader h = new SmartHeader();
        h.bomCode = foundCode;
        h.productId = foundProductId;
        h.version = foundVersion;
        h.productName = foundProductName;
        return h;
    }

    private int findSmartDetailHeaderRow(Sheet sheet) {
        for (int r = 0; r <= Math.min(10, sheet.getLastRowNum()); r++) {
            Row row = sheet.getRow(r);
            if (row == null) continue;
            String rowText = "";
            for (int c = 0; c < row.getLastCellNum(); c++) {
                rowText += getCellString(row, c) + "|";
            }
            if (rowText.contains("物料编码") && rowText.contains("数量") && rowText.contains("物料")) {
                return r;
            }
        }
        return -1;
    }

    private Map<String, Integer> buildSmartColumnIndex(Row headerRow) {
        Map<String, Integer> map = new HashMap<>();
        if (headerRow == null) return map;
        for (int c = 0; c < headerRow.getLastCellNum(); c++) {
            String t = getCellString(headerRow, c).trim();
            if (t.isEmpty()) continue;
            if (t.contains("物料编码")) map.put("物料编码", c);
            else if (t.contains("产品名称") && !t.contains("替代")) map.put("产品名称", c);
            else if (t.equals("数量") || t.contains("数量")) map.put("数量", c);
            else if (t.equals("单位")) map.put("单位", c);
            else if (t.contains("物料位置") || t.contains("位号")) map.put("物料位置", c);
            else if (t.equals("备注")) map.put("备注", c);
            else if (t.contains("替代物料编码") || t.contains("替代")) map.put("替代物料编码", c);
            else if (t.contains("层级")) map.put("层级", c);
            else if (t.equals("序号")) map.put("序号", c);
            else if (t.equals("封装")) map.put("封装", c);
        }
        return map;
    }

    private boolean isNumericRow(Row row, Integer levelStart) {
        if (row == null || levelStart == null) return false;
        for (int c = levelStart; c < levelStart + 4 && c < row.getLastCellNum(); c++) {
            String v = getCellString(row, c).trim();
            if (v.matches("[1-4]")) return true;
        }
        return false;
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) return true;
        for (int c = 0; c < row.getLastCellNum(); c++) {
            if (StrUtil.isNotBlank(getCellString(row, c))) {
                return false;
            }
        }
        return true;
    }

    private ErpRdBomSaveReqVO.Item parseSmartRow(Row row, Map<String, Integer> colIndex, Integer levelStart,
                                                Map<String, ErpProductDO> codeMap, Map<String, ErpProductDO> normMap,
                                                String topCodeNorm) {
        Integer codeCol = colIndex.get("物料编码");
        if (codeCol == null) codeCol = COL_MATERIAL_CODE;
        String rawCode = getCellString(row, codeCol).trim();
        if (StrUtil.isBlank(rawCode)) {
            // 合并单元格/列漂移容错：全行扫描匹配编码形态
            for (int c = 0; c < row.getLastCellNum(); c++) {
                String v = getCellString(row, c).trim();
                if (StrUtil.isBlank(v)) continue;
                String normV = v.replaceAll("\\s+", "");
                if (normV.matches(".*\\d+.*\\..*") && (normMap.containsKey(normV) || codeMap.containsKey(v.trim()))) {
                    rawCode = v.trim();
                    codeCol = c;
                    break;
                }
            }
        }
        if (StrUtil.isBlank(rawCode)) {
            // 仍为空则尝试取整行首个像编码的单元格（兜底）
            for (int c = 0; c < row.getLastCellNum(); c++) {
                String v = getCellString(row, c).trim();
                if (StrUtil.isNotBlank(v) && v.replaceAll("\\s+", "").matches("\\d+\\.\\d+.*")) {
                    rawCode = v.trim();
                    break;
                }
            }
        }
        if (StrUtil.isBlank(rawCode)) {
            throw new IllegalArgumentException("物料编码不能为空");
        }
        String normCode = rawCode.replaceAll("\\s+", "");
        if (topCodeNorm != null && normCode.equals(topCodeNorm)) {
            return null;
        }
        ErpProductDO material = normMap.get(normCode);
        if (material == null) material = codeMap.get(rawCode.trim());
        if (material == null) {
            material = normMap.get(normCode);
        }
        if (material == null) {
            material = autoCreateProduct(rawCode, row, colIndex);
            codeMap.put(rawCode.trim(), material);
            normMap.put(normCode, material);
            String norm2 = rawCode.trim().replaceAll("\\s+", "");
            normMap.putIfAbsent(norm2, material);
        }
        ErpProductDO ensuredMaterial = ensureProductUsable(material);
        if (ensuredMaterial != material) {
            material = ensuredMaterial;
            codeMap.put(rawCode.trim(), material);
            normMap.put(normCode, material);
        }
        Integer qtyCol = colIndex.get("数量");
        if (qtyCol == null) qtyCol = COL_USAGE_QTY;
        String qtyStr = getCellString(row, qtyCol).trim();
        if (StrUtil.isBlank(qtyStr)) {
            throw new IllegalArgumentException("数量不能为空");
        }
        BigDecimal qty;
        try {
            qty = new BigDecimal(qtyStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("数量格式不正确：" + qtyStr);
        }
        if (qty.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("数量必须大于0");
        }
        ErpRdBomSaveReqVO.Item item = new ErpRdBomSaveReqVO.Item();
        item.setMaterialId(material.getId());
        item.setMaterialType(0);
        item.setUnitId(material.getUnitId());
        item.setUsageQty(qty);
        String positionVal = null;
        Integer posCol = colIndex.get("物料位置");
        if (posCol != null) {
            String pos = getCellString(row, posCol).trim();
            if (StrUtil.isNotBlank(pos)) {
                positionVal = pos;
            }
        }
        String remarkVal = null;
        Integer remarkCol = colIndex.get("备注");
        if (remarkCol != null) {
            String rm = getCellString(row, remarkCol).trim();
            if (StrUtil.isNotBlank(rm)) remarkVal = rm;
        }
        // 物料位置：位号形态进位号，否则进独立的 position 列
        if (StrUtil.isNotBlank(positionVal)) {
            boolean looksLikeDesignator = positionVal.matches("(?i).*[A-Za-z]+\\d+.*")
                    && !positionVal.contains("安装")
                    && positionVal.length() < 64
                    && !positionVal.matches(".*[\\u4e00-\\u9fa5]{4,}.*");
            if (looksLikeDesignator) {
                item.setReferenceDesignator(positionVal);
            } else {
                item.setPosition(positionVal);
            }
        }
        if (StrUtil.isNotBlank(remarkVal)) {
            item.setRemark(remarkVal);
        }
        return item;
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

    private ErpProductDO autoCreateProduct(String rawCode, Row row, Map<String, Integer> colIndex) {
        String productName = "";
        Integer nameCol = colIndex.get("产品名称");
        if (nameCol != null) {
            productName = getCellString(row, nameCol).trim();
        }
        if (StrUtil.isBlank(productName)) {
            productName = rawCode.trim();
        }
        String unitStr = "";
        Integer unitCol = colIndex.get("单位");
        if (unitCol != null) {
            unitStr = getCellString(row, unitCol).trim();
        }
        Long unitId = findUnitIdByName(unitStr);
        Long categoryId = findDefaultCategoryId();
        ProductSaveReqVO req = new ProductSaveReqVO();
        req.setName(productName);
        req.setMaterialCode(rawCode.trim());
        req.setBarCode(rawCode.trim());
        req.setCategoryId(categoryId);
        req.setUnitId(unitId);
        req.setStatus(1);
        Long newId = productService.createProduct(req);
        ErpProductDO created = productMapper.selectById(newId);
        if (created == null) {
            throw new IllegalArgumentException("自动创建物料失败：" + rawCode);
        }
        log.info("[autoCreateProduct] 自动创建物料成功，code={}, id={}, name={}", rawCode, newId, productName);
        return created;
    }

    private ErpProductDO autoCreateTopProduct(String rawCode, String productName) {
        Long unitId = findUnitIdByName("");
        Long categoryId = findDefaultCategoryId();
        ProductSaveReqVO req = new ProductSaveReqVO();
        req.setName(StrUtil.isBlank(productName) ? rawCode : productName);
        req.setMaterialCode(rawCode.trim());
        req.setBarCode(rawCode.trim());
        req.setCategoryId(categoryId);
        req.setUnitId(unitId);
        req.setStatus(1);
        Long newId = productService.createProduct(req);
        ErpProductDO created = productMapper.selectById(newId);
        if (created == null) {
            throw new IllegalArgumentException("自动创建顶层物料失败：" + rawCode);
        }
        log.info("[autoCreateTopProduct] 自动创建顶层物料成功，code={}, id={}, name={}", rawCode, newId, productName);
        return created;
    }

    private Long findUnitIdByName(String unitName) {
        if (StrUtil.isNotBlank(unitName)) {
            List<ErpProductUnitDO> units = productUnitMapper.selectList(ErpProductUnitDO::getName, unitName.trim());
            if (CollUtil.isNotEmpty(units)) {
                return units.get(0).getId();
            }
            List<ErpProductUnitDO> allUnits = productUnitMapper.selectList();
            for (ErpProductUnitDO u : allUnits) {
                if (unitName.trim().equalsIgnoreCase(u.getName())) {
                    return u.getId();
                }
            }
        }
        List<ErpProductUnitDO> all = productUnitMapper.selectList();
        if (CollUtil.isNotEmpty(all)) {
            return all.get(0).getId();
        }
        throw new IllegalArgumentException("系统未配置产品单位，无法自动创建物料");
    }

    private Long findDefaultCategoryId() {
        List<ErpProductCategoryDO> list = productCategoryMapper.selectList();
        if (CollUtil.isNotEmpty(list)) {
            for (ErpProductCategoryDO c : list) {
                if (!ErpProductCategoryDO.PARENT_ID_ROOT.equals(c.getParentId())) {
                    return c.getId();
                }
            }
            return list.get(0).getId();
        }
        throw new IllegalArgumentException("系统未配置产品分类，无法自动创建物料");
    }

    private ErpProductDO ensureProductUsable(ErpProductDO product) {
        boolean needUpdate = false;
        ErpProductDO update = new ErpProductDO().setId(product.getId());
        if (CommonStatusEnum.isDisable(product.getStatus())) {
            update.setStatus(CommonStatusEnum.ENABLE.getStatus());
            needUpdate = true;
        }
        Integer auditStatus = product.getAuditStatus();
        if (ErpAuditStatus.PROCESS.getStatus().equals(auditStatus)
                || ErpAuditStatus.REJECT.getStatus().equals(auditStatus)
                || ErpAuditStatus.FAILED.getStatus().equals(auditStatus)) {
            update.setAuditStatus(ErpAuditStatus.DRAFT.getStatus());
            update.setProcessInstanceId(null);
            needUpdate = true;
        }
        if (needUpdate) {
            productMapper.updateById(update);
            ErpProductDO refreshed = productMapper.selectById(product.getId());
            if (refreshed != null) {
                log.info("[ensureProductUsable] 已自动启用/重置产品，id={}, code={}, name={}", product.getId(), product.getMaterialCode(), product.getName());
                return refreshed;
            }
        }
        return product;
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
