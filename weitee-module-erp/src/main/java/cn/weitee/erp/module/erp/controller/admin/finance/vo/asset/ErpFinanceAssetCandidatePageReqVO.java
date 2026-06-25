package cn.weitee.erp.module.erp.controller.admin.finance.vo.asset;

import cn.weitee.erp.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - ERP 固定资产候选分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpFinanceAssetCandidatePageReqVO extends PageParam {

    @Schema(description = "来源类型", example = "10")
    private Integer sourceType;

    @Schema(description = "状态", example = "0")
    private Integer status;
}
