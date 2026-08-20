package cn.weitee.erp.module.infra.framework.drm;

/**
 * DRM 适配客户端。
 *
 * 该接口隔离 Java 业务与公司 DRM 服务的传输细节，便于按部署现状切换 HTTP 适配实现。
 */
public interface DrmServiceClient {

    DrmDetectionResult detect(byte[] content, String fileName);

    byte[] decrypt(byte[] content, String fileName, DrmContext context);

    byte[] encrypt(byte[] content, String fileName, DrmContext context);
}
