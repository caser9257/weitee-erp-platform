package cn.weitee.erp.module.erp.enums;

public class ErpRdBomBpmConstants {

    public static final String SCENE_CODE = "erp.rd.bom.submit";

    public static final String VARIABLE_BOM_ID = "bomId";
    public static final String VARIABLE_BOM_CODE = "bomCode";
    public static final String VARIABLE_PRODUCT_ID = "productId";

    /** 是否首次提交（无对比基准版本） */
    public static final String VARIABLE_FIRST_SUBMIT = "firstSubmit";
    /** 对比基准版本号（首次提交时无值） */
    public static final String VARIABLE_BASELINE_VERSION = "baselineVersion";
    /** 与基准版本相比：新增明细数 */
    public static final String VARIABLE_DIFF_ADDED_COUNT = "diffAddedCount";
    /** 与基准版本相比：删除明细数 */
    public static final String VARIABLE_DIFF_REMOVED_COUNT = "diffRemovedCount";
    /** 与基准版本相比：修改明细数 */
    public static final String VARIABLE_DIFF_CHANGED_COUNT = "diffChangedCount";

}
