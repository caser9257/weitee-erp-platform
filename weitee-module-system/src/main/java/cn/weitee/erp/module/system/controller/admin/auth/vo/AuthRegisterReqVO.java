package cn.weitee.erp.module.system.controller.admin.auth.vo;

import cn.weitee.erp.module.system.framework.validation.SystemValidationConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "\u7BA1\u7406\u540E\u53F0 - Register Request VO")
@Data
public class AuthRegisterReqVO extends CaptchaVerificationReqVO {

    @Schema(description = "\u7528\u6237\u8D26\u53F7", requiredMode = Schema.RequiredMode.REQUIRED, example = "weitee")
    @NotBlank(message = "\u7528\u6237\u8D26\u53F7\u4E0D\u80FD\u4E3A\u7A7A")
    @Pattern(regexp = SystemValidationConstants.USERNAME_PATTERN, message = "\u7528\u6237\u8D26\u53F7\u652F\u6301\u6C49\u5B57\u3001\u5B57\u6BCD\u3001\u6570\u5B57")
    @Size(min = 1, max = 10, message = "\u7528\u6237\u8D26\u53F7\u957F\u5EA6\u4E3A 1-10 \u4E2A\u5B57\u7B26")
    private String username;

    @Schema(description = "\u7528\u6237\u6635\u79F0", requiredMode = Schema.RequiredMode.REQUIRED, example = "\u828B\u826E")
    @NotBlank(message = "\u7528\u6237\u6635\u79F0\u4E0D\u80FD\u4E3A\u7A7A")
    @Size(max = 30, message = "\u7528\u6237\u6635\u79F0\u957F\u5EA6\u4E0D\u80FD\u8D85\u8FC7 30 \u4E2A\u5B57\u7B26")
    private String nickname;

    @Schema(description = "\u5BC6\u7801", requiredMode = Schema.RequiredMode.REQUIRED, example = "123456")
    @NotEmpty(message = "\u5BC6\u7801\u4E0D\u80FD\u4E3A\u7A7A")
    @Length(min = 4, max = 16, message = "\u5BC6\u7801\u957F\u5EA6\u4E3A 4-16 \u4F4D")
    private String password;
}