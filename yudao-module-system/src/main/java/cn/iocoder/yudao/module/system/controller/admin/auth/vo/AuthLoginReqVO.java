package cn.iocoder.yudao.module.system.controller.admin.auth.vo;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.system.enums.social.SocialTypeEnum;
import cn.iocoder.yudao.module.system.framework.validation.SystemValidationConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Schema(description = "\u7BA1\u7406\u540E\u53F0 - \u8D26\u53F7\u5BC6\u7801\u767B\u5F55 Request VO\uFF1B\u5982\u679C\u767B\u5F55\u5E76\u7ED1\u5B9A\u793E\u4EA4\u7528\u6237\uFF0C\u9700\u8981\u4F20\u9012 social \u5F00\u5934\u7684\u53C2\u6570")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthLoginReqVO extends CaptchaVerificationReqVO {

    @Schema(description = "\u8D26\u53F7", requiredMode = Schema.RequiredMode.REQUIRED, example = "yudaoyuanma")
    @NotEmpty(message = "\u767B\u5F55\u8D26\u53F7\u4E0D\u80FD\u4E3A\u7A7A")
    @Size(min = 1, max = 10, message = "\u8D26\u53F7\u957F\u5EA6\u4E3A 1-10 \u4F4D")
    @Pattern(regexp = SystemValidationConstants.USERNAME_PATTERN,
            message = "\u8D26\u53F7\u652F\u6301\u6C49\u5B57\u3001\u5B57\u6BCD\u3001\u6570\u5B57\uFF0C\u957F\u5EA6\u4E3A 1-10 \u4F4D")
    private String username;

    @Schema(description = "\u5BC6\u7801", requiredMode = Schema.RequiredMode.REQUIRED, example = "buzhidao")
    @NotEmpty(message = "\u5BC6\u7801\u4E0D\u80FD\u4E3A\u7A7A")
    @Length(min = 4, max = 16, message = "\u5BC6\u7801\u957F\u5EA6\u4E3A 4-16 \u4F4D")
    private String password;

    @Schema(description = "\u793E\u4EA4\u5E73\u53F0\u7684\u7C7B\u578B\uFF0C\u53C2\u89C1 SocialTypeEnum \u679A\u4E3E\u503C", requiredMode = Schema.RequiredMode.REQUIRED,
            example = "10")
    @InEnum(SocialTypeEnum.class)
    private Integer socialType;

    @Schema(description = "\u6388\u6743\u7801", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private String socialCode;

    @Schema(description = "state", requiredMode = Schema.RequiredMode.REQUIRED,
            example = "9b2ffbc1-7425-4155-9894-9d5c08541d62")
    private String socialState;

    @AssertTrue(message = "\u6388\u6743\u7801\u4E0D\u80FD\u4E3A\u7A7A")
    public boolean isSocialCodeValid() {
        return socialType == null || StrUtil.isNotEmpty(socialCode);
    }

    @AssertTrue(message = "\u6388\u6743 state \u4E0D\u80FD\u4E3A\u7A7A")
    public boolean isSocialState() {
        return socialType == null || StrUtil.isNotEmpty(socialState);
    }

}