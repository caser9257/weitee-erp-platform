package cn.weitee.erp.module.infra.service.file;

import cn.weitee.erp.module.infra.framework.drm.DrmContext;
import cn.weitee.erp.module.infra.framework.drm.DrmPolicyMapper;
import cn.weitee.erp.module.infra.framework.drm.DrmServiceClient;
import org.springframework.stereotype.Service;

/** 文件对外输出时的 DRM 加密。 */
@Service
public class ProtectedExportService {

    private final DrmServiceClient drmServiceClient;
    private final DrmPolicyMapper drmPolicyMapper;

    public ProtectedExportService(DrmServiceClient drmServiceClient, DrmPolicyMapper drmPolicyMapper) {
        this.drmServiceClient = drmServiceClient;
        this.drmPolicyMapper = drmPolicyMapper;
    }

    /**
     * 对外输出内容进行加密。已是密文时不重复加密。
     */
    public byte[] encryptIfNeeded(byte[] content, String fileName) {
        if (content == null || content.length == 0) {
            return content;
        }
        if (drmServiceClient.detect(content, fileName).isEncrypted()) {
            return content;
        }
        DrmContext context = drmPolicyMapper.currentUserContext();
        return drmServiceClient.encrypt(content, fileName, context);
    }
}
