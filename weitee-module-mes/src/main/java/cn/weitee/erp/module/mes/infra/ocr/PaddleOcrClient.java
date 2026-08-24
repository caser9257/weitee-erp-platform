package cn.weitee.erp.module.mes.infra.ocr;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.Resource;
import java.util.Map;

import static java.util.stream.Collectors.joining;

/**
 * PaddleOCR HTTP 客户端：调用本地部署的 PaddleOCR 服务（如 FastAPI 封装）。
 * <p>
 * 请求约定：POST {paddle.ocr.url}，multipart 字段为 {@code file}；
 * 响应兼容简单的 {@code text} 字段，以及 PaddleX 服务返回的
 * {@code raw_result.rec_texts} 数组。
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
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        ByteArrayResource image = new ByteArrayResource(imageBytes) {
            @Override
            public String getFilename() {
                return "sop.png";
            }
        };
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", image);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, new HttpEntity<>(body, headers), Map.class);
        Map responseBody = response.getBody();
        if (responseBody == null) {
            throw new IllegalStateException("PaddleOCR 服务返回为空");
        }
        String text = extractText(responseBody);
        if (!text.isBlank()) {
            return text;
        }
        throw new IllegalStateException("PaddleOCR 服务未返回识别文本");
    }

    private String extractText(Object value) {
        if (value instanceof String text) {
            return text.trim();
        }
        if (!(value instanceof Map<?, ?> map)) {
            if (value instanceof Iterable<?> values) {
                return joinValues(values);
            }
            if (value != null && value.getClass().isArray()) {
                int length = java.lang.reflect.Array.getLength(value);
                java.util.List<Object> values = new java.util.ArrayList<>(length);
                for (int i = 0; i < length; i++) {
                    values.add(java.lang.reflect.Array.get(value, i));
                }
                return joinValues(values);
            }
            return "";
        }
        String text = extractText(map.get("text"));
        if (!text.isBlank()) {
            return text;
        }
        text = extractText(map.get("rec_texts"));
        if (!text.isBlank()) {
            return text;
        }
        text = extractText(map.get("data"));
        if (!text.isBlank()) {
            return text;
        }
        text = extractText(map.get("raw_result"));
        if (!text.isBlank()) {
            return text;
        }
        text = extractText(map.get("overall_ocr_res"));
        if (!text.isBlank()) {
            return text;
        }
        return "";
    }

    private String joinValues(Iterable<?> values) {
        return java.util.stream.StreamSupport.stream(values.spliterator(), false)
                .map(String::valueOf)
                .map(String::trim)
                .filter(item -> !item.isBlank())
                .collect(joining("\n"));
    }
}
