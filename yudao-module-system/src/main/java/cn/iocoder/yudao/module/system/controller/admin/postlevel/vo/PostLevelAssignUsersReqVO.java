package cn.iocoder.yudao.module.system.controller.admin.postlevel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "绠＄悊鍚庡彴 - 宀椾綅鍒嗛厤浜哄憳 Request VO")
@Data
public class PostLevelAssignUsersReqVO {

    @NotNull(message = "宀椾綅缂栧彿涓嶈兘涓虹┖")
    private Long postId;

    @Valid
    @NotNull(message = "鍒嗛厤浜哄憳鍒楄〃涓嶈兘涓虹┖")
    private List<PostLevelAssignUserItemReqVO> assignments;

}
