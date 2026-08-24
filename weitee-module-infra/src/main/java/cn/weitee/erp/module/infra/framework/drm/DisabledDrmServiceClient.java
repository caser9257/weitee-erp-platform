package cn.weitee.erp.module.infra.framework.drm;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** DRM 未启用时的兼容实现，保留原有文件流程。 */
@Component
@ConditionalOnProperty(name = "drm.enabled", havingValue = "false", matchIfMissing = true)
public class DisabledDrmServiceClient implements DrmServiceClient {

    @Override
    public DrmDetectionResult detect(byte[] content, String fileName) {
        return new DrmDetectionResult(false);
    }

    @Override
    public byte[] decrypt(byte[] content, String fileName, DrmContext context) {
        return content;
    }

    @Override
    public byte[] encrypt(byte[] content, String fileName, DrmContext context) {
        return content;
    }
}
