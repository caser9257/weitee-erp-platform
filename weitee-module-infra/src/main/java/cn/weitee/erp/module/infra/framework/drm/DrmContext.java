package cn.weitee.erp.module.infra.framework.drm;

import lombok.Builder;
import lombok.Value;

/** DRM 加密/解密上下文。 */
@Value
@Builder
public class DrmContext {

    String authorId;
    int departmentId;
    int secretLevelId;
    String authUserId;
    int permission;
    boolean supportScreenWaterMark;
    boolean supportPrintWaterMark;
}
