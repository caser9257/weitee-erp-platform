package cn.weitee.erp.module.erp.service.product;

import cn.hutool.core.util.StrUtil;
import cn.weitee.erp.framework.common.enums.CommonStatusEnum;
import cn.weitee.erp.framework.excel.core.util.FileImportProtector;
import cn.weitee.erp.module.erp.controller.admin.product.vo.category.ErpProductCategoryListReqVO;
import cn.weitee.erp.module.erp.controller.admin.product.vo.product.ErpProductImportResultVO;
import cn.weitee.erp.module.system.service.notify.ImportNotifyHelper;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductCategoryDO;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductDO;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductUnitDO;
import cn.weitee.erp.module.erp.dal.mysql.product.ErpProductCategoryMapper;
import cn.weitee.erp.module.erp.dal.mysql.product.ErpProductMapper;
import cn.weitee.erp.module.erp.dal.mysql.product.ErpProductUnitMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Font;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * ERP 产品导入 Service 实现类
 */
@Service
@Validated
@Slf4j
public class ErpProductImportServiceImpl implements ErpProductImportService {

    @Resource
    private FileImportProtector fileImportProtector;

    /** 模板列定义（与服务器导出格式对齐的可映射列） */
    private static final String[] HEADERS = {
            "产品名称", "产品编号", "产品型号", "产品分类", "启用状态",
            "产品封装", "质量等级", "品牌/制造商", "替代型号", "产品说明",
            "批号必填", "是否需要质检", "产品有效期", "有效期单位", "基本单位",
            "条形码", "建议进价", "建议售价", "最低售价"
    };

    /**
     * 标准字段 -> 兼容表头名（按顺序匹配，新格式优先、旧模板名兜底）。
     * 导入解析按表头名定位列，服务器导出的 62 列格式中未列出的列自动忽略。
     */
    private static final Map<String, String[]> HEADER_ALIASES = Map.ofEntries(
            Map.entry("name", new String[]{"产品名称", "产品名称*"}),
            Map.entry("materialCode", new String[]{"产品编号", "物料编号"}),
            Map.entry("standard", new String[]{"产品型号", "规格型号"}),
            Map.entry("categoryName", new String[]{"产品分类", "分类名称*"}),
            Map.entry("status", new String[]{"启用状态", "状态"}),
            Map.entry("packaging", new String[]{"产品封装"}),
            Map.entry("qualityGrade", new String[]{"质量等级"}),
            Map.entry("brandManufacturer", new String[]{"品牌/制造商"}),
            Map.entry("alternativeModel", new String[]{"替代型号"}),
            Map.entry("remark", new String[]{"产品说明", "备注"}),
            Map.entry("batchControl", new String[]{"批号必填", "是否批次管理"}),
            Map.entry("inspection", new String[]{"是否需要质检", "是否来料检验"}),
            Map.entry("mrpEnable", new String[]{"是否参与MRP"}),
            Map.entry("expiryDay", new String[]{"产品有效期", "保质期(天)"}),
            Map.entry("expiryDayUnit", new String[]{"有效期单位"}),
            Map.entry("weight", new String[]{"重量(g)"}),
            Map.entry("unitName", new String[]{"基本单位", "单位名称*"}),
            Map.entry("barCode", new String[]{"条形码", "条码*"}),
            Map.entry("purchasePrice", new String[]{"建议进价", "采购价"}),
            Map.entry("salePrice", new String[]{"建议售价", "销售价"}),
            Map.entry("minPrice", new String[]{"最低售价", "最低价"})
    );

    /** 必填标准字段（缺失表头直接拒绝导入） */
    private static final List<String> REQUIRED_FIELDS = List.of("name", "barCode", "categoryName", "unitName");

    private static final DataFormatter DATA_FORMATTER = new DataFormatter();

    @Resource
    private ErpProductMapper erpProductMapper;
    @Resource
    private ErpProductCategoryMapper erpProductCategoryMapper;
    @Resource
    private ErpProductUnitMapper erpProductUnitMapper;

    @Resource
    private ImportNotifyHelper importNotifyHelper;

