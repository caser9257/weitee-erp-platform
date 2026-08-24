package cn.weitee.erp.module.infra.framework.drm;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * DRM 服务配置。
 *
 * 默认关闭 DRM，避免未配置服务时影响既有文件流程。
 */
@Component
@ConfigurationProperties(prefix = "drm")
@Data
public class DrmProperties {

    /** 是否启用 DRM 处理 */
    private boolean enabled;

    /** DRM 适配服务地址，不包含具体操作路径 */
    private String serviceUrl;

    /** 密文识别路径 */
    private String detectPath = "/detect";

    /** 解密路径 */
    private String decryptPath = "/decrypt";

    /** 加密路径 */
    private String encryptPath = "/encrypt";

    /** 连接超时，单位毫秒 */
    private int connectTimeout = 3000;

    /** 读取超时，单位毫秒 */
    private int readTimeout = 60000;

    /** 单次处理最大文件大小，单位字节 */
    private long maxFileSize = 500L * 1024 * 1024;

    /** 适配服务认证令牌，通过部署环境注入 */
    private String authToken;

    /** 作者默认值，需要时由当前登录用户覆盖 */
    private String defaultAuthorId = "system";

    /** DRM 默认密级，手册中 1-6 对应六类密级 */
    private int defaultSecretLevelId = 5;

    /** DRM 默认权限，Read=1 */
    private int defaultPermission = 1;

    /** 是否开启屏幕水印 */
    private boolean supportScreenWaterMark;

    /** 是否开启打印水印 */
    private boolean supportPrintWaterMark;
}
