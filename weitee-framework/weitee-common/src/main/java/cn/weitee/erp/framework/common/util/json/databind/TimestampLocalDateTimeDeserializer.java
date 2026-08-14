package cn.weitee.erp.framework.common.util.json.databind;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * LocalDateTime 反序列化器
 * <p>
 * 兼容两种入参格式：
 * <ul>
 *   <li>毫秒时间戳（Long），如 1786602240000</li>
 *   <li>字符串，支持 {@code yyyy-MM-dd HH:mm:ss} 与 ISO 格式（{@code yyyy-MM-dd'T'HH:mm:ss}）</li>
 * </ul>
 * 解析失败时抛出明确异常，避免静默落入 epoch 0（1970-01-01）。
 */
public class TimestampLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

    public static final TimestampLocalDateTimeDeserializer INSTANCE = new TimestampLocalDateTimeDeserializer();

    private static final DateTimeFormatter DATETIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String text = p.getValueAsString();
        if (text == null || text.isEmpty()) {
            return null;
        }
        String trimmed = text.trim();
        // 1. 纯数字：毫秒时间戳
        if (trimmed.chars().allMatch(Character::isDigit)) {
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(trimmed)), ZoneId.systemDefault());
        }
        // 2. 字符串：优先空格格式，其次 ISO
        try {
            return LocalDateTime.parse(trimmed, DATETIME_FORMATTER);
        } catch (Exception e) {
            // ignore，继续尝试 ISO
        }
        try {
            return LocalDateTime.parse(trimmed, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception e) {
            throw new IOException("无法解析 LocalDateTime 字段，值：" + trimmed, e);
        }
    }

}
