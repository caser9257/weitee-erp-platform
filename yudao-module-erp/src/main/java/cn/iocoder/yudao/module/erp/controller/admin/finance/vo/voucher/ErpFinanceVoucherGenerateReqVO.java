package cn.iocoder.yudao.module.erp.controller.admin.finance.vo.voucher;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.erp.enums.common.ErpBizTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 财务凭证生成 Request VO")
@Data
public class ErpFinanceVoucherGenerateReqVO {

    @Schema(description = "账簿编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "账簿编号不能为空")
    private Long ledgerId;

    @Schema(description = "业务类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "40")
    @NotNull(message = "业务类型不能为空")
    @InEnum(ErpBizTypeEnum.class)
    private Integer bizType;

    @Schema(description = "业务单据编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @NotNull(message = "业务单据编号不能为空")
    private Long bizId;

    @Schema(description = "凭证模板编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @NotNull(message = "凭证模板编号不能为空")
    private Long templateId;

    @Schema(description = "凭证时间，未传则取业务时间", example = "2026-04-29T10:00:00")
    private LocalDateTime voucherTime;

    @Schema(description = "备注", example = "手工触发生成")
    private String remark;
}
