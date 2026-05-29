import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

public class GenerateBomDemoSql {

    private static final int PRODUCT_COUNT = 35;
    private static final int VARIANTS_PER_FAMILY = 7;
    private static final long TENANT_ID = 1L;
    private static final long UNIT_ID = 972020L;

    private static final long PRODUCT_BASE = 972000L;
    private static final long L1_BASE = 972100L;
    private static final long L2_BASE = 972200L;
    private static final long L3_BASE = 972300L;
    private static final long LEAF_BASE = 972401L;
    private static final long BOM_FG_BASE = 972500L;
    private static final long BOM_L1_BASE = 972600L;
    private static final long BOM_L2_BASE = 972700L;
    private static final long BOM_L3_BASE = 972800L;

    private static final List<CategorySpec> CATEGORIES = List.of(
            new CategorySpec(973010L, 0L, "成品", "FG", 1),
            new CategorySpec(973011L, 973010L, "楼宇温控成品", "FG-HVAC", 1),
            new CategorySpec(973012L, 973010L, "工业电源成品", "FG-POWER", 2),
            new CategorySpec(973013L, 973010L, "采集终端成品", "FG-COLLECT", 3),
            new CategorySpec(973014L, 973010L, "执行控制成品", "FG-ACT", 4),
            new CategorySpec(973015L, 973010L, "网关设备成品", "FG-GATEWAY", 5),
            new CategorySpec(973020L, 0L, "半成品", "SEMI", 2),
            new CategorySpec(973021L, 973020L, "功能总成", "SEMI-ASSY", 1),
            new CategorySpec(973022L, 973021L, "机壳显示类", "SEMI-HMI", 1),
            new CategorySpec(973023L, 973021L, "主控电源类", "SEMI-CTRL", 2),
            new CategorySpec(973024L, 973021L, "接口执行类", "SEMI-IO", 3),
            new CategorySpec(973030L, 0L, "原材料与标准件", "RM", 3),
            new CategorySpec(973031L, 973030L, "电子件", "RM-ELEC", 1),
            new CategorySpec(973032L, 973030L, "结构件", "RM-MECH", 2),
            new CategorySpec(973033L, 973030L, "连接与线束", "RM-CONN", 3),
            new CategorySpec(973034L, 973030L, "包装与标识", "RM-PACK", 4),
            new CategorySpec(973035L, 973030L, "紧固与密封", "RM-FIX", 5)
    );

