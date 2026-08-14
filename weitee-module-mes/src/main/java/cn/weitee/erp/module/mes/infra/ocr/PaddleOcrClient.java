package cn.weitee.erp.module.mes.infra.ocr;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.Resource;
import java.util.Base64;
import java.util.Map;

/**
 * PaddleOCR HTTP 客户端：调用本地部署的 PaddleOCR 服务（如 FastAPI 封装）。
 * <p>
 * 请求约定：POST {paddle.ocr.url}，body 为 JSON { "image": "<base64>" }；
 * 响应约定：{ "text": "识别出的文本（多行）" }。
 * 若实际 Paddle 服务字段不同，仅需调整本类的请求/响应解析。
 */
@Component
@ConditionalOnProperty(name = "paddle.ocr.mock", havingValue = "false", matchIfMissing = true)
public class PaddleOcrClient implements SopOcrClient {

    @Resource
    private RestTemplate restTemplate;
    @Resource
    private cn.weitee.erp.module.mes.framework.config.MesProperties mesProperties;

    @Override
    @SuppressWarnings("unchecked")
    public String recognize(byte[] imageBytes) {
        String url = mesProperties.getPaddleOcrUrl();
        if (url == null || url.isBlank()) {
            throw new IllegalStateException("未配置 paddle.ocr.url，无法调用 PaddleOCR 服务");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, String> body = Map.of("image", Base64.getEncoder().encodeToString(imageBytes));
        ResponseEntity<Map> response = restTemplate.postForEntity(url, new HttpEntity<>(body, headers), Map.class);
        if (response.getBody() == null) {
            throw new IllegalStateException("PaddleOCR 服务返回为空");
        }
        Object text = response.getBody().get("text");
        return text == null ? "" : String.valueOf(text);
    }
}
