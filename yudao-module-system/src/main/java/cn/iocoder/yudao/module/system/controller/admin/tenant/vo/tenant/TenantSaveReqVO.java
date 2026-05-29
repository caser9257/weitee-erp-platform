package cn.iocoder.yudao.module.system.controller.admin.tenant.vo.tenant;

import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.module.system.framework.validation.SystemValidationConstants;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "\u7BA1\u7406\u540E\u53F0 - \u79DF\u6237\u521B\u5EFA/\u4FEE\u6539 Request VO")
@Data
public class TenantSaveReqVO {

    @Schema(description = "\u79DF\u6237\u7F16\u53F7", example = "1024")
    private Long id;

    @Schema(description = "\u79DF\u6237\u540D", requiredMode = Schema.RequiredMode.REQUIRED, example = "\u828B\u9053")
    @NotNull(message = "\u79DF\u6237\u540D\u4E0D\u80FD\u4E3A\u7A7A")
    private String name;

    @Schema(description = "\u8054\u7CFB\u4EBA", requiredMode = Schema.RequiredMode.REQUIRED, example = "\u828B\u826E")
    @NotNull(message = "\u8054\u7CFB\u4EBA\u4E0D\u80FD\u4E3A\u7A7A")
    private String contactName;

    @Schema(description = "\u8054\u7CFB\u624B\u673A", example = "15601691300")
    private String contactMobile;

    @Schema(description = "\u79DF\u6237\u72B6\u6001", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "\u79DF\u6237\u72B6\u6001\u4E0D\u80FD\u4E3A\u7A7A")
    private Integer status;

    @Schema(description = "\u7ED1\u5B9A\u57DF\u540D\u6570\u7EC4", example = "https://www.iocoder.cn")
    private List<String> websites;

    @Schema(description = "\u79DF\u6237\u5957\u9910\u7F16\u53F7", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "\u79DF\u6237\u5957\u9910\u7F16\u53F7\u4E0D\u80FD\u4E3A\u7A7A")
    private Long packageId;

    @Schema(description = "\u8FC7\u671F\u65F6\u95F4", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "\u8FC7\u671F\u65F6\u95F4\u4E0D\u80FD\u4E3A\u7A7A")
    private LocalDateTime expireTime;

    @Schema(description = "\u8D26\u53F7\u6570\u91CF", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "\u8D26\u53F7\u6570\u91CF\u4E0D\u80FD\u4E3A\u7A7A")
    private Integer accountCount;

    @Schema(description = "\u7528\u6237\u8D26\u53F7", requiredMode = Schema.RequiredMode.REQUIRED, example = "yudao")
    @Pattern(regexp = SystemValidationConstants.USERNAME_PATTERN, message = "\u7528\u6237\u8D26\u53F7\u652F\u6301\u6C49\u5B57\u3001\u5B57\u6BCD\u3001\u6570\u5B57")
    @Size(min = 1, max = 10, message = "\u7528\u6237\u8D26\u53F7\u957F\u5EA6\u4E3A 1-10 \u4E2A\u5B57\u7B26")
    private String username;

    @Schema(description = "\u5BC6\u7801", requiredMode = Schema.RequiredMode.REQUIRED, example = "123456")
    @Length(min = 4, max = 16, message = "\u5BC6\u7801\u957F\u5EA6\u4E3A 4-16 \u4F4D")
    private String password;

    @AssertTrue(message = "\u7528\u6237\u8D26\u53F7\u3001\u5BC6\u7801\u4E0D\u80FD\u4E3A\u7A7A")
    @JsonIgnore
    public boolean isUsernameValid() {
        return id != null || ObjectUtil.isAllNotEmpty(username, password);
    }

}