    private static final List<LeafMaterial> LEAVES = List.of(
            new LeafMaterial("壁挂温控器后壳", 973032L, 7.600000, 10.500000, 9.000000, 0.120000),
            new LeafMaterial("壁挂温控器前壳", 973032L, 8.200000, 11.200000, 9.700000, 0.130000),
            new LeafMaterial("段码液晶屏", 973031L, 11.800000, 15.900000, 13.600000, 0.040000),
            new LeafMaterial("温控主控PCB", 973031L, 19.600000, 26.800000, 22.500000, 0.030000),
            new LeafMaterial("NTC温度探头", 973031L, 4.300000, 6.000000, 5.000000, 0.015000),
            new LeafMaterial("RS485接线端子", 973033L, 1.800000, 2.600000, 2.200000, 0.008000),
            new LeafMaterial("6Pin显示排线", 973033L, 1.300000, 1.900000, 1.600000, 0.006000),
            new LeafMaterial("导轨卡扣", 973032L, 2.800000, 3.900000, 3.300000, 0.025000),
            new LeafMaterial("铝型材散热片", 973032L, 9.800000, 13.200000, 11.300000, 0.180000),
            new LeafMaterial("金属屏蔽罩", 973032L, 3.600000, 5.000000, 4.200000, 0.040000),
            new LeafMaterial("开关电源板", 973031L, 26.500000, 35.800000, 30.400000, 0.120000),
            new LeafMaterial("AC输入端子", 973033L, 2.100000, 3.000000, 2.500000, 0.010000),
            new LeafMaterial("DC输出端子", 973033L, 2.000000, 2.900000, 2.400000, 0.010000),
            new LeafMaterial("绝缘导热垫", 973035L, 0.950000, 1.350000, 1.150000, 0.005000),
            new LeafMaterial("采集终端下壳", 973032L, 10.400000, 14.300000, 12.200000, 0.140000),
            new LeafMaterial("采集终端上盖", 973032L, 9.900000, 13.600000, 11.600000, 0.120000),
            new LeafMaterial("PC透光视窗", 973031L, 2.600000, 3.700000, 3.100000, 0.012000),
            new LeafMaterial("LoRa主控板", 973031L, 28.800000, 39.500000, 33.600000, 0.035000),
            new LeafMaterial("SMA天线座", 973033L, 1.700000, 2.400000, 2.000000, 0.004000),
            new LeafMaterial("LoRa天线线束", 973033L, 3.200000, 4.500000, 3.800000, 0.018000),
            new LeafMaterial("多功能接线端子", 973033L, 2.900000, 4.100000, 3.500000, 0.014000),
            new LeafMaterial("硅胶按键", 973031L, 1.600000, 2.300000, 1.900000, 0.010000),
            new LeafMaterial("阀控金属箱体", 973032L, 18.600000, 25.400000, 21.700000, 0.260000),
            new LeafMaterial("阀控面盖", 973032L, 9.500000, 13.000000, 11.100000, 0.110000),
            new LeafMaterial("继电器驱动板", 973031L, 24.300000, 33.200000, 28.100000, 0.045000),
            new LeafMaterial("阀门反馈线束", 973033L, 4.600000, 6.400000, 5.400000, 0.020000),
            new LeafMaterial("霍尔位置传感器", 973031L, 3.800000, 5.300000, 4.400000, 0.008000),
            new LeafMaterial("保险丝端子座", 973033L, 2.500000, 3.500000, 2.900000, 0.010000),
            new LeafMaterial("硅胶密封圈", 973035L, 1.100000, 1.600000, 1.300000, 0.006000),
            new LeafMaterial("网关下壳", 973032L, 14.800000, 20.100000, 17.300000, 0.210000),
            new LeafMaterial("网关上盖", 973032L, 12.900000, 17.600000, 15.100000, 0.180000),
            new LeafMaterial("网关核心板", 973031L, 38.500000, 52.300000, 44.200000, 0.060000),
            new LeafMaterial("环境传感接口板", 973031L, 13.600000, 18.400000, 15.700000, 0.030000),
            new LeafMaterial("RJ45网口座", 973033L, 2.200000, 3.200000, 2.600000, 0.008000),
            new LeafMaterial("USB调试接口", 973033L, 1.600000, 2.300000, 1.900000, 0.004000),
            new LeafMaterial("WiFi天线", 973033L, 5.200000, 7.100000, 6.000000, 0.012000),
            new LeafMaterial("包装彩盒", 973034L, 3.900000, 5.500000, 4.600000, 0.070000),
            new LeafMaterial("说明书", 973034L, 0.350000, 0.500000, 0.420000, 0.008000),
            new LeafMaterial("合格证", 973034L, 0.180000, 0.260000, 0.220000, 0.003000),
            new LeafMaterial("珍珠棉内托", 973034L, 1.400000, 2.000000, 1.700000, 0.020000),
            new LeafMaterial("M3紧固件包", 973035L, 0.960000, 1.350000, 1.100000, 0.010000),
            new LeafMaterial("防静电袋", 973034L, 0.260000, 0.380000, 0.320000, 0.005000),
            new LeafMaterial("导光柱", 973031L, 0.420000, 0.600000, 0.500000, 0.003000)
    );

    private static final List<FamilySpec> FAMILIES = List.of(
            new FamilySpec("壁挂温控器", "T1", 973011L,
                    "人机外壳总成", "控制采集组件", "传感接线组件",
                    36, 37,
                    1, 0,
                    3,
                    4, 5, 6,
                    118.0, 53.0, 25.0, 11.5,
                    0.98, 0.42, 0.13, 0.05),
            new FamilySpec("导轨电源模块", "P2", 973012L,
                    "机壳散热总成", "功率转换组件", "输入输出接口组件",
                    41, 38,
                    8, 7,
                    10,
                    11, 12, 13,
                    148.0, 69.0, 32.0, 15.0,
                    1.20, 0.48, 0.22, 0.09),
            new FamilySpec("LoRa采集终端", "C3", 973013L,
                    "机壳天线总成", "采集通信组件", "传感接口组件",
                    36, 37,
                    15, 14,
                    17,
                    18, 19, 20,
                    169.0, 75.0, 38.0, 17.5,
                    1.08, 0.44, 0.16, 0.06),
            new FamilySpec("阀门执行控制盒", "A4", 973014L,
                    "箱体驱动总成", "驱动控制组件", "反馈接线组件",
                    36, 38,
                    22, 23,
                    24,
                    25, 26, 27,
                    198.0, 88.0, 42.0, 19.0,
                    1.45, 0.63, 0.21, 0.08),
            new FamilySpec("环境监测网关", "G5", 973015L,
                    "机壳显示总成", "主控通信组件", "电源接口组件",
                    36, 37,
                    30, 29,
                    31,
                    32, 33, 35,
                    238.0, 106.0, 51.0, 23.0,
                    1.68, 0.72, 0.25, 0.10)
    );

