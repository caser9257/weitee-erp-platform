package cn.iocoder.yudao.module.erp.controller.admin.finance.vo.dualledger;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

@Schema(description = "管理后台 - ERP 双账套结果分页 Request VO")
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpFinanceDualLedgerResultPageReqVO extends PageParam {

    @Schema(description = "业务类型", example = "11")
    private Integer bizType;

    @Schema(description = "业务主键", example = "88")
    private Long bizId;

    @Schema(description = "业务单号", example = "CGRK202605240001")
    private String bizNo;

    @Schema(description = "对比状态", example = "20")
    private Integer compareStatus;

    @Schema(description = "是否一致", example = "true")
    private Boolean consistent;
}
