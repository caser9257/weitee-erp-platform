package cn.weitee.erp.framework.common.util.json.databind;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TimestampLocalDateTimeDeserializerTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(LocalDateTime.class, TimestampLocalDateTimeDeserializer.INSTANCE);
        objectMapper.registerModule(module);
    }

    @Test
    void deserialize_shouldParseSpaceDatetimeString() throws Exception {
        LocalDateTime result = objectMapper.readValue("\"2026-08-14 08:00:00\"", LocalDateTime.class);
        assertEquals(LocalDateTime.of(2026, 8, 14, 8, 0, 0), result);
    }

    @Test
    void deserialize_shouldParseIsoDatetimeString() throws Exception {
        LocalDateTime result = objectMapper.readValue("\"2026-08-14T08:00:00\"", LocalDateTime.class);
        assertEquals(LocalDateTime.of(2026, 8, 14, 8, 0, 0), result);
    }

    @Test
    void deserialize_shouldParseMillisecondTimestamp() throws Exception {
        long ts = LocalDateTime.of(2026, 8, 14, 8, 0, 0)
                .atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
        LocalDateTime result = objectMapper.readValue(String.valueOf(ts), LocalDateTime.class);
        assertEquals(LocalDateTime.of(2026, 8, 14, 8, 0, 0), result);
    }

    @Test
    void deserialize_shouldRejectInvalidValue() {
        assertThrows(Exception.class, () -> objectMapper.readValue("\"not-a-date\"", LocalDateTime.class));
    }
}