    public static void main(String[] args) throws IOException {
        Path output = Path.of("sql/mysql/63-erp-bom-pricing-preview-bulk-demo.sql");
        Files.createDirectories(output.getParent());
        Files.writeString(output, buildSql(), StandardCharsets.UTF_8);
        System.out.println("Wrote " + output.toAbsolutePath());
    }

    private static String buildSql() {
        StringBuilder sql = new StringBuilder(160_000);
        add(sql, "/*");
        add(sql, "  标准四层BOM演示数据");
        add(sql, "  分类树收敛为：成品 / 半成品 / 原材料与标准件");
        add(sql, "  BOM层级为：成品 -> 功能总成 -> 子组件 -> 基础件");
        add(sql, "*/");
        add(sql, "");
        add(sql, "SET NAMES utf8mb4;");
        add(sql, "SET FOREIGN_KEY_CHECKS = 0;");
        add(sql, "USE `ruoyi-vue-pro`;");
        add(sql, "");
        add(sql, "START TRANSACTION;");
        add(sql, "");
        add(sql, "DELETE FROM `erp_bom_item` WHERE `id` BETWEEN 961300 AND 961399 OR `id` BETWEEN 972900 AND 973399;");
        add(sql, "DELETE FROM `erp_bom` WHERE `id` BETWEEN 961200 AND 961219 OR `id` BETWEEN 972500 AND 972899;");
        add(sql, "DELETE FROM `erp_product` WHERE `id` BETWEEN 961001 AND 961030 OR `id` BETWEEN 972000 AND 972499;");
        add(sql, "DELETE FROM `erp_product_category` WHERE (`id` BETWEEN 961010 AND 961019) OR (`id` BETWEEN 972010 AND 973099);");
        add(sql, "DELETE FROM `erp_product_unit` WHERE `id` IN (961020, 972020);");
        add(sql, "");
        add(sql, "INSERT INTO `erp_product_unit`");
        add(sql, "(`id`, `name`, `status`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)");
        add(sql, "VALUES");
        add(sql, "(972020, '套', 0, '1', NOW(), '1', NOW(), b'0', 1);");
        add(sql, "");
        add(sql, "INSERT INTO `erp_product_category`");
        add(sql, "(`id`, `parent_id`, `name`, `code`, `sort`, `status`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)");
        add(sql, "VALUES");
        for (int i = 0; i < CATEGORIES.size(); i++) {
            String suffix = i == CATEGORIES.size() - 1 ? ";" : ",";
            add(sql, rowCategory(CATEGORIES.get(i)) + suffix);
        }
        add(sql, "");
        add(sql, "INSERT INTO `erp_product`");
        add(sql, "(`id`, `name`, `bar_code`, `category_id`, `unit_id`, `status`, `standard`, `remark`, `expiry_day`, `weight`, `purchase_price`, `sale_price`, `min_price`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)");
        add(sql, "VALUES");
        for (int i = 0; i < LEAVES.size(); i++) {
            LeafMaterial leaf = LEAVES.get(i);
            String suffix = i == LEAVES.size() - 1 ? ";" : ",";
            add(sql, rowProduct(
                    leafProductId(i),
                    leaf.name(),
                    "RM-" + pad3(i + 1),
                    leaf.categoryId(),
                    leaf.name(),
                    "标准四层BOM基础件",
                    leaf.weight(),
                    leaf.purchasePrice(),
                    leaf.salePrice(),
                    leaf.minPrice(),
                    suffix));
        }
        add(sql, "");
        add(sql, "INSERT INTO `erp_product`");
        add(sql, "(`id`, `name`, `bar_code`, `category_id`, `unit_id`, `status`, `standard`, `remark`, `expiry_day`, `weight`, `purchase_price`, `sale_price`, `min_price`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)");
        add(sql, "VALUES");
        for (int seq = 1; seq <= PRODUCT_COUNT; seq++) {
            int familyIndex = (seq - 1) / VARIANTS_PER_FAMILY;
            int variant = ((seq - 1) % VARIANTS_PER_FAMILY) + 1;
            FamilySpec family = FAMILIES.get(familyIndex);
            String modelCode = family.modelPrefix() + pad2(variant);
            ProductSpec fg = productSpec(family.fgBasePrice(), variant, 3.6, 1.36, 1.20);
            ProductSpec l1 = productSpec(family.l1BasePrice(), variant, 1.7, 1.34, 1.18);
            ProductSpec l2 = productSpec(family.l2BasePrice(), variant, 0.95, 1.32, 1.16);
            ProductSpec l3 = productSpec(family.l3BasePrice(), variant, 0.42, 1.30, 1.14);
            String suffix = seq == PRODUCT_COUNT ? ";" : ",";

            add(sql, rowProduct(
                    PRODUCT_BASE + seq,
                    finishedProductName(family, modelCode),
                    "FG-" + modelCode,
                    family.categoryId(),
                    family.name() + "标准版",
                    "标准四层BOM整机",
                    family.fgBaseWeight() + familyIndex * 0.030000 + variant * 0.012000,
                    fg.purchasePrice(),
                    fg.salePrice(),
                    fg.minPrice(),
                    ","));
            add(sql, rowProduct(
                    L1_BASE + seq,
                    assemblyProductName(family, family.level1Name(), modelCode),
                    "L1-" + modelCode,
                    973022L,
                    family.level1Name(),
                    "L1功能总成",
                    family.l1BaseWeight() + familyIndex * 0.020000 + variant * 0.007000,
                    l1.purchasePrice(),
                    l1.salePrice(),
                    l1.minPrice(),
                    ","));
            add(sql, rowProduct(
                    L2_BASE + seq,
                    assemblyProductName(family, family.level2Name(), modelCode),
                    "L2-" + modelCode,
                    973023L,
                    family.level2Name(),
                    "L2子组件",
                    family.l2BaseWeight() + familyIndex * 0.010000 + variant * 0.004000,
                    l2.purchasePrice(),
                    l2.salePrice(),
                    l2.minPrice(),
                    ","));
            add(sql, rowProduct(
                    L3_BASE + seq,
                    assemblyProductName(family, family.level3Name(), modelCode),
                    "L3-" + modelCode,
                    973024L,
                    family.level3Name(),
                    "L3基础组件",
                    family.l3BaseWeight() + familyIndex * 0.005000 + variant * 0.002000,
                    l3.purchasePrice(),
                    l3.salePrice(),
                    l3.minPrice(),
                    suffix));
        }
        add(sql, "");
        add(sql, "INSERT INTO `erp_bom`");
        add(sql, "(`id`, `bom_code`, `product_id`, `version`, `status`, `source_rd_bom_id`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)");
        add(sql, "VALUES");
        for (int seq = 1; seq <= PRODUCT_COUNT; seq++) {
            int familyIndex = (seq - 1) / VARIANTS_PER_FAMILY;
            int variant = ((seq - 1) % VARIANTS_PER_FAMILY) + 1;
            FamilySpec family = FAMILIES.get(familyIndex);
            String modelCode = family.modelPrefix() + pad2(variant);
            String suffix = seq == PRODUCT_COUNT ? ";" : ",";

            add(sql, rowBom(BOM_FG_BASE + seq, "BOM-FG-" + modelCode, PRODUCT_BASE + seq,
                    finishedProductName(family, modelCode) + " 整机BOM", ","));
            add(sql, rowBom(BOM_L1_BASE + seq, "BOM-L1-" + modelCode, L1_BASE + seq,
                    assemblyProductName(family, family.level1Name(), modelCode) + " BOM", ","));
            add(sql, rowBom(BOM_L2_BASE + seq, "BOM-L2-" + modelCode, L2_BASE + seq,
                    assemblyProductName(family, family.level2Name(), modelCode) + " BOM", ","));
            add(sql, rowBom(BOM_L3_BASE + seq, "BOM-L3-" + modelCode, L3_BASE + seq,
                    assemblyProductName(family, family.level3Name(), modelCode) + " BOM", suffix));
        }
        add(sql, "");
        add(sql, "INSERT INTO `erp_bom_item`");
        add(sql, "(`id`, `bom_id`, `material_id`, `material_type`, `unit_id`, `usage_qty`, `loss_rate`, `lead_time_day`, `mrp_enable_flag`, `supply_owner`, `sort`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)");
        add(sql, "VALUES");
        long itemId = 972900L;
        for (int seq = 1; seq <= PRODUCT_COUNT; seq++) {
            int familyIndex = (seq - 1) / VARIANTS_PER_FAMILY;
            int variant = ((seq - 1) % VARIANTS_PER_FAMILY) + 1;
            FamilySpec family = FAMILIES.get(familyIndex);
            String modelCode = family.modelPrefix() + pad2(variant);
            long fgBom = BOM_FG_BASE + seq;
            long l1Bom = BOM_L1_BASE + seq;
            long l2Bom = BOM_L2_BASE + seq;
            long l3Bom = BOM_L3_BASE + seq;
            long l1Product = L1_BASE + seq;
            long l2Product = L2_BASE + seq;
            long l3Product = L3_BASE + seq;

            String fgName = finishedProductName(family, modelCode);
            String l1Name = assemblyProductName(family, family.level1Name(), modelCode);
            String l2Name = assemblyProductName(family, family.level2Name(), modelCode);
            String l3Name = assemblyProductName(family, family.level3Name(), modelCode);

            add(sql, rowBomItem(itemId++, fgBom, l1Product, 1, 1.000000, 0.015000, 3, 1, "COMPANY", 1,
                    fgName + " 装配 " + l1Name) + ",");
            add(sql, rowBomItem(itemId++, fgBom, leafProductId(family.fgLeafAIndex()), 0, 1.000000, 0.010000, 1, 1, "COMPANY", 2,
                    fgName + " 使用 " + leafName(family.fgLeafAIndex())) + ",");
            add(sql, rowBomItem(itemId++, fgBom, leafProductId(family.fgLeafBIndex()), 0, 1.000000, 0.010000, 1, 1, "COMPANY", 3,
                    fgName + " 使用 " + leafName(family.fgLeafBIndex())) + ",");

            add(sql, rowBomItem(itemId++, l1Bom, l2Product, 1, 1.000000, 0.015000, 2, 1, "COMPANY", 1,
                    l1Name + " 装配 " + l2Name) + ",");
            add(sql, rowBomItem(itemId++, l1Bom, leafProductId(family.l1LeafAIndex()), 0, 1.000000, 0.010000, 2, 1, "COMPANY", 2,
                    l1Name + " 使用 " + leafName(family.l1LeafAIndex())) + ",");
            add(sql, rowBomItem(itemId++, l1Bom, leafProductId(family.l1LeafBIndex()), 0, 1.000000, 0.010000, 2, 1, "COMPANY", 3,
                    l1Name + " 使用 " + leafName(family.l1LeafBIndex())) + ",");

            add(sql, rowBomItem(itemId++, l2Bom, l3Product, 1, 1.000000, 0.015000, 2, 1, "COMPANY", 1,
                    l2Name + " 装配 " + l3Name) + ",");
            add(sql, rowBomItem(itemId++, l2Bom, leafProductId(family.l2LeafIndex()), 0, 1.000000, 0.010000, 2, 1, "COMPANY", 2,
                    l2Name + " 使用 " + leafName(family.l2LeafIndex())) + ",");

            add(sql, rowBomItem(itemId++, l3Bom, leafProductId(family.l3LeafAIndex()), 0, 1.000000, 0.010000, 1, 1, "COMPANY", 1,
                    l3Name + " 使用 " + leafName(family.l3LeafAIndex())) + ",");
            add(sql, rowBomItem(itemId++, l3Bom, leafProductId(family.l3LeafBIndex()), 0, 1.000000, 0.010000, 1, 1, "COMPANY", 2,
                    l3Name + " 使用 " + leafName(family.l3LeafBIndex())) + ",");
            String suffix = seq == PRODUCT_COUNT ? ";" : ",";
            add(sql, rowBomItem(itemId++, l3Bom, leafProductId(family.l3LeafCIndex()), 0, 1.000000, 0.010000, 1, 1, "COMPANY", 3,
                    l3Name + " 使用 " + leafName(family.l3LeafCIndex())) + suffix);
        }
        add(sql, "");
        add(sql, "COMMIT;");
        add(sql, "");
        add(sql, "SET FOREIGN_KEY_CHECKS = 1;");
        return sql.toString();
    }

