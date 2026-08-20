package cn.weitee.erp.module.infra.framework.drm;

import cn.weitee.erp.framework.security.core.util.SecurityFrameworkUtils;
import org.springframework.stereotype.Component;

/** 将当前登录用户映射为 DRM 授权参数。 */
@Component
public class DrmPolicyMapper {

    private final DrmProperties properties;

    public DrmPolicyMapper(DrmProperties properties) {
        this.properties = properties;
    }

    public DrmContext currentUserContext() {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        Long deptId = SecurityFrameworkUtils.getLoginUserDeptId();
        String userIdText = userId != null ? String.valueOf(userId) : properties.getDefaultAuthorId();
        int departmentId = deptId != null && deptId <= Integer.MAX_VALUE ? deptId.intValue() : 0;
        return DrmContext.builder()
                .authorId(userIdText)
                .departmentId(departmentId)
                .secretLevelId(properties.getDefaultSecretLevelId())
                .authUserId(userIdText)
                .permission(properties.getDefaultPermission())
                .supportScreenWaterMark(properties.isSupportScreenWaterMark())
                .supportPrintWaterMark(properties.isSupportPrintWaterMark())
                .build();
    }
}
