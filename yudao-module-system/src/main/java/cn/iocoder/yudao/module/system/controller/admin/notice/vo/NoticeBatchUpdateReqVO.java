package cn.iocoder.yudao.module.system.controller.admin.notice.vo;

import cn.iocoder.yudao.framework.common.enums.BatchEditModeEnum;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotBlank;
import java.util.List;

@Schema(description = "管理后台 - 通知公告批量修改 Request VO")
@Data
public class NoticeBatchUpdateReqVO {

    @Schema(description = "通知公告编号列表", requiredMode = Schema.RequiredMode.REQUIRED, example = "[1,2]")
    @NotEmpty(message = "通知公告编号不能为空")
    private List<Long> ids;

    @Schema(description = "修改字段标识", requiredMode = Schema.RequiredMode.REQUIRED, example = "status")
    @NotBlank(message = "修改字段标识不能为空")
    private String fieldKey;

    @Schema(description = "修改模式，参见 BatchEditModeEnum 枚举", requiredMode = Schema.RequiredMode.REQUIRED, example = "overwrite")
    @NotBlank(message = "修改模式不能为空")
    @InEnum(value = BatchEditModeEnum.class, message = "修改模式必须是 {value}")
    private String mode;

    @Schema(description = "修改值", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotBlank(message = "修改值不能为空")
    private String value;

}