    private static ProductSpec productSpec(double base, int variant, double variantStep,
                                           double saleFactor, double minFactor) {
        double purchase = base + variant * variantStep;
        return new ProductSpec(round6(purchase), round6(purchase * saleFactor), round6(purchase * minFactor));
    }

    private static String finishedProductName(FamilySpec family, String modelCode) {
        return family.name() + " " + modelCode;
    }

    private static String assemblyProductName(FamilySpec family, String levelName, String modelCode) {
        return family.name() + levelName + " " + modelCode;
    }

    private static String leafName(int leafIndex) {
        return LEAVES.get(leafIndex).name();
    }

    private static long leafProductId(int leafIndex) {
        return LEAF_BASE + leafIndex;
    }

    private static String rowCategory(CategorySpec category) {
        return "(" + category.id() + ", " + category.parentId() + ", '" + esc(category.name()) + "', '" + esc(category.code())
                + "', " + category.sort() + ", 0, '1', NOW(), '1', NOW(), b'0', " + TENANT_ID + ")";
    }

    private static String rowProduct(long id, String name, String barCode, long categoryId, String standard, String remark,
                                     double weight, double purchasePrice, double salePrice, double minPrice, String suffix) {
        return "(" + id + ", '" + esc(name) + "', '" + esc(barCode) + "', " + categoryId + ", " + UNIT_ID
                + ", 0, '" + esc(standard) + "', '" + esc(remark) + "', 3650, " + fmt(weight) + ", "
                + fmt(purchasePrice) + ", " + fmt(salePrice) + ", " + fmt(minPrice)
                + ", '1', NOW(), '1', NOW(), b'0', " + TENANT_ID + ")" + suffix;
    }

