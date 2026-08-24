package cn.weitee.erp.module.infra.framework.drm;

import lombok.Value;

/** DRM 密文识别结果。 */
@Value
public class DrmDetectionResult {

    boolean encrypted;
}
