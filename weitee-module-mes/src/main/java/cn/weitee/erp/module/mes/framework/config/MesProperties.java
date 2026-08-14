package cn.weitee.erp.module.mes.framework.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * MES 模块配置：PaddleOCR 服务地址等。
 */
@Component
@ConfigurationProperties(prefix = "mes")
@Data
public class MesProperties {

    /** PaddleOCR 服务地址（如 http://localhost:8866/ocr） */
    private String paddleOcrUrl = "http://localhost:8866/ocr";

}
