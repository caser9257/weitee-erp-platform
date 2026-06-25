package cn.weitee.erp.module.erp.controller.admin.finance.vo.match;

import cn.weitee.erp.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 三单匹配分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpThreeWayMatchPageReqVO extends PageParam {

    @Schema(description = "租赁合同编号")
    private String leaseContractNo;

    @Schema(description = "服务接收单编号")
    private String serviceReceiptNo;

    @Schema(description = "发票号")
    private String invoiceNo;

    @Schema(description = "匹配结果：0-不匹配 1-完全匹配 2-部分匹配")
    private Integer matchResult;

    @Schema(description = "状态：0-待匹配 10-已确认 20-已生成应付")
    private Integer status;

}
