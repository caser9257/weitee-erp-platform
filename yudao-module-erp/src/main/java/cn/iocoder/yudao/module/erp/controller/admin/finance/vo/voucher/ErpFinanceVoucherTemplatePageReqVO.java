package cn.iocoder.yudao.module.erp.controller.admin.finance.vo.voucher;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 财务凭证模板分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpFinanceVoucherTemplatePageReqVO extends PageParam {

    @Schema(description = "账簿编号", example = "1")
    private Long ledgerId;

    @Schema(description = "业务类型", example = "40")
    private Integer bizType;

    @Schema(description = "模板名称", example = "费用报销标准凭证")
    private String name;

    @Schema(description = "状态", example = "0")
    private Integer status;

    @Schema(description = "是否自动生成", example = "false")
    private Boolean autoGenerate;
}
