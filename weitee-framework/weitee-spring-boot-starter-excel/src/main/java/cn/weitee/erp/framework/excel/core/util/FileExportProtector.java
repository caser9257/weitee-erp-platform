package cn.weitee.erp.framework.excel.core.util;

/**
 * 导出文件保护扩展点。具体实现由基础设施模块提供，Excel 工具在独立运行时不强制依赖 DRM。
 */
public interface FileExportProtector {

    byte[] protect(byte[] content, String fileName, String contentType);
}
