package cn.iocoder.yudao.module.erp.controller.admin.finance.vo.asset;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - ERP 固定资产状态变更 Request VO")
@Data
public class ErpFinanceAssetStatusReqVO {

    @NotNull(message = "资产编号不能为空")
    private Long id;

    @NotNull(message = "状态不能为空")
    private Integer status;
}
