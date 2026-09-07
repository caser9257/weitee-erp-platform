package cn.weitee.erp.module.erp.service.product;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.weitee.erp.framework.common.enums.CommonStatusEnum;
import cn.weitee.erp.framework.common.pojo.PageParam;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.framework.excel.core.util.FileImportProtector;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.erp.controller.admin.product.ErpProductCadenceAccessHelper;
import cn.weitee.erp.module.erp.controller.admin.product.vo.product.ErpProductImportResultVO;
import cn.weitee.erp.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.weitee.erp.module.erp.controller.admin.product.vo.product.ErpRdCadencePageReqVO;
import cn.weitee.erp.module.erp.controller.admin.product.vo.product.ProductSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductCadenceDO;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductDO;
import cn.weitee.erp.module.erp.dal.mysql.product.ErpProductCadenceMapper;
import cn.weitee.erp.module.erp.dal.mysql.product.ErpProductMapper;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.enums.ErrorCodeConstants;
import cn.weitee.erp.module.system.service.notify.ImportNotifyHelper;
import lombok.extern.slf4j.Slf4j;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * 研发物料(Cadence) Service 实现类。
 *
 * 仅研发部门(deptId=103)可访问；Cadence 字段属于物料属性，导入只维护 erp_product_cadence 扩展数据。
 * 已审核物料的 Cadence 变更走物料修改审批，禁止直接覆盖主数据形成审批后门；
 * 一次导入合并为一个批次审批（ErpProductBatchUpdateBpmService），通过整批生效、驳回整批作废。
 */
@Service
@Validated
@Slf4j
public class ErpProductRdCadenceServiceImpl implements ErpProductRdCadenceService {

    @Resource
    private ErpProductMapper erpProductMapper;
    @Resource
    private ErpProductCadenceMapper productCadenceMapper;
    @Resource
    private ErpProductService productService;
    @Resource
    private ErpProductPendingChangeService pendingChangeService;
    @Resource
    private ErpProductBatchUpdateBpmService productBatchUpdateBpmService;
    @Resource
    private FileImportProtector fileImportProtector;
    @Resource
    private ImportNotifyHelper importNotifyHelper;

    /** 写入字段 -> 英文表头别名（按别名定位列，优先匹配顺序从左到右） */
    private static final Map<String, String[]> HEADER_ALIASES = Map.ofEntries(
            Map.entry("materialCode", new String[]{
                    "Part_Number", "Part Number", "Part_No", "PartNo.", "物料编号"}),
            Map.entry("pcbComponent", new String[]{
                    "PCB_Component", "PCB Component", "Is_PCB", "是否PCB元器件"}),
            Map.entry("schematicPart", new String[]{
                    "Schematic_Part", "Schematic Part", "Symbol", "Cell", "原理图符号"}),
            Map.entry("pcbFootprint", new String[]{
                    "PCB_Footprint", "PCB Footprint", "Footprint", "Package", "PCB封装"}),
            Map.entry("cadenceDescription", new String[]{
                    "Description", "Part Description", "关键参数描述"}),
            Map.entry("manufacturerPartNumber", new String[]{
                    "Manufacturer", "Manufacturer Part Number", "Mfr Part No", "MFR", "厂家型号"}),
            Map.entry("dimension", new String[]{"Dimension", "Size", "三维尺寸"}),
            Map.entry("threeDLib", new String[]{"3D_Lib", "3D Lib", "3D模型"}),
            Map.entry("datasheet", new String[]{"Datasheet", "Data Sheet", "数据手册"}),
            Map.entry("lifecycle", new String[]{"Lifecycle", "Life Cycle", "生命周期"}),
            Map.entry("preferredPart", new String[]{
                    "Preferred_Part", "Preferred Part", "Preferred", "优选"}),
            Map.entry("operatingTemperature", new String[]{
                    "Operating_Temperature", "Operating Temperature", "Op Temp", "工作温度"}),
            Map.entry("mountingType", new String[]{
                    "Mounting_Type", "Mounting Type", "Mount", "安装类型"}),
            Map.entry("dnp", new String[]{"DNP", "Do Not Populate", "空置标志"}),
            Map.entry("importedOrReplacement", new String[]{
                    "Imported_or_Replacement", "Imported or Replacement", "进口/替代"}),
            Map.entry("secondDescription", new String[]{
                    "Second_Description", "Second Description", "参数描述2"}),
            Map.entry("thirdDescription", new String[]{
                    "Third_Description", "Third Description", "参数描述3"}),
            Map.entry("fourthDescription", new String[]{
                    "Fourth_Description", "Fourth Description", "参数描述4"})
    );

