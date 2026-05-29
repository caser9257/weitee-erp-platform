package cn.iocoder.yudao.module.system.controller.admin.postlevel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 岗位层级详情 Response VO")
@Data
public class PostLevelDetailRespVO {

    private Long postId;

    private String code;

    private String name;

    private String level;

    private Long deptId;

    private String deptName;

    private Integer status;

    private Integer sort;

    private Integer staffQuota;

    private Boolean keyPosition;

    private Boolean allowPartTime;

    private String jobDescription;

    private String remark;

    private Integer assignedUserCount;

    private Integer primaryUserCount;

    private Integer vacancyCount;

    private List<String> riskTags;

    private List<PostLevelAssignedUserRespVO> assignedUsers;

    private List<PostLevelChangeLogRespVO> changeLogs;

}