    private static String rowBom(long id, String bomCode, long productId, String remark, String suffix) {
        return "(" + id + ", '" + esc(bomCode) + "', " + productId + ", 'V1.0', 1, NULL, '" + esc(remark)
                + "', '1', NOW(), '1', NOW(), b'0')" + suffix;
    }

    private static String rowBomItem(long id, long bomId, long materialId, int materialType, double usageQty,
                                     double lossRate, int leadTimeDay, int mrpEnableFlag, String supplyOwner,
                                     int sort, String remark) {
        return "(" + id + ", " + bomId + ", " + materialId + ", " + materialType + ", " + UNIT_ID + ", "
                + fmt(usageQty) + ", " + fmt(lossRate) + ", " + leadTimeDay + ", " + mrpEnableFlag + ", '"
                + esc(supplyOwner) + "', " + sort + ", '" + esc(remark) + "', '1', NOW(), '1', NOW(), b'0')";
    }

    private static void add(StringBuilder sql, String line) {
        sql.append(line).append('\n');
    }

    private static String pad2(int value) {
        return String.format(Locale.ROOT, "%02d", value);
    }

    private static String pad3(int value) {
        return String.format(Locale.ROOT, "%03d", value);
    }

    private static String esc(String value) {
        return value.replace("'", "''");
    }

    private static String fmt(double value) {
        return String.format(Locale.ROOT, "%.6f", value);
    }

    private static double round6(double value) {
        return Math.round(value * 1_000_000d) / 1_000_000d;
    }

    private record ProductSpec(double purchasePrice, double salePrice, double minPrice) {
    }

    private record CategorySpec(long id, long parentId, String name, String code, int sort) {
    }

    private record FamilySpec(String name, String modelPrefix, long categoryId,
                              String level1Name, String level2Name, String level3Name,
                              int fgLeafAIndex, int fgLeafBIndex,
                              int l1LeafAIndex, int l1LeafBIndex,
                              int l2LeafIndex,
                              int l3LeafAIndex, int l3LeafBIndex, int l3LeafCIndex,
                              double fgBasePrice, double l1BasePrice, double l2BasePrice, double l3BasePrice,
                              double fgBaseWeight, double l1BaseWeight, double l2BaseWeight, double l3BaseWeight) {
    }

    private record LeafMaterial(String name, long categoryId, double purchasePrice, double salePrice,
                                double minPrice, double weight) {
    }
}
