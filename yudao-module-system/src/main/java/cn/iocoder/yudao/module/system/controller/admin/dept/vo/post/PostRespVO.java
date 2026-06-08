package cn.iocoder.yudao.module.system.controller.admin.dept.vo.post;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.module.system.enums.DictTypeConstants;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 岗位信息 Response VO")
@Data
@ExcelIgnoreUnannotated
public class PostRespVO {

    @Schema(description = "岗位序号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @ExcelProperty("岗位序号")
    private Long id;

    @Schema(description = "岗位名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "小土豆")
    @ExcelProperty("岗位名称")
    private String name;

    @Schema(description = "岗位编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "yudao")
    @ExcelProperty("岗位编码")
    private String code;

    @Schema(description = "岗位层级", example = "高级")
    @ExcelProperty("岗位层级")
    private String level;

    @Schema(description = "所属部门编号", example = "103")
    private Long deptId;

    @Schema(description = "编制人数", example = "2")
    @ExcelProperty("编制人数")
    private Integer staffQuota;

    @Schema(description = "是否关键岗位", example = "true")
    @ExcelProperty("关键岗位")
    private Boolean keyPosition;

    @Schema(description = "是否允许兼岗", example = "false")
    @ExcelProperty("允许兼岗")
    private Boolean allowPartTime;

    @Schema(description = "岗位说明", example = "负责计划统筹")
    private String jobDescription;

    @Schema(description = "显示顺序", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @ExcelProperty("岗位排序")
    private Integer sort;

    @Schema(description = "状态，参见 CommonStatusEnum 枚举类", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty(value = "状态", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.COMMON_STATUS)
    private Integer status;

    @Schema(description = "备注", example = "快乐的备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
