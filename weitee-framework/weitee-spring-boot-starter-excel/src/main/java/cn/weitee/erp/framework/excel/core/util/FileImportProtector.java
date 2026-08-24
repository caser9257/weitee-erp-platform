package cn.weitee.erp.framework.excel.core.util;

/**
 * 文件进入业务解析器前的保护扩展点。
 *
 * <p>基础设施模块可实现 DRM 解密；未启用保护时应原样返回内容。</p>
 */
public interface FileImportProtector {

    byte[] preparePlainContent(byte[] content, String fileName);
}