    /** 必填标准字段（缺失表头直接拒绝导入） */
    private static final List<String> REQUIRED_FIELDS = List.of("materialCode", "schematicPart", "pcbFootprint");

    private static final DataFormatter DATA_FORMATTER = new DataFormatter();

    /** Cadence 数据完整子查询（schematic_part 与 pcb_footprint 均非空） */
    private static final String CADENCE_COMPLETE_SUB =
            "SELECT 1 FROM erp_product_cadence c WHERE c.product_id = erp_product.id"
                    + " AND c.schematic_part IS NOT NULL AND c.pcb_footprint IS NOT NULL";

    private void assertRdDept(Long deptId) {
        if (!ErpProductCadenceAccessHelper.canAccess(deptId)) {
            throw exception(ErrorCodeConstants.PRODUCT_CADENCE_ACCESS_DENIED);
        }
    }

    @Override
    public PageResult<ErpProductRespVO> getRdCadencePage(ErpRdCadencePageReqVO reqVO, Long deptId) {
        assertRdDept(deptId);
        PageResult<ErpProductDO> page = erpProductMapper.selectPage(reqVO, buildQuery(reqVO));
        return new PageResult<>(productService.buildProductVOList(page.getList()), page.getTotal());
    }

    @Override
    public List<ErpProductRespVO> exportRdCadence(ErpRdCadencePageReqVO reqVO, Long deptId) {
        assertRdDept(deptId);
        ErpRdCadencePageReqVO exportReq = BeanUtils.toBean(reqVO, ErpRdCadencePageReqVO.class);
        exportReq.setPageSize(PageParam.PAGE_SIZE_NONE);
        PageResult<ErpProductDO> page = erpProductMapper.selectPage(exportReq, buildQuery(reqVO));
        return productService.buildProductVOList(page.getList());
    }

    private LambdaQueryWrapperX<ErpProductDO> buildQuery(ErpRdCadencePageReqVO reqVO) {
        LambdaQueryWrapperX<ErpProductDO> q = new LambdaQueryWrapperX<ErpProductDO>()
                .likeIfPresent(ErpProductDO::getName, reqVO.getName())
                .likeIfPresent(ErpProductDO::getMaterialCode, reqVO.getMaterialCode())
                .eqIfPresent(ErpProductDO::getCategoryId, reqVO.getCategoryId())
                .eq(ErpProductDO::getPcbComponent, Boolean.TRUE)
                .orderByDesc(ErpProductDO::getId);
        // 编码沿革：当前码或历史旧码命中均可检索到（and() 返回父类型，独立调用丢弃返回值）
        if (reqVO.getMaterialCode() != null && !reqVO.getMaterialCode().isBlank()) {
            String code = reqVO.getMaterialCode().trim();
            q.and(w -> w.like(ErpProductDO::getMaterialCode, code)
                    .or().apply("EXISTS (SELECT 1 FROM erp_product_code_history h "
                            + "WHERE h.product_id = erp_product.id AND h.deleted = 0 "
                            + "AND h.old_code LIKE CONCAT('%', {0}, '%'))", code));
        }
        if (reqVO.getCadenceComplete() != null) {
            if (Boolean.TRUE.equals(reqVO.getCadenceComplete())) {
                q.apply("EXISTS (" + CADENCE_COMPLETE_SUB + ")");
            } else {
                q.apply("NOT EXISTS (" + CADENCE_COMPLETE_SUB + ")");
            }
        }
        return q;
    }

