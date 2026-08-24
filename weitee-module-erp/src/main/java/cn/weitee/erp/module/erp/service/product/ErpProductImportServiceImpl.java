package cn.weitee.erp.module.erp.service.product;

import cn.hutool.core.util.StrUtil;
import cn.weitee.erp.framework.common.enums.CommonStatusEnum;
import cn.weitee.erp.framework.excel.core.util.FileImportProtector;
import cn.weitee.erp.module.erp.controller.admin.product.vo.category.ErpProductCategoryListReqVO;
import cn.weitee.erp.module.erp.controller.admin.product.vo.product.ErpProductImportResultVO;
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
import java.util.List;
import java.util.Map;

/**
 * ERP 产品导入 Service 实现类
 */
@Service
@Validated
@Slf4j
public class ErpProductImportServiceImpl implements ErpProductImportService {

    @Resource
    private FileImportProtector fileImportProtector;

    /** 模板列定义 */
    private static final String[] HEADERS = {
            "产品名称*", "物料编号", "条码*", "分类名称*", "单位名称*", "规格型号", "状态",
            "保质期(天)", "重量(g)", "采购价", "销售价", "最低价",
            "是否批次管理", "是否来料检验", "是否参与MRP", "备注", "产品封装", "质量等级",
            "品牌/制造商", "替代型号"
    };

    /** 列索引常量 */
    private static final int COL_NAME = 0;
    private static final int COL_MATERIAL_CODE = 1;
    private static final int COL_BAR_CODE = 2;
    private static final int COL_CATEGORY_NAME = 3;
    private static final int COL_UNIT_NAME = 4;
    private static final int COL_STANDARD = 5;
    private static final int COL_STATUS = 6;
    private static final int COL_EXPIRY_DAY = 7;
    private static final int COL_WEIGHT = 8;
    private static final int COL_PURCHASE_PRICE = 9;
    private static final int COL_SALE_PRICE = 10;
    private static final int COL_MIN_PRICE = 11;
    private static final int COL_BATCH_CONTROL = 12;
    private static final int COL_INSPECTION = 13;
    private static final int COL_MRP_ENABLE = 14;
    private static final int COL_REMARK = 15;
    private static final int COL_PACKAGING = 16;
    private static final int COL_QUALITY_GRADE = 17;
    private static final int COL_BRAND_MANUFACTURER = 18;
    private static final int COL_ALTERNATIVE_MODEL = 19;

    private static final DataFormatter DATA_FORMATTER = new DataFormatter();

    @Resource
    private ErpProductMapper erpProductMapper;
    @Resource
    private ErpProductCategoryMapper erpProductCategoryMapper;
    @Resource
    private ErpProductUnitMapper erpProductUnitMapper;

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

            // 创建示例数据行
            Row exampleRow = sheet.createRow(1);
            exampleRow.createCell(COL_NAME).setCellValue("示例电阻");
            exampleRow.createCell(COL_MATERIAL_CODE).setCellValue("MAT-1001");
            exampleRow.createCell(COL_BAR_CODE).setCellValue("R-1001");
            exampleRow.createCell(COL_CATEGORY_NAME).setCellValue("电子料");
            exampleRow.createCell(COL_UNIT_NAME).setCellValue("个");
            exampleRow.createCell(COL_STANDARD).setCellValue("0603 10K 1%");
            exampleRow.createCell(COL_STATUS).setCellValue("1");
            exampleRow.createCell(COL_EXPIRY_DAY).setCellValue("");
            exampleRow.createCell(COL_WEIGHT).setCellValue("0.01");
            exampleRow.createCell(COL_PURCHASE_PRICE).setCellValue("0.05");
            exampleRow.createCell(COL_SALE_PRICE).setCellValue("0.08");
            exampleRow.createCell(COL_MIN_PRICE).setCellValue("0.04");
            exampleRow.createCell(COL_BATCH_CONTROL).setCellValue("0");
            exampleRow.createCell(COL_INSPECTION).setCellValue("1");
            exampleRow.createCell(COL_MRP_ENABLE).setCellValue("1");
            exampleRow.createCell(COL_REMARK).setCellValue("示例数据");
            exampleRow.createCell(COL_PACKAGING).setCellValue("编带");
            exampleRow.createCell(COL_QUALITY_GRADE).setCellValue("A级");
            exampleRow.createCell(COL_BRAND_MANUFACTURER).setCellValue("示例制造商");
            exampleRow.createCell(COL_ALTERNATIVE_MODEL).setCellValue("替代型号-001");