    @Override
    public byte[] downloadTemplate() {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("产品导入模板");

            // 创建表头
            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 4000);
            }

            // 创建示例数据行（索引与 HEADERS 一致）
            Row exampleRow = sheet.createRow(1);
            exampleRow.createCell(0).setCellValue("示例电阻");
            exampleRow.createCell(1).setCellValue("MAT-1001");
            exampleRow.createCell(2).setCellValue("0603 10K 1%");
            exampleRow.createCell(3).setCellValue("电子料");
            exampleRow.createCell(4).setCellValue("启用");
            exampleRow.createCell(5).setCellValue("编带");
            exampleRow.createCell(6).setCellValue("A级");
            exampleRow.createCell(7).setCellValue("示例制造商");
            exampleRow.createCell(8).setCellValue("替代型号-001");
            exampleRow.createCell(9).setCellValue("示例数据");
            exampleRow.createCell(10).setCellValue("否");
            exampleRow.createCell(11).setCellValue("是");
            exampleRow.createCell(12).setCellValue("365");
            exampleRow.createCell(13).setCellValue("天");
            exampleRow.createCell(14).setCellValue("个");
            exampleRow.createCell(15).setCellValue("R-1001");
            exampleRow.createCell(16).setCellValue("0.05");
            exampleRow.createCell(17).setCellValue("0.08");
            exampleRow.createCell(18).setCellValue("0.04");

            // 创建填写说明 sheet
            Sheet helpSheet = workbook.createSheet("填写说明");
            String[] helps = {
                    "必填列：产品名称、产品分类、基本单位；条形码可留空（留空时自动使用产品编号作为条码）",
                    "产品分类支持层级路径（如：NPIC->原材料->接插件），路径中缺失的分级将自动创建；也可直接填写已存在的分类名称",
                    "基本单位不存在时将自动创建（如：PCS）；也可直接填写系统中已存在的单位名称",
                    "启用状态：启用/停用（或 1/0），默认为启用",
                    "批号必填 / 是否需要质检：是/否（或 1/0），默认为否",
                    "产品有效期配合有效期单位（天/周/月/年，默认天）换算为天数",
                    "条形码已存在时：该行导入失败，已建档物料不支持导入覆盖，修改请走「编辑」提交审批",
                    "示例数据行（第 2 行）仅作格式参考，导入时会被当作真实数据处理，请先删除",
            };
            for (int i = 0; i < helps.length; i++) {
                Row helpRow = helpSheet.createRow(i);
                helpRow.createCell(0).setCellValue(helps[i]);
                helpSheet.setColumnWidth(0, 6000);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            log.error("[downloadTemplate] 生成产品导入模板失败", e);
            throw new RuntimeException("生成产品导入模板失败");
        }
    }

    @Override
    public ErpProductImportResultVO importProducts(MultipartFile file, Boolean updateSupport) {
        ErpProductImportResultVO result = new ErpProductImportResultVO();
        result.setTotalCount(0);
        result.setSuccessCount(0);
        result.setFailCount(0);
        result.setFailDetails(new ArrayList<>());
        result.setSuccessCategoryIds(new ArrayList<>());
        // updateSupport 参数已废弃：已建档物料一律拒绝导入覆盖（修改必须走审批），保留入参仅为 API 兼容

        // 预加载分类与单位名称映射（避免 N+1）
        Map<String, Long> categoryNameMap = buildCategoryNameMap();
        Map<String, Long> unitNameMap = buildUnitNameMap();
        // 分类路径自动创建的会话级缓存：key=标准化路径，value=分类id
        Map<String, Long> categoryPathCache = new HashMap<>();
        Map<String, Long> unitNameCache = new HashMap<>();

        Set<Long> successCategoryIds = new LinkedHashSet<>();
        try {
            byte[] plainContent = fileImportProtector.preparePlainContent(file.getBytes(), file.getOriginalFilename());
            try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(plainContent))) {
            Sheet sheet = workbook.getSheetAt(0);
            // 表头驱动：按列名定位列，兼容服务器导出格式与本系统模板
            Row headerRow = sheet.getRow(0);
            Map<String, Integer> headerMap = parseHeaderMap(headerRow);
            int totalRows = sheet.getLastRowNum(); // 0-based，第 0 行为表头
            result.setTotalCount(totalRows);

            for (int i = 1; i <= totalRows; i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }
                try {
                    Long categoryId = importRow(row, i, headerMap, categoryNameMap, unitNameMap, categoryPathCache, unitNameCache, result);
                    result.setSuccessCount(result.getSuccessCount() + 1);
                    successCategoryIds.add(categoryId);
                } catch (Exception e) {
                    result.setFailCount(result.getFailCount() + 1);
                    ErpProductImportResultVO.FailDetail failDetail = new ErpProductImportResultVO.FailDetail();
                    failDetail.setRowNumber(i + 1);
                    failDetail.setBarCode(getByHeader(row, headerMap, "barCode"));
                    failDetail.setReason(e.getMessage());
                    result.getFailDetails().add(failDetail);
                }
            }
            }
            result.setSuccessCategoryIds(new ArrayList<>(successCategoryIds));
        } catch (IOException e) {
            log.error("[importProducts] 导入产品失败", e);
            throw new RuntimeException("导入产品失败：" + e.getMessage());
        }
        importNotifyHelper.sendImportResult("erp_import_result_product", "产品导入",
                result.getTotalCount(), result.getSuccessCount(), result.getFailCount(),
                result.getFailDetails().stream()
                        .map(d -> "第" + d.getRowNumber() + "行 " + d.getBarCode() + "：" + d.getReason())
                        .collect(Collectors.toList()));
        return result;
    }

    private Long importRow(Row row, int rowIndex, Map<String, Integer> headerMap,
                           Map<String, Long> categoryNameMap,
                           Map<String, Long> unitNameMap, Map<String, Long> categoryPathCache,
                           Map<String, Long> unitNameCache,
                           ErpProductImportResultVO result) {
        String name = getByHeader(row, headerMap, "name");
        String materialCode = getByHeader(row, headerMap, "materialCode");
        String barCode = getByHeader(row, headerMap, "barCode");
        String categoryName = getByHeader(row, headerMap, "categoryName");
        String unitName = getByHeader(row, headerMap, "unitName");

        // 1. 必填校验
        if (StrUtil.isBlank(name)) {
            throw new IllegalArgumentException("产品名称不能为空");
        }
        // 条码可选：服务器导出数据通常无条形码，为空时用产品编号兜底（DB bar_code NOT NULL 且作为查重键）
        if (StrUtil.isBlank(barCode)) {
            barCode = materialCode;
        }
        if (StrUtil.isBlank(barCode)) {
            throw new IllegalArgumentException("条形码和产品编号不能同时为空");
        }
        if (StrUtil.isBlank(categoryName)) {
            throw new IllegalArgumentException("分类名称不能为空");
        }
        if (StrUtil.isBlank(unitName)) {
            throw new IllegalArgumentException("单位名称不能为空");
        }

        // 2. 分类解析：优先精确匹配名称；未命中时自动创建——
        //    含 -> 按层级路径逐级查找/创建（如"WT->30 原材料"）；不含 -> 作为单级分类自动创建（如"微组装"）
        String categoryKey = categoryName.trim();
        Long categoryId = categoryNameMap.get(categoryKey);
        if (categoryId == null) {
            categoryId = ensureCategoryPath(categoryKey, categoryPathCache);
        }
        Long unitId = unitNameMap.get(unitName.trim());
        if (unitId == null) {
            unitId = ensureUnit(unitName.trim(), unitNameCache);
        }

        // 3. 状态解析（默认启用）
        Integer status = parseStatus(getByHeader(row, headerMap, "status"));

        // 4. 数字字段解析
        BigDecimal weight = parseDecimal(getByHeader(row, headerMap, "weight"));
        BigDecimal purchasePrice = parseDecimal(getByHeader(row, headerMap, "purchasePrice"));
        BigDecimal salePrice = parseDecimal(getByHeader(row, headerMap, "salePrice"));
        BigDecimal minPrice = parseDecimal(getByHeader(row, headerMap, "minPrice"));
        Integer expiryDay = parseExpiryDays(getByHeader(row, headerMap, "expiryDay"),
                getByHeader(row, headerMap, "expiryDayUnit"));

        // 5. 布尔字段解析（默认 false）
        Boolean batchControlFlag = parseBoolean(getByHeader(row, headerMap, "batchControl"));
        Boolean inspectionRequiredFlag = parseBoolean(getByHeader(row, headerMap, "inspection"));
        Boolean mrpEnable = parseBoolean(getByHeader(row, headerMap, "mrpEnable"));

        // 6. 按条码查重：存在时按 updateSupport 决定更新或失败
        ErpProductDO exists = erpProductMapper.selectOne(ErpProductDO::getBarCode, barCode);
        ErpProductDO product = ErpProductDO.builder()
                .name(name.trim())
                .materialCode(StrUtil.isBlank(materialCode) ? null : materialCode.trim())
                .barCode(barCode.trim())
                .categoryId(categoryId)
                .unitId(unitId)
                .status(status)
                .standard(getByHeader(row, headerMap, "standard"))
                .packaging(getByHeader(row, headerMap, "packaging"))
                .qualityGrade(getByHeader(row, headerMap, "qualityGrade"))
                .brandManufacturer(getByHeader(row, headerMap, "brandManufacturer"))
                .alternativeModel(getByHeader(row, headerMap, "alternativeModel"))
                .remark(getByHeader(row, headerMap, "remark"))
                .expiryDay(expiryDay)
                .batchControlFlag(batchControlFlag)
                .inspectionRequiredFlag(inspectionRequiredFlag)
                .weight(weight)
                .purchasePrice(purchasePrice)
                .salePrice(salePrice)
                .minPrice(minPrice)
                .mrpEnable(mrpEnable)
                .build();
        if (exists != null) {
            // 已建档物料一律拒绝导入覆盖：修改必须走「编辑→修改审批」链路，防止绕过审批的后门
            throw new IllegalArgumentException("条码已存在：" + barCode
                    + "。已建档物料不支持导入覆盖（含勾选自动覆盖），请通过列表页「编辑」提交修改审批");
        } else {
            erpProductMapper.insert(product);
            log.info("[importProducts] 新增产品，row={}, id={}, barCode={}", rowIndex + 1, product.getId(), barCode);
        }
        return categoryId;
    }

    private Map<String, Long> buildCategoryNameMap() {
        List<ErpProductCategoryDO> categories = erpProductCategoryMapper.selectList(new ErpProductCategoryListReqVO());
        Map<String, Long> map = new HashMap<>();
        for (ErpProductCategoryDO category : categories) {
            // 同名分类时以最后一个为准，避免歧义
            map.put(category.getName(), category.getId());
        }
        return map;
    }

    /** 分类层级路径分隔符，如：WT->10 成品->10.01 功放模块 */
    private static final String CATEGORY_PATH_SEPARATOR = "->";

    /**
     * 按层级路径（A->B->C）逐级查找分类，缺失节点自动创建（启用态、sort=0）。
     *
     * 幂等：同父节点下同名分类复用不重建；pathCache 为本次导入的会话级缓存，避免同路径多行重复查库。
     * 分类创建不随产品行失败回滚：分类是主数据，行级失败仅影响该行产品，重导时已建分类直接复用。
     */
    private Long ensureCategoryPath(String categoryPath, Map<String, Long> pathCache) {
        Long cached = pathCache.get(categoryPath);
        if (cached != null) {
            return cached;
        }
        Long parentId = ErpProductCategoryDO.PARENT_ID_ROOT;
        StringBuilder walked = new StringBuilder();
        for (String segment : categoryPath.split(CATEGORY_PATH_SEPARATOR)) {
            String name = segment.trim();
            if (StrUtil.isBlank(name)) {
                throw new IllegalArgumentException("分类路径存在空层级：" + categoryPath);
            }
            if (walked.length() > 0) {
                walked.append(CATEGORY_PATH_SEPARATOR);
            }
            walked.append(name);
            String walkedPath = walked.toString();
            Long nodeId = pathCache.get(walkedPath);
            if (nodeId == null) {
                // 编码与名称按首个空格拆分（如"30 原材料"→code=30, name=原材料）
                // 无空格时 code 为空，只显示名称（如"WT"→code="", name=WT）
                int spaceIdx = name.indexOf(' ');
                String catCode = spaceIdx > 0 ? name.substring(0, spaceIdx) : "";
                String catName = spaceIdx > 0 ? name.substring(spaceIdx + 1) : name;
                // 查重用拆分后的 catName（与创建时一致，避免因名称不匹配导致重复创建）
                ErpProductCategoryDO node = erpProductCategoryMapper.selectOne(
                        ErpProductCategoryDO::getParentId, parentId, ErpProductCategoryDO::getName, catName);
                if (node != null) {
                    nodeId = node.getId();
                    // 如果已有分类的 code 为空但当前有数字编码，补上 code
                    if (StrUtil.isBlank(node.getCode()) && StrUtil.isNotBlank(catCode)) {
                        node.setCode(catCode);
                        erpProductCategoryMapper.updateById(node);
                    }
                }
            }
            if (nodeId == null) {
                int spaceIdx2 = name.indexOf(' ');
                String catCode = spaceIdx2 > 0 ? name.substring(0, spaceIdx2) : "";
                String catName = spaceIdx2 > 0 ? name.substring(spaceIdx2 + 1) : name;
                ErpProductCategoryDO node = ErpProductCategoryDO.builder()
                        .parentId(parentId)
                        .name(catName)
                        .code(catCode)
                        .status(CommonStatusEnum.ENABLE.getStatus())
                        .sort(0)
                        .build();
                erpProductCategoryMapper.insert(node);
                nodeId = node.getId();
                log.info("[ensureCategoryPath] 分类不存在，已自动创建，path={}, id={}", walkedPath, nodeId);
            }
            pathCache.put(walkedPath, nodeId);
            parentId = nodeId;
        }
        return parentId;
    }

    /** 单位不存在时自动创建（启用态、精度默认 3）；幂等：同名单位复用不重建 */
    private Long ensureUnit(String unitName, Map<String, Long> unitNameCache) {
        return unitNameCache.computeIfAbsent(unitName, name -> {
            ErpProductUnitDO unit = erpProductUnitMapper.selectOne(ErpProductUnitDO::getName, name);
            if (unit != null) {
                return unit.getId();
            }
            ErpProductUnitDO created = ErpProductUnitDO.builder()
                    .name(name)
                    .status(CommonStatusEnum.ENABLE.getStatus())
                    .quantityPrecision(3)
                    .build();
            erpProductUnitMapper.insert(created);
            log.info("[ensureUnit] 单位不存在，已自动创建，name={}, id={}", name, created.getId());
            return created.getId();
        });
    }

    private Map<String, Long> buildUnitNameMap() {
        List<ErpProductUnitDO> units = erpProductUnitMapper.selectList();
        Map<String, Long> map = new HashMap<>();
        for (ErpProductUnitDO unit : units) {
            map.put(unit.getName(), unit.getId());
        }
        return map;
    }

    /** 解析表头行：按 HEADER_ALIASES 把列名映射为标准字段索引，缺失必填列直接拒绝导入 */
    private Map<String, Integer> parseHeaderMap(Row headerRow) {
        if (headerRow == null) {
            throw new IllegalArgumentException("导入文件缺少表头行");
        }
        Map<String, Integer> rawHeaders = new HashMap<>();
        for (int c = 0; c < headerRow.getLastCellNum(); c++) {
            String text = getCellString(headerRow, c);
            if (StrUtil.isNotBlank(text)) {
                rawHeaders.putIfAbsent(text, c);
            }
        }
        Map<String, Integer> headerMap = new HashMap<>();
        HEADER_ALIASES.forEach((field, aliases) -> {
            for (String alias : aliases) {
                Integer index = rawHeaders.get(alias);
                if (index != null) {
                    headerMap.put(field, index);
                    return;
                }
            }
        });
        List<String> missing = new ArrayList<>();
        for (String field : REQUIRED_FIELDS) {
            if (!headerMap.containsKey(field)) {
                missing.add(HEADER_ALIASES.get(field)[0]);
            }
        }
        if (!missing.isEmpty()) {
            throw new IllegalArgumentException("导入文件缺少必需列：" + String.join("、", missing));
        }
        return headerMap;
    }

    /** 按标准字段取单元格文本；表头未提供该列时返回空串（按默认值处理） */
    private String getByHeader(Row row, Map<String, Integer> headerMap, String field) {
        Integer index = headerMap.get(field);
        return index == null ? "" : getCellString(row, index);
    }

    private Integer parseStatus(String value) {
        if (StrUtil.isBlank(value)) {
            return CommonStatusEnum.ENABLE.getStatus();
        }
        String v = value.trim();
        if ("1".equals(v) || "启用".equals(v)) {
            return CommonStatusEnum.ENABLE.getStatus();
        }
        if ("0".equals(v) || "停用".equals(v)) {
            return CommonStatusEnum.DISABLE.getStatus();
        }
        throw new IllegalArgumentException("状态只支持 启用/停用（或 1/0）：" + value);
    }

    private Boolean parseBoolean(String value) {
        if (StrUtil.isBlank(value)) {
            return Boolean.FALSE;
        }
        String v = value.trim();
        if ("1".equals(v) || "是".equals(v)) {
            return Boolean.TRUE;
        }
        if ("0".equals(v) || "否".equals(v)) {
            return Boolean.FALSE;
        }
        throw new IllegalArgumentException("是否字段只支持 是/否（或 1/0）：" + value);
    }

    /** 产品有效期 + 有效期单位 换算为天数（单位缺省按天） */
    private Integer parseExpiryDays(String value, String unit) {
        BigDecimal amount = parseDecimal(value);
        if (amount == null) {
            return null;
        }
        long factor = 1;
        if (StrUtil.isNotBlank(unit)) {
            switch (unit.trim()) {
                case "天":
                case "日":
                    factor = 1;
                    break;
                case "周":
                    factor = 7;
                    break;
                case "月":
                    factor = 30;
                    break;
                case "年":
                    factor = 365;
                    break;
                default:
                    throw new IllegalArgumentException("有效期单位只支持 天/周/月/年：" + unit);
            }
        }
        return amount.multiply(BigDecimal.valueOf(factor)).intValue();
    }

    private BigDecimal parseDecimal(String value) {
        if (StrUtil.isBlank(value)) {
            return null;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("数字格式不正确：" + value);
        }
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
