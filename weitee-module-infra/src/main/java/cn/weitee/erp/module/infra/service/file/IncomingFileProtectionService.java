package cn.weitee.erp.module.infra.service.file;

import cn.weitee.erp.framework.excel.core.util.FileImportProtector;
import cn.weitee.erp.module.infra.framework.drm.DrmContext;
import cn.weitee.erp.module.infra.framework.drm.DrmPolicyMapper;
import cn.weitee.erp.module.infra.framework.drm.DrmServiceClient;
import org.springframework.stereotype.Service;

/** 文件进入系统时的 DRM 识别与解密。 */
@Service
public class IncomingFileProtectionService implements FileImportProtector {

    private final DrmServiceClient drmServiceClient;
    private final DrmPolicyMapper drmPolicyMapper;

    public IncomingFileProtectionService(DrmServiceClient drmServiceClient, DrmPolicyMapper drmPolicyMapper) {
        this.drmServiceClient = drmServiceClient;
        this.drmPolicyMapper = drmPolicyMapper;
    }

    /**
     * 返回供系统内部处理的明文内容。明文不调用解密服务。
     */
    public byte[] preparePlainContent(byte[] content, String fileName) {
        if (content == null || content.length == 0) {
            return content;
        }
        if (!drmServiceClient.detect(content, fileName).isEncrypted()) {
            return content;
        }
        DrmContext context = drmPolicyMapper.currentUserContext();
        return drmServiceClient.decrypt(content, fileName, context);
    }
}