    @Override
    public byte[] downloadTemplate() {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Cadence 导入模板");
            String[] headers = {
                    "Part_Number", "PCB_Component", "Part_Type", "Schematic_Part", "Value", "PCB_Footprint",
                    "Description", "Package", "Part_Name", "Manufacturer", "Dimension", "3D_Lib", "Datasheet", "Lifecycle",
                    "Preferred_Part", "Grade", "Operating_Temperature", "Mounting_Type", "DNP",
                    "Imported_or_Replacement", "Second_Description", "Third_Description", "Fourth_Description"
            };
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
                sheet.setColumnWidth(i, 4000);
            }
            // 示例行（仅格式参考，导入前需删除）
            Row exampleRow = sheet.createRow(1);
            exampleRow.createCell(0).setCellValue("MAT-1001");
            exampleRow.createCell(1).setCellValue("Yes");
            exampleRow.createCell(3).setCellValue("R_0603_10K");
            exampleRow.createCell(5).setCellValue("0603");
            exampleRow.createCell(6).setCellValue("10K 1%");
            exampleRow.createCell(9).setCellValue("示例制造商");
            exampleRow.createCell(11).setCellValue("R0603_3D");
            exampleRow.createCell(14).setCellValue("Yes");
            exampleRow.createCell(18).setCellValue("No");

