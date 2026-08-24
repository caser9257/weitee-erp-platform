package cn.weitee.erp.module.mes.infra.ocr;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Mock OCR 客户端：paddle.ocr.mock=true 时启用，用于联调/测试（Paddle 服务未部署时）。
 */
@Component
@ConditionalOnProperty(name = "paddle.ocr.mock", havingValue = "true")
public class MockSopOcrClient implements SopOcrClient {

    @Override
    public String recognize(byte[] imageBytes) {
        return "第一步：确认物料与工装到位。\n"
                + "第二步：按工艺卡要求装夹工件。\n"
                + "第三步：启动设备并核对参数。\n"
                + "第四步：完成后自检并填写报工单。";
    }
}
