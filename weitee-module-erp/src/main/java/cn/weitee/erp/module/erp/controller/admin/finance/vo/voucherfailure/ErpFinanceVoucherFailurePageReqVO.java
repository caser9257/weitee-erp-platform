package cn.weitee.erp.module.erp.controller.admin.finance.vo.voucherfailure;

import cn.weitee.erp.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.weitee.erp.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - ERP 凭证生成失败记录分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpFinanceVoucherFailurePageReqVO extends PageParam {

    @Schema(description = "业务类型", example = "21")
    private Integer bizType;

    @Schema(description = "业务单据编号", example = "1024")
    private Long bizId;

    @Schema(description = "状态：0=待重试 1=重试成功 2=人工确认关闭", example = "0")
    private Integer status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
