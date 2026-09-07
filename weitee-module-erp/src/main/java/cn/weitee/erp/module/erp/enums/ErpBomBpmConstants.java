package cn.weitee.erp.module.erp.enums;

/**
 * 制造 BOM 生命周期审批常量
 *
 * 制造 BOM 不再提供结构维护入口（唯一来源 = 研发 BOM 审批发布），
 * 停用/废止必须经过 BPM 生命周期申请。
 */
public class ErpBomBpmConstants {

    /**
     * 制造 BOM 停用（废止）申请场景码
     */
    public static final String SCENE_CODE_DISABLE = "erp.mrp.bom.disable";

    public static final String VARIABLE_BOM_ID = "bomId";
    public static final String VARIABLE_BOM_CODE = "bomCode";
    public static final String VARIABLE_PRODUCT_ID = "productId";

}
