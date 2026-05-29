package cn.iocoder.yudao.module.system.service.postlevel;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PostLevelClassifierTest {

    @Test
    void shouldResolveConfiguredLevelsByPostNameWhenOriginalLevelIsBlank() {
        assertEquals("\u9ad8\u7ea7", PostLevelClassifier.resolveLevel(null, "\u9879\u76ee\u7ba1\u7406"));
        assertEquals("\u9ad8\u7ea7", PostLevelClassifier.resolveLevel("", "\u9500\u552e\u603b\u76d1"));
        assertEquals("\u4e2d\u7ea7", PostLevelClassifier.resolveLevel(null, "\u91c7\u8d2d\u5de5\u7a0b\u5e08"));
        assertEquals("\u4e2d\u7ea7", PostLevelClassifier.resolveLevel("", "\u8f6f\u4ef6\u8c03\u8bd5\u5de5\u7a0b\u5e08"));
        assertEquals("\u521d\u7ea7", PostLevelClassifier.resolveLevel(null, "\u9879\u76ee\u52a9\u7406"));
        assertEquals("\u521d\u7ea7", PostLevelClassifier.resolveLevel("", "\u6d4b\u8bd5\u6280\u670d\u5458"));
        assertEquals("\u521d\u7ea7", PostLevelClassifier.resolveLevel("", "\u8f85\u52a9\u64cd\u4f5c\u5458 (\u5305\u88c5)"));
        assertEquals("\u672a\u5206\u7ea7", PostLevelClassifier.resolveLevel(null, "\u672a\u77e5\u5c97\u4f4d"));
    }

    @Test
    void shouldPreferExistingLevelWhenItIsProvided() {
        assertEquals("\u9ad8\u7ea7", PostLevelClassifier.resolveLevel("\u9ad8\u7ea7", "\u9879\u76ee\u52a9\u7406"));
        assertEquals("\u4e2d\u7ea7", PostLevelClassifier.resolveLevel("\u4e2d\u7ea7", "\u68c0\u9a8c\u5458"));
        assertEquals("\u521d\u7ea7", PostLevelClassifier.resolveLevel("\u521d\u7ea7", "\u9500\u552e\u603b\u76d1"));
    }

    @Test
    void shouldFallbackToPostNameWhenStoredLevelIsUnclassified() {
        assertEquals("\u9ad8\u7ea7", PostLevelClassifier.resolveLevel("\u672a\u5206\u7ea7", "\u9879\u76ee\u7ba1\u7406"));
        assertEquals("\u4e2d\u7ea7", PostLevelClassifier.resolveLevel("\u672a\u5206\u7ea7", "\u4f1a\u8ba1"));
        assertEquals("\u521d\u7ea7", PostLevelClassifier.resolveLevel("\u672a\u5206\u7ea7", "\u9879\u76ee\u52a9\u7406"));
        assertEquals("\u4e2d\u7ea7", PostLevelClassifier.resolveLevel("\u672a\u5206\u7ea7", "\u5fae\u7ec4\u88c5\u8c03\u6d4b\u8bd5\u5de5\u7a0b\u5e08"));
        assertEquals("\u521d\u7ea7", PostLevelClassifier.resolveLevel("\u672a\u5206\u7ea7", "\u7ea4\u710a\u64cd\u4f5c\u5458"));
    }
}
