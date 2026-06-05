package cn.iocoder.yudao.module.system.controller.admin.dept.vo.post;

import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 岗位信息的精简 Response VO")
@Data
public class PostSimpleRespVO {

    @Schema(description = "岗位序号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @ExcelProperty("岗位序号")
    private Long id;

    @Schema(description = "岗位名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "小土豆")
    @ExcelProperty("岗位名称")
    private String name;

    @Schema(description = "岗位层级", example = "高级")
    private String level;

    @Schema(description = "所属部门编号", example = "103")
    private Long deptId;

}
