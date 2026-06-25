package cn.weitee.erp.module.system.controller.admin.postlevel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 岗位任职人员 Response VO")
@Data
public class PostLevelAssignedUserRespVO {

    private Long userId;

    private String username;

    private String nickname;

    private String mobile;

    private Integer status;

    private Boolean primary;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private String remark;

}