            // 创建填写说明 sheet
            Sheet helpSheet = workbook.createSheet("填写说明");
            String[] helps = {
                    "带 * 号的列为必填列：产品名称、条码、分类名称、单位名称",
                    "分类名称、单位名称必须与系统中已存在的名称完全一致，否则该行导入失败",
                    "状态：1=启用，0=停用，默认为 1",
                    "是否批次管理 / 是否来料检验 / 是否参与MRP：1=是，0=否，默认为 0",
                    "采购价、销售价、最低价、重量、保质期为数字，可留空",
                    "条码已存在时：导入时勾选“自动覆盖现有数据”则更新该产品，否则该行导入失败",
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
        boolean overwrite = Boolean.TRUE.equals(updateSupport);

        // 预加载分类与单位名称映射（避免 N+1）
        Map<String, Long> categoryNameMap = buildCategoryNameMap();
        Map<String, Long> unitNameMap = buildUnitNameMap();

        try {
            byte[] plainContent = fileImportProtector.preparePlainContent(file.getBytes(), file.getOriginalFilename());
            try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(plainContent))) {
            Sheet sheet = workbook.getSheetAt(0);
            int totalRows = sheet.getLastRowNum(); // 0-based，第 0 行为表头
            result.setTotalCount(totalRows);

            for (int i = 1; i <= totalRows; i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }
                try {
                    importRow(row, i, overwrite, categoryNameMap, unitNameMap, result);
                    result.setSuccessCount(result.getSuccessCount() + 1);
                } catch (Exception e) {
                    result.setFailCount(result.getFailCount() + 1);
                    ErpProductImportResultVO.FailDetail failDetail = new ErpProductImportResultVO.FailDetail();
                    failDetail.setRowNumber(i + 1);
                    failDetail.setBarCode(getCellString(row, COL_BAR_CODE));
                    failDetail.setReason(e.getMessage());
                    result.getFailDetails().add(failDetail);
                }
            }
            }
        } catch (IOException e) {
            log.error("[importProducts] 导入产品失败", e);
            throw new RuntimeException("导入产品失败：" + e.getMessage());
        }
        return result;
    }

    private void importRow(Row row, int rowIndex, boolean overwrite, Map<String, Long> categoryNameMap,
                           Map<String, Long> unitNameMap, ErpProductImportResultVO result) {
        String name = getCellString(row, COL_NAME);
        String materialCode = getCellString(row, COL_MATERIAL_CODE);
        String barCode = getCellString(row, COL_BAR_CODE);
        String categoryName = getCellString(row, COL_CATEGORY_NAME);
        String unitName = getCellString(row, COL_UNIT_NAME);

        // 1. 必填校验
        if (StrUtil.isBlank(name)) {
            throw new IllegalArgumentException("产品名称不能为空");
        }
        if (StrUtil.isBlank(barCode)) {
            throw new IllegalArgumentException("条码不能为空");
        }
        if (StrUtil.isBlank(categoryName)) {
            throw new IllegalArgumentException("分类名称不能为空");
        }
        if (StrUtil.isBlank(unitName)) {
            throw new IllegalArgumentException("单位名称不能为空");
        }

        // 2. 分类 / 单位名称解析为编号
        Long categoryId = categoryNameMap.get(categoryName.trim());
        if (categoryId == null) {
            throw new IllegalArgumentException("分类名称不存在：" + categoryName);
        }
        Long unitId = unitNameMap.get(unitName.trim());
        if (unitId == null) {
            throw new IllegalArgumentException("单位名称不存在：" + unitName);
        }

        // 3. 状态解析（默认启用）
        Integer status = parseStatus(getCellString(row, COL_STATUS));

        // 4. 数字字段解析
        BigDecimal weight = parseDecimal(getCellString(row, COL_WEIGHT));
        BigDecimal purchasePrice = parseDecimal(getCellString(row, COL_PURCHASE_PRICE));
        BigDecimal salePrice = parseDecimal(getCellString(row, COL_SALE_PRICE));
        BigDecimal minPrice = parseDecimal(getCellString(row, COL_MIN_PRICE));
        Integer expiryDay = parseInt(getCellString(row, COL_EXPIRY_DAY));

        // 5. 布尔字段解析（默认 false）
        Boolean batchControlFlag = parseBoolean(getCellString(row, COL_BATCH_CONTROL));
        Boolean inspectionRequiredFlag = parseBoolean(getCellString(row, COL_INSPECTION));
        Boolean mrpEnable = parseBoolean(getCellString(row, COL_MRP_ENABLE));

        // 6. 按条码查重：存在时按 updateSupport 决定更新或失败
        ErpProductDO exists = erpProductMapper.selectOne(ErpProductDO::getBarCode, barCode);
        ErpProductDO product = ErpProductDO.builder()
                .name(name.trim())
                .materialCode(StrUtil.isBlank(materialCode) ? null : materialCode.trim())
                .barCode(barCode.trim())
                .categoryId(categoryId)
                .unitId(unitId)
                .status(status)
                .standard(getCellString(row, COL_STANDARD))
                .packaging(getCellString(row, COL_PACKAGING))
                .qualityGrade(getCellString(row, COL_QUALITY_GRADE))
                .brandManufacturer(getCellString(row, COL_BRAND_MANUFACTURER))
                .alternativeModel(getCellString(row, COL_ALTERNATIVE_MODEL))
                .remark(getCellString(row, COL_REMARK))
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
            if (!overwrite) {
                throw new IllegalArgumentException("条码已存在：" + barCode + "（如需覆盖请勾选“自动覆盖现有数据”）");
            }
            product.setId(exists.getId());
            erpProductMapper.updateById(product);
            log.info("[importProducts] 更新产品，row={}, id={}, barCode={}", rowIndex + 1, exists.getId(), barCode);
        } else {
            erpProductMapper.insert(product);
            log.info("[importProducts] 新增产品，row={}, id={}, barCode={}", rowIndex + 1, product.getId(), barCode);
        }
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

    private Map<String, Long> buildUnitNameMap() {
        List<ErpProductUnitDO> units = erpProductUnitMapper.selectList();
        Map<String, Long> map = new HashMap<>();
        for (ErpProductUnitDO unit : units) {
            map.put(unit.getName(), unit.getId());
        }
        return map;
    }

    private Integer parseStatus(String value) {
        if (StrUtil.isBlank(value)) {
            return CommonStatusEnum.ENABLE.getStatus();
        }
        if ("0".equals(value.trim())) {
            return CommonStatusEnum.DISABLE.getStatus();
        }
        if ("1".equals(value.trim())) {
            return CommonStatusEnum.ENABLE.getStatus();
        }
        throw new IllegalArgumentException("状态只支持 0（停用）或 1（启用）");
    }

    private Boolean parseBoolean(String value) {
        if (StrUtil.isBlank(value)) {
            return Boolean.FALSE;
        }
        if ("1".equals(value.trim())) {
            return Boolean.TRUE;
        }
        if ("0".equals(value.trim())) {
            return Boolean.FALSE;
        }
        throw new IllegalArgumentException("是否字段只支持 0（否）或 1（是）");
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

    private Integer parseInt(String value) {
        if (StrUtil.isBlank(value)) {
            return null;
        }
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("整数格式不正确：" + value);
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
