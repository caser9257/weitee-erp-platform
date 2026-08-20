package cn.weitee.erp.module.infra.service.file;

import cn.weitee.erp.framework.excel.core.util.FileExportProtector;
import org.springframework.stereotype.Component;

/** 为通用 Excel 导出工具提供 DRM 保护。 */
@Component
public class DrmFileExportProtector implements FileExportProtector {

    private final ProtectedExportService protectedExportService;

    public DrmFileExportProtector(ProtectedExportService protectedExportService) {
        this.protectedExportService = protectedExportService;
    }

    @Override
    public byte[] protect(byte[] content, String fileName, String contentType) {
        return protectedExportService.encryptIfNeeded(content, fileName);
    }
}
