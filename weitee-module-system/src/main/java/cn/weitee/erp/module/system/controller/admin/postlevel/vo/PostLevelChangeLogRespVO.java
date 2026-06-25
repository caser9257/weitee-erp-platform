package cn.weitee.erp.module.system.controller.admin.postlevel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 岗位变动记录 Response VO")
@Data
public class PostLevelChangeLogRespVO {

    private Long userId;

    private String nickname;

    private String action;

    private LocalDateTime actionTime;

    private String remark;

}