            Sheet helpSheet = workbook.createSheet("填写说明");
            String[] helps = {
                    "必填列：Part_Number（物料编号，须已在 ERP 建档且已审核通过）；Schematic_Part、PCB_Footprint 建议填写（留空保留现有值，两字段齐全的物料才会出现在 Cadence 视图）",
                    "Part_Number 必须能匹配 ERP 中已存在的物料；匹配不到、或物料未审核/未启用，该行导入失败",
                    "PCB_Component 列：物料未标记为 PCB 元器件时，填写 Yes 可随本次导入一并提交审批自动标记；留空则该行导入失败",
                    "布尔列 PCB_Component、Preferred_Part、DNP 仅接受 Yes/No 或 1/0（留空表示保留该物料现有值，不修改）",
                    "Part_Type、Value、Package、Part_Name、Grade 为 Cadence 参考列，本系统导入时不写入，仅作备注",
                    "其余 Cadence 列留空表示保留该物料现有值，不覆盖",
                    "已审核物料的变更（含 PCB 元器件标记）将进入物料修改审批，不会立即覆盖生效数据",
                    "示例行仅作格式参考，导入前请删除"
            };
            for (int i = 0; i < helps.length; i++) {
                helpSheet.createRow(i).createCell(0).setCellValue(helps[i]);
                helpSheet.setColumnWidth(0, 8000);
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            log.error("[downloadTemplate] 生成 Cadence 导入模板失败", e);
            throw new RuntimeException("生成 Cadence 导入模板失败");
        }
    }

    @Override
    public ErpProductImportResultVO precheckImport(MultipartFile file, Long deptId, boolean markAllAsPcb) {
        return processImport(file, false, null, deptId, markAllAsPcb);
    }

    @Override
    public ErpProductImportResultVO importCadence(MultipartFile file, Long userId, Long deptId, boolean markAllAsPcb) {
        return processImport(file, true, userId, deptId, markAllAsPcb);
    }

    private ErpProductImportResultVO processImport(MultipartFile file, boolean commit, Long userId, Long deptId, boolean markAllAsPcb) {
        assertRdDept(deptId);
        ErpProductImportResultVO result = newResult();
        Set<String> seenPartNumbers = new HashSet<>();
        List<ProductBatchUpdateItem> batchItems = new ArrayList<>();
        try {
            byte[] plain = fileImportProtector.preparePlainContent(file.getBytes(), file.getOriginalFilename());
            try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(plain))) {
                // 自动扫描所有 Sheet：定位表头匹配度最高的 Sheet + 表头行，无需用户指定
                int[] best = findBestSheetAndRow(workbook);
                int sheetIndex = best[0];
                int headerRowIndex = best[1];
                int matchScore = best[2];
                if (matchScore < 1) {
                    List<String> sheetNames = new ArrayList<>();
                    for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                        sheetNames.add(workbook.getSheetName(i));
                    }
                    throw new IllegalArgumentException(
                            "所有 Sheet 均未识别到 Cadence 表头（Part_Number/Schematic_Part/PCB_Footprint 等）。"
                                    + "文件包含 Sheet：" + sheetNames + "。请下载模板按格式填写后上传");
                }
                Sheet sheet = workbook.getSheetAt(sheetIndex);
                Row headerRow = sheet.getRow(headerRowIndex);
                log.warn("[processImport] 自动选中 Sheet[{}]「{}」，表头位于第 {} 行，匹配到 {} 个已知列",
                        sheetIndex, sheet.getSheetName(), headerRowIndex + 1, matchScore);
                Map<String, Integer> headerMap = parseHeaderMap(headerRow, result.getIgnoredColumns());
                int totalRows = sheet.getLastRowNum();
                for (int i = headerRowIndex + 1; i <= totalRows; i++) {
                    Row row = sheet.getRow(i);
                    // 全空白行（Excel 格式化拖出的幽灵行，可能多达数万行）直接跳过：不计成功也不计失败
                    if (row == null || isBlankRow(row)) {
                        continue;
                    }
                    try {
                        ProductBatchUpdateItem item = handleRow(row, i, headerMap, seenPartNumbers, markAllAsPcb);
                        if (item != null) {
                            batchItems.add(item);
                        }
                        result.setSuccessCount(result.getSuccessCount() + 1);
                    } catch (Exception e) {
                        result.setFailCount(result.getFailCount() + 1);
                        addFail(result, i + 1, getByHeader(row, headerMap, "materialCode"), e.getMessage());
                    }
                }
                // 总行数 = 实际处理的数据行（剔除幽灵空行），避免"共 64816 行"吓到用户
                result.setTotalCount(result.getSuccessCount() + result.getFailCount());
            }
        } catch (IOException e) {
            log.error("[processImport] 读取 Cadence 导入文件失败", e);
            throw new RuntimeException("读取导入文件失败：" + e.getMessage());
        } catch (IllegalArgumentException e) {
            // 表头校验失败等预处理错误：直接写入结果的失败明细，前端可展示
            result.setTotalCount(0);
            result.setFailCount(1);
            addFail(result, 1, "-", e.getMessage());
            return result;
        }
        if (commit && CollUtil.isNotEmpty(batchItems)) {
            // 一次导入合并为一个批次审批：通过整批生效、驳回整批作废。
            // BPM 创建失败时失败通知已由 Service 发出，此处跳过成功统计通知，避免误导
            ProductBatchSubmitResult submitResult = productBatchUpdateBpmService.submitBatchUpdate(
                    userId, batchItems, "Cadence 数据导入（已提交批量审批）");
            if (submitResult.bpmCreateFailed()) {
                return result;
            }
        }
        if (commit) {
            // 全部行均与现有一致时不会创建审批（避免空审批），文案必须明说，防止用户误以为审批丢失
            String scene = CollUtil.isEmpty(batchItems) && result.getFailCount() == 0
                    ? "Cadence 数据导入（数据与现有一致，未产生待审批变更）"
                    : "Cadence 数据导入（已提交批量审批）";
            importNotifyHelper.sendImportResult("erp_import_result_product", scene,
                    result.getTotalCount(), result.getSuccessCount(), result.getFailCount(),
                    result.getFailDetails().stream()
                            .map(d -> "第" + d.getRowNumber() + "行 " + d.getBarCode() + "：" + d.getReason())
                            .limit(3).collect(Collectors.toList()));
        }
        return result;
    }

    /**
     * 校验并构建单个物料的批量变更明细。
     *
     * @return 变更明细；物料与现有数据完全一致时返回 null（视为成功跳过，不进入批次）
     */
    private ProductBatchUpdateItem handleRow(Row row, int rowIndex, Map<String, Integer> headerMap,
                                             Set<String> seenPartNumbers, boolean markAllAsPcb) {
        String materialCode = getByHeader(row, headerMap, "materialCode").trim();
        if (StrUtil.isBlank(materialCode)) {
            throw new IllegalArgumentException("物料编号(Part_Number)不能为空");
        }
        if (!seenPartNumbers.add(materialCode)) {
            throw new IllegalArgumentException("同一文件存在重复 Part_Number：" + materialCode);
        }
        ErpProductDO product = erpProductMapper.selectOne(ErpProductDO::getMaterialCode, materialCode);
        if (product == null) {
            // 编码沿革兜底：物料改过码时，历史 Cadence 文件里的旧码仍应对上号
            product = erpProductMapper.selectByCodeOrHistory(materialCode);
        }
        if (product == null) {
            throw new IllegalArgumentException("物料不存在：" + materialCode);
        }
        if (CommonStatusEnum.isDisable(product.getStatus())) {
            throw new IllegalArgumentException("物料未启用：" + materialCode);
        }
        if (!ErpAuditStatus.APPROVE.getStatus().equals(product.getAuditStatus())) {
            throw new IllegalArgumentException("物料未审核通过（需已审批）：" + materialCode);
        }
        // 未标记 PCB 元器件时：允许通过 markAllAsPcb（全量开关）或 PCB_Component=Yes 显式声明，随本次导入一并走修改审批（不绕过审批）
        Boolean pcbFlag = markAllAsPcb ? Boolean.TRUE : parseBooleanValue(getByHeader(row, headerMap, "pcbComponent"), "PCB_Component");
        if (!Boolean.TRUE.equals(product.getPcbComponent())) {
            if (!Boolean.TRUE.equals(pcbFlag)) {
                throw new IllegalArgumentException("物料未标记为 PCB 元器件(is_pcb_component=0)。"
                        + "可在导入对话框勾选[标记为 PCB 元器件]开关，或在模板 PCB_Component 列填写 Yes：" + materialCode);
            }
        }
        // Schematic_Part / PCB_Footprint 选填：留空表示保留现有值；两字段齐全的物料才会出现在
        // v_cadence_component 视图中（数据完整性可按列表页“完整/缺失”筛选跟踪），不阻断导入
        String schematicPart = getByHeader(row, headerMap, "schematicPart").trim();
        String pcbFootprint = getByHeader(row, headerMap, "pcbFootprint").trim();

        // 以现有物料为基准构建目标值：当前 Cadence 值先填入，再覆盖文件中提供（非空）的列
        ErpProductCadenceDO cadence = productCadenceMapper.selectByProductId(product.getId());
        if (cadence != null) {
            ErpProductCadenceConverter.applyToProduct(cadence, product);
        }
        ProductSaveReqVO reqVO = BeanUtils.toBean(product, ProductSaveReqVO.class);
        // 显式声明 PCB_Component=Yes 且物料未标记时，主数据变更随修改审批一并生效
        if (Boolean.TRUE.equals(pcbFlag) && !Boolean.TRUE.equals(product.getPcbComponent())) {
            reqVO.setPcbComponent(true);
        }
        applyIfPresent(reqVO::setSchematicPart, schematicPart);
        applyIfPresent(reqVO::setPcbFootprint, pcbFootprint);
        applyIfPresent(reqVO::setCadenceDescription, getByHeader(row, headerMap, "cadenceDescription"));
        applyIfPresent(reqVO::setManufacturerPartNumber, getByHeader(row, headerMap, "manufacturerPartNumber"));
        applyIfPresent(reqVO::setDimension, getByHeader(row, headerMap, "dimension"));
        applyIfPresent(reqVO::setThreeDLib, getByHeader(row, headerMap, "threeDLib"));
        applyIfPresent(reqVO::setDatasheet, getByHeader(row, headerMap, "datasheet"));
        applyIfPresent(reqVO::setLifecycle, getByHeader(row, headerMap, "lifecycle"));
        applyIfPresent(reqVO::setOperatingTemperature, getByHeader(row, headerMap, "operatingTemperature"));
        applyIfPresent(reqVO::setMountingType, getByHeader(row, headerMap, "mountingType"));
        applyIfPresent(reqVO::setImportedOrReplacement, getByHeader(row, headerMap, "importedOrReplacement"));
        applyIfPresent(reqVO::setSecondDescription, getByHeader(row, headerMap, "secondDescription"));
        applyIfPresent(reqVO::setThirdDescription, getByHeader(row, headerMap, "thirdDescription"));
        applyIfPresent(reqVO::setFourthDescription, getByHeader(row, headerMap, "fourthDescription"));
        parseBooleanField(reqVO::setPreferredPart, getByHeader(row, headerMap, "preferredPart"), "Preferred_Part");
        parseBooleanField(reqVO::setDnp, getByHeader(row, headerMap, "dnp"), "DNP");

        // diff 计算：无变更视为成功跳过（不进入批次，避免空审批）
        var changes = pendingChangeService.diffProduct(product, reqVO);
        if (changes.isEmpty()) {
            return null;
        }
        // 关键字段冻结校验：被 BOM 引用的物料禁改 materialCode/standard（导入时前置拦截，而非等到审批阶段）
        pendingChangeService.validateFrozenFields(product.getId(), changes.keySet());
        return new ProductBatchUpdateItem(product.getId(), reqVO, changes.keySet());
    }

    /**
     * 自动定位数据 Sheet 与表头行：遍历所有 Sheet 的前 15 行，
     * 找到包含最多已知表头别名的组合。
     *
     * @return [sheetIndex, headerRowIndex, matchScore]
     */
    private int[] findBestSheetAndRow(Workbook workbook) {
        // 收集所有已知别名的小写集合
        Set<String> allAliasesLower = new HashSet<>();
        HEADER_ALIASES.values().forEach(aliases ->
                java.util.Arrays.stream(aliases).forEach(a -> allAliasesLower.add(a.toLowerCase())));
        int bestSheet = 0;
        int bestRow = 0;
        int bestScore = -1;
        for (int s = 0; s < workbook.getNumberOfSheets(); s++) {
            Sheet sheet = workbook.getSheetAt(s);
            if (sheet == null) {
                continue;
            }
            int maxScan = Math.min(15, sheet.getLastRowNum() + 1);
            for (int r = 0; r < maxScan; r++) {
                Row row = sheet.getRow(r);
                if (row == null) {
                    continue;
                }
                int score = 0;
                for (int c = 0; c < row.getLastCellNum(); c++) {
                    String text = getCellString(row, c).toLowerCase().trim();
                    if (allAliasesLower.contains(text)) {
                        score++;
                    }
                }
                if (score > bestScore) {
                    bestScore = score;
                    bestSheet = s;
                    bestRow = r;
                }
            }
        }
        return new int[]{bestSheet, bestRow, bestScore};
    }

    private Map<String, Integer> parseHeaderMap(Row headerRow, List<String> ignoredColumns) {
        if (headerRow == null) {
            throw new IllegalArgumentException("导入文件缺少表头行");
        }
        // 不区分大小写收集表头（保留原始名用于显示，小写用于匹配）
        Map<String, Integer> raw = new HashMap<>();
        Map<String, Integer> rawLower = new HashMap<>();
        for (int c = 0; c < headerRow.getLastCellNum(); c++) {
            String text = getCellString(headerRow, c);
            if (StrUtil.isNotBlank(text)) {
                raw.putIfAbsent(text, c);
                rawLower.putIfAbsent(text.toLowerCase(), c);
            }
        }
        log.warn("[parseHeaderMap] 文件实际表头({}列): {}", raw.size(), raw.keySet());
        // 逐列打印表头内容，便于排查非标准格式
        for (int c = 0; c < headerRow.getLastCellNum(); c++) {
            String text = getCellString(headerRow, c);
            log.warn("[parseHeaderMap]   列[{}]='{}' (cellType={})", c, text, headerRow.getCell(c) != null ? headerRow.getCell(c).getCellType() : "null");
        }
        Map<String, Integer> headerMap = new HashMap<>();
        Set<String> mappedAliases = new HashSet<>();
        HEADER_ALIASES.forEach((field, aliases) -> {
            for (String alias : aliases) {
                // 精确匹配
                Integer idx = raw.get(alias);
                if (idx != null) {
                    headerMap.put(field, idx);
                    mappedAliases.add(alias);
                    return;
                }
                // 不区分大小写匹配
                idx = rawLower.get(alias.toLowerCase());
                if (idx != null) {
                    headerMap.put(field, idx);
                    mappedAliases.add(alias);
                    return;
                }
            }
        });
        // 文件中未被映射的列：统一报告（含参考列），但不写入（不区分大小写判断是否已映射）
        Set<String> mappedAliasesLower = mappedAliases.stream()
                .map(String::toLowerCase).collect(java.util.stream.Collectors.toSet());
        raw.keySet().stream()
                .filter(col -> !mappedAliasesLower.contains(col.toLowerCase()))
                .forEach(ignoredColumns::add);
        List<String> missing = new ArrayList<>();
        for (String field : REQUIRED_FIELDS) {
            if (!headerMap.containsKey(field)) {
                missing.add(String.join("/", HEADER_ALIASES.get(field)));
            }
        }
        if (!missing.isEmpty()) {
            throw new IllegalArgumentException("导入文件缺少必需列：" + String.join("、", missing)
                    + "；文件实际表头：" + raw.keySet());
        }
        return headerMap;
    }

    private void applyIfPresent(Consumer<String> setter, String value) {
        if (StrUtil.isNotBlank(value)) {
            setter.accept(value.trim());
        }
    }

    private void parseBooleanField(Consumer<Boolean> setter, String value, String col) {
        Boolean parsed = parseBooleanValue(value, col);
        if (parsed != null) {
            setter.accept(parsed);
        }
    }

    /** 解析布尔列：空返回 null（表示未提供、保留现有值），非法值抛出异常 */
    private Boolean parseBooleanValue(String value, String col) {
        if (StrUtil.isBlank(value)) {
            return null;
        }
        String v = value.trim();
        if ("1".equals(v) || "Yes".equalsIgnoreCase(v) || "true".equalsIgnoreCase(v)) {
            return Boolean.TRUE;
        }
        if ("0".equals(v) || "No".equalsIgnoreCase(v) || "false".equalsIgnoreCase(v)) {
            return Boolean.FALSE;
        }
        throw new IllegalArgumentException(col + " 只支持 Yes/No（或 1/0）：" + value);
    }

    private String getByHeader(Row row, Map<String, Integer> headerMap, String field) {
        Integer idx = headerMap.get(field);
        return idx == null ? "" : getCellString(row, idx);
    }

    /**
     * 判断是否全空白行：无任何物理单元格，或所有单元格格式化后均为空白。
     * Cadence 等外部导出的 Excel 常把单元格样式拖到数万行，形成"幽灵空行"，
     * 不跳过会把每行都当数据校验并报"Part_Number 不能为空"。
     */
    private boolean isBlankRow(Row row) {
        if (row.getPhysicalNumberOfCells() == 0) {
            return true;
        }
        for (int c = 0; c < row.getLastCellNum(); c++) {
            if (StrUtil.isNotBlank(getCellString(row, c))) {
                return false;
            }
        }
        return true;
    }

    private String getCellString(Row row, int cellIndex) {
        org.apache.poi.ss.usermodel.Cell cell = row.getCell(cellIndex);
        if (cell == null) {
            return "";
        }
        String value = DATA_FORMATTER.formatCellValue(cell);
        return value == null ? "" : value.trim();
    }

    private ErpProductImportResultVO newResult() {
        ErpProductImportResultVO r = new ErpProductImportResultVO();
        r.setTotalCount(0);
        r.setSuccessCount(0);
        r.setFailCount(0);
        r.setFailDetails(new ArrayList<>());
        r.setSuccessCategoryIds(new ArrayList<>());
        r.setIgnoredColumns(new ArrayList<>());
        return r;
    }

    private void addFail(ErpProductImportResultVO result, int rowNumber, String partNumber, String reason) {
        ErpProductImportResultVO.FailDetail fd = new ErpProductImportResultVO.FailDetail();
        fd.setRowNumber(rowNumber);
        fd.setBarCode(partNumber);
        fd.setReason(reason);
        result.getFailDetails().add(fd);
    }

}
