package cn.iocoder.yudao.module.erp.controller.admin.finance.vo.apstatement;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - ERP 可核销应付分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpApStatementPaymentEnablePageReqVO extends PageParam {

    @Schema(description = "供应商编号", example = "1")
    private Long supplierId;

    @Schema(description = "业务类型", example = "11")
    private Integer bizType;

    @Schema(description = "台账单号", example = "AP-11-PI-001")
    private String statementNo;

    @Schema(description = "业务单号", example = "PI-001")
    private String bizNo;

    @Schema(description = "结算账户编号", example = "1")
    private Long accountId;

}
