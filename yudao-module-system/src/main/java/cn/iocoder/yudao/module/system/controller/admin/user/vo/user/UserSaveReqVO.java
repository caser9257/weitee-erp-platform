package cn.iocoder.yudao.module.system.controller.admin.user.vo.user;

import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.validation.Mobile;
import cn.iocoder.yudao.module.system.framework.operatelog.core.DeptParseFunction;
import cn.iocoder.yudao.module.system.framework.operatelog.core.PostParseFunction;
import cn.iocoder.yudao.module.system.framework.operatelog.core.SexParseFunction;
import cn.iocoder.yudao.module.system.framework.validation.SystemValidationConstants;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mzt.logapi.starter.annotation.DiffLogField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Set;

@Schema(description = "\u7BA1\u7406\u540E\u53F0 - \u7528\u6237\u521B\u5EFA/\u4FEE\u6539 Request VO")
@Data
public class UserSaveReqVO {

    @Schema(description = "\u7528\u6237\u7F16\u53F7", example = "1024")
    private Long id;

    @Schema(description = "\u7528\u6237\u8D26\u53F7", requiredMode = Schema.RequiredMode.REQUIRED, example = "yudao")
    @NotBlank(message = "\u7528\u6237\u8D26\u53F7\u4E0D\u80FD\u4E3A\u7A7A")
    @Pattern(regexp = SystemValidationConstants.USERNAME_PATTERN, message = "\u7528\u6237\u8D26\u53F7\u652F\u6301\u6C49\u5B57\u3001\u5B57\u6BCD\u3001\u6570\u5B57")
    @Size(min = 1, max = 10, message = "\u7528\u6237\u8D26\u53F7\u957F\u5EA6\u4E3A 1-10 \u4E2A\u5B57\u7B26")
    @DiffLogField(name = "\u7528\u6237\u8D26\u53F7")
    private String username;

    @Schema(description = "\u7528\u6237\u6635\u79F0", requiredMode = Schema.RequiredMode.REQUIRED, example = "\u828B\u826E")
    @Size(max = 30, message = "\u7528\u6237\u6635\u79F0\u957F\u5EA6\u4E0D\u80FD\u8D85\u8FC730\u4E2A\u5B57\u7B26")
    @DiffLogField(name = "\u7528\u6237\u6635\u79F0")
    private String nickname;

    @Schema(description = "\u5907\u6CE8", example = "\u6211\u662F\u4E00\u4E2A\u7528\u6237")
    @DiffLogField(name = "\u5907\u6CE8")
    private String remark;

    @Schema(description = "\u90E8\u95E8\u7F16\u53F7", example = "1024")
    @DiffLogField(name = "\u90E8\u95E8", function = DeptParseFunction.NAME)
    private Long deptId;

    @Schema(description = "\u5C97\u4F4D\u7F16\u53F7\u6570\u7EC4", example = "1")
    @DiffLogField(name = "\u5C97\u4F4D", function = PostParseFunction.NAME)
    private Set<Long> postIds;

    @Schema(description = "\u7528\u6237\u90AE\u7BB1", example = "yudao@iocoder.cn")
    @Email(message = "\u90AE\u7BB1\u683C\u5F0F\u4E0D\u6B63\u786E")
    @Size(max = 50, message = "\u90AE\u7BB1\u957F\u5EA6\u4E0D\u80FD\u8D85\u8FC7 50 \u4E2A\u5B57\u7B26")
    @DiffLogField(name = "\u7528\u6237\u90AE\u7BB1")
    private String email;

    @Schema(description = "\u624B\u673A\u53F7\u7801", example = "15601691300")
    @Mobile
    @DiffLogField(name = "\u624B\u673A\u53F7\u7801")
    private String mobile;

    @Schema(description = "\u7528\u6237\u6027\u522B\uFF0C\u53C2\u89C1 SexEnum \u679A\u4E3E\u7C7B", example = "1")
    @DiffLogField(name = "\u7528\u6237\u6027\u522B", function = SexParseFunction.NAME)
    private Integer sex;

    @Schema(description = "\u7528\u6237\u5934\u50CF", example = "https://www.iocoder.cn/xxx.png")
    @DiffLogField(name = "\u7528\u6237\u5934\u50CF")
    private String avatar;

    @Schema(description = "\u5BC6\u7801", requiredMode = Schema.RequiredMode.REQUIRED, example = "123456")
    @Length(min = 4, max = 16, message = "\u5BC6\u7801\u957F\u5EA6\u4E3A 4-16 \u4F4D")
    private String password;

    @AssertTrue(message = "\u5BC6\u7801\u4E0D\u80FD\u4E3A\u7A7A")
    @JsonIgnore
    public boolean isPasswordValid() {
        return id != null || ObjectUtil.isAllNotEmpty(password);
    }

}