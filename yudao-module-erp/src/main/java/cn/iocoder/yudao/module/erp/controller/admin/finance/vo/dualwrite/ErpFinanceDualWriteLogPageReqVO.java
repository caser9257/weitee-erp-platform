package cn.iocoder.yudao.module.erp.controller.admin.finance.vo.dualwrite;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - ERP 双写日志分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpFinanceDualWriteLogPageReqVO extends PageParam {

    @Schema(description = "源凭证编号", example = "1")
    private Long sourceVoucherId;

    @Schema(description = "目标凭证编号", example = "2")
    private Long targetVoucherId;

    @Schema(description = "源账簿编号", example = "1")
    private Long sourceLedgerId;

    @Schema(description = "目标账簿编号", example = "2")
    private Long targetLedgerId;

    @Schema(description = "业务类型", example = "1")
    private Integer bizType;

    @Schema(description = "双写状态", example = "1")
    private Integer status;
}
