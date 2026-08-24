package cn.weitee.erp.module.infra.framework.drm;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.util.Iterator;
import java.util.Map;

/**
 * DRM HTTP 适配客户端。
 *
 * 路径和认证由配置提供，不将厂商服务的未知路径写死在业务层。
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "drm.enabled", havingValue = "true")
public class HttpDrmServiceClient implements DrmServiceClient {

    private final RestTemplate restTemplate;
    private final DrmProperties properties;

    public HttpDrmServiceClient(RestTemplateBuilder restTemplateBuilder, DrmProperties properties) {
        this.properties = properties;
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofMillis(properties.getConnectTimeout()))
                .setReadTimeout(Duration.ofMillis(properties.getReadTimeout()))
                .build();
    }

    @Override
    public DrmDetectionResult detect(byte[] content, String fileName) {
        JsonNode body = exchangeJson(properties.getDetectPath(), content, fileName, null);
        Boolean encrypted = readEncrypted(body);
        if (encrypted == null) {
            throw new DrmServiceException("DRM 密文识别响应缺少 encrypted 字段");
        }
        return new DrmDetectionResult(encrypted);
    }

    @Override
    public byte[] decrypt(byte[] content, String fileName, DrmContext context) {
        return exchangeBytes(properties.getDecryptPath(), content, fileName, context);
    }

    @Override
    public byte[] encrypt(byte[] content, String fileName, DrmContext context) {
        return exchangeBytes(properties.getEncryptPath(), content, fileName, context);
    }

    private JsonNode exchangeJson(String path, byte[] content, String fileName, DrmContext context) {
        ResponseEntity<JsonNode> response = exchange(path, content, fileName, context, JsonNode.class);
        return response.getBody();
    }

    private byte[] exchangeBytes(String path, byte[] content, String fileName, DrmContext context) {
        ResponseEntity<byte[]> response = exchange(path, content, fileName, context, byte[].class);
        byte[] result = response.getBody();
        if (result == null || (content.length > 0 && result.length == 0)) {
            throw new DrmServiceException("DRM 服务返回空文件");
        }
        checkSize(result);
        return result;
    }

    private <T> ResponseEntity<T> exchange(String path, byte[] content, String fileName,
                                            DrmContext context, Class<T> responseType) {
        checkSize(content);
        String url = buildUrl(path);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        if (properties.getAuthToken() != null && !properties.getAuthToken().isBlank()) {
            String token = properties.getAuthToken().trim();
            headers.set(HttpHeaders.AUTHORIZATION, token.regionMatches(true, 0, "Bearer ", 0, 7)
                    ? token : "Bearer " + token);
        }

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(content) {
            @Override
            public String getFilename() {
                return fileName == null || fileName.isBlank() ? "file.bin" : fileName;
            }
        });
        body.add("fileName", fileName);
        if (context != null) {
            body.add("authorId", context.getAuthorId());
            body.add("departmentId", String.valueOf(context.getDepartmentId()));
            body.add("secretLevelId", String.valueOf(context.getSecretLevelId()));
            body.add("authUserId", context.getAuthUserId());
            body.add("permission", String.valueOf(context.getPermission()));
            body.add("supportScreenWaterMark", String.valueOf(context.isSupportScreenWaterMark()));
            body.add("supportPrintWaterMark", String.valueOf(context.isSupportPrintWaterMark()));
        }

        try {
            return restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(body, headers), responseType);
        } catch (RestClientResponseException ex) {
            log.warn("[drmExchange][DRM 服务返回失败，path={}, status={}]", path, ex.getStatusCode().value());
            throw new DrmServiceException("DRM 服务处理失败", ex);
        } catch (Exception ex) {
            log.warn("[drmExchange][DRM 服务调用异常，path={}]", path, ex);
            throw new DrmServiceException("DRM 服务不可用", ex);
        }
    }

    private String buildUrl(String path) {
        if (properties.getServiceUrl() == null || properties.getServiceUrl().isBlank()) {
            throw new DrmServiceException("未配置 drm.service-url，无法调用 DRM 服务");
        }
        return UriComponentsBuilder.fromHttpUrl(properties.getServiceUrl())
                .path(path == null ? "" : path)
                .toUriString();
    }

    private void checkSize(byte[] content) {
        if (content == null) {
            throw new DrmServiceException("DRM 文件内容为空");
        }
        if (content.length > properties.getMaxFileSize()) {
            throw new DrmServiceException("DRM 文件超过大小限制");
        }
    }

    private Boolean readEncrypted(JsonNode body) {
        if (body == null || body.isNull()) {
            return null;
        }
        if (body.isBoolean()) {
            return body.booleanValue();
        }
        for (String field : new String[]{"encrypted", "isEncrypted"}) {
            JsonNode value = body.get(field);
            if (value != null && value.isBoolean()) {
                return value.booleanValue();
            }
        }
        JsonNode data = body.get("data");
        if (data != null && data.isObject()) {
            return readEncrypted(data);
        }
        if (body.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = body.fields();
            while (fields.hasNext()) {
                JsonNode value = fields.next().getValue();
                if (value.isObject()) {
                    Boolean nested = readEncrypted(value);
                    if (nested != null) {
                        return nested;
                    }
                }
            }
        }
        return null;
    }
}
