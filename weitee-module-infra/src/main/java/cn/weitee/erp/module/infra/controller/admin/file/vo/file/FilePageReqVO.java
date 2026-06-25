package cn.weitee.erp.module.infra.controller.admin.file.vo.file;

import cn.weitee.erp.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.weitee.erp.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 文件分页 Request VO")
@Data
public class FilePageReqVO extends PageParam {

    @Schema(description = "文件名，模糊匹配", example = "test")
    private String name;

    @Schema(description = "文件路径，模糊匹配", example = "weitee")
    private String path;

    @Schema(description = "文件类型，模糊匹配", example = "jpg")
    private String type;

    @Schema(description = "文件夹ID")
    private Long folderId;

    @Schema(description = "标签ID")
    private Long tagId;

    @Schema(description = "创建时间", example = "[2022-07-01 00:00:00, 2022-07-01 23:59:59]")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
