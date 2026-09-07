package cn.weitee.erp.module.erp.service.rd;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.weitee.erp.framework.common.enums.CommonStatusEnum;
import cn.weitee.erp.framework.excel.core.util.FileImportProtector;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.enums.RdBomRowIssueType;
import cn.weitee.erp.module.erp.enums.rd.RdBomIntegrityIssueType;
import cn.weitee.erp.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.weitee.erp.module.erp.controller.admin.rd.vo.bom.ErpRdBomBaselineDiffVO;
import cn.weitee.erp.module.erp.controller.admin.rd.vo.bom.ErpRdBomImportResultVO;
import cn.weitee.erp.module.erp.controller.admin.rd.vo.bom.ErpRdBomIntegrityIssueRespVO;
import cn.weitee.erp.module.erp.controller.admin.rd.vo.bom.ErpRdBomPrecheckResultVO;
import cn.weitee.erp.module.erp.controller.admin.rd.vo.bom.ErpRdBomSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductCadenceDO;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductCodeHistoryDO;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductDO;
import cn.weitee.erp.module.erp.dal.dataobject.rd.ErpRdBomDO;
import cn.weitee.erp.module.erp.dal.mysql.product.ErpProductCadenceMapper;
import cn.weitee.erp.module.erp.dal.mysql.product.ErpProductCodeHistoryMapper;
import cn.weitee.erp.module.erp.dal.mysql.product.ErpProductMapper;
import cn.weitee.erp.module.erp.enums.ErrorCodeConstants;
import cn.weitee.erp.module.erp.service.product.ErpProductService;
import cn.weitee.erp.module.system.service.notify.ImportNotifyHelper;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
    private ErpProductCodeHistoryMapper codeHistoryMapper;
    @Resource
    private ErpProductCadenceMapper productCadenceMapper;
    @Resource
    private ErpRdBomService rdBomService;

    @Resource
    private ImportNotifyHelper importNotifyHelper;

    /** 模板列定义 */
    private static final String[] HEADERS = {
            "物料编号*", "物料名称", "物料类型(1=自制/装配体,0/空=采购件)", "位号", "用量*", "损耗率", "提前期(天)", "备注", "产品型号"
    };

    private static final int COL_MATERIAL_CODE = 0;
    private static final int COL_MATERIAL_NAME = 1;
    private static final int COL_MATERIAL_TYPE = 2;
    private static final int COL_REFERENCE_DESIGNATOR = 3;
    private static final int COL_USAGE_QTY = 4;
    private static final int COL_LOSS_RATE = 5;
    private static final int COL_LEAD_TIME_DAY = 6;
    private static final int COL_REMARK = 7;

    /**
     * POI DataFormatter 官方明确非线程安全（内部 formats 缓存存在竞态），禁止静态共享实例；
     * ThreadLocal 保证线程内复用、请求间隔离，消除并发导入时的静默格式化错乱
     */
    private static final ThreadLocal<DataFormatter> DATA_FORMATTER = ThreadLocal.withInitial(DataFormatter::new);

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
                    "若文件包含「规格型号」列（或「产品型号」「规格」「型号」），将与产品档案的规格比对，不一致该行导入失败；大小写、空格、全半角差异不影响判定",
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
                                               Boolean updateSupport, boolean allowBaselineMissing, MultipartFile file) {
        ErpRdBomImportResultVO result = new ErpRdBomImportResultVO();
        result.setTotalCount(0);
        result.setSuccessCount(0);
        result.setFailCount(0);
        result.setFailDetails(new ArrayList<>());
        result.setValidationIssues(new ArrayList<>());

        ParsedWorkbook parsed = parseWorkbook(file);
        result.setDuplicateMaterialCodes(parsed.getDuplicateMaterialCodes());
        result.setTotalCount(parsed.getTotalCount());
        result.setSuccessCount(parsed.getSuccessCount());
        result.setFailCount(parsed.getFailCount());
        result.setFailDetails(parsed.getFailDetails());

        if (parsed.getItems().isEmpty()) {
            // 空文件诊断：不给静默的"成功 0 个"，明确告知原因
            if (parsed.getTotalCount() == 0) {
                log.warn("[importRdBom] 未解析到明细行，工作表结构：{}", parsed.getSheetSummary());
                ErpRdBomImportResultVO.FailDetail emptyDetail = new ErpRdBomImportResultVO.FailDetail();
                emptyDetail.setReason("未解析到任何明细行。已扫描：" + parsed.getSheetSummary()
                        + "。明细表头需包含「物料编码」与「数量」字样");
                emptyDetail.setIssueType(RdBomRowIssueType.FILE_EMPTY.getCode());
                result.getFailDetails().add(emptyDetail);
                result.setFailCount(1);
            }
            notifyRdBomImportResult(result);
            return result;
        }

        Long detectedProductId = detectProductId(productId, parsed.getHeader());
        String detectedBomCode = detectBomCode(bomCode, parsed.getHeader());

        ErpProductDO topProduct;
        try {
            topProduct = resolveTopProduct(detectedProductId, parsed.getHeader(), parsed);
        } catch (RdBomRowParseException e) {
            // 顶层缺档与顶层停用/未审核同属"导入入口校验"，统一转业务错误码；
            // 否则 RdBomRowParseException 会逃逸为 500，用户看不到"编码对应不上产品库"的具体提示
            if (e.getIssueType() == RdBomRowIssueType.MISSING_MATERIAL) {
                throw exception(ErrorCodeConstants.RD_BOM_TOP_MATERIAL_NOT_EXISTS,
                        StrUtil.blankToDefault(e.getMaterialCode(), ""));
            }
            throw e;
        }
        if (topProduct == null) {
            throw exception(ErrorCodeConstants.PRODUCT_NOT_EXISTS);
        }
        // 导入路径的顶层物料校验转业务异常（行级异常仅供明细归堆，顶层失败应返回可理解的错误码而非 500）
        try {
            topProduct = ensureProductUsable(topProduct);
        } catch (RdBomRowParseException e) {
            if (e.getIssueType() == RdBomRowIssueType.MATERIAL_DISABLED) {
                throw exception(ErrorCodeConstants.RD_BOM_TOP_MATERIAL_DISABLED, topProduct.getName());
            }
            throw exception(ErrorCodeConstants.RD_BOM_TOP_MATERIAL_NOT_APPROVED, topProduct.getName());
        }
        Long finalProductId = topProduct.getId();
        String finalBomCode = StrUtil.isBlank(detectedBomCode) ? topProduct.getMaterialCode() : detectedBomCode.trim();
        if (StrUtil.isBlank(finalBomCode)) {
            finalBomCode = "RD-BOM-" + System.currentTimeMillis();
        }
        ErpRdBomDO duplicateBom = rdBomService.getRdBomByIdentity(finalProductId, finalBomCode,
                null);
        if (duplicateBom != null) {
            throw exception(ErrorCodeConstants.RD_BOM_DUPLICATE, finalBomCode, "草稿");
        }

        ErpRdBomSaveReqVO saveReq = new ErpRdBomSaveReqVO();
        saveReq.setProductId(finalProductId);
        saveReq.setBomCode(finalBomCode);
        // 导入只创建草稿；版本号在发布/升版流程中生成，兼容保留旧请求参数但不落库。
        saveReq.setVersion(null);
        saveReq.setRemark(StrUtil.isBlank(remark) ? null : remark.trim());
        saveReq.setItems(parsed.getItems());

        // 行级完整性校验（部分成功语义）：ERROR 行剔入 failDetails（带 Excel 行号），合法行才落库；
        // 不再复用 createRdBom 的整单断言，避免一行缺位号导致整单回滚且无行号诊断
        List<ErpRdBomIntegrityIssueRespVO> rowIssues = rdBomService.validateRdBomItemsIntegrity(parsed.getItems());
        Map<Integer, ErpRdBomIntegrityIssueRespVO> errorRowByIndex = rowIssues.stream()
                .filter(issue -> "ERROR".equals(issue.getSeverity()))
                .collect(Collectors.toMap(ErpRdBomIntegrityIssueRespVO::getRowIndex, issue -> issue, (a, b) -> a));
        List<ErpRdBomSaveReqVO.Item> validItems;
        if (errorRowByIndex.isEmpty()) {
            validItems = parsed.getItems();
        } else {
            validItems = new ArrayList<>();
            for (int i = 0; i < parsed.getItems().size(); i++) {
                ErpRdBomIntegrityIssueRespVO error = errorRowByIndex.get(i + 1);
                if (error == null) {
                    validItems.add(parsed.getItems().get(i));
                    continue;
                }
                parsed.failDetails.add(buildIntegrityFailDetail(parsed, i, error));
            }
            parsed.successCount -= errorRowByIndex.size();
            parsed.failCount += errorRowByIndex.size();
        }

        // 回写最终计数（剔行后与 failDetails 一致）
        result.setSuccessCount(parsed.getSuccessCount());
        result.setFailCount(parsed.getFailCount());
        result.setFailDetails(parsed.getFailDetails());

        // 增量差异警示：与该成品最新版 BOM 比对，缺失物料默认硬拦；人工勾选确认后放行
        ErpRdBomBaselineDiffVO baselineDiff = rdBomService.diffImportAgainstLatest(finalProductId,
                validItems.stream().map(ErpRdBomSaveReqVO.Item::getMaterialId).collect(Collectors.toList()));
        if (baselineDiff != null && CollUtil.isNotEmpty(baselineDiff.getMissingItems())
                && !allowBaselineMissing) {
            String sampleCodes = baselineDiff.getMissingItems().stream()
                    .limit(5)
                    .map(ErpRdBomBaselineDiffVO.MissingItem::getMaterialCode)
                    .collect(Collectors.joining("、"));
            throw exception(ErrorCodeConstants.RD_BOM_BASELINE_MISSING,
                    baselineDiff.getMissingItems().size(), sampleCodes);
        }
        result.setBaselineDiff(baselineDiff);

        if (validItems.isEmpty()) {
            // 全部行完整性不通过：不创建 BOM，问题明细已在 failDetails
            result.setValidationIssues(rowIssues);
            notifyRdBomImportResult(result);
            return result;
        }
        saveReq.setItems(validItems);
        Long bomId = rdBomService.createRdBomForImport(saveReq);
        result.setBomId(bomId);

        List<ErpRdBomIntegrityIssueRespVO> issues = rdBomService.validateRdBomIntegrity(bomId);
        result.setValidationIssues(issues);
        notifyRdBomImportResult(result);
        return result;
    }

    /** 研发BOM导入结果站内信通知：覆盖全部正常 return 出口（含部分成功 / 全部失败），异常路径由调用方抛出不发送 */
    private void notifyRdBomImportResult(ErpRdBomImportResultVO result) {
        // 重复编码属于提交前必须人工核对的提醒，置顶以避免明细过多时被站内信摘要截断。
        List<String> samples = new ArrayList<>();
        if (CollUtil.isNotEmpty(result.getDuplicateMaterialCodes())) {
            String duplicateSummary = result.getDuplicateMaterialCodes().stream()
                    .map(duplicate -> duplicate.getMaterialCode() + "（第"
                            + duplicate.getRowNumbers().stream().map(String::valueOf).collect(Collectors.joining("、"))
                            + "行）")
                    .collect(Collectors.joining("、"));
            samples.add("产品编码重复，请再次核对清单后进行提交：" + duplicateSummary);
        }
        // 明细 = 解析失败行 + 完整性问题行，统一"第N行 编码：原因"格式，站内信中逐行可复制
        samples.addAll(result.getFailDetails().stream()
                .map(d -> "第" + d.getRowNumber() + "行 " + StrUtil.blankToDefault(d.getMaterialCode(), "")
                        + "：" + d.getReason())
                .collect(Collectors.toList()));
        for (ErpRdBomIntegrityIssueRespVO issue : result.getValidationIssues()) {
            samples.add("第" + issue.getRowIndex() + "行 "
                    + StrUtil.blankToDefault(resolveMaterialCode(issue.getMaterialId()), "")
                    + "：[" + ("ERROR".equals(issue.getSeverity()) ? "错误" : "警告") + "] "
                    + issue.getMessage());
        }
        importNotifyHelper.sendImportResultWithFullDetails("erp_import_result_rd_bom", "研发BOM导入",
                result.getTotalCount(), result.getSuccessCount(), result.getFailCount(), samples);
    }

    private ErpRdBomImportResultVO.FailDetail buildIntegrityFailDetail(ParsedWorkbook parsed, int itemIndex,
                                                                       ErpRdBomIntegrityIssueRespVO error) {
        ErpRdBomImportResultVO.FailDetail failDetail = new ErpRdBomImportResultVO.FailDetail();
        List<Integer> excelRows = parsed.getItemExcelRows();
        failDetail.setRowNumber(itemIndex < excelRows.size() ? excelRows.get(itemIndex) : null);
        failDetail.setMaterialCode(resolveMaterialCode(error.getMaterialId()));
        // issueType 已标明 INTEGRITY_ERROR 类别，reason 直接给具体原因，不再重复类别前缀
        String reason = error.getMessage();
        if (StrUtil.isBlank(reason)) {
            reason = RdBomIntegrityIssueType.defaultMessageOf(error.getIssueType());
        }
        failDetail.setReason(reason);
        failDetail.setIssueType(RdBomRowIssueType.INTEGRITY_ERROR.getCode());
        return failDetail;
    }

    private String resolveMaterialCode(Long materialId) {
        if (materialId == null) {
            return null;
        }
        ErpProductRespVO product = productService.getProductVOMap(List.of(materialId)).get(materialId);
        return product != null ? product.getMaterialCode() : String.valueOf(materialId);
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
        result.setDuplicateMaterialCodes(parsed.getDuplicateMaterialCodes());

        // 空文件诊断先行：0 行明细时给出可操作的原因与工作表结构摘要，而不是静默全 0
        if (parsed.getTotalCount() == 0) {
            log.warn("[precheckRdBom] 未解析到明细行，工作表结构：{}", parsed.getSheetSummary());
            addRowIssue(result.getRowIssues(), null, null,
                    "未解析到任何明细行。已扫描：" + parsed.getSheetSummary()
                            + "。明细表头需包含「物料编码」与「数量」字样",
                    RdBomRowIssueType.FILE_EMPTY.getCode());
            result.setReadyToImport(false);
            return result;
        }

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
            // 顶层编码不在明细行收集范围内（表头信息不属于明细），Map 未命中时兜底直查数据库
            if (topProduct == null) {
                topProduct = productMapper.selectOne(ErpProductDO::getMaterialCode, topNorm);
                if (topProduct == null) {
                    topProduct = productMapper.selectOne(ErpProductDO::getMaterialCode, topCode);
                }
                if (topProduct == null) {
                    // 编码沿革兜底：顶层物料改过码时，历史文件里的旧码仍应对上号
                    topProduct = productMapper.selectByCodeOrHistory(topNorm);
                }
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
            String resolvedBomCode = StrUtil.isBlank(detectedHeader.getBomCode())
                    ? topProduct.getMaterialCode() : detectedHeader.getBomCode().trim();
            if (StrUtil.isNotBlank(resolvedBomCode)) {
                ErpRdBomDO duplicateBom = rdBomService.getRdBomByIdentity(topProduct.getId(), resolvedBomCode,
                        null);
                if (duplicateBom != null) {
                    result.setDuplicateBom(toDuplicateBom(duplicateBom));
                }
            }
        }
        result.setDetectedHeader(detectedHeader);

        // 增量差异警示：与该成品最新版 BOM 比对，缺失物料默认硬拦（预检阶段同样视为阻断）
        ErpRdBomBaselineDiffVO baselineDiff = rdBomService.diffImportAgainstLatest(detectedHeader.getProductId(),
                parsed.getItems().stream().map(ErpRdBomSaveReqVO.Item::getMaterialId).collect(Collectors.toList()));
        result.setBaselineDiff(baselineDiff);

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
                case CADENCE_DATA_INCOMPLETE:
                    addRowIssue(result.getRowIssues(), rowNumber, rowException.getMaterialCode(),
                            rowException.getMessage(), issueType.getCode());
                    blockedRows++;
                    break;
                case SPEC_MISMATCH:
                    addRowIssue(result.getRowIssues(), rowNumber, rowException.getMaterialCode(),
                            rowException.getMessage(), issueType.getCode());
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
            issue.setIssueType(failDetail.getIssueType());
            result.getRowIssues().add(issue);
        }
        // 缺料差异默认硬拦：与导入路径同一判定（导入时未勾选放行同样拒绝）
        boolean baselineMissingBlocked = baselineDiff != null && CollUtil.isNotEmpty(baselineDiff.getMissingItems());
        result.setReadyToImport(result.getMissingMaterials().isEmpty()
                && result.getUnapprovedMaterials().isEmpty()
                && result.getRowIssues().isEmpty()
                && result.getDuplicateBom() == null
                && !baselineMissingBlocked);
        notifyPrecheckIssues(result, baselineDiff);
        return result;
    }

    private ErpRdBomPrecheckResultVO.DuplicateBom toDuplicateBom(ErpRdBomDO bom) {
        ErpRdBomPrecheckResultVO.DuplicateBom duplicate = new ErpRdBomPrecheckResultVO.DuplicateBom();
        duplicate.setId(bom.getId());
        duplicate.setBomCode(bom.getBomCode());
        duplicate.setVersion(bom.getVersion());
        duplicate.setStatus(bom.getStatus());
        return duplicate;
    }

    /**
     * 预检问题站内信：存在待建档/待审核/行问题时推送，明细逐行可复制修改；
     * 全部通过不推送——自动预检每次上传都会触发，避免站内信轰炸
     */
    private void notifyPrecheckIssues(ErpRdBomPrecheckResultVO result, ErpRdBomBaselineDiffVO baselineDiff) {
        if (Boolean.TRUE.equals(result.getReadyToImport())) {
            return;
        }
        List<String> samples = new ArrayList<>();
        for (ErpRdBomPrecheckResultVO.MissingMaterial m : result.getMissingMaterials()) {
            String rowText = CollUtil.isEmpty(m.getRowNumbers()) ? ""
                    : "（行" + m.getRowNumbers().stream().map(String::valueOf).collect(Collectors.joining("、")) + "）";
            samples.add("待建档 " + StrUtil.blankToDefault(m.getMaterialCode(), "") + rowText
                    + "：" + StrUtil.blankToDefault(m.getMaterialName(), "产品库无此编码，请先建档并审核通过"));
        }
        for (ErpRdBomPrecheckResultVO.UnapprovedMaterial u : result.getUnapprovedMaterials()) {
            String rowText = CollUtil.isEmpty(u.getRowNumbers()) ? ""
                    : "（行" + u.getRowNumbers().stream().map(String::valueOf).collect(Collectors.joining("、")) + "）";
            samples.add((Boolean.TRUE.equals(u.getDisabled()) ? "已停用 " : "未审核通过 ")
                    + StrUtil.blankToDefault(u.getMaterialCode(), "") + rowText
                    + "：请先完成物料审核或启用");
        }
        for (ErpRdBomPrecheckResultVO.RowIssue issue : result.getRowIssues()) {
            samples.add((issue.getRowNumber() != null ? "第" + issue.getRowNumber() + "行 " : "")
                    + StrUtil.blankToDefault(issue.getMaterialCode(), "")
                    + (StrUtil.isNotBlank(issue.getMaterialCode()) ? "：" : "")
                    + issue.getReason());
        }
        if (baselineDiff != null && CollUtil.isNotEmpty(baselineDiff.getMissingItems())) {
            String codes = baselineDiff.getMissingItems().stream()
                    .map(ErpRdBomBaselineDiffVO.MissingItem::getMaterialCode)
                    .collect(Collectors.joining("、"));
            samples.add("较最新版 BOM（" + StrUtil.blankToDefault(baselineDiff.getBaselineVersion(), "未版本化")
                    + "）减少 " + baselineDiff.getMissingItems().size() + " 个物料：" + codes
                    + "。如属正常改版，请在导入弹窗勾选「确认放行」");
        }
        if (samples.isEmpty()) {
            return;
        }
        importNotifyHelper.sendImportResult("erp_import_result_rd_bom", "研发BOM导入预检",
                result.getTotalCount() == null ? 0 : result.getTotalCount(),
                result.getReadyCount() == null ? 0 : result.getReadyCount(),
                samples.size(), samples);
    }

    // ========== 解析主链路（import 与 precheck 共用） ==========

    /**
     * 解析结果载体：有效明细行、行级失败明细与分类异常、表头识别结果
     */
    private static class ParsedWorkbook {

        private final List<ErpRdBomSaveReqVO.Item> items = new ArrayList<>();
        /** 与 items 一一对应的 Excel 行号（1-based，含表头），供完整性剔行回写行号 */
        private final List<Integer> itemExcelRows = new ArrayList<>();
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
        private Map<Long, ErpProductCadenceDO> cadenceMap = new HashMap<>();
        /** 文件明细中出现的产品编码及其 Excel 行号，用于重复编码提醒。 */
        private final Map<String, List<Integer>> materialCodeRows = new LinkedHashMap<>();
        /** 各工作表结构摘要（表名/行数/是否找到明细表头），0 行明细时拼入诊断文案 */
        private String sheetSummary = "";

        List<ErpRdBomSaveReqVO.Item> getItems() {
            return items;
        }

        List<Integer> getItemExcelRows() {
            return itemExcelRows;
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

        Map<Long, ErpProductCadenceDO> getCadenceMap() {
            return cadenceMap;
        }

        String getSheetSummary() {
            return sheetSummary;
        }

        void recordMaterialCode(String materialCode, int rowNumber) {
            if (StrUtil.isBlank(materialCode)) {
                return;
            }
            String displayCode = materialCode.trim();
            String normalizedCode = displayCode.replaceAll("\\s+", "");
            materialCodeRows.computeIfAbsent(normalizedCode, key -> new ArrayList<>()).add(rowNumber);
        }

        List<ErpRdBomPrecheckResultVO.DuplicateMaterialCode> getDuplicateMaterialCodes() {
            return materialCodeRows.entrySet().stream()
                    .filter(entry -> entry.getValue().size() > 1)
                    .map(entry -> {
                        ErpRdBomPrecheckResultVO.DuplicateMaterialCode duplicate =
                                new ErpRdBomPrecheckResultVO.DuplicateMaterialCode();
                        duplicate.setMaterialCode(entry.getKey());
                        duplicate.setRowNumbers(entry.getValue());
                        return duplicate;
                    })
                    .toList();
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
                // 多工作表选择：封面/版本履历页常在明细之前，取第一张含智能明细表头的表；
                // 全部无明细表头时退回第一张非空表，且该表必须具备标准模板表头（「物料编号」），否则按空文件诊断
                Sheet sheet = null;
                int detailHeaderRow = -1;
                int selectedIndex = 0;
                Sheet firstNonEmpty = null;
                int firstNonEmptyIndex = 0;
                StringBuilder summaryBuilder = new StringBuilder();
                for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                    Sheet candidate = workbook.getSheetAt(i);
                    if (candidate.getLastRowNum() + 1 > MAX_IMPORT_ROWS + 1) {
                        // 含表头在内超限即拒绝：XSSF DOM 解析内存膨胀数十倍，超大文件必须入口拦截
                        throw exception(ErrorCodeConstants.RD_BOM_IMPORT_ROWS_EXCEED, MAX_IMPORT_ROWS);
                    }
                    int headerRow = findSmartDetailHeaderRow(candidate);
                    summaryBuilder.append(workbook.getSheetName(i))
                            .append("(").append(candidate.getLastRowNum() + 1).append("行")
                            .append(headerRow >= 0 ? ",明细表头@第" + (headerRow + 1) + "行" : ",无明细表头")
                            .append(")");
                    if (i < workbook.getNumberOfSheets() - 1) {
                        summaryBuilder.append("；");
                    }
                    if (firstNonEmpty == null && !isSheetEmpty(candidate)) {
                        firstNonEmpty = candidate;
                        firstNonEmptyIndex = i;
                    }
                    if (headerRow >= 0) {
                        sheet = candidate;
                        detailHeaderRow = headerRow;
                        selectedIndex = i;
                        break;
                    }
                }
                parsed.sheetSummary = summaryBuilder.toString();
                if (sheet == null) {
                    sheet = firstNonEmpty != null ? firstNonEmpty : workbook.getSheetAt(0);
                    selectedIndex = firstNonEmptyIndex;
                    if (!standardHeaderPresent(sheet)) {
                        // 无智能明细表头也无标准表头：交由空文件诊断给出可操作原因
                        return parsed;
                    }
                }

                Map<String, Integer> colIndex = detailHeaderRow >= 0
                        ? buildSmartColumnIndex(sheet.getRow(detailHeaderRow))
                        : null;

                // 先收集全部物料编码，批量查询（避免逐行查库）
                Set<String> allCodes = collectMaterialCodes(sheet, detailHeaderRow, colIndex);
                parsed.materialCodeMap = buildMaterialCodeMap(allCodes);
                parsed.normalizedCodeMap = buildNormalizedCodeMap(parsed.materialCodeMap);
                parsed.cadenceMap = buildCadenceMap(parsed.materialCodeMap.values());

                parsed.header = tryParseSmartHeader(sheet, parsed.materialCodeMap, parsed.normalizedCodeMap);
                if (parsed.header == null || (parsed.header.bomCode == null && parsed.header.productId == null)) {
                    // BOM 头信息可能写在明细之前的封面/履历页：向更前的表找关键字段（编码或产品 id）齐全的头
                    for (int i = 0; i < selectedIndex; i++) {
                        SmartHeader coverHeader = tryParseSmartHeader(workbook.getSheetAt(i),
                                parsed.materialCodeMap, parsed.normalizedCodeMap);
                        if (coverHeader != null && (coverHeader.bomCode != null || coverHeader.productId != null)) {
                            parsed.header = coverHeader;
                            break;
                        }
                    }
                }

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
            parsed.recordMaterialCode(getCellString(row, codeCol), i + 1);
            try {
                ErpRdBomSaveReqVO.Item item = parseSmartRow(row, colIndex, levelStart,
                        parsed.materialCodeMap, parsed.normalizedCodeMap, parsed.cadenceMap, topMaterialCodeNorm);
                if (item == null) {
                    continue;
                }
                parsed.items.add(item);
                parsed.itemExcelRows.add(i + 1);
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
        Integer specCol = detectStandardSpecColumn(sheet);
        for (int i = 1; i <= lastRow; i++) {
            Row row = sheet.getRow(i);
            if (row == null || isRowEmpty(row)) {
                continue;
            }
            parsed.recordMaterialCode(getCellString(row, COL_MATERIAL_CODE), i + 1);
            try {
                parsed.items.add(parseItemRow(row, parsed.materialCodeMap, parsed.cadenceMap, specCol));
                parsed.itemExcelRows.add(i + 1);
                parsed.successCount++;
            } catch (RdBomRowParseException e) {
                recordRowFailure(parsed, i, row, COL_MATERIAL_CODE, e);
            } catch (Exception e) {
                recordFormatFailure(parsed, i, row, COL_MATERIAL_CODE, e.getMessage());
            }
        }
    }

    /**
     * 标准模板路径的规格列表头探测：仅扫描第 0 行（标准模板唯一表头行），
     * 整格文本精确匹配常见规格列名集合；不命中则视为无规格列，跳过校验
     */
    private Integer detectStandardSpecColumn(Sheet sheet) {
        if (sheet.getLastRowNum() < 0) {
            return null;
        }
        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            return null;
        }
        for (int c = 0; c < headerRow.getLastCellNum(); c++) {
            if (isSpecHeader(getCellString(headerRow, c))) {
                return c;
            }
        }
        return null;
    }

    /** 型号列允许使用产品列表导出的「型号编码」等带业务前缀/后缀的表头。 */
    private boolean isSpecHeader(String header) {
        String normalized = header == null ? "" : header.replace("*", "").replaceAll("\\s+", "").trim();
        return StrUtil.isNotBlank(normalized)
                && !normalized.contains("替代")
                && (normalized.contains("规格") || normalized.contains("型号"));
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

    /** 判断工作表是否无任何内容（全空行或空表） */
    private boolean isSheetEmpty(Sheet sheet) {
        if (sheet == null || sheet.getLastRowNum() < 0) {
            return true;
        }
        for (int r = 0; r <= sheet.getLastRowNum(); r++) {
            if (!isRowEmpty(sheet.getRow(r))) {
                return false;
            }
        }
        return true;
    }

    /** 标准模板表头特征：表头行存在以「物料编号」开头的列 */
    private boolean standardHeaderPresent(Sheet sheet) {
        if (sheet == null || sheet.getLastRowNum() < 0) {
            return false;
        }
        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            return false;
        }
        for (int c = 0; c < headerRow.getLastCellNum(); c++) {
            if (getCellString(headerRow, c).replace("*", "").trim().startsWith("物料编号")) {
                return true;
            }
        }
        return false;
    }

    /** 明细表头扫描窗口上限：真实文件头区（封面/版本履历）可能很长，全表扫描代价可接受，仅保留防御性上限 */
    private static final int DETAIL_HEADER_SCAN_LIMIT = 200;

    private int findSmartDetailHeaderRow(Sheet sheet) {
        for (int r = 0; r <= Math.min(DETAIL_HEADER_SCAN_LIMIT, sheet.getLastRowNum()); r++) {
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
            if (t.contains("物料编码") && !t.contains("替代")) map.put("物料编码", c);
            else if (t.contains("产品名称") && !t.contains("替代")) map.put("产品名称", c);
            else if (t.equals("数量") || t.contains("数量")) map.put("数量", c);
            else if (t.equals("单位")) map.put("单位", c);
            else if (t.contains("物料位置") || t.contains("位号")) map.put("物料位置", c);
            else if (t.equals("备注")) map.put("备注", c);
            else if (t.contains("替代物料编码") || t.contains("替代")) map.put("替代物料编码", c);
            else if (isSpecHeader(t)) map.put("规格型号", c);
            else if (t.contains("层级")) map.put("层级", c);
            else if (t.equals("序号")) map.put("序号", c);
            else if (t.equals("封装")) map.put("封装", c);
        }
        return map;
    }

    /**
     * 层级子表头行判定（"1 2 3 4"行）：除层级区数字外**整行皆空**才算。
     * 不能仅凭层级区出现 1-4 就判定——数据行（如层级=1 的顶层行）同样含这些数字，
     * 误判会把真实数据行当子表头静默跳过，绕过全部行级校验（历史缺陷根因）
     */
    private boolean isNumericRow(Row row, Integer levelStart) {
        if (row == null || levelStart == null) return false;
        boolean hasLevelNumber = false;
        for (int c = 0; c < row.getLastCellNum(); c++) {
            String v = getCellString(row, c).trim();
            if (StrUtil.isBlank(v)) {
                continue;
            }
            if (c >= levelStart && c < levelStart + 4 && v.matches("[1-4]")) {
                hasLevelNumber = true;
                continue;
            }
            return false;
        }
        return hasLevelNumber;
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
                                                Map<Long, ErpProductCadenceDO> cadenceMap, String topCodeNorm) {
        Integer codeCol = colIndex.get("物料编码");
        if (codeCol == null) codeCol = COL_MATERIAL_CODE;
        String rawCode = getCellString(row, codeCol).trim();
        if (StrUtil.isBlank(rawCode)) {
            // 无编码必须失败：禁止扫描同行其他格子"猜码救活"——替代料列/层级号/备注数字
            // 都可能碰巧命中产品库，导致错误物料被静默顶替导入（历史缺陷根因，勿恢复兜底）
            throw new IllegalArgumentException("物料编码不能为空");
        }
        String normCode = rawCode.replaceAll("\\s+", "");
        ErpProductDO material = normMap.get(normCode);
        if (material == null) material = codeMap.get(rawCode.trim());
        if (material == null) {
            material = normMap.get(normCode);
        }
        if (material == null) {
            // Map 未命中时兜底直查数据库：预收集的 Map 可能因列位漂移/合并单元格漏收该编码，
            // 直接按编码查库（norm 优先，原始串兜底），命中则回填 Map 供后续行复用
            material = productMapper.selectOne(ErpProductDO::getMaterialCode, normCode);
            if (material == null) {
                material = productMapper.selectOne(ErpProductDO::getMaterialCode, rawCode.trim());
            }
            if (material == null) {
                // 编码沿革兜底：物料改过码时，历史文件里的旧码仍应对上号
                material = productMapper.selectByCodeOrHistory(normCode);
            }
            if (material != null) {
                codeMap.put(rawCode.trim(), material);
                normMap.put(normCode, material);
                String norm2 = rawCode.trim().replaceAll("\\s+", "");
                normMap.putIfAbsent(norm2, material);
            }
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
        Integer specCol = colIndex.get("规格型号");
        String excelSpec = specCol != null ? getCellString(row, specCol).trim() : null;
        ensureSpecMatched(material, excelSpec, specCol != null);
        // 顶层行跳过必须在全部校验之后：跳过仅表示"不计入明细"（避免 BOM 自引用），
        // 绝不能豁免物料可用性与规格校验——否则顶层行改错型号/规格会被静默放过（历史缺陷根因）
        if (topCodeNorm != null && normCode.equals(topCodeNorm)) {
            return null;
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
                // 显式校验取值：只放行 "0"/"1"，防止任意整数静默入库污染下游 MRP 展开
                if (!"0".equals(typeStr) && !"1".equals(typeStr)) {
                    throw new IllegalArgumentException("物料类型只支持 0 或 1：" + typeStr);
                }
                materialType = Integer.valueOf(typeStr);
            }
        }
        ensureCadenceComponentReady(material, cadenceMap);
        ErpRdBomSaveReqVO.Item item = new ErpRdBomSaveReqVO.Item();
        item.setMaterialId(material.getId());
        item.setMaterialStandard(StrUtil.isNotBlank(excelSpec) ? excelSpec : material.getStandard());
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

    private ErpRdBomSaveReqVO.Item parseItemRow(Row row, Map<String, ErpProductDO> materialCodeMap,
                                                Map<Long, ErpProductCadenceDO> cadenceMap, Integer specCol) {
        String materialCode = getCellString(row, COL_MATERIAL_CODE);
        if (StrUtil.isBlank(materialCode)) {
            throw new IllegalArgumentException("物料编号不能为空");
        }
        ErpProductDO material = materialCodeMap.get(materialCode.trim());
        if (material == null) {
            // Map 未命中时兜底直查数据库（与 parseSmartRow 同因：预收集可能漏收）
            String normCode = materialCode.trim().replaceAll("\\s+", "");
            material = productMapper.selectOne(ErpProductDO::getMaterialCode, normCode);
            if (material == null) {
                material = productMapper.selectOne(ErpProductDO::getMaterialCode, materialCode.trim());
            }
            if (material == null) {
                // 编码沿革兜底：物料改过码时，历史文件里的旧码仍应对上号
                material = productMapper.selectByCodeOrHistory(normCode);
            }
            if (material != null) {
                materialCodeMap.put(materialCode.trim(), material);
            }
        }
        if (material == null) {
            material = autoCreateProduct(materialCode.trim(), getCellString(row, COL_MATERIAL_NAME));
        }
        // 与智能表头路径对齐：标准模板同样强制校验物料启用与审核状态
        material = ensureProductUsable(material);
        String excelSpec = specCol != null ? getCellString(row, specCol).trim() : null;
        ensureSpecMatched(material, excelSpec, specCol != null);

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
            // 显式校验取值：只放行 "0"/"1"，防止任意整数静默入库污染下游 MRP 展开
            String normalizedType = typeStr.trim();
            if (!"0".equals(normalizedType) && !"1".equals(normalizedType)) {
                throw new IllegalArgumentException("物料类型只支持 0 或 1：" + typeStr);
            }
            materialType = Integer.valueOf(normalizedType);
        }
        ensureCadenceComponentReady(material, cadenceMap);

        ErpRdBomSaveReqVO.Item item = new ErpRdBomSaveReqVO.Item();
        item.setMaterialId(material.getId());
        item.setMaterialStandard(StrUtil.isNotBlank(excelSpec) ? excelSpec : material.getStandard());
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

    /** 单次 IN 查询分批大小，防止超大 IN 拖垮数据库 */
    private static final int IN_BATCH_SIZE = 500;
    /** 单次导入明细行数上限，防止超大文件 DOM 解析 OOM */
    private static final int MAX_IMPORT_ROWS = 5000;

    private Map<String, ErpProductDO> buildMaterialCodeMap(Collection<String> codes) {
        if (CollUtil.isEmpty(codes)) {
            return new HashMap<>();
        }
        List<String> codeList = new ArrayList<>(codes);
        Map<String, ErpProductDO> map = new HashMap<>();
        // 分批 IN 查询：导入编码数可达数千级，单条巨型 IN 有慢查询/参数上限风险
        for (int from = 0; from < codeList.size(); from += IN_BATCH_SIZE) {
            List<String> batch = codeList.subList(from, Math.min(from + IN_BATCH_SIZE, codeList.size()));
            List<ErpProductDO> products = productMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ErpProductDO>()
                    .in(ErpProductDO::getMaterialCode, batch));
            for (ErpProductDO product : products) {
                if (StrUtil.isNotBlank(product.getMaterialCode())) {
                    map.put(product.getMaterialCode().trim(), product);
                }
            }
        }
        fillHistoryCodeMatches(codeList, map);
        return map;
    }

    /**
     * 编码沿革兜底：当前码未命中的编码，按沿革旧码反查物料（改码后历史 BOM 文件仍可对上号）。
     * 仅对尚未命中的编码补查，避免重复查询。
     */
    private void fillHistoryCodeMatches(Collection<String> codes, Map<String, ErpProductDO> map) {
        List<String> missing = codes.stream()
                .map(String::trim)
                .filter(StrUtil::isNotBlank)
                .filter(code -> !map.containsKey(code))
                .distinct()
                .toList();
        if (missing.isEmpty()) {
            return;
        }
        for (int from = 0; from < missing.size(); from += IN_BATCH_SIZE) {
            List<String> batch = missing.subList(from, Math.min(from + IN_BATCH_SIZE, missing.size()));
            Map<Long, ErpProductDO> productMap = productMapper.selectListByCodesOrHistoryCodes(batch)
                    .stream()
                    .collect(java.util.stream.Collectors.toMap(ErpProductDO::getId, p -> p, (a, b) -> a));
            for (ErpProductCodeHistoryDO history : codeHistoryMapper.selectListByOldCodes(batch)) {
                ErpProductDO product = productMap.get(history.getProductId());
                if (product != null && !map.containsKey(history.getOldCode())) {
                    map.put(history.getOldCode(), product);
                }
            }
        }
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

    private Map<Long, ErpProductCadenceDO> buildCadenceMap(Collection<ErpProductDO> products) {
        Set<Long> productIds = products.stream()
                .map(ErpProductDO::getId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
        if (productIds.isEmpty()) {
            return new HashMap<>();
        }
        return productCadenceMapper.selectListByProductIds(productIds).stream()
                .collect(Collectors.toMap(ErpProductCadenceDO::getProductId, item -> item, (a, b) -> a));
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

    /**
     * 规格型号行级硬校验：文件携带规格列时该行必须填写且与产品档案 standard 一致（宽松归一化比对）。
     * - 文件无规格列（历史 8 列模板）：整体豁免，不校验；
     * - 列存在但行为空：视为缺失，硬拦——型号缺失即"编码与产品档案无法核对"，属 BOM 质量问题；
     * - 档案未维护规格而 Excel 有值 / 双方不一致：硬拦，防止错规格物料进入 BOM。
     */
    private void ensureSpecMatched(ErpProductDO material, String excelSpec, boolean specColumnPresent) {
        if (!specColumnPresent) {
            return;
        }
        if (StrUtil.isBlank(excelSpec)) {
            throw new RdBomRowParseException(RdBomRowIssueType.SPEC_MISMATCH,
                    "产品型号缺失：本文件含「产品型号/规格」列但该行未填写，请补全后重试：" + material.getMaterialCode(),
                    material.getMaterialCode(), material.getName(), material);
        }
        String archiveSpec = material.getStandard();
        if (StrUtil.isBlank(archiveSpec)) {
            throw new RdBomRowParseException(RdBomRowIssueType.SPEC_MISMATCH,
                    "规格型号不一致：文件=\"" + excelSpec.trim() + "\"，产品档案未维护规格型号，请先补全物料档案",
                    material.getMaterialCode(), material.getName(), material);
        }
        if (!normalizeSpec(excelSpec).equals(normalizeSpec(archiveSpec))) {
            throw new RdBomRowParseException(RdBomRowIssueType.SPEC_MISMATCH,
                    "规格型号不一致：文件=\"" + excelSpec.trim() + "\"，产品档案=\"" + archiveSpec.trim()
                            + "\"，请核对编码是否用错或更新物料档案",
                    material.getMaterialCode(), material.getName(), material);
        }
    }

    /**
     * 规格宽松归一化：全角转半角、去全部空白、转小写；
     * 仅抵消排版差异，不放松任何实质字符比对（不做单位换算与包含判定）
     */
    private String normalizeSpec(String value) {
        StringBuilder builder = new StringBuilder(value.length());
        for (char c : value.toCharArray()) {
            if (Character.isWhitespace(c)) {
                continue;
            }
            if (c >= '\uFF01' && c <= '\uFF5E') {
                c = (char) (c - 0xFEE0);
            }
            builder.append(Character.toLowerCase(c));
        }
        return builder.toString();
    }

    private void ensureCadenceComponentReady(ErpProductDO product,
                                             Map<Long, ErpProductCadenceDO> cadenceMap) {
        if (!Boolean.TRUE.equals(product.getPcbComponent())) {
            return;
        }
        ErpProductCadenceDO cadence = cadenceMap.get(product.getId());
        if (cadence == null || StrUtil.isBlank(cadence.getSchematicPart())
                || StrUtil.isBlank(cadence.getPcbFootprint())) {
            throw new RdBomRowParseException(RdBomRowIssueType.CADENCE_DATA_INCOMPLETE,
                    "PCB 元器件缺少 Cadence 原理图库符号或 PCB 封装：" + product.getMaterialCode(),
                    product.getMaterialCode(), product.getName(), product);
        }
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

    private void addRowIssue(List<ErpRdBomPrecheckResultVO.RowIssue> list, Integer rowNumber,
                             String materialCode, String reason, String issueType) {
        ErpRdBomPrecheckResultVO.RowIssue issue = new ErpRdBomPrecheckResultVO.RowIssue();
        issue.setRowNumber(rowNumber);
        issue.setMaterialCode(materialCode);
        issue.setReason(reason);
        issue.setIssueType(issueType);
        list.add(issue);
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
            // 顶层编码不在明细行收集范围内（表头信息不属于明细），Map 未命中时兜底直查数据库
            if (topProduct == null) {
                topProduct = productMapper.selectOne(ErpProductDO::getMaterialCode, topNorm);
                if (topProduct == null) {
                    topProduct = productMapper.selectOne(ErpProductDO::getMaterialCode, topCode);
                }
                if (topProduct == null) {
                    // 编码沿革兜底：顶层物料改过码时，历史文件里的旧码仍应对上号
                    topProduct = productMapper.selectByCodeOrHistory(topNorm);
                }
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
        String value = DATA_FORMATTER.get().formatCellValue(cell);
        return value == null ? "" : value.trim();
    }

}
