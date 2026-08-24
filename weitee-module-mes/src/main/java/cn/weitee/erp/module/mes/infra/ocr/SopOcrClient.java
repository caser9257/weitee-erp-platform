package cn.weitee.erp.module.mes.infra.ocr;

/**
 * SOP OCR 识别客户端抽象（可插拔：PaddleHTTP / Mock）。
 */
public interface SopOcrClient {

    /**
     * 识别图片，返回识别文本。
     *
     * @param imageBytes 图片字节
     * @return 识别文本（多行）
     */
    String recognize(byte[] imageBytes);
}
