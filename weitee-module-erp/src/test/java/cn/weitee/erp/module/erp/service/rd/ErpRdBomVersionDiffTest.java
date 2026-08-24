package cn.weitee.erp.module.erp.service.rd;

import cn.weitee.erp.module.erp.controller.admin.rd.vo.bom.ErpRdBomVersionDiffRespVO;
import cn.weitee.erp.module.erp.dal.dataobject.rd.ErpRdBomItemDO;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * 研发 BOM 版本明细对比算法测试（纯静态方法，无依赖）
 */
class ErpRdBomVersionDiffTest {

    private static ErpRdBomItemDO item(Long id, Long materialId, String usageQty, String designator, String remark) {
        ErpRdBomItemDO item = new ErpRdBomItemDO();
        item.setId(id);
        item.setMaterialId(materialId);
        item.setUsageQty(usageQty != null ? new BigDecimal(usageQty) : null);
        item.setReferenceDesignator(designator);
        item.setRemark(remark);
        return item;
    }

    @Test
    void diff_mixedChanges_shouldClassifyAllCategories() {
        List<ErpRdBomItemDO> sourceItems = List.of(
                item(11L, 101L, "5", "R101-R103", "旧备注"),
                item(12L, 102L, "2", null, null),
                item(13L, 103L, "1", "C1", null));
        List<ErpRdBomItemDO> targetItems = List.of(
                item(21L, 101L, "7", "R101-R105", "新备注"),   // CHANGED
                item(22L, 104L, "3", null, null),              // ADDED
                item(23L, 103L, "1", "C1", null));             // UNCHANGED

        Map<Long, String> names = new HashMap<>();
        names.put(101L, "电阻");
        names.put(102L, "接插件");
        names.put(103L, "电容");
        names.put(104L, "电感");

        List<ErpRdBomVersionDiffRespVO.Entry> entries =
                ErpRdBomServiceImpl.buildVersionDiffEntries(sourceItems, targetItems, names);

        assertEquals(4, entries.size());
        assertEquals("CHANGED", entries.get(0).getChangeType());
        assertEquals("ADDED", entries.get(1).getChangeType());
        assertEquals("UNCHANGED", entries.get(2).getChangeType());
        assertEquals("REMOVED", entries.get(3).getChangeType());

        ErpRdBomVersionDiffRespVO.Entry changed = entries.get(0);
        assertEquals("电阻", changed.getMaterialName());
        assertNotNull(changed.getOldItem());
        assertNotNull(changed.getNewItem());
        // 用量/位号/备注 三处字段级变化
        assertEquals(3, changed.getChanges().size());
        assertEquals("usageQty", changed.getChanges().get(0).getField());
        assertEquals("5", changed.getChanges().get(0).getOldValue());
        assertEquals("7", changed.getChanges().get(0).getNewValue());

        assertNull(entries.get(1).getOldItem());
        assertNull(entries.get(3).getNewItem());
    }

    @Test
    void diff_numericEqualDifferentScale_shouldBeUnchanged() {
        List<ErpRdBomItemDO> sourceItems = List.of(item(11L, 101L, "0.50", null, null));
        List<ErpRdBomItemDO> targetItems = List.of(item(21L, 101L, "0.5", null, null));

        List<ErpRdBomVersionDiffRespVO.Entry> entries =
                ErpRdBomServiceImpl.buildVersionDiffEntries(sourceItems, targetItems, Map.of());

        assertEquals(1, entries.size());
        assertEquals("UNCHANGED", entries.get(0).getChangeType());
        assertNull(entries.get(0).getChanges());
    }

    @Test
    void diff_duplicateMaterialInOneVersion_shouldUseLatestRow() {
        // 同物料两行，取 id 较大者参与对比（防御）
        List<ErpRdBomItemDO> sourceItems = List.of(
                item(11L, 101L, "1", null, null),
                item(12L, 101L, "9", null, null));

        List<ErpRdBomVersionDiffRespVO.Entry> entries =
                ErpRdBomServiceImpl.buildVersionDiffEntries(
                        sourceItems, List.of(item(21L, 101L, "5", null, null)), Map.of());

        assertEquals(1, entries.size());
        assertEquals("CHANGED", entries.get(0).getChangeType());
        assertEquals("9", entries.get(0).getOldItem().getUsageQty().toPlainString());
    }

    @Test
    void diff_identicalLists_shouldAllBeUnchanged() {
        List<ErpRdBomItemDO> sourceItems = List.of(
                item(11L, 101L, "5", "R1", null),
                item(12L, 102L, "2", null, "备注"));

        List<ErpRdBomVersionDiffRespVO.Entry> entries =
                ErpRdBomServiceImpl.buildVersionDiffEntries(
                        sourceItems, List.of(
                                item(21L, 101L, "5", "R1", null),
                                item(22L, 102L, "2", null, "备注")), Map.of());

        assertEquals(2, entries.size());
        entries.forEach(entry -> {
            assertEquals("UNCHANGED", entry.getChangeType());
            assertNull(entry.getChanges());
        });
    }

}
