package cn.weitee.erp.module.infra.framework.drm;

/** DRM 服务调用失败异常。 */
public class DrmServiceException extends RuntimeException {

    public DrmServiceException(String message) {
        super(message);
    }

    public DrmServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
