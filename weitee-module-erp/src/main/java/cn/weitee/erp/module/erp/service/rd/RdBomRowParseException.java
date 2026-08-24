package cn.weitee.erp.module.erp.service.rd;

import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductDO;
import cn.weitee.erp.module.erp.enums.RdBomRowIssueType;
import lombok.Getter;
import lombok.Setter;

/**
 * 研发 BOM 导入行级解析异常（携带失败分类，供导入结果明细与预检查归堆使用）
 *
 * @author WeTai
 */
@Getter
public class RdBomRowParseException extends RuntimeException {

    private final RdBomRowIssueType issueType;
    /**
     * 物料编号（缺档/状态不可用时为触发行上的编码）
     */
    private final String materialCode;
    /**
     * Excel 行内可提取的物料名称（用于待建档清单照单建档，可为空）
     */
    private final String materialName;
    /**
     * 状态不可用时的物料档案（NOT_APPROVED / DISABLED 时携带，可为空）
     */
    private final transient ErpProductDO product;
    /**
     * Excel 行号（从 1 开始，含表头；由解析循环回填）
     */
    @Setter
    private Integer rowNumber;

    public RdBomRowParseException(RdBomRowIssueType issueType, String message) {
        this(issueType, message, null, null, null);
    }

    public RdBomRowParseException(RdBomRowIssueType issueType, String message, String materialCode, String materialName) {
        this(issueType, message, materialCode, materialName, null);
    }

    public RdBomRowParseException(RdBomRowIssueType issueType, String message, String materialCode,
                                  String materialName, ErpProductDO product) {
        super(message);
        this.issueType = issueType;
        this.materialCode = materialCode;
        this.materialName = materialName;
        this.product = product;
    }

}
