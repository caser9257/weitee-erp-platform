package cn.weitee.erp.module.erp.enums;

public class ErpProductBpmConstants {

    public static final String SCENE_CODE = "erp.product.create";

    /**
     * 物料修改审批场景码
     */
    public static final String SCENE_CODE_UPDATE = "erp.product.update";

    /**
     * 物料批量修改审批场景码（批量导入合并为一次审批，bizId = batchId）
     */
    public static final String SCENE_CODE_UPDATE_BATCH = "erp.product.update.batch";

    /**
     * 物料两段式变更：阶段一（变更申请 → 审批 → 解锁编辑权限）
     */
    public static final String SCENE_CODE_CHANGE_REQUEST = "erp.product.change.request";

    /**
     * 物料两段式变更：阶段二（变更完成确认 → 审批 → 生效落库）
     */
    public static final String SCENE_CODE_CHANGE_CONFIRM = "erp.product.change.confirm";

    /**
     * 物料废除审批场景码（一段式：通过即销号留痕、编码释放）
     */
    public static final String SCENE_CODE_OBSOLETE_REQUEST = "erp.product.obsolete.request";

    /**
     * 物料启停审批场景码（一段式：通过即切换启停状态）
     */
    public static final String SCENE_CODE_STATUS_CHANGE = "erp.product.status.change";

    /**
     * 冻结字段：物料编码（ERP 编号）——被 BOM 引用后禁改
     */
    public static final String FROZEN_FIELD_MATERIAL_CODE = "materialCode";

    /**
     * 冻结字段：规格型号——被 BOM 引用后禁改
     */
    public static final String FROZEN_FIELD_STANDARD = "standard";

    public static final String VARIABLE_PRODUCT_ID = "productId";
    public static final String VARIABLE_PRODUCT_NAME = "productName";
    public static final String VARIABLE_MATERIAL_CODE = "materialCode";
    public static final String VARIABLE_CHANGED_FIELDS = "changedFields";
    public static final String VARIABLE_CHANGED_COUNT = "changedCount";

    public static final String VARIABLE_BATCH_ID = "batchId";
    public static final String VARIABLE_BATCH_SIZE = "batchSize";

}
