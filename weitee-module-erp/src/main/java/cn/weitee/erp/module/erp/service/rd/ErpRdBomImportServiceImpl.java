package cn.weitee.erp.module.erp.service.rd;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.weitee.erp.framework.common.enums.CommonStatusEnum;
import cn.weitee.erp.framework.excel.core.util.FileImportProtector;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.enums.RdBomRowIssueType;
import cn.weitee.erp.module.erp.controller.admin.rd.vo.bom.ErpRdBomImportResultVO;
import cn.weitee.erp.module.erp.controller.admin.rd.vo.bom.ErpRdBomIntegrityIssueRespVO;
import cn.weitee.erp.module.erp.controller.admin.rd.vo.bom.ErpRdBomPrecheckResultVO;
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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * 研发 BOM Excel 导入 Service 实现（复用产品导入模式，导入即跑 P0 位号/用量/悬浮件校验）
 *
 * 解析链路统一收敛到 {@link #parseWorkbook(MultipartFile)}：
 * import = parseWorkbook + 建草稿 + 完整性校验；
 * precheck = parseWorkbook + 失败归堆（待建档/待催审/格式问题），纯读不落库。
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

        ParsedWorkbook parsed = parseWorkbook(file);
        result.setTotalCount(parsed.getTotalCount());
        result.setSuccessCount(parsed.getSuccessCount());
        result.setFailCount(parsed.getFailCount());
        result.setFailDetails(parsed.getFailDetails());

        if (parsed.getItems().isEmpty()) {
            return result;
        }

        Long detectedProductId = detectProductId(productId, parsed.getHeader());
        String detectedBomCode = detectBomCode(bomCode, parsed.getHeader());
        String detectedVersion = detectVersion(version, parsed.getHeader());

        ErpProductDO topProduct = resolveTopProduct(detectedProductId, parsed.getHeader(), parsed);
        if (topProduct == null) {
            throw exception(ErrorCodeConstants.PRODUCT_NOT_EXISTS);
        }
        topProduct = ensureProductUsable(topProduct);
        Long finalProductId = topProduct.getId();
        String finalBomCode = StrUtil.isBlank(detectedBomCode) ? topProduct.getMaterialCode() : detectedBomCode.trim();
        if (StrUtil.isBlank(finalBomCode)) {
            finalBomCode = "RD-BOM-" + System.currentTimeMillis();
        }

        ErpRdBomSaveReqVO saveReq = new ErpRdBomSaveReqVO();
        saveReq.setProductId(finalProductId);
        saveReq.setBomCode(finalBomCode);
        saveReq.setVersion(StrUtil.isBlank(detectedVersion) ? null : detectedVersion.trim());
        saveReq.setRemark(StrUtil.isBlank(remark) ? null : remark.trim());
        saveReq.setItems(parsed.getItems());
        Long bomId = rdBomService.createRdBom(saveReq);
        result.setBomId(bomId);

        List<ErpRdBomIntegrityIssueRespVO> issues = rdBomService.validateRdBomIntegrity(bomId);
        result.setValidationIssues(issues);
        return result;
    }

    @Override
    public ErpRdBomPrecheckResultVO precheckRdBom(Long productId, String bomCode, String version, String remark,
                                                   Boolean updateSupport, MultipartFile file) {
        ParsedWorkbook parsed = parseWorkbook(file);
        ErpRdBomPrecheckResultVO result = new ErpRdBomPrecheckResultVO();
        result.setTotalCount(parsed.getTotalCount());
        result.setReadyCount(parsed.getSuccessCount());
        result.setMissingMaterials(new ArrayList<>());
        result.setUnapprovedMaterials(new ArrayList<>());
        result.setRowIssues(new ArrayList<>());

        // 表头识别结果回显
        ErpRdBomPrecheckResultVO.DetectedHeader detectedHeader = new ErpRdBomPrecheckResultVO.DetectedHeader();
        if (parsed.getHeader() != null) {
            detectedHeader.setBomCode(parsed.getHeader().bomCode);
            detectedHeader.setVersion(parsed.getHeader().version);
            detectedHeader.setProductName(parsed.getHeader().productName);
            detectedHeader.setProductId(parsed.getHeader().productId);
        }

        // 顶层物料模拟判定：与 importRdBom 的 resolveTopProduct 同一匹配顺序
        Long detectedProductId = detectProductId(productId, parsed.getHeader());
        ErpProductDO topProduct = null;
        if (detectedProductId != null) {
            topProduct = productService.getProduct(detectedProductId);
        }
        boolean topLevelMatchedByCode = false;
        if (topProduct == null && parsed.getHeader() != null && StrUtil.isNotBlank(parsed.getHeader().bomCode)) {
            String topCode = parsed.getHeader().bomCode.trim();
            String topNorm = topCode.replaceAll("\\s+", "");
            topProduct = parsed.getNormalizedCodeMap().get(topNorm);
            if (topProduct == null) {
                topProduct = parsed.getMaterialCodeMap().get(topCode);
            }
            topLevelMatchedByCode = topProduct != null;
        }
        if (topProduct == null) {
            if (StrUtil.isNotBlank(detectedHeader.getBomCode())) {
                // 可识别顶层编码但系统缺档 → 进待建档清单
                detectedHeader.setTopLevelMissing(true);
                addMissingMaterial(result.getMissingMaterials(), parsed.getHeader().bomCode.trim(),
                        parsed.getHeader().productName, null, true);
            } else {
                // 无法确定顶层归属，与导入行为保持一致直接报错
                throw exception(ErrorCodeConstants.PRODUCT_NOT_EXISTS);
            }
        } else {
            if (detectedHeader.getProductId() == null) {
                detectedHeader.setProductId(topProduct.getId());
            }
            // 顶层存在但状态不可用：导入必然失败，提前进待催审清单
            if (CommonStatusEnum.isDisable(topProduct.getStatus())) {
                addUnapprovedMaterial(result.getUnapprovedMaterials(),
                        buildUnapproved(topProduct, true), null);
            } else if (!ErpAuditStatus.APPROVE.getStatus().equals(topProduct.getAuditStatus())) {
                addUnapprovedMaterial(result.getUnapprovedMaterials(),
                        buildUnapproved(topProduct, false), null);
            }
        }
        result.setDetectedHeader(detectedHeader);

        // 行级异常归堆
        int blockedRows = 0;
        for (RdBomRowParseException rowException : parsed.getRowExceptions()) {
            RdBomRowIssueType issueType = rowException.getIssueType();
            Integer rowNumber = rowException.getRowNumber();
            switch (issueType) {
                case MISSING_MATERIAL:
                    addMissingMaterial(result.getMissingMaterials(), rowException.getMaterialCode(),
                            rowException.getMaterialName(), rowNumber, false);
                    blockedRows++;
                    break;
                case MATERIAL_NOT_APPROVED:
                case MATERIAL_DISABLED:
                    addUnapprovedMaterial(result.getUnapprovedMaterials(),
                            buildUnapproved(rowException.getProduct(),
                                    issueType == RdBomRowIssueType.MATERIAL_DISABLED),
                            rowNumber);
                    blockedRows++;
                    break;
                default:
                    break;
            }
        }
        result.setBlockedCount(blockedRows);
        result.setIssueCount(parsed.getFormatErrorCount());
        // 格式类问题单独归入 rowIssues（不阻断导入，与导入的部分成功语义一致）
        for (ErpRdBomImportResultVO.FailDetail failDetail : parsed.getFailDetails()) {
            if (!RdBomRowIssueType.FORMAT_ERROR.getCode().equals(failDetail.getIssueType())) {
                continue;
            }
            ErpRdBomPrecheckResultVO.RowIssue issue = new ErpRdBomPrecheckResultVO.RowIssue();
            issue.setRowNumber(failDetail.getRowNumber());
            issue.setMaterialCode(failDetail.getMaterialCode());
            issue.setReason(failDetail.getReason());
            result.getRowIssues().add(issue);
        }
        result.setReadyToImport(result.getMissingMaterials().isEmpty()
                && result.getUnapprovedMaterials().isEmpty());
        return result;
    }

    // ========== 解析主链路（import 与 precheck 共用） ==========

    /**
     * 解析结果载体：有效明细行、行级失败明细与分类异常、表头识别结果
     */
    private static class ParsedWorkbook {

        private final List<ErpRdBomSaveReqVO.Item> items = new ArrayList<>();
        private final List<ErpRdBomImportResultVO.FailDetail> failDetails = new ArrayList<>();
        /** 携带分类的行级异常（不含格式错误；与 failDetails 无顺序对应关系） */
        private final List<RdBomRowParseException> rowExceptions = new ArrayList<>();
        private int totalCount;
        private int successCount;
        private int failCount;
        private int formatErrorCount;
        private SmartHeader header;
        private Map<String, ErpProductDO> materialCodeMap = new HashMap<>();
        private Map<String, ErpProductDO> normalizedCodeMap = new HashMap<>();

        List<ErpRdBomSaveReqVO.Item> getItems() {
            return items;
        }

        List<ErpRdBomImportResultVO.FailDetail> getFailDetails() {
            return failDetails;
        }

        List<RdBomRowParseException> getRowExceptions() {
            return rowExceptions;
        }

        int getTotalCount() {
            return totalCount;
        }

        int getSuccessCount() {
            return successCount;
        }

        int getFailCount() {
            return failCount;
        }

        int getFormatErrorCount() {
            return formatErrorCount;
        }

        SmartHeader getHeader() {
            return header;
        }

        Map<String, ErpProductDO> getMaterialCodeMap() {
            return materialCodeMap;
        }

        Map<String, ErpProductDO> getNormalizedCodeMap() {
            return normalizedCodeMap;
        }
    }

    /**
     * 单次解密 + 单次解析完成编码收集与明细解析。
     * 文件读取失败直接抛出（原"预收集降级"分支在单次读取下无存在意义，已移除）。
     */
    private ParsedWorkbook parseWorkbook(MultipartFile file) {
        ParsedWorkbook parsed = new ParsedWorkbook();
        try {
            byte[] plainContent = fileImportProtector.preparePlainContent(file.getBytes(), file.getOriginalFilename());
            try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(plainContent))) {
                Sheet sheet = workbook.getSheetAt(0);

                int detailHeaderRow = findSmartDetailHeaderRow(sheet);
                Map<String, Integer> colIndex = detailHeaderRow >= 0
                        ? buildSmartColumnIndex(sheet.getRow(detailHeaderRow))
                        : null;

                // 先收集全部物料编码，批量查询（避免逐行查库）
                Set<String> allCodes = collectMaterialCodes(sheet, detailHeaderRow, colIndex);
                parsed.materialCodeMap = buildMaterialCodeMap(allCodes);
                parsed.normalizedCodeMap = buildNormalizedCodeMap(parsed.materialCodeMap);

                parsed.header = tryParseSmartHeader(sheet, parsed.materialCodeMap, parsed.normalizedCodeMap);

                if (detailHeaderRow >= 0) {
                    parseSmartSheet(sheet, detailHeaderRow, colIndex, parsed);
                } else {
                    parseStandardSheet(sheet, parsed);
                }
            }
        } catch (IOException e) {
            log.error("[parseWorkbook] 读取导入文件失败", e);
            throw new RuntimeException("读取导入文件失败：" + e.getMessage());
        }
        return parsed;
    }

    private Set<String> collectMaterialCodes(Sheet sheet, int detailHeaderRow, Map<String, Integer> colIndex) {
        Set<String> allCodes = new HashSet<>();
        int startRow = detailHeaderRow >= 0 ? detailHeaderRow + 1 : 1;
        int codeCol = colIndex != null ? colIndex.getOrDefault("物料编码", COL_MATERIAL_CODE) : COL_MATERIAL_CODE;
        for (int r = startRow; r <= sheet.getLastRowNum(); r++) {
            Row row = sheet.getRow(r);
            if (row == null || isRowEmpty(row)) {
                continue;
            }
            String code = getCellString(row, codeCol);
            if (StrUtil.isNotBlank(code)) {
                allCodes.add(code.trim());
            }
        }
        return allCodes;
    }

    private void parseSmartSheet(Sheet sheet, int detailHeaderRow, Map<String, Integer> colIndex, ParsedWorkbook parsed) {
        Integer levelStart = colIndex.get("层级");
        if (levelStart == null) {
            Integer seqCol = colIndex.get("序号");
            if (seqCol != null) {
                levelStart = seqCol + 1;
            }
        }
        String topMaterialCodeNorm = parsed.header != null && parsed.header.bomCode != null
                ? parsed.header.bomCode.replaceAll("\\s+", "") : null;
        int dataStart = detailHeaderRow + 1;
        if (dataStart <= sheet.getLastRowNum()) {
            Row maybeSub = sheet.getRow(dataStart);
            if (maybeSub != null && isNumericRow(maybeSub, levelStart)) {
                dataStart++;
            }
        }
        int codeCol = colIndex.getOrDefault("物料编码", COL_MATERIAL_CODE);
        for (int i = dataStart; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null || isRowEmpty(row)) {
                continue;
            }
            parsed.totalCount++;
            try {
                ErpRdBomSaveReqVO.Item item = parseSmartRow(row, colIndex, levelStart,
                        parsed.materialCodeMap, parsed.normalizedCodeMap, topMaterialCodeNorm);
                if (item == null) {
                    continue;
                }
                parsed.items.add(item);
                parsed.successCount++;
            } catch (RdBomRowParseException e) {
                recordRowFailure(parsed, i, row, codeCol, e);
            } catch (Exception e) {
                recordFormatFailure(parsed, i, row, codeCol, e.getMessage());
            }
        }
    }

    private void parseStandardSheet(Sheet sheet, ParsedWorkbook parsed) {
        int lastRow = sheet.getLastRowNum();
        parsed.totalCount = lastRow;
        for (int i = 1; i <= lastRow; i++) {
            Row row = sheet.getRow(i);
            if (row == null || isRowEmpty(row)) {
                continue;
            }
            try {
                parsed.items.add(parseItemRow(row, parsed.materialCodeMap));
                parsed.successCount++;
            } catch (RdBomRowParseException e) {
                recordRowFailure(parsed, i, row, COL_MATERIAL_CODE, e);
            } catch (Exception e) {
                recordFormatFailure(parsed, i, row, COL_MATERIAL_CODE, e.getMessage());
            }
        }
    }

    private void recordRowFailure(ParsedWorkbook parsed, int rowIndex, Row row, int codeCol, RdBomRowParseException e) {
        parsed.failCount++;
        e.setRowNumber(rowIndex + 1);
        parsed.rowExceptions.add(e);
        ErpRdBomImportResultVO.FailDetail failDetail = new ErpRdBomImportResultVO.FailDetail();
        failDetail.setRowNumber(rowIndex + 1);
        failDetail.setMaterialCode(StrUtil.isNotBlank(e.getMaterialCode()) ? e.getMaterialCode() : getCellString(row, codeCol));
        failDetail.setReason(e.getMessage());
        failDetail.setIssueType(e.getIssueType().getCode());
        parsed.failDetails.add(failDetail);
    }

    private void recordFormatFailure(ParsedWorkbook parsed, int rowIndex, Row row, int codeCol, String reason) {
        parsed.failCount++;
        parsed.formatErrorCount++;
        ErpRdBomImportResultVO.FailDetail failDetail = new ErpRdBomImportResultVO.FailDetail();
        failDetail.setRowNumber(rowIndex + 1);
        failDetail.setMaterialCode(getCellString(row, codeCol));
        failDetail.setReason(reason);
        failDetail.setIssueType(RdBomRowIssueType.FORMAT_ERROR.getCode());
        parsed.failDetails.add(failDetail);
    }

    // ========== 表头与单元格工具（原有逻辑） ==========

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

    // ========== 行解析（原有逻辑，异常改为携带分类） ==========

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
            material = autoCreateProduct(rawCode, extractProductName(row, colIndex));
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
        Integer materialType = 0;
        Integer typeCol = colIndex.get("物料类型");
        if (typeCol != null) {
            String typeStr = getCellString(row, typeCol).trim();
            if (StrUtil.isNotBlank(typeStr)) {
                try {
                    materialType = Integer.valueOf(typeStr);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("物料类型只支持 0 或 1：" + typeStr);
                }
            }
        }
        ErpRdBomSaveReqVO.Item item = new ErpRdBomSaveReqVO.Item();
        item.setMaterialId(material.getId());
        item.setMaterialType(materialType);
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
            material = autoCreateProduct(materialCode.trim(), getCellString(row, COL_MATERIAL_NAME));
        }
        // 与智能表头路径对齐：标准模板同样强制校验物料启用与审核状态
        material = ensureProductUsable(material);

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

    private String extractProductName(Row row, Map<String, Integer> colIndex) {
        Integer nameCol = colIndex != null ? colIndex.get("产品名称") : null;
        return nameCol != null ? getCellString(row, nameCol) : "";
    }

    // ========== 物料档案查询与校验 ==========

    private Map<String, ErpProductDO> buildMaterialCodeMap(Collection<String> codes) {
        if (CollUtil.isEmpty(codes)) {
            return new HashMap<>();
        }
        List<ErpProductDO> products = productMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ErpProductDO>()
                .in(ErpProductDO::getMaterialCode, codes));
        Map<String, ErpProductDO> map = new HashMap<>();
        for (ErpProductDO product : products) {
            if (StrUtil.isNotBlank(product.getMaterialCode())) {
                map.put(product.getMaterialCode().trim(), product);
            }
        }
        return map;
    }

    private Map<String, ErpProductDO> buildNormalizedCodeMap(Map<String, ErpProductDO> materialCodeMap) {
        Map<String, ErpProductDO> normalizedCodeMap = new HashMap<>();
        for (Map.Entry<String, ErpProductDO> e : materialCodeMap.entrySet()) {
            String norm = e.getKey().replaceAll("\\s+", "");
            normalizedCodeMap.putIfAbsent(norm, e.getValue());
            normalizedCodeMap.putIfAbsent(e.getKey().trim(), e.getValue());
        }
        return normalizedCodeMap;
    }

    /**
     * 自动建档已被禁用：物料必须预先建档并通过审核。此处抛出带分类的行级异常，
     * 由预检查归入待建档清单、由导入记入失败明细。
     */
    private ErpProductDO autoCreateProduct(String rawCode, String materialNameHint) {
        throw new RdBomRowParseException(RdBomRowIssueType.MISSING_MATERIAL,
                "物料编号不存在，需先创建并审核通过：" + rawCode, rawCode, materialNameHint);
    }

    private ErpProductDO autoCreateTopProduct(String rawCode, String productName) {
        throw new RdBomRowParseException(RdBomRowIssueType.MISSING_MATERIAL,
                "顶层物料编号不存在，需先创建并审核通过：" + rawCode, rawCode, productName);
    }

    private ErpProductDO ensureProductUsable(ErpProductDO product) {
        if (CommonStatusEnum.isDisable(product.getStatus())) {
            throw new RdBomRowParseException(RdBomRowIssueType.MATERIAL_DISABLED,
                    "物料未启用：" + product.getName(), product.getMaterialCode(), product.getName(), product);
        }
        if (!ErpAuditStatus.APPROVE.getStatus().equals(product.getAuditStatus())) {
            throw new RdBomRowParseException(RdBomRowIssueType.MATERIAL_NOT_APPROVED,
                    "物料未审核通过（需先走物料审核），物料：" + product.getName() + "，当前审核状态=" + product.getAuditStatus(),
                    product.getMaterialCode(), product.getName(), product);
        }
        return product;
    }

    // ========== 预检查归堆辅助 ==========

    private void addMissingMaterial(List<ErpRdBomPrecheckResultVO.MissingMaterial> list, String materialCode,
                                    String materialName, Integer rowNumber, boolean topLevel) {
        String normKey = materialCode != null ? materialCode.replaceAll("\\s+", "") : "";
        ErpRdBomPrecheckResultVO.MissingMaterial target = list.stream()
                .filter(m -> normKey.equals(m.getMaterialCode() != null ? m.getMaterialCode().replaceAll("\\s+", "") : ""))
                .findFirst().orElse(null);
        if (target == null) {
            target = new ErpRdBomPrecheckResultVO.MissingMaterial();
            target.setMaterialCode(materialCode);
            target.setMaterialName(StrUtil.blankToDefault(materialName, null));
            target.setRowNumbers(new ArrayList<>());
            target.setTopLevel(topLevel);
            list.add(target);
        }
        if (StrUtil.isBlank(target.getMaterialName()) && StrUtil.isNotBlank(materialName)) {
            target.setMaterialName(materialName);
        }
        if (rowNumber != null && !target.getRowNumbers().contains(rowNumber)) {
            target.getRowNumbers().add(rowNumber);
        }
    }

    private ErpRdBomPrecheckResultVO.UnapprovedMaterial buildUnapproved(ErpProductDO product, boolean disabled) {
        ErpRdBomPrecheckResultVO.UnapprovedMaterial item = new ErpRdBomPrecheckResultVO.UnapprovedMaterial();
        item.setMaterialCode(product.getMaterialCode());
        item.setMaterialId(product.getId());
        item.setProductName(product.getName());
        item.setAuditStatus(product.getAuditStatus());
        item.setStatus(product.getStatus());
        item.setDisabled(disabled);
        item.setRowNumbers(new ArrayList<>());
        return item;
    }

    private void addUnapprovedMaterial(List<ErpRdBomPrecheckResultVO.UnapprovedMaterial> list,
                                       ErpRdBomPrecheckResultVO.UnapprovedMaterial item, Integer rowNumber) {
        ErpRdBomPrecheckResultVO.UnapprovedMaterial target = list.stream()
                .filter(m -> java.util.Objects.equals(m.getMaterialId(), item.getMaterialId()))
                .findFirst().orElse(null);
        if (target == null) {
            list.add(item);
            target = item;
        } else if (Boolean.TRUE.equals(item.getDisabled())) {
            target.setDisabled(true);
        }
        if (rowNumber != null && !target.getRowNumbers().contains(rowNumber)) {
            target.getRowNumbers().add(rowNumber);
        }
    }

    // ========== 表头检测辅助（import 与 precheck 共用） ==========

    private Long detectProductId(Long explicit, SmartHeader header) {
        if (explicit != null) {
            return explicit;
        }
        return header != null ? header.productId : null;
    }

    private String detectBomCode(String explicit, SmartHeader header) {
        if (StrUtil.isNotBlank(explicit)) {
            return explicit.trim();
        }
        return header != null && StrUtil.isNotBlank(header.bomCode) ? header.bomCode.trim() : null;
    }

    private String detectVersion(String explicit, SmartHeader header) {
        if (StrUtil.isNotBlank(explicit)) {
            return explicit.trim();
        }
        return header != null && StrUtil.isNotBlank(header.version) ? header.version.trim() : null;
    }

    /**
     * 顶层物料判定，与 precheck 的模拟判定共用同一匹配顺序：
     * 显式 productId → header.productId → header.bomCode 编码匹配 → 缺档抛 MISSING_MATERIAL
     */
    private ErpProductDO resolveTopProduct(Long productId, SmartHeader header, ParsedWorkbook parsed) {
        ErpProductDO topProduct = null;
        if (productId != null) {
            topProduct = productService.getProduct(productId);
        }
        if (topProduct == null && header != null && StrUtil.isNotBlank(header.bomCode)) {
            String topCode = header.bomCode.trim();
            String topNorm = topCode.replaceAll("\\s+", "");
            topProduct = parsed.getNormalizedCodeMap().get(topNorm);
            if (topProduct == null) {
                topProduct = parsed.getMaterialCodeMap().get(topCode);
            }
            if (topProduct == null) {
                String topName = StrUtil.isNotBlank(header.productName) ? header.productName : topCode;
                autoCreateTopProduct(topCode, topName);
            }
        }
        return topProduct;
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